import processService from "../service/process"

export default function (name, context)
{
    let objects = null;
    if (context)
    {
        if (context.isProperty())
        {
            context = context.pop();
        }
        objects = context.extractObjects();
    }
    
    return processService.transition(
            name,
            objects
        )
        .catch(function(err)
            {
                console.error(err);
            }
        );

}
