/**
 * Helper module for proxyquire based js testing. Clients go
 *
 * const INTERNAL_BUFFER = require("./interceptor")();
 *
 * and tests can proxiquire exactly that require to get access to internal buffers for testing while making it impossible
 * to accidentally mess with those structures without proxyquire.
 *
 * @param [data]    {*} if given truthy, this is the value that will be returned again, otherwise it's a new array. The value
 *                      can also be used to qualify multiple interceptor calls in a single module
 * @returns passed through data or a new object.
 */
export default function (data)
{
    return data || {}
}

