
import Dialog from "../util/dialog"
import DataCursor from "../domain/cursor"

export default function (text)
{
    if (text instanceof DataCursor)
    {
        text = text.get();
    }

    if (typeof text === "object")
    {
        text = JSON.stringify(text, null, 4);
    }

    return Dialog.alert(String(text));
}
