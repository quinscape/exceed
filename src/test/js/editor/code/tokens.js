var brace = require("brace");
var assert = require("power-assert");

require("../../../../../src/main/js/editor/code/ace-mode-exceed");

var EditSession = brace.acequire("ace/edit_session").EditSession;
var ExceedViewMode = brace.acequire("ace/mode/exceed_view").Mode;

var tokens = require("../../../../../src/main/js/editor/code/tokens");
var xmlUtil = require("../../../../../src/main/js/editor/code/xml-util");

var testView = require("../../../../../src/test/js/editor/code/TestView.json");
var posInDocument = require("../../../../../src/main/js/util/posInDocument");


describe("Tokens Module", function(){

    var xmlDoc = xmlUtil.toXml(testView);

    var testSession = new EditSession(xmlDoc, "ace/mode/exceed_view");


    var locate = function (sub, skip)
    {
        var pos = posInDocument(xmlDoc, sub, skip);
        //console.log("pos", pos);
        return tokens.currentLocation(testSession, pos.row, pos.col, testView);
    };

    it("locates component models based on cursor location", function()
    {
        //console.log("XML", xmlDoc);

        var result = locate("<Grid");
        assert(result.models[0].model === testView.root);
        assert(!result.attr);
        assert(!result.attrValue);

        result = locate("luid");
        assert(result.models[0].model === testView.root);
        assert(result.attr === "fluid");
        assert(!result.attrValue);

        result = locate("true");
        assert(result.models[0].model === testView.root);
        assert(result.attr === "fluid");
        assert(result.attrValue);
        assert(result.expression);

        result = locate("login");
        assert(result.models[0].model === testView.root.kids[0].kids[0].kids[1].kids[1]);
        assert(result.attr === "name");
        assert(result.attrValue);
        assert(!result.expression);
    });

    it("locates unclosed tags", function()
    {

        var testSession = new EditSession("<Root>\n    <\ņ</Root>\n", "ace/mode/exceed_view");
        testView = { root: { name: "Root"} };

        var result = locate("<", 1);
        assert(result.models[0].model === testView.root);
        assert(!result.attr);
        assert(!result.attrValue);
    });

    it("generates view models from the edit session", function ()
    {

        var testSession = new EditSession("<Root>\n    <Child/>\n    <Child attr=\"foo\" expr=\"{ 123 }\">\n        <GrandKid/>\n    </Child>\n</Root>", "ace/mode/exceed_view");

        var model = tokens.toModel(testSession);

        assert(model.root.name === 'Root');
        var kids = model.root.kids;
        assert(kids.length === 2);

        assert(kids[0].name === 'Child');
        assert(kids[0].attrs === undefined);
        assert(kids[0].kids === undefined)
        assert(kids[1].name === 'Child');
        assert(kids[1].attrs.attr === "foo");
        assert(kids[1].attrs.expr === "{ 123 }");
        assert(kids[1].kids[0].name === "GrandKid");

    });
});
