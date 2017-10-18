/**
 * The version properties of our location models will always be different, otherwise there'd be no merge.
 *
 * So we filter out all properties with the name "versionGUID".
 */
import React from "react";
import update from "immutability-helper";
import Modal from "react-bootstrap/lib/Modal";
import cx from "classnames";
import i18n from "../../../service/i18n";
import ValueLink from "../../../util/value-link";
import MergeEditor from "../../../ui/MergeEditor";
import Button from "../../../ui/Button";
import Toolbar from "../../std/form/Toolbar";


function filterVersion(name, val)
{
    return name !== "versionGUID" ? val : undefined;
}


function getLocationName(location)
{
    // we normally use the name of the type on "our" side, but we might get null as "ours" on deletion
    // in which case we take the "theirs" name.
    return (location.ours && location.ours.name) || location.theirs.name;
}



function convert(model)
{
    return JSON.stringify(model, filterVersion, 4);
}

/**
 * Converts the locations of a de.quinscape.exceed.runtime.action.MergeLocation into an the internal text format plus
 * flags
 *
 * @param locations
 */
function convertLocationsToInternal(locations)
{
    return locations.map(location =>
        {
            return ({
                name: getLocationName(location),
                ours: convert(location.ours),
                theirs: convert(location.theirs),
                error: false,
                resolved: false
            });
        });
}

class MergeModal extends React.Component
{

    state =
    {
            locations: convertLocationsToInternal(this.props.locationsLink.value),
            mergeResult: [],
            detail: 0
    }

    setDetail(detail)
    {
        this.setState({
            detail: detail
        });
    }

    componentWillReceiveProps(nextProps)
    {
        if (!this.props.openLink.value && nextProps.openLink.value)
        {
            var newLocations = nextProps.locationsLink.value;
            console.log("received", newLocations);
            this.setState({
                locations: convertLocationsToInternal(newLocations)
            });
        }
    }

    markResolved(resolved)
    {
        var location = this._mergeEditor.checkChanges();

        const detail = this.state.detail;
        location.resolved = resolved;

        this.setState({
            locations: update(this.state.locations, {
                [detail]: { $set: location }
            })
        });
    }

    useTheirs()
    {
        const detail = this.state.detail;
        const location = this.state.locations[detail];

        this.setState({
            locations: update(this.state.locations, {
                [detail]: {
                    ours: {$set: location.theirs },
                    resolved: { $set: true }
                }
            })
        });
    }

    close()
    {
        this.props.openLink.requestChange(false)
    }

    saveMerged()
    {

        const locationsLink = this.props.locationsLink;
        const original = locationsLink.value;

        var external = this.state.locations.map( (location,idx) => {

            if (location.ours === "null")
            {
                return {
                    name : location.name,
                    merged: null
                };
            }

            var mergedType = JSON.parse(location.ours);
            var theirs = original[idx].theirs;
            if (theirs)
            {
                mergedType.versionGUID = theirs.versionGUID;
            }

            return {
                name : location.name,
                merged: mergedType
            };
        });
        locationsLink.requestChange(external);

        this.close();
    }

    render()
    {
        //console.log("MergeModal", this.props, this.state);

        var locations = this.state.locations;
        if (!locations || !locations.length )
        {
            return false;
        }

        var detail = this.state.detail;

        var locationLink = new ValueLink(locations[detail], location =>
            {
                this.setState({
                    locations: update(this.state.locations, {
                        [detail]: {$set : location},
                    })
                });
            }
        );

        var location = locationLink.value;

        return (
            <Modal show={ this.props.openLink.value } onHide={ this.close } dialogClassName="merge-modal">
                <Modal.Header closeButton>
                    <Modal.Title>{ i18n("Merge Conflict") }</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <form className="form" onSubmit={ this.submit }>
                        <div className="container-fluid">
                            <div className="row">
                                <div className="col-md-12">
                                    { i18n("Merge Hint") }
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-md-5 col-md-offset-1">
                                    <Button text="Use current" onClick={ ev => this.markResolved(true) } disabled={ location.error }/>
                                    <Button text="Mark Unresolved" onClick={ ev => this.markResolved(false) } disabled={ !location.resolved }/>
                                </div>
                                <div className="col-md-5 col-md-offset-1">
                                    <Button text="Use Theirs" onClick={ ev => this.useTheirs() }/>
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-md-5 col-md-offset-1">
                                    { <label>{ i18n("Your Model") }</label> }
                                </div>
                                <div className="col-md-5 col-md-offset-1">
                                    { <label>{ i18n("Other Model") }</label> }
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-md-1 col-no-padding">
                                    <ul className="block-list">

                                    {
                                        locations.map( (location, idx) => {

                                            return (
                                                <li key={ location.name }>
                                                    <a
                                                        className={ cx(
                                                            "btn btn-link",
                                                            detail === idx && "selected"
                                                        ) }
                                                        onClick={ e =>
                                                        {
                                                            this.setDetail(idx);
                                                            e.preventDefault()
                                                        } }
                                                    >
                                                        { location.name + ' '}
                                                        { location.error && <span className="text-danger glyphicon glyphicon-exclamation-sign"/> }
                                                        { location.resolved && <span className="text-success glyphicon glyphicon-ok"/> }
                                                    </a>
                                                </li>
                                            );
                                        } )
                                    }
                                    </ul>
                                </div>
                                <MergeEditor
                                    ref={ c => this._mergeEditor = c}
                                    locationLink={ locationLink }
                                />
                            </div>
                            <div className="row">
                                <div className="col-md-12">
                                    <Toolbar>
                                        <Button
                                            icon="arrow-left"
                                            text={ i18n("Close Unmerged") }
                                            onClick={ this.close }
                                        />
                                        <Button
                                            icon="arrow-left"
                                            text={ i18n("Save merged") }
                                            onClick={ this.saveMerged }
                                            disabled={ !locations.every( location => location.resolved ) }
                                        />
                                    </Toolbar>
                                </div>
                            </div>
                        </div>
                    </form>
                </Modal.Body>
            </Modal>
        );
    }
};

export default MergeModal
