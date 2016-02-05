var React = require("react");
var ReactDOM = require("react-dom");

var GUIContext = require("./gui-context");
var UIState = require("./ui-state");

var Enum = require("../../util/enum");

/**
 *
 * @type
 */
var DragState = new Enum({
    OFF: true,
    LOCKED: true,
    ON: true
});


/**
 * High-level component wrapping SVG elements to enable focus and focus highlighting and keyboard interaction.
 */
var SVGElement = React.createClass({

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
        onUpdate: React.PropTypes.func,
        dragThreshold: React.PropTypes.number
    },

    componentDidMount: function ()
    {
        GUIContext._register(this);

        this.drag = DragState.OFF;
    },

    componentWillUnmount: function ()
    {
        GUIContext._deregister(this);
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

            this.offsetX = ev.pageX - layout.x;
            this.offsetY = ev.pageY - layout.y;
        }

        ev.preventDefault();
        return false;
    },

    onMouseMove: function (ev)
    {
        if (this.drag !== DragState.OFF)
        {
            var layout = this.props.positionLink.value;
            var x = ev.pageX - layout.x - this.offsetX;
            var y = ev.pageY - layout.y - this.offsetY;

            if (Math.sqrt(x*x+y*y) > this.props.dragThreshold)
            {
                this.drag = DragState.ON;
            }

            if (this.drag === DragState.ON)
            {
                ReactDOM.findDOMNode(this).setAttribute("transform", "translate(" + x +"," + y+ ")");
            }
        }
    },

    onMouseUp: function (ev)
    {
        var svgElem = this;

        var dragState = this.drag;
        this.drag = DragState.OFF;

        console.log("onMouseUp", dragState);

        if (dragState === DragState.ON)
        {
            var link = this.props.positionLink;
            var layout = link.value;
            var x = ev.pageX - this.offsetX;
            var y = ev.pageY - this.offsetY;

            link.requestChange(React.addons.update(layout, {
                x: {$set: x},
                y: {$set: y}
            })).then(function ()
            {
                ReactDOM.findDOMNode(svgElem).removeAttribute("transform");
            });
        }
        else
        {
            this.drag = DragState.OFF;
            this.props.onInteraction.call(this, ev)
        }
    },

    render: function ()
    {
        //console.log("Render GUI element " + this.props.id);
        return (
            <g onMouseDown={ this.onMouseDown } onMouseMove={ this.onMouseMove } onMouseUp={ this.onMouseUp}>
                { this.props.children }
            </g>
        );
    }
});

module.exports = SVGElement;
