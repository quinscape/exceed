const ValueLink = require("./value-link");

/**
 * Replacement for the LinkedStateMixin.
 *
 * @param component     Component state source
 * @param name          state prop to bind
 * @returns {ValueLink} value link
 */

export default function linkState(component, name)
{
    return new ValueLink(component.state[name], (newValue) => {
        component.setState({
            [name] : newValue
        })
    });
}
