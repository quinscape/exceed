var token;
var header;
var param;

function init()
{
    if (!token)
    {
        var metas = document.getElementsByTagName("meta");
        for (var i = 0; i < metas.length; i++)
        {
            var elem = metas[i];
            var currentName = elem.getAttribute("name");
            if (currentName === "token")
            {
                token = elem.getAttribute("content");
            }
            else if (currentName === "token-header")
            {
                header = elem.getAttribute("content");
            }
            else if (currentName === "token-param")
            {
                param = elem.getAttribute("content");
            }
        }

        if (!token || !header || !param)
        {
            throw new Error("Could not initialize CSFR module");
        }
    }
}

module.exports = {
    token: function ()
    {
        init();
        return token;
    },
    tokenHeader: function ()
    {
        init();
        return header;

    },
    tokenParam: function ()
    {

        init();
        return param;
    }
};
