var l=[];

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
    return " " + name + "=\"" + htmlEscape( attrs[name]) + "\"";

}

function dump(lines, model, indent)
{
    if (model.name === "[String]")
    {
        lines.push(htmlEscape(reindent(model.attrs.value, indent)));
        return;
    }

    var line = indent +  "<" + model.name;
    var attrs = model.attrs;
    if (attrs)
    {
        if (attrs.id !== undefined)
        {
            line += attr(attrs, "id");
        }

        if (attrs.var !== undefined)
        {
            line += attr(attrs, "var");
        }

        if (attrs.name !== undefined)
        {
            line += attr(attrs, "name");
        }

        for (var name in attrs)
        {
            if (attrs.hasOwnProperty(name) && primaryAttributes[name] !== true)
            {
                line += attr(attrs, name);
            }
        }
    }
    var kids = model.kids;
    if (kids)
    {
        line += ">";

        lines.push(line);

        var newIndent = indent + "    ";
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


module.exports = {

    toXml: function (model)
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
                        dump(l, model.root, "");
                    }
                    else if (typeof s == "string")
                    {
                        l.push("<!--\n" + reindent(s, "    ") + "\n-->");
                    }
                }
            }
            else
            {
                dump(l, model.root, "");
            }
        }

        return l.join("\r\n");
    },
    reindent: reindent
};
