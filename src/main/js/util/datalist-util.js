module.exports = {
    ROOT_NAME: "[DataListRoot]",
    validatePath: function (path)
    {
        if (!path || typeof path !== "object" || !path.length)
        {
            throw new Error("Invalid path: " + JSON.stringify(path));
        }
    }
};
