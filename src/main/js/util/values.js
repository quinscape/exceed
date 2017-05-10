if (typeof Object.values === "function")
{
    /**
     * Returns the keys of the given object
     *
     * @param o     object
     * @returns {Array} keys
     */
    module.exports = Object.values;
}
else
{
    /**
     * Returns the keys of the given object
     *
     * @param o     object
     * @returns {Array} keys
     */
    module.exports = function (obj)
    {
        if (obj !== Object(obj))
        {
            throw new TypeError('Object.values called on a non-object');
        }

        const values = [];
        for (let name in obj)
        {
            if (Object.prototype.hasOwnProperty.call(obj, name))
            {
                values.push(obj[name]);
            }
        }
        return values;
    }}

