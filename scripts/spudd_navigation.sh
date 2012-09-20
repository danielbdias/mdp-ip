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
PROBLEM_TYPE=navigation

#Indicate the directory where the results will be saved
REPORTS_DIR=$ROOT_DIR/reportsMDPIP
RESULTS_DIR=$REPORTS_DIR/results/$ALGORITHM

#Java execution parameters
BINARIES_DIR=$ROOT_DIR/bin
CLASSPATH=$ROOT_DIR:./libs/grappa1_4.jar
MAIN_CLASS=mdp.Principal

###########################################################################
#Script execution
###########################################################################

execute_problem(){
	local test_type=$1	
	local current_problem_index=$2
	local iterations=$3

	cd $ROOT_DIR

	echo Executing problem $PROBLEM_TYPE"_"$current_problem_index

	current_problem=$PROBLEM_DIR/$PROBLEM_TYPE"_"$current_problem_index".net"
	current_report=$RESULTS_DIR/$PROBLEM_TYPE"_test_"$test_type".txt"
	current_log=$RESULTS_DIR/$PROBLEM_TYPE"_"$current_problem_index".log"
	
	java -Xms200m -Xmx2048m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $iterations 0 1 $current_report 0 Fact NOT FALSE Total 160 1000 1000 0.0 300 60 60 1 1 > $current_log

	current_value_function=$REPORTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_0_0REGR.net"
	new_value_function=$RESULTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_0_0REGR_"$test_type".net"

	mv $current_value_function $new_value_function

	current_initial_state_value=$REPORTS_DIR/"initial_value"$PROBLEM_TYPE"_"$current_problem_index"_0_0REGR.txt"
	new_initial_state_value=$RESULTS_DIR/"initial_value"$PROBLEM_TYPE"_"$current_problem_index"_0_0REGR_"$test_type".txt"

	mv $current_initial_state_value $new_initial_state_value

	echo Problem $PROBLEM_TYPE"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

MAX_ITER=500

echo Tests with 100% of time...

TEST_TYPE="full"

#execute_problem $TEST_TYPE 6 $MAX_ITER
#execute_problem $TEST_TYPE 8 $MAX_ITER
#execute_problem $TEST_TYPE 9 $MAX_ITER
#execute_problem $TEST_TYPE 10 $MAX_ITER
#execute_problem $TEST_TYPE 12 $MAX_ITER
#execute_problem $TEST_TYPE 15 $MAX_ITER
execute_problem $TEST_TYPE 18 $MAX_ITER
execute_problem $TEST_TYPE 20 $MAX_ITER
execute_problem $TEST_TYPE 21 $MAX_ITER
execute_problem $TEST_TYPE 24 $MAX_ITER
#execute_problem $TEST_TYPE 25 $MAX_ITER
#execute_problem $TEST_TYPE 30 $MAX_ITER

echo End of tests

