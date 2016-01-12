var testDom = require("./test-dom");
var React = require("react");
var ReactDOM = require("react-dom");
var expect = require("expect");
var createRenderer = require("react-addons-test-utils").createRenderer;
var renderIntoDocument = require("react-addons-test-utils").renderIntoDocument;
var expectJSX = require("expect-jsx");
var sinon = require("sinon");
var proxyquire = require("proxyquire");
var assert = require("power-assert");

var security = require("../../../../src/main/js/service/security");

var InPageEditor = require("../../../../src/main/js/editor/InPageEditor");
var ValueLink = require("../../../../src/main/js/util/value-link");
expect.extend(expectJSX);

var renderer = createRenderer();

var simpleView = {
    root: {
        name: "Foo",
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



var Foo = React.createClass({
    render: function ()
    {
        return <div className="foo">{ this.props. children }</div>;
    }
});

var Bar = React.createClass({
    render: function ()
    {
        return <div className="bar">{ this.props. children }</div>;
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


//security.init("ROLE_EDITOR");

var ViewComponent = proxyquire("../../../../src/main/js/ui/ViewComponent", {
    "../service/component" : {
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
                    context: true
                },
                Injected: {
                    component: Injected
                },
                KeyedConsumer: {
                    component: KeyedConsumer,
                    contextKey: "name",
                    context: "customContextProp"
                }
            }
        }
    }
});

var activeLink = new ValueLink(false, sinon.spy());

describe("ViewComponent", function ()
{
    beforeEach(function ()
    {
        security.init("ROLE_USER");
    });

    it("renders a component tree", function ()
    {
        renderer.render(<ViewComponent
            model={ simpleView }
            componentData={{}}
            activeLink={ activeLink }
        />);

        expect(renderer.getRenderOutput()).toEqualJSX(
            <div>
                <Foo model={ simpleView.root }>
                    <Bar>
                        abc123
                    </Bar>
                </Foo>
            </div>
        );
    });

    it("renders the inline editor on ROLE_EDITOR", function ()
    {
        security.init("ROLE_EDITOR");

        renderer.render(<ViewComponent
            model={ simpleView }
            componentData={{}}
            activeLink={ activeLink }
        />);

        expect(renderer.getRenderOutput()).toEqualJSX(
            <div>
                <Foo model={ simpleView.root }>
                    <Bar>
                        abc123
                    </Bar>
                </Foo>
                <InPageEditor activeLink={ activeLink } model={ simpleView } />
            </div>
        );
    });

    it("injects vars and query data", function ()
    {
        var view = {
            root: {
                name: "Injected",
                attrs: {
                    "id": "i-1"
                }
            }
        };

        var payload = { value : "injected value" };

        renderer.render(
            <ViewComponent
                model={ view }
                componentData={{
                    'i-1' : {
                        vars: { foo: 12},
                        data: { value : payload }
                    }
                }}
                activeLink={ activeLink }
            />
        );

        expect(renderer.getRenderOutput()).toEqualJSX(
            <div>
                <Injected value={ payload } vars={{foo: 12}}/>
            </div>
        );

    });

    it("renders components with context", function (done)
    {
        var view = {
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
                        }
                    }

                ]
            }
        };

        testDom.render(<ViewComponent
            model={ view }
            componentData={{}}
            activeLink={ activeLink }
        />, function ()
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

    it("renders components with keyed context", function (done)
    {

        var view = {
            root: {
                name: "Provider",
                attrs: {
                    "context": { foo: "value for 'foo'"}
                },
                kids: [
                    {
                        name: "KeyedConsumer",
                        attrs: {
                            "name": "foo"
                        }
                    }

                ]
            }
        };

        testDom.render(<ViewComponent
            model={ view }
            componentData={{}}
            activeLink={ activeLink }
        />, function ()
        {
            //console.log("HTML", document.body.innerHTML);
            var providerElem = document.querySelector(".provider");
            assert(providerElem);
            var consumerElem = providerElem.querySelector(".keyed-consumer");
            assert(consumerElem.innerHTML === "value for 'foo'");

            testDom.cleanup();
            done();
        });
    });


    after(function ()
    {
        assert(activeLink.requestChange.callCount == 0);
    })
});
