package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorDomainService
    implements EditorMessageHandler<DomainTypeQuery>
{
    @Override
    public void handle(MessageContext context, DomainTypeQuery msg) throws Exception
    {
        RuntimeApplication runtimeApplication = context.getRuntimeContext().getRuntimeApplication();
        ApplicationModel applicationModel = runtimeApplication.getApplicationModel();

        Map<String, Object> data = new HashMap<>();

        data.put("domainTypes",  createModelDataList(applicationModel.getDomainType("DomainTypeModel"), applicationModel.getDomainTypes()));
        data.put("enums", createModelDataList(applicationModel.getDomainType("EnumTypeModel"), applicationModel.getEnums()));
        data.put("propertyTypes", applicationModel.getPropertyTypes());
        data.put("domainLayout", applicationModel.getDomainLayout());

        context.reply(msg, data);
    }

    private DataGraph createModelDataList(DomainType enumTypeModel, Map<String, ?> map)
    {
        ArrayList<?> rows = new ArrayList<>(map.values());
        return new DataGraph(mapProperties(enumTypeModel), rows, rows.size());
    }

    private Map<String, DomainProperty> mapProperties(DomainType domainTypeModel)
    {
        Map<String, DomainProperty> map = new HashMap<>();
        for (DomainProperty domainProperty : domainTypeModel.getProperties())
        {
            map.put(domainProperty.getName(), domainProperty);
        }
        return map;
    }

    @Override
    public Class<DomainTypeQuery> getMessageType()
    {
        return DomainTypeQuery.class;
    }
}
