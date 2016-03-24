package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ComponentCompletionHandler
    implements EditorMessageHandler<ComponentCompleteQuery>
{

    @Autowired
    private CompletionService completionService;

    @Override
    public void handle(MessageContext context, ComponentCompleteQuery msg) throws Exception
    {
        List<AceCompletion> completions = completionService.autocomplete(context.getRuntimeContext(),
            msg.getViewModel(), msg.getPath(), msg.getIndex());

        context.reply(msg, completions);
    }


    @Override
    public Class<ComponentCompleteQuery> getMessageType()
    {
        return ComponentCompleteQuery.class;
    }
}
