
import store from "../service/store"
import { navigateView } from "../actions/view"

export default function (model)
{
    store.dispatch(
        navigateView({
            url: model.url
        })
    );
}
