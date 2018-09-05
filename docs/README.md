# Project Documentation 

This directory contains the generated documenation for exceed.

The documentation is published from this folder with gh-pages.
 
It is automatically generated from two sources:

 * JSON data generated by a java class
 * Markup documents under src/main/doc
 
 
### Updating the docs
 
If the model classes have changed [src/main/base/resources/js/model-docs.json](https://github.com/quinscape/exceed/blob/master/src/main/base/resources/js/model-docs.json)
needs to be regenerated by invoking a helper class.  
 
```bash
mvn exec:java -Dexec.mainClass=de.quinscape.exceed.tooling.GenerateModelDocs -Dexec.args="src/main/base/resources/js/model-docs.json"
```
 
In Intellij there's a shared configuration named "GenerateModelDocs" that does the same. The class is also integrated into the 
full maven build
   
If you only changed the markdown documents you can just run

 
```bash
yarn run docs
```

This is also integrated into the full maven build. Both commands should
be entered from the base directory of the project.

