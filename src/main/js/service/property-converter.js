import React from "react"
import assign from "object-assign"
// noinspection JSFileReferences
import BigNumber from "bignumber.js"


import parseNumber from "../util/parse-number"
import describeProperty from "../util/describe-property"
import i18n from "./i18n"

import store from "../service/store"
import { getCurrency } from "../reducers/meta"

const domainService = require("./domain");


const CURRENCY_MULTIPLIER = 10000;

const INVALID_DECIMAL = {
    ok: false,
    error: i18n("Invalid Decimal")
};

const INVALID_CURRENCY = {
    ok: false,
    error: i18n("Invalid Currency")
};

const INVALID_NUMBER = {
    ok: false,
    error: i18n("Invalid Number")
};

const INVALID_DATE = {
    ok: false,
    error: i18n("Invalid Date")
};


const INTEGER_MIN = -Math.pow(2,31);
const INTEGER_MAX = Math.pow(2,31) - 1;

const OUT_OF_RANGE = {
    ok: false,
    error: i18n("Number out of range")
};

const MISSING_VALUE = {
    ok: false,
    error: i18n("Value is required")
};

const TOO_LONG = {
    ok: false,
    error: i18n("Value too long")
};

const INVALID_ENUM_VALUE = {
    ok: false,
    error: i18n("Invalid enum value")
};


function Result(value)
{
    return {
        ok: true,
        value: value
    };
}


function checkMaxLength(value, propertyType)
{
    value = value || "";
    const maxLength = propertyType.maxLength;
    if (maxLength > 0 && value.length >= maxLength)
    {
        return TOO_LONG;
    }

    return Result(value);
}

let converters;
let renderersByType;

function normDate(dateObj){
    dateObj.setUTCHours(0);
    dateObj.setUTCMinutes(0);
    dateObj.setUTCSeconds(0);
    dateObj.setUTCMilliseconds(0);
    return dateObj;
}

function fromServerDate(value, propertyType)
{
    const date = new Date(value);
    //console.log({date});

    if (propertyType.type === "Date")
    {
        return normDate(date);
    }
    return date;
}

function fromUserDate(value, propertyType)
{
    const date = Date.parse(value);
    if (isNaN(date))
    {
        return assign(Result(value), INVALID_DATE);
    }

    const dateObj = new Date(date);

    if (propertyType.type === "Date")
    {
        normDate(dateObj);
    }
    return Result(dateObj);
}

function toUserDate(value, propertyType)
{
    return value.toISOString();
}
function toServerDate(value, propertyType)
{
    if (propertyType.type === "Date")
    {
        return normDate(value).toISOString();
    }
    return value.toISOString();
}

function getTrailingZeroes(propertyType)
{
    const trailingZeroes = propertyType.config && propertyType.config.trailingZeroes;
    if (trailingZeroes !== undefined)
    {
        return !!trailingZeroes;
    }
    else
    {
        return !!domainService.getDecimalConfig().defaultTrailingZeroes;
    }
}

function getDecimalPlaces(propertyType)
{
    const decimalPlaces = propertyType.config && propertyType.config.decimalPlaces;
    if (decimalPlaces !== undefined)
    {
        return decimalPlaces;
    }
    else
    {
        return domainService.getDecimalConfig().defaultDecimalPlaces;
    }

}

const NO_CONVERSION = {
    fromServer: false,
    toServer: false,
    fromUser: false,
    toUser: false
};


