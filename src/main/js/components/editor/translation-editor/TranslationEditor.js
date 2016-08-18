const React = require("react");

const update = require("react-addons-update");
const assign = require("object-assign");

const keys = require("../../../util/keys");
const values = require("../../../util/values");

const DataGraph = require("../../../util/data-graph");
const ValueLink = require("../../../util/value-link");
const Link = require("../../../ui/Link");
const Button = require("../../../ui/Button");
const FilterField = require("../../../ui/FilterField");
const PagingComponent = require("../../../ui/PagingComponent");
const i18n = require("../../../service/i18n");
const undoService = require("../../../service/undo");
const domainService = require("../../../service/domain");
const actionService = require("../../../service/action");

const Form = require("../../std/form/Form");
const Field = require("../../std/form/Field");
const SelectField = require("../../std/form/SelectField");

const TranslationEditorDetails = require("./TranslationEditorDetails");

const cx = require("classnames");

const MAX = 20;

const DEFAULT_SORT = "tag";

function mapTranslationEntryToName(entry)
{
    return entry.name;
}

function sortIn(translation, newTranslations, changedTranslations, removedTranslationIds, markedNew)
{
    if (markedNew[translation.id])
    {
        if (translation.translation)
        {
            markedNew[translation.id] = false;
            newTranslations.push(translation);
        }
    }
    else
    {
        if (translation.translation)
        {
            changedTranslations.push(translation);
        }
        else
        {
            removedTranslationIds.push(translation.id);
        }

    }
}

function getSortedTags(dataGraph, sort, reverse, offset, filter, refFilter)
{
    //console.log("getSortedTags", sort, reverse, offset);

    filter = filter.toLocaleLowerCase();

    var map = dataGraph.rootObject;

    let list = [];
    let marked = {};
    let hasRefs = {};
    for (let name in map)
    {
        if (map.hasOwnProperty(name))
        {
            let entry = map[name];

            let match = false;

            if (refFilter !== "-")
            {
                var refs = entry.references;
                if (refs)
                {
                    for (var i = 0; i < refs.length; i++)
                    {
                        if (refs[i].type === refFilter)
                        {
                            match = true;
                            break;
                        }
                    }
                }
            }
            else
            {
                if (filter === null)
                {
                    match = true;
                }
                else if  (name.toLocaleLowerCase().indexOf(filter) >= 0)
                {
                    match = true;
                }
                else
                {
                    let translations = entry.translations;
                    for (let locale in translations)
                    {
                        if (translations.hasOwnProperty(locale))
                        {
                            let translation = translations[locale];
                            if (translation.translation.toLocaleLowerCase().indexOf(filter) >= 0)
                            {
                                match = true;
                                break;
                            }
                        }
                    }

                    let localTranslations = entry.localTranslations;
                    for (let i = 0; i < localTranslations.length; i++)
                    {
                        let translation = localTranslations[i];

                        let processName = (translation.processName || "").toLowerCase();
                        let viewName = (translation.viewName || "").toLowerCase();

                        if (
                            translation.translation.toLocaleLowerCase().indexOf(filter) >= 0 ||
                            processName.indexOf(filter) >= 0 ||
                            viewName.indexOf(filter) >= 0
                        )
                        {
                            marked[name] = true;
                            match = true;
                            break;
                        }
                    }
                }
            }


            if (match)
            {
                list.push(entry);

                var refs = entry.references;
                hasRefs[name] = refs && refs.length > 0;
            }
        }
    }

    list.sort((a, b) =>
    {

        let result;
        if (sort === DEFAULT_SORT)
        {
            result = a.name.localeCompare(b.name);
        }
        else
        {
            let ta = a.translations[sort].translation;
            let tb = b.translations[sort].translation;
            result = ta.localeCompare(tb);
        }

        if (reverse)
        {
            result = -result;
        }

        return result;
    });

    var paged = list
        .slice(offset, offset + MAX);
    return {
        list: paged.map(mapTranslationEntryToName),
        marked: marked,
        rowCount: list.length,
        hasRefs: hasRefs
    };
}

function createNewTranslation(name)
{
    var newObject = domainService.create("AppTranslation");
    newObject.processName = null;
    newObject.viewName = null;
    newObject.locale = null;
    newObject.tag = name;
    newObject.translation = "";
    newObject.created = new Date().toISOString();

    return newObject;
}

function filterTranslationText(t)
{
    return !!t.translation;
}

