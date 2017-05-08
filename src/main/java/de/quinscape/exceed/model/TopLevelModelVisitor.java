package de.quinscape.exceed.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;

public interface TopLevelModelVisitor<I, O>
{
    O visit(RoutingTable routingTable, I in);
    O visit(PropertyType propertyType, I in);
    O visit(Process process, I in);
    O visit(ApplicationConfig applicationModel, I in);
    O visit(View view, I in);
    O visit(DomainType domainType, I in);
    O visit(DomainVersion domainVersion, I in);
    O visit(EnumType enumType, I in);
    O visit(LayoutModel layoutModel, I in);

    TopLevelModel visit(DomainEditorViews domainEditorViews, Object o);
}
