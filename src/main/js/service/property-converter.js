const assign = require("object-assign");

var domainService = require("./domain");

var i18n = require("./i18n");

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


function Result(value)
{
    return {
        ok: true,
        value: value
    }
}


function checkMaxLength(value, propertyType)
{
    value = value || "";
    var maxLength = propertyType.maxLength;
    if (maxLength > 0 && value.length >= maxLength)
    {
        return TOO_LONG;
    }

    return Result(value);
}

var ToUserConverters;
var FromUserConverters;

function resetConverters()
{
    ToUserConverters = {
        "Boolean": Result,
        "Date": Result,
        "Enum": function (value, propertyType)
        {
            //console.log("ToUserConverters.ENUM ", value, propertyType);

            var enumName = propertyType.typeParam;
            var enumModel = domainService.getEnum(enumName);

            if (value < 0 || value >= enumModel.values.length)
            {
                throw new Error("Invalid ordinal for enum '" + enumName + "': " + value);
            }
            return Result(enumModel.values[value]);
        },
        "Integer": Result,
        "Long": Result,
        "PlainText": Result,
        "RichText": Result,
        "Timestamp": Result,
        "UUID": Result
    };
    FromUserConverters = {
        "Boolean": function (value, propertyType)
        {
            value = !!value;
            return Result(value);
        },
        "Date": function (value, propertyType)
        {
            var date = Date.parse(value);
            if (isNaN(date))
            {
                return INVALID_DATE;
            }
            var dateObj = new Date(date);
            dateObj.setUTCHours(0);
            dateObj.setUTCMinutes(0);
            dateObj.setUTCSeconds(0);
            dateObj.setUTCMilliseconds(0);
            return Result(dateObj.toISOString());
        },
        "Enum": function (value, propertyType)
        {
            var enumName = propertyType.typeParam;
            var enumModel = domainService.getEnum(enumName);
            var values = enumModel.values;
            for (var i = 0; i < values.length; i++)
            {
                if (value === values[i])
                {
                    return Result(i);
                }
            }

            throw new Error("Invalid value for enum '" + enumName + "': " + value);

        },
        "Integer": function (value, propertyType)
        {
            var num = +value;
            if (isNaN(num))
            {
                return INVALID_NUMBER;
            }
            else
            {
                if (num < INTEGER_MIN || num > INTEGER_MAX)
                {
                    return OUT_OF_RANGE;
                }
                return Result(num);
            }
        },
        "Long": function (value, propertyType)
        {
            var num = +value;

            if (isNaN(num))
            {
                return INVALID_NUMBER;
            }
            else
            {
                return Result(num);
            }
        },
        "PlainText": checkMaxLength,
        "RichText": checkMaxLength,
        "Timestamp": function (value, propertyType)
        {
            var date = Date.parse(value);
            if (isNaN(date))
            {
                return INVALID_DATE;
            }
            return Result(new Date(date).toISOString());
        },
        "UUID": Result
    };
}

resetConverters();

module.exports = {
    fromUser: function(value, propertyType)
    {
        if (!propertyType || !propertyType.type)
        {
            throw new Error("Need property type object")
        }

        if (propertyType.required && !value && typeof value !== "number")
        {
            return MISSING_VALUE;
        }

        var name = propertyType.type;
        var converter = FromUserConverters[name];

        if (!converter)
        {
            throw new Error("No fromUser converter for property type '" + name +"'");
        }

        return converter.call(null, value, propertyType);
    },
    toUser: function(value, propertyType)
    {
        if (!propertyType || !propertyType.type)
        {
            throw new Error("Need property type object")
        }

        if (propertyType.required && !value)
        {
            return assign(Result(""), MISSING_VALUE);
        }

        var name = propertyType.type;
        var converter = ToUserConverters[name];

        if (!converter)
        {
            throw new Error("No toUser converter for property type '" + name +"'");
        }

        return converter.call(null, value, propertyType);
    },
    registerToUser: function(name, fn)
    {
        ToUserConverters[name] = fn;
    },
    registerFromUser: function(name, fn)
    {
        FromUserConverters[name] = fn;
    },
    reset: resetConverters,
    Result: Result
};
