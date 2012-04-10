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
ALGORITHM=rtdpip

#Directory where the problems are located
PROBLEM_DIR=$ROOT_DIR/problemsMDPIP

#Indicate which problem will be used in tests
PROBLEM_TYPE=bi_ring_IP

#Indicate the directory where the results will be saved
REPORTS_DIR=$ROOT_DIR/reportsMDPIP
RESULTS_DIR=$REPORTS_DIR/results/$ALGORITHM

#Test execution
NUMBER_OF_RUNS_PER_TEST=50

#Max depth to explore with RTDP
MAX_DEPTH=1000 

#Number of trials to execute in RTDP
MAX_TRIALS=300

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
	local timeout=$3

	cd $ROOT_DIR

	echo Executing problem $PROBLEM_TYPE"_"$current_problem_index

	current_problem=$PROBLEM_DIR/$PROBLEM_TYPE"_"$current_problem_index"_Interval07.net"
	current_report=$RESULTS_DIR/$PROBLEM_TYPE"_test_"$test_type"_Interval07.txt"
	current_log=$RESULTS_DIR/$PROBLEM_TYPE"_"$current_problem_index"_Interval07.log"
	

	java -Xms200m -Xmx2048m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $NUMBER_OF_RUNS_PER_TEST 0 1 $current_report 0 Fact NOT TRUE RTDPIP $MAX_DEPTH $timeout 1000 0.0 $MAX_TRIALS 60 60 1 1 > $current_log

	current_value_function=$REPORTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_Interval07_RTDPIP.net"
	new_value_function=$RESULTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_Interval07_RTDPIP_"$test_type".net"

	mv $current_value_function $new_value_function

	echo Problem $PROBLEM_TYPE"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

echo Tests with 25% of time...

TEST_TYPE="onequarter"

execute_problem $TEST_TYPE 1 1
execute_problem $TEST_TYPE 2 1
execute_problem $TEST_TYPE 3 4
execute_problem $TEST_TYPE 4 11
execute_problem $TEST_TYPE 5 41
execute_problem $TEST_TYPE 6 124
#execute_problem $TEST_TYPE 7 312
#execute_problem $TEST_TYPE 8 965

echo Tests with 10% of time...

TEST_TYPE="tenpercent"

execute_problem $TEST_TYPE 1 1
execute_problem $TEST_TYPE 2 1
execute_problem $TEST_TYPE 3 1
execute_problem $TEST_TYPE 4 4
execute_problem $TEST_TYPE 5 16
execute_problem $TEST_TYPE 6 49
#execute_problem $TEST_TYPE 7 125
#execute_problem $TEST_TYPE 8 386

echo Tests with 5% of time...

TEST_TYPE="fivepercent"

execute_problem $TEST_TYPE 1 1
execute_problem $TEST_TYPE 2 1
execute_problem $TEST_TYPE 3 1
execute_problem $TEST_TYPE 4 2
execute_problem $TEST_TYPE 5 8
execute_problem $TEST_TYPE 6 25
#execute_problem $TEST_TYPE 7 63
#execute_problem $TEST_TYPE 8 183

echo End of tests
