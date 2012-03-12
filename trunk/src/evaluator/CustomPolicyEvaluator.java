package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ArrayList<String[]> spuddList = getArgsForSpudd(1, 8);
//		
//		for (String[] arguments : spuddList)
//			PolicyEvaluator.main(arguments);
				
		for (String type : new String[] { "full", "threequarters", "half" }) {
			ArrayList<String[]> rtdpList = getArgsForRtdpIP(1, 8, type);
			
			for (String[] arguments : rtdpList)
				PolicyEvaluator.main(arguments);	
		}
	}

	private static ArrayList<String[]> getArgsForSpudd(int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//uni_ring_IP_" + i +".net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//valueuni_ring_IP_" + i +"_0_0REGR.net",
				"30",
				"50",
				"160",
				"MDPIP",
				"Total",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//uni_ring_IP_sim_results.txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}

	private static ArrayList<String[]> getArgsForRtdpIP(int initialProblem, int finalProblem, String type) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//uni_ring_IP_" + i +".net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//valueuni_ring_IP_" + i +"_RTDPIP_" + type + ".net",
				"30",
				"50",
				"160",
				"MDPIP",
				"RTDPIP",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//uni_ring_IP_sim_results_" + type + ".txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}
}
