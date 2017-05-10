import NavigationHistory from "../util/navigation-history"
import { refetchView, resetAppState } from "../actions/view"

import store from "../service/store"

const appNavHistory = new NavigationHistory({
    onRestore: state => {
        if (state)
        {
            store.dispatch(
                resetAppState(state)
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
