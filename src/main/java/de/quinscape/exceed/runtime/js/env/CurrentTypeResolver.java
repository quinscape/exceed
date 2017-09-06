package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

public class CurrentTypeResolver
    implements PropertyTypeResolver
{
    public final static CurrentTypeResolver INSTANCE = new CurrentTypeResolver();

    private CurrentTypeResolver()
    {

    }

    @Override
    public PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext contextModel)
    {
        if (contextModel.getTopLevelModel() instanceof View)
        {
            View view = (View)contextModel.getTopLevelModel();
            final String domainType = view.getDomainType();
            if (domainType != null)
            {
                return DomainProperty.builder()
                    .withType(PropertyType.DOMAIN_TYPE, domainType)
                    .build();
            }
        }
        else if (contextModel.getTopLevelModel() instanceof Process)
        {
            final Process process = (Process) contextModel.getTopLevelModel();
            final ProcessState processState = (ProcessState)contextModel.getContextModel();
            final Transition transition = (Transition) contextModel.getFineDetail();

            final String sourceState = transition.getFrom();


            final ProcessState targetState = process.getStates().get(transition.getTo());

            String domainType = null;
            if (targetState instanceof ViewState)
            {
                final View view = context.getApplicationModel().getView(targetState.getScopeLocation());
                final String domainTypeFromTargetView = view.getDomainType();
                if (domainTypeFromTargetView != null)
                {
                    domainType = domainTypeFromTargetView;
                }
            }

            if (domainType == null && sourceState != null)
            {
                final ProcessState srcState = process.getStates().get(sourceState);
                if (srcState instanceof ViewState)
                {
                    final View view = context.getApplicationModel().getView(srcState.getScopeLocation());
                    final String domainTypeFromSourceView = view.getDomainType();
                    if (domainTypeFromSourceView != null)
                    {
                        domainType = domainTypeFromSourceView;
                    }
                }
            }


            return DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, domainType)
                .build();
        }

        throw new InvalidExpressionException("'current' only valid in views with domain type reference");
    }
}
