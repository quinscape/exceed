package de.quinscape.exceed.model;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;

/**
 * Visitor interface for all top-level models within an application.
 *
 * @param <I> additional input of the visitor methods
 * @param <O> output of the visitor methods
 */
public interface TopLevelModelVisitor<I, O>
{
    O visit(RoutingTable routingTable, I in);

    O visit(PropertyTypeModel propertyType, I in);

    O visit(Process process, I in);

    O visit(ApplicationConfig applicationModel, I in);

    O visit(View view, I in);

    O visit(DomainTypeModel domainType, I in);

    O visit(DomainVersion domainVersion, I in);

    O visit(EnumType enumType, I in);

    O visit(LayoutModel layoutModel, I in);

    O visit(DomainRule domainRule, I in);

    O visit(QueryTypeModel queryTypeModel);

    O visit(StateMachine stateMachine, I in);
}
