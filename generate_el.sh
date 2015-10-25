#!/bin/bash

if [ ! -d "$JAVACC_HOME" ]; then
	echo "JAVACC_HOME not set"
	exit 1;
fi


DIR=$(dirname $0)

$JAVACC_HOME/bin/jjtree -OUTPUT_DIRECTORY=$DIR/src/main/java/de/quinscape/exceed/expression $DIR/src/main/resources/Expression.jjt
$JAVACC_HOME/bin/javacc -OUTPUT_DIRECTORY=$DIR/src/main/java/de/quinscape/exceed/expression $DIR/src/main/java/de/quinscape/exceed/expression/Expression.jj

