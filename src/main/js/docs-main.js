//
import ModelDocs from "./editor/ModelDocs";
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
const MouseTrap = require("./util/global-undo");
const Event = require("./util/event");
import domready from "domready";

import React from "react";
import { render } from "react-dom";

const i18n = require("./service/i18n");

import Services from "./services"

import { evaluateEmbedded } from "./util/startup"

domready(function ()
{

    const data = evaluateEmbedded("root-data", "x-ceed/view-data");

    render(
        <div className="container">
            <div className="row">
                <div className="col-md-12">
                    <ModelDocs
                        locations={ data.meta.modelLocations }
                        model={ data.meta.modelDocs }
                    />
                </div>
            </div>
        </div>,
        document.getElementById("root"));
});


module.exports = Services;
