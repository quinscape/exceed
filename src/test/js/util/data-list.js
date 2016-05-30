var assert = require("power-assert");
var sinon = require("sinon");

var changeSpy = sinon.spy();

var DataList = require("../../../../src/main/js/util/data-list");

var dl = new DataList(require("./test-lists/data-list.json"), function (newData, path)
{
    //console.log("CHANGED", newData, path);

    changeSpy(newData, path);
});

describe("DataList", function ()
{
    var cursor;

    it("provides data cursors", function ()
    {
        var call, newRows;

        cursor = dl.getCursor([0]);
        assert(cursor.type === "[DataListRoot]");
        assert(cursor.value.name === "TestFoo");
        assert(!cursor.isProperty());

        cursor.set(["name"], "AnotherFoo");
        call = changeSpy.getCall(0);
        newRows = call.args[0];
        assert(newRows[0].name === "AnotherFoo");
        assert.deepEqual(call.args[1], [0, "name"]);

        cursor.set(["embedded", "name"], "Changed Embedded");
        call = changeSpy.getCall(1);
        newRows = call.args[0];
        assert(newRows[0].embedded.name === "Changed Embedded");
        assert.deepEqual(call.args[1], [0, "embedded","name"]);

        cursor.merge(["bazes"], {
            "three" : { num: 3.1},
            "five"  : { num: 5}
        });
        call = changeSpy.getCall(2);
        newRows = call.args[0];
        assert(newRows[0].bazes.three.num === 3.1);
        assert(newRows[0].bazes.five.num === 5);
        assert.deepEqual(call.args[1], [0, "bazes"]);


        cursor = cursor.getCursor(["embedded"]);
        assert(cursor.type === "Embedded");
        assert(cursor.value.name === "Changed Embedded");
        assert(!cursor.isProperty());

        cursor = cursor.pop();
        assert(cursor.value.name === "AnotherFoo");

        cursor = dl.getCursor([0, "bars", 0]);
        assert(cursor.value.name === "Bar 1");
        assert(!cursor.isProperty());
        cursor = cursor.pop(2);
        assert(cursor.value.name === "AnotherFoo");


        // property cursor
        cursor = dl.getCursor([0, "name"]);

        //console.log("PROPERTY CURSOR", cursor);

        assert(cursor.value === "AnotherFoo");
        assert(cursor.isProperty());

        cursor.set(null, "YetAnotherFoo");
        call = changeSpy.getCall(3);
        newRows = call.args[0];
        assert(newRows[0].name === "YetAnotherFoo");

    });

    it("validates cursors", function ()
    {
        // checks if the used paths exist in the type declarations

        assert.throws(function ()
        {
            dl.getCursor(["nonNumeric"]);
        }, /Error: First key path entry must be a numeric row index/);

        assert.throws(function ()
        {
            dl.getCursor([0, "fake"]);
        }, /No column 'fake' in dataList columns/);

        assert.throws(function ()
        {
            dl.getCursor([0, "embedded", "fake"]);
        }, /Cannot find property for 'Embedded.fake'/);

        assert.throws(function ()
        {
            dl.getCursor([0, "bars", 0, "fake"]);
        }, /Cannot find property for 'Bar.fake'/);

        assert.throws(function ()
        {
            dl.getCursor([0, "bazes", "one", "fake"]);
        }, /Cannot find property for 'Baz.fake'/);

    });

    describe("DataListCursor", function ()
    {
        it("provides property type information", function ()
        {
            cursor = dl.getCursor([0]);
            assert(cursor.type === "[DataListRoot]");

            var propertyType = cursor.getPropertyType(["name"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 1);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["num"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "num");
            assert(propertyType.type === "Integer");
            assert(propertyType.data === 2);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["embedded", "name"]);
            assert(propertyType.parent === "Embedded");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 6);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["bars"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "bars");
            assert(propertyType.type === "List");
            assert(propertyType.typeParam === "Bar");
            assert(propertyType.data === 4);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["bars", 0, "name"]);
            assert(propertyType.parent === "Bar");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 7);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["bazes"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "bazes");
            assert(propertyType.type === "Map");
            assert(propertyType.typeParam === "Baz");
            assert(propertyType.data === 5);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["bazes", "one", "num"]);
            assert(propertyType.parent === "Baz");
            assert(propertyType.name === "num");
            assert(propertyType.type === "Integer");
            assert(propertyType.data === 8);
            assert(propertyType.dataList === dl);

            propertyType = cursor.getPropertyType(["joinedName"]);
            assert(propertyType.parent === "Joined");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 9);
            assert(propertyType.dataList === dl);

            cursor = dl.getCursor([0, "embedded", "name"]);

            assert(cursor.isProperty());

            propertyType = cursor.getPropertyType();
            assert(propertyType.parent === "Embedded");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 6);
            assert(propertyType.dataList === dl);

        });

        it("extracts implicitly typed domain objects", function ()
        {
            var simpleList = new DataList(require("./test-lists/simple-list.json"), null);

            var obj = simpleList.getCursor([1]).getDomainObject();

            assert(obj.name === "TestFoo #2");
            assert(obj.num === 234);
            assert(obj._type === "Foo");
        });

        it("extracts typed domain objects", function ()
        {
            var joinedList = new DataList(require("./test-lists/joined-list.json"), null);

            var obj = joinedList.getCursor([0]).getDomainObject("Foo");

            assert(obj.name === "TestFoo");
            assert(obj.num === 123);
            assert(obj._type === "Foo");

            obj = joinedList.getCursor([0]).getDomainObject("Baz");

            assert(obj.name === undefined);
            assert(obj.num === 1);
            assert(obj._type === "Baz");


            assert.throws(function ()
            {
                joinedList.getCursor([0]).getDomainObject();
            }, /Implicit type detection failed/);


        });



        it("extracts nested domain objects", function ()
        {
            var simpleList = new DataList(require("./test-lists/nested.json"), null);

            var obj = simpleList.getCursor([0, "embedded"]).getDomainObject();

            assert(obj._type === "Embedded");
            assert(obj.name === "EmbeddedObject");

            obj = simpleList.getCursor([0, "bars", 1]).getDomainObject();
            assert(obj._type === "Bar");
            assert(obj.name === "Bar 2");

            obj = simpleList.getCursor([0, "bazes", "one"]).getDomainObject();
            assert(obj._type === "Baz");
            assert(obj.num === 1);

            assert.throws(function ()
            {
                simpleList.getCursor([0, "bars"]).getDomainObject();
            }, /Cannot extract single domain object from List/)

            assert.throws(function ()
            {
                simpleList.getCursor([0, "bazes"]).getDomainObject();

            }, /Cannot extract single domain object from Map/)

        });
    })

    it("can be copied", function ()
    {
        var simpleList = new DataList(require("./test-lists/nested.json"), null);

        var newChange = function () {};

        var copy = new DataList(simpleList);
        var copy2 = new DataList(simpleList, newChange);

        // we expect the property lookup to be the same, saving on initialization costs for copies
        assert(copy.propertyLookup === simpleList.propertyLookup);
        assert(copy2.propertyLookup === simpleList.propertyLookup);

        // change callback can be either kept as-is, or changed
        assert(copy.onChange === simpleList.onChange);
        assert(copy2.onChange === newChange);
    });

});
