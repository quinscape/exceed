const PROCESS_TYPE = "process.Process";

module.exports = {
    isProcess: function(model)
    {
        return model && model.type === PROCESS_TYPE
    },
    isView: function(model)
    {
        return model && model.type === "view.View";
    }
};
