JavaScript Tests
================

This folder contains the tests for our javascript modules. They will be automatically picked up by gulp but can also be 
directly by mocha or via IDE. In this case you want to require ( mocha command line -r ) the babel-test-setup.js from 
this folder to set up the same babel plugins than the normal build plus powered asserts if NO_POWER_ASSERT is not set.
  
Note that the gulp mocha setup transpiles the tests into a temporary folder first, so relative paths require special 
consideration. The temporary test build folder is the same folder depth as the normal js sources, so you can have to go 
back to the project root and then up to your src/main/js/ source. This way the relative paths work both for transpiled
and untranspiled tests in the IDE.


Example
-------

The test code in util/posInDocument.js needs to require its test target with

var posInDocument = require("../../../../src/main/js/util/posInDocument");


