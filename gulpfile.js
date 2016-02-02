"use strict";

var path = require("path");

var browserify = require("browserify");
var buffer = require("vinyl-buffer");
var chalk = require("chalk");
var extend = require("extend");
var gulpif = require("gulp-if");
var gulp = require("gulp");
var gutil = require("gulp-util");
var mocha = require("gulp-mocha");
var notifier = require("node-notifier");
var streamify = require("gulp-streamify");
var uglify = require("gulp-uglify");
var watchify = require("watchify");

var MAIN_FILE = "./src/main/js/main.js";

function report(msg, err)
{
    gutil.log(chalk.red("\n" + msg + ":\n") + err.stack);
    notifier.notify({
        title: "Error",
        message: err.message.substring(0,100)
    });
}

function createErrorReporter(msg)
{
    return report.bind(null, msg);
}


var sourcemaps = require("gulp-sourcemaps");
var source = require("vinyl-source-stream");
var babelify = require("babelify");

var PRODUCTION = process.env.NODE_ENV === "production";

var REACT_CATCH_ERRORS = !PRODUCTION && !process.env.NO_CATCH_ERRORS;

var BABEL_PLUGINS = [

    // JSX support
    "transform-react-jsx",
    "transform-react-display-name",

    // var obj = { [computed] : true }
    "transform-es2015-computed-properties",

    // const MY_CONSTANT = 1 ( const -> let -> var )
    "check-es2015-constants", "transform-es2015-block-scoping",

    // if (process.env.NODE_ENV !== "production")
    "transform-node-env-inline",

    "transform-es2015-arrow-functions",

    "transform-es2015-modules-commonjs",

    // auto "use strict";
    "transform-strict-mode"
];


gulp.task("build", function(cb) {
    bundle(false, cb);
});

gulp.task("watch", function(cb) {
    bundle(true, cb);
});

function bundle(watch, cb) {
    var bro;

    var compress = !process.env.NO_UGLIFY;

    if (watch) {
        bro = watchify(browserify(MAIN_FILE,
            // Assigning debug to have sourcemaps
            extend(watchify.args, {
                debug: true,
                fullPaths: true
            })));
        bro.on("update", function() {
            rebundle(bro);
        });
        bro.on("log", function() {
            gutil.log.apply(gutil, arguments);
        });
    } else {
        bro = browserify(MAIN_FILE, {
            debug: true,
            fullPaths: true
        });
    }

    if (PRODUCTION)
    {
        bro.exclude("./src/main/js/editor/InPageEditor.js");
    }

    //// TODO: expose services for project components / actions
    //bro.require("./src/service/services", {
    //    expose: "exceed-services"
    //});


    bro.transform(
        // Babel js transpiler
        babelify.configure({
            sourceRoot: path.resolve("./src/main/js"),

            // we exclude all files *.es5.js from babel. Mostly to escape a cyclic transformation issue in
            // react-transform-catch-errors. Might also be useful otherwise, hence the wildcard.
            ignore: [ "*.es5.js" ],
 	        plugins: REACT_CATCH_ERRORS ?

                // Conditionally add react-transfro
                BABEL_PLUGINS.concat(

                [["react-transform", {
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
                }]]
            ) : BABEL_PLUGINS
        }))
        .transform("bulkify")
        .transform("browserify-shim", {
            // we need browserify-shim to be global so that it will correctly replace our shimed scripts in the code
            // we include from libraries like react-bootstrap that have them as peer dependencies.
            global: true
        });

    function rebundle(bundler) {
        return bundler.bundle()
            .on("error", createErrorReporter("Error in browserify build"))
            .pipe(source("main.js"))
            .pipe(buffer())
            // loads map from browserify file
            .pipe(sourcemaps.init({
                loadMaps: true
            }))
            .pipe(gulpif(compress, streamify(uglify(
                /*{
                output: {
                    beautify: true
                },
                mangle: {
                    except: ['none']
                }
            }*/))))
            .pipe(sourcemaps.write(".")) // writes .map file
            .pipe(gulp.dest("./src/main/base/resources/js"));
    }

    return rebundle(bro);
}

gulp.task("test", function ()
{
    var usePoweredAsserts = !process.env.NO_POWER_ASSERT;

    // do babeljs runtime registration via require hook with our settings.
    require("babel-core/register")({
        plugins:
            ( usePoweredAsserts ?
                BABEL_PLUGINS.concat("babel-plugin-espower") :
                BABEL_PLUGINS
            )
    });

    gulp.src("./src/test/js/**/*.js")
        .pipe(gulp.dest("./target/js/tests"))
        .pipe(mocha({
            reporter: "spec"
        }));
});

gulp.task("default", ["build"]);
