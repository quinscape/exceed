
import store from "../service/store"
import { navigateView } from "../actions/view"

module.exports = function (model)
{
    store.dispatch(
        navigateView({
            url: model.url
        })
    );
};
