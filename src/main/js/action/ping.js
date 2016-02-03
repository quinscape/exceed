var exports = function (model, data)
{
    console.log("ping", model, data);
    return data;
};

exports.catch = function(e, actionModel, data)
{
    //console.log("client-side error handling", e, actionModel, data);
    return Promise.resolve({value: -100});
};

module.exports = exports;
