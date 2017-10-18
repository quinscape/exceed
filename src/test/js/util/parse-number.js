import assert from "power-assert";

// noinspection JSFileReferences
import BigNumber from "bignumber.js"
import parseNumber from "../../../main/js/util/parse-number"

describe("parseNumber", function(){

    it("parses German numbers", function()
    {
        BigNumber.config({
            FORMAT: {
                // the decimal separator
                decimalSeparator: ',',
                // the grouping separator of the integer part
                groupSeparator: '.',
                // the primary grouping size of the integer part
                groupSize: 3,
                // the secondary grouping size of the integer part
                secondaryGroupSize: 0,
                // the grouping separator of the fraction part
                fractionGroupSeparator: ' ',
                // the grouping size of the fraction part
                fractionGroupSize: 0
            }
        });


        const formatted = new BigNumber("1234.56").toFormat();
        assert(formatted === "1.234,56");
        assert(parseNumber(formatted).toString() === "1234.56");

        const formatted2 = new BigNumber("123456").toFormat();
        assert(formatted2 === "123.456");
        assert(parseNumber(formatted2).toString() === "123456");

    });


    it("parses American numbers", function()
    {
        BigNumber.config({
            FORMAT: {
                // the decimal separator
                decimalSeparator: '.',
                // the grouping separator of the integer part
                groupSeparator: ',',
                // the primary grouping size of the integer part
                groupSize: 3,
                // the secondary grouping size of the integer part
                secondaryGroupSize: 0,
                // the grouping separator of the fraction part
                fractionGroupSeparator: ' ',
                // the grouping size of the fraction part
                fractionGroupSize: 0
            }
        });

        const formatted = new BigNumber("1234.56").toFormat();
        assert(formatted === "1,234.56");
        assert(parseNumber(formatted).toString() === "1234.56");

        const formatted2 = new BigNumber("123456").toFormat();
        assert(formatted2 === "123,456");
        assert(parseNumber(formatted2).toString() === "123456");
    });

    it("parses without grouping", function()
    {
        BigNumber.config({
            FORMAT: {
                // the decimal separator
                decimalSeparator: '.',
                // the primary grouping size of the integer part
                groupSize: 0
            }
        });


        const formatted = new BigNumber("1234.56").toFormat();
        assert(formatted === "1234.56");
        assert(parseNumber(formatted).toString() === "1234.56");

        const formatted2 = new BigNumber("123456").toFormat();
        assert(formatted2 === "123456");
        assert(parseNumber(formatted2).toString() === "123456");

    });
});
