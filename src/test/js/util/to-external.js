import assert from "power-assert";
import clone from "clone";
import toExternal from "../../../main/js/util/to-external";

describe("toExternal", function ()
{

    it("converts views to the external format", function ()
    {
        const view = {
            name: "Test",
            content: {
                root: {
                    name: "Component",
                    attrs: {
                        value: "{ fn() }"
                    },
                    exprs: {
                        value: "magicallyTransformed()"
                    },
                    kids: [
                        {
                            name: "Sub",
                            attrs: {
                                value: "123"
                            }
                        }

                    ]
                }
            }
        };

        const clonedView = clone(view);

        const ext = toExternal(view);

        assert.deepEqual(clonedView, view);

        assert(ext.content.root.name === "Component");
        assert(ext.content.root.attrs.value === "{ fn() }");
        assert(ext.content.root.exprs === undefined);
        assert(ext.content.root.kids[0].name === "Sub");
        assert(ext.content.root.kids[0].attrs.value === "123");

    });
});
