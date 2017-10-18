import React from "react";
import ContainerContext from "./container-context";

import GUIContext from "./gui-context"
import UIState from "./ui-state";
import GlobalDrag from "../../util/global-drag"
import PropTypes from 'prop-types'

import assign from "object-assign";

/**
 * SVGElement is a helper element for implementing SVG based user interface elements that can be interacted with by mouse and keyboard.
 *
 * It acts as a grouping wrapper for the graphical elements given as children and has no graphical representation of its own.
 */
class GUIElement extends React.Component
{
    getDefaultProps()
    {
        return {
            uiState: UIState.NORMAL,
            dragThreshold: 2,
            draggable: true
        };
    }

    static propTypes = {
        id: PropTypes.string.isRequired,
        className: PropTypes.string,
        style: PropTypes.object,
        uiState: PropTypes.oneOf(UIState.values()),
        onInteraction: PropTypes.func,
        onUpdate: PropTypes.func.isRequired,
        draggable: PropTypes.bool,
        dragThreshold: PropTypes.number
    }

    static contextTypes = {
        containerContext: PropTypes.instanceOf(ContainerContext)
    }


    componentDidMount()
    {
        GUIContext._register(this);
        // we use a global move listener, so that our dragged object doesn't act strange when the user managed to
        // move the cursor outside of the svg element or svg container.
        if (this.props.draggable)
        {
            GlobalDrag.init();
        }
    }

    componentWillUnmount()
    {
        GUIContext._deregister(this);
        if (this.props.draggable)
        {
            GlobalDrag.destroy();
        }
    }

    componentWillUpdate(nextProps, nextState)
    {
        if (nextProps.uiState !== this.props.uiState)
        {
            GUIContext._setElementState(this.props.id, nextProps.uiState, true);
        }
    }

    onMouseDown(ev)
    {
        var layout = this.props.position;

        var x = GUIContext.applyZoom(ev.pageX);
        var y = GUIContext.applyZoom(ev.pageY);

        //console.log("START:" , layout.x, layout.y, "EVENT", x, y);

        // we use a simple offset system for dragging and dropping, we don't figure out the exact position of the mouse
        // cursor in object space, we just remember the offset after accounting for zoom and apply it later.
        this.offsetX = x - layout.x;
        this.offsetY = y - layout.y;

        this.dragLocked = true;
        GlobalDrag.setActiveDrag(this);

        ev.preventDefault();
        return false;
    }

    onMouseMove(x, y)
    {
        if (GlobalDrag.isActiveDrag(this))
        {
            var layout = this.props.position;

            x = GUIContext.applyZoom(x) - this.offsetX;
            y = GUIContext.applyZoom(y) - this.offsetY;

            if (this.dragLocked)
            {
                var dx = layout.x - x;
                var dy = layout.y - y;

                if (Math.sqrt( dx * dx + dy * dy) > this.props.dragThreshold)
                {
                    //console.log("ACTIVATE DRAG");
                    this.dragLocked = false;
                }
            }

            if (!this.dragLocked && this.props.draggable)
            {
                //console.log("GUIElement move");
                var copy = assign({}, layout);
                copy.x = x;
                copy.y = y;
                this.props.updatePosition(copy, false);
            }
        }
    }

    /**
     * Called by GlobalDrag for every update in position for this GUIElement by dragging.
     *
     * @param screenX     x-coordinate in screen space
     * @param screenY     y-coordinate in screen space
     */
    onMouseUp(screenX, screenY)
    {
        if (GlobalDrag.isActiveDrag(this))
        {
            // uncorrected object space values
            var x = GUIContext.applyZoom(screenX) - this.offsetX;
            var y = GUIContext.applyZoom(screenY) - this.offsetY;

            GlobalDrag.setActiveDrag(null);

            //console.log("onMouseUp", link);
            var pos = this.props.position;

            if (this.dragLocked)
            {
                var fn = this.props.onInteraction;
                if (typeof fn == "function")
                {
                    var clickPos;
                    // does the onInteraction method expect a position argument?
                    if (fn.length === 1)
                    {
                        // yep -> figure out the actual object space position of the click relative to
                        //        the current position
                        clickPos = this.context.containerContext.toObjectCoordinates(screenX, screenY);
                        clickPos.x -= pos.x;
                        clickPos.y -= pos.y;
                    }
                    fn(clickPos);
                }
            }
            else if (this.props.draggable)
            {
                // copy pos structure and update the coordinates
                var copy = assign({}, pos);
                copy.x = x;
                copy.y = y;
                this.props.updatePosition(copy, true);
            }
        }
    }

    render()
    {
        //console.log("Render GUI element " + this.props.id);
        return (
            <g
                onMouseOver={ this.props.onMouseOver }
                onMouseOut={ this.props.onMouseOut }
                onMouseDown={ this.onMouseDown }
                className={ this.props.className }
                style={ this.props.style }
            >
                { this.props.children }
            </g>
        );
    }
}

export default GUIElement
