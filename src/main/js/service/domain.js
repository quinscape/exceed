"use strict";

var Promise = require("es6-promise-polyfill").Promise;

var enumsMap;

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
    init: function (enums)
    {
        //console.log("INIT ENUMS", enums)
        enumsMap = enums;
    },
    getEnum: function (name)
    {
        return enumsMap[name];
    }

};
