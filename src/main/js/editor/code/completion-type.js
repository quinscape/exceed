var Enum = require("../../util/enum");

/**
 *
 * @enum string
 */
var CompletionType = new Enum({
    COMPONENT : true,
    PROP : true,
    PROP_NAME : true
});

module.exports = CompletionType;
