import ajax from "../service/ajax";
import assign from "object-assign";

import appNavHistory from "../service/app-history";
import { findCurrentViewChanges } from "../reducers/inpage";

import { hydrateAppState } from "./reset";

const FETCH_DEFAULT_OPTS = {
    preview: null
};

export function fetch(state, opts)
{
    opts = assign({}, FETCH_DEFAULT_OPTS, opts);

    const ajaxOpts = {
        url: opts.url || window.location.href,
        contentType: "application/json",
        dataType: opts.dataType
    };

    const changes = findCurrentViewChanges(state);

    //console.log({changes});

    if (changes !== false)
    {
        ajaxOpts.method =  "POST";

        ajaxOpts.data = assign({}, opts.data);
        ajaxOpts.data.changedViewModels = changes;

        ajaxOpts.headers = assign({}, opts.headers, {
            "X-ceed-Preview" : "true"
        });
    }
    else
    {
        ajaxOpts.method =  opts.method;
        ajaxOpts.data = opts.data || {};
        ajaxOpts.headers = opts.headers;
    }

    return ajax(ajaxOpts);
}


export function refetchView()
{
    return (dispatch, getState) => {

        return fetch( getState(),{
                method: "GET",
                url: window.location.href,
                dataType: "XHR"
            })
            .then(function(xhr)
            {
                const json = xhr.responseText;

                //console.log("refetchView =>", json);

                const data = JSON.parse(json);
                if (!data.error)
                {
                    appNavHistory.update(data);

                    dispatch(
                        hydrateAppState(data)
                    );
                }
                return data;
            })
            .catch(function (err)
            {
                console.error(err);
            });

    }
}


const EXECUTE_TRANSITION_DEFAULTS = {
    progressive: true
};

export function executeTransition(opts)
{
    return (dispatch, getState) =>
    {
        opts = assign({}, EXECUTE_TRANSITION_DEFAULTS, opts);
        const url = opts.url;

        if (!url)
        {
            return Promise.reject(new Error("No url given"));
        }

        if (!opts.progressive)
        {
            throw new Error("Unsupported non-progressive transition")
        }

        return fetch( getState(),{
            method: "POST",
            url: url,
            data: opts.data,
            dataType: "XHR",
        })
        .then((xhr) =>
        {
            //console.log("URL", url, xhr.responseURL, xhr.responseText);
            const data = JSON.parse(xhr.responseText);
            const url = opts.urlProvider(xhr, data);

            dispatch(
                enterView(data, url)
            )
        })
        // .catch(function (err)
        // {
        //     console.error(err);
        // });
    };
}

function useXHRResponseUrl(xhr, data)
{
    return xhr.responseURL;
}

const NAVIGATE_TO_DEFAULTS = {
    progressive: true,
    urlProvider: useXHRResponseUrl
};

function redirectToUrl(url)
{
    window.location.href = url;

    return new Promise(function (resolve, reject)
    {
        //
        // This promise will never actually resolve. It is meant to convey to the caller that the full view change
        // operation is not done yet.
        //
        // When the page change happens, there will be a brand new js context.
        //
        // We add a long timeout to be sure the location change does not fail silently
        //
        window.setTimeout(
            () => reject(new Error("Non progressive page load timeout")),
            5000
        );
    });
}

/**
 * Internal thunk producer to enter a new view making sure to update the navigation history.
 *
 * @param data          new redux state from server
 * @param url           new url
 * @returns {function} action thunk
 */
function enterView(data, url)
{
    return (dispatch, getState) => {

        appNavHistory.update(getState());
        appNavHistory.newState(data, url);

        dispatch(
            hydrateAppState(data)
        );
    };
}

export function navigateView(opts)
{
    return (dispatch, getState) =>
    {
        opts = assign({}, NAVIGATE_TO_DEFAULTS, opts);

        const url = opts.url;

        if (!url)
        {
            return Promise.reject(new Error("No url given"));
        }

        if (!opts.progressive)
        {
            return redirectToUrl(opts.url);
        }

        return fetch( getState(),{
                method: "GET",
                url: url,
                dataType: "XHR",
            })
            .then((xhr) =>
            {
                //console.log("URL", url, xhr.responseURL, xhr.responseText);
                const data = JSON.parse(xhr.responseText);
                const url = opts.urlProvider(xhr, data);

                dispatch(
                    enterView( data,url)
                );
            })
            .catch(function (err)
            {
                console.error(err);
            });
    }
}

/**
 * Dev-Mode action creator to preview a new view model version for the current view.
 *
 */
export function previewView(model)
{
    return (dispatch, getState) =>
    {
        //console.log("PREVIEW", model);

        return fetch( getState(),{
            preview: model
        }).then(function (viewData)
        {
            //console.log("PREVIEW-DATA", viewData);

            // if it is not an error
            if (!viewData.error)
            {
                // update view
                dispatch(
                    hydrateAppState(viewData)
                );
            }

            // resolve to original view data
            return viewData;
        });
    }
}

function createErrorViewModel(err)
{
    return {
        content: {
            main: {
                name: "Error",
                    attrs: {
                    "error" : err
                }
            }
        }
    };
}

export function showError(err)
{
    hydrateAppState(
        createErrorViewModel(err)
    )
}

