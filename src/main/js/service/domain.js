"use strict";

var applicationDomain;

var uuid = require("node-uuid");

module.exports =
{
    create: function (type)
    {
        return {
            "id" : uuid.v4(),
            "_type" : type
        };
    },
    init: function (domainData)
    {
        //console.log("INIT DOMAIN", domainData);
        applicationDomain = domainData;
    },
    getEnum: function (name)
    {
        return applicationDomain.enumTypes[name];
    },
    getDomainType: function (name)
    {
        return applicationDomain.domainTypes[name];
    },
    getEnumTypes: function ()
    {
        return applicationDomain.enumTypes;
    },
    getDomainTypes: function ()
    {
        return applicationDomain.domainTypes;
    }
};
