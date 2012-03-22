package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ArrayList<String[]> spuddList = getArgsForSpudd("traffic", 4, 4);
//		
//		for (String[] arguments : spuddList)
//			PolicyEvaluator.main(arguments);
		
		for (String type : new String[] { "onequarter" }) {
		//for (String type : new String[] { "full", "threequarter", "half", "onequarter", "tenpercent", "fivepercent" }) {
			//ArrayList<String[]> rtdpList = getArgsForRtdpIP("uni_ring_IP", 1, 8, type);
			ArrayList<String[]> rtdpList = getArgsForRtdpIP("traffic", 3, 4, type);
			
			for (String[] arguments : rtdpList)
				PolicyEvaluator.main(arguments);	
		}
		
		System.out.println("End of simulation");
	}

	protected static ArrayList<String[]> getArgsForSpudd(String problem, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +".net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//traffic//value" + problem + "_" + i +"_0_0REGR.net",
				"30",
				"50",
				"160",
				"MDPIP",
				"Total",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//" + problem + "_sim_results.txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}

	private static ArrayList<String[]> getArgsForRtdpIP(String problem, int initialProblem, int finalProblem, String type) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +".net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//value" + problem + "_" + i +"_RTDPIP_" + type + ".net",
				"30",
				"50",
				"160",
				"MDPIP",
				"RTDPIP",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//" + problem + "_sim_results_" + type + ".txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}
}
