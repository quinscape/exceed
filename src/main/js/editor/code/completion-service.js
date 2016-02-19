var sys = require("../../sys");
var hub = require("../../service/hub");

/**
 * In Code-Editor service for code completion and intentions. Handles server-side querying
 */
module.exports = {
    /**
     *
     * @param viewName      name of the view
     * @param path          array of indizes describing the model path within the view
     * @param index         current position in the index
     * @returns {Promise}
     */
    autoComplete: function(path, index)
    {
        return hub.request({
            type: "message.ComponentCompleteQuery",
            path: path,
            index: index
        });
    },
    /**
     *
     * @param viewModel     current editor view model
     * @param path          array of indizes describing the model path within the view
     * @param propName      prop name to complete
     * @returns {Promise}
     */
    autoCompleteProp: function(viewModel, path, propName)
    {
        return hub.request({
            type: "message.PropCompleteQuery",
            viewModel: viewModel,
            path: path,
            propName: propName
        });
    },
    autoCompletePropName: function(viewModel, path)
    {
        return hub.request({
            type: "message.PropNameCompleteQuery",
            viewModel: viewModel,
            path: path
        });
    }
};
