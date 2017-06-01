import React from "react"

class ComponentClassIndex extends React.Component {

    render()
    {
        const { descriptors } = this.props;

        const classIndex = {};
        const classNames = [];

        for (let componentName in descriptors)
        {
            if (descriptors.hasOwnProperty(componentName))
            {
                const descriptor = descriptors[componentName];

                const classes = descriptor.classes;
                if (classes)
                {
                    for (let i = 0; i < classes.length; i++)
                    {
                        const cls = classes[i];

                        let array = classIndex[cls];
                        if (!array)
                        {
                            classNames.push(cls);

                            array = [];
                            classIndex[cls] = array;
                        }

                        array.push(componentName);
                    }
                }
            }
        }

        return (
            <div>
                <h1> ComponentClassIndex </h1>
                {
                    classNames.sort().map(cls =>
                        <div key={ cls } id={ "Class-" + cls }>
                            <h2>Components with "{ cls }"</h2>
                            {
                                classIndex[cls].map( (name, idx) =>
                                    <a key={name} href={ "component.html#Component-" + name }>
                                        { idx > 0 && ", " }
                                        { name }
                                    </a>
                                )
                            }
                        </div>
                    )
                }

            </div>
        )
    }
}

export default ComponentClassIndex;
