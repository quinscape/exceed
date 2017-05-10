export const SCOPE_SET = "SCOPE_SET";

/**
 * Action creator to set a scoped value based on a given cursor and a value.
 *
 * @param cursor    {DataCursor} cursor
 * @param value     {*} value
 * @returns {object} action
 */
export function setScopeValue(cursor, value)
{
    const qualifier = cursor.getGraph().qualifier;
    if (qualifier !== "SCOPE")
    {
        // XXX: User registerable qualifier set handlers?
        throw new Error("Unexpected graph qualifier: '" + qualifier + "', 'SCOPE' expected");
    }

    return {
        type: SCOPE_SET,
        path: cursor.getPath(),
        graph: cursor.set(value)
    };
}
