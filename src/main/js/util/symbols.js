
var re = /^symbol:(.*?):/

function findSymbols(symbols, elem)
{
    var id = elem.id;
    if (id)
    {
        var m = re.exec(id);
        if (m)
        {
            var name = m[1];
            var group = symbols[name];
            var inner = elem.innerHTML;

            console.log("BBOX:", elem.getBBox());

            if (group)
            {
                symbols[name] = group + inner;
            }
            else
            {
                symbols[name] = inner;
            }
        }
    }

    var nextSibling = elem.nextElementSibling;
    if (nextSibling)
    {
        findSymbols(symbols, nextSibling);
    }

    var firstChild = elem.firstElementChild;
    if (firstChild)
    {
        findSymbols(symbols, firstChild);
    }
}

module.exports = function (xmlDoc)
{
    var symbols = {

    };

    findSymbols(symbols, xmlDoc.documentElement);

    return symbols;
};
