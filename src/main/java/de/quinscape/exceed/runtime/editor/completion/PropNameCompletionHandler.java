package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PropNameCompletionHandler
    implements EditorMessageHandler<PropNameCompleteQuery>
{

    @Autowired
    private CompletionService completionService;

    @Override
    public void handle(MessageContext context, PropNameCompleteQuery msg) throws Exception
    {
        List<AceCompletion> completions = completionService.autocompletePropName(context.getRuntimeApplication(), msg.getViewModel(), msg.getPath());

        context.reply(msg, completions);
    }


    @Override
    public Class<PropNameCompleteQuery> getMessageType()
    {
        return PropNameCompleteQuery.class;
    }
}
