"use strict";

var React = require("react/addons");
var Compositor = require("../compositor");

var ComplexComponent = {

    /**
     * Returns the current vars for this component.
     *
     * @returns {Object} current vars map.
     */
    getVars: function()
    {
        var injection = this.props._injection;
        return injection && injection.vars;
    },

    /**
     * Finds the compositor instance for the current component.
     *
     * Note that this is *not* the compositor component, but react-inject Compositor instance.
     *
     */
    findCompositor: function ()
    {
        return Compositor.find(React.findDOMNode(this));
    },

    /**
     * Updates the current vars for this component by merging the given new values
     * with the current vars.
     *
     * @param newVars
     */
    updateVars: function(newVars)
    {
        var compositor = this.findCompositor();
        var vars = React.addons.update(this.getVars(), {$merge: newVars});
        compositor.updateComponent(this, vars);
    },
    /**
     * Replaces all vars for this component with the new values.
     *
     * @param newVars
     */
    setVars: function(newVars)
    {
        var compositor = this.findCompositor();
        compositor.updateComponent(this, newVars);
    }

};

module.exports = ComplexComponent;
