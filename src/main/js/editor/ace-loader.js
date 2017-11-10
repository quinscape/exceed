import hub from "../service/hub"
import store from "../service/store"

import { getConnectionId } from "../reducers/meta"

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

        const state = store.getState();

        return Promise.all([

                System.import(/* webpackChunkName: "ace" */ "./ace-api"),
                hub.init(getConnectionId(state))
            ])
            .then((data) =>
            {
                return theAPI = data[0];
            });
    }
};

module.exports = AceAPI;
