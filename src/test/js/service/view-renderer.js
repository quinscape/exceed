var testDom = require("../ui/test-dom").setup();
var React = require("react");
var expect = require("expect");
var createRenderer = require("react-addons-test-utils").createRenderer;
var renderIntoDocument = require("react-addons-test-utils").renderIntoDocument;
var expectJSX = require("expect-jsx");

var assert = require("power-assert");
var Promise = require("es6-promise-polyfill").Promise;
var sinon = require("sinon");
var proxyquire = require("proxyquire");

expect.extend(expectJSX);


var Foo = React.createClass({
    render: function ()
    {
        return <div className="foo">{ this.props.children }</div>;
    }
});

var Bar = React.createClass({
    render: function ()
    {
        return <div className="bar">{ this.props.children }</div>;
    }
});

var Provider = React.createClass({
    render: function ()
    {
        return <div className="provider">{ this.props.renderChildrenWithContext(this.props.context) }</div>;
    }
});

var Consumer = React.createClass({
    render: function ()
    {
        return <div className="consumer">{ this.props.context + ":" + this.props.expr }</div>;
    }
});

var KeyedConsumer = React.createClass({
    render: function ()
    {
        return <div className="keyed-consumer">{ this.props.customContextProp }</div>;
    }
});

var Injected = React.createClass({
    render: function ()
    {
        return <div className="keyed-consumer">{ this.props.context }</div>;
    }
});


var ViewComponentRenderer = proxyquire("../../../../src/main/js/service/view-renderer", {
    "./component" : {
        getComponents: function()
        {
            return {
                Foo: {
                    component: Foo,
                    modelAware: true
                },
                Bar: {
                    component: Bar
                },
                Provider: {
                    component: Provider,
                    contextProvider: true
                },
                Consumer: {
                    component: Consumer,
                    "context" : {
                        "context": "context[props.name]"
                    }
                },
                Injected: {
                    component: Injected,
                    queries: {
                        value : {}
                    }
                },
                KeyedConsumer: {
                    component: KeyedConsumer,
                    contextProvider: true,
                    propTypes: {
                        "customContextProp": "context[props.name]"
                    }
                }
            }
        }
    }
});

var ViewComponent = require("../../../../src/main/js/service/render").ViewComponent;

// View Model client format with "exprs" and transformed expressions
var simpleView = {
    name: "SimpleTestView",
    version: "00000000-0000-0000-0000-000000000000",
    root: {
        name: "Foo",
        exprs: {
            model: "_v.root"
        },
        kids: [
            {
                name: "Bar",
                kids: [
                    {
                        name: "[String]",
                        attrs: {
                            "value": "abc123"
                        }
                    }
                ]
            }

        ]
    }
};

describe("View Components", function ()
{

    it("render a component tree", function ()
    {
        var renderer = createRenderer();
        renderer.render(<ViewComponent
            model={ simpleView }
            componentData={{}}
        />);

        expect(renderer.getRenderOutput()).toEqualJSX(
            <Foo model={ simpleView.root }>
                <Bar>
                    abc123
                </Bar>
            </Foo>
        );

    });
    it("inject vars and query data", function ()
    {
        var view = {
            name: "InjectionView",
            version: "00000000-0000-0000-0000-000000000000",
            root: {
                name: "Injected",
                attrs: {
                    "id": "i-1"
                }
            }
        };

        var payload = { value : "injected value" };

        var renderer = createRenderer();
        renderer.render(
            <ViewComponent
                model={ view }
                componentData={{
                    'i-1' : {
                        vars: { foo: 12 },
                        data: { value : payload }
                    }
                }}
            />
        );

        expect(renderer.getRenderOutput()).toEqualJSX(
                <Injected value={ payload } vars={{foo: 12}}/>
        );

    });
    it("render components with context", function (done)
    {
        var view = {
            name: "Test-Component-Context",
            version: "00000000-0000-0000-0000-000000000000",
            root: {
                name: "Provider",
                attrs: {
                    "context": "myContext"
                },
                kids: [
                    {
                        name: "Consumer",
                        attrs: {
                            "expr": "{ context }"
                        },
                        exprs: {
                            "expr" : "context"
                        }
                    }
                ]
            }
        };


        testDom.render(<ViewComponent
            model={ view }
            componentData={{}}
        />,
        function ()
        {
            //console.log("HTML", document.body.innerHTML);
            var providerElem = document.querySelector(".provider");
            assert(providerElem);
            var consumerElem = providerElem.querySelector(".consumer");
            assert(consumerElem.innerHTML === "myContext:myContext");

            testDom.cleanup();
            done();
        });
    });

    it("render components with keyed context", function (done)
    {

        var view = {
            name: "Test-Keyed-Component-Context",
            version: "00000000-0000-0000-0000-000000000000",
            root: {
                name: "Provider",
                exprs: {
                    "context": "{ foo: 'value for \\'foo\\''}"
                },
                kids: [
                    {
                        name: "KeyedConsumer",
                        attrs: {
                            "name": "foo"
                        },
                        exprs: {
                            customContextProp: "context[\"foo\"]"
                        }
                    }

                ]
            }
        };

        testDom.render(<ViewComponent
            model={ view }
            componentData={{}}
        />, function ()
        {
            //console.log("HTML", document.body.innerHTML);
            var providerElem = document.querySelector(".provider");
            assert(providerElem);


            var consumerElem = providerElem.querySelector(".keyed-consumer");
            assert(consumerElem);
            assert(consumerElem.innerHTML === "value for 'foo'");

            testDom.cleanup();
            done();
        });
    });


    after(function ()
    {
        testDom.cleanup();
    })
});