function getEnumType(enumName)
{
    const enumType = domainService.getEnumType(enumName);

    if (!enumType)
    {
        throw new Error("Enum Type '" + enumName + "' not found");
    }
    return enumType;
}


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

    converters = {
        "Boolean": {
            fromServer: false,
            toServer: false,
            fromUser: function (value, propertyType) {
                value = !!value;
                return Result(value);
            },
            toUser: false
        },
        "Enum": {
            fromServer: false,
            toServer: false,
            fromUser: function (value, propertyType)
            {
                const values = getEnumType(propertyType.typeParam).values;

                let index = -1;
                for (let i = 0; i < values.length; i++)
                {
                    if (values[i] === value)
                    {
                        index = i;
                        break;
                    }
                }

                if (index < 0)
                {
                    return assign(Result(value), INVALID_ENUM_VALUE);
                }
                return Result(index);
            },
            toUser: function (value, propertyType) {
                //console.log("ToUserConverters.ENUM ", value, propertyType);
                return getEnumType(propertyType.typeParam).values[value];
            }
        },
        "Integer": {
            fromServer: false,
            toServer: false,
            fromUser: function (value, propertyType) {

                if (value === "")
                {
                    return assign(Result(""), INVALID_NUMBER);
                }

                const num = +value;

                if (isNaN(num))
                {
                    return assign(Result(value), INVALID_NUMBER);
                } else
                {
                    if (num < INTEGER_MIN || num > INTEGER_MAX)
                    {
                        return assign(Result(value), OUT_OF_RANGE);
                    }
                    return Result(num);
                }
            },
            toUser: false
        },
        "Long": {
            fromServer: false,
            toServer: false,
            fromUser: function (value, propertyType) {
                const num = +value;

                if (isNaN(num))
                {
                    return assign(Result(value), INVALID_NUMBER);
                } else
                {
                    return Result(num);
                }
            },
            toUser: false
        },
        "PlainText": {
            fromServer: false,
            toServer: false,
            fromUser: checkMaxLength,
            toUser: false
        },
        "RichText": {
            fromServer: false,
            toServer: false,
            fromUser: checkMaxLength,
            toUser: false
        },
        "UUID": NO_CONVERSION,
        "Map": NO_CONVERSION,
        "List": NO_CONVERSION,
        "Object": NO_CONVERSION,
        "Currency": {
            fromServer: false,
            toServer: false,
            fromUser: function (value, propertyType) {
                try
                {
                    return Result(parseNumber(value).times(CURRENCY_MULTIPLIER).round().toNumber());
                } catch (e)
                {
                    return assign(Result(value), INVALID_CURRENCY);
                }
            },
            toUser: function (value, propertyType) {
                return new BigNumber(value / CURRENCY_MULTIPLIER).toFormat(2);
            }
        },
        "Date": {
            fromServer: fromServerDate,
            toServer: toServerDate,
            fromUser: fromUserDate,
            toUser: toUserDate
        },
        "Timestamp": {
            fromServer: fromServerDate,
            toServer: toServerDate,
            fromUser: fromUserDate,
            toUser: toUserDate
        },
        "Decimal": {
            fromServer: function (value, propertyType) {
                return new BigNumber(value);
            },
            toServer: function (value, propertyType) {
                return value.toString();
            },
            fromUser: function (value, propertyType) {
                try
                {
                    return Result(parseNumber(value));
                } catch (e)
                {
                    return assign(Result(value), INVALID_DECIMAL);
                }
            },
            toUser: function (value, propertyType) {
                if (getTrailingZeroes(propertyType))
                {
                    const decimalPlaces = getDecimalPlaces(propertyType);
                    return value.toFormat(decimalPlaces);
                }

                return value.toFormat();
            }
        },
        "State": NO_CONVERSION
    };
}

reset();

function getEntry(name)
{
    const entry = converters[name];

    if (!entry)
    {
        throw new Error("No converters available for type '" + name +"'");
    }

    return entry;
}

/**
 * Common converter function factory.
 *
 * @param converterName     name of the conversion for error message purposes
 * @param value             {*} value to convert
 * @param propertyType      {object} property type
 * @returns converter result
 */
function convert(converterName, value, propertyType)
{

    if (!propertyType || !propertyType.type)
    {
        throw new Error("Need property type object")
    }

    const name = propertyType.type;

    const converter = getEntry(name)[converterName];

    let result;
    if (converter === false)
    {
        result = value;
    }
    else
    {
        if (!converter)
        {
            throw new Error("No " + converterName + " converter for property type '" + name +"'");
        }
        result = converter.call(null, value, propertyType);
    }

    //console.log("convert " + converterName, value, { type: propertyType }, result);

    return result;
}


const propertyConverter = {

    toServer: function (value, propertyType)
    {
        return convert("toServer", value, propertyType);
    },

    fromServer: function (value, propertyType)
    {
        return convert("fromServer", value, propertyType);
    },

    toUser: function (value, propertyType)
    {
        const result = convert("toUser", value, propertyType);

        if (result === null || result === undefined)
        {
            return "";
        }

        return result;
    },

    /**
     * Renders the given value as react element for a static, read-only context.
     * @param value
     * @param propertyType
     * @returns {*}
     */
    renderStatic: function (value, propertyType)
    {
        const converted = this.toUser(value, propertyType);

        const renderer = renderersByType[propertyType.type];

        if (renderer)
        {
            return renderer(converted, propertyType);
        }

        return <span className={ "pv-" + describeProperty(propertyType, true) } >{ converted }</span>;
    },

    fromUser: function(value, propertyType)
    {
        if (!propertyType || !propertyType.type)
        {
            throw new Error("Need property type object")
        }

        if (propertyType.required && (value === null || value === ""))
        {
            return MISSING_VALUE;
        }

        const name = propertyType.type;
        const converter = getEntry(name).fromUser;
        if (converter === false)
        {
            return new Result(value);
        }
        if (!converter)
        {
            throw new Error("No fromUser converter for property type '" + name +"'");
        }

        return converter(value, propertyType);
    },

    register: function (name, fromServerConverter, toServerConverter, fromUserConverter, toUserConverter)
    {
        converters[name] = {
            fromServer: fromServerConverter,
            toServer: toServerConverter,
            fromUser: fromUserConverter,
            toUser: toUserConverter
        };
    },


    registerRenderer: function (type, renderer)
    {
        renderersByType[type] = renderer;
    },

    reset: reset,
    Result: Result
};

export default propertyConverter
