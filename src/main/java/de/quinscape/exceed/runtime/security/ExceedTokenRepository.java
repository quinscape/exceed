package de.quinscape.exceed.runtime.security;

/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.DBUtil;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.ServletContext;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Exceed specific implementation of the {@link PersistentTokenRepository} interface.
 * <p>
 * Stores persistent tokens in the AppLogins type of the default application.
 */
public class ExceedTokenRepository
    implements PersistentTokenRepository
{
    public static final String PROPERTY_USERNAME = "username";

    public static final String PROPERTY_SERIES= "series";

    public static final String PROPERTY_TOKEN = "token";

    public static final String PROPERTY_LAST_USED = "lastUsed";

    private final ServletContext servletContext;

    private final ApplicationService applicationService;


    public ExceedTokenRepository(ServletContext servletContext, ApplicationService applicationService)
    {
        this.servletContext = servletContext;
        this.applicationService = applicationService;
    }


    public void createNewToken(PersistentRememberMeToken token)
    {

        final RuntimeApplication app = getDefaultApplication();
        final RuntimeContext runtimeContext = app.createSystemContext();

        final GenericDomainObject domainObject = (GenericDomainObject) runtimeContext.getDomainService().create(
            runtimeContext,
            AppAuthentication.LOGINS_TYPE,
            UUID.randomUUID().toString()
        );

        domainObject.setProperty(PROPERTY_USERNAME, token.getUsername());
        domainObject.setProperty(PROPERTY_SERIES, token.getSeries());
        domainObject.setProperty(PROPERTY_TOKEN, token.getTokenValue());
        domainObject.setProperty(PROPERTY_LAST_USED, new Timestamp(token.getDate().getTime()));

        domainObject.insert(runtimeContext);
    }


    private RuntimeApplication getDefaultApplication()
    {
        return applicationService.getRuntimeApplication(servletContext, applicationService.getDefaultApplication());
    }


    public void updateToken(String series, String tokenValue, Date lastUsed)
    {
        final RuntimeApplication app = getDefaultApplication();
        final RuntimeContext runtimeContext = app.createSystemContext();

        final DomainService domainService = runtimeContext.getDomainService();
        final DomainObject domainObject = DomainUtil.queryOne(
            runtimeContext,
            AppAuthentication.LOGINS_TYPE,
            DBUtil.jooqField(domainService, AppAuthentication.LOGINS_TYPE, PROPERTY_SERIES)
                .eq(
                    series
                )
        );

        if (domainObject == null)
        {
            throw new IllegalStateException("No domain object found for series: " + series);
        }

        domainObject.setProperty(PROPERTY_TOKEN, tokenValue);
        domainObject.setProperty(PROPERTY_LAST_USED, new Timestamp(lastUsed.getTime()));

        domainObject.getDomainService().update(runtimeContext, domainObject);
    }


    /**
     * Loads the token data for the supplied series identifier.
     * <p>
     * If an error occurs, it will be reported and null will be returned (since the result
     * should just be a failed persistent login).
     *
     * @param seriesId
     * @return the token matching the series, or null if no match found or an exception
     * occurred.
     */
    public PersistentRememberMeToken getTokenForSeries(String seriesId)
    {
        final RuntimeApplication app = getDefaultApplication();
        final RuntimeContext runtimeContext = app.createSystemContext();

        final DomainService domainService = runtimeContext.getDomainService();
        final DomainObject domainObject = DomainUtil.queryOne(
            runtimeContext,
            AppAuthentication.LOGINS_TYPE,
            DBUtil.jooqField(domainService, AppAuthentication.LOGINS_TYPE, PROPERTY_SERIES)
                .eq(
                    seriesId
                )
        );

        if (domainObject == null)
        {
            return null;
        }

        return new PersistentRememberMeToken(
            (String) domainObject.getProperty(PROPERTY_USERNAME),
            (String) domainObject.getProperty(PROPERTY_SERIES),
            (String) domainObject.getProperty(PROPERTY_TOKEN),
            new Date(((Timestamp) domainObject.getProperty(PROPERTY_LAST_USED)).getTime())
        );
    }


    public void removeUserTokens(String username)
    {

        final RuntimeApplication app = getDefaultApplication();
        final RuntimeContext runtimeContext = app.createSystemContext();

        final DomainService domainService = runtimeContext.getDomainService();
        final List<DomainObject> domainObject = DomainUtil.query(
            runtimeContext,
            AppAuthentication.LOGINS_TYPE,
            DBUtil.jooqField(domainService, AppAuthentication.LOGINS_TYPE, PROPERTY_USERNAME)
                .eq(
                    username
                )
        );

        for (DomainObject object : domainObject)
        {
            object.getDomainService().delete(runtimeContext, object);
        }
    }
}
