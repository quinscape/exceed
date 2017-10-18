const testDom = require("../ui/test-dom").setup();
const React = require("react");
const expect = require("expect");
const createRenderer = require("react-test-renderer/shallow").createRenderer;
const expectJSX = require("expect-jsx");
expect.extend(expectJSX);

const assert = require("power-assert");
const Promise = require("es6-promise-polyfill").Promise;
const sinon = require("sinon");
const proxyquire = require("proxyquire");

const Annotate = require("../../../main/js/ui/Annotate").default;
const Rule = require("../../../main/js/ui/Annotate").Rule;
const countGroups = require("../../../main/js/ui/Annotate").countGroups;

describe("Annotate", function ()
{
    it("can count regexp groups", function ()
    {
        assert(countGroups("abc") === 0);
        assert(countGroups("(abc)") === 1);
        assert(countGroups("\\(abc)") === 0);
        assert(countGroups("\\(a(b)c)") === 1);
        assert(countGroups("(a(b)c)") === 2);

    });

    // XXX: currently deactivated for React 16
    it.skip("renders annotated text", function ()
    {
        const renderer = createRenderer();
        renderer.render(
            <Annotate value="123 Test abc">
                <Rule
                    regexp="Test"
                >
                    <h1>Test</h1>
                </Rule>
                <Rule
                    regexp="[0-9]+"
                >
                    {
                        value => <em>{ value }</em>
                    }
                </Rule>
            </Annotate>
        );

        expect(renderer.getRenderOutput()).toIncludeJSX(<em>123</em>);
        expect(renderer.getRenderOutput()).toIncludeJSX(<h1>Test</h1>);
        expect(renderer.getRenderOutput()).toIncludeJSX("abc");
    });

    it.skip("renders extracted text", function ()
    {
        const renderer = createRenderer();
        renderer.render(
            <Annotate value="test('foo')">
                <Rule
                    regexp="test\('(.*?)'\)"
                >
                    {
                        (complete, extracted) => <h1>{ extracted }</h1>
                    }
                </Rule>
            </Annotate>
        );

        expect(renderer.getRenderOutput()).toIncludeJSX(<h1>foo</h1>);
    });

});
