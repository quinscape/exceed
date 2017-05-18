import svgLayout from "../../../gfx/svg-layout"
const TextSize = svgLayout.TextSize;

// initialized delayed
var HEADING_LAYOUT;
var PROP_LAYOUT;
var FONT_SIZE_LARGE;
var FONT_SIZE_LARGER;
var FONT_SIZE_NORMAL;

const LIGHT_BACKGROUND = true;

function initLayout()
{
    if (!HEADING_LAYOUT)
    {
        let exampleSizes = svgLayout.getExampleTextSizes();

        HEADING_LAYOUT = exampleSizes[TextSize.LARGE];
        PROP_LAYOUT = exampleSizes[TextSize.NORMAL];

        FONT_SIZE_LARGE = svgLayout.getFontSize(TextSize.LARGER);
        FONT_SIZE_LARGER = svgLayout.getFontSize(TextSize.NORMAL);
        FONT_SIZE_NORMAL = svgLayout.getFontSize(TextSize.SMALL);
    }
}


const DECORATION_TEXTS = {
    Boolean : "\u2714",
    Date : "1.1.70",
    Enum : "A,B,C",
    Integer : "42",
    Long : "42L",
    Object : "\u2605",
    List : "[]",
    Map : "{}",
    PlainText : "\"str\"",
    RichText : "&lt;html/&gt;",
    Timestamp : "\u231a",
    UUID : "id"
};

const DECORATION_COLORS =  LIGHT_BACKGROUND ? {
    // Color theme against light backgrounds
    Boolean : "#0c0",
    Date : "#000",
    Enum : "#444",
    Integer : "#292",
    Long : "#00C",
    PlainText : "#777",
    RichText : "#777",
    Timestamp : "#000",
    UUID : "#522",
    Object : "#444",
    List : "#222",
    Map : "#222"

    } : {

    // Color theme against dark backgrounds
    Boolean : "#0c0",
    Date : "#000",
    Enum : "#444",
    Integer : "#292",
    Long : "#00C",
    PlainText : "#777",
    RichText : "#777",
    Timestamp : "#000",
    UUID : "#522",
    Object : "#444",
    List : "#222",
    Map : "#222"
};


module.exports = {
    getHeadingLayout: function ()
    {
        initLayout();
        return HEADING_LAYOUT;
    },
    getPropLayout: function ()
    {
        initLayout();
        return PROP_LAYOUT;
    },
    getLargeFontSize: function ()
    {
        initLayout();
        return FONT_SIZE_LARGE;
    },
    getLargerFontSize: function ()
    {
        initLayout();
        return FONT_SIZE_LARGER;
    },
    getNormalFontSize: function ()
    {
        initLayout();
        return FONT_SIZE_NORMAL;
    },
    getLabelForType: function (type)
    {
        return DECORATION_TEXTS[type];
    },
    getColorForType: function (type)
    {
        return DECORATION_COLORS[type];
    }

};

