const React = require("react");
const GUIContext = require("./gui-context");
const ContainerContext = require("./container-context");
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
        zoom: React.PropTypes.bool,
        onInteraction: React.PropTypes.func
    },

    childContextTypes: {
        containerContext: React.PropTypes.instanceOf(ContainerContext)
    },

    getDefaultProps : function ()
    {
        return {
            centerX : 0,
            centerY : 0,
            width: "100%",
            height: 600,
            style: null,
            zoom: true,
            onInteraction: null
        };

    },

    getChildContext: function()
    {
        //console.log("GUIContainer.getChildContext");

        this.ctx = new ContainerContext(this);

        return {
            containerContext: this.ctx
        };
    },

    componentDidMount: function ()
    {
        console.log("GUIContainer mount");

        GUIContext.update();

        this.centerX = this.props.centerX;
        this.centerY = this.props.centerY;
        this.targetZoom = GUIContext.getZoom();
        this.dz = 0;
        this.animating = false;

        if (this.props.zoom && this.ctx.svgElem)
        {
            Event.add(this.ctx.svgElem, "mousewheel", this.onMouseWheel, false);
        }

        GlobalDrag.init();
    },
    componentWillUnmount: function ()
    {
        if (this.props.zoom && this.ctx.svgElem)
        {
            Event.remove(this.ctx.svgElem, "mousewheel", this.onMouseWheel, false);
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
        if (this.ctx.svgElem)
        {
            this.ctx.svgElem.setAttribute("viewBox", new ViewBox(this.centerX, this.centerY, this.props.height).render())
        }
    },

    onMouseWheel: function (ev)
    {
        if (GlobalDrag.isActiveDrag(this))
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
        this.dragLocked = true;

        var x = GUIContext.applyZoom(ev.pageX);
        var y = GUIContext.applyZoom(ev.pageY);

        this.offsetX = this.centerX + x;
        this.offsetY = this.centerY + y;

        GlobalDrag.setActiveDrag(this);

        return Event.preventDefault(ev);
    },

    onMouseMove: function (x, y)
    {
        if (GlobalDrag.isActiveDrag(this))
        {

            x = this.offsetX - GUIContext.applyZoom(x);
            y = this.offsetY - GUIContext.applyZoom(y);

            if (this.dragLocked)
            {
                var dx = this.centerX - x;
                var dy = this.centerY - y;

                var dist = Math.sqrt( dx * dx + dy * dy);

                console.log("LOCKED MOVE", dist);

                if (dist > 2)
                {
                    //console.log("ACTIVATE DRAG");
                    this.dragLocked = false;
                }
            }

            if (!this.dragLocked)
            {
                console.log("CONTAINER MOVE", x,y);

                this.centerX = x;
                this.centerY = y;

                this.updateViewBox();
            }
        }
    },

    onMouseUp: function (x, y)
    {
        if (GlobalDrag.isActiveDrag(this))
        {
            x = this.offsetX - GUIContext.applyZoom(x);
            y = this.offsetY - GUIContext.applyZoom(y);

            GlobalDrag.setActiveDrag(null);

            if (this.dragLocked)
            {
                var fn = this.props.onInteraction;
                typeof fn == "function" && fn();
            }
            else
            {
                this.centerX = x;
                this.centerY = y;

                this.updateViewBox();
            }
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
                    ref={ elem => this.ctx.init(elem) }
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
