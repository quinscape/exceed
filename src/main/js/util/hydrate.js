import assign from "object-assign";

import { convertComponents, convert } from "../util/convert";
import { newFormState, prepareViewModel } from "../form/form-state";

export default function (state, data)
{
    const newState = {
        component: assign(
            {},
            state ? state.component : null,
            convertComponents(data.component),
        ),
        scope: {
            dirty: {},
            graph: convert(data.scope.graph)
        },
        // merge meta
        meta: assign(
            {},
            state ? state.meta : null,
            data.meta
        ),
        inpage: state ? state.inpage : null
    };

    prepareViewModel(newState);

    newState.formState = newFormState(newState);
    return newState;
}
