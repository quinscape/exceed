
var pos = 10;
var count = 0;

export default function (message)
{

    var div = document.createElement("div");
    div.className="notification";
    div.style.top = pos + "px";
    div.appendChild(document.createTextNode(message));

    document.body.appendChild(div);

    pos += div.clientHeight + 10;
    count++;

    window.setTimeout(function ()
    {
        div.parentNode.removeChild(div);
        count--;

        if (count === 0)
        {
            pos = 10;
        }

    }, 9900);
};
