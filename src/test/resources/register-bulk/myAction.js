export default function (data, action)
{
    data.push("myAction:" + action.param);
    return data;
};
