package de.quinscape.exceed.runtime.service.client.provider.editor;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.DataGraphQualifier;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.ExceedEditorProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
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

    public String getName()
    {
        return "editor";
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
        data.include("domainType", DomainType.class, applicationModel.getDomainTypes());
        data.include("enumType", EnumType.class, applicationModel.getEnums());
        data.include("process", Process.class, applicationModel.getProcesses());
        data.include("view", View.class, applicationModel.getViews());
        data.include("layout", LayoutModel.class, applicationModel.getLayouts());

        final DataGraph dataGraph = new DataGraph(data.columns, data.root, 1, DataGraphQualifier.EDITOR);
        final DomainService domainService = runtimeContext.getDomainService();

        return new JSONData(domainService.toJSON(dataGraph));
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


        public void include(String name, TopLevelModel topLevelModel)
        {
            include(name, topLevelModel.getClass(), DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE);
            root.put(name, topLevelModel);
        }


        public void include(String name, Class<? extends TopLevelModel> cls, Map<String, ? extends TopLevelModel> map)
        {

            include(name, cls, DomainProperty.MAP_PROPERTY_TYPE);
            root.put(name, map);
        }


        private void include(String name, Class<? extends TopLevelModel> cls, String propertyType)
        {
            columns.put(
                name,
                DomainProperty.builder()
                    .withName(name)
                    .withType(propertyType)
                    .withTypeParam(Model.getType(cls))
                    .build()
            );
        }
    }
}
