function failWithString()
{
    return Promise.reject("KAPOTT!");
}

function failWithError()
{
    return Promise.reject(new Error("Boom"));
}
