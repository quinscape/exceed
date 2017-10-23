const assign = require("object-assign");
const React = require("react");
const ReactDOM = require("react-dom");

import UIState from "./ui-state";

const elements = {};

const CONTAINER_ID = "focus-proxy-container";
const ZOOM_FACTOR = 8;

var zoom = ZOOM_FACTOR;

var updateTimerId;
var proxyContainer;

class FocusProxies extends React.Component
{
    onFocus(ev)
    {
        var id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.FOCUSED);
    }
    onBlur(ev)
    {
        var id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.NORMAL);
    }

    onUpdate()
    {
        this.forceUpdate();
    }

    render()
    {
        //console.log("render proxies", this.props.elements);
        var proxies = [];
        var elements = this.props.elements;
        for (var id in elements)
        {
            if (elements.hasOwnProperty(id))
            {
                var elem = elements[id];
                var isDisabled = elem.uiState === UIState.DISABLED;
                //console.log("render proxy", elem, isDisabled);
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
}

function getProxyContainer()
{
    if (!proxyContainer)
    {
        proxyContainer = document.createElement("div");
        proxyContainer.setAttribute("id", CONTAINER_ID);
        document.body.appendChild(proxyContainer);
    }
    return proxyContainer;
}

function renderProxies()
{
    updateTimerId = null;
    var proxyContainer = getProxyContainer();

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

        updateTimerId = setTimeout(renderProxies, 10);
    },
    _register: function (guiElem)
    {
        var id = guiElem.props.id;

        //console.log("_register", id);

        var elem = elements[id];
        if (!elem)
        {
            elem = {
                id : guiElem.props.id,
                uiState : guiElem.props.uiState,
                onUpdate : guiElem.props.onUpdate,
                onInteraction : guiElem.props.onInteraction
            };
            elements[id] = elem;

            //console.log("NEW ELEMENT", elem);
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
            return defaultUiState;
        }

        return elem.uiState;
    },
    focus: function(id)
    {
        var elem = proxyContainer.firstChild;
        while (elem)
        {
            if (elem.getAttribute("data-id") === id)
            {
                elem.focus();
            }
            elem = elem.nextSibling;
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

        if (!noUpdate)
        {
            elem.onUpdate.call(null);
        }
    },
    setZoom: function (newZoom)
    {
        zoom = newZoom;
    },
    getZoom: function ()
    {
        return zoom;
    },
    applyZoom: function (v)
    {
        return v * zoom / ZOOM_FACTOR;
    }

};

class FocusProxies extends React.Component
{
    onFocus(ev)
    {
        const id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.FOCUSED);
    }
    onBlur(ev)
    {
        const id = ev.target.dataset.id;
        GUIContext._setElementState(id, UIState.NORMAL);
    }

    onUpdate()
    {
        this.forceUpdate();
    }

    render()
    {
        //console.log("render proxies", this.props.elements);
        const proxies = [];
        const elements = this.props.elements;
        for (let id in elements)
        {
            if (elements.hasOwnProperty(id))
            {
                const elem = elements[id];
                const isDisabled = elem.uiState === UIState.DISABLED;
                //console.log("render proxy", elem, isDisabled);
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
}

function getProxyContainer()
{
    if (!proxyContainer)
    {
        proxyContainer = document.createElement("div");
        proxyContainer.setAttribute("id", CONTAINER_ID);
        document.body.appendChild(proxyContainer);
    }
    return proxyContainer;
}

function renderProxies()
{
    updateTimerId = null;
    const proxyContainer = getProxyContainer();

    ReactDOM.render(React.createElement(FocusProxies, {
        elements: elements
    }), proxyContainer/*, function ()
    {
//        console.log("proxies updated");
    }*/);
}

export default GUIContext
