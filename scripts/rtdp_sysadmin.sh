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
ALGORITHM=rtdp

#Directory where the problems are located
PROBLEM_DIR=$ROOT_DIR/problemsMDPIP

#Indicate which problem will be used in tests
PROBLEM_TYPE=uni_ring_IP

#Indicate the directory where the results will be saved
REPORTS_DIR=$ROOT_DIR/reportsMDPIP
RESULTS_DIR=$REPORTS_DIR/results/$ALGORITHM

#Test execution
NUMBER_OF_RUNS_PER_TEST=75

#Max depth to explore with RTDP
MAX_DEPTH=40 

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

	current_problem=$PROBLEM_DIR/$PROBLEM_TYPE"_"$current_problem_index".net"
	current_report=$RESULTS_DIR/$PROBLEM_TYPE"_test_"$test_type".txt"
	current_log=$RESULTS_DIR/$PROBLEM_TYPE"_"$current_problem_index".log"
	
	java -Xms200m -Xmx2048m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $NUMBER_OF_RUNS_PER_TEST 0 1 $current_report 0 Fact NOT TRUE RTDPIP $MAX_DEPTH $timeout 1000 0.0 $MAX_TRIALS 60 60 1 1 > $current_log

	current_value_function=$REPORTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_RTDPIP.net"
	new_value_function=$RESULTS_DIR/"value"$PROBLEM_TYPE"_"$current_problem_index"_RTDPIP_"$test_type".net"

	mv $current_value_function $new_value_function

	current_initial_state_value=$REPORTS_DIR/"initial_value"$PROBLEM_TYPE"_"$current_problem_index"_RTDPIP.txt"
	new_initial_state_value=$RESULTS_DIR/"initial_value"$PROBLEM_TYPE"_"$current_problem_index"_RTDPIP_"$test_type".txt"

	mv $current_initial_state_value $new_initial_state_value

	echo Problem $PROBLEM_TYPE"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

echo Tests with 200% of time...

TEST_TYPE="quadruple"

execute_problem $TEST_TYPE 1 4
execute_problem $TEST_TYPE 2 12
execute_problem $TEST_TYPE 3 48
execute_problem $TEST_TYPE 4 164
execute_problem $TEST_TYPE 5 544
execute_problem $TEST_TYPE 6 1636
execute_problem $TEST_TYPE 7 4320
execute_problem $TEST_TYPE 8 14792

#echo Tests with 100% of time...

#TEST_TYPE="full"

#execute_problem $TEST_TYPE 1 0
#execute_problem $TEST_TYPE 2 3
#execute_problem $TEST_TYPE 3 12
#execute_problem $TEST_TYPE 4 41
#execute_problem $TEST_TYPE 5 136
#execute_problem $TEST_TYPE 6 409
#execute_problem $TEST_TYPE 7 1080
#execute_problem $TEST_TYPE 8 3698

#echo Tests with 50% of time...

#TEST_TYPE="onehalf"

#execute_problem $TEST_TYPE 1 1
#execute_problem $TEST_TYPE 2 2
#execute_problem $TEST_TYPE 3 6
#execute_problem $TEST_TYPE 4 21
#execute_problem $TEST_TYPE 5 68
#execute_problem $TEST_TYPE 6 205
#execute_problem $TEST_TYPE 7 540
#execute_problem $TEST_TYPE 8 1849

#echo Tests with 25% of time...

#TEST_TYPE="onequarter"

#execute_problem $TEST_TYPE 1 1
#execute_problem $TEST_TYPE 2 1
#execute_problem $TEST_TYPE 3 3
#execute_problem $TEST_TYPE 4 11
#execute_problem $TEST_TYPE 5 34
#execute_problem $TEST_TYPE 6 103
#execute_problem $TEST_TYPE 7 270
#execute_problem $TEST_TYPE 8 925

#echo Tests with 10% of time...

#TEST_TYPE="tenpercent"

#execute_problem $TEST_TYPE 1 1
#execute_problem $TEST_TYPE 2 1
#execute_problem $TEST_TYPE 3 1
#execute_problem $TEST_TYPE 4 4
#execute_problem $TEST_TYPE 5 14
#execute_problem $TEST_TYPE 6 41
#execute_problem $TEST_TYPE 7 108
#execute_problem $TEST_TYPE 8 370

#echo Tests with 5% of time...

#TEST_TYPE="fivepercent"

#execute_problem $TEST_TYPE 1 1
#execute_problem $TEST_TYPE 2 1
#execute_problem $TEST_TYPE 3 1
#execute_problem $TEST_TYPE 4 2
#execute_problem $TEST_TYPE 5 7
#execute_problem $TEST_TYPE 6 21
#execute_problem $TEST_TYPE 7 54
#execute_problem $TEST_TYPE 8 185

echo End of tests
