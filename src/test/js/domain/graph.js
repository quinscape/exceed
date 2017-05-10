import assert from "power-assert";
import sinon from "sinon";
import proxyquire from "proxyquire";

import * as Graph from "../../../main/js/domain/graph"
import DataCursor from "../../../main/js/domain/cursor"

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
                "name": "flag",
                "type": "Boolean",
                "data" : 5
            },
            {
                "name": "bazes",
                "type": "Map",
                "typeParam": "Baz",
                "data" : 6
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

const graph = Graph.DataGraph(require("./test-lists/data-list.json"));

describe("DataGraph", function ()
{
    let cursor;


    it("supports simple maps", function ()
    {
        const simpleMap = Graph.DataGraph(require("./test-lists/simple-map.json"));

        const cursor = new DataCursor(TYPES, simpleMap, ['A']);
        assert(cursor.isProperty());
        assert(cursor.type === "PlainText");
        assert(cursor.get() === "TestFoo #3");

        cursor.set("TestFoo #3.2");
        assert(cursor.get() === "TestFoo #3.2");
    });

    it("supports complex maps", function ()
    {
        const complexMap = Graph.DataGraph(require("./test-lists/complex-map.json"));

        const cursor = new DataCursor(TYPES, complexMap, ['A']);
        assert(!cursor.isProperty());
        assert(cursor.type === "DomainType");
        assert(cursor.typeParam === "Foo");
        assert(cursor.getPropertyType(complexMap).type  === "DomainType");
        assert(cursor.get() === complexMap.rootObject.A);
        assert(cursor.get(["name"]) === "TestFoo #3");
        assert(cursor.get(["num"]) === 456);

        const cursor2 = new DataCursor(TYPES, complexMap,['A', 'name']);
        assert(cursor2.isProperty());
        assert(cursor2.type === "PlainText");
        const pt = cursor2.getPropertyType();
        assert(pt.parent === "Foo");
        assert(pt.type === "PlainText");

        assert.deepEqual(cursor.getDomainObject(), complexMap.rootObject.A);

        cursor.set("TestFoo #3.1", ["name"]);
        assert(cursor.get(["name"]) === "TestFoo #3.1");
    });

    it("supports complex object", function ()
    {
        const complex = Graph.DataGraph(require("./test-lists/complex.json"));

        const cursor = new DataCursor(COMPLEX_MAP_TYPES, complex, ["foo"]);
        assert( !cursor.isProperty());

        //cursor = cursor.get( ["foo"] );
        //
        //assert(!cursor.isProperty());

        assert(cursor.get() === complex.rootObject.foo);
        const pt = cursor.getPropertyType();
        assert(pt.type === "DomainType");
        assert(pt.typeParam === "Foo");
        assert(cursor.get(["name"]) === "TestFoo #4");
        assert(cursor.get(["num"]) === 333);

        assert.deepEqual(cursor.getDomainObject(), complex.rootObject.foo);

        var cursor3 = new DataCursor(COMPLEX_MAP_TYPES, complex, ["num"]);
        assert(cursor3.type === "Integer");
        assert(cursor3.get() === 111);

        const newComplex = cursor.set("TestFoo #3.3", ["name"]);
        assert(cursor.get(["name"]) === "TestFoo #3.3");

        const cursor4 =  new DataCursor(COMPLEX_MAP_TYPES, complex, []);

        assert(cursor4.get(null) === complex.rootObject);
        assert(cursor4.get(["foo"]) === complex.rootObject.foo);
        cursor4.updateGraph(newComplex);
        assert(cursor4.get(["foo", "name"]) === "TestFoo #3.3");

        const newComplex2 = cursor4.set("TestFoo #3.4", ["foo", "name"]);
        assert(newComplex2.rootObject.foo.name === "TestFoo #3.4");
    });

    it("supports complex object maps", function ()
    {
        const complex = Graph.DataGraph(require("./test-lists/complex2.json"));
        const cursor =  new DataCursor(COMPLEX_MAP_TYPES, complex, ["foos"]);

        const cursor2 =  cursor.getCursor(["One"]);

        assert(cursor2.get().name === "FOO1");

        assert.deepEqual(cursor2.getDomainObject(), complex.rootObject.foos.One);

    });

    it("validates wildcard columns", function ()
    {
        assert(Graph.validateWildcard({'*': true}));
        assert(!Graph.validateWildcard({'a': false}));
        assert(!Graph.validateWildcard({'a': false, "b" : false}));

        assert.throws(function ()
        {
            Graph.validateWildcard({"*": false, "b" : false})
        });
        assert.throws(function ()
        {
            Graph.validateWildcard({"a": false, "*" : false})
        });
        assert.throws(function ()
        {
            Graph.validateWildcard({})
        });
    });

    describe("Cursor", function ()
    {
        it("can be created", function ()
        {

            const cursor = new DataCursor(TYPES, graph, [0]);

            assert(cursor.type === Graph.ROOT_NAME);
            assert(cursor.get().name === "TestFoo");
            assert(cursor.get(['name']) === "TestFoo");
            assert(!cursor.isProperty());

            const newGraph = cursor.set( "AnotherFoo", ["name"]);

            assert(graph !== newGraph);
            assert(graph.rootObject !== newGraph.rootObject);
            assert(graph.columns === newGraph.columns);
            assert(graph.count === newGraph.count);
            assert(cursor.get().name === "AnotherFoo");

            assert(cursor.get(["embedded", "name"]) === "EmbeddedObject");

            const newGraph2 = cursor.set("Changed Embedded", ["embedded", "name"]);
            assert(cursor.get(["embedded", "name"]) === "Changed Embedded");

            assert(cursor.get(["bazes", "one", "num"]) === 1);
            assert(cursor.get(["bazes", "two", "num"]) === 2);

            const newGraph3 = cursor.merge({
                "three" : { num: 3.1},
                "five"  : { num: 5}
            }, ["bazes"]);

            assert(cursor.get(["bazes", "one", "num"], graph) === 1);
            assert(cursor.get(["bazes", "two", "num"], graph) === 2);
            assert(cursor.get(["bazes", "one", "num"], newGraph3) === 1);
            assert(cursor.get(["bazes", "two", "num"], newGraph3) === 2);
            assert(cursor.get(["bazes", "three", "num"], newGraph3) === 3.1);
            assert(cursor.get(["bazes", "five", "num"], newGraph3) === 5);


            const cursor2 = cursor.getCursor(["embedded"]);
            assert(cursor2.type === "Embedded");
            assert(cursor2.get().name === "Changed Embedded");
            assert(!cursor2.isProperty());

            const cursor3 = cursor2.pop();
            assert(cursor3.get(null).name === "AnotherFoo");

            const cursor4 = new DataCursor(TYPES, graph, [0, "bars", 0]);

            assert(cursor4.get().name === "Bar 1");
            assert(!cursor4.isProperty());

            // property cursor
            const cursor5 = new DataCursor(TYPES, graph, [0, "name"]);

            //console.log("PROPERTY CURSOR", cursor);

            assert(cursor5.get() === "TestFoo");
            assert(cursor5.isProperty());

            cursor5.set("YetAnotherFoo");
            assert(cursor5.get() === "YetAnotherFoo");

            const cursor6 = new DataCursor(TYPES, graph, []);

            assert(cursor6.get() === graph.rootObject);

            const cursor7 = cursor6.getCursor([0, 'name']);

            assert(cursor7.get() === "TestFoo")


        });

        it("is validated", function ()
        {
            // checks if the used paths exist in the type declarations

            assert.throws(function ()
            {
                new DataCursor(TYPES, graph, ["nonNumeric"]);
            }, /Error: First key path entry must be a numeric row index/);

            assert.throws(function ()
            {
                new DataCursor(TYPES, graph, [0, "wrong"]);
            }, /No column 'wrong' in DataGraph columns/);

            assert.throws(function ()
            {
                new DataCursor(TYPES, graph, [0, "embedded", "wrong"]);
            }, /Cannot find property for 'Embedded.wrong'/);

            assert.throws(function ()
            {
                new DataCursor(TYPES, graph, [0, "bars", 0, "wrong"]);
            }, /Cannot find property for 'Bar.wrong'/);

            assert.throws(function ()
            {
                new DataCursor(TYPES, graph, [0, "bazes", "one", "wrong"]);
            }, /Cannot find property for 'Baz.wrong'/);

        });


        it("provides property type information", function ()
        {
            cursor = new DataCursor(TYPES, graph, [0]);
            assert(cursor.type === Graph.ROOT_NAME);

            const propertyType = cursor.getPropertyType(["name"]);
            assert(propertyType.parent === "Foo");
            assert(propertyType.name === "name");
            assert(propertyType.type === "PlainText");
            assert(propertyType.data === 1);

            const propertyType2 = cursor.getPropertyType(["num"]);
            assert(propertyType2.parent === "Foo");
            assert(propertyType2.name === "num");
            assert(propertyType2.type === "Integer");
            assert(propertyType2.data === 2);

            const propertyType3 = cursor.getPropertyType(["embedded", "name"]);
            assert(propertyType3.parent === "Embedded");
            assert(propertyType3.name === "name");
            assert(propertyType3.type === "PlainText");
            assert(propertyType3.data === 6);

            const propertyType4 = cursor.getPropertyType(["bars"]);
            assert(propertyType4.parent === "Foo");
            assert(propertyType4.name === "bars");
            assert(propertyType4.type === "List");
            assert(propertyType4.typeParam === "Bar");
            assert(propertyType4.data === 4);

            const propertyType5 = cursor.getPropertyType(["bars", 0, "name"]);
            assert(propertyType5.parent === "Bar");
            assert(propertyType5.name === "name");
            assert(propertyType5.type === "PlainText");
            assert(propertyType5.data === 7);

            const propertyType6 = cursor.getPropertyType(["bazes"]);
            assert(propertyType6.parent === "Foo");
            assert(propertyType6.name === "bazes");
            assert(propertyType6.type === "Map");
            assert(propertyType6.typeParam === "Baz");
            assert(propertyType6.data === 5);

            const propertyType7 = cursor.getPropertyType(["bazes", "one", "num"]);
            assert(propertyType7.parent === "Baz");
            assert(propertyType7.name === "num");
            assert(propertyType7.type === "Integer");
            assert(propertyType7.data === 8);

            const propertyType8 = cursor.getPropertyType(["joinedName"]);
            assert(propertyType8.parent === "Joined");
            assert(propertyType8.name === "name");
            assert(propertyType8.type === "PlainText");
            assert(propertyType8.data === 9);

            cursor = new DataCursor(TYPES, graph, [0, "embedded", "name"]);

            assert(cursor.isProperty());

            const propertyType9 = cursor.getPropertyType();
            assert(propertyType9.parent === "Embedded");
            assert(propertyType9.type === "PlainText");
            assert(propertyType9.data === 6);

        });

        it("extracts implicitly typed domain objects", function ()
        {

            const simpleList = Graph.DataGraph(require("./test-lists/simple-list.json"));

            const cursor = new DataCursor(SIMPLE_LIST_TYPES, simpleList, [1]);
            const  obj = cursor.getDomainObject();

            assert(obj.name === "TestFoo #2");
            assert(obj.num === 234);
            assert(obj._type === "Foo");
        });

        it("extracts typed domain objects", function ()
        {
            const joinedList = Graph.DataGraph(require("./test-lists/joined-list.json"));

            const obj = new DataCursor(JOINED_LIST_TYPES, joinedList, [0]).getDomainObject("Foo");

            assert(obj.name === "TestFoo");
            assert(obj.num === 123);
            assert(obj._type === "Foo");

            const obj2 = new DataCursor(JOINED_LIST_TYPES, joinedList, [0]).getDomainObject("Baz");

            assert(obj2.name === undefined);
            assert(obj2.num === 1);
            assert(obj2._type === "Baz");


            assert.throws(function ()
            {
                new DataCursor(JOINED_LIST_TYPES, joinedList, [0]).getDomainObject();
            }, /Implicit type detection failed/);
        });



        it("extracts nested domain objects", function ()
        {

            const nestedList = Graph.DataGraph(require("./test-lists/nested.json"));

            const cursor = new DataCursor(NESTED_TYPES, nestedList, [0, "embedded"]);
            const obj = cursor.getDomainObject();

            assert(obj._type === "Embedded");
            assert(obj.name === "EmbeddedObject");

            const obj2 = new DataCursor(NESTED_TYPES, nestedList, [0, "bars", 1]).getDomainObject();
            assert(obj2._type === "Bar");
            assert(obj2.name === "Bar 2");

            const obj3 = new DataCursor(NESTED_TYPES, nestedList, [0, "bazes", "one"]).getDomainObject();
            assert(obj3._type === "Baz");
            assert(obj3.num === 1);

            assert.throws(function ()
            {
                new DataCursor(NESTED_TYPES, nestedList, [0, "bars"]).getDomainObject();
            }, /Cannot extract single domain object from List/);

            assert.throws(function ()
            {
                new DataCursor(NESTED_TYPES, nestedList, [0, "bazes"]).getDomainObject();

            }, /Cannot extract single domain object from Map/)

        });


        describe("updates immutably", function ()
        {

            it("push", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["numbers"]);
                cursor.push([4, 5]);
                assert.deepEqual(cursor.get(), [1, 2, 3, 4, 5]);


                const cursor2 = new DataCursor(TYPES, complex, ["numbers"]);
                cursor2.push(4);
                assert.deepEqual(cursor2.get(), [1, 2, 3, 4]);

            });

            it("unshift", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["numbers"]);
                cursor.unshift(["foo", "bar"]);
                // XXX: this follows "react-addons-update", not the Ecmascript unshift. The resulting order is reversed
                assert.deepEqual(cursor.get(), ["bar", "foo", 1, 2, 3]);

                const cursor2 = new DataCursor(TYPES, complex, ["numbers"]);
                cursor2.unshift("foo");
                assert.deepEqual(cursor2.get(), ["foo", 1, 2, 3]);
            });

            it("splice", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["numbers"]);
                cursor.splice([[0, 1, "foo"], [2, 1, "bar"]]);
                assert.deepEqual(cursor.get(), ["foo", 2, "bar"]);
            });

            it("set", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["numbers"]);
                cursor.set([4]);
                assert.deepEqual(cursor.get(), [4]);
            });

            it("merge", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["foos", "One"]);
                cursor.merge({
                    name: "Foo X",
                    flag: true
                });
                assert.deepEqual(cursor.get(), {
                    "_type": "Foo",
                    "name": "Foo X",
                    "num": 111,
                    "flag": true
                });

            });

            it("apply", function ()
            {
                const complex = Graph.DataGraph(require("./test-lists/complex2.json"));

                const cursor = new DataCursor(TYPES, complex, ["numbers"]);
                cursor.apply((array) =>
                {
                    return [array.length].concat(array);
                });

                assert.deepEqual(cursor.get(), [3, 1, 2, 3]);
            });
        });
    });
});
