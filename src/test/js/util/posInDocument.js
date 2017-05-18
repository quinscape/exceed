var assert = require("power-assert");

import posInDocument from "../../../../src/main/js/util/posInDocument"

describe("posInDocument", function(){

    it("locates substrings", function()
    {
        assert.deepEqual(posInDocument("abc\nabc\n", "a") , {row: 0, col: 0});
        assert.deepEqual(posInDocument("abc\nabc\n", "a", 1) , {row: 1, col: 0});
        assert.deepEqual(posInDocument("abc\nabc\n", "c", 1) , {row: 1, col: 2});


    });
});
