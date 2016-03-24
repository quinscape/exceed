var assert = require("power-assert");
var sinon = require("sinon");

var changeSpy = sinon.spy();

var DataList = require("../../../../src/main/js/util/data-list");

describe("DataList Helper", function ()
{
    var dl = new DataList({
        types: {
            "Foo": {
                "properties": [
                    {
                        "name": "name",
                        "type": "PlainText",
                        "data" : 1
                    },
                    {
                        "name": "num",
                        "type": "Integer",
                        "data" : 2
                    },
                    {
                        "name": "embedded",
                        "type": "DomainType",
                        "typeParam": "Embedded",
                        "data" : 3
                    },
                    {
                        "name": "bars",
                        "type": "List",
                        "typeParam": "Bar",
                        "data" : 4
                    },
                    {
                        "name": "bazes",
                        "type": "Map",
                        "typeParam": "Baz",
                        "data" : 5
                    }
                ]
            },
            "Embedded": {
                "properties": [
                    {
                        "name": "name",
                        "type": "PlainText",
                        "data" : 6
                    }
                ]
            },
            "Bar": {
                "properties": [
                    {
                        "name": "name",
                        "type": "PlainText",
                        "data" : 7
                    }
                ]
            },
            "Baz": {
                "properties": [
                    {
                        "name": "num",
                        "type": "Integer",
                        "data" : 8
                    }
                ]
            },
            "Joined": {
                "properties": [
                    {
                        "name": "name",
                        "type": "PlainText",
                        "data" : 9
                    }
                ]
            }
        },
        columns: {
            "name": {
                type: "Foo",
                name: "name"
            },
            "num": {
                type: "Foo",
                name: "num"
            },
            "embedded": {
                type: "Foo",
                name: "embedded"
            },
            "bars": {
                type: "Foo",
                name: "bars"
            },
            "bazes": {
                type: "Foo",
                name: "bazes"
            },
            "joinedName": {
                type: "Joined",
                name: "name"
            }
        },
        rows: [
            {
                name: "TestFoo",
                num: 123,
                embedded: {
                    name: "EmbeddedObject"
                },
                bars: [
                    {name: "Bar 1"},
                    {name: "Bar 2"}
                ],
                bazes: {
                    "one": {num: 1},
                    "two": {num: 2}
                }
            },
            {
                name: "TestFoo #2",
                num: 234,
                embedded: {
                    name: "EmbeddedObject #2"
                },
                bars: [
                    {name: "Bar 3"},
                    {name: "Bar 4"}
                ],
                bazes: {
                    "three": {num: 3},
                    "four": {num: 4}
                }
            }
        ]
    }, function (newData, path)
    {
        //console.log("CHANGED", newData, path);

        changeSpy(newData, path);
    });

    var cursor;

    it("provides data cursors", function ()
    {
        var call, newRows;

        cursor = dl.getCursor([0]);
        assert(cursor.type === "[DataListRoot]");
        assert(cursor.value.name === "TestFoo");

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

        cursor = cursor.pop();
        assert(cursor.value.name === "AnotherFoo");

        cursor = dl.getCursor([0, "bars", 0]);
        assert(cursor.value.name === "Bar 1");
        cursor = cursor.pop(2);
        assert(cursor.value.name === "AnotherFoo");


        // property cursor
        cursor = dl.getCursor([0, "name"]);
        assert(cursor.value === "AnotherFoo");

        cursor.set(null, "YetAnotherFoo");
        call = changeSpy.getCall(3);
        newRows = call.args[0];
        assert(newRows[0].name === "YetAnotherFoo");


    });


    it("provides property type information for cursor paths", function ()
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

        propertyType = cursor.getPropertyType();
        assert(propertyType.parent === "Embedded");
        assert(propertyType.type === "PlainText");
        assert(propertyType.data === 6);
        assert(propertyType.dataList === dl);

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

});
