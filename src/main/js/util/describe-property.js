/**
 *
 * @param propType      property model structure
 * @param [asClass]     {boolean} true if the ouput is to be embedded in a HTML class attribute
 * @returns {*}
 */
export default function(propType, asClass)
{
    const { type, typeParam } = propType;

    switch (type)
    {
        case "Map":
        case "List":
            if (!typeParam || typeParam === "Object")
            {
                return type;
            }
            else
            {
                return type + ( asClass ? "-" + typeParam : "<" + typeParam + ">");
            }

        case "DomainType":
        case "Enum":
        case "State":
            if (!typeParam)
            {
                return type;
            }
            else
            {
                return typeParam;
            }

        default:
            return type;
    }
}
