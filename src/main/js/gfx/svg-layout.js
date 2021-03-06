import React from "react";
import ReactDOM from "react-dom";
import { Promise } from "es6-promise-polyfill";

import Enum from "./../util/enum";

const TEST_PHRASE = "THEqUICKbROWNFOXjUMpSOVERTHELAZydOg";

const FONT_SIZES = {
    LARGE: 16,
    LARGER: 14,
    NORMAL: 12,
    SMALL: 11,
    TINY: 10
};

const createReactClass = require("create-react-class");

/**
 * Symbolic text size constants
 *
 * @type TextSize
 */
const TextSize = new Enum(FONT_SIZES);

let exampleTextSizes = {};

class Size {
    /**
     * Our impromptu layout model for text
     *
     * @param ppc       average pixels per character for the example phrase
     * @param height    pixel height of the text element containing the test phrase
     * @constructor
     */
    constructor(ppc,height)
    {
        this.height = height;
        this.ppc = ppc;
    }

    estimateWidth(text)
    {
        return text.length * this.ppc;
    }

}


let start;

let initPromise;

const probeElems = {};

/**
 * Helper module for SVG Text layout. It measures a test phrase in several sizes so that we have a rough guess what size a text is in vector units.
 *
 * @type {{TextSize: TextSize, getExampleTextSizes: function, getFontSize: function, init: function}}
 */
export default {
    TextSize: TextSize,

    /**
     *
     * @returns {Map.<Size>} sizes
     */
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
                    const container = document.createElement("div");
                    container.style.position = "absolute";
                    container.style.left = "-2000px";
                    document.body.appendChild(container);

                    const SvgLayout = createReactClass({

                        componentDidMount: function () {
                            const sizes = {};
                            const sizeNames = TextSize.values();

                            for (let i = 0; i < sizeNames.length; i++)
                            {
                                const name = sizeNames[i];

                                const bBox = probeElems[name].getBBox();
                                sizes[name] = new Size(bBox.width / TEST_PHRASE.length, bBox.height);
                            }

                            ReactDOM.unmountComponentAtNode(container);
                            document.body.removeChild(container);

                            //console.info("svg layout done in %sms", Date.now() - start);
                            resolve(exampleTextSizes = sizes);
                        },

                        text: function (name, idx, size) {
                            return (
                                <text ref={e => probeElems[name] = e} key={idx} x="10" y={20 + idx * 20}
                                      fontSize={size}>{TEST_PHRASE}</text>
                            )
                        },

                        render: function () {
                            const probes = [];

                            let idx = 0;
                            for (let name in FONT_SIZES)
                            {
                                if (FONT_SIZES.hasOwnProperty(name))
                                {
                                    probes.push(this.text(name, idx++, FONT_SIZES[name]));
                                }
                            }

                            return (
                                <svg width="400" height="100">
                                    {probes}
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

