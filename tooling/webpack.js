(function (window)
{
// The only purpose of this is to be defined as a custom library in IntelliJ to let it know that the webpack build will
// be providing these so it stops warning about undefined variables.
    window.__DEV = true;
    window.__PROD = false;

    window.System = {
        /**
         * Import module
         * @param name
         *
         * @returns {Promise} promise
         */
        import: function (name)
        {

        }
    };

})(typeof window !== "undefined" ? window : global);

