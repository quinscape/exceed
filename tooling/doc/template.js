import fs from "fs";

class Template
{
    constructor(template)
    {
        //console.log("TEMPLATE", template);

        const varRE = /(\$[A-Z0-9_]+)/g;

        let m;

        const array = [];

        let index = 0;
        do
        {
            m = varRE.exec(template);
            if (m)
            {
                if (m.index > index)
                {
                    array.push(template.substring(index, m.index), null);
                }
                array.push(null, m[1].substring(1));
                index = m.index + m[1].length;
            }

        } while (m);
        array.push(template.substring(index), null);

        this.parts = array;
    }

    render(model)
    {
        const { parts } = this;

        let out = "";

        for (let i = 0; i < parts.length; i+=2)
        {
            const str = parts[i];
            const variable = parts[i+1];

            if (str)
            {
                out += str;
            }
            else
            {
                out += model[variable] || "";
            }
        }
        return out;
    }
}

export default Template

