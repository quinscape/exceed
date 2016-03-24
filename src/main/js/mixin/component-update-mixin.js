var viewService;

var ComponentUpdateMixin = {

    /**
     * Updates a component that receives query data to use a set of changed vars
     *
     * @param vars  new vars, will be merged with current.
     */
    updateComponent: function (vars)
    {
        if (!viewService)
        {
            viewService = require("../service/view");
        }
        viewService.updateComponent(this.props.id, vars);
    }
};

module.exports = ComponentUpdateMixin;
