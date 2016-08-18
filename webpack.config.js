const PRODUCTION = (process.env.NODE_ENV === "production");
const REACT_CATCH_ERRORS = !PRODUCTION && !process.env.NO_CATCH_ERRORS;

var path = require("path");
var os = require("os");
var webpack = require("webpack");

var TrackUsagePlugin = require("babel-plugin-track-usage/webpack/track-usage-plugin");

var babelConfig = {
    "plugins": [
        "check-es2015-constants",
        "transform-es2015-arrow-functions",
        "transform-react-jsx",
        "transform-react-display-name",
        "transform-es2015-computed-properties",
        "transform-es2015-destructuring",
        "transform-es2015-block-scoping",
        "transform-node-env-inline",
        "transform-es2015-modules-commonjs",
        "transform-strict-mode",
        "transform-object-rest-spread",
        [
            "track-usage",
            {
                "sourceRoot" : "src/main/js/",
                "trackedFunctions": {
                    "i18n": {
                        "module": "./service/i18n",
                        "fn": "",
                        "varArgs": true
                    },
                    "LIST": {
                        "module": "./service/process",
                        "fn": "list",
                        "varArgs": true
                    },
                    "OBJECT": {
                        "module": "./service/process",
                        "fn": "object",
                        "varArgs": true
                    },
                    "PROPERTY": {
                        "module": "./service/process",
                        "fn": "property",
                        "varArgs": true
                    }
                },
                "debug": false
            }
        ]
    ]
    // using "cacheDirectory" would unfortunately cause problems with the track-usages output
};


if (REACT_CATCH_ERRORS)
{
    babelConfig.plugins.push(
        ["react-transform", {
            "transforms": [{
                "transform": "react-transform-catch-errors",
                // now go the imports!
                "imports": [

                    // the first import is your React distribution
                    // (if you use React Native, pass "react-native" instead)
                    "react",

                    // the second import is the React component to render error
                    // (it can be a local path too, like "./src/ErrorReporter")

                    path.resolve("./src/main/js/ui/ErrorReport.es5.js")
                    // the third import is OPTIONAL!
                    // when specified, its export is used as options to the reporter.
                    // see specific reporter's docs for the options it needs.

                    // it will be imported from different files so it either has to be a Node module
                    // or a file that you configure with Webpack/Browserify/SystemJS to resolve correctly.
                    // for example, see https://github.com/gaearon/babel-plugin-react-transform/pull/28#issuecomment-144536185

                    // , "my-reporter-options"
                ]
            }]
        }]
    );
}


module.exports = {
    entry: "./src/main/js/main.js",
    output: {
        path: __dirname,
        filename: "src/main/base/resources/js/main.js",
        library: "Exceed",
        libraryTarget: "var"
    },
    plugins: [
        // Always expose NODE_ENV to webpack, you can now use `process.env.NODE_ENV`
        // inside your code for any environment checks; UglifyJS will automatically
        // drop any unreachable code.
        new webpack.DefinePlugin({
            "process.env": {
                "NODE_ENV": "'" + process.env.NODE_ENV + "'"
            }
        }),
        new TrackUsagePlugin({
            output: path.join(__dirname, "src/main/base/resources/js/track-usage.json")
        })
    ],
    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                loader: "babel-loader",
                exclude: [/node_modules/, /\.es5\.js/],
                query: babelConfig
            },
            {
                test: /.json$/,
                loader: "json-loader"
            }
        ]
    }
};
