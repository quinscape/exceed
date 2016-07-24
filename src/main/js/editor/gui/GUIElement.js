var React = require("react");
var ReactDOM = require("react-dom");

var GUIContext = require("./gui-context");
var UIState = require("./ui-state");

const assign = require("object-assign");

var DragState = require("./drag-state");
var GlobalDrag = require("../../util/global-drag");


var immutableUpdate = require("react-addons-update");

function findSVG(e)
{
    do
    {
        if (e.tagName === "svg")
        {
            return e;
        }
    } while(e = e.parentNode);

    return null;
}

function noOp()
{

}

/**
 * SVGElement is a helper element for implementing SVG based user interface elements that can be interacted with by mouse and keyboard.
 *
 * It acts as a grouping wrapper for the graphical elements given as children and has no graphical representation of its own.
 */
var GUIElement = React.createClass({

    getDefaultProps: function ()
    {
        return {
            uiState: UIState.NORMAL,
            dragThreshold: 4
        };
    },

    propTypes: {
        id: React.PropTypes.string.isRequired,
        uiState: React.PropTypes.oneOf(UIState.values()),
        onInteraction: React.PropTypes.func,
        onUpdate: React.PropTypes.func.isRequired,
        dragThreshold: React.PropTypes.number
    },

    componentDidMount: function ()
    {
        GUIContext._register(this);

        this.drag = DragState.OFF;

        // we use a global move listener, so that our dragged object doesn't act strange when the user managed to
        // move the cursor outside of the svg element or svg container.
        GlobalDrag.init();
    },

    componentWillUnmount: function ()
    {
        GUIContext._deregister(this);
        GlobalDrag.destroy();
    },

    componentWillUpdate: function (nextProps, nextState)
    {
        if (nextProps.uiState !== this.props.uiState)
        {
            GUIContext._setElementState(this.props.id, nextProps.uiState, true);
        }
    },

    onMouseDown: function (ev)
    {
        if (this.drag === DragState.OFF)
        {
            this.drag = DragState.LOCKED;

            var layout = this.props.positionLink.value;

            //console.log("START:" , layout.x, layout.y);

            this.offsetX = GUIContext.applyZoom(ev.pageX) - layout.x;
            this.offsetY = GUIContext.applyZoom(ev.pageY) - layout.y;

            GlobalDrag.setActiveDrag(this);
        }

        ev.preventDefault();
        return false;
    },

    onMouseMove: function (x, y)
    {
        if (this.drag !== DragState.OFF)
        {
            var link = this.props.positionLink;
            var layout = link.value;

            x = GUIContext.applyZoom(x) - this.offsetX;
            y = GUIContext.applyZoom(y) - this.offsetY;

            if (Math.sqrt( x * x+ y * y) > this.props.dragThreshold)
            {
                this.drag = DragState.ON;
            }

            var copy = assign({}, layout);
            copy.x = x;
            copy.y = y;
            link.requestChange(copy, false);
        }
    },

    onMouseUp: function (x, y)
    {
        if (this.drag !== DragState.OFF)
        {
            var dragState = this.drag;
            this.drag = DragState.OFF;

            GlobalDrag.setActiveDrag(null);

            x = GUIContext.applyZoom(x) - this.offsetX;
            y = GUIContext.applyZoom(y) - this.offsetY;

            if (dragState === DragState.ON)
            {
                var link = this.props.positionLink;
                //console.log("onMouseUp", link);
                var pos = link.value;

                var copy = assign({}, pos);
                copy.x = x;
                copy.y = y;
                link.requestChange(copy, true);
            }
            else
            {
                var fn = this.props.onInteraction;
                if (typeof fn == "function")
                {
                    fn({
                        x: x,
                        y: y
                    });
                }
            }
        }
    },

    render: function ()
    {
        //console.log("Render GUI element " + this.props.id);
        return (
            <g
                onMouseDown={ this.onMouseDown }
                className={ this.props.className }>
                { this.props.children }
            </g>
        );
    }
});

module.exports = GUIElement;
