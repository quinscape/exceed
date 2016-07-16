package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.service.ActionExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.util.ArrayList;
import java.util.List;

public class SyslogAction
    implements Action<SyslogActionModel>, ServerActionConverter<SyslogActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(SyslogAction.class);


    @Override
    public Object execute(RuntimeContext runtimeContext, SyslogActionModel model)
    {
        if (log.isInfoEnabled())
        {
            log.info(JSON.defaultJSON().forValue(model.getArgs()));
        }

        return true;
    }


    @Override
    public Class<SyslogActionModel> getActionModelClass()
    {
        return SyslogActionModel.class;
    }


    @Override
    public SyslogActionModel createModel(ExpressionContext<ActionExecutionEnvironment> ctx, ASTFunction node)
    {

        SyslogActionModel model = new SyslogActionModel();
        List<Object> args = new ArrayList<>();

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            args.add(node.jjtGetChild(i).jjtAccept(ctx.getEnv(), null));
        }

        model.setArgs(args);
        return model;
    }
}
