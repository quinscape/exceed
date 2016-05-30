package de.quinscape.exceed.runtime.editor.domain;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.ColumnDescriptor;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditorDomainService
    implements EditorMessageHandler<DomainTypeQuery>
{

    private Map<String, DomainType> types;
    private Map<String, ColumnDescriptor> columns;

    public EditorDomainService()
    {
        types = createDomainModelTypes();
        columns = createColumnDescriptors();
    }


    @Override
    public void handle(MessageContext context, DomainTypeQuery msg) throws Exception
    {
        RuntimeApplication runtimeApplication = context.getRuntimeContext().getRuntimeApplication();

        Map<String, Object> data = new HashMap<>();

        ApplicationModel applicationModel = runtimeApplication.getApplicationModel();
        data.put("domainTypes",  createDomainDataList(applicationModel.getDomainTypes()));
        data.put("propertyTypes", applicationModel.getPropertyTypes());
        data.put("domainLayout", applicationModel.getDomainLayout());
        data.put("enums", applicationModel.getEnums());

        context.reply(msg, data);
    }


    private DataList createDomainDataList(Map<String, DomainType> domainTypes)
    {
        ArrayList<DomainType> rows = new ArrayList<>(domainTypes.values());
        return new DataList(types, columns, rows, rows.size());
    }


    private Map<String, ColumnDescriptor> createColumnDescriptors()
    {
        Map<String, ColumnDescriptor> map = new HashMap<>();

        map.put("name", new ColumnDescriptor("DomainType", "name"));
        map.put("properties", new ColumnDescriptor("DomainType", "properties"));
        return map;
    }


    private Map<String, DomainType> createDomainModelTypes()
    {
        HashMap<String, DomainType> map = new HashMap<>();

        DomainType type = new DomainType();
        type.setName("DomainType");
        type.setProperties(Arrays.asList(
            new DomainProperty("name", "PlainText", null, true),
            new DomainProperty("properties", "List", null, true, "DomainProperty", 0)
        ));
        type.setPkFields(Collections.singletonList("name"));
        map.put("DomainType", type);

        DomainType domainPropertyType = new DomainType();
        domainPropertyType.setName("DomainProperty");
        domainPropertyType.setProperties(Arrays.asList(
            new DomainProperty("name", "PlainText", null, true),
            new DomainProperty("type", "PlainText", null, true),
            new DomainProperty("typeParam", "Object", null, true),
            new DomainProperty("defaultValue", "Object", null, true),
            new DomainProperty("required", "Boolean", null, true),
            new DomainProperty("maxLength", "Integer", null, true),
            new DomainProperty("data", "Object", null, true)
        ));
        type.setPkFields(Collections.singletonList("name"));

        map.put("DomainProperty", domainPropertyType);

        return map;
    }


    @Override
    public Class<DomainTypeQuery> getMessageType()
    {
        return DomainTypeQuery.class;
    }
}
