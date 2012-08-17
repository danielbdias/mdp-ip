package evaluator;

import java.util.ArrayList;

import mdp.ADDEnumerator;
import mdp.MDPType;

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
		
//		ArrayList<String[]> policyEvaluationList = new ArrayList<String[]>();
//		
//		scenario04(policyEvaluationList);
//				
//		for (String[] arguments : policyEvaluationList)
//			PolicyEvaluator.main(arguments);
		
		anotherTask();
		
		System.out.println("End of simulation");
	}
	
	protected static void anotherTask() {
		ArrayList<String[]> addEnumerationList = new ArrayList<String[]>();
		
		String[] times = { "Full", "OneHalf", "OneQuarter", "TenPercent", "FivePercent" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		String baseFolderName = "SysAdmin_UniRing_";
		
		for (String time : times) {
			String problemFile = "uni_ring_IP_4.net";
			String addFile = "valueuni_ring_IP_4_RTDPIP_" + time.toLowerCase() + ".net";
			String outputFile = "uni_ring_IP_statevalues_results_" + time.toLowerCase() + ".txt";
			
			addEnumerationList.add(new String[] {
				rootDir + "//problemsMDPIP//" + problemFile,
				"Total",
				"1",
				"0",
				rootDir + "//reportsMDPIP//results//rtdp//Random//" + baseFolderName + time  + "//" + addFile,
				rootDir + "//reportsMDPIP//results//rtdp//Random//" + outputFile
			});		
		}
		
		for (String[] arguments : addEnumerationList)
			ADDEnumerator.main(arguments);
	}
	
	protected static void scenario04(ArrayList<String[]> policyEvaluationList) {
		//RTDP - partial reward
		
		String[] times = { "5hundred", "10hundred", "15hundred", "20hundred", "30hundred", "40hundred", "50hundred" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		String baseFolderName = "SysAdmin_UniRing4_Fragmented";
		
		for (String time : times) {
			String problemFile = "uni_ring_IP_4.net";
			String addFile = "valueuni_ring_IP_4_RTDPIP_" + time.toLowerCase() + ".net";
			String outputFile = "uni_ring_IP_sim_results_fragmented.txt";
			
			policyEvaluationList.add(new String[] {
				rootDir + "//problemsMDPIP//" + problemFile,
				rootDir + "//reportsMDPIP//results//rtdp//Min//" + baseFolderName + "//" + addFile,
				"1",
				"50",
				"160",
				"MDPIP",
				"Total",
				rootDir + "//reportsMDPIP//results//rtdp//Min//" + outputFile,
				"GlobalMyopicAdversarial"
			});
		}
	}

	protected static void scenario03(ArrayList<String[]> policyEvaluationList) {
		//SPUDD - partial reward
		
		String[] times = { "5hundred", "10hundred", "15hundred", "20hundred", "30hundred", "40hundred", "50hundred" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		String baseFolderName = "SysAdmin_UniRing4_Fragmented";
		
		for (String time : times) {
			String problemFile = "uni_ring_IP_4.net";
			String addFile = "valueuni_ring_IP_4_0_0REGR_" + time.toLowerCase() + ".net";
			String outputFile = "uni_ring_IP_sim_results_fragmented.txt";
			
			policyEvaluationList.add(new String[] {
				rootDir + "//problemsMDPIP//" + problemFile,
				rootDir + "//reportsMDPIP//results//spudd//" + baseFolderName + "//" + addFile,
				"1",
				"50",
				"160",
				"MDPIP",
				"Total",
				rootDir + "//reportsMDPIP//results//spudd//" + outputFile,
				"GlobalMyopicAdversarial"
			});
		}
	}
	
	protected static void scenario02(ArrayList<String[]> policyEvaluationList) {
		//RTDP - simulation reward
		
		String[] times = { "Full", "OneHalf", "OneQuarter", "TenPercent", "FivePercent" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		String baseFolderName = "SysAdmin_UniRing_";
		
		int startInstance = 1;
		int endInstance = 8;
		
		for (String time : times) {
			for (int i = startInstance; i <= endInstance; i++) {
				String problemFile = "uni_ring_IP_" + i + ".net";
				String addFile = "valueuni_ring_IP_" + i + "_RTDPIP_" + time.toLowerCase() + ".net";
				String outputFile = "uni_ring_IP_sim_results_" + time.toLowerCase() + ".txt";
				
				policyEvaluationList.add(new String[] {
					rootDir + "//problemsMDPIP//" + problemFile,
					rootDir + "//reportsMDPIP//results//rtdp//Random//" + baseFolderName + time  + "//" + addFile,
					"1",
					"50",
					"160",
					"MDPIP",
					"Total",
					rootDir + "//reportsMDPIP//results//rtdp//Random//" + outputFile,
					"GlobalMyopicAdversarial"
				});
			}
		}
	}
	
	protected static void scenario01(ArrayList<String[]> policyEvaluationList) {
		//SPUDD - simulation reward
		
		String[] times = { "Full", "OneHalf", "OneQuarter", "TenPercent", "FivePercent" };
		String rootDir = "//home//daniel//workspaces//java//mdpip//ADD";
		String baseFolderName = "SysAdmin_UniRing_";
		
		int startInstance = 1;
		int endInstance = 8;
		
		for (String time : times) {
			for (int i = startInstance; i <= endInstance; i++) {
				String problemFile = "uni_ring_IP_" + i + ".net";
				String addFile = "valueuni_ring_IP_" + i + "_0_0REGR_" + time.toLowerCase() + ".net";
				String outputFile = "uni_ring_IP_sim_results_" + time.toLowerCase() + ".txt";
				
				policyEvaluationList.add(new String[] {
					rootDir + "//problemsMDPIP//" + problemFile,
					rootDir + "//reportsMDPIP//results//spudd//" + baseFolderName + time  + "//" + addFile,
					"1",
					"50",
					"160",
					"MDPIP",
					"Total",
					rootDir + "//reportsMDPIP//results//spudd//" + outputFile,
					"GlobalMyopicAdversarial"
				});
			}
		}
	}
}
