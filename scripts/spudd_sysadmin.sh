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
PROBLEM_TYPE=indep_ring_IP

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

	echo Problem $PROBLEM_TYPE"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

#echo Tests with 100% of time...

#TEST_TYPE="full"

#execute_problem $TEST_TYPE 1 75
#execute_problem $TEST_TYPE 2 75
#execute_problem $TEST_TYPE 3 75
#execute_problem $TEST_TYPE 4 75
#execute_problem $TEST_TYPE 5 75
#execute_problem $TEST_TYPE 6 75
#execute_problem $TEST_TYPE 7 75
#execute_problem $TEST_TYPE 8 75

echo Tests with 50% of time...

TEST_TYPE="onehalf"

execute_problem $TEST_TYPE 1 41
execute_problem $TEST_TYPE 2 41
execute_problem $TEST_TYPE 3 41
execute_problem $TEST_TYPE 4 41
execute_problem $TEST_TYPE 5 41
execute_problem $TEST_TYPE 6 41
execute_problem $TEST_TYPE 7 41
execute_problem $TEST_TYPE 8 41

echo Tests with 25% of time...

TEST_TYPE="onequarter"

execute_problem $TEST_TYPE 1 23
execute_problem $TEST_TYPE 2 23
execute_problem $TEST_TYPE 3 23
execute_problem $TEST_TYPE 4 23
execute_problem $TEST_TYPE 5 23
execute_problem $TEST_TYPE 6 23
execute_problem $TEST_TYPE 7 23
execute_problem $TEST_TYPE 8 23

echo Tests with 10% of time...

TEST_TYPE="tenpercent"

execute_problem $TEST_TYPE 1 11
execute_problem $TEST_TYPE 2 11
execute_problem $TEST_TYPE 3 11
execute_problem $TEST_TYPE 4 11
execute_problem $TEST_TYPE 5 11
execute_problem $TEST_TYPE 6 11
execute_problem $TEST_TYPE 7 11
execute_problem $TEST_TYPE 8 11

echo Tests with 5% of time...

TEST_TYPE="fivepercent"

execute_problem $TEST_TYPE 1 7
execute_problem $TEST_TYPE 2 7
execute_problem $TEST_TYPE 3 7
execute_problem $TEST_TYPE 4 7
execute_problem $TEST_TYPE 5 7
execute_problem $TEST_TYPE 6 7
execute_problem $TEST_TYPE 7 7
execute_problem $TEST_TYPE 8 7

echo End of tests

