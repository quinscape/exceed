import NavigationHistory from "../util/navigation-history"
import { refetchView } from "../actions/view"
import { restoreAppState } from "../actions/reset"

import store from "../service/store"

const appNavHistory = new NavigationHistory({
    onRestore: state => {
        if (state)
        {
            store.dispatch(
                restoreAppState(state)
            );
        }
        else
        {
            store.dispatch(
                refetchView()
            ).then(() => {
                //console.log("REFETCH THEN");
                appNavHistory.update(store.getState());
            });
        }
    }
});
export default appNavHistory
