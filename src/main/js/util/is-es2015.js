/**
 * Detects whether the exported value is a commonjs-style export or a new ES2015 export
 * @param module
 * @returns {boolean}
 */
export default function(module)
{
    // webpack defines a read-only, not enumerable property __esModule = true on ES2015 exported modules
    return module.__esModule === true;
}
