const React = require("react");
const GUIContext = require("./gui-context");
const DragState = require("./drag-state");
const Event = require("../../util/event");
const GlobalDrag = require("../../util/global-drag");

const requestAnimationFrame = require("raf");

const MIN_ZOOM  = 1;
const MAX_ZOOM  = 16;

const ZOOM_SPEED = 1.5;
const ZOOM_ACCELERATION = 0.01;


const ANIMATION_STEP = 1 / 100 * 1000;

function ViewBox(centerX, centerY, height)
{
    var size = GUIContext.applyZoom(height);
    var hh = size/2;
    // we use height for both dimensions since we have a locked aspect ratio anyway and the width
    // most often is given as "100%"
    this.x = (centerX - hh);
    this.y = (centerY - hh);
    this.size = size;
}

ViewBox.prototype.render = function ()
{
    return this.x + " " + this.y + " " + this.size + " " + this.size;
};

/**
 * Contains SVG based GUIElement and provides a GUIContext context object to GUIElement instances to manage SVG
 * component focus and keyboard interaction via focus proxy elements.
 */
var GUIContainer = React.createClass({

    propTypes:  {
        centerX : React.PropTypes.number,
        centerY : React.PropTypes.number,
        width: React.PropTypes.oneOfType([React.PropTypes.number, React.PropTypes.string]),
        height: React.PropTypes.number,
        style: React.PropTypes.object,
        onInteraction: React.PropTypes.func
    },

    getDefaultProps : function ()
    {
        return {
            centerX : 0,
            centerY : 0,
            width: "100%",
            height: 600,
            style: null,
            onInteraction: null
        };

    },

    componentDidMount: function ()
    {
        GUIContext.update();

        this.centerX = this.props.centerX;
        this.centerY = this.props.centerY;
        this.targetZoom = GUIContext.getZoom();
        this.dz = 0;
        this.animating = false;
        this.drag = DragState.OFF;

        if (this.svgElem)
        {
            Event.add(this.svgElem, "mousewheel", this.onMouseWheel, false);
        }

        GlobalDrag.init();
    },
    componentWillUnmount: function ()
    {
        if (this.svgElem)
        {
            Event.remove(this.svgElem, "mousewheel", this.onMouseWheel, false);
        }

        GlobalDrag.destroy();
    },

    animateZoom: function ()
    {
        const now = Date.now();
        var finished = false;

        var delta = now - this.lastFrame;

        this.lastFrame = now;
        //console.log("animate");

        // We consume the time that has passed in discrete steps
        while (delta > 0)
        {
            //console.log("animate", this.targetZoom, GUIContext.getZoom());
            var d = this.targetZoom - GUIContext.getZoom();
            //console.log("update", d);

            // as soon as we reach our destination or overshoot in the zoom direction we were heading..
            if ( ( this.dz < 0 && d > this.dz ) || ( this.dz > 0 && d < this.dz ) )
            {
                // we set the final targetZoom..
                GUIContext.setZoom(this.targetZoom);
                // reset our speed
                this.dz = 0;
                // and stop animating
                this.animating = false;
                break;
            }
            else
            {
                // accelerate towards target zoom
                if (d < 0)
                {
                    if (this.dz  > -ZOOM_SPEED)
                    {
                        this.dz = Math.min(0, this.dz) - ZOOM_ACCELERATION;
                    }
                }
                else
                {
                    if (this.dz  < ZOOM_SPEED)
                    {
                        this.dz = Math.max(0, this.dz) + ZOOM_ACCELERATION;
                    }
                }
            }

            delta -= ANIMATION_STEP;
        }

        // set current zoom ..
        GUIContext.setZoom(GUIContext.getZoom() + this.dz);

        if (this.animating)
        {
            // .. and request the next animation step.
            requestAnimationFrame(this.animateZoom);
        }

        this.updateViewBox();

    },

    updateViewBox: function ()
    {
        if (this.svgElem)
        {
            this.svgElem.setAttribute("viewBox", new ViewBox(this.centerX, this.centerY, this.props.height).render())
        }
    },

    onMouseWheel: function (ev)
    {
        if (this.drag !== DragState.OFF)
        {
            return;
        }

        // cross-browser wheel delta
        var event = window.event || ev;

        this.targetZoom -= Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail)));

        if (this.targetZoom < MIN_ZOOM)
        {
            this.targetZoom = MIN_ZOOM;
        }

        if (this.targetZoom > MAX_ZOOM)
        {
            this.targetZoom = MAX_ZOOM;
        }

        //console.log("TARGET: ", this.targetZoom);

        if (!this.animating)
        {
            //console.log("start animation");
            this.animating = true;
            requestAnimationFrame(this.animateZoom);
        }

        return Event.preventDefault(ev);
    },

    onMouseDown: function (ev)
    {
        if (this.drag === DragState.OFF)
        {
            this.drag = DragState.LOCKED;

            var x = GUIContext.applyZoom(ev.pageX);
            var y = GUIContext.applyZoom(ev.pageY);

            this.offsetX = this.centerX + x;
            this.offsetY = this.centerY + y;

            GlobalDrag.setActiveDrag(this);

            return Event.preventDefault(ev);
        }
    },

    onMouseMove: function (x, y)
    {
        if (this.drag !== DragState.OFF)
        {
            x = this.offsetX - GUIContext.applyZoom(x);
            y = this.offsetY - GUIContext.applyZoom(y);

            if (this.drag === DragState.LOCKED)
            {
                var dx = this.centerX - x;
                var dy = this.centerY - y;
                if (Math.sqrt(dx * dx + dy * dy) > 3)
                {
                    this.drag = DragState.ON;
                }
            }

            if (this.drag === DragState.ON)
            {
                this.centerX = x;
                this.centerY = y;

                this.updateViewBox();
            }
        }
    },

    onMouseUp: function (x, y)
    {
        var dragState = this.drag;
        this.drag = DragState.OFF;

        GlobalDrag.setActiveDrag(null);

        if (dragState === DragState.ON)
        {
            x = this.offsetX - GUIContext.applyZoom(x);
            y = this.offsetY - GUIContext.applyZoom(y);

            this.centerX = x;
            this.centerY = y;

            this.updateViewBox();

        }
        else if (dragState === DragState.LOCKED)
        {
            var fn = this.props.onInteraction;
            typeof fn == "function" && fn();
        }
    },

    render: function ()
    {
        var viewBox;
        var width = this.props.width;

        if (typeof this.centerX === "number")
        {
            viewBox = new ViewBox(this.centerX, this.centerY, this.props.height);
        }
        else
        {
            viewBox = new ViewBox(this.props.centerX, this.props.centerY, this.props.height);
        }

        return (
            <div className="gui-container">
                <svg
                    ref={ elem => this.svgElem = elem}
                    width={ width }
                    height={ this.props.height }
                    style={ this.props.style }
                    viewBox={ viewBox.render() }
                    preserveAspectRatio="xMidYMid meet"
                >
                    {/*
                        We use a dedicated oversized background rect to catch all mouse down events that actually
                        hit the background and not the element groups contained.

                        mouse up and mouse move events are registered via GlobalDrag.
                    */}
                    <rect
                        x={ viewBox.x - viewBox.size }
                        y={ viewBox.y - viewBox.size }
                        width={ viewBox.size * 3 }
                        height={ viewBox.size * 3 }
                        fill="transparent"
                        onMouseDown={ this.onMouseDown }
                        onTouchStart={ this.onMouseDown }
                    />
                    { this.props.children }

                </svg>
            </div>
        );
    }
});

module.exports = GUIContainer;
