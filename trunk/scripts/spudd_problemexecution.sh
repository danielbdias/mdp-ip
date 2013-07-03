#!/bin/bash
#
# Script used to execute SPUDD-IP tests
#
# Author: Daniel Dias (dbdias at ime.usp.br)

###########################################################################
#Parameter definition
###########################################################################

#Commom parameter definition
sh setup_vars.sh

#Algorithm name
ALGORITHM=spudd

#Indicate the directory where the results will be saved
RESULTS_DIR=$REPORTS_DIR/results/$ALGORITHM

#Java execution parameters
MAIN_CLASS=mdp.algorithms.SPUDDIP

###########################################################################
#Script execution
###########################################################################

execute_problem(){
	local problem_type=$1
	local current_problem_index=$2
	local test_type=$3	
	local max_iter=$4

	cd $ROOT_DIR

	echo Executing problem $problem_type"_"$current_problem_index

	current_problem=$PROBLEM_DIR/$problem_type"_"$current_problem_index".net"
	current_report=$RESULTS_DIR/"report_"$problem_type"_spudd.txt"
	current_log=$RESULTS_DIR/logs/$problem_type"_"$current_problem_index"_spudd.log"
	value_function=$RESULTS_DIR/value_functions/"valuefunction_"$problem_type"_"$current_problem_index"_spudd.net"
	initial_state_value=$RESULTS_DIR/initial_values/"initial_value"$problem_type"_"$current_problem_index"_spudd.txt"

	java -Xms200m -Xmx2560m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $current_report $max_iter $value_function $initial_state_value > $current_log

	echo Problem $problem_type"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

MAX_ITER=100

echo Tests with 100% of time...

TEST_TYPE="full"

PROBLEM_TYPE=uni_ring_IP

#execute_problem $PROBLEM_TYPE 1 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 2 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 3 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 4 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 5 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 6 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 7 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 8 $TEST_TYPE $MAX_ITER

PROBLEM_TYPE=traffic

#execute_problem $PROBLEM_TYPE 3 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 4 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 5 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 6 $TEST_TYPE $MAX_ITER

PROBLEM_TYPE=navigation

#execute_problem $PROBLEM_TYPE 6 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 8 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 9 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 10 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 12 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 15 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 18 $TEST_TYPE $MAX_ITER

PROBLEM_TYPE=triangle_tireworld

execute_problem $PROBLEM_TYPE 8 $TEST_TYPE $MAX_ITER
execute_problem $PROBLEM_TYPE 12 $TEST_TYPE $MAX_ITER
execute_problem $PROBLEM_TYPE 17 $TEST_TYPE $MAX_ITER
#execute_problem $PROBLEM_TYPE 23 $TEST_TYPE $MAX_ITER

echo End of tests

