import sys from "../../sys"
import url from "url"
import { DEFAULT_NAME, DEFAULT_TYPE} from "../editor-defaults"

/**
 * Returns an editor location based on the given location and query, or the default position
 *
 * @param location      full location including search
 * @returns {*}
 */
export default function matchUrl(location)
{
    console.log("MATCH URL", location);

    const current = url.parse(location, true);

    const base = sys.contextPath + "/editor/" + sys.appName + "/";

    const path = current.path;
    const pos = path.indexOf(base);
    if (pos < 0 || pos + base.length === path.length)
    {
        return {
            type: DEFAULT_TYPE,
            name: DEFAULT_NAME,
            uri: location
        };
    }
    else
    {
        const parts = path.substr(pos + base.length).split("/");
        const detail = current.query.detail;
        const resultType = current.query.resultType;
        return {
            type: parts[0],
            name: parts.length > 1 ? parts[1] : null,
            resultType: resultType,
            detail: detail && JSON.stringify(detail),
            uri: location
        };
    }
}
