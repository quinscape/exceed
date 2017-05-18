import cando from "../../../../src/main/js/cando"

var assert = require("power-assert");

var currentValue;

var storage = {};

// mock storage API
global.window = {
    localStorage: {
        getItem: function (name)
        {
            return storage[name];
        },
        setItem: function (name, value)
        {
            storage[name] = value;
        }

    }
};

cando.rerunFeatureTests();

import Settings from "../../../../src/main/js/util/settings";

describe("Settings", function ()
{
    var test = Settings.create("test", "test-default");

    it("uses configuration values defaults", function ()
    {
        assert(test() === "test-default");
    });

    it("writes configuration values", function ()
    {
        test("foo");
        assert(storage[Settings.NAME_PREFIX + "test"] === "\"foo\"");
    });

    it("reads configuration values", function ()
    {
        assert(test() === "foo");

        var newSetting = Settings.create("test2", "not-used");
        storage[Settings.NAME_PREFIX + "test2"] = "\"bar\"";
        assert(newSetting() === "bar");

    });

    it("detects non-unique setting names", function ()
    {
        assert.throws(function ()
        {
            Settings.create("test", 1);
        }, /Setting 'test' is already defined/)

    });

    it("flushes named cache elements", function ()
    {
        test("qux");
        var newSetting = Settings.create("test3", "not-used");
        assert(newSetting() === "not-used");

        Settings.flush("test3");

        storage[Settings.NAME_PREFIX + "test3"] = "\"bar\"";
        storage[Settings.NAME_PREFIX + "test"] = "\"not-visible\"";
        assert(newSetting() === "bar");
        assert(test() === "qux");

    });

    it("flushes cache", function ()
    {
        test("qux");
        var newSetting = Settings.create("test4", "not-used");
        assert(newSetting() === "not-used");

        Settings.flush();

        storage[Settings.NAME_PREFIX + "test4"] = "\"bar\"";
        storage[Settings.NAME_PREFIX + "test"] = "\"visible\"";
        assert(newSetting() === "bar");
        assert(test() === "visible");

    });
});
