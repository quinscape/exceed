import uri from "./uri";
import sys from "../sys";

export default function (href, params)
{
    return uri( "/app/" + sys.appName + href, params, false);
}
