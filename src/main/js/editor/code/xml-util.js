const l = [];

function htmlEscape(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function reindent(text, indent)
{
    return text
        .replace(/^\s*/mg, "")
        .replace(/(^|\r\n|\n)(.)/g, "$1" + indent + "$2");
}

const primaryAttributes = {
    id: true,
    var: true,
    name: true
};

function attr(attrs, name)
{
    return name + "=\"" + htmlEscape( attrs[name]) + "\"";

}

function dump(lines, model, indent)
{
    if (model.name === "[String]")
    {
        lines.push(htmlEscape(reindent(model.attrs.value, indent)));
        return;
    }

    let line = indent + "<" + model.name;
    let lineLength = line.length;

    const attrs = model.attrs;
    if (attrs)
    {
        const ats = [];

        if (attrs.id !== undefined)
        {
            ats.push(attr(attrs, "id"));
            lineLength += ats[ats.length-1].length;
        }

        if (attrs.var !== undefined)
        {
            ats.push(attr(attrs, "var"));
            lineLength += ats[ats.length-1].length;
        }

        if (attrs.name !== undefined)
        {
            ats.push(attr(attrs, "name"));
            lineLength += ats[ats.length-1].length;
        }

        for (let name in attrs)
        {
            if (attrs.hasOwnProperty(name) && primaryAttributes[name] !== true)
            {
                const xmlAttr = attr(attrs, name);
                ats.push(xmlAttr);

                lineLength += xmlAttr.length;
            }
        }

        if (lineLength < 73)
        {
            line += " " + ats.join(" ");
        }
        else
        {
            line += " " + ats.join("\n    " + indent);
        }

    }
    const kids = model.kids;
    if (kids)
    {
        line += ">";

        lines.push(line);

        const newIndent = indent + "    ";
        for (let i = 0; i < kids.length; i++)
        {
            dump(lines, kids[i], newIndent);
        }

        line = indent + "</" + model.name + ">";
        lines.push(line);
    }
    else
    {
        line += "/>";
        lines.push(line);
    }
}


module.exports = {

    toXml: function (model, content)
    {
        content = content || "main";

        const l = [];
        const comments = model.comments;
        if (comments)
        {
            for (let pos = 0; pos < comments.length; pos++)
            {
                let s = comments[pos];
                if (s === null)
                {
                    dump(l, model.content[content], "");
                }
                else if (typeof s === "string")
                {
                    l.push("<!--\n" + reindent(s, "    ") + "\n-->");
                }
            }
        }
        else
        {
            dump(l, model.content[content], "");
        }
        return l.join("\r\n");
    },
    reindent: reindent
};
