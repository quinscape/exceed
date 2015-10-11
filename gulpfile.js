"use strict";

var path = require("path");

var browserify = require("browserify");
var buffer = require("vinyl-buffer");
var chalk = require("chalk");
var espower = require("gulp-espower");
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

var BABEL_JS_WHITELIST = [

    // SUPPORTED TRANSFORMS / JS EXTENSIONS:

    // JSX support
    "react", "react.displayName",

    // var obj = { [computed] : true }
    "es6.properties.computed",

    // const MY_CONSTANT = 1 ( const -> let -> var )
    "es6.constants", "es6.blockScoping",

    // if (process.env.NODE_ENV !== "production")
    "utility.inlineEnvironmentVariables",

    // auto "use strict";
    "strict"
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
                debug: true
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

    bro.transform(
        // Babel js transpiler
        babelify.configure({
            externalHelpers: true,
            sourceRoot: path.resolve("./src/main/js"),
            whitelist: BABEL_JS_WHITELIST
        }))
        .transform("bulkify")
        .transform("browserify-shim");

    function rebundle(bundler) {
        return bundler.bundle()
            .on("error", createErrorReporter("Error in browserify build"))
            .pipe(source("main.js"))
            .pipe(buffer())
            // loads map from browserify file
            .pipe(sourcemaps.init({
                loadMaps: true
            }))
            .pipe(gulpif(compress, streamify(uglify())))
            .pipe(sourcemaps.write(".")) // writes .map file
            .pipe(gulp.dest("./src/main/base/resources/js"));
    }

    return rebundle(bro);
}

gulp.task("test", function ()
{
    // do babeljs runtime registration via require hook with our settings.
    require("babel-core/register")({
        whitelist: BABEL_JS_WHITELIST
    });

    gulp.src("./src/test/js/**/*.js")
        .pipe(espower())
        .pipe(gulp.dest("./target/js/tests"))
        .pipe(mocha({
            reporter: "spec"
        }));
});

gulp.task("default", ["build"]);
