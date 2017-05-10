export const EDITOR_CONFIG_UPDATE = "EDITOR_CONFIG_UPDATE";

export function updateConfig(cursor, value)
{
    return {
        type: EDITOR_CONFIG_UPDATE,
        graph: cursor.set(value)
    };
}
