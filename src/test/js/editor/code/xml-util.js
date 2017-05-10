var assert = require("power-assert");
var sinon = require("sinon");

var xmlUtil = require("../../../../../src/main/js/editor/code/xml-util");

describe("XML Util", function ()
{

    it("reindents text", function ()
    {
        // can indent 2 spaces
        assert(xmlUtil.reindent("a\nb\n", "  ") === "  a\n  b\n");
        // can indent 4 spaces
        assert(xmlUtil.reindent("a\nb\n", "    ") === "    a\n    b\n");
        // can indent 4 spaces on initial 2
        assert(xmlUtil.reindent("  a\n  b\n", "    ") === "    a\n    b\n");
    });

    it.skip("converts JSON view models to XML notation", function ()
    {
        var xmlDoc = xmlUtil.viewToXml({
            type: "xcd.view.View",
            root: {
                name: "Tag",
                attrs: {
                    value: "foo"
                },
                kids: [
                    {
                        name: "[String]",
                        attrs: {
                            value: "Hello World"
                        }
                    }
                ]
            }
        });

        assert(xmlDoc === "<Tag value=\"foo\">\r\n    Hello World\r\n</Tag>");

        xmlDoc = xmlUtil.viewToXml({
            type: "xcd.view.View",
            root: {
                name: "Tag",
                attrs: {
                    value: "foo"
                },
                kids: [
                    {
                        name: "Child",
                        attrs: {
                            value: "{ context }"
                        }
                    }
                ]
            }
        });

        assert(xmlDoc === "<Tag value=\"foo\">\r\n    <Child value=\"{ context }\"/>\r\n</Tag>");

    });
});
