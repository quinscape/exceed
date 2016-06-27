"use strict";

var Promise = require("es6-promise-polyfill").Promise;

var applicationDomain;

var uuid = require("node-uuid");

module.exports =
{
    create: function (type)
    {
        return Promise.resolve({
            "id" : uuid.v4(),
            "_type" : type
        });
    },
    init: function (domainData)
    {
        //console.log("INIT DOMAIN", domainData);
        applicationDomain = domainData;
    },
    getEnum: function (name)
    {
        return applicationDomain.enums[name];
    },
    getDomainType: function (name)
    {
        return applicationDomain.domainTypes[name];
    },
    getDomainTypes: function ()
    {
        return applicationDomain.domainTypes;
    }
};
