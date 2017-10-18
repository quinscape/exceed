import React from "react"

import ModelLink from "./ModelLink"
import NamedGroup from "./NamedGroup"
import Memoizer from "../util/memoizer"
import sys from "../sys"
import uri from "../util/uri"
import debounce from "../util/debounce"

import i18n from "../service/i18n";
import Icon from "../ui/Icon";

import { searchModel } from "../actions/editor";
import {
    getDomainTypes,
    getEnumTypes,
    getFilter,
    getLayouts,
    getProcesses,
    getSearchResults,
    getViews
} from "../reducers/editor";

const RESULT_TYPE_LABELS = {
    NAME: i18n("SearchResult:Name"),
    REFERENCE: i18n("SearchResult:Reference"),
    TITLE: i18n("SearchResult:Title"),
    COMPONENT: i18n("SearchResult:Component"),
    ATTRIBUTE: i18n("SearchResult:Attribute"),
    STATE: i18n("SearchResult:State"),
    PROPERTY: i18n("SearchResult:Property"),
    ROUTE: i18n("SearchResult:route"),
    TRANSITION: i18n("SearchResult:Transition"),
    ACTION: i18n("SearchResult:Action"),
    EXPRESSION: i18n("SearchResult:Expression"),
    CONTEXT: i18n("SearchResult:Context"),
    SCOPE: i18n("SearchResult:Scope"),
    DESCRIPTION: i18n("SearchResult:Description"),
    DEFAULT_VALUE: i18n("SearchResult:Default Value"),
    COMPONENT_ID: i18n("SearchResult:Component Id"),
    QUERY: i18n("SearchResult:Query")
};

const MODEL_TYPE_TO_EDITOR_TYPE = {
    "xcd.ApplicationConfig": "config",
    "xcd.routing.RoutingTable": "routing",
    "xcd.domain.DomainType": "domainType",
    "xcd.domain.EnumType": "enumType",
    "xcd.process.Process": "process",
    "xcd.view.View": "view",
    "xcd.view.LayoutModel": "layout"
};

function getViewSelector(process)
{
    return views => {

        const filtered = {};

        for (let name in views)
        {
            if (views.hasOwnProperty(name))
            {
                const view = views[name];
                if (!!view.processName === process)
                {
                    filtered[name] = name;
                }
            }
        }

        return filtered;
    }
}

function getEditorType(modelType)
{
    const editorType = MODEL_TYPE_TO_EDITOR_TYPE[modelType];
    if (!editorType)
    {
        throw new Error("Unhandled model type: " + modelType);
    }
    return editorType;
}

const getStandaloneViews = Memoizer(getViewSelector(false));
const getProcessViews = Memoizer(getViewSelector(true));


function SearchResults(props)
{
    const { results, currentLocation } = props;

    return (
        !!results.length && (
            <div className="search-results">
                <h5>{ i18n("Search Results") }</h5>
                {
                    results.map((result, idx) =>
                        <ModelLink
                            key={ idx }
                            type={ getEditorType(result.type) }
                            name={ result.name }
                            params={ {
                                resultType: result.resultType,
                                detail: result.detail
                            }}
                            currentLocation={ currentLocation }
                        >
                            <span className="text-info pull-right">{ RESULT_TYPE_LABELS[result.resultType] }</span>
                            { result.name }
                        </ModelLink>
                    )
                }
            </div>
        )
    );
}

class ModelSelector extends React.Component
{
    onFilterChange = debounce( ev => {

        const searchTerm = this._searchField.value || "";
        this.search(searchTerm);

    }, 250);

    search(searchTerm)
    {
        this.props.store.dispatch(
            searchModel(searchTerm.toLowerCase())
        );
    }

    clearFilter = ev => {

        this._searchField.value = "";
        this.search("");
    };

    render()
    {
        const state = this.props.store.getState();
        const { currentLocation } = this.props;

        const currentFilter = getFilter(state);

        const searchResults = getSearchResults(state);

        return (
            <div>
                <div className="model-selector col-md-2" style={ { height: this.props.height, background: "#f0f0f0", borderRight: "1px solid #ccc" } }>
                    <h4>
                        <a
                            className="pull-right"
                            title={ i18n("Go To App {0}", sys.appName)}
                            href={ uri("/app/" + sys.appName) }
                        >
                            <Icon className="glyphicon-link"/>
                        </a>
                        { i18n("App {0}", sys.appName) }

                    </h4>
                    <form
                        className="form"
                        action={ uri("/editor/" + sys.appName + "/search/") }
                        method="GET"
                        onSubmit={ this.onFilterChange }>

                        <div className="form-group">
                            <label htmlFor="model-search" className="sr-only">{ i18n("Filter") }</label>
                            <div className="input-group">

                                <input
                                    ref={ elem => this._searchField = elem }
                                    id="model-search"
                                    type="text"
                                    className="form-control"
                                    defaultValue={ currentFilter }
                                    onChange={ this.onFilterChange }
                                    placeholder={ i18n("Partial model name") }
                                />
                                <span className="input-group-btn">
                        <button
                            className="btn btn-default"
                            disabled={ !currentFilter }
                            type="button"
                            onClick={ this.clearFilter }
                        >
                            <Icon className="glyphicon-erase"/>
                        </button>
                    </span>
                            </div>
                        </div>
                    </form>
                    <ModelLink type="config" currentLocation={ currentLocation } filter={ currentFilter }>
                        <h5>
                        <Icon className="glyphicon-cog text-info"/>
                        { " " + i18n("Config") }
                        </h5>
                    </ModelLink>
                    <ModelLink type="routing" currentLocation={ currentLocation } filter={ currentFilter }>
                        <h5>
                        <Icon className="glyphicon-road text-info"/>
                        { " " + i18n("Routing") }
                        </h5>
                    </ModelLink>
                    <ModelLink type="translation" currentLocation={ currentLocation } filter={ currentFilter }>
                        <h5>
                        <Icon className="glyphicon-globe text-info"/>
                        { " " + i18n("Translations") }
                        </h5>
                    </ModelLink>
                    <NamedGroup type="domainType" map={ getDomainTypes(state) } currentLocation={ currentLocation } filter={ currentFilter }>
                        { i18n("Domain Types")}
                    </NamedGroup>
                    <NamedGroup type="enumType" map={ getEnumTypes(state) } currentLocation={ currentLocation } filter={ currentFilter }>
                        { i18n("Enum Types")}
                    </NamedGroup>
                    <NamedGroup type="process" map={ getProcesses(state) } currentLocation={ currentLocation } filter={ currentFilter } subType="view" subItems={ getProcessViews(getViews(state)) } icon="chevron-down" subIcon="space">
                        { i18n("Processes")}
                    </NamedGroup>
                    <NamedGroup type="view" map={ getStandaloneViews(getViews(state)) } currentLocation={ currentLocation } filter={ currentFilter }>
                        { i18n("Views")}
                    </NamedGroup>
                    <NamedGroup type="layout" map={ getLayouts(state) } currentLocation={ currentLocation } filter={ currentFilter }>
                        { i18n("Layouts")}
                    </NamedGroup>
                    <SearchResults results={ searchResults } currentLocation={ currentLocation } />
                    <hr/>
                </div>
            </div>
        );
    }
}

export default ModelSelector
