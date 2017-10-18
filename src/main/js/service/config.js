import store from "../service/store"
import { getComponentConfig } from "../reducers/meta"

/**
 * Returns the given config value for the given valid configuration key.
 *
 * Valid configuration keys are defined in de.quinscape.exceed.model.config.ComponentConfig on the Java side.
 *
 * @param name      configuration name
 * @returns {*} value
 */
export default function (name)
{
    return getComponentConfig(store.getState(), name);
}

