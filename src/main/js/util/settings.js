const cando = require("../cando");

const NAME_PREFIX = "exceed_setting.";

var settings = {};

function getValue(name, defaultValue)
{
    if (settings.hasOwnProperty(name))
    {
        return settings[name];
    }

    //console.log("cando.localStorage", cando.localStorage, window && window.localStorage);

    if (cando.localStorage)
    {
        var fromStorage = window.localStorage.getItem(NAME_PREFIX + name);
        var result = fromStorage ? JSON.parse(fromStorage) : defaultValue;
        settings[name] = result;
        return result;
    }
    return defaultValue;
}

function setValue(name, value)
{
    settings[name] = value;

    if (cando.localStorage)
    {
        window.localStorage.setItem(NAME_PREFIX + name,  JSON.stringify(value));
    }

}

var settingNames = {};

/**
 * The settings module provides access to named configuration settings that are backed by localStorage if available.
 *
 * It caches the current value and will not detect localStorage changes happening outside of this module. Call flush
 * in those cases
 *
 * @type {{flush: function, create: function, NAME_PREFIX: string}}
 */
module.exports = {
    /**
     * Flushes the internal cache to reread values from localStorage.
     *
     * @param [name]    {string} if given, clear only that setting will be flushed, else all known settings are flushed.
     */
    flush: function (name)
    {
        if (name === undefined)
        {
            settings = {};
        }
        else
        {
            delete settings[name];
        }
    },

    /**
     * Creates a helper function to get or set named setting values.
     *
     * @param name              unique name
     * @param defaultValue      default value to use if no setting is defined yet.
     * @returns {Function} function no-arg calls will return the setting value, calling it with a value sets that value.
     */
    create: function (name, defaultValue)
    {
        if (settingNames.hasOwnProperty(name))
        {
            throw new Error("Setting '" + name + "' is already defined");
        }

        settingNames[name] = true;

        return function (value)
        {
            if (value !== undefined)
            {
                setValue(name, value);
            }
            else
            {
                return getValue(name, defaultValue);
            }
        }
    },

    NAME_PREFIX: NAME_PREFIX
};
