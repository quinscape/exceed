const INDENT_PER_LEVEL = "    ";

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

var primaryAttributes = {
    id: true,
    var: true,
    name: true
};

function attr(attrs, name)
{
    return name + "=\"" + htmlEscape( attrs[name]) + "\"";

}

function renderTagStart(model, indent)
{
    if (model.name === "[String]")
    {
        return htmlEscape(reindent(model.attrs.value, indent));
    }
    else
    {
        var line = indent + "<" + model.name;
        var attrs = model.attrs;
        if (attrs)
        {
            var ats = [];
            var lineLength = line.length;

            if (attrs.id !== undefined)
            {
                ats.push(attr(attrs, "id"));
                lineLength += ats[ats.length - 1].length;
            }

            if (attrs.var !== undefined)
            {
                ats.push(attr(attrs, "var"));
                lineLength += ats[ats.length - 1].length;
            }

            if (attrs.name !== undefined)
            {
                ats.push(attr(attrs, "name"));
                lineLength += ats[ats.length - 1].length;
            }


            for (var name in attrs)
            {
                if (attrs.hasOwnProperty(name) && primaryAttributes[name] !== true)
                {
                    var xmlAttr = attr(attrs, name);
                    ats.push(xmlAttr);

                    lineLength += xmlAttr.length;
                }
            }

            if (ats.length)
            {
                if (lineLength < 73)
                {
                    line += " " + ats.join(" ");
                }
                else
                {
                    line += " " + ats.join("\n    " + indent);
                }
            }

        }
        return line;
    }
}
function dump(lines, model, indent)
{
    var line = renderTagStart(model, indent);

    var kids = model.kids;
    if (kids)
    {
        line += ">";

        lines.push(line);

        var newIndent = indent + INDENT_PER_LEVEL;
        for (var i = 0; i < kids.length; i++)
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

function findContent(model)
{
    if (model.name === "Content")
    {
        return model;
    }

    var kids = model.kids;
    if (kids)
    {
        for (var i = 0; i < kids.length; i++)
        {
            var result = findContent(kids[i]);
            if (result)
            {
                return result;
            }
        }
    }
    return null;
}

function dumpView(l, root, includeLayout)
{
    if (!includeLayout && root.kids.length)
    {
        l.push(renderTagStart(root, "") + ">");

        var content = findContent(root);
        if (content)
        {
            var kids = content.kids;
            if (kids)
            {
                for (var i = 0; i < kids.length; i++)
                {
                    dump(l, kids[i], INDENT_PER_LEVEL);
                }
            }
        }

        // first tag must be view
        l.push("</View>");
    }
    else
    {
        dump(l, root, "");
    }
}

module.exports = {

    toXml: function (model, includeLayout)
    {


        var l = [];
        if (model.type !== "view.View")
        {
            dump(l, model, "");

        }
        else
        {
            var pos, comments = model.comments;
            if (comments)
            {
                for (pos = 0; pos < comments.length; pos++)
                {
                    var s = comments[pos];
                    if (s === null)
                    {
                        dumpView(l, model.root, includeLayout);
                    }
                    else if (typeof s == "string")
                    {
                        l.push("<!--\n" + reindent(s, "    ") + "\n-->");
                    }
                }
            }
            else
            {
                dumpView(l, model.root, includeLayout);
            }
        }

        return l.join("\r\n");
    },
    reindent: reindent
};
