#!/bin/bash
#
# Script used to execute RTDP-IP tests
#
# Author: Daniel Dias (dbdias at ime.usp.br)

###########################################################################
#Parameter definition
###########################################################################

#Commom parameter definition
sh setup_vars.sh

#Algorithm name
ALGORITHM=rtdp

#Indicate the directory where the results will be saved
RESULTS_DIR=$REPORTS_DIR/results/$ALGORITHM/min

#Max depth to explore with RTDP
MAX_DEPTH=100

#State Selection Nature (1=min, 4=rnd)
STATE_SELECTION_NATURE=1

#Java execution parameters
MAIN_CLASS=mdp.algorithms.RTDPIP

###########################################################################
#Script execution
###########################################################################

execute_problem(){
	local problem_type=$1
	local current_problem_index=$2
	local test_type=$3	
	local timeout=$4

	cd $ROOT_DIR

	echo Executing problem $problem_type"_"$current_problem_index

	current_problem=$PROBLEM_DIR/$problem_type"_"$current_problem_index".net"
	current_report=$RESULTS_DIR/"report_"$problem_type"_rtdp_min.txt"
	current_log=$RESULTS_DIR/logs/$problem_type"_"$current_problem_index"_rtdp_min.log"
	value_function=$RESULTS_DIR/value_functions/"valuefunction_"$problem_type"_"$current_problem_index"_rtdp_min.net"
	initial_state_value=$RESULTS_DIR/initial_values/"initial_value"$problem_type"_"$current_problem_index"_rtdp_min.txt"

	java -Xms200m -Xmx2560m -classpath $CLASSPATH -cp $BINARIES_DIR $MAIN_CLASS $current_problem $current_report $MAX_DEPTH $timeout $STATE_SELECTION_NATURE $value_function $initial_state_value > $current_log

	echo Problem $problem_type"_"$current_problem_index executed
	echo 
}

echo Starting tests...
echo 

PROBLEM_TYPE="triangle_tireworld"
TEST_TYPE="convergence"

execute_problem $PROBLEM_TYPE 8 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 12 $TEST_TYPE 20
execute_problem $PROBLEM_TYPE 17 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 23 $TEST_TYPE 100
execute_problem $PROBLEM_TYPE 30 $TEST_TYPE 200
execute_problem $PROBLEM_TYPE 38 $TEST_TYPE 400
execute_problem $PROBLEM_TYPE 47 $TEST_TYPE 800
execute_problem $PROBLEM_TYPE 57 $TEST_TYPE 1200
#execute_problem $PROBLEM_TYPE 68 $TEST_TYPE 1200
#execute_problem $PROBLEM_TYPE 80 $TEST_TYPE 1800
#execute_problem $PROBLEM_TYPE 93 $TEST_TYPE 2400
#execute_problem $PROBLEM_TYPE 107 $TEST_TYPE 2400
#execute_problem $PROBLEM_TYPE 122 $TEST_TYPE 3600

PROBLEM_TYPE="navigation"
TEST_TYPE="convergence"

execute_problem $PROBLEM_TYPE 6 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 8 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 9 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 10 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 12 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 15 $TEST_TYPE 20
execute_problem $PROBLEM_TYPE 18 $TEST_TYPE 20
execute_problem $PROBLEM_TYPE 20 $TEST_TYPE 20
execute_problem $PROBLEM_TYPE 21 $TEST_TYPE 20
execute_problem $PROBLEM_TYPE 24 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 25 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 28 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 30 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 49 $TEST_TYPE 64
execute_problem $PROBLEM_TYPE 64 $TEST_TYPE 96
execute_problem $PROBLEM_TYPE 81 $TEST_TYPE 200
execute_problem $PROBLEM_TYPE 100 $TEST_TYPE 400
execute_problem $PROBLEM_TYPE 121 $TEST_TYPE 1200

PROBLEM_TYPE="uni_ring_IP"
TEST_TYPE="convergence"

execute_problem $PROBLEM_TYPE 1 $TEST_TYPE 10
execute_problem $PROBLEM_TYPE 2 $TEST_TYPE 40
execute_problem $PROBLEM_TYPE 3 $TEST_TYPE 100
execute_problem $PROBLEM_TYPE 4 $TEST_TYPE 300
execute_problem $PROBLEM_TYPE 5 $TEST_TYPE 1200

PROBLEM_TYPE="traffic"
TEST_TYPE="convergence"

#execute_problem $PROBLEM_TYPE 3 $TEST_TYPE 1200
#execute_problem $PROBLEM_TYPE 4 $TEST_TYPE 1800
#execute_problem $PROBLEM_TYPE 5 $TEST_TYPE 1800

echo End of tests
