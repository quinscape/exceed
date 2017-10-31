View Models
=============

View models decribe the views / pages of the application. 

~~ViewStructure~~

Views
-----

```json
 {
    "content": {
        "main": {
            "name": "Toolbar",
            "kids" : [
                {
                    "name": "Button",
                    "attrs": {
                        "icon": "comment",
                        "text": "Hello",
                        "action" : "{ alert('Hello from the Exceed View!') }"
                    }
                }
            ]
        }
    }
}
```

Here we see a minimal view model with a single button labeled "Hello", displaying a pretty alert saying "Hello from the Exceed View!".

The `content` property contains a map of named content areas. The default name for the view content is "main". Each
content area is a tree of component model instances.

Each component model has a `name` attribute identifying the component. 
We follow the React Js convention of component names starting with a lower-case letter being just a html tag and a 
component name starting with an upper-case letter being a full-fledged React component.

Here we have a *Toolbar* component which is mostly present to show how to nest components in one another and a *Button* 
component inside of it with button text and additional icon given as attributes.

View Expressions
----------------

Component attributes can either be a normal string value like for our `icon` and `text` attributes here, or they can
be expressions which are marked by being wrapped in pair of curly braces, vaguely reminiscent of JSX. The view expressions
are one use of the [exceed unified expression language](./models-unified.md). It gets transpiled into Javascript executable
on the client.

Component Packages
------------------

Exceed organizes its components in component packages which are folders with react components, component CSS files
and a [component package descriptor](./model-reference.html#xcd.component.ComponentPackageDescriptor).

The package descriptor contains a [component descriptor](./model-reference.html#xcd.component.ComponentDescriptor) 
for each component in the component package. The component descriptor describes react components and their props. 
It has rules about how to combine components.

Each component can have a server-side data provider implementation that automatically provides data for the component.
Components have provide values for their children with a context mechanism described in the component descriptor.

Layout
------
Exceed provides layout models as an easy way to separate content specific to a view from content reused across the 
application like navigation and page footer.

Here you see the base default layout:

```json
{
    "root": {
        "name" : "div",
        "kids": [
            {
                "name": "StandardNav"
            },
            {
                "name": "Content"
            },
            {
                "name": "Footer"
            }
        ]
    }
}
```
The layout model looks like a simplified view model, with the `root` property containing a single content tree.

This layout `root` gets imported into every view using it. 

The standard layout renders the `StandardNav` component which renders navigation entries based on the routing table.
It contains a special `Content` component which embeds the actual view content at its location. The `Content` component
accepts a `name` attribute which defaults to `"main"`. 

A layout can define multiple `Content` slots to be used by views. `"main"` is just the default name for the single content
slot. We recommend that you keep it that way and add others as needed.

 
Process Views vs Standalone Views
---------------------------------

As you can see above, views come in two flavors, as process views and standalone views.  

Feature               | Process View                                    | Standalone View 
--------------------- | ----------------------------------------------- | ------------------
**Location**          | Inside the view folder of a process folder      | In the */models/view* folder
**Navigation**        | View transitions orchestrated by parent process | unstructured navigation between mapped URIs
**Dominant Use-Case** | Application UI                                  | REST-ish data presentation

~~InfoReact~~
