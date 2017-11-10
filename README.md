# Exceed Application Engine #

Model-based application engine for editing and running applications defined in a JSON format.

[Project documentation](https://quinscape.github.io/exceed/index.html)

## Development Install

The project uses Java and maven for the java part and a nodejs build chain based on current nodejs/yarn/webpack 2.

## Requirements

You need to install the following before you can build / use this project as development install.

 * Java 8
 * Maven 3.3
 * Nodejs v8.7
 * Yarn 1.2.1
 
The maven build comes with maven-front-end-plugin, which in the end will automate the java build to include the js build. 
(WIP, current broken, must install yarn https://github.com/eirslett/frontend-maven-plugin#installing-node-and-yarn)

## Package.json scripts

The package.json comes with several "scripts" that can be run with 

```
yarn run <scriptName> 
```

Scripts:

```json
{
    "scripts": {
        "clean": "node tooling/build-clean.js",
        "docs": "babel-node tooling/build-docs.js",
        "dist": "cross-env NODE_ENV=production USE_EDITOR=false webpack -p",
        "dist-editor": "cross-env NODE_ENV=production webpack -p",
        "build": "webpack --debug --output-pathinfo",
        "watch": "webpack --debug --output-pathinfo -w",
        "test": "mocha --compilers js:babel-register -r tooling/webpack -R spec --recursive src/test/js"
    }
}
```

 * watch - Compiles the current sources once and then enters watch mode to keep compiling changes as they happen.
 * clean - Cleans all .js bundles and .map files from the build directory
 * dist - build a production version of the bundles without editor
 * dist - build a production version of the bundles including the editor
 * test - Run the mocha tests

## Writing your own exceed application

At this point it might be easiest to clone the [exceed-test](https://github.com/quinscape/exceed-test) repository and 
rename and customize it to your needs.
it sets up an actual spring-boot application with the configuration provided by the exceed main project.

The exceed-test project comes with a Intellij Project config loading the main project as module from the same project base directory.

## Development setup

You can further improve the development setup of working on both the base project and your application by providing the 
location of the base project as system property `exceed.library.source`. Exceed will then load the base extension not from
the exceed jar file, but from that location, making the base distribution hot-loadable within the application development.
(Make sure you run a `yarn run watch` if you edit the base js sources).

```bash
-Dexceed.library.source=<exceed-project-location>
```

We are still in the very early stages of development. In the end we're hoping to have better scaffolding tools and/or
editors.  
