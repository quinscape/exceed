const Promise = require("es6-promise-polyfill").Promise;
const assign = require("object-assign");
const cando = require("../cando");

const Enum = require("./../util/enum");
const CSFR = require("./csfr");

var createXMLHTTPObject = require("./../util/xhr-factory");


var HttpMethod = new Enum({
    GET: true,
    POST: true
});

var DataType = new Enum({
    JSON: true,
    TEXT: true,
    XHR: true
});

var defaultOpts = {
    method: "get",
    dataType: DataType.JSON,
    headers: null
};

var contentType = {};
contentType[DataType.JSON] = "application/json";
contentType[DataType.XHR] = "application/json";
contentType[DataType.TEXT] = "text/plain";



function logError(opts, err)
{
    var l = [];
    for (var name in err)
    {
        if (err.hasOwnProperty(name))
        {
            l.push(name + " = " + JSON.stringify(err[name]) );
        }
    }
    console.error("AJAX ERROR", opts.url, " =>\n", l.join("\n"));
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


    var promise = new Promise(function (resolve, reject)
    {
        if (typeof opts.url !== "string")
        {
            reject({
                opts: opts,
                status: 0,
                error: "No url",
                xhr: null
            });
        }

        var dataType = opts.dataType.toUpperCase();
        if (!DataType.isValid(dataType))
        {
            reject({
                opts: opts,
                status: 0,
                error: "Invalid dataType: " + opts.dataType,
                xhr: null
            });
            return;
        }

        if (!HttpMethod.isValid(opts.method.toUpperCase()))
        {
            reject({
                opts: opts,
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
                opts: opts,
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
                opts: opts,
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
            xhr.setRequestHeader(CSFR.tokenHeader(), CSFR.token());
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
                        opts: opts,
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
                        opts: opts,
                        status: xhr.status,
                        xhr: xhr,
                        error: "Error parsing JSON: " + e.message + "\n" + responseText
                    });
                }
            }
            else if (dataType === DataType.TEXT)
            {
                resolve(responseText);
            }
            else if (dataType === DataType.XHR)
            {
                resolve(xhr);
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
