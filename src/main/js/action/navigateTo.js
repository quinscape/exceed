
import store from "../service/store"
import { navigateView } from "../actions/view"

import uri from "../util/uri"

export default function (url)
{
    store.dispatch(
        navigateView({
            url: uri(url)
        })
    );
}
