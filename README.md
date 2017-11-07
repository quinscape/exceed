# Exceed Application Engine #

Model-based application engine for editing and running applications defined in a JSON format.

[Project documentation](https://quinscape.github.io/exceed/index.html)

## Development Install

The project uses Java and maven for the java part and a nodejs build chain based on current nodejs/yarn/webpack 2.

## Requirements

You need to install the following before you can build / use this project as development install.

 * Java 8
 * Maven 3.3
 * Nodejs v6.10
 * Yarn 0.23.2
 
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
  …
  "scripts": {
    "clean": "rm src/main/base/resources/js/exceed-*.js src/main/base/resources/js/exceed-*.js.map",
    "dist": "NODE_ENV=production USE_EDITOR=false webpack -p",
    "dist-editor": "NODE_ENV=production webpack -p",
    "build": "webpack --debug --output-pathinfo",
    "watch": "webpack --debug --output-pathinfo -w",
    "test": "mocha --compilers js:babel-register -r tooling/webpack -R spec --recursive src/test/js"
  }
  …
}
```

 * watch - Compiles the current sources once and then enters watch mode to keep compiling changes as they happen.
 * clean - Cleans all .js bundles and .map files from the build directory
 * dist - build a production version of the bundles without editor
 * dist - build a production version of the bundles including the editor
 * test - Run the mocha tests
