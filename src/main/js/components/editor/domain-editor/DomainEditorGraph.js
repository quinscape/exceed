const React = require("react");
const immutableUpdate = require("react-addons-update");
const assign = require("object-assign");

import sys from "../../../sys";

const ValueLink = require("../../../util/value-link");
const hub = require("../../../service/hub");

import GUIContext from "../../../editor/gui/gui-context"
import GUIContainer from "../../../editor/gui/GUIContainer"
import GUIElement from "../../../editor/gui/GUIElement"
import UIState from "../../../editor/gui/ui-state";

const values = require("../../../util/values");

const MIN_EASE_INITIAL = 40;
const MIN_EASE_UPDATE = 20;

const PROP_NAME_COLOR = "#338";

const ArrowComponent = require("../../../gfx/arrow");
import Handle from "../../../gfx/Handle";

const DomainLayout = require("./domain-layout");
const DomainGraph = require("./domain-graph");
const NodeType = DomainGraph.NodeType;

class Entity extends React.Component
{
    onUpdate()
    {
        this.forceUpdate();
    }

    onInteraction(pos)
    {
        var layout = this.props.position;

        //console.log("ENTITY click at", pos.x, pos.y);

        var yStart = -layout.height/2 + layout.headerHeight + layout.lineHeight;

        var line = ((pos.y - yStart) / layout.lineHeight)|0;
        if (line < 0 || line >= this.props.model.properties.length)
        {
            line = null;
        }

        //console.log("INTERACT", line);
        GUIContext.focus(this.props.model.name);
        this.props.onInteraction && this.props.onInteraction.call(null, this.props.model.name, line);
    }

    shouldComponentUpdate(nextProps, nextState)
    {
        return (
            this.props.position !== nextProps.position ||
            this.props.model !== nextProps.model
        );
    }

    render()
    {
        try
        {
            var domainProperty, name;

            var model = this.props.model;
            var properties = model.properties;

            var elementId = model.name;
            var layout = this.props.position;

            //console.log(JSON.stringify(layout));


            var uiState = GUIContext.getElementState(elementId, UIState.NORMAL);

            const entityBorderColor = uiState === UIState.FOCUSED ? "#ffbf64" : "#555";
            const entityFillColor = uiState === UIState.FOCUSED ? "#e3ded6" : "#ddd";

            //  console.log("Render Entity: ", model.name);

            var x = layout.x - layout.width/2;
            var y = layout.y - layout.height/2;

            //console.log("Entity '" +  elementId + "': " + x + "," + y + " to " + (x + layout.width) + "," + (y + layout.height));

            var xPos = x + 10;
            var yPos = y + layout.headerHeight;

            var separatorHeight = (yPos + 8 );

            var headingColor = uiState === UIState.FOCUSED ? "#8a7557" : "#444";
            var texts = [
                <path key="sep" d={ "M" + (xPos - 10) + "," + separatorHeight + " L" + (x + layout.width) + "," + separatorHeight } stroke={ entityBorderColor } />,
                <text key="h" x={ xPos } y={ yPos } fontSize={ DomainLayout.getLargeFontSize() } fill={ headingColor }>
                    { elementId }
                </text>
            ];

            yPos += layout.lineHeight * 1.75;

            var decoratorOffset = layout.nameWidth + (layout.width - layout.nameWidth) / 2;

            for (var i = 0; i < properties.length; i++)
            {
                domainProperty = properties[i];
                name = domainProperty.name;

                texts.push(
                    <text
                        key={ name }
                        x={ xPos }
                        y={ yPos }
                        fontSize={ DomainLayout.getNormalFontSize() }
                        fill={ PROP_NAME_COLOR }
                    >
                        { "+ " + name }
                    </text>,

                    <text
                        key={ name + "deco" }
                        x={ xPos + decoratorOffset }
                        y={ yPos }
                        fontSize={ DomainLayout.getNormalFontSize() }
                        fill={ DomainLayout.getColorForType(domainProperty.type) }
                        textAnchor="middle"
                    >
                        { DomainLayout.getLabelForType(domainProperty.type) }
                    </text>
                );

                yPos += DomainLayout.getPropLayout().height;
            }

            return (
                <GUIElement
                    id={ elementId }
                    className="entity"
                    position={ this.props.position }
                    updatePosition={ this.props.updatePosition }
                    onInteraction={ this.onInteraction }
                    onUpdate={ this.onUpdate }
                >
                    <rect
                        x={ x }
                        y={ y }
                        width={ layout.width }
                        height={ layout.height }
                        stroke={ entityBorderColor }
                        fill={ entityFillColor }
                        rx="4"
                        ry="4"
                    />
                    { texts }
                </GUIElement>
            );
        }
        catch(e)
        {
            console.error("Error rendering Entity", e);
        }
    }
};


class DomainEditorGraph extends React.Component
{
    constructor(props)
    {
        super(props);
        var positions = this.props.positionsLink.value;

        var graph = DomainGraph.create(
            positions,
            this.props.domainTypes,
            this.props.visible,
            MIN_EASE_INITIAL
        );

        var aabb = graph.getAABB();

        this.state =  {
            graph: graph,
            centerX: (aabb.minX + aabb.maxX) / 2,
            centerY: (aabb.minY + aabb.maxY) / 2,
            positions: positions
        };

    }

