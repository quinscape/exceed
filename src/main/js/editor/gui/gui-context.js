var assign = require("object.assign").getPolyfill();
var React = require("react");
var ReactDOM = require("react-dom");

var UIState = require("./ui-state");

var elements = {};

function GUIElementContext(id, uiState)
{
    this.id = id;
    this.uiState = uiState;
    this.onUpdate = null;
    this.onInteraction = null;
}

const CONTAINER_ID = "focus-proxy-container";

var updateTimerId;

var FocusProxies = React.createClass({
    onFocus: function (ev)
    {
        var id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.FOCUSED);
    },
    onBlur: function (ev)
    {
        var id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.NORMAL);
    },

    onUpdate: function ()
    {
        this.forceUpdate();
    },

    render: function ()
    {
        //console.log("render proxies", this.props.elements);
        var proxies = [];
        var elements = this.props.elements;
        for (var id in elements)
        {
            if (elements.hasOwnProperty(id))
            {
                var elem = elements[id];
                //console.log("render proxy", elem);
                var isDisabled = elem.uiState === UIState.DISABLED;
                proxies.push(
                    React.createElement(
                        isDisabled ? "span" : "a", {
                            key: id,
                            href:"#",
                            "data-id": id,
                            onClick: isDisabled ? null : elem.onInteraction
                        },
                        id
                    )
                );
            }
        }


        return (
            <div onFocusCapture={ this.onFocus } onBlurCapture={ this.onBlur }>
                { proxies }
            </div>
        );
    }
});

function renderProxies()
{
    var proxyContainer = document.getElementById(CONTAINER_ID);
    if (!proxyContainer)
    {
        proxyContainer = document.createElement("div");
        proxyContainer.setAttribute("id", CONTAINER_ID);
        document.body.appendChild(proxyContainer);
    }

    ReactDOM.render(React.createElement(FocusProxies, {
        elements: elements
    }), proxyContainer/*, function ()
    {
        console.log("proxies updated");
    }*/);
}

var GUIContext = {
    UIState : UIState,
    update: function ()
    {
        if (updateTimerId)
        {
            clearTimeout(updateTimerId);
        }

        updateTimerId = setTimeout(function ()
        {
            updateTimerId = null;
            renderProxies();
        }, 15);
    },
    _register: function (guiElem)
    {
        var id = guiElem.props.id;

        //console.log("_register", id);

        var elem = elements[id];
        if (elem != null)
        {
            elem.onUpdate = guiElem.props.onUpdate;
            elem.onInteraction = guiElem.props.onInteraction;
        }
        else
        {
            elem = new GUIElementContext(id, UIState.NORMAL);
            elements[id] = elem;
        }

        GUIContext.update();

        return elem;
    },
    _deregister: function (guiElem)
    {
        var id = guiElem.props.id;

        //console.log("_deregister", id);

        var elem = elements[id];
        if (!elem)
        {
            throw new Error("Id '" + id + "' is not registered");
        }
        delete elements[id];

        GUIContext.update();
    },
    _getElements: function(id)
    {
        return elements;
    },
    getElementState: function(id, defaultUiState)
    {
        var elem = elements[id];
        if (!elem)
        {
            elem = new GUIElementContext(id, defaultUiState);
            elements[id] = elem;

            return defaultUiState;
        }

        return elem.uiState;
    },
    focus: function(id)
    {
        var proxy = document.querySelector("a[data-id='" + id + "']");

        if (proxy)
        {
            proxy.focus();
        }
    },
    _setElementState: function(id, uiState, noUpdate)
    {
        //console.log("Set %s to %s", id, uiState);

        if (!UIState.isValid(uiState))
        {
            throw new Error("Invalid ui state " + uiState);
        }

        var elem = elements[id];
        if (!elem)
        {
            throw new Error("Id '" + id + "' is not registered");
        }

        elem.uiState = uiState;

        !noUpdate && elem.onUpdate && elem.onUpdate.call(null);
    }
};

module.exports = GUIContext;
