export default (function ()
{
    var parseXml;

    if (typeof window === "undefined")
    {
        return null;
    }

    if (typeof window.DOMParser !== "undefined")
    {
        parseXml = function (xmlStr)
        {
            return ( new window.DOMParser() ).parseFromString(xmlStr, "text/xml");
        };
    } else if (typeof window.ActiveXObject !== "undefined" &&
        new window.ActiveXObject("Microsoft.XMLDOM"))
    {
        parseXml = function (xmlStr)
        {
            var xmlDoc = new window.ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async = "false";
            xmlDoc.loadXML(xmlStr);
            return xmlDoc;
        };
    }
    return parseXml;
})();
