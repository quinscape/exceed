function handleText(elem)
{
    if (typeof elem !== "object")
    {
        return {
            name: "[String]",
            attrs: {
                value: String(elem)
            }
        };
    }
    return elem;
}

/**
 * Helper function to convert JSX to exceed model structures.
 *
 * Needs to be imported into a variable and then declared with "@jsx variable" declaration.
 *
 * (See https://babeljs.io/docs/plugins/transform-react-jsx/#example-custom )
 *
 * @param name
 * @param attrs
 * @returns {{name: *, attrs: *, kids}}
 */
export default function jsxToModel(name, attrs)
{
    return {
        name,
        attrs,
        kids : Array.prototype.slice.call(arguments, 2).map(handleText)
    }
}
