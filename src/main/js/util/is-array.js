/**
 * Returns true if the given value is an array like object
 *
 * @param value         potential array
 * @returns {boolean} true if array
 */
export default function(value)
{
    return value && typeof value === "object" && typeof value.length === "number";
}
