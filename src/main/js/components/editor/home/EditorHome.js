import Link from "../../../ui/Link";
import appUri from "../../../util/app-uri";
import i18n from "../../../service/i18n";
import AutoHeight from "../../../ui/AutoHeight";
import SymbolElement from "../../../editor/gui/SymbolElement";
import GUIContainer from "../../../editor/gui/GUIContainer";
import React from "react";

const editorSymbols = require("../../../../svg/editor.svg");


import { navigateView } from "../../../actions/view"

var EditorHome = AutoHeight( class extends React.Component {
    render()
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
});

export default EditorHome
