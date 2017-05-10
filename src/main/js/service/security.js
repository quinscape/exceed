let login , roles;

module.exports = {
    init: function (_login, rolesArray)
    {
        if (!rolesArray || !rolesArray.length)
        {
            throw new Error("No roles");
        }

        login = _login;
        roles = {};

        for (let i = 0; i < rolesArray.length; i++)
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
