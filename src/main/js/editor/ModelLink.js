import React from "react"
import cx from "classnames"
import store from "../service/store";

const uri = require("../util/uri");
const sys = require("../sys");

import { navigateEditor } from "../actions/editor/editorView"

class ModelLink extends React.Component {

    navigate = (ev) => {

        ev.preventDefault();
        
        try
        {
            const { type, name, params} = this.props;

            store.dispatch(
                navigateEditor({
                    type: type,
                    name: name,
                    detail: params && params.detail,
                    resultType: params && params.resultType,
                    uri: this.renderURI()
                })
            );

        }
        catch(e)
        {
            console.error(e);
        }
    };


    render()
    {
        const { type, name, children, currentLocation, filter } = this.props;

        const haveName = !!name;
        const isActive = currentLocation.type === type && (!name && !currentLocation.name || currentLocation.name === name);

        const tag = isActive ? "span" : "a";

        if (filter && (haveName ? name : type).indexOf(filter) < 0)
        {
            return false;
        }

        //console.log(type, name, currentLocation, isActive);

        return (
            React.createElement(tag, {
                    className: "model-link",
                    href: isActive ? null : this.renderURI(),
                    onClick: this.navigate
                }, children
            )
        );
    }

    renderURI()
    {
        const { type, name, params, hash } = this.props;

        const haveName = !!name;
        return uri("/editor/{app}/{type}{name}", {
            app: sys.appName,
            type: type,
            name: haveName ? "/" + name : "",
            detail: params && JSON.stringify(params.detail),
            resultType: params && params.resultType,
        }) + (hash ? "#" + hash : "");
    }
}

export default ModelLink


