//const ReactCSSTransitionGroup = require("react-addons-css-transition-group");
//XXX: port 2 new version, only import fixed
import DataGraph from "../../../domain/graph";
import omit from "../../../util/omit";

const React = require("react");
const update = require("react-addons-update");
const LinkedStateMixin = require("react-addons-linked-state-mixin");
const assign = require("object-assign");

const DomainEditorGraph = require("./DomainEditorGraph");
const DomainTypeForm = require("./DomainTypeForm");
const EnumTypeForm = require("./EnumTypeForm");
const NamedSelector = require("./NamedSelector");
const MergeModal = require("./MergeModal");

const SelectField = require("../../std/form/SelectField");
const Toolbar = require("../../std/form/Toolbar");

const actionService = require("../../../service/action");
const domainService = require("../../../service/domain");
const i18n = require("../../../service/i18n");
const undoService = require("../../../service/undo");

const Button = require("../../../ui/Button");
const FilterField = require("../../../ui/FilterField");
const AutoHeight = require("../../../ui/AutoHeight");

const Dialog = require("../../../util/dialog");
const firstProp = require("../../../util/firstProp");
const keys = require("../../../util/keys");
const notify = require("../../../util/notify");
const Settings = require("../../../util/settings");
const ValueLink = require("../../../util/value-link");
const values = require("../../../util/values");
const Enum = require("../../../util/enum");

const EditingType = new Enum({
    NONE: true,
    DOMAIN_TYPE: true,
    ENUM_TYPE: true
});

const DEFAULT_VIEW_SETTING = Settings.create("def-domain-view", "Default");

function isReferenced(types, typeName)
{
    for (var name in types)
    {
        if (types.hasOwnProperty(name))
        {
            var properties = types[name].properties;
            for (var i = 0; i < properties.length; i++)
            {
                var foreignKey = properties[i].foreignKey;
                if (foreignKey && foreignKey.type === typeName)
                {
                    return true;
                }
            }
        }
    }
    return false;
}

function filterSystem(type)
{
    return !type.system;
}

/**
 * Creates the default visibility for the application domain. System types are only visible if there is a relation to them.
 *
 * @param types
 * @returns {{}}
 */
function getDefaultVisible(types)
{
    var visible = {};
    for (var name in types)
    {
        if (types.hasOwnProperty(name))
        {
            var domainType = types[name];
            visible[name] = !domainType.system && (domainType.name.indexOf("App") !== 0 || isReferenced(types, name));
        }
    }

    return visible;
}

function negate(v)
{
    return !v;
}

function mapToName(type)
{
    return type.name;
}

