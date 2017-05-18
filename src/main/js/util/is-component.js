/**
 * Returns true if the given value is a React component
 *
 * @param value         potential component
 * @returns {boolean}
 */
export default function(value)
{
    return typeof value == "function" && typeof value.prototype.render == "function";
}
