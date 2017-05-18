function components(col)
{
    if (col[0] == "#")
    {
        col = col.substring(1);
    }

    if (col.length == 3)
    {
        return {
            r: parseInt(col[0], 16) * 17,
            g: parseInt(col[1], 16) * 17,
            b: parseInt(col[2], 16) * 17
        };
    }
    else if (col.length == 6)
    {
        return {
            r: parseInt(col.substring(0, 2), 16),
            g: parseInt(col.substring(2, 4), 16),
            b: parseInt(col.substring(4, 6), 16)
        };
    }
    else
    {
        throw new Error("Invalid color " + col);
    }
}

function hex(n)
{
    var s = n.toString(16);

    return s.length == 1 ? "0" + s : s;
}

function mix(col1, col2, ratio)
{
    var c1 = components(col1);
    var c2 = components(col2);

    var r = (c1.r + (c2.r - c1.r) * ratio) | 0;
    var g = (c1.g + (c2.g - c1.g) * ratio) | 0;
    var b = (c1.b + (c2.b - c1.b) * ratio) | 0;

    return rgb(r,g,b);
}

function rgb(r,g,b)
{
    return "#" + hex(r) + hex(g) + hex(b);
}

function alpha(color, opacity)
{
    var col = components(color);
    return "rgba(" + col.r + ", " + col.g + ", " + col.b + ", " + opacity + ")";
}

export default {
    rgb: rgb,
    alpha: alpha,
    mix: mix
}
