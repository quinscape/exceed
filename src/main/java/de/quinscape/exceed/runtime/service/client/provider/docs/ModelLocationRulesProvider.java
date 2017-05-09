package de.quinscape.exceed.runtime.service.client.provider.docs;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedDocsProvider;
import de.quinscape.exceed.runtime.view.ViewData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@ExceedDocsProvider
public class ModelLocationRulesProvider
    implements ClientStateProvider
{
    private final static Logger log = LoggerFactory.getLogger(ModelLocationRulesProvider.class);


    @Autowired
    public ModelLocationRulesProvider(ModelLocationRules modelLocationRules)
    {
        this.modelLocationRules = modelLocationRules;
    }


    public String getName()
    {
        return "modelLocations";
    }

    private final ModelLocationRules modelLocationRules;

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.APPLICATION;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws
        Exception
    {
        return new DefaultClientData(
            modelLocationRules.getRules()
                .stream()
                .filter( rule -> Model.getType(rule.getType()).getAnnotation(Internal.class) == null)
                .collect(Collectors.toList())
        );
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }
}
