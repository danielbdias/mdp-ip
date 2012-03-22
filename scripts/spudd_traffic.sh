#!/bin/bash
#
# Script used to execute SPUDD-IP tests
#
# Author: Daniel Dias (dbdias at ime.usp.br)

###########################################################################
#Parameter definition
###########################################################################

ROOT_DIR=/home/daniel/workspaces/java/mdpip/ADD

#Algorithm name
ALGORITHM=spudd

#Directory where the problems are located
PROBLEM_DIR=$ROOT_DIR/problemsMDPIP

#Indicate which problem will be used in tests
PROBLEM_TYPE=traffic

#Indicate the directory where the results will be saved
RESULTS_DIR=$ROOT_DIR/reportsMDPIP/results/$ALGORITHM

#Define the range to be tested
INITIAL_PROBLEM=6
LAST_PROBLEM=6

#Test execution
NUMBER_OF_RUNS_PER_TEST=50

#Java execution parameters
BINARIES_DIR=$ROOT_DIR/bin
CLASSPATH=$ROOT_DIR:./libs/grappa1_4.jar
MAIN_CLASS=mdp.Principal

###########################################################################
#Script execution
###########################################################################
echo Starting tests...
echo 

cd $ROOT_DIR

current_problem_index=$INITIAL_PROBLEM

while test $current_problem_index -le $LAST_PROBLEM
do
	echo Executing problem $PROBLEM_TYPE"_"$current_problem_index

	current_problem=$PROBLEM_DIR/$PROBLEM_TYPE"_"$current_problem_index".net"
	current_report=$RESULTS_DIR/$PROBLEM_TYPE"_test.txt"
	current_log=$RESULTS_DIR/$PROBLEM_TYPE"_"$current_problem_index".log"

	time -f "Took %U seconds..." java -Xms200m -Xmx2048m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $NUMBER_OF_RUNS_PER_TEST 0 1 $current_report 0 Fact NOT FALSE Total 160 1000 1000 0.0 300 60 60 1 1 > $current_log

	echo Problem $PROBLEM_TYPE"_"$current_problem_index executed
	echo 

	current_problem_index=`expr $current_problem_index + 1`
done

echo End of tests

