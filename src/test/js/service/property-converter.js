var assert = require("power-assert");

var propertyConverter = require("../../../../src/main/js/service/property-converter");

function assumeNeutralToUser(value, propertyType)
{
    var result = propertyConverter.toUser(value, propertyType);
    assert(result.ok === true);
    assert(result.value === value);
}

function assumeNeutralFromUser(value, propertyType)
{
    var result = propertyConverter.fromUser(value, propertyType);
    assert(result.ok === true);
    assert(result.value === value);
}

const ENUM_DATALIST = {
    enums: {
        MyEnum: {
            name: "MyEnum",
            values: ["A", "B", "C"]
        }
    }
};

function Foo(value)
{
    this.value = value;
}


describe("PropertyConverter", function ()
{
    describe("to User-Type", function ()
    {
        it("Boolean -> UT", function ()
        {
            assumeNeutralToUser(true, { type : "Boolean" });
        });

        it("Date -> UT", function ()
        {
            // preliminary
            assumeNeutralToUser("2016-02-13T00:00:00Z", { type : "Date" });
        });

        it("Enum -> UT", function ()
        {
            var result = propertyConverter.toUser(1, {
                "type" : "Enum",
                "typeParam" : "MyEnum",
                // data list property resolution should provide the dataList property
                dataList: ENUM_DATALIST
            });
            assert(result.ok === true);
            assert(result.value === "B");
        });

        it("Integer -> UT", function ()
        {
            assumeNeutralToUser(122, { type : "Integer" });
        });

        it("Long -> UT", function ()
        {
            assumeNeutralToUser(9873408, { type : "Long" });
        });
        it("PlainText -> UT", function ()
        {
            assumeNeutralToUser("abc", { type : "PlainText" });
        });
        it("RichText -> UT", function ()
        {
            assumeNeutralToUser("<b>abc</b>", { type : "RichText" });
        });
        it("Timestamp -> UT", function ()
        {
            assumeNeutralToUser("2016-02-13T16:34:33Z", { type : "Timestamp" });
        });
        it("UUID -> UT", function ()
        {
            assumeNeutralToUser("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });

    });

    describe("from User-Type", function ()
    {
        it("UT -> Boolean", function ()
        {
            assumeNeutralFromUser(true, { type : "Boolean" });
        });
        it("UT -> Date", function ()
        {
            // preliminary
            assumeNeutralFromUser("2016-02-13T00:00:00.000Z", { type : "Date" });
        });
        it("UT -> Enum", function ()
        {
            var result = propertyConverter.fromUser("C", {
                "type" : "Enum",
                "typeParam" : "MyEnum",
                // data list property resolution should provide the dataList property
                dataList: ENUM_DATALIST
            });
            assert(result.ok === true);
            assert(result.value === 2);
        });
        it("UT -> Integer", function ()
        {
            assumeNeutralFromUser(122, { type : "Integer" });
        });
        it("UT -> Long", function ()
        {
            assumeNeutralFromUser(9873408, { type : "Long" });
        });
        it("UT -> PlainText", function ()
        {
            assumeNeutralFromUser("abc", { type : "PlainText" });
        });
        it("UT -> RichText", function ()
        {
            assumeNeutralFromUser("<b>abc</b>", { type : "RichText" });
        });
        it("UT -> Timestamp", function ()
        {
            assumeNeutralFromUser("2016-02-13T16:34:33.000Z", { type : "Timestamp" });
        });

        it("UT -> UUID", function ()
        {
            assumeNeutralFromUser("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });
    });

    it("registers custom converters", function ()
    {
        propertyConverter.reset();

        assert.throws(function ()
        {
            propertyConverter.toUser("abc", { type : "Foo"});
        }, /No toUser converter for property type 'Foo'/)
        assert.throws(function ()
        {
            propertyConverter.fromUser("abc", {type: "Foo"});
        }, /No fromUser converter for property type 'Foo'/)

        propertyConverter.registerFromUser("Foo", function (value, propertyType)
        {
            return new propertyConverter.Result(new Foo(value));
        });

        propertyConverter.registerToUser("Foo", function (value, propertyType)
        {
            return new propertyConverter.Result(value.value);
        });

        var result = propertyConverter.toUser(new Foo("abc"), { type : "Foo"});
        assert(result.ok === true);
        assert(result.value === "abc");

        result = propertyConverter.fromUser("abc", { type : "Foo"});
        assert(result.ok === true);
        assert(result.value instanceof Foo);
        assert(result.value.value === "abc");
    });
});

