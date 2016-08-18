//const ReactCSSTransitionGroup = require("react-addons-css-transition-group");
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

const DataGraph = require("../../../util/data-graph");
const Dialog = require("../../../util/dialog");
const Event  = require("../../../util/event");
const firstProp = require("../../../util/firstProp");
const keys = require("../../../util/keys");
const notify = require("../../../util/notify");
const omit = require("../../../util/omit");
const Settings = require("../../../util/settings");
const ValueLink = require("../../../util/value-link");
const values = require("../../../util/values");

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

function calculateHeight()
{
    return window ? window.innerHeight - 110 : 500;
}


var DomainEditor = React.createClass({

    mixins: [LinkedStateMixin],

    getInitialState: function ()
    {

        const domainTypes = domainService.getDomainTypes();

        var dataGraph = new DataGraph(domainTypes, {
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

        this.undo = undoService.create({
                dataGraph: dataGraph,
                domainEditorViews: domainEditorViews,
                selectedView: selectedView
            },
            this.restoreState
        );

        return {
            dataGraph: dataGraph,
            editing: null,
            editingType: null,
            editingProp: 0,
            height: calculateHeight(),
            filter: "",
            domainEditorViews: domainEditorViews,
            selectedView: selectedView,
            mergeOpen: false,
            mergeLocations: []
        };
    },

    restoreState: function (state, done)
    {
        this.setState({
            dataGraph: state.dataGraph,
            domainEditorViews: state.domainEditorViews,
            selectedView: state.selectedView,
            editing: state.editing,
            editingType: state.editingType,
            editingProp: state.editingProp
        }, done);
    },

    onChange: function (newList, path)
    {

        const editing = this.state.editing;
        const editingType = this.state.editingType;
        var editingProp = this.state.editingProp;

        if (editing && editingType == "DomainType")
        {
            var currentPropsLength = newList.rootObject.domainTypes[editing].properties.length;

            //console.log("currentPropsLength", newList, currentPropsLength, editing, editingProp);

            if (editingProp >= currentPropsLength)
            {
                editingProp = currentPropsLength - 1;
            }
        }

        this.undo.newState({
            dataGraph: newList,
            domainEditorViews: this.state.domainEditorViews,
            selectedView: this.state.selectedView,
            editing: editing,
            editingType: editingType,
            editingProp: editingProp
        });
    },

    componentDidMount: function ()
    {
        Event.add(window, "resize", this.onResize, false);
    },

    componentWillUnmount: function ()
    {
        this.undo.destroy();
        Event.remove(window, "resize", this.onResize, false);
    },


    onResize: function ()
    {
        //console.log("RESIZE");

        this.setState({
            height: calculateHeight()
        })
    },

    toggleDomainType: function (domainType)
    {
        var selectedView = this.state.selectedView;
        this.undo.newState({
            dataGraph: this.state.dataGraph,
            domainEditorViews: update(this.state.domainEditorViews, {
                [selectedView] : {
                    visible: {
                        [ domainType ]: {$apply: negate}
                    }
                }
            }),
            selectedView: selectedView,
            editing: this.state.editing,
            editingType: this.state.editingType,
            editingProp: this.state.editingProp
        });

    },

    editType: function (name)
    {
        this.setState({
            editing: name,
            editingType: name ? "DomainType" : null,
            editingProp: 0
        });
    },

    removeType: function (name)
    {
        var dataGraph = this.state.dataGraph;
        var newGraph = dataGraph.getCursor(['domainTypes'], false).set(null, omit(dataGraph.rootObject.domainTypes, name));

        console.log("newGraph", newGraph);

        this.undo.newState({
            dataGraph: newGraph,
            domainEditorViews: this.state.domainEditorViews,
            selectedView: this.state.selectedView,
            editing: null,
            editingType: null,
            editingProp: 0
        });
        this.setState({
            editing: null,
            editingType: null,
            editingProp: 0
        });
    },

    editEnum: function (name)
    {
        this.setState({
            editing: name,
            editingType: name ? "EnumType" : null,
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
                if (this.state.dataGraph.rootObject.domainTypes.hasOwnProperty(name))
                {
                    notify(i18n("Domain Type '{0}' Name Already Exists", name));
                    return;
                }

                var newTypeCursor = this.state.dataGraph.getCursor(["domainTypes", name], false);

                var selectedView = this.state.selectedView;
                this.undo.newState({
                    dataGraph: newTypeCursor.set(null, {
                        extension: result.inputs.extension,
                        name: name,
                        properties: [{
                            name: "id",
                            type: "UUID"
                        }]
                    }),
                    domainEditorViews: update(this.state.domainEditorViews, {
                        [selectedView]: {
                            visible: {
                                [ name ]: {$set: true}
                            }
                        }
                    }),
                    selectedView: this.state.selectedView,
                    editing: name,
                    editingType: name ? "DomainType" : null,
                    editingProp: 0
                });

                //this.editType(name);
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
            if (this.state.dataGraph.rootObject.enumTypes.hasOwnProperty(name))
            {
                notify(i18n("Enum Type '{0}' Name Already Exists", name));
                return;
            }

            var newEnum = this.state.dataGraph.getCursor(["enumTypes", name], false);

            this.undo.newState({
                dataGraph: newEnum.set(null, {
                    _type: "EnumType",
                    name: name,
                    values: ["New"]
                }),
                domainEditorViews: this.state.domainEditorViews,
                selectedView: this.state.selectedView,
                editing: this.state.editing,
                editingType: this.state.editingType,
                editingProp: this.state.editingProp
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

    save: function ()
    {
        const prevTypes = domainService.getDomainTypes();
        const nextTypes = this.state.dataGraph.rootObject.domainTypes;

        let newTypes = [];
        let changedTypes = [];
        let removedTypes = [];

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

        for (let name in prevTypes)
        {
            if (prevTypes.hasOwnProperty(name))
            {
                if (!nextTypes[name])
                {
                    removedTypes.push(prevTypes[name]);
                }
            }
        }

        actionService.execute({
                action: "updateDomain",
                newTypes: newTypes,
                newTypeExtensions: newTypes.map(domainType => domainType.extension),
                changedTypes: changedTypes,
                removedTypes: removedTypes
            }).then((result) =>
            {
                if (result.ok)
                {
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
            this.undo.newState({
                dataGraph: this.state.dataGraph,
                domainEditorViews: update(this.state.domainEditorViews, {
                    [newViewName] : { $set: {
                        visible: getDefaultVisible(this.state.dataGraph.rootObject.domainTypes),
                        positions: false
                    }}
                }),
                selectedView: newViewName,
                editing: this.state.editing,
                editingType: this.state.editingType,
                editingProp: this.state.editingProp
            });
        }
    },

    removeView: function (viewToRemove)
    {
        const domainEditorViews = this.state.domainEditorViews;

        this.undo.newState({
            dataGraph: this.state.dataGraph,
            domainEditorViews: omit(domainEditorViews, viewToRemove),
            selectedView: firstProp(domainEditorViews),
            editing: this.state.editing,
            editingType: this.state.editingType,
            editingProp: this.state.editingProp
        });
    },

    setMergeOpen: function (isOpen, mergeLocations)
    {
        console.log("SET MERGERESULT", mergeLocations);

        this.setState({
            mergeOpen: isOpen,
            mergeLocations: mergeLocations || this.state.mergeLocations
        })
    },

    render: function ()
    {
        //console.log("RENDER", this.props, this.state);

        var domainTypesCursor = this.state.dataGraph.getCursor(["domainTypes"]);
        var domainTypes = domainTypesCursor.value;

        var enumTypesCursor = this.state.dataGraph.getCursor(["enumTypes"]);
        var enumTypes = enumTypesCursor.value;

        var domainTypeNames = values(domainTypes).filter(filterSystem).map(mapToName);
        domainTypeNames.sort();

        var enumTypeNames = values(enumTypes).map(mapToName);
        enumTypeNames.sort();

        var height = this.state.height;
        var editing = this.state.editing;
        var selectedView = this.state.selectedView;
        var positionsLink = new ValueLink(this.state.domainEditorViews[selectedView].positions, (positions) =>
        {
            this.undo.newState({
                dataGraph: this.state.dataGraph,
                domainEditorViews: update(this.state.domainEditorViews, {
                    [selectedView] : {
                        positions: { $set: positions }
                    }
                }),
                selectedView: selectedView,
                editing: this.state.editing,
                editingType: this.state.editingType,
                editingProp: this.state.editingProp
            });
        });

        var selectedViewLink = new ValueLink(this.state.selectedView, (selectedView) => {

            // since changes to the view will push their current view selection as part of their undo state, we do not
            // need to create new undo states for simple view changes. The view will never the less follow the current
            // view selection
            this.setState({
                selectedView: selectedView
            });
        });

        var editingPropLink = new ValueLink(this.state.editingProp, (editingProp) => {
            this.setState({
                editingProp: editingProp
            });
        });

        //console.log("DOMAIN-LAYOUT", positionsLink.value);

        var domainViewNames = keys(this.state.domainEditorViews);
        domainViewNames.sort();

        var haveMultipleDomainViews = domainViewNames.length > 1;

        //console.log("domainViewNames", domainViewNames);

        var visible = this.state.domainEditorViews[selectedView].visible;


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
                                    value={ selectedViewLink }
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
                            editing={ this.state.editing }
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
                            editing={ this.state.editing }
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
                                positionsLink={ positionsLink }
                            />
                        </div>
                    </div>
                    <div className="col-md-9"
                         style={{ display: !editing && "none", zIndex: 10, position: "relative", background: "rgba(245,250,255,0.9)" }}>
                        {
                            this.state.editingType == "DomainType" &&
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
                                editingPropLink={ editingPropLink }
                            />
                        }
                        {
                            this.state.editingType == "EnumType" &&
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
                        locationsLink={ new ValueLink(this.state.mergeLocations, locations =>
                        {

                            var cursor = this.state.dataGraph.getCursor(["domainTypes"], false);

                            var newTypes = assign({}, cursor.get());

                            for (var i = 0; i < locations.length; i++)
                            {
                                var mergedType = locations[i].merged;
                                newTypes[mergedType.name] = mergedType;
                            }

                            this.undo.replaceState({
                                dataGraph: cursor.set(null, newTypes),
                                domainEditorViews: this.state.domainEditorViews,
                                selectedView: this.state.selectedView,
                                editing: this.state.editing,
                                editingType: this.state.editingType,
                                editingProp: this.state.editingProp
                            }, this.save);
                        })}
                    />
                }
            </div>
        );
    }
});


module.exports = DomainEditor;