    componentWillReceiveProps(nextProps)
    {
        if (
            this.props.domainTypes !== nextProps.domainTypes ||
            this.props.visible !== nextProps.visible ||
            this.props.relationLookup !== nextProps.relationLookup ||
            this.props.positionsLink.value !== nextProps.positionsLink.value
        )
        {
//            console.log("RELAYOUT");

            var positions = nextProps.positionsLink.value;
            var newGraph = DomainGraph.create(
                positions,
                nextProps.domainTypes,
                nextProps.visible,
                MIN_EASE_UPDATE
            );

            this.setState({
                graph: newGraph,
                positions: positions
            });
        }
    }

    shouldComponentUpdate(nextProps, nextState)
    {
        return (
            this.state.graph !== nextState.graph ||
            this.props.height !== nextProps.height ||
            this.props.domainTypes !== nextProps.domainTypes ||
            this.props.visible !== nextProps.visible ||
            this.props.positionsLink.value !== nextProps.positionsLink.value
        );
    }

    onLayoutChange(layout, permanent)
    {
        var graph = this.state.graph;

        var name = layout.name;
        var node = graph.lookup[name];
        if (node)
        {
            var newNodes = immutableUpdate(graph.nodes, {
                [node.id]: {
                    $apply: n => {
                        let copy = assign({}, n);
                        copy.x = layout.x;
                        copy.y = layout.y;
                        return copy;
                    }
                }
            });

            var newGraph = graph.copy(newNodes);
            this.setState({graph: newGraph});
        }

        if (permanent)
        {

            var link = this.props.positionsLink;
            const haveExistingLayout = !!link.value;

            if (node.type === NodeType.ENTITY)
            {
                var nodes = graph.nodes;
                var changedId = node.id;

                // FK nodes should be all at the end of the nodes array

                var index = nodes.length - 1;
                var fkNode;

                var changedFkNodes = [];
                while ( (fkNode = nodes[index]).type === NodeType.FK)
                {

                    var srcId = fkNode.srcId;
                    var dstId = fkNode.dstId;
                    // does the transition for the FK node touch the currently moved entity node?
                    if (srcId === changedId || dstId === changedId)
                    {
                        // yes -> recalculate position
                        DomainGraph.setFKNodePosition(nodes[srcId], nodes[dstId], fkNode);
                        if (haveExistingLayout)
                        {
                            changedFkNodes.push(fkNode);
                        }
                    }
                    index--;
                }
            }

            if (!haveExistingLayout)
            {
                var fullLayout = {};
                var lookup = graph.lookup;
                for (var n in lookup)
                {
                    if (lookup.hasOwnProperty(n))
                    {
                        var v = lookup[n];
                        fullLayout[n] = {
                            x: v.x,
                            y: v.y
                        }
                    }
                }
                link.requestChange(fullLayout);
            }
            else
            {
                var spec = {
                    [name] : { $set: {
                        x: layout.x,
                        y: layout.y
                    } }
                };

                for (var i = 0; i < changedFkNodes.length; i++)
                {
                    fkNode = changedFkNodes[i];
                    spec[fkNode.name] = {
                        $set: {
                            x: fkNode.x,
                            y: fkNode.y
                        }
                    };
                }

                link.requestChange( immutableUpdate(link.value, spec));
            }
        }
    }

    render()
    {
        var domainTypes = this.props.domainTypes;
        var nodes = this.state.graph.nodes;

        var entities = [];
        var relations = [];

        for (var i = 0; i < nodes.length; i++)
        {
            var node = nodes[i];
            var name = node.name;
            if (node.type === NodeType.ENTITY)
            {
                entities.push(
                    <Entity
                        key={ name }
                        model={ domainTypes[name] }
                        onInteraction={ this.props.onInteraction }
                        position={ node }
                        updatePosition={this.onLayoutChange }
                    />
                );
            }
            else
            {
                var srcNode = nodes[node.srcId];
                var dstNode = nodes[node.dstId];

                var x0 = srcNode.x + ( dstNode.x < srcNode.x ? -srcNode.width/2 : srcNode.width/2 );
                var y0 = srcNode.y - srcNode.height/2 + srcNode.headerHeight + srcNode.headerHeight * 0.75 + node.srcPropIndex * DomainLayout.getPropLayout().height + DomainLayout.getPropLayout().height / 2;

                var x2 = dstNode.x + ( dstNode.x >= srcNode.x ? -dstNode.width/2 : dstNode.width/2 );
                var y2 = dstNode.y - dstNode.height/2 + dstNode.headerHeight + dstNode.headerHeight * 0.75 + node.dstPropIndex * DomainLayout.getPropLayout().height + DomainLayout.getPropLayout().height/2;

                var key = "h_" + name;

                relations.push(
                    <ArrowComponent
                        key={ name }
                        x0={ x0 }
                        y0={ y0 }
                        x1={ node.x }
                        y1={ node.y }
                        x2={ x2 }
                        y2={ y2 }
                    />,

                    <Handle
                        id={ key }
                        key={ key }
                        position={ node }
                        updatePosition={ this.onLayoutChange }
                    />
                );
            }
        }

        return (
            <GUIContainer style={{ backgroundColor: "#f0f0f0" }} height={this.props.height} centerX={ this.state.centerX } centerY={ this.state.centerY }>
                { relations }
                { entities }
            </GUIContainer>
        );
    }
};

export default DomainEditorGraph
