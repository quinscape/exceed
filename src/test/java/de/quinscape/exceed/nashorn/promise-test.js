function fn(success,fail)
{
    return new Promise(function (resolve, reject)
    {
        throw new Error("Boom");

    }).then(success, fail);
}
