package de.quinscape.exceed.model.config;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedCollection;
import de.quinscape.exceed.model.context.ContextModel;
import org.springframework.util.StringUtils;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Encapsulates general application configuration. Corresponds to the "/models/config.json" resource.
 */
public class ApplicationConfig
    extends AbstractTopLevelModel
{
    private final static Map<String,String> DEFAULT_USERS = ImmutableMap.of(
        "admin", "ROLE_ADMIN,ROLE_EDITOR,ROLE_USER",
        "editor", "ROLE_EDITOR,ROLE_USER",
        "user", "ROLE_USER"
    );

    public static final int DECIMAL_PLACES_DEFAULT = 3;

    private List<String> supportedLocales = Collections.singletonList("en_US");

    private Map<String, String> supportedLocalesMap = createLookup(supportedLocales);

    private List<String> styleSheets;

    private String schema = "public";

    private ContextModel applicationContextModel;

    private ContextModel sessionContextModel;

    private String defaultLayout = "Standard";

    private DecimalConfig decimalConfig = new DecimalConfig();

    private ComponentConfig componentConfig = new ComponentConfig();

    private String defaultCurrency = "EUR";

    private String authSchema;

    private Map<String,String> defaultUsers = DEFAULT_USERS;


    /**
     * Database schema for this application
     *
     * @return
     */
    public String getSchema()
    {
        return schema;
    }


    public void setSchema(String schema)
    {
        if (schema == null)
        {
            throw new IllegalArgumentException("schema can't be null");
        }

        this.schema = schema;
    }

    /**
     * Stylesheets resource paths for this application
     *
     * @return
     */
    @JSONTypeHint(String.class)
    public List<String> getStyleSheets()
    {
        return styleSheets;
    }


    public void setStyleSheets(List<String> styleSheets)
    {
        this.styleSheets = styleSheets;
    }


    /**
     * The application context for this application. Each property will exist once per application.
     */
    public ContextModel getApplicationContextModel()
    {
        return applicationContextModel;
    }


    @JSONProperty("applicationContext")
    public void setApplicationContextModel(ContextModel applicationContextModel)
    {
        this.applicationContextModel = applicationContextModel;
    }


    /**
     * The session context model. Each property in it will exist once per user session.
     */
    public ContextModel getSessionContextModel()
    {
        return sessionContextModel;
    }


    @JSONProperty("sessionContext")
    public void setSessionContextModel(ContextModel sessionContext)
    {
        this.sessionContextModel = sessionContext;
    }


    /**
     * Array of normalized locale specifications to support for this application.
     */
    public List<String> getSupportedLocales()
    {
        if (supportedLocales == null)
        {
            return Collections.emptyList();
        }
        return supportedLocales;
    }


    /**
     * Returns the language part of the given locale code
     *
     * @param code
     * @return
     */
    private String languagePart(String code)
    {
        final int pos = code.indexOf('-');
        if (pos >= 0)
        {
            return code.substring(0, pos);
        }
        return code;
    }


    public void setSupportedLocales(List<String> supportedLocales)
    {
        if (supportedLocales == null || supportedLocales.size() == 0)
        {
            throw new IllegalStateException("Supported locale list can't be empty or null");
        }

        this.supportedLocalesMap = createLookup(supportedLocales);
        this.supportedLocales = supportedLocales;
    }


    private ImmutableMap<String, String> createLookup(List<String> supportedLocales)
    {
        Map<String, String> map = new HashMap<>();

        for (String supportedLocale : supportedLocales)
        {
            map.put(supportedLocale, supportedLocale);

            String lang = languagePart(supportedLocale);
            map.putIfAbsent(lang, supportedLocale);
        }

        return ImmutableMap.copyOf(map);
    }


    /**
     * Matches the given locale with the existing application locales and returns the most favorable.
     * <p>
     * If there is no full match, the matching falls back on language only matching, if that produces no result, the
     * first supported locale defined in the application is returned.
     * </p>
     *
     * @param locale locale
     * @return supported application locale code
     */
    public String matchLocale(Locale locale)
    {
        final String country = locale.getCountry();
        String code;
        if (StringUtils.hasText(country))
        {
            code = locale.getLanguage() + "-" + country;
        }
        else
        {
            code = locale.getLanguage();
        }
        final String result = supportedLocalesMap.get(code);
        if (result != null)
        {
            return result;
        }
        return supportedLocales.get(0);
    }


    /**
     * Returns the default layout for this application.
     *
     * @return
     */
    public String getDefaultLayout()
    {
        return defaultLayout;
    }


    public void setDefaultLayout(String defaultLayout)
    {
        if (defaultLayout == null)
        {
            throw new IllegalArgumentException("defaultLayout can't be null");
        }

        this.defaultLayout = defaultLayout;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
    
    public DecimalConfig getDecimalConfig()
    {
        return decimalConfig;
    }


    public void setDecimalConfig(DecimalConfig decimalConfig)
    {
        this.decimalConfig = decimalConfig;
    }


    public ComponentConfig getComponentConfig()
    {
        return componentConfig;
    }


    public void setComponentConfig(ComponentConfig componentConfig)
    {
        this.componentConfig = componentConfig;
    }


    /**
     * Default currency for the application (three letter code)
     * @return
     */
    public String getDefaultCurrency()
    {
        return defaultCurrency;
    }


    public void setDefaultCurrency(String defaultCurrency)
    {
        if (defaultCurrency == null)
        {
            throw new IllegalArgumentException("defaultCurrency can't be null");
        }

        this.defaultCurrency = defaultCurrency;
    }


    /**
     * Schema to store the application users for this app in. Defaults to the same as the schema property.
     *
     * Can be used to have multiple applications that share the user pool.
     * 
     */
    public String getAuthSchema()
    {
        return authSchema != null ? authSchema : schema;
    }


    public void setAuthSchema(String authSchema)
    {
        this.authSchema = authSchema;
    }


    /**
     * Configuration for the default user accounts. Maps user names to a set of role names.
     *
     */
    @DocumentedCollection(
        keyDesc = "userName",
        valueDesc = "Set of role string"
    )
    public Map<String, Set<String>> getDefaultUsers()
    {
        return defaultUsers;
    }


    public void setDefaultUsers(Map<String, Set<String>> defaultUsers)
    {
        this.defaultUsers = defaultUsers;
    }
}
