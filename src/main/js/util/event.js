/**
 * Helper module for browser events we need to do by hand in some cases where React doesn't help us.
 *
 * It just provides a thin compatibility layer for the use cases we need here.
 *
 */

const MOUSEWHEEL = "mousewheel";

var Event;

var env = {};

const PENDING = "pending";

/**
 * Creates a feature test instrumented Event object from the given spec
 * @param spec
 * @returns {{}}
 */
function runFeatureTests(spec)
{

    var out = {};
    for (var name in spec)
    {
        if (spec.hasOwnProperty(name))
        {
            var array = spec[name];
            out[name] = probe(name, array);
            env[name] = PENDING;
        }
    }

    return out;
}

/**
 * Probe function factory.
 *
 * Receives a name and an array of three functions. The probe function itself is stored in Event. On first call, the first
 * probe function checks the environment and returns a value if it is truthy the second function replaces the probe function
 * if it is falsy, the third function will be used from now on.
 *
 *
 * @param name
 * @param array
 * @returns {Function}
 */
function probe(name, array)
{
    if (!array || array.length !== 3)
    {
        throw new Error("Need exactly 3 functions in probe array")
    }

    return function()
    {
        var result = array[0].apply(Event, arguments);
        env[name] = result;

        // overwrite the probe with the function corresponding to the first result
        Event[name] = result ? array[1] : array[2];

        return Event[name].apply(Event, arguments);
    };
}


// W3C DOM
if (typeof window === "undefined")
{
    Event =
    {
        env: "no-op",
        add: function (elem, eventName, func, capture)
        {
        },
        remove: function (elem, eventName, func, capture)
        {
        },
        preventDefault: function(ev)
        {
        }
    }

}
else
{
    Event = runFeatureTests({
        add: [
            function (elem, eventName, func, capture)
            {
                return !!elem.addEventListener;
            },
            function (elem, eventName, func, capture)
            {
                if (eventName === MOUSEWHEEL)
                {
                    elem.addEventListener(MOUSEWHEEL, func, capture);
                    elem.addEventListener("DOMMouseScroll", func, capture);
                }
                else
                {
                    elem.addEventListener(eventName, func, capture);
                }
            },
            function (elem, eventName, func, capture)
            {
                elem.attachEvent("on" + eventName, func);
            }],
        remove: [
            function (elem, eventName, func, capture)
            {
                return !!elem.removeEventListener;
            },
            function (elem, eventName, func, capture)
            {
                if (eventName === MOUSEWHEEL)
                {
                    elem.removeEventListener(MOUSEWHEEL, func, capture);
                    elem.removeEventListener("DOMMouseScroll", func, capture);
                }
                else
                {
                    elem.removeEventListener(eventName, func, capture);
                }
            },
            function (elem, eventName, func, capture)
            {
                elem.detachEvent("on" + eventName, func);
            }],
        position: [
            function (ev, pos)
            {
                return typeof ev.pageX == "number" && typeof ev.pageY ==     "number"
            },
            function (ev, pos)
            {
                pos.x = ev.pageX;
                pos.y = ev.pageY;
            },
            function (ev, pos)
            {
                var event = ev || window.event;
                pos.x = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
                pos.y = event.clientY + document.body.scrollTop + document.documentElement.scrollTop;
            }
        ],
        preventDefault: [
            function(ev)
            {
                return !!ev.preventDefault;
            },
            function(ev)
            {
                ev.preventDefault();
                return false;
            },
            function(ev)
            {
                ev.returnValue = false;
                return false;
            }
        ]
    });

    Event.env = env;
    Event.PENDING = PENDING;
}

//window.ExceedEvent = Event;

export default Event
