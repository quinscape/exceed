export default function(propType)
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
                return type + "<" + typeParam + ">";
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
