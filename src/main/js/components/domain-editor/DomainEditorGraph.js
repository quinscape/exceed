const sys = require("../../sys");

const React = require("react");

const classes = require("classnames");
const ValueLink = require("../../util/value-link");
const hub = require("../../service/hub");

const GUIContext = require("./../../editor/gui/gui-context");
const GUIContainer = require("./../../editor/gui/GUIContainer");
const GUIElement = require("./../../editor/gui/GUIElement");
const UIState = require("./../../editor/gui/ui-state");

const svgLayout = require("../../gfx/svg-layout");

const immutableUpdate = require("react-addons-update");

const TextSize = svgLayout.TextSize;

const assign = require("object-assign");
const values = require("../../util/values");

const SIZE = 160;

const MIN_EASE_INITIAL = 60;
const MIN_EASE_UPDATE = 20;

const SEPARATION_LIMIT = SIZE;
const EDGE_LIMIT = SIZE * 1.5;

const TENSION_LIMIT = Math.pow(SIZE, 0.8);

const Graph = require("../../gfx/graph");

const ArrowComponent = require("../../gfx/arrow");
const Control = require("../../gfx/Control");

function findPropertyIndexWithName(type, name)
{
    var properties = type.properties;
    for (var i = 0; i < properties.length; i++)
    {
        var domainProperty = properties[i];
        if (domainProperty.name === name)
        {
            return i;
        }
    }

    return null;
}

function addToGraph(graph, domainType)
{
    var domainProperty;

    var width = HEADING_LAYOUT.ppc * domainType.name.length;
    var height = HEADING_LAYOUT.height;
    var nameWidth = 0;

    var properties = domainType.properties;

    for (var i = 0; i < properties.length; i++)
    {
        domainProperty = properties[i];
        var propertyName = domainProperty.name;

        height += PROP_LAYOUT.height;
        var nameWidthEstimate = PROP_LAYOUT.ppc * propertyName.length;
        var decoration = DECORATION_TEXTS[domainProperty.type];
        if (!decoration)
        {
            throw new Error("No decoration for type'" + domainProperty.type + "'");
        }

        var decoratorWidthEstimate = PROP_LAYOUT.ppc * decoration.length;

        width = Math.max(width, nameWidthEstimate + decoratorWidthEstimate);
        nameWidth = Math.max(nameWidth, nameWidthEstimate);
    }

    return graph.newNode({
        name: domainType.name,
        width: width + 40,
        height: height + 28,
        nameWidth: nameWidth,
        headerHeight: HEADING_LAYOUT.height,
        lineHeight: PROP_LAYOUT.height
    });
}

const DECORATION_TEXTS = {
    Boolean : "\u2714",
    Date : "1.1.70",
    Enum : "A,B,C",
    Integer : "42",
    Long : "42L",
    Object : "\u2605",
    List : "[]",
    Map : "{}",
    PlainText : "\"str\"",
    RichText : "&lt;html/&gt;",
    Timestamp : "\u231a",
    UUID : "id"
};

//// DARK color theme
// const DECORATION_COLORS = {
//     Boolean : "#0c0",
//     Date : "#000",
//     Enum : "#444",
//     Integer : "#292",
//     Long : "#00C",
//     PlainText : "#777",
//     RichText : "#777",
//     Timestamp : "#000",
//     UUID : "#522",
//     Object : "#444",
//     List : "#222",
//     Map : "#222"
// };

const DECORATION_COLORS = {
    Boolean : "#0c0",
    Date : "#000",
    Enum : "#444",
    Integer : "#292",
    Long : "#00C",
    PlainText : "#777",
    RichText : "#777",
    Timestamp : "#000",
    UUID : "#522",
    Object : "#444",
    List : "#222",
    Map : "#222"
};

// initialized delayed
var HEADING_LAYOUT;
var PROP_LAYOUT;
var FONT_SIZE_LARGE;
var FONT_SIZE_LARGER;
var FONT_SIZE_NORMAL;

