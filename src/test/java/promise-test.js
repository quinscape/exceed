function fn(success,fail)
{
    return new Promise(function (resolve, reject)
    {
        resolve("Async value");
    }).then(success, fail);
}
