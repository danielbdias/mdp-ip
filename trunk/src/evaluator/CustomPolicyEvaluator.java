package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//"Stationary", "NonStationary", "GlobalMyopicAdversarial", "LocalMyopicAdversarial"
			
		//For each policy simulation, we must pass the follow arguments:
		//1) Problem file path (example, bi_ring_IP_1.net)
		//2) Number of initial states in the problem
		//3) Number of simulations to be run in the simulator
		//4) Size of the simulation horizon
		//5) Type of MDP to be simulated (MDP or MDPIP)
		//6) Type of the solution to be simulated (Total, for SPUDD, or RTDP, 
		//   for Real Time Dynamic Programming)
		//7) Output report file path (example, simulation_report.txt)
		//8) Nature type of the simulation (used only in MDP-IPs, "GlobalMyopicAdversarial", 
		//   "LocalMyopicAdversarial", "NonStationary" and "Stationary")
		
		ArrayList<String[]> policyEvaluationList = new ArrayList<String[]>();
		
//		scenarioSPUDD(policyEvaluationList, "SysAdmin_UniRing", "uni_ring_IP", 1, 8);
//		scenarioSPUDD(policyEvaluationList, "Traffic", "traffic", 3, 6);
//		scenarioSPUDD(policyEvaluationList, "Navigation", "navigation", 0, 5);
//		
//		scenarioRTDP(policyEvaluationList, "SysAdmin_UniRing", "uni_ring_IP", 1, 8, "Min");
//		scenarioRTDP(policyEvaluationList, "Navigation", "navigation", 0, 5, "Min");
//		scenarioRTDP(policyEvaluationList, "Traffic", "traffic", 3, 6, "Random");
//		scenarioRTDP(policyEvaluationList, "SysAdmin_UniRing", "uni_ring_IP", 1, 8, "Random");
		scenarioRTDP(policyEvaluationList, "Navigation", "navigation", 0, 5, "Random");
		
		for (String[] arguments : policyEvaluationList)
			PolicyEvaluator.main(arguments);
		
		System.out.println("End of simulation");
	}
	
	protected static void scenarioSPUDD(ArrayList<String[]> policyEvaluationList, String baseFolderName, String instanceName, int startInstance, int endInstance) {
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
			
		for (int i = startInstance; i <= endInstance; i++) {
			String problemFile = instanceName + "_" + i + ".net";
			String addFile = "value" + instanceName + "_" + i + "_0_0REGR_full.net";
			String outputFile = instanceName + "_sim_results.txt";
			
			policyEvaluationList.add(new String[] {
				rootDir + "//problemsMDPIP//" + problemFile,
				rootDir + "//reportsMDPIP//results//spudd//" + baseFolderName  + "//" + addFile,
				"1",
				"50",
				"40",
				"MDPIP",
				"Total",
				rootDir + "//reportsMDPIP//results//spudd//" + outputFile,
				"NonStationary"
			});
		}
	}
		
	protected static void scenarioRTDP(ArrayList<String[]> policyEvaluationList, String baseFolderName, String instanceName, int startInstance, int endInstance, String rtdpType) {
		String[] times = { "Full", "OneHalf", "OneQuarter", "TenPercent", "FivePercent" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		
		for (String time : times) {
			for (int i = startInstance; i <= endInstance; i++) {
				String problemFile = instanceName + "_" + i + ".net";
				String addFile = "value" + instanceName + "_" + i + "_RTDPIP_" + time.toLowerCase() + ".net";
				String outputFile = instanceName + "_sim_results_" + time.toLowerCase() + ".txt";
				
				policyEvaluationList.add(new String[] {
					rootDir + "//problemsMDPIP//" + problemFile,
					rootDir + "//reportsMDPIP//results//rtdp//" + rtdpType + "//" + baseFolderName  + "//" + addFile,
					"1",
					"50",
					"40",
					"MDPIP",
					"Total",
					rootDir + "//reportsMDPIP//results//rtdp//" + rtdpType + "//" + outputFile,
					"NonStationary"
				});
			}
		}
	}
}