function initLayout()
{
    if (!HEADING_LAYOUT)
    {
        let exampleSizes = svgLayout.getExampleTextSizes();

        HEADING_LAYOUT = exampleSizes[TextSize.LARGE];
        PROP_LAYOUT = exampleSizes[TextSize.NORMAL];

        FONT_SIZE_LARGE = svgLayout.getFontSize(TextSize.LARGER);
        FONT_SIZE_LARGER = svgLayout.getFontSize(TextSize.NORMAL);
        FONT_SIZE_NORMAL = svgLayout.getFontSize(TextSize.SMALL);
    }
}

var Entity = React.createClass({


    onUpdate: function ()
    {
        this.forceUpdate();
    },

    onInteraction: function (pos)
    {
        // var layout = this.props.positionLink.value;
        //
        // var yStart = layout.y - layout.height/2 + layout.headerHeight + layout.lineHeight * 2;
        //
        // if (pos.y > yStart && pos.y < yStart + layout.height)
        // {
        //     console.log("SUB POS: ", ((pos.y - yStart) / layout.lineHeight)|0)
        // }

        GUIContext.focus(this.props.model.name);
//        console.log("INTERACT");
        this.props.onInteraction && this.props.onInteraction.call(null, this.props.model);
    },

    shouldComponentUpdate(nextProps, nextState)
    {
        return (
            this.props.positionLink.value !== nextProps.positionLink.value ||
            this.props.model !== nextProps.model
        );
    },

    render: function ()
    {
        try
        {
            var domainProperty, name;

            var model = this.props.model;
            var properties = model.properties;

            var elementId = model.name;
            var layout = this.props.positionLink.value;

            //console.log(JSON.stringify(layout));


            var uiState = GUIContext.getElementState(elementId, UIState.NORMAL);

            const entityBorderColor = uiState === UIState.FOCUSED ? "#ffbf64" : "#555";
            const entityFillColor = uiState === UIState.FOCUSED ? "#e3ded6" : "#ddd";

            //  console.log("Render Entity: ", model.name);

            var x = layout.x - layout.width/2;
            var y = layout.y - layout.height/2;

            var xPos = x + 10;
            var yPos = y + layout.headerHeight;

            var separatorHeight = (yPos + 8 );

            var headingColor = uiState === UIState.FOCUSED ? "#8a7557" : "#444";
            var texts = [
                <path key="sep" d={ "M" + (xPos - 10) + "," + separatorHeight + " L" + (x + layout.width) + "," + separatorHeight } stroke={ entityBorderColor } />,
                <text key="h" x={ xPos } y={ yPos } fontSize={ FONT_SIZE_LARGE } fill={ headingColor }>
                    { elementId }
                </text>
            ];

            yPos += layout.lineHeight * 2;

            var decoratorOffset = layout.nameWidth + (layout.width - layout.nameWidth) / 2;

            for (var i = 0; i < properties.length; i++)
            {
                domainProperty = properties[i];
                name = domainProperty.name;

                texts.push(
                    <text key={ name } x={ xPos } y={ yPos } fontSize={ FONT_SIZE_NORMAL } fill="#338">
                        { "+ " + name }
                    </text>,
                    <text key={ name + "deco" } x={ xPos + decoratorOffset } y={ yPos } fontSize={ FONT_SIZE_NORMAL } fill={ DECORATION_COLORS[domainProperty.type] } textAnchor="middle">
                        { DECORATION_TEXTS[domainProperty.type] }
                    </text>
                );

                yPos += PROP_LAYOUT.height;
            }

            return (
                <GUIElement className="entity" id={ elementId } positionLink={ this.props.positionLink } onInteraction={ this.onInteraction } onUpdate={ this.onUpdate }>
                    <rect x={ x } y={ y } width={ layout.width } height={ layout.height } stroke={ entityBorderColor } fill={ entityFillColor } rx="4" ry="4"/>
                    { texts }
                </GUIElement>
            );
        }
        catch(e)
        {
            console.error("Error rendering Entity", e);
        }
    }
});