function listTranslations(entry)
{
    var list = [];
    var translations = entry.translations;
    for (var name in translations)
    {
        if (translations.hasOwnProperty(name))
        {
            var translation = translations[name];
            if (translation.translation)
            {
                list.push(translation);
            }
        }
    }
    return list.concat(entry.localTranslations.filter(filterTranslationText));
}

/**
 * Creates an updateTranslation model for the current changes in the translation editor and updates the translations marked as being new
 *
 * @param currentState          current state map
 * @param prevState             previous state map
 * @param markedNew             {object} contains true for every id marked as new (object created but not backed by DB)
 * @param supportedLocales      array of locale names
 *
 * @returns {{action: string, newTranslations: Array, changedTranslations: Array, removedTranslationIds: Array}}
 */
function createModelAndUpdateMarked(currentState, prevState, markedNew, supportedLocales)
{
    var newTranslations = [];
    var changedTranslations = [];
    var removedTranslationIds = [];

    for (let name in currentState)
    {
        if (currentState.hasOwnProperty(name))
        {
            let prevEntry = prevState[name];
            let entry = currentState[name];

            if (prevEntry == null)
            {
                //console.log("NEW ENTRY", entry);
                newTranslations = newTranslations.concat(listTranslations(entry).filter(filterTranslationText));
            }
            else
            {
                let translations = entry.translations;
                let prevTranslations = prevEntry.translations;
                for (let i = 0; i < supportedLocales.length; i++)
                {
                    let locale = supportedLocales[i];
                    let prev = prevTranslations[locale];
                    let translation = translations[locale];
                    if (translation !== prev)
                    {
                        if (translation.translation !== prev.translation)
                        {
                            //console.log("CHANGED", translation)

                            sortIn(translation, newTranslations, changedTranslations, removedTranslationIds, markedNew)
                        }
                    }
                }

                let prevLocalTranslations = prevEntry.localTranslations;

                let localTranslations = entry.localTranslations;
                for (let i = 0; i < localTranslations.length; i++)
                {
                    let translation = localTranslations[i];
                    let id = translation.id;

                    let found = false;
                    for (let j = 0; j < prevLocalTranslations.length; j++)
                    {
                        let prev = prevLocalTranslations[j];

                        if (id === prev.id)
                        {
                            found = true;
                            if (translation !== prev)
                            {
                                if (
                                    translation.locale !== prev.locale ||
                                    translation.translation !== prev.translation ||
                                    translation.processName !== prev.processName ||
                                    translation.viewName !== prev.viewName
                                )
                                {
                                    //console.log("CHANGED LOCAL", translation);
                                    sortIn(translation, newTranslations, changedTranslations, removedTranslationIds, markedNew)
                                }
                            }
                            break;
                        }
                    }
                    if (!found)
                    {
                        //console.log("NEW LOCAL", translation);
                        if (translation.translation)
                        {
                            newTranslations.push(translation);
                        }
                    }
                }

                for (let i = 0; i < prevLocalTranslations.length; i++)
                {
                    let prev = prevLocalTranslations[i];

                    let found = false;
                    for (let j = 0; j < localTranslations.length; j++)
                    {
                        if (localTranslations[j].id === prev.id)
                        {
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        //console.log("REMOVED LOCAL", prev);
                        removedTranslationIds.push(prev.id);
                    }
                }
            }
        }
    }

    for (let name in prevState)
    {
        if (prevState.hasOwnProperty(name))
        {
            if (!currentState[name])
            {
                //console.log("REMOVED ENTRY", name);
                let prevEntry = prevState[name];
                removedTranslationIds = removedTranslationIds.concat(listTranslations(prevEntry).map(t => t.id));
            }
        }
    }

    const actionModel = {
        action: "updateTranslation",
        newTranslations: newTranslations,
        changedTranslations: changedTranslations,
        removedTranslationIds: removedTranslationIds
    };
    return actionModel;
}

function mapByValues(values)
{
    var map = {};
    for (var i = 0; i < values.length; i++)
    {
        map[values[i]] = true;

    }
    return map;
}
var TranslationEditor = React.createClass({

    getInitialState: function ()
    {

        var rawList = {
            type: "OBJECT",
            columns: {
                "*": {
                    type: "DomainType",
                    typeParam: "TranslationEntry"
                }
            },
            rootObject: this.props.initialState,
            count: this.props.rowCount
        };

        var dataGraph = new DataGraph(domainService.getDomainTypes(), rawList, this.onChange);

        var markedNew = mapByValues(this.props.markedNew);

        var initial = {
            dataGraph: dataGraph,
            dataView: getSortedTags(dataGraph, DEFAULT_SORT, false, 0, "", "-"),
            markedNew: markedNew,
            offset: 0,
            sort: DEFAULT_SORT,
            reverse: false,
            detail: null,
            filter: "",
            refFilter: "-"
        };

        if (!this.undo)
        {
            this.undo = undoService.create({
                dataGraph: dataGraph
            }, (state, done) => this.setState({
                dataGraph: state.dataGraph,
                dataView: getSortedTags(state.dataGraph, this.state.sort, this.state.reverse, this.state.offset, this.state.filter, this.state.refFilter)
            }, done));
        }
        return initial;
    },

    componentWillUnmount: function ()
    {
        this.undo.destroy();
    },

    onChange: function (newList, path)
    {
//        console.log("CHANGE", path, "ROOT:" , newList.rootObject);

        this.undo.newState({
            dataGraph: newList
        });
    },

    setFilter: function (newFilter)
    {
        newFilter = newFilter || "";

        var newRefFilter = newFilter ? "-" : this.state.refFilter;
        this.setState({
            dataView: getSortedTags(this.state.dataGraph, this.state.sort, this.state.reverse, this.state.offset, newFilter, newRefFilter),
            filter: newFilter,
            refFilter: newRefFilter
        });

    },
    setSort: function (newSort)
    {
        var sort = this.state.sort;
        var reverse = this.state.reverse;

        if (sort === newSort)
        {
            reverse = !reverse;
        }
        else
        {
            reverse = false;
        }

        this.setState({
            dataView: getSortedTags(this.state.dataGraph, newSort, reverse, this.state.offset, this.state.filter, this.state.refFilter),
            sort: newSort,
            reverse: reverse
        });
    },

    setOffset: function (newOffset)
    {
        this.setState({
            dataView: getSortedTags(this.state.dataGraph, this.state.sort, this.state.reverse, newOffset, this.state.filter, this.state.refFilter),
            offset: newOffset
        })
    },

    closeDetail: function ()
    {
        this.setState({
            detail: null
        });
    },

    editDetail: function (name)
    {
        this.setState({
            detail: name
        })
    },

    removeLocalTranslation: function (detail, localTranslationIndex)
    {
        var cursor = this.state.dataGraph.getCursor([detail, "localTranslations"]);
        cursor.splice(null, [[localTranslationIndex, 1]]);
    },

    addLocalTranslation: function (detail)
    {
        const translation = createNewTranslation(detail);
        translation.locale = this.props.supportedLocales[0];

        // We need to introduce two changes now, the new local transition and
        // marking its id as being new. We do both here locally without triggering
        // the dataGraph onChange to do both in undo step.

        const dataGraph = this.state.dataGraph;

        const newRoot = update(dataGraph.rootObject, {
            [detail]: {
                localTranslations: {$push: [translation]}
            }
        });

        const markedNew = assign({}, this.state.markedNew);
        markedNew[translation.id] = true;

        this.undo.newState({
            dataGraph: dataGraph.copy(newRoot)
        });

        this.setState({
            markedNew: markedNew
        });

    },

    setRule: function (detail, localTranslationIndex, value)
    {
//        console.log("SET RULE", detail, localTranslationIndex, "to", value);

        var processName = null;
        var viewName = null;

        if (value > -1)
        {
            processName = this.props.ruleLocations[value].processName;
            viewName = this.props.ruleLocations[value].viewName;
        }

        var cursor = this.state.dataGraph.getCursor([detail, "localTranslations", localTranslationIndex]);
        cursor.apply(null, (translation) =>
        {

            translation.processName = processName;
            translation.viewName = viewName;

            return translation;
        });
    },

    setLocale: function (detail, localTranslationIndex, locale)
    {

//        console.log("SET LOCALE", detail, localTranslationIndex, "to", locale);

        var cursor = this.state.dataGraph.getCursor([detail, "localTranslations", localTranslationIndex, "locale"]);
        cursor.set(null, locale);
    },

    removeEntry: function (detail, hasRefs)
    {

        var newRoot;
        const root = this.state.dataGraph.rootObject;

        var entry = root[detail];

        if (hasRefs)
        {
            newRoot = root;

            var translations = entry.translations;
            for (let locale in translations)
            {
                if (translations.hasOwnProperty(locale))
                {
                    newRoot = update(newRoot, {
                        [detail]: {
                            translations: {
                                [locale]: {
                                    translation: {$set: ""}
                                }
                            }
                        }
                    });
                }
            }

            var localTranslations = entry.localTranslations;
            if (localTranslations.length)
            {
                newRoot = update(newRoot, {
                    [detail]: {
                        localTranslations: {$set: []}
                    }
                });
            }
        }
        else
        {
            newRoot = {};
            for (let name in root)
            {
                if (root.hasOwnProperty(name) && name !== detail)
                {
                    newRoot[name] = root[name];
                }
            }
        }
        this.onChange(newRoot, [detail]);

    },
    save: function ()
    {

        if (this.undo.isSaved())
        {
            return;
        }

        const supportedLocales = this.props.supportedLocales;
        const markedNew = assign({}, this.state.markedNew);

        var savedState = this.undo.getSavedState().dataGraph.rootObject;
        var currentState = this.state.dataGraph.rootObject;

        var actionModel = createModelAndUpdateMarked(currentState, savedState, markedNew, supportedLocales);

//        console.log("UPDATE i18n", actionModel);

        actionService.execute(actionModel).then((result) =>
        {
            if (result.ok)
            {
                this.undo.markSaved();
                this.setState({
                    markedNew: markedNew
                });
            }

        })
            .catch(function (err)
            {
                console.error(err);
            });

        //console.log("CHANGES", changes);
    },

    newEntry: function ()
    {
        const name = prompt("Enter new translation tag name");
        if (name)
        {
            const dataGraph = this.state.dataGraph;
            if (dataGraph.rootObject[name])
            {
                alert("Translation tag '" + name + "' already exists");
                return;
            }

            // We need to introduce several changes now, a new translation for every supported localed and
            // marking those ids as being new. We do both here locally without triggering
            // the dataGraph onChange to do both in undo step.

            const supportedLocales = this.props.supportedLocales;

            const translations = {};

            const markedNew = assign({}, this.state.markedNew);
            for (var i = 0; i < supportedLocales.length; i++)
            {
                var locale = supportedLocales[i];
                var newTranslation = createNewTranslation(name);

                translations[locale] = newTranslation;
                markedNew[newTranslation.id] = true;
            }

            const newRoot = update(dataGraph.rootObject, {
                [name]: {
                    $set: {
                        name: name,
                        translations: translations
                    }
                }
            });

            this.undo.newState({
                dataGraph: dataGraph.copy(newRoot)

            });

            this.setState({
                markedNew: markedNew
            });

        }
    },

    setReferenceFilter: function (refFilter)
    {
        refFilter = refFilter || "-";

        var newFilter = refFilter !== "-" ? "" : this.state.filter;

        this.setState({
            dataView: getSortedTags(this.state.dataGraph, this.state.sort, this.state.reverse, this.state.offset, newFilter, refFilter),
            refFilter: refFilter,
            filter: newFilter
        });
    },

    getReferenceFilterHelpText: function (refFilter)
    {
        switch(refFilter)
        {
            case "MODULE":
                return i18n("RefFilter Help MODULE");
            case "DOMAIN":
                return i18n("RefFilter Help DOMAIN");
            case "LOCALE":
                return i18n("RefFilter Help LOCALE");
            case "VIEW":
                return i18n("RefFilter Help VIEW");
            case "QUALIFIER":
                return i18n("RefFilter Help QUALIFIER");
            default:
                return "";
        }

    },

    render: function ()
    {
//        console.log("STATE", this.state);

        const dataGraph = this.state.dataGraph;
        const dataView = this.state.dataView;
        const cursor = dataGraph.getCursor([0]);

        const supportedLocales = this.props.supportedLocales;

        const localeColWidth = ((70 / supportedLocales.length) | 0) + "%";

        const tagSortIcon = this.state.sort !== DEFAULT_SORT ? null : this.state.reverse ? "sort-by-attributes-alt" : "sort-by-attributes";

        var refFilterHelpText = this.getReferenceFilterHelpText(this.state.refFilter);

        var detailCursor = this.state.detail !== null && dataGraph.getCursor([this.state.detail]);
        return (
            <div className="translation-editor">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-md-12">
                            <h4>{ i18n('TranslationEditor') }</h4>
                            <div className="btn-toolbar" role="toolbar">
                                <Button
                                    icon="repeat"
                                    text={ i18n('Revert') }
                                    onClick={ this.undo.revert }
                                    disabled={ this.undo.isSaved() }
                                />
                                <Button
                                    icon="save"
                                    text={ i18n('Save') }
                                    onClick={ this.save }
                                    disabled={ this.undo.isSaved() }
                                />
                                <Button
                                    text={ i18n('Undo') }
                                    onClick={ this.undo.undo }
                                    disabled={ !this.undo.canUndo() }
                                />
                                <Button
                                    text={ i18n('Redo') }
                                    onClick={ this.undo.redo }
                                    disabled={ !this.undo.canRedo() }
                                />

                                <Button
                                    text={ i18n('New Tag') }
                                    onClick={ this.newEntry }
                                />
                            </div>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-md-6">

                            <FilterField
                                valueLink={ new ValueLink(this.state.filter, this.setFilter) }
                                placeholder={ i18n("Filter by translation, tag or local translation") }
                            />
                        </div>
                        <div className="col-md-6">
                            <SelectField
                                label={ i18n("Reference Type Filter") }
                                data={ [ "-", "MODULE", "DOMAIN", "LOCALE", "VIEW", "QUALIFIER"] }
                                value={ new ValueLink(this.state.refFilter, this.setReferenceFilter ) }
                                propertyType={ { type: "PlainText" } }
                            />
                        </div>
                    </div>
                    {
                        refFilterHelpText &&
                            <div className="row">
                                <div className="col-md-10 col-md-push-1">
                                    <h4><span className="glyphicon glyphicon-info-sign text-info"/> Filtered by Type: { this.state.refFilter } </h4>
                                    <p> { refFilterHelpText } </p>
                                </div>
                            </div>
                    }
                    <div className="row">
                        <div className="col-md-12">
                            <Form data={ cursor } horizontal={ false }>
                                <table className="table table-striped table-hover table-bordered table-condensed">
                                    <thead>
                                    <tr>
                                        <th width="15%">
                                            <Link text={ i18n('i18n Tag') } onClick={ (e) => this.setSort("tag") }/>
                                            <span className={ "glyphicon glyphicon-" + tagSortIcon }/>
                                        </th>
                                        {
                                            supportedLocales.map(locale =>
                                            {

                                                const icon = this.state.sort !== locale ? null : this.state.reverse ? "sort-by-attributes-alt" : "sort-by-attributes";

                                                return (
                                                    <th key={locale} width={ localeColWidth }>
                                                        <Link text={ i18n(locale) }
                                                              onClick={ (e) => this.setSort(locale) }/>
                                                        <span className={ "glyphicon glyphicon-" + icon }/>
                                                    </th>
                                                );
                                            })
                                        }
                                        <th width="15%">{ i18n('Action') }</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        dataView.list.map((name, idx) =>
                                            <tr key={ idx }>
                                                <td>

                                                    <div className="form-control-static">
                                                        { name }
                                                    </div>
                                                </td>
                                                {
                                                    supportedLocales.map(locale =>
                                                        <td key={ locale }>
                                                            <Field labelClass="sr-only"
                                                                   value={ dataGraph.getCursor([name, "translations", locale, "translation"])}/>
                                                        </td>
                                                    )
                                                }
                                                <td>
                                                    <Button className={ dataView.marked[name] && "bg-info"  } icon="edit"
                                                          text={ "Detail (" + dataGraph.rootObject[name].localTranslations.length + ")" }
                                                          onClick={ e => this.editDetail(name) }/>
                                                    <Button icon="erase" text={ "Remove" }
                                                          onClick={ e => this.removeEntry(name, dataView.hasRefs[name]) }/>
                                                </td>
                                            </tr>
                                        )
                                    }
                                    </tbody>
                                </table>
                                <PagingComponent
                                    offsetLink={ new ValueLink(this.state.offset, this.setOffset) }
                                    limit={ MAX }
                                    rowCount={ dataView.rowCount }
                                />
                            </Form>
                            <TranslationEditorDetails
                                detail={ this.state.detail }
                                ruleLocations={ this.props.ruleLocations }
                                supportedLocales={ supportedLocales }
                                detailCursor={ detailCursor }
                                onClose={ this.closeDetail }
                                removeLocalTranslation={ this.removeLocalTranslation }
                                addLocalTranslation={ this.addLocalTranslation }
                                setRule={ this.setRule }
                                setLocale={ this.setLocale }
                            />
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});

module.exports = TranslationEditor;

