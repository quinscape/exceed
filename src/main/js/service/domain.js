import uuid from "uuid";
import assign from "object-assign"
// noinspection JSFileReferences
import BigNumber from "bignumber.js"

let applicationDomain;

let _decimalConfig =
    // dump from an empty de.quinscape.exceed.model.config.DecimalConfig instance
    {
        "defaultPrecision":0,
        "defaultDecimalPlaces":3,

        // bignumber.js options
        "MODULO_MODE": BigNumber.ROUND_DOWN,
        "CRYPTO":false,
        "ROUNDING_MODE": BigNumber.ROUND_HALF_UP,
        "FORMAT":{
            "fractionGroupSize":0,
            "groupSize":3,
            "secondaryGroupSize":0,
            "groupSeparator":",",
            "decimalSeparator":".",
            "fractionGroupSeparator":" "
        },
        "EXPONENTIAL_AT":[
            -7,
            20
        ],
        "POW_PRECISION":0,
    }
;

function lookupEnum(name)
{
    if (typeof name === "number")
    {
        return name;
    }

    const value = BigNumber[name];
    if (value === undefined)
    {
        throw new Error("Undefined enum value BigNumber." + name );
    }
    return value;
}

function configureDecimals()
{
    const {maxDecimalPlaces, decimalConfig} = applicationDomain;

    if (maxDecimalPlaces >= 0)
    {
        const config = assign(
            {},
            decimalConfig,
            {
                MODULO_MODE: lookupEnum(decimalConfig.MODULO_MODE),
                ROUNDING_MODE: lookupEnum(decimalConfig.ROUNDING_MODE),
                DECIMAL_PLACES: maxDecimalPlaces
            }
        );

        _decimalConfig = config;
        
        BigNumber.config(config);
    }
    else
    {
        _decimalConfig = BigNumber.config();
    }
}

module.exports =
    {
        create: function (type, id) {
            const haveId = typeof id !== "undefined";

            if (__SERVER)
            {
                return __domainService_create(type, id);
            }
            else
            {
                return {
                    "id": haveId ? id : uuid.v4(),
                    "_type": type
                };
            }
        },
        init: function (domainData) {
            //console.log("INIT DOMAIN", domainData);
            applicationDomain = domainData;

            configureDecimals();
        },
        /**
         * Returns the enum type model with the given name
         */
        getEnumType: function (name) {
            return applicationDomain.enumTypes[name];
        },
        getStateMachine: function (name) {
            return applicationDomain.stateMachines[name];
        },
        getDomainType: function (name) {
            return applicationDomain.domainTypes[name];
        },
        /**
         * Returns the enum type models
         */
        getEnumTypes: function () {
            return applicationDomain.enumTypes;
        },
        getDomainData: function () {
            return applicationDomain;
        },
        getDecimalConfig()
        {
            return _decimalConfig;
        },
        getDomainTypes: function () {
            return applicationDomain.domainTypes;
        }
    };
