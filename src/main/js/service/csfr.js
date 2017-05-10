import store from "./store";

import { getTokenInfo } from "../reducers"

module.exports = {
    token: function ()
    {
        return getTokenInfo(store.getState()).value;
    },
    tokenHeader: function ()
    {
        return getTokenInfo(store.getState()).header;
    },
    tokenParam: function ()
    {

        return getTokenInfo(store.getState()).param;
    }
};
