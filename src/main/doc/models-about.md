Models in Practice
==================

Let's get a bit more concrete and take a look at what models look like in detail.

An exceed applications consists of models and resources which are grouped in so called "extensions".
An extension is directory with a standardized layout.

Here we see an example application with one model of each type being present. The type of the JSON model is determined
by its relative location in the extension folder and the name of it is taken from the file name.  

Standardized Extension layout
-----------------------------

~~ExtensionStructure~~

Model Composition
-----------------

At startup, each extension in an exceed application is combined into the final application model. The base library of
exceed comes with the so called "base" extension which provides all the very basic atoms/definitions of an exceed application
and also common processes and defaults for common views, e.g. the login view.

You can imagine the composition process like copying the extensions into a target directory one after another. In the 
case of a collision one of three things happen:

 * The model is merged with the existing model at this location
 * The model overwrites the existing model  
 * An error occurs
 
