const React = require("react");

import UIState from "./ui-state";
import GUIElement from "./GUIElement"
const Symbol = require("../../ui/Symbol");
import ValueLink from "../../util/value-link"

import PropTypes from 'prop-types'

import GUIContext from "../../editor/gui/gui-context"

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
class SymbolElement extends React.Component
{

    static propTypes = {
        /**
         * Unique GUIElement id
         */
        id: PropTypes.string.isRequired,
        /**
         * Set of symbols imported from SVG using the "symbol-loader" package
         */
        symbols: PropTypes.object.isRequired,
        /**
         * Name of the symbol in the
         */
        name: PropTypes.string.isRequired,
        /**
         * The x-coordinate to draw the symbol at.
         */
        x: PropTypes.number.isRequired,
        /**
         * The y-coordinate to draw the symbol at.
         */
        y: PropTypes.number.isRequired,
        /**
         * Layer of the symbol to draw (or undefined for all layers)
         */
        layer: PropTypes.string,
        /**
         * Additional SVG transform to apply to the symbol group.
         */
        transform: PropTypes.string,
        /**
         * Method to call on interaction
         */
        onInteraction: PropTypes.func,
        /**
         * If true, all drag&drop of the symbol group element (default: true)
         */
        draggable: PropTypes.bool,
        /**
         * If true, set CSS animation classes on symbol group element.
         */
        animated: PropTypes.bool
    }

    getDefaultProps()
    {
        return {
            x: 0,
            y: 0,
            draggable: true,
            animated: true
        };
    }

    state =
    {
        focused: null
    }

    onUpdate()
    {
        this.forceUpdate();
    }

    onMouseOver()
    {
        this.setState({
            focused: true
        });
    }

    onMouseOut()
    {
        this.setState({
            focused: false
        });
    }

    render()
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
};

export default SymbolElement
