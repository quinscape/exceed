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

At startup, all extensions of the exceed application are combined into the final application model. The base library of
exceed comes with the so called "base" extension which provides all the very basic atoms/definitions of an exceed application
and also common processes and defaults for common views, e.g. the login view.

You can imagine the composition process as copying the extension directories into a target directory one after another. 
In the case of a collision one of three things happen:

 * The model is merged with the existing model at this location
 * The model overwrites the existing model  
 * An error occurs
 
 
Merging
-------
 
The merge process of a model is done by a recursive reducer function that is  controlled by @MergeStrategy annotations 
on the model classes. 

All JSON model files are first read into Java instances, which are then combined if they occupy the same relative location
within the extension. There can be many models at one location all of which are reduced to a single model one after 
another. In general there are two merge modes: 

 * REPLACE just simply replaces the previous model. If set on a property, it replaces that property. Primitive types and 
   app resource instances are always merged with REPLACE. 
 * DEEP does a recursive merge with the previous model. Each property is merged after with the corresponding property on the
   other model. Property methods and embedded types can be annotated with another @MergeStrategy annotation to change the merge mode.
   Since REPLACE never recurses into the properties this only works to change DEEP into REPLACE for selected properties or embedded models.    
   

