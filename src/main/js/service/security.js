var roles = {};

module.exports = {
    init: function ()
    {
        var rolesArray = document.body.dataset.roles.replace(" ", "").split(",");
        for (var i = 0; i < rolesArray.length; i++)
        {
            roles[rolesArray[i]] = true;
        }
    },
    hasRole: function (role)
    {
        return !!roles[role];
    }
};
