var assert = require("power-assert");
var sinon = require("sinon");

var changeSpy = sinon.spy();

var util = require("../../../../src/main/js/util/data-graph-util");
var DataGraph = require("../../../../src/main/js/util/data-graph");

const TYPES = {
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
};

const SIMPLE_LIST_TYPES = {
    "Foo": {
        "name" : "Foo",
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
            }
        ]
    }
};

const COMPLEX_MAP_TYPES = {
    "Foo": {
        "name" : "Foo",
        "properties": [
            {
                "name": "name",
                "type": "PlainText",
                "domainType" : "Foo",
                "data" : 1
            },
            {
                "name": "num",
                "type": "Integer",
                "domainType" : "Foo",
                "data" : 2
            }
        ]
    }
};

const JOINED_LIST_TYPES = {
    "Foo": {
        "name" : "Foo",
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
            }
        ]
    },
    "Baz": {
        "name" : "Baz",
        "properties": [
            {
                "name": "num",
                "type": "Integer",
                "data" : 8
            }
        ]
    }
};


const NESTED_TYPES = {
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
};

var dl = new DataGraph(TYPES, require("./test-lists/data-list.json"), function (newGraph, path)
{
    //console.log("CHANGED", root, path);

    dl = newGraph;

    changeSpy(newGraph, path);

});

