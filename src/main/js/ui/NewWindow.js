import React from "react"
import { render } from "react-dom"

import Icon from "./Icon"

import uri from "../util/uri"
import sys from "../sys";

class NewWindow extends React.Component {

    static initialState = {
        opened: false
    };


    componentDidUpdate(prevProps, prevState)
    {
        const wasOpened = prevState && prevState.opened;
        const isOpened = this.state && this.state.opened;

        console.log("componentDidUpdate", wasOpened, isOpened);

        if (!wasOpened && isOpened)
        {
            const wnd = window.open();
            if (!wnd)
            {
                alert("Could not open window");
                return;
            }

            this._wnd = wnd;

            const div = wnd.document.createElement("div");

            const style = wnd.document.createElement("link");

            style.setAttribute("rel", "stylesheet");
            style.setAttribute("href", uri("/res/" + sys.appName + "/style/" + sys.appName + ".css"));

            wnd.document.getElementsByTagName("head")[0].appendChild(style);

            wnd.document.body.appendChild(div);

            render(
                <div>
                    { this.props.children() }
                </div>,
                div
            );
        }
        else if(isOpened)
        {
            this._wnd.focus();
        }
    }

    onClick = ev => {

        this.setState({
            opened: true
        });

        ev.preventDefault();
    };

    render()
    {
        // doesn't render in normal context
        return (
            <a
                href="#new-window"
                className="btn btn-link text-info"
                onClick={ this.onClick }
            >
                <Icon className="glyphicon-new-window"/>
                { " " }
                { this.props.text }
            </a>

        )
    }
}

export default NewWindow
