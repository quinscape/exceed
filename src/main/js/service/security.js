var roles;

module.exports = {
    init: function (rolesString)
    {
        if (!rolesString)
        {
            throw new Error("No roles");
        }

        roles = {};

        var rolesArray = rolesString.replace(" ", "").split(",");
        for (var i = 0; i < rolesArray.length; i++)
        {
            roles[rolesArray[i]] = true;
        }
    },
    hasRole: function (role)
    {
        return roles.hasOwnProperty(role) && !!roles[role];
    }
};
