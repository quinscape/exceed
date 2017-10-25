package de.quinscape.exceed.runtime.service.client.provider.editor;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.translation.TranslationEditorState;
import de.quinscape.exceed.model.translation.TranslationEntry;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.DataGraphQualifier;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.TranslationProvider;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.view.ViewData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ExceedEditorProvider
public class EditorDataProvider
    implements ClientStateProvider
{
    private final static Logger log = LoggerFactory.getLogger(EditorDataProvider.class);

    private final DomainProperty translationProperty;


    public String getName()
    {
        return "editor";
    }

    private final TranslationProvider translationProvider;


    @Autowired
    public EditorDataProvider(TranslationProvider translationProvider)
    {
        this.translationProvider = translationProvider;
        translationProperty = DomainProperty.builder()
            .withName("translation")
            .withType(PropertyType.MAP, "TranslationEntry")
            .build();
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.MODEL_VERSION;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        log.debug("(Re)creating editor data");

        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final EditorData data = new EditorData();

        data.include("config", applicationModel.getConfigModel());
        data.include("routing", applicationModel.getRoutingTable());
        data.include("domainType", DomainTypeModel.class, filter(applicationModel.getDomainTypes(), DomainTypeModel.class));
        data.include("queryType", QueryTypeModel.class, filter(applicationModel.getDomainTypes(), QueryTypeModel.class));
        data.include("enumType", EnumType.class, applicationModel.getEnums());
        data.include("process", Process.class, applicationModel.getProcesses());
        data.include("view", View.class, applicationModel.getViews());
        data.include("layout", LayoutModel.class, applicationModel.getLayouts());
        data.include("rule", DomainRule.class, applicationModel.getDomainRules());

        final TranslationEditorState i18nState = new TranslationEditorState(runtimeContext,
            translationProvider);
        data.include("translation", TranslationEntry.class, i18nState.getTranslations());

        final DataGraph dataGraph = new DataGraph(data.columns, data.root, 1, DataGraphQualifier.EDITOR);
        final DomainService domainService = runtimeContext.getDomainService();

        return new JSONData(domainService.toJSON(dataGraph));
    }


    private <T> Map<String, T> filter(Map<String, DomainType> domainTypes, Class<T> modelClass)
    {
        Map<String, T> map = Maps.newHashMapWithExpectedSize(domainTypes.size());

        for (Map.Entry<String, DomainType> e : domainTypes.entrySet())
        {
            final DomainType value = e.getValue();

            if (value.getClass().equals(modelClass))
            {
                map.put(e.getKey(), (T) value);
            }
        }
        return map;
    }


    @Override
    public boolean isMutable()
    {
        return true;
    }


    private static class EditorData
    {

        public final Map<String, DomainProperty> columns;
        public final Map<String, Object> root;


        private EditorData()
        {
            root = new HashMap<>();
            columns = new HashMap<>();
        }


        public void include(String name, Object topLevelModel)
        {
            include(name, topLevelModel.getClass(), PropertyType.DOMAIN_TYPE);
            root.put(name, topLevelModel);
        }


        public void include(String name, Class<?> cls, Map<String, ?> map)
        {

            include(name, cls, PropertyType.MAP);
            root.put(name, map);
        }

        public void include(String name, DomainProperty column, Object value)
        {
            columns.put(name, column);
            root.put(name, value);
        }


        private void include(String name, Class<?> cls, String propertyType)
        {
            columns.put(
                name,
                DomainProperty.builder()
                    .withName(name)
                    .withType(propertyType, Model.getType(cls))
                    .build()
            );
        }
    }
}
