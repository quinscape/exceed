export default function (promise, thenChain, elseChain) {

    return promise.then(
        function (result)
        {
            if (result)
            {
                return thenChain();
            }
            else
            {
                if (typeof elseChain === "function")
                {
                    return elseChain();
                }
            }
        });
}
