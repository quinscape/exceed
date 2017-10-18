import { Promise } from "es6-promise-polyfill"

/**
 * Conditional action chaining.
 *
 * @param cond
 * @param trueAction
 * @param falseAction
 * @returns {*}
 */
export default function (cond)
{
    return cond ? Promise.resolve(true) : Promise.reject(false);
}
