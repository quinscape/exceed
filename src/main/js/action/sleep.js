var Promise = require("es6-promise-polyfill").Promise;

export default function (model)
{
    return new Promise(function (resolve, reject)
    {
//        console.log("SLEEP", model);

        window.setTimeout(resolve, (model.time || 3) * 1000);
    })
}