describe("DataGraph", function ()
{
    var cursor;

    it("provides data cursors", function ()
    {
        var call, newRootObject;

        cursor = dl.getCursor([0]);
        assert(cursor.type === util.ROOT_NAME);
        assert(cursor.get().name === "TestFoo");
        assert(cursor.get(['name']) === "TestFoo");
        assert(!cursor.isProperty());

        cursor.set(["name"], "AnotherFoo");
        call = changeSpy.getCall(0);
        newRootObject = call.args[0].rootObject;
        assert(newRootObject[0].name === "AnotherFoo");
        assert.deepEqual(call.args[1], [0, "name"]);

        assert(cursor.get(["embedded", "name"]) === "EmbeddedObject");

        cursor.set(["embedded", "name"], "Changed Embedded");
        call = changeSpy.getCall(1);
        newRootObject = call.args[0].rootObject;
        assert(newRootObject[0].embedded.name === "Changed Embedded");
        assert.deepEqual(call.args[1], [0, "embedded","name"]);

        cursor.merge(["bazes"], {
            "three" : { num: 3.1},
            "five"  : { num: 5}
        });

        call = changeSpy.getCall(2);
        newRootObject = call.args[0].rootObject;
        assert(newRootObject[0].bazes.three.num === 3.1);
        assert(newRootObject[0].bazes.five.num === 5);
        assert.deepEqual(call.args[1], [0, "bazes"]);


        cursor = cursor.getCursor(["embedded"]);
        assert(cursor.type === "Embedded");
        assert(cursor.get().name === "Changed Embedded");
        assert(!cursor.isProperty());

        cursor = cursor.pop();
        assert(cursor.get().name === "AnotherFoo");

        cursor = dl.getCursor([0, "bars", 0]);
        assert(cursor.get().name === "Bar 1");
        assert(!cursor.isProperty());
        cursor = cursor.pop(2);
        assert(cursor.get().name === "AnotherFoo");


        // property cursor
        cursor = dl.getCursor([0, "name"]);

        //console.log("PROPERTY CURSOR", cursor);

        assert(cursor.get() === "AnotherFoo");
        assert(cursor.isProperty());

        cursor.set(null, "YetAnotherFoo");
        call = changeSpy.getCall(3);
        newRootObject = call.args[0].rootObject;
        assert(newRootObject[0].name === "YetAnotherFoo");

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
        }, /No column 'fake' in DataGraph columns/);

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

    it("can be copied", function ()
    {
        var simpleList = new DataGraph(NESTED_TYPES, require("./test-lists/nested.json"), null);

        var newChange = function () {};

        var copy = new DataGraph(NESTED_TYPES, simpleList);
        var copy2 = new DataGraph(null, simpleList, newChange);

        assert(copy.types === NESTED_TYPES);
        assert(copy2.types === NESTED_TYPES);

        // change callback can be either kept as-is, or changed
        assert(copy.onChange === simpleList.onChange);
        assert(copy2.onChange === newChange);
    });

    it("supports simple maps", function ()
    {
        var spy = sinon.spy();
        var raw = require("./test-lists/simple-map.json");
        var simpleMap = new DataGraph({}, raw, (newGraph,path) => {
            simpleMap = newGraph;
            spy(newGraph,path);
        });


        var cursor = simpleMap.getCursor(['A']);
        assert(cursor.isProperty());
        assert(cursor.type === "PlainText");
        assert(cursor.get() === "TestFoo #3");

        cursor.set(null, "TestFoo #3.2");

        var call = spy.getCall(0);
        assert(cursor.get() === "TestFoo #3.2");
        assert(call.args[0].rootObject.A == "TestFoo #3.2");
        assert.deepEqual(call.args[1], ["A"]);
    });

    it("supports complex maps", function ()
    {
        var spy = sinon.spy();
        var raw = require("./test-lists/complex-map.json");
        var complexMap = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph,path) => {
            complexMap = newGraph;
            spy(newGraph,path);
        });


        var cursor = complexMap.getCursor(['A']);
        assert(!cursor.isProperty());
        assert(cursor.type === "DomainType");
        assert(cursor.typeParam === "Foo");
        assert(cursor.getPropertyType().type  === "DomainType");
        assert(cursor.get() === raw.rootObject.A);
        assert(cursor.get(["name"]) === "TestFoo #3");
        assert(cursor.get(["num"]) === 456);

        var cursor2 = complexMap.getCursor(['A', 'name']);
        assert(cursor2.isProperty());
        assert(cursor2.type === "PlainText");
        var pt = cursor2.getPropertyType();
        assert(pt.parent === "Foo");
        assert(pt.type === "PlainText");

        assert.deepEqual(cursor.getDomainObject(), raw.rootObject.A);

        cursor.set(["name"], "TestFoo #3.1");
        var call = spy.getCall(0);
        assert(cursor.get(["name"]) === "TestFoo #3.1");
        assert(call.args[0].rootObject.A.name == "TestFoo #3.1");
        assert.deepEqual(call.args[1], ["A", "name"]);

    });

    it("supports complex object", function ()
    {
        var spy = sinon.spy();
        var raw = require("./test-lists/complex.json");
        var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph,path) => {
            complex = newGraph;
            spy(newGraph,path);
        });

        var cursor = complex.getCursor( ["foo"] );
        assert( !cursor.isProperty());

        //cursor = cursor.get( ["foo"] );
        //
        //assert(!cursor.isProperty());

        assert(cursor.get() === raw.rootObject.foo);
        var pt = cursor.getPropertyType();
        assert(pt.type === "DomainType");
        assert(pt.typeParam === "Foo");
        assert(cursor.get(["name"]) === "TestFoo #4");
        assert(cursor.get(["num"]) === 333);

        assert.deepEqual(cursor.getDomainObject(), raw.rootObject.foo);

        var cursor3 = complex.getCursor(["num"]);
        assert(cursor3.type === "Integer");
        assert(cursor3.get() === 111);

        cursor.set(["name"], "TestFoo #3.3");
        var call = spy.getCall(0);
        assert(cursor.get(["name"]) === "TestFoo #3.3");
        assert(call.args[0].rootObject.foo.name == "TestFoo #3.3");
        assert.deepEqual(call.args[1], ["foo", "name"]);

        var cursor4 = complex.getCursor([]);

        assert(cursor4.get() === complex.rootObject);
        assert(cursor4.get(["foo"]) === complex.rootObject.foo);
        assert(cursor4.get(["foo", "name"]) === "TestFoo #3.3");

        cursor4.set(["foo", "name"], "TestFoo #3.4");

        assert(complex.rootObject.foo.name === "TestFoo #3.4");
    });

    it("supports complex object maps", function ()
    {
        var spy = sinon.spy();
        var raw = require("./test-lists/complex2.json");
        var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph,path) => {
            complex = newGraph;
            spy(newGraph,path);
        });

        var cursor = complex.getCursor(["foos"]);

        cursor = cursor.getCursor(["One"]);

        assert(cursor.get().name === "FOO1");

        assert.deepEqual(cursor.getDomainObject(), complex.rootObject.foos.One);

    });

    describe("Cursor", function ()
    {
        it("provides property type information", function ()
        {
            cursor = dl.getCursor([0]);
            assert(cursor.type === util.ROOT_NAME);

            var propertyType = cursor.getPropertyType(["name"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 1);

            propertyType = cursor.getPropertyType(["num"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "num");
            assert(propertyType.type === "Integer");
            assert(propertyType.data === 2);

            propertyType = cursor.getPropertyType(["embedded", "name"]);
            assert(propertyType.parent === "Embedded");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 6);

            propertyType = cursor.getPropertyType(["bars"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "bars");
            assert(propertyType.type === "List");
            assert(propertyType.typeParam === "Bar");
            assert(propertyType.data === 4);

            propertyType = cursor.getPropertyType(["bars", 0, "name"]);
            assert(propertyType.parent === "Bar");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 7);

            propertyType = cursor.getPropertyType(["bazes"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "bazes");
            assert(propertyType.type === "Map");
            assert(propertyType.typeParam === "Baz");
            assert(propertyType.data === 5);

            propertyType = cursor.getPropertyType(["bazes", "one", "num"]);
            assert(propertyType.parent === "Baz");
            assert(propertyType.name === "num");
            assert(propertyType.type === "Integer");
            assert(propertyType.data === 8);

            propertyType = cursor.getPropertyType(["joinedName"]);
            assert(propertyType.parent === "Joined");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 9);

            cursor = dl.getCursor([0, "embedded", "name"]);

            assert(cursor.isProperty());

            propertyType = cursor.getPropertyType();
            assert(propertyType.parent === "Embedded");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 6);

        });

        it("extracts implicitly typed domain objects", function ()
        {

            var simpleList = new DataGraph(SIMPLE_LIST_TYPES, require("./test-lists/simple-list.json"), null);

            var obj = simpleList.getCursor([1]).getDomainObject();

            assert(obj.name === "TestFoo #2");
            assert(obj.num === 234);
            assert(obj._type === "Foo");
        });

        it("extracts typed domain objects", function ()
        {
            var joinedList = new DataGraph(JOINED_LIST_TYPES, require("./test-lists/joined-list.json"), null);

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

            var nestedList = new DataGraph(NESTED_TYPES, require("./test-lists/nested.json"), null);

            var obj = nestedList.getCursor([0, "embedded"]).getDomainObject();

            assert(obj._type === "Embedded");
            assert(obj.name === "EmbeddedObject");

            obj = nestedList.getCursor([0, "bars", 1]).getDomainObject();
            assert(obj._type === "Bar");
            assert(obj.name === "Bar 2");

            obj = nestedList.getCursor([0, "bazes", "one"]).getDomainObject();
            assert(obj._type === "Baz");
            assert(obj.num === 1);

            assert.throws(function ()
            {
                nestedList.getCursor([0, "bars"]).getDomainObject();
            }, /Cannot extract single domain object from List/);

            assert.throws(function ()
            {
                nestedList.getCursor([0, "bazes"]).getDomainObject();

            }, /Cannot extract single domain object from Map/)

        });


        describe("updates immutably", function ()
        {

            it("push", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["numbers"], false);
                cursor.push(null, [4, 5]);
                assert.deepEqual(cursor.get(), [1, 2, 3, 4, 5]);

            });

            it("unshift", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["numbers"], false);
                cursor.unshift(null, ["foo", "bar"]);

                // XXX: this follows "react-addons-update", not the Ecmascript unshift. The resulting order is reversed
                assert.deepEqual(cursor.get(), ["bar", "foo", 1, 2, 3]);

            });

            it("splice", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["numbers"], false);
                cursor.splice(null, [[0, 1, "foo"], [2, 1, "bar"]]);
                assert.deepEqual(cursor.get(), ["foo", 2, "bar"]);

            });

            it("set", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["numbers"], false);
                cursor.set(null, [4]);
                assert.deepEqual(cursor.get(), [4]);
            });

            it("merge", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["foos", "One"], false);
                cursor.merge(null, {
                    name: "Foo X",
                    num: 333
                });
                assert.deepEqual(cursor.get(), {
                    "_type": "Foo",
                    "name": "Foo X",
                    "num": 333
                });

            });

            it("apply", function ()
            {
                var raw = require("./test-lists/complex2.json");

                var complex = new DataGraph(COMPLEX_MAP_TYPES, raw, (newGraph, path) =>
                {
                    // graph never changes
                });

                var cursor = complex.getCursor(["numbers"], false);
                cursor.apply(null, (array) =>
                {

                    return [array.length].concat(array);
                });
                assert.deepEqual(cursor.get(), [3, 1, 2, 3]);

            });
        });
    });


    describe('Cursor change handlers', function ()
    {
        it("can force local changes", function ()
        {
            var spy = sinon.spy();
            var raw = require("./test-lists/simple-map.json");
            var simpleMap = new DataGraph({}, raw, (newGraph, path) =>
            {
                simpleMap = newGraph;

                //console.log("LOCAL CHANGE", path, newGraph.rootObject.A);
                spy(newGraph, path);
            });

            // giving the local change handler as null forces skipping of both the local onChange and the list onChange, just
            // returning the newGraph after modification

            var c2 = simpleMap.getCursor(["B"], false);
            var newGraph = c2.set(null, "local change");

            assert(spy.notCalled);
            assert(c2.get() === "local change");
            assert(simpleMap.rootObject.B === "TestFoo #4");
            assert(newGraph.rootObject.B === "local change");
        });

        it("can prevent changes", function ()
        {
            var spy = sinon.spy();
            var raw = require("./test-lists/simple-map.json");
            var simpleMap = new DataGraph({}, raw, (newGraph, path) =>
            {
                simpleMap = newGraph;

                //console.log("LOCAL CHANGE", path, newGraph.rootObject.A);
                spy(newGraph, path);
            });

            // local change handler can prevent the change by return the value false
            var cursor = simpleMap.getCursor(["A"], function (newGraph, path)
            {

                assert(this === cursor);

                return util.walk(newGraph.rootObject, path) !== "Ignored";
            });
            cursor.set(null, "Ignored");
            assert(cursor.get() === "Ignored");
            assert(simpleMap.getCursor(['A']).get() === "TestFoo #3");

            cursor.set(null, "Not Ignored");
            assert(cursor.get() === "Not Ignored");
            assert(simpleMap.getCursor(['A']).get() === "Not Ignored");
            assert(spy.calledOnce);

        });

        it("can modify changes", function ()
        {
            var spy = sinon.spy();
            var raw = require("./test-lists/simple-map.json");
            var simpleMap = new DataGraph({}, raw, (newGraph, path) =>
            {
                simpleMap = newGraph;

                //console.log("LOCAL CHANGE", path, newGraph.rootObject.A);
                spy(newGraph, path);
            });

            // returning a DataGraph from the local change handler replaces that DataGraph list with the initial one which is
            // then propagated in one update.

            var cursor = simpleMap.getCursor(["A"], (newGraph, path) =>
            {
                var value = util.walk(newGraph.rootObject, path);

                return newGraph.getCursor(['A'], false).set(null, "(" + value + ")")
            });

            cursor.set(null, "aaa");
            assert(cursor.get() === "(aaa)");

        });
    })
});
