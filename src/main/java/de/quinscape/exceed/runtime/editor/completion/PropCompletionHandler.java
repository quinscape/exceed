package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PropCompletionHandler
    implements EditorMessageHandler<PropCompleteQuery>
{

    @Autowired
    private CompletionService completionService;

    @Override
    public void handle(MessageContext context, PropCompleteQuery msg) throws Exception
    {
        List<AceCompletion> completions = completionService.autocompleteProp(context.getRuntimeApplication(),
            msg.getPropName(), msg.getViewModel(), msg.getPath());

        context.reply(msg, completions);
    }


    @Override
    public Class<PropCompleteQuery> getMessageType()
    {
        return PropCompleteQuery.class;
    }
}
