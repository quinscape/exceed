module.exports = function (s, suffix)
{
    return (
        suffix.length === 0 ||
        s.lastIndexOf(suffix) === s.length - suffix.length
    );
};
