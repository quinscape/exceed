
const Event = require("./event");

var activeDragObject;

var initCount = 0;

var eventPos = { x: 0, y: 0};

function globalMouseMove(ev)
{
    if (activeDragObject)
    {
        Event.position(ev, eventPos);

        activeDragObject.onMouseMove(eventPos.x, eventPos.y);

        return Event.preventDefault(ev);
    }
}

function globalMouseUp(ev)
{
    if (activeDragObject)
    {
        Event.position(ev, eventPos);

        activeDragObject.onMouseUp(eventPos.x, eventPos.y);
        activeDragObject = null;
        return Event.preventDefault(ev);
    }
}

/**
 * Helper module for drag and drop operations and the fact that we register mousemove and mouseup on a different target
 * than mousedown usually. mousedown needs to be on the element to be dragged to initiate the drag, once the dragging has
 * started, it is better to register mousemove and mouseup globally so that moving the mouse out of the container does
 * not as easily mess up interaction when we miss a mouse up. It also allows us to intuitively move objects out of the
 * screen and not have it stuck at the border.
 *
 * @type {{init: GlobalDrag.init, destroy: GlobalDrag.destroy, setActiveDrag: GlobalDrag.setActiveDrag}}
 */
var GlobalDrag = {
    init: function ()
    {
        if (initCount === 0)
        {
            //console.log("INIT GLOBAL DRAG");

            Event.add(window, "mousemove", globalMouseMove, false);
            Event.add(window, "mouseup", globalMouseUp, false);
        }

        initCount++;
    },
    destroy: function ()
    {
        if (--initCount === 0)
        {
            //console.log("DESTROY GLOBAL DRAG");

            Event.remove(window, "mousemove", globalMouseMove, false);
            Event.remove(window, "mouseup", globalMouseUp, false);
        }
    },
    setActiveDrag: function (dragObject)
    {
        //console.log("ACTIVE DRAG", dragObject);

        activeDragObject = dragObject;
    },
    isActiveDrag: function (dragObject)
    {
        return activeDragObject === dragObject;
    }

};

module.exports = GlobalDrag;
