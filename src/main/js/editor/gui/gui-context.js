import React from "react";
import ReactDOM from "react-dom";

import UIState from "./ui-state";

const elements = {};

const CONTAINER_ID = "focus-proxy-container";
const ZOOM_FACTOR = 8;

let zoom = ZOOM_FACTOR;

let updateTimerId;
let proxyContainer;

const GUIContext = {
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
        const id = guiElem.props.id;

        //console.log("_register", id);

        let elem = elements[id];
        if (!elem)
        {
            elem = {
                id : guiElem.props.id,
                uiState : guiElem.props.uiState,
                onUpdate : guiElem.props.onUpdate,
                onFocus : guiElem.props.onFocus,
                onInteraction : guiElem.props.onInteraction,
                data : guiElem.props.data
            };
            elements[id] = elem;

            //console.log("NEW ELEMENT", elem);
        }
        GUIContext.update();

        return elem;
    },
    _deregister: function (guiElem)
    {
        const id = guiElem.props.id;

        //console.log("_deregister", id);

        let elem = elements[id];
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
        let elem = elements[id];
        if (!elem)
        {
            return defaultUiState;
        }

        return elem.uiState;
    },
    focus: function(id)
    {
        let elem = proxyContainer.firstChild;
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

        let elem = elements[id];
        if (!elem)
        {
            throw new Error("Id '" + id + "' is not registered");
        }

        if (uiState === UIState.FOCUSED && elem.onFocus)
        {
            elem.onFocus();
        }

        elem.uiState = uiState;

        if (!noUpdate)
        {
            elem.onUpdate && elem.onUpdate.call(null);
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
