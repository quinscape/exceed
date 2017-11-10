import React from "react"

import store from "../service/store"
import propertyConverter from "./property-converter"
import describeProperty from "../util/describe-property"
import { getCurrency } from "../reducers/meta"

let renderersByType;

function reset()
{
    renderersByType = {
        "Currency" : (value, propertyType) => {


            return (
                <span>
                    {
                        value + " "
                    }
                    <em className="text-muted">
                        {
                            getCurrency(store.getState(), propertyType)
                        }
                    </em>
                </span>
            );
        },

        "Boolean" : (value, propertyType) => {
            return (
                value ?
                    <span className="glyphicon glyphicon-check text-success" /> :
                    <span className="glyphicon glyphicon-remove-sign text-danger" />
            );
        }
    };
}

reset();

const propertyRenderer = {

    /**
     * Renders the given value as react element for a static, read-only context.
     * @param value
     * @param propertyType
     * @returns {*}
     */
    renderStatic: function (value, propertyType)
    {
        const converted = propertyConverter.toUser(value, propertyType);

        const renderer = renderersByType[propertyType.type];

        if (renderer)
        {
            return renderer(converted, propertyType);
        }

        return <span className={ "pv-" + describeProperty(propertyType, true) } >{ converted }</span>;
    },

    registerRenderer: function (type, renderer)
    {
        renderersByType[type] = renderer;
    },

    reset: reset
};

export default propertyRenderer
