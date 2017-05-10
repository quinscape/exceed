import NavigationHistory from "../util/navigation-history"
import { resetEditorView } from "../actions/editor"

import store from "../service/store"

const editorNavHistory = new NavigationHistory({
    onRestore: state => {
        if (state)
        {
            store.dispatch(
                resetEditorView(state)
            );
        }
    }
});
export default editorNavHistory
