export default function(pathA,pathB)
{
    if (pathA.length !== pathB.length)
    {
        return false;
    }

    for (let i = 0; i < pathA.length; i++)
    {
        if (pathA[i] !== pathB[i])
        {
            return false;
        }
    }
    return true;
}
