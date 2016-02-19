var sys = require("../sys");

var React = require("react");

var classes = require("classnames");
var ValueLink = require("../util/value-link");
var hub = require("../service/hub");

var Promise = require("es6-promise-polyfill").Promise;

var GUIContext = require("./gui/gui-context");
var GUIContainer = require("./gui/GUIContainer");
var SVGElement = require("./gui/SVGElement");
var UIState = require("./gui/ui-state");

var EntityModal = require("./EntityModal");

var svgLayout = require("../util/svg-layout");

var immutableUpdate = require("react-addons-update");

var TextSize = svgLayout.TextSize;

var decorations = {
    Boolean : "\u2173",
    Date : "1.1.70",
    Enum : "A,B,C",
    Integer : "42",
    Long : "42L",
    PlainText : "\"abc\"",
    RichText : "&lt;abc/&gt;",
    Timestamp : "\u231a",
    UUID : "id"
};

var decorationColors = {
    Boolean : "#080",
    Date : "#fff",
    Enum : "#ccc",
    Integer : "#cfc",
    Long : "#ccf",
    PlainText : "#ccf",
    RichText : "#ddd",
    Timestamp : "#fff",
    UUID : "#fff"
};

// initialized delayed
var EXAMPLE_TEXT_SIZES;
var HEADING_LAYOUT;
var PROP_LAYOUT;
var FONT_SIZE_LARGE;
var FONT_SIZE_NORMAL;

var Entity = React.createClass({


    onUpdate: function ()
    {
        this.forceUpdate();
    },

    onInteraction: function (ev)
    {
        GUIContext.focus(this.props.model.name);
        console.log("INTERACT");
        this.props.onInteraction && this.props.onInteraction.call(null, this.props.model);
        ev.preventDefault();
    },

    render: function ()
    {
        try
        {
            const entityBorderColor = uiState === UIState.FOCUSED ? "#ffe1b6" : "#888";
            const entityFillColor = uiState === UIState.FOCUSED ? "#686560" : "#555";

            var domainProperty, name;
            var model = this.props.model;
            var properties = model.properties;

            var elementId = model.name;
            var layout = this.props.layoutLink.value;


            var uiState = GUIContext.getElementState(elementId, UIState.NORMAL);

            //console.log("Entity: ", uiState);

            var focusStyles = {
                stroke: "#f00",
                strokeWidth: 2
            };

            var xPos = layout.x + 10;
            var yPos = layout.y + layout.headerHeight;

            var separatorHeight = (yPos + 8 );

            var headingColor = uiState === UIState.FOCUSED ? "#ffd8a0" : "#9dd";
            var texts = [
                <path key="sep" d={ "M" + (xPos - 10) + "," + separatorHeight + " L" + (layout.x + layout.width) + "," + separatorHeight } stroke={ entityBorderColor } />,
                <text key="h" x={ xPos } y={ yPos } fontSize={ FONT_SIZE_LARGE } fill={ headingColor }>
                    { elementId }
                </text>
            ];

            yPos += layout.headerHeight + 8;

            var decoratorOffset = layout.nameWidth + (layout.width - layout.nameWidth) / 2;

            for (var i = 0; i < properties.length; i++)
            {
                domainProperty = properties[i];
                name = domainProperty.name;

                texts.push(
                    <text key={ name } x={ xPos } y={ yPos } fontSize={ FONT_SIZE_NORMAL } fill="#ccc">
                        { "+ " + name }
                    </text>,
                    <text key={ name + "deco" } x={ xPos + decoratorOffset } y={ yPos } fontSize={ FONT_SIZE_NORMAL } fill={ decorationColors[domainProperty.type] } textAnchor="middle">
                        { decorations[domainProperty.type] }
                    </text>
                );

                yPos += PROP_LAYOUT.height;
            }

            return (
                <SVGElement id={ elementId }  positionLink={ this.props.layoutLink } onInteraction={ this.onInteraction } onUpdate={ this.onUpdate }>
                    <rect x={ layout.x } y={ layout.y } width={ layout.width } height={ layout.height } stroke={ entityBorderColor } fill={ entityFillColor } rx="4" ry="4"/>
                    { texts }
                </SVGElement>
            );
        }
        catch(e)
        {
            console.error("Error rendering Entity", e);
        }
    }
});

