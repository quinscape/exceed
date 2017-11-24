Customizing Exceed
==================

## Models and Resources

All models and resources used by the system are contained in the [resource extensions](./model-reference.html) which get
stacked on top of each other to be composed into the final application model.

The models/resources in the "base" extension describe general aspects of any exceed application. You define your application
by adding one or more extensions to that definition. The extensions are sorted in increasing priority.

Consider this example of an *startup.json* startup configuration:

```json
{
    "env" : {
        "spring.profiles.active" : "development"
    },

    "apps" : [
        {
            "name" : "myapp",
            "extensions" : [
                "my-org-layout", 
                "my-app"
            ]
        }
    ]
}
  
```  

We have a single application called `myapp` and two extensions located in the extensions root folder with our servlet context
 ( */WEB-INF/extensions/* ). So the effective extensions are `base`, `my-org-layout` and `my-app`.
 
Everything in `my-org-layout` has a higher priority than base. So this extension can change the visual aspects of a standard
exceed application. It can provide its own bootstrap theme, and additional css and images etcpp in the below the */resources/*
location. This extension can be reused for all exceed applications in your organization.

Added on top of that is the `my-app` extension which defines the actual application with its business processes and RESTish
data-presentations etc. With a setup like here, `my-app` shouldn't contain any design elements. If you work with only 
one extension, you throw everything together, of course.

Higher priority resource files always overwrite resource files at the lower priority location. Higher priority models either
replace the lower priority models or are merged with them with [location dependent merging rules](./models-about.html#merging). 

## Base Template

We've seen how we're able to change the general [layout](./models-view.html#layout)
of an exceed application. This lets you control the complete HTML document body.

There exists what we call a "base template" which contains the outer HTML document
around the layout root.

```html 
<!DOCTYPE html>
<html lang="$LOCALE">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>$TITLE</title>

    <!-- Bootstrap -->
    <link id="application-styles" href="$CONTEXT_PATH/res/$APP_NAME/style/$APP_NAME.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="$CONTEXT_PATH/res/$APP_NAME/js/html5shiv.min.js"></script>
    <script src="$CONTEXT_PATH/res/$APP_NAME/js/respond.min.js"></script>
    <![endif]-->
    <script id="root-data" type="x-ceed/view-data">
        $VIEW_DATA
    </script>
$SCRIPTS
$HEAD
</head>
<body>
$CONTENT_BEFORE
<div id="root">
    $CONTENT
</div>
$CONTENT_AFTER
</body>
</html>
``` 
Our "template engine" replaces the contained placeholders with content:

 * `$APP_NAME` - Name of the current application
 * `$CONTENT_AFTER` - Additional HTML markup to insert after the main content
 * `$CONTENT_BEFORE` - Additional HTML markup to insert before the main content
 * `$CONTENT` - Main content area containing the layout root. This is currently still empty but will contain the server-side rendered React content.
 * `$CONTEXT_PATH` - Servlet context path of the application container
 * `$HEAD` - Additional HTML to insert in the &lt;head&gt;
 * `$LOCALE` - Current locale code
 * `$SCRIPTS` - HTML script tags to load the exceed Javascript files bundles 
 * `$TITLE` - Document title
 * `$VIEW_DATA` - dehydrated initial redux state 

It is our hope that changing the base template will be the absolute exception among the use cases of exceed. You can change the template
by providing an alternate resource at the location */resources/template/template.html* in an application extension. 

See you can configure the values for the additional placeholders in the default template or placeholders in your own template
under [component.json / componentConfig / baseTemplate ](./model-reference.html#xcd.config.BaseTemplateConfig). 

### Java-based config

As third step, we also provide a java-based based way of providing such a template placeholder. Any spring bean implementing
de.quinscape.exceed.runtime.template.TemplateVariablesProvider will be automatically used. 

```java
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.Map;

public interface TemplateVariablesProvider
{

    /**
     * Provides or overrides base template variables.
     *
     * @param runtimeContext    current runtime context
     * @param model             model map for the base template prefilled with the default values.
     */
    void provide(RuntimeContext runtimeContext, Map<String, Object> model);
}
```

### Server-Rendering

Server-rendering is implemented as template variables provider in de.quinscape.exceed.runtime.universal.ReactServerSideRenderer.
It is controlled by [component config setting "serverRendering"](./model-reference.html#xcd.config.ComponentConfig).

