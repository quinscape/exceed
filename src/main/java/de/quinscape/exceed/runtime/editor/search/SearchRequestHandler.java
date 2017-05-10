package de.quinscape.exceed.runtime.editor.search;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.service.search.ModelSearchService;
import de.quinscape.exceed.runtime.service.websocket.EditorMessageHandler;
import de.quinscape.exceed.runtime.service.websocket.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SearchRequestHandler
    implements EditorMessageHandler<SearchRequest>
{
    private final ModelSearchService modelSearchService;


    @Autowired
    public SearchRequestHandler(ModelSearchService modelSearchService)
    {
        this.modelSearchService = modelSearchService;
    }


    @Override
    public void handle(MessageContext context, SearchRequest msg) throws Exception
    {
        final ApplicationModel applicationModel = context.getRuntimeContext().getApplicationModel();
        
        List<SearchResult> results = modelSearchService.search(applicationModel, msg.getSearchTerm());
        context.reply(msg, results);
    }


    @Override
    public Class<SearchRequest> getMessageType()
    {
        return SearchRequest.class;
    }
}