var DomainEditor = AutoHeight(React.createClass({

    mixins: [LinkedStateMixin],

    newUndoState: function (partial, cb)
    {
        this.undo.newState(
            assign({}, this.state.undoState, partial),
            cb
        );
    },

    replaceUndoState: function (partial, cb)
    {
        this.undo.replaceState(
            assign({}, this.state.undoState, partial),
            cb
        );
    },

    getInitialState: function ()
    {

        const domainTypes = domainService.getDomainTypes();

        const dataGraph = DataGraph(domainTypes, {
            type: "OBJECT",
            columns: {
                domainTypes: {
                    name: "domainTypes",
                    type: "Map",
                    typeParam: "DomainTypeModel"
                },
                enumTypes: {
                    name: "enumTypes",
                    type: "Map",
                    typeParam: "EnumTypeModel"
                }
            },
            rootObject: {
                domainTypes: domainTypes,
                enumTypes: domainService.getEnumTypes()
            }
        }, this.onChange);

        var domainEditorViews = this.props.domainEditorViews;
        var selectedView = DEFAULT_VIEW_SETTING();

        if (!domainEditorViews[selectedView])
        {
            domainEditorViews = update(domainEditorViews, {
                [selectedView]: { $set: {
                    positions: false,
                    visible: getDefaultVisible(domainTypes)
                }}
            });
        }

        var undoState = {
            dataGraph: dataGraph,
            removedDomainTypes: [],
            domainEditorViews: domainEditorViews,
            selectedView: selectedView
        };

        this.undo = undoService.create(undoState, this.restoreUndoState);

        return {

            undoState: undoState,

            filter: "",
            mergeOpen: false,
            mergeLocations: [],

            editing: null,
            editingType: EditingType.NONE,
            editingProp: null
        };
    },

    restoreUndoState: function (undoState, done)
    {
        this.setState({
            undoState: undoState,
        }, done);
    },

    onChange: function (newList, path)
    {

        const editing = this.state.editing;
        const editingType = this.state.editingType;

        var editingProp = this.state.editingProp;

        if (editing && editingType === EditingType.DOMAIN_TYPE)
        {
            var currentPropsLength = newList.rootObject.domainTypes[editing].properties.length;

            //console.log("currentPropsLength", newList, currentPropsLength, editing, editingProp);

            if (editingProp >= currentPropsLength)
            {
                editingProp = currentPropsLength - 1;
            }
        }

        this.newUndoState({
            dataGraph: newList,
            editing: editing,
            editingType: editingType,
            editingProp: editingProp
        });
    },


    componentWillUnmount: function ()
    {
        this.undo.destroy();
    },

    toggleDomainType: function (domainType)
    {
        var selectedView = this.state.undoState.selectedView;

        this.newUndoState({
            domainEditorViews: update(this.state.undoState.domainEditorViews, {
                [selectedView] : {
                    visible: {
                        [ domainType ]: {$apply: negate}
                    }
                }
            })
        });
    },

    editType: function (name, index)
    {
        console.log("editType", name, index);

        this.setState({
            editing: name,
            editingType: name ? EditingType.DOMAIN_TYPE : EditingType.NONE,
            editingProp: typeof index === "number" ? index: 0
        });
    },

    removeType: function (name)
    {
        const undoState = this.state.undoState;
        const dataGraph = undoState.dataGraph;
        const cursor = dataGraph.getCursor(['domainTypes'], false);
        const domainTypes = dataGraph.rootObject.domainTypes;

        const removedType = domainTypes[name];

        const remainingTypes = omit(domainTypes, name);
        const newGraph = cursor.set(null, remainingTypes);

        //console.log("newGraph", newGraph);

        this.newUndoState({
            dataGraph: newGraph,
            removedDomainTypes: update(undoState.removedDomainTypes, { $push: [ removedType ] }),
            editing: null,
            editingType: EditingType.NONE,
            editingProp: 0
        });
    },

    editEnum: function (name)
    {
        this.setState({
            editing: name,
            editingType: name ? EditingType.ENUM_TYPE : EditingType.NONE,
            editingProp: 0
        });
    },

    newType: function ()
    {
        Dialog.prompt({
            title: i18n("Enter New Domain Type Name"),
            choices: [ i18n("Cancel"), i18n("Create New Type")],
            properties: [
            {
                name: "name",
                type: "PlainText"
            },
            {
                name: "extension",
                selectValues: this.props.extensions.map((ext,idx) => { return {
                    display: ext.name,
                    value: idx
                }; }),
                type: "PlainText"
            }
        ]}).then((result) => {

            console.log("Dialog result", result);

            if (result.choice)
            {
                var name = result.inputs.name;
                if (this.state.undoState.dataGraph.rootObject.domainTypes.hasOwnProperty(name))
                {
                    notify(i18n("Domain Type '{0}' Name Already Exists", name));
                    return;
                }

                var newTypeCursor = this.state.undoState.dataGraph.getCursor(["domainTypes", name], false);

                var selectedView = this.state.undoState.selectedView;
                this.newUndoState({
                    dataGraph: newTypeCursor.set(null, {
                        extension: result.inputs.extension,
                        name: name,
                        properties: [{
                            name: "id",
                            type: "UUID"
                        }]
                    }),
                    domainEditorViews: update(this.state.undoState.domainEditorViews, {
                        [selectedView]: {
                            visible: {
                                [ name ]: {$set: true}
                            }
                        }
                    }),
                    editing: name,
                    editingType: name ? EditingType.DOMAIN_TYPE : null,
                    editingProp: 0
                });
            }
        }).catch(function (err)
        {
            console.error(err);
        });

    },

    newEnum: function ()
    {
        var name = prompt(i18n("Enter New Enum Type Name"));
        if (name)
        {
            if (this.state.undoState.dataGraph.rootObject.enumTypes.hasOwnProperty(name))
            {
                notify(i18n("Enum Type '{0}' Name Already Exists", name));
                return;
            }

            var enumTypesCursor = this.state.undoState.dataGraph.getCursor(["enumTypes"], false);

            this.newUndoState({
                dataGraph: enumTypesCursor.set([ name ], {
                    _type: "EnumType",
                    name: name,
                    values: ["New"]
                })
            });

            this.editEnum(name);
            //console.log("NEW TYPE", name);
        }

    },

    setFilter: function (filter)
    {
        this.setState({
            filter: filter || ""
        });
    },

    createUpdateActionModel: function ()
    {
        const prevTypes = domainService.getDomainTypes();
        const nextTypes = this.state.undoState.dataGraph.rootObject.domainTypes;

        let newTypes = [];
        let changedTypes = [];

        for (let name in nextTypes)
        {
            if (nextTypes.hasOwnProperty(name))
            {
                let prevType = prevTypes[name];

                if (!prevType)
                {
                    newTypes.push(nextTypes[name]);
                }
                else if (nextTypes[name] !== prevType)
                {
                    changedTypes.push(nextTypes[name]);
                }
            }
        }

        return {
            action: "updateDomain",
            newTypes: newTypes,
            newTypeExtensions: newTypes.map(domainType => domainType.extension),
            changedTypes: changedTypes,
            removedTypes: this.state.undoState.removedDomainTypes
        };
    },


    save: function ()
    {
        var actionModel = this.createUpdateActionModel();

        console.log("UPDATE DOMAIN", actionModel);

        actionService.execute(actionModel).then((result) =>
            {
                console.log("UPDATE DOMAIN RESULT", result);

                if (result.ok)
                {
                    var dataGraph = this.state.undoState.dataGraph;
                    this.replaceUndoState({
                        dataGraph: dataGraph.copy(
                            update(dataGraph.rootObject, {
                                domainTypes: { $set: result.mergedDomainTypes }
                            })
                        )
                    });
                    this.undo.markSaved();
                }
                else
                {
                    this.setMergeOpen(true, result.locations);
                }
            })
            .catch(function (err)
            {
                console.error(err);
            });
    },

    addView: function ()
    {
        var newViewName = prompt(i18n("Enter New Domain View Name"));
        if (newViewName)
        {
            this.newUndoState({
                domainEditorViews: update(this.state.undoState.domainEditorViews, {
                    [newViewName] : { $set: {
                        visible: getDefaultVisible(this.state.undoState.dataGraph.rootObject.domainTypes),
                        positions: false
                    }}
                }),
                selectedView: newViewName
            });
        }
    },

    removeView: function (viewToRemove)
    {
        const domainEditorViews = this.state.undoState.domainEditorViews;

        this.newUndoState({
            domainEditorViews: omit(domainEditorViews, viewToRemove),
            selectedView: firstProp(domainEditorViews)
        });
    },

    setMergeOpen: function (isOpen, mergeLocations)
    {
        mergeLocations = mergeLocations || this.state.mergeLocations;

        console.log("OPEN", isOpen, "MERGE LOCATIONS", mergeLocations);

        this.setState({
            mergeOpen: isOpen,
            mergeLocations: mergeLocations
        })
    },

    updateGraphPositions: function (positions)
    {
        //console.log("UPDATE POSITIONS", JSON.stringify(positions, 0, 4));

        var selectedView = this.state.undoState.selectedView;

        this.newUndoState({
            domainEditorViews: update(this.state.undoState.domainEditorViews, {
                [selectedView] : {
                    positions: { $set: positions }
                }
            })
        });
    },

    selectDomainView: function (selectedView)
    {
        // since changes to the view will push their current view selection as part of their undo state, we do not
        // need to create new undo states for simple view changes. The view will never the less follow the current
        // view selection
        this.setState({
            selectedView: selectedView
        });
    },

    performMerge: function(locations)
    {
        var cursor = this.state.undoState.dataGraph.getCursor(["domainTypes"], false);

        var withUpdatedVersion = assign({}, cursor.get());

        for (var i = 0; i < locations.length; i++)
        {
            var mergedType = locations[i].merged;
            if (mergedType == null)
            {
                delete withUpdatedVersion[locations[i].name];
            }
            else
            {
                withUpdatedVersion[mergedType.name] = mergedType;
            }
        }

        console.log("MERGED TYPES", withUpdatedVersion);

        this.replaceUndoState({
            dataGraph: cursor.set(null, withUpdatedVersion),
            removedDomainTypes: [],
            editing: null,
            editingType: EditingType.NONE,
            editingProp: 0
        }, this.save);
    },

    render: function ()
    {
        //console.log("RENDER", this.props, this.state);

        var undoState = this.state.undoState;
        var domainTypesCursor = undoState.dataGraph.getCursor(["domainTypes"]);
        var domainTypes = domainTypesCursor.value;

        var enumTypesCursor = undoState.dataGraph.getCursor(["enumTypes"]);
        var enumTypes = enumTypesCursor.value;

        var domainTypeNames = values(domainTypes).filter(filterSystem).map(mapToName);
        domainTypeNames.sort();

        var enumTypeNames = values(enumTypes).map(mapToName);
        enumTypeNames.sort();

        var height = this.props.height;
        var editing = this.state.editing;
        var selectedView = undoState.selectedView;

        var domainViewNames = keys(undoState.domainEditorViews);
        domainViewNames.sort();

        var haveMultipleDomainViews = domainViewNames.length > 1;

        //console.log("domainViewNames", domainViewNames);

        var visible = undoState.domainEditorViews[selectedView].visible;


        return (
            <div className="domain-editor container-fluid" style={{ height: height}}>
                <div className="row">
                    <div className="col-md-12">
                        <div className="form-inline">
                            <Toolbar>
                                <Button
                                    icon="save"
                                    className="btn-primary"
                                    text={ i18n("Save") }
                                    disabled={ this.undo.isSaved() }
                                    onClick={ this.save }
                                />
                                <Button
                                    icon="repeat"
                                    text={ i18n("Revert All") }
                                    disabled={ this.undo.isSaved() }
                                    onClick={ e => this.undo.revert() }
                                />
                                <Button
                                    text={ i18n("Undo") }
                                    disabled={ !this.undo.canUndo() }
                                    onClick={ e => this.undo.undo() }
                                />
                                <Button
                                    text={ i18n("Redo") }
                                    disabled={ !this.undo.canRedo() }
                                    onClick={ e => this.undo.redo() }
                                />
                                <SelectField
                                    label={ i18n("Domain View") }
                                    data={ domainViewNames }
                                    value={ new ValueLink(undoState.selectedView, this.selectDomainView ) }
                                    propertyType={ {type: "PlainText"} }
                                    disabled={ !haveMultipleDomainViews }
                                />
                                <Button
                                    text={ i18n("New Domain View") }
                                    onClick={ e => this.addView() }
                                />
                                <Button
                                    text={ i18n("Remove Domain View") }
                                    disabled={ !haveMultipleDomainViews }
                                    onClick={ e => this.removeView(selectedView) }
                                />
                            </Toolbar>
                        </div>
                    </div>
                </div>
                <div className="row">
                    <div className="col-md-3" style={{ height: height, overflow: "auto" }}>
                        <FilterField
                            valueLink={ new ValueLink(this.state.filter, (filter) => this.setFilter(filter) ) }
                            placeholder={ i18n("Filter domain types") }
                        />
                        <NamedSelector
                            title={ i18n('Domain Types') }
                            editing={ editing }
                            names={ domainTypeNames }
                            edit={ this.editType }
                            new={ this.newType }
                            visible={ visible }
                            toggle={ this.toggleDomainType }
                            newLabel={ i18n("New (D)omainType") }
                            newAccessKey="D"
                            filter={ this.state.filter }
                        />
                        <hr/>
                        <NamedSelector
                            title={ i18n('Enum Types') }
                            editing={ editing }
                            names={ enumTypeNames }
                            edit={ this.editEnum }
                            new={ this.newEnum }
                            newLabel={ i18n("New (E)numType") }
                            newAccessKey="E"
                            filter={ this.state.filter }
                        />

                    </div>
                    <div className="col-md-9" style={!!editing ? { height: 0, overflow: "visible", zIndex: 0 } : null}>

                        <div className="hidden-xs">
                            <DomainEditorGraph
                                height={ height }
                                domainTypes={ domainTypes }
                                visible={ visible }
                                onInteraction={ this.editType }
                                positionsLink={ new ValueLink(undoState.domainEditorViews[selectedView].positions, this.updateGraphPositions ) }
                            />
                        </div>
                    </div>
                    <div className="col-md-9"
                         style={{ display: !editing && "none", zIndex: 10, position: "relative", background: "rgba(245,250,255,0.9)" }}>
                        {
                            this.state.editingType === EditingType.DOMAIN_TYPE &&
                            <DomainTypeForm
                                key={ editing }
                                name={ editing }
                                edit={ this.editType }
                                remove={ this.removeType }
                                domainTypes={ domainTypes }
                                domainTypeNames={ domainTypeNames }
                                storageOptions={ this.props.storageOptions }
                                enumTypes={ enumTypeNames }
                                cursor={ editing && domainTypesCursor.getCursor([editing]) }
                                editingPropLink={
                                    new ValueLink(this.state.editingProp, (editingProp) => {
                                        this.setState({
                                            editingProp: editingProp
                                        });
                                    })
                                }
                            />
                        }
                        {
                            this.state.editingType === EditingType.ENUM_TYPE &&
                            <EnumTypeForm
                                key={ editing }
                                name={ editing }
                                new={ this.newEnum }
                                edit={ this.editEnum }
                                cursor={ editing && enumTypesCursor.getCursor([editing]) }
                            />
                        }
                    </div>
                </div>
                {
                    <MergeModal
                        openLink={ new ValueLink(this.state.mergeOpen, this.setMergeOpen) }
                        locationsLink={ new ValueLink(this.state.mergeLocations, this.performMerge)}
                    />
                }
            </div>
        );
    }
}));


module.exports = DomainEditor;

