var contextValue;

module.exports = {
    get: function()
    {
        return contextValue;
    },
    set: function(v)
    {
        contextValue = v;
    }
};
