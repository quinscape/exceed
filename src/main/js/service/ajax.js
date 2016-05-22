var Promise = require("es6-promise-polyfill").Promise;
var assign = require("object.assign").getPolyfill();
var cando = require("../cando");

var Enum = require("./../util/enum");

var createXMLHTTPObject = require("./../util/xhr-factory");


var HttpMethod = new Enum({
    GET: true,
    POST: true
});

var DataType = new Enum({
    JSON: true,
    TEXT: true
});

var defaultOpts = {
    method: "get",
    dataType: DataType.JSON,
    headers: null
};

var contentType = {};
contentType[DataType.JSON] = "application/json";
contentType[DataType.TEXT] = "text/plain";


var csrfToken;
var csrfTokenHeader;

function logError(opts, err)
{
    var l = [];
    for (var name in err)
    {
        if (err.hasOwnProperty(name))
        {
            l.push(name + " = " + err[name] );
        }
    }
    console.error("AJAX ERROR", opts.url, " => ", l.join(", "), opts);
}

function serialize(data)
{
    var s = "";

    for (var k in data)
    {
        if (data.hasOwnProperty(k))
        {
            s += (s.length ? "&" : "") + encodeURIComponent(k) + "=" + encodeURIComponent(data[k]);
        }
    }
    return s;
}


/**
 * Requests an URL from the server via AJAX and returns a promise resolving to the returned data.
 *
 * @param opts              options
 * @param opts.url          [string} url
 * @param opts.method       HTTP method (default = "get")
 * @param opts.data         {string|object} POST data
 * @param opts.dataType     {string} expected data type of the resonse (default = "JSON")
 * @param opts.contentType  [string} POST content type. data should be a string in this case.
 *
 * @returns {Promise} resolving to the requested data in the type corresponding to the dataType option.
 */
module.exports = function(opts)
{
    opts = assign({}, defaultOpts, opts);

    if (!csrfToken)
    {
        csrfToken = document.querySelector("meta[name='token']").getAttribute("content");
        csrfTokenHeader = document.querySelector("meta[name='token-type']").getAttribute("content");
    }

    var promise = new Promise(function (resolve, reject)
    {
        if (typeof opts.url !== "string")
        {
            reject({
                status: 0,
                error: "No url",
                xhr: null
            });
        }

        var dataType = opts.dataType.toUpperCase();
        if (!DataType.isValid(dataType))
        {
            reject({
                status: 0,
                error: "Invalid dataType: " + opts.dataType,
                xhr: null
            });
            return;
        }

        if (!HttpMethod.isValid(opts.method.toUpperCase()))
        {
            reject({
                status: 0,
                error: "Invalid method: " + opts.method,
                xhr: null
            });
            return;
        }

        var method = opts.method.toUpperCase();

        if (method === "POST" && opts.data === undefined)
        {
            reject({
                status: 0,
                error: "No data for POST request",
                xhr: null
            });
            return;
        }

        var xhr = createXMLHTTPObject();
        if (!xhr)
        {
            reject({
                status: 0,
                error: "Could not create XMLHTTPObject",
                xhr: null
            });
            return;
        }

        //console.debug("AJAX", JSON.stringify(opts));

        xhr.open(method, opts.url, true);
        xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        xhr.setRequestHeader("Accept", contentType[opts.dataType]);

        var headers = opts.headers;
        if (headers)
        {
            for (var name in headers)
            {
                if (headers.hasOwnProperty(name))
                {
                    xhr.setRequestHeader(name, headers[name]);
                }
            }
        }

        var data = null;
        if (method === "POST")
        {
            xhr.setRequestHeader(csrfTokenHeader, csrfToken);
            xhr.setRequestHeader("Content-type", opts.contentType || "application/x-www-form-urlencoded");

            data = opts.data;

            if (typeof data !== "string")
            {
                if (opts.contentType === "application/json")
                {
                    data = JSON.stringify(data);
                }
                else
                {
                    data = serialize(data);
                }
            }
        }

        xhr.onreadystatechange = function ()
        {
            if (xhr.readyState != 4)
            {
                return;
            }
            if (xhr.status !== 200 && xhr.status !== 304)
            {

                if (xhr.status !== 0)
                {
                    reject({
                        status: xhr.status,
                        xhr: xhr,
                        error: "HTTP error " + xhr.status
                    });
                }
                return;
            }

            var responseText = xhr.responseText;

            if (dataType === DataType.JSON)
            {
                try
                {
                    var data = JSON.parse(responseText);
                    resolve( data );
                }
                catch(e)
                {
                    reject({
                        status: xhr.status,
                        xhr: xhr,
                        error: "Error parsing JSON: " + e.message
                    });
                }
            }
            else if (dataType === DataType.TEXT)
            {
                resolve(responseText);
            }
        };

        if (xhr.readyState == 4)
        {
            return;
        }
        xhr.send(data);
    });

    // install default error logger
    promise.then(null, function (err)
    {
        logError(opts, err);
    });

    return promise;
};
