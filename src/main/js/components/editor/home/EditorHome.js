const React = require("react");
const GUIContainer = require("../../../editor/gui/GUIContainer");
const SymbolElement = require("../../../editor/gui/SymbolElement");

const editorSymbols = require("../../../../svg/editor.svg");

const AutoHeight = require("../../../ui/AutoHeight");

const i18n = require("../../../service/i18n");
const appUri = require("../../../util/app-uri");
const Link = require("../../../ui/Link");

import { navigateView } from "../../../actions/view"

var EditorHome = AutoHeight( React.createClass({
    render: function ()
    {
        return (
            <div className="editor-home">
                <GUIContainer height={ this.props.height } centerX={ 0 } centerY={ 0 } zoom={ false }>
                    <SymbolElement
                        id="domain"
                        symbols={ editorSymbols }
                        name="domain"
                        x={ -150 }
                        y={ 10 }
                        onInteraction={ () => navigateView({
                            url: appUri("/domain-editor"),
                            progressive: false
                        }) }
                        draggable={ false }
                    />
                    <SymbolElement
                        id="domain"
                        symbols={ editorSymbols }
                        name="domain"
                        x={ 0 }
                        y={ 0 }
                        onInteraction={ () => navigateView({
                            url: appUri("/domain-editor"),
                            progressive: false
                        }) }
                        draggable={ false }
                    />
                    <SymbolElement
                        id="domain"
                        symbols={ editorSymbols }
                        name="domain"
                        x={ 150 }
                        y={ 10 }
                        onInteraction={ () => navigateView({
                            url: appUri("/domain-editor"),
                            progressive: false
                        }) }
                        draggable={ false }
                    />
                </GUIContainer>
            </div>
        );
    }
}));

module.exports = EditorHome;
