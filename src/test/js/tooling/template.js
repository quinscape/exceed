import assert from "power-assert";
import Template from "../../../../tooling/doc/template";

describe("Doc-Template", function(){

    it ("evaluates templates", function ()
    {
        const template = new Template("[$A,$B]");

        const out = template.render({
            A: "aaa",
            B: "bbb"
        });
        assert(out === "[aaa,bbb]");

        const out2 = template.render({});
        assert(out2 === "[,]");


        const template = new Template("[$A,$B]");

        const out = template.render({
            A: "aaa",
            B: "bbb"
        });

    });
});
