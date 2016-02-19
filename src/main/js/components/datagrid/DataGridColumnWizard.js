
module.exports = function(editor, completion)
{
    editor.getSession().suppressPreviewOnce = true;

    var pos = editor.getCursorPosition();
    editor.insert("<DataGrid.Column name=\"\" />\r\n");
    editor.moveCursorTo(pos.row, pos.column + 23);


    window.setTimeout(function ()
    {
        editor.execCommand("startAutocomplete");
    }, 10);
};
