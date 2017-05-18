import React from "react";
import update from "react-addons-update";

import aceLoader from "../editor/ace-loader"


const DIFF_EDITOR_OPTIONS = {
    showFoldWidgets: false,
    showLineNumbers: false
};

import i18n from "../service/i18n";

const MODEL_DELETED = JSON.stringify(i18n("Model deleted"));

class MergeEditor extends React.Component
{
    componentWillReceiveProps(nextProps)
    {
        var location = nextProps.locationLink.value;
        if (location !== this.props.locationLink.value)
        {
            var editors = this._aceDiff.getEditors();

            editors.left.setValue(location.ours, 1);
            editors.right.setValue(location.theirs, 1);
        }
    }

    componentDidMount()
    {
        aceLoader.load().then(ace => {
            const AceDiff = require("brace-diff");
            //console.log("MOUNT MergeEditor", this.props);

            var location = this.props.locationLink.value;

            var isOursDeleted = location.ours === "null";
            var isTheirsDeleted = location.theirs === "null";
            this._aceDiff = new AceDiff({
                mode: "ace/mode/json",
                left: {
                    content: isOursDeleted ? MODEL_DELETED : location.ours,
                    editable: !isOursDeleted,
                    copyLinkEnabled: false
                },
                right: {
                    content: isTheirsDeleted ? MODEL_DELETED : location.theirs ,
                    editable: false,
                    copyLinkEnabled: true
                }
            });

            var editors = this._aceDiff.getEditors();

            editors.left.setOptions(DIFF_EDITOR_OPTIONS);
            editors.right.setOptions(DIFF_EDITOR_OPTIONS);

            editors.left.on("blur", this.checkChanges);
        });
    }


    checkChanges()
    {
        var locationLink = this.props.locationLink;
        var location = locationLink.value;

        if (location.ours === "null")
        {
            return location;
        }

        var editors = this._aceDiff.getEditors();
        var edited = editors.left.getValue();

        var hasError = false;
        try
        {
            JSON.parse(edited);
        }
        catch(err)
        {
            hasError = true;
        }

        if (location.ours !== edited)
        {
            //console.log("CHANGED");

            var newLocation = update(location, {
                ours: {$set : edited},
                error: {$set: hasError},
                resolved: {$set: false}
            });
            locationLink.requestChange(newLocation);

            return newLocation;
        }
        return location;
    }

    render()
    {
        return (
            <div className="merge-editor">
                <div id="acediff-left-editor" className="col-md-5 col-no-padding" />
                <div id="acediff-gutter" className="col-md-1 col-no-padding"/>
                <div id="acediff-right-editor" className="col-md-5 col-no-padding"/>
            </div>
        );
    }
}

export default MergeEditor
