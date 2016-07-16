const Mousetrap = require("mousetrap");

var _originalStopCallback = Mousetrap.prototype.stopCallback;

/**
 * Extend Mousetrap to make sure our undo keystrokes act globally, even if the user is in an input.
 *
 */

Mousetrap.prototype.stopCallback = function(e, element, combo, sequence) {
    var self = this;

    if (self.paused) {
        return true;
    }

    if (combo === "mod+z" || combo === "shift+mod+z")
    {
        return false;
    }

    return _originalStopCallback.call(self, e, element, combo);
};

Mousetrap.init();


module.exports = Mousetrap;
