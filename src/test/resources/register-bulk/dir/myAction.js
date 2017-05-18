export default function (data, action)
{
    data.push("dir/myAction:" + action.param);
    return data;
};
