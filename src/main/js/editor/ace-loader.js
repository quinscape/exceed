
let theAPI;

const AceAPI = {
    get: function()
    {
        if (!theAPI)
        {
            throw new Error("ACE api not yet loaded");
        }

        return theAPI;
    },

    load: function ()
    {
        if (theAPI)
        {
            return Promise.resolve(theAPI);
        }

        return System.import(/* webpackChunkName: "ace" */ "./ace-api")
            .then((module) =>
            {
                return theAPI = module;
            });
    }
};

export default AceAPI;
