import assert from "power-assert";
import clone from "clone";
import { matchLocationRule } from "../../../main/js/util/model";

describe("ModelUtils", function ()
{
    describe("matchLocationRule", function ()
    {
        it("matches model paths", function ()
        {
            assert(matchLocationRule({"type":"test.Test","prefix":"/models/view/","infix":null}, "/models/view/test.json") === "test.Test");
            assert(matchLocationRule({"type":"test.Test","prefix":"/models/view/","infix":null}, "/models/app.json") === false);
            assert(matchLocationRule({"type":"test.Test","prefix":"/models/app.json","infix":null}, "/models/app.json") === "test.Test");
            assert(matchLocationRule({"type":"test.Test","prefix":"/models/process/","infix":"/view"}, "/models/view/test.json") === false);
            assert(matchLocationRule({"type":"test.Test","prefix":"/models/process/","infix":"/view"}, "/models/process/test/view/list.json") === "test.Test");
        });
    });
});