function createGraph(domainLayout, domainTypes, visible, minRuns)
{
    //console.log("createGraph", domainLayout);

    var graph = new Graph({
        createLookup: "name",
        separationLimit: SEPARATION_LIMIT,
        edgeLimit: EDGE_LIMIT,
        separationForceMultiplier: 0.5,
        considerSize: true,
        edgeOffset: (n, refIndex, v) => {
            var ref = n.ref[refIndex];

            var srcOff = PROP_LAYOUT.height * ref.srcIdx;
            var dstOff = PROP_LAYOUT.height * ref.dstIdx;

            if (v.x < 0)
            {
                v.x += n.width /2;
            }
            else
            {
                v.x -= n.width /2;
            }
            v.y += -srcOff + dstOff;
        }
    });

    var copied = false;

    for (let name in domainTypes)
    {
        if (domainTypes.hasOwnProperty(name) && visible[name])
        {
            let domainType = domainTypes[name];
            let node = addToGraph(graph, domainType);

            if (domainLayout)
            {
                var oldNode = domainLayout[name];
                if (oldNode)
                {
                    //console.log("COPY", oldNode);
                    node.x = oldNode.x;
                    node.y = oldNode.y;
                    copied = true;
                }
            }
        }
    }

    var nodes = graph.nodes;
    var lookup = graph.lookup;
    for (let i = 0; i < nodes.length; i++)
    {
        let node = nodes[i];
        let domainType = domainTypes[node.name];
        let properties = domainType.properties;
        for (var j = 0; j < properties.length; j++)
        {
            var propertyModel = properties[j];
            var foreignKey = propertyModel.foreignKey;
            if (foreignKey)
            {
                var type = foreignKey.type;
                var targetType = lookup[type];
                if (targetType)
                {
                    node.ref.push({
                        id: targetType.id,
                        srcIdx: j,
                        dstIdx: findPropertyIndexWithName(domainType, foreignKey.property)
                    });
                }
            }
        }
    }

    if (!copied)
    {
        graph.arrange();
        var tension = Infinity;
        for (let i = 0; i < minRuns || tension < TENSION_LIMIT; i++)
        {
            tension = graph.ease();
        }
//        console.log("TENSION:", tension);
    }

    return graph;
}

function createRelationLookup(domainTypes)
{
    var lookup = {};

    for (var name in domainTypes)
    {
        if (domainTypes.hasOwnProperty(name))
        {
            var domainType = domainTypes[name];

            var list = [];
            var properties = domainType.properties;
            for (var i = 0; i < properties.length; i++)
            {
                var domainProperty = properties[i];
                if (domainProperty.foreignKey)
                {
                    list.push(i);
                }
            }

            lookup[name] = list;
        }
    }

    return lookup;
}

function initArrowControls(graph, domainLayout, domainTypes, relationLookup)
{
    var newArrowControls = {};

    for (var name in relationLookup)
    {
        if (relationLookup.hasOwnProperty(name))
        {
            var indizes = relationLookup[name];
            for (var i = 0; i < indizes.length; i++)
            {
                var idx = indizes[i];

                var key = fkKey(name, idx);

                var pos = domainLayout && domainLayout[key];
                if (pos)
                {
                    newArrowControls[key] = pos;
                }
                else
                {
                    var srcNode = graph.lookup[name];
                    var dstNode = graph.lookup[ domainTypes[name].properties[idx].foreignKey.type ];

                    newArrowControls[key] = {
                        x: (srcNode.x + dstNode.x ) / 2,
                        y: (srcNode.y + dstNode.y ) / 2
                    };
                }
            }
        }
    }

    console.log("ARROW CONTROLS:", newArrowControls);

    return newArrowControls;
}

function fkKey(name, idx)
{
    return "fk_" + name + "_" + idx;
}

