#!/bin/bash
#
# Script used to execute SPUDD-IP tests
#
# Author: Daniel Dias (dbdias at ime.usp.br)

###########################################################################
#Parameter definition
###########################################################################

#Commom parameter definition
. ./setup_vars.sh

###########################################################################
#Script execution
###########################################################################

echo Generating problem file...
echo

cd $ROOT_DIR
java -Xms200m -Xmx2560m -classpath $CLASSPATH -cp $BINARIES_DIR generator.NavigationGen $PROBLEM_DIR/navigation_1.net 4 4 0.9 0.01 0.1 0.9

echo File generated

