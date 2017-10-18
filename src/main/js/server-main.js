// setImmediate from promises is provided on the server-side by a java host object
import { Promise } from "es6-promise-polyfill"

import BigNumber from "bignumber.js"
import bindToGlobal from "./util/bind-to-global"

import domainService from "./service/domain"
import propertyConverter from "./service/property-converter";
import cast from "./util/cast";
import when from "./util/when";

function filterOdd(value, index)
{
    return (index & 1) !== 0;
}

/**
 * All properties of this object are bound to the global object as non-writable property for easy access
 */
module.exports = bindToGlobal({
    _a: {},
    _v: {
        isNew: function (domainObject) {
            return domainObject && domainObject.id === null;
        },
        cast: cast,
        when: when,
        conditional : function (cond)
        {
            if (cond)
            {
                return Promise.resolve(true);
            }
            else
            {
                return Promise.reject(false);
            }
        },
        debug: function (data)
        {
            const len = data && data.length;
            if (typeof len !== "number" || len < 2)
            {
                return;
            }

            let txt = data[0] + " = " + data[1];
            for (let i = 2; i < data.length; i+=2)
            {
                txt += ", " + data[i] + " = " + data[i + 1];
            }
            console.debug(txt);

            return data[1];
        }
    },
    _domainService: domainService,
    _converter: propertyConverter,
    _now: function () {
        return new Date();
    },

    _decimal: function (val)
    {
        return new BigNumber(val)
    },

    Promise: Promise

});
