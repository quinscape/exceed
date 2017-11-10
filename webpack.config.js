const PRODUCTION = (process.env.NODE_ENV === "production");
const USE_EDITOR = (process.env.USE_EDITOR !== "false");

const path = require("path");
const fs = require("fs");
const os = require("os");
const webpack = require("webpack");

const WebpackStatsPlugin  = require("./tooling/webpack-stats-plugin");
const CleanObsoleteChunks = require('webpack-clean-obsolete-chunks');

const TrackUsagePlugin = require("babel-plugin-track-usage/webpack/track-usage-plugin");

const WatchTimePlugin = require("webpack-watch-time-plugin");

// load .babelrc config but change turn off import handling because webpack does that
// the .babelrc is used as-is for the tests (for which it is converted to commonjs)
const babelConfig =(
        function ()
        {
            const BabelConfig = JSON.parse(fs.readFileSync("./.babelrc", "UTF-8"));

            // turn off import/export handling for webpack
            BabelConfig.presets[0][1].modules = false;

            // filter out espower
            BabelConfig.plugins = BabelConfig.plugins.filter(
                function(s)
                {
                    return s !== "babel-plugin-espower"
                });

            // add our track-usage plugin
            BabelConfig.plugins.push([
                "track-usage",
                {
                    "sourceRoot": "src/main/js/",
                    "trackedFunctions": {
                        "i18n": {
                            "module": "./service/i18n",
                            "fn": "",
                            "varArgs": true
                        },
                        "scope": {
                            "module": "./service/process",
                            "fn": "scope"
                        }
                    },
                    "debug": false
                }
            ]);

            return BabelConfig;
        }
    )();


module.exports = [
    // CLIENT SIDE CONFIGURATION
    {
        devtool: "source-map",

        context: path.resolve(__dirname, "src/main/js"),
        entry: {
            app: "./app-main.js",
            editor: "./editor-main.js",
        },

        output: {
            path: path.join(__dirname, "src/main/base/resources/js"),
            publicPath: "/exceed/res/exceed/js",
            filename: "exceed-[name]-[chunkhash].js",
            chunkFilename: "exceed-[name]-[chunkhash].js",
            library: "Exceed",
            libraryTarget: "var",
            //pathinfo: true
        },

        plugins: [
            new webpack.optimize.CommonsChunkPlugin({
                name: "common"
            }),

            // Always expose NODE_ENV to webpack, you can now use `process.env.NODE_ENV`
            // inside your code for any environment checks; UglifyJS will automatically
            // drop any unreachable code.
            new webpack.DefinePlugin({
                "__SERVER": false,
                "__PROD": PRODUCTION,
                "__DEV": !PRODUCTION,
                "process.env": {
                    "USE_EDITOR": USE_EDITOR
                }
            }),
            new TrackUsagePlugin({
                output: path.join(__dirname, "src/main/base/resources/js/track-usage.json")
            }),
            WebpackStatsPlugin(path.join(__dirname, "src/main/base/resources/js/webpack-stats.json")),
            new CleanObsoleteChunks(),
            WatchTimePlugin
        ],

        module: {
            loaders: [
                {
                    test: /\.jsx?$/,
                    loader: "babel-loader",
                    exclude: [/node_modules/, /\.es5\.js/],
                    // XXX: jsonified to escape warning "DeprecationWarning: loaderUtils.parseQuery() received a non-string value"
                    //query: JSON.stringify(babelConfig)
                    query: babelConfig
                },
                {
                    test: /.json$/,
                    loader: "json-loader",
                    include: [
                        /node_modules/
                    ]
                },
                {
                    test: /.svg$/,
                    loader: "symbol-loader",
                    query: {
                        parseStyle: true
                    }
                }
            ]
        }
    },
    // SERVER SIDE CONFIGURATION
    {

        devtool: "source-map",

        context: path.resolve(__dirname, "src/main/js"),
        entry: {
            server: "./server-main.js",
            srvrender: "./server-render.js"
        },

        node: {
            console: false,
            global: false,
            process: false,
            __filename: "mock",
            __dirname: "mock",
            Buffer: false,
            setImmediate: false
        },

        output: {
            path: path.join(__dirname, "src/main/base/resources/js"),
            publicPath: "/exceed/res/exceed/js",
            filename: "exceed-[name].js",
            library: "Exceed",
            libraryTarget: "var",
            //pathinfo: true
        },

        plugins: [
            new webpack.optimize.CommonsChunkPlugin({
                name: "srvcommon"
            }),

            new webpack.DefinePlugin({
                "__PROD": !true,
                "__DEV": !false,
                "__SERVER": true,
                "process.env": {
                    "USE_EDITOR": false
                }
            }),
            new webpack.IgnorePlugin(/^(node-libs-browser|process|timers-browserify)/)
        ],

        module: {
            loaders: [
                {
                    test: /\.jsx?$/,
                    loader: "babel-loader",
                    exclude: [/node_modules/, /\.es5\.js/],
                    // XXX: jsonified to escape warning "DeprecationWarning: loaderUtils.parseQuery() received a non-string value"
                    //query: JSON.stringify(babelConfig)
                    query: babelConfig
                },
                {
                    test: /.json$/,
                    loader: "json-loader",
                    include: [
                        /node_modules/
                    ]
                }
            ]
        }
    }
];
