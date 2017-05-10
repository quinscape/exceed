const ace = require("brace");
if (typeof window !== "undefined")
{
//    window.ace = ace;
}
module.exports = ace;

require('./code/ace-mode-exceed');
require("brace/mode/json");
require("brace/ext/language_tools");
