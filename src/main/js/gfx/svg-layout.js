var React = require("react");
var ReactDOM = require("react-dom");
var Promise = require("es6-promise-polyfill").Promise;
import Enum from "./../util/enum";

const TEST_PHRASE = "THEqUICKbROWNFOXjUMpSOVERTHELAZydOg";

const FONT_SIZES = {
    LARGE: 16,
    LARGER: 14,
    NORMAL: 12,
    SMALL: 11,
    TINY: 10
};

/**
 * Symbolic text size constants
 *
 * @type TextSize
 */
var TextSize = new Enum(FONT_SIZES);

var exampleTextSizes = {};

/**
 * Our impromptu layout model for text
 *
 * @param ppc       average pixels per character for the example phrase
 * @param height    pixel height of the text element containing the test phrase
 * @constructor
 */
function Size(ppc,height)
{
    this.height = height;
    this.ppc = ppc;
}

var start;

var initPromise;

var probeElems = {};

/**
 * Helper module for SVG Text layout. It measures a test phrase in several sizes so that we have a rough guess what size a text is in vector units.
 *
 * @type {{TextSize: TextSize, getExampleTextSizes: function, getFontSize: function, init: function}}
 */
export default {
    TextSize: TextSize,
    getExampleTextSizes: function()
    {
        return exampleTextSizes;
    },
    getFontSize: function(textSize)
    {
        if (!TextSize.isValid(textSize))
        {
            throw new Error("textSize must be one of ", TextSize.values().join(", "));
        }

        return FONT_SIZES[textSize];
    },
    init: function()
    {
        if (!initPromise)
        {
            initPromise = new Promise(function (resolve, reject)
            {
                try
                {
                    var container = document.createElement("div");
                    container.style.position = "absolute";
                    container.style.left = "-2000px";
                    document.body.appendChild(container);

                    var SvgLayout = React.createClass({

                        componentDidMount: function ()
                        {
                            var sizes = {};
                            var sizeNames = TextSize.values();

                            for (var i = 0; i < sizeNames.length; i++)
                            {
                                var name = sizeNames[i];

                                var bBox = probeElems[name].getBBox();
                                sizes[name] = new Size(bBox.width / TEST_PHRASE.length, bBox.height);
                            }

                            ReactDOM.unmountComponentAtNode(container);
                            document.body.removeChild(container);

                            //console.info("svg layout done in %sms", Date.now() - start);
                            resolve(exampleTextSizes = sizes);
                        },

                        text: function(name, idx, size)
                        {
                            return (
                                <text ref={ e => probeElems[name] = e } key={ idx } x="10" y={ 20 + idx * 20} fontSize={ size }>{ TEST_PHRASE }</text>
                            )
                        },

                        render: function ()
                        {
                            var probes = [];

                            var idx = 0;
                            for (var name in FONT_SIZES)
                            {
                                if (FONT_SIZES.hasOwnProperty(name))
                                {
                                    probes.push(this.text(name, idx++, FONT_SIZES[name]));
                                }
                            }


                            return (
                                <svg width="400" height="100">
                                    { probes }
                                </svg>
                            )
                        }
                    });

                    start = Date.now();

                    ReactDOM.render(React.createElement(SvgLayout, null), container);
                }
                catch(e)
                {
                    reject(e);
                }
            })
        }

        //console.log("svgLayout init");
        return initPromise;
    }
}

