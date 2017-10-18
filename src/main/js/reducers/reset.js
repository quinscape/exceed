import { APP_RESET } from "../actions"

export default function(state, action)
{
    // double check type
    if (action.type === APP_RESET)
    {
        // merge update with current state
        return action.newState;
    }
    return state;
}