var DomainEditorGraph = React.createClass({

    getInitialState: function ()
    {

        initLayout();

        var graph = createGraph(
            this.props.domainLayoutLink.value,
            this.props.domainTypes,
            this.props.visible,
            MIN_EASE_INITIAL
        );

        var aabb = graph.getAABB();
        var relationLookup = createRelationLookup(this.props.domainTypes);

        return {
            graph: graph,
            centerX: (aabb.minX + aabb.maxX) / 2,
            centerY: (aabb.minY + aabb.maxY) / 2,
            arrowControls: initArrowControls(graph, this.props.arrowControls, this.props.domainTypes, relationLookup),
            relationLookup: relationLookup
        };

    },

    componentWillReceiveProps: function (nextProps)
    {
        if (
            this.props.domainTypes !== nextProps.domainTypes ||
            this.props.visible !== nextProps.visible ||
            this.props.domainLayoutLink.value !== nextProps.domainLayoutLink.value ||
            this.props.arrowControls !== nextProps.arrowControls
        )
        {
//            console.log("RELAYOUT");

            var domainLayout = nextProps.domainLayoutLink.value;
            var newGraph = createGraph(
                domainLayout,
                nextProps.domainTypes,
                nextProps.visible,
                MIN_EASE_UPDATE
            );

            var relationLookup = createRelationLookup(nextProps.domainTypes);
            this.setState({
                graph: newGraph,
                relationLookup: relationLookup,
                arrowControls: initArrowControls(newGraph, nextProps.arrowControls, nextProps.domainTypes, relationLookup),
            });
        }
    },

    shouldComponentUpdate: function (nextProps, nextState)
    {
        return (
            this.state.graph !== nextState.graph ||
            this.state.arrowControls !== nextState.arrowControls ||
            this.props.height !== nextProps.height ||
            this.props.domainTypes !== nextProps.domainTypes ||
            this.props.visible !== nextProps.visible ||
            this.props.domainLayoutLink.value !== nextProps.domainLayoutLink.value
        );
    },

    onLayoutChange: function (layout, permanent)
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
            var link = this.props.domainLayoutLink;

            if (link.value === false)
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
                link.requestChange(
                    immutableUpdate(link.value,
                    {
                        [name] : { $set: {
                            x: layout.x,
                            y: layout.y
                        } }
                    })
                );
            }
        }
    },
    createControlLink: function (key)
    {
        return new ValueLink( this.state.arrowControls[key], (pos, permanent) => {

            //console.log("UPDATE CONTROL", key, pos);

            this.setState({
                arrowControls : immutableUpdate(this.state.arrowControls, {
                    [key] : { $set: pos }
                })
            });
        });
    },

    render: function ()
    {
        var domainTypes = this.props.domainTypes;
        var visible = this.props.visible;
        var domainLayout = this.state.graph.lookup;

        var entities = [];
        var relations = [];

        values(domainTypes).map(domainType => {

            var name = domainType.name;
            if (!visible[name])
            {
                return false;
            }
            var typeLayout = domainLayout[name];

            entities.push(
                <Entity
                    key={ name }
                    model={ domainType }
                    onInteraction={ () => this.props.onInteraction(name) }
                    positionLink={ new ValueLink(typeLayout, this.onLayoutChange ) }
                />
            );

            var list = this.state.relationLookup[name];
            for (var i = 0; i < list.length; i++)
            {
                var idx = list[i];
                var foreignKeyProperty = domainType.properties[idx];

                var fkType = foreignKeyProperty.foreignKey.type;
                if (visible[fkType])
                {
                    var targetLayout = domainLayout[fkType];

                    var key = fkKey(name,idx);

                    var x0 = typeLayout.x + ( targetLayout.x < typeLayout.x ? -typeLayout.width/2 : typeLayout.width/2 );
                    var y0 = typeLayout.y - typeLayout.height/2 + typeLayout.headerHeight + typeLayout.headerHeight + idx * PROP_LAYOUT.height+ PROP_LAYOUT.height/2;

                    var x2 = targetLayout.x + ( targetLayout.x >= typeLayout.x ? -targetLayout.width/2 : targetLayout.width/2 );
                    var y2 = targetLayout.y - targetLayout.height/2 + targetLayout.headerHeight + targetLayout.headerHeight + PROP_LAYOUT.height/2;

                    var controlLink = this.createControlLink(key);
                    relations.push(
                        <ArrowComponent
                            key={ key }
                            x0={ x0 }
                            y0={ y0 }
                            x1={ controlLink.value.x }
                            y1={ controlLink.value.y }
                            x2={ x2 }
                            y2={ y2 }
                        />,

                        <Control id={ key + 2} key={ key + 2 } positionLink={ controlLink } />
                    );

                }
            }
        });


        return (
            <GUIContainer style={{ backgroundColor: "#f0f0f0" }} height={this.props.height} centerX={ this.state.centerX } centerY={ this.state.centerY }>
                { relations }
                { entities }
            </GUIContainer>
        );
    }
});

module.exports = DomainEditorGraph;
