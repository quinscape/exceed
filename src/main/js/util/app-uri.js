const uri = require("./uri");
const sys = require("../sys");

module.exports = function (href, params)
{
    return uri( "/app/" + sys.appName + href, params, false);
};
