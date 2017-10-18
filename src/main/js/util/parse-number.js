// noinspection JSFileReferences
import BigNumber from "bignumber.js"

const RE = {};

function clean(value, c)
{
    if (!c)
    {
        return value;
    }

    let regex = RE[c];
    if (!regex)
    {
        regex = RE[c] = new RegExp("\\" + c, "g");
    }
    return value.replace(regex, "");
}

/**
 * Parses the given formatted number string by cleaning it up using the current BigNumber configuration before
 * creating the BigNumber instance
 *
 * @param value
 */
export default function (value)
{
    const { decimalSeparator, groupSeparator, fractionGroupSeparator } = BigNumber.config().FORMAT;

    const decimalPos = value.indexOf(decimalSeparator);
    if (decimalPos < 0)
    {
        return new BigNumber( clean(value, groupSeparator) );
    }
    else
    {
        return new BigNumber( clean(value.substring(0, decimalPos), groupSeparator) + "." + clean(value.substring(decimalPos + 1), fractionGroupSeparator) );
    }
}
