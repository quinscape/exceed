import assert from "power-assert";
import assign from "object-assign"
// noinspection JSFileReferences
import BigNumber from "bignumber.js"

var domainService = require("../../../../src/main/js/service/domain");

const DOMAIN_DATA = {
    enumTypes: {
        MyEnum: {
            name: "MyEnum",
            values: ["A", "B", "C"]
        }
    },
    maxDecimalPlaces: 3,
    decimalConfig: assign({}, domainService.getDecimalConfig(), {
        defaultDecimalPlaces: 3,
        defaultTrailingZeroes: false
    })
};

const DOMAIN_DATA_TRAILING = assign({}, DOMAIN_DATA, {
    decimalConfig: assign({}, domainService.getDecimalConfig(), {
        defaultDecimalPlaces: 3,
        defaultTrailingZeroes: true
    })
});

domainService.init(DOMAIN_DATA);


var propertyConverter = require("../../../../src/main/js/service/property-converter").default;

function assertNeutralFromServer(value, propertyType)
{
    var result = propertyConverter.fromServer(value, propertyType);
    assert(result === value);
}

function assertNeutralToServer(value, propertyType)
{
    var result = propertyConverter.toServer(value, propertyType);
    assert(result === value);
}

function assertNeutralToUser(value, propertyType)
{
    var result = propertyConverter.toUser(value, propertyType);
    assert(result === value);
}

function assertNeutralFromUser(value, propertyType)
{
    var result = propertyConverter.fromUser(value, propertyType);
    assert(result.ok === true);
    assert(result.value === value);
}

function Foo(value)
{
    this.value = value;
}


