"use strict";

var Promise = require("es6-promise-polyfill").Promise;

var uuid = require("node-uuid");

module.exports = function (type)
{
    return Promise.resolve({
        "id" : uuid.v4(),
        "_type" : type
    });
};
