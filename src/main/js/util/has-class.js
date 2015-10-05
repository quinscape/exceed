module.exports = function(classes,cls)
{
    var classesLen = classes.length, classLen = cls.length, pos;

    if (classes)
    {
        var start = 0;
        while ( (pos = classes.indexOf(cls, start)) >= 0)
        {
            if (( pos == 0 || classes.charAt( pos - 1) == " ") && ( pos + classLen == classesLen ||  classes.charAt( pos + classLen) == " "))
            {
                return true;
            }

            start = pos + classLen;
        }
    }
    return false;
};
