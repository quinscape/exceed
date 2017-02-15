const React = require("react");

var UIState = require("./ui-state");
const GUIElement = require("./GUIElement");
const Symbol = require("../../ui/Symbol");
const ValueLink = require("../../util/value-link");

const GUIContext = require("../../editor/gui/gui-context");

/**
 * SVG symbol as GUIElement. Supports drag and drop and handles mouse/focus activity by optionally setting classes on
 * the symbol SVG group.
 *
 *  * symbol-initial    for the initial state before any user interaction
 *  * symbol-focused    Mouse pointer is hovering over the symbol or user has focused the symbol via keyboard or other means.
 *  * symbol-normal     Mouse pointer has left / blur event happened.
 *
 *  These classes can be define CSS animation rules. The symbol element will set it's transform-origin onto the symbol position.
 */
var SymbolElement = React.createClass({

    propTypes: {
        /**
         * Unique GUIElement id
         */
        id: React.PropTypes.string.isRequired,
        /**
         * Set of symbols imported from SVG using the "symbol-loader" package
         */
        symbols: React.PropTypes.object.isRequired,
        /**
         * Name of the symbol in the
         */
        name: React.PropTypes.string.isRequired,
        /**
         * The x-coordinate to draw the symbol at.
         */
        x: React.PropTypes.number.isRequired,
        /**
         * The y-coordinate to draw the symbol at.
         */
        y: React.PropTypes.number.isRequired,
        /**
         * Layer of the symbol to draw (or undefined for all layers)
         */
        layer: React.PropTypes.string,
        /**
         * Additional SVG transform to apply to the symbol group.
         */
        transform: React.PropTypes.string,
        /**
         * Method to call on interaction
         */
        onInteraction: React.PropTypes.func,
        /**
         * If true, all drag&drop of the symbol group element (default: true)
         */
        draggable: React.PropTypes.bool,
        /**
         * If true, set CSS animation classes on symbol group element.
         */
        animated: React.PropTypes.bool
    },

    getDefaultProps: function ()
    {
        return {
            x: 0,
            y: 0,
            draggable: true,
            animated: true
        };
    },

    getInitialState: function ()
    {
        return {
            focused: null
        };
    },

    onUpdate: function ()
    {
        this.forceUpdate();
    },

    onMouseOver: function ()
    {
        this.setState({
            focused: true
        });
    },

    onMouseOut: function ()
    {
        this.setState({
            focused: false
        });
    },

    render: function ()
    {
        var elementId = this.props.id;

        var animated = this.props.animated;

        var uiState = GUIContext.getElementState(elementId, UIState.NORMAL);

        var aabb = this.props.symbols[this.props.name].aabb;

        var w = aabb.maxX - aabb.minX;
        var h = aabb.maxY - aabb.minY;

        var className;
        if (animated)
        {
            if (this.state.focused === null)
            {
                className= "symbol-initial";
            }
            else
            {
                className = uiState === UIState.FOCUSED || this.state.focused ? "symbol-focused" : "symbol-normal";
            }
        }

        return (
            <GUIElement
                id={ elementId }
                onUpdate={ this.onUpdate }
                className={ className }
                position={ {
                    x: this.props.x,
                    y: this.props.y
                } }
                uiState={ UIState.DISABLED }
                onInteraction={ this.props.onInteraction }
                draggable={ false }
                onMouseOver={ animated && this.onMouseOver}
                onMouseOut={ animated && this.onMouseOut}
                style={ animated && {
                    transformOrigin : this.props.x + "px "  + this.props.y + "px"
                } }
            >
                <Symbol
                    symbols={ this.props.symbols }
                    name={ this.props.name }
                    layer={ this.props.layer }
                    x={ this.props.x }
                    y={ this.props.y }
                    transform={ this.props.transform }
                />

            </GUIElement>
        );
    }
});

module.exports = SymbolElement;
