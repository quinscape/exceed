//const ReactCSSTransitionGroup = require("react-addons-css-transition-group");

const domainService = require("../../service/domain");
const undoService = require("../../service/undo");
const update = require("react-addons-update");

const LinkedStateMixin = require("react-addons-linked-state-mixin");

const DomainEditorGraph = require("./DomainEditorGraph");

const keys = require("../../util/keys");
const values = require("../../util/values");

const DataGraph = require("../../util/data-graph");
const ValueLink = require("../../util/value-link");
const Event  = require("../../util/event");
const notify = require("../../util/notify");

const Link = require("../../ui/Link");
const Button = require("../../ui/Button");
const FilterField = require("../../ui/FilterField");

const i18n = require("../../service/i18n");

const DomainTypeForm = require("./DomainTypeForm");
const EnumTypeForm = require("./EnumTypeForm");

const NamedSelector = require("./NamedSelector");

const cx = require("classnames");

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

function windowHeight()
{
    return window ? window.innerHeight - 80 : 500;
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

        var defaultVisible = getDefaultVisible(domainTypes);

        if (!this.undo)
        {
            this.undo = undoService.create({
                dataGraph: dataGraph,
                visible: defaultVisible,
                domainLayout: false
            }, state =>
            {
                this.setState({
                    dataGraph: state.dataGraph,
                    visible: state.visible,
                    domainLayout: state.domainLayout
                });
            });
        }

        return {
            dataGraph: dataGraph,
            visible: defaultVisible,
            domainLayout: false,
            editing: null,
            editingType: null,
            height: windowHeight(),
            filter: ""
        };
    },

    onChange: function (newList, path)
    {
        this.undo.newState({
            dataGraph: newList,
            visible: this.state.visible,
            domainLayout: this.state.domainLayout
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
            height: windowHeight()
        })
    },

    toggleDomainType: function (domainType)
    {
        this.undo.newState({
            dataGraph: this.state.dataGraph,
            visible: update(this.state.visible, {
                [ domainType ]: {$apply: negate}
            }),
            domainLayout: this.state.domainLayout
        });

    },

    editType: function (name)
    {
        this.setState({
            editing: name,
            editingType: name ? "DomainType" : null
        });
    },

    editEnum: function (name)
    {
        this.setState({
            editing: name,
            editingType: name ? "EnumType" : null
        });
    },

    newType: function ()
    {
        var name = prompt(i18n("Enter New Domain Type Name"));
        if (name)
        {

            if (this.state.dataGraph.rootObject.domainTypes.hasOwnProperty(name))
            {
                notify(i18n("Domain Type '{0}' Name Already Exists", name));
                return;
            }

            var newTypeCursor = this.state.dataGraph.getCursor(["domainTypes", name], false);

            this.undo.newState({
                dataGraph: newTypeCursor.set(null, {
                    _type: "DomainType",
                    name: name,
                    properties: [{
                        name: "id",
                        type: "UUID"
                    }]
                }),
                visible: update(this.state.visible, {
                    [ name ]: {$set: true}
                }),
                domainLayout: this.state.domainLayout
            });

            this.editType(name);
            //console.log("NEW TYPE", name);
        }
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
                visible: this.state.visible,
                domainLayout: this.state.domainLayout
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
         console.log("SAVE", this.state.dataGraph)
    },

    render: function ()
    {
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
        var domainLayoutLink = new ValueLink(this.state.domainLayout, (domainLayout) =>
        {
//            console.log("NEW LAYOUT", domainLayout);

            this.undo.newState({
                dataGraph: this.state.dataGraph,
                visible: this.state.visible,
                domainLayout: domainLayout
            });
        });

        //console.log("DOMAIN-LAYOUT", domainLayoutLink.value);

        return (
            <div className="domain-editor container-fluid" style={{ height: height}}>
                <div className="row">
                    <div className="col-md-12">
                        <div className="btn-toolbar" role="toolbar">
                            <Button
                                icon="save"
                                className="btn-primary"
                                text={ i18n("Save") }
                                disabled={ this.undo.isSaved() }
                                onClick={ e => this.save }
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
                            visible={ this.state.visible }
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
                                visible={ this.state.visible }
                                onInteraction={ this.editType }
                                domainLayoutLink={ domainLayoutLink }
                            />
                        </div>
                    </div>
                    <div className="col-md-9"
                         style={{ display: !editing && "none", zIndex: 10, position: "relative", background: "rgba(255,255,255,0.9)" }}>
                        {
                            this.state.editingType == "DomainType" &&
                            <DomainTypeForm
                                key={ editing }
                                name={ editing }
                                edit={ this.editType }
                                domainTypes={ domainTypes }
                                domainTypeNames={ domainTypeNames }
                                storageOptions={ this.props.storageOptions }
                                enumTypes={ enumTypeNames }
                                cursor={  editing && domainTypesCursor.getCursor([editing]) }
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
            </div>
        );
    }
});


module.exports = DomainEditor;
