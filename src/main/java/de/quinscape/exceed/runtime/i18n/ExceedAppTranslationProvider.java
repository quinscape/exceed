package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.util.DomainUtil;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Transactional
public class ExceedAppTranslationProvider
    implements TranslationProvider
{
    public List<AppTranslation> provideTranslations(RuntimeContext runtimeContext)
    {
        final DomainService domainService = runtimeContext.getDomainService();
        final DomainOperations ops = domainService.getDataSource(
            domainService.getDomainType(TRANSLATION_TYPE).getDataSourceName()
        ).getStorageConfiguration().getDomainOperations();

        final DomainType translationTypeModel = runtimeContext.getDomainService().getDomainType(TRANSLATION_TYPE);
        final QueryDomainType queryDomainType = new QueryDomainType(
            translationTypeModel
        );
        DomainUtil.selectDomainTypeFields(queryDomainType, translationTypeModel);

        QueryDefinition queryDefinition = new QueryDefinition(
            queryDomainType
        );

        queryDefinition.setOrderBy(Collections.singletonList(PROPERTY_TAG));

        final DataGraph graph = ops.query(runtimeContext, runtimeContext.getDomainService(), queryDefinition);


        List<AppTranslation> list = new ArrayList<>(graph.getCount());

        for (DomainObject domainObject : (List<DomainObject>) graph.getRootCollection())
        {
            final AppTranslation instance = new AppTranslation();

            for (DomainProperty domainProperty : translationTypeModel.getProperties())
            {
                final String propertyName = domainProperty.getName();
                JSONUtil.DEFAULT_UTIL.setProperty(
                    instance,
                    propertyName,
                    domainObject.getProperty(propertyName)
                );
            }

            list.add(instance);
        }

        return list;
    }

}