describe("PropertyConverter", function ()
{
    describe("does 'fromServer' conversion", function ()
    {
        it("for Boolean", function ()
        {
            assertNeutralFromServer(true, { type : "Boolean" });
        });

        it("for Date", function ()
        {
            var result = propertyConverter.fromServer("2016-02-13T00:00:00Z", {
                "type" : "Date"
            });
            assert(result.toISOString() === "2016-02-13T00:00:00.000Z");
        });

        it("for Decimal", function ()
        {
            var result = propertyConverter.fromServer("1234.56", {
                "type" : "Decimal"
            });
            assert(result.toString() === "1234.56");
        });

        it("for Enum", function ()
        {
            assertNeutralFromServer(1, {
                "type" : "Enum",
                "typeParam" : "MyEnum",
            });
        });

        it("for Integer", function ()
        {
            assertNeutralFromServer(122, { type : "Integer" });
        });

        it("for Long", function ()
        {
            assertNeutralFromServer(9873408, { type : "Long" });
        });
        it("for PlainText", function ()
        {
            assertNeutralFromServer("abc", { type : "PlainText" });
        });
        it("for RichText", function ()
        {
            assertNeutralFromServer("<b>abc</b>", { type : "RichText" });
        });
        it("for Timestamp", function ()
        {
            var result = propertyConverter.fromServer("2016-02-14T12:34:56Z", {
                "type" : "Timestamp"
            });
            assert(result.toISOString() === "2016-02-14T12:34:56.000Z");
        });
        it("for UUID", function ()
        {
            assertNeutralFromServer("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });

    });

    describe("does 'toServer' conversion", function ()
    {
        it("for Boolean", function ()
        {
            assertNeutralToServer(true, { type : "Boolean" });
        });

        it("for Date", function ()
        {
            var result = propertyConverter.toServer(new Date("2016-02-13T12:34:56.789Z"), {
                "type" : "Date"
            });
            assert(result === "2016-02-13T00:00:00.000Z");
        });

        it("for Decimal", function ()
        {
            var result = propertyConverter.toServer("1234.56", {
                "type" : "Decimal"
            });
            assert(result.toString() === "1234.56");
        });

        it("for Enum", function ()
        {
            assertNeutralToServer(1, {
                "type" : "Enum",
                "typeParam" : "MyEnum",
            });
        });

        it("for Integer", function ()
        {
            assertNeutralToServer(122, { type : "Integer" });
        });

        it("for Long", function ()
        {
            assertNeutralToServer(9873408, { type : "Long" });
        });
        it("for PlainText", function ()
        {
            assertNeutralToServer("abc", { type : "PlainText" });
        });
        it("for RichText", function ()
        {
            assertNeutralToServer("<b>abc</b>", { type : "RichText" });
        });
        it("for Timestamp", function ()
        {
            var result = propertyConverter.toServer(new Date("2016-02-14T12:34:56.789Z"), {
                "type" : "Timestamp"
            });
            assert(result === "2016-02-14T12:34:56.789Z");
        });
        it("for UUID", function ()
        {
            assertNeutralToServer("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });

    });


    describe("does 'toUser' conversion", function ()
    {
        it("for Boolean", function ()
        {
            assertNeutralToUser(true, { type : "Boolean" });
        });

        it("for Date", function ()
        {
            var result = propertyConverter.toUser(new Date("2016-02-13T00:00:00Z"), {
                "type" : "Date"
            });
            assert(result === "2016-02-13T00:00:00.000Z");
        });

        it("for Decimal", function ()
        {
            var result = propertyConverter.toUser(new BigNumber("1234.56"), {
                "type" : "Decimal"
            });
            assert(result === "1,234.56");

            var result2 = propertyConverter.toUser(new BigNumber("12.3"), {
                type : "Decimal",
                config : {
                    decimalPlaces : 3,
                    trailingZeroes: true
                }
            });
            assert(result2 === "12.300");

            // test application defaults
            domainService.init(DOMAIN_DATA_TRAILING);

            var result3 = propertyConverter.toUser(new BigNumber("2.3"), {
                type : "Decimal"
            });
            assert(result3 === "2.300");

            domainService.init(DOMAIN_DATA);
        });

        it("for Enum", function ()
        {
            var result = propertyConverter.toUser(1, {
                "type" : "Enum",
                "typeParam" : "MyEnum",
            });
            assert(result === "B");
        });

        it("for Integer", function ()
        {
            assertNeutralToUser(122, { type : "Integer" });
        });

        it("for Long", function ()
        {
            assertNeutralToUser(9873408, { type : "Long" });
        });
        it("for PlainText", function ()
        {
            assertNeutralToUser("abc", { type : "PlainText" });
        });
        it("for RichText", function ()
        {
            assertNeutralToUser("<b>abc</b>", { type : "RichText" });
        });
        it("for Timestamp", function ()
        {
            var result = propertyConverter.toUser(new Date("2016-02-14T12:34:56Z"), {
                "type" : "Timestamp"
            });
            assert(result === "2016-02-14T12:34:56.000Z");
        });
        it("for UUID", function ()
        {
            assertNeutralToUser("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });

    });

    describe("does 'fromUser' conversion", function ()
    {
        it("for Boolean", function ()
        {
            assertNeutralFromUser(true, { type : "Boolean" });
        });
        it("for Date", function ()
        {
            var result = propertyConverter.fromUser("2016-02-13T12:34:56Z", {
                "type" : "Date"
            });
            assert(result.ok === true);
            assert(result.value.toISOString() === "2016-02-13T00:00:00.000Z");
        });
        it("for Decimal", function ()
        {
            var result = propertyConverter.fromUser("2,345.67", {
                "type" : "Decimal"
            });
            assert(result.ok === true);
            assert(result.value.toString() === "2345.67");
        });
        it("for Enum", function ()
        {
            var result = propertyConverter.fromUser("C", {
                "type" : "Enum",
                "typeParam" : "MyEnum"
            });
            assert(result.ok === true);
            assert(result.value === 2);
        });
        it("for Integer", function ()
        {
            assertNeutralFromUser(122, { type : "Integer" });
        });
        it("for Long", function ()
        {
            assertNeutralFromUser(9873408, { type : "Long" });
        });
        it("for PlainText", function ()
        {
            assertNeutralFromUser("abc", { type : "PlainText" });
        });
        it("for RichText", function ()
        {
            assertNeutralFromUser("<b>abc</b>", { type : "RichText" });
        });
        it("for Timestamp", function ()
        {
            var result = propertyConverter.fromUser("2016-02-13T12:34:56Z", {
                "type" : "Timestamp"
            });
            assert(result.ok === true);
            assert(result.value.toISOString() === "2016-02-13T12:34:56.000Z");
        });

        it("for UUID", function ()
        {
            assertNeutralFromUser("34049fc7-6c05-49bc-b9c2-bb60ce67cd87", { type : "UUID" });
        });
    });

    it("registers custom converters", function ()
    {
        propertyConverter.reset();

        assert.throws(function ()
        {
            propertyConverter.toUser("abc", { type : "Foo"});
        }, /No converters available for type 'Foo'/);
        assert.throws(function ()
        {
            propertyConverter.fromUser("abc", {type: "Foo"});
        }, /No converters available for type 'Foo'/);

        propertyConverter.register("Foo", false, false, function (value, propertyType)
        {
            return new propertyConverter.Result(new Foo(value));
        }, function (value, propertyType)
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

