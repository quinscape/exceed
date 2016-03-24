
var BabelConfig = {

    plugins: [

        "transform-es2015-arrow-functions",

        // JSX support
        "transform-react-jsx",
        "transform-react-display-name",

        // var obj = { [computed] : true }
        "transform-es2015-computed-properties",

        // const MY_CONSTANT = 1 ( const -> let -> var )
        "check-es2015-constants", "transform-es2015-block-scoping",

        // if (process.env.NODE_ENV !== "production")
        "transform-node-env-inline",

        // ES2015 module imports
        "transform-es2015-modules-commonjs",

        // auto "use strict";
        "transform-strict-mode",

        // ... Spread operator
        "transform-object-rest-spread"
    ],

    registerForTests: function ()
    {
        var usePoweredAsserts = !process.env.NO_POWER_ASSERT;

        var plugins = BabelConfig.plugins;

        if (usePoweredAsserts)
        {
            plugins = plugins.concat("babel-plugin-espower");
        }

        // do babeljs runtime registration via require hook with our settings.
        require("babel-core/register")({
            plugins: plugins
        });

    }
};

module.exports = BabelConfig;
