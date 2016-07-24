var roles;

var login;

module.exports = {
    init: function (_login, rolesString)
    {
        if (!rolesString)
        {
            throw new Error("No roles");
        }

        login = _login;
        roles = {};

        var rolesArray = rolesString.replace(" ", "").split(",");
        for (var i = 0; i < rolesArray.length; i++)
        {
            roles[rolesArray[i]] = true;
        }
    },
    getLogin: function ()
    {
        return login;
    },
    hasRole: function (role)
    {
        return roles.hasOwnProperty(role) && !!roles[role];
    }
};