var DomainEditor = React.createClass({
    getInitialState: function ()
    {
        return {
            domainModels: null,
            activeEdit: null
        };
    },

    componentDidMount: function ()
    {
        if (this.state.domainModels == null)
        {
            var component = this;
            hub.request({
                type: "message.DomainTypeQuery"
            }).then(function (data)
            {

                console.log("DOMAIN", data);

                component.setState({
                    domainModels: data
                }/*, function()
                {
                    console.log("DomainEditor state set");
                }*/)

            }).catch(function (err)
            {
                console.error(e);
            })
        }

    },

    createDomainModelLink: function(activeEdit)
    {
        var domainEditor = this;

        return new ValueLink(this.state.domainModels.domainTypes[activeEdit], function (newModel)
        {
            ajax({
                method: "POST",
                url : sys.contextPath + "/editor/" + sys.appName + "/domain/type",
                contentType: "application/json",
                data: {
                    name: model.name,
                    model: newModel
                }
            }).then(function()
            {
                domainEditor.setState({
                    domainModels: immutableUpdate(domainEditor.state.domainModels, {
                        domainTypes: {
                            $apply: function(domainTypes)
                            {
                                if (model.name !== newModel.name)
                                {
                                    delete domainTypes[model.name];
                                }
                                return domainTypes;
                            },
                            [newModel.name] : {$set: newModel}
                        }
                    })
                })
            });
        });
    },

    createLayoutLink: function (domainLayout, name)
    {
        if (!domainLayout)
        {
            return null;
        }

        var domainEditor = this;

        return new ValueLink(domainLayout[name], function (newLayout)
        {
            return ajax({
                method: "POST",
                url : sys.contextPath + "/editor/" + sys.appName + "/domain/layout",
                contentType: "application/json",
                data: {
                    name: name,
                    layout: newLayout
                }
            }).then(function(data)
            {
                //console.log(data);

                var partialState = {
                    domainModels: immutableUpdate(domainEditor.state.domainModels, {
                        domainLayout: {
                            $apply: function(domainLayout)
                            {
                                return domainLayout || {};
                            },
                            [name]: {$set: newLayout}
                        }
                    })
                };

                //console.log("Set domainlayout state", partialState);

                domainEditor.setState(partialState)
            }).catch(function (err)
            {
                console.error(err);

            });
        });
    },

    openEntityModal: function (model)
    {
        console.log("openEntityModal", model);

        this.setState({
            activeEdit: model.name
        });
    },

    render: function ()
    {
        if (!this.state.domainModels)
        {
            return (<div/>);
        }

        if (!EXAMPLE_TEXT_SIZES)
        {
            EXAMPLE_TEXT_SIZES = svgLayout.getExampleTextSizes();

            HEADING_LAYOUT = EXAMPLE_TEXT_SIZES[TextSize.LARGE];
            PROP_LAYOUT = EXAMPLE_TEXT_SIZES[TextSize.NORMAL];

            FONT_SIZE_LARGE = svgLayout.getFontSize(TextSize.LARGE);
            FONT_SIZE_NORMAL = svgLayout.getFontSize(TextSize.NORMAL);
        }

        var domainProperty;

        try
        {
            var entities = [];
            var relations = [];


            if (this.state.domainModels != null)
            {
                var domainTypes = this.state.domainModels.domainTypes;
                var domainLayout = this.state.domainModels.domainLayout || {};

                for (var name in domainTypes)
                {
                    if (domainTypes.hasOwnProperty(name))
                    {
                        var domainType = domainTypes[name];

                        var layout = domainLayout[name];
                        if (!layout)
                        {
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
                                var decoratorWidthEstimate = PROP_LAYOUT.ppc * decorations[domainProperty.type].length;

                                width = Math.max(width, nameWidthEstimate + decoratorWidthEstimate);
                                nameWidth = Math.max(nameWidth, nameWidthEstimate);
                            }

                            domainLayout[name] = {
                                x: 0,
                                y: 0,
                                width: width + 40,
                                height: height + 28,
                                nameWidth: nameWidth,
                                headerHeight: HEADING_LAYOUT.height,
                                lineHeight: PROP_LAYOUT.height
                            };

                            console.log("domainLayout", domainLayout)
                        }


                        entities.push(
                            <Entity key={ name } model={ domainType } onInteraction={ this.openEntityModal }  layoutLink={ this.createLayoutLink(domainLayout, name) } />
                        );
                    }
                }
            }


            var domainEditor = this;

            var openLink = new ValueLink( !!this.state.activeEdit, function(open)
            {
                console.log("openLink.requestChange", open);
                if (!open)
                {
                    domainEditor.setState({
                        activeEdit: null
                    });
                }
            });


            var domainModels = this.state.domainModels || { enums: {}, propertyTypes: {}};
            console.log("render DomainEditor", domainModels);
            return (
                <div>
                    <GUIContainer>
                        { entities }
                        { relations }
                    </GUIContainer>
                    <EntityModal openLink={ openLink } modelLink={ this.createDomainModelLink(this.state.activeEdit) } enums={ domainModels.enums } propertyTypes={ domainModels.propertyTypes }/>
                </div>
            );

        }
        catch(e)
        {
            console.error("Error rendering DomainEditor", e);
        }
    }
});

module.exports = DomainEditor;
