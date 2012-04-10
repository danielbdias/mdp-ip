package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ArrayList<String[]> spuddList = getArgsForSpudd("bi_ring_IP", 1, 6);
//		
//		for (String[] arguments : spuddList)
//			PolicyEvaluator.main(arguments);
		
		for (String type : new String[] { "tenpercent" }) {
			ArrayList<String[]> rtdpList = getArgsForRtdpIP("bi_ring_IP", 1, 6, type);
			
			for (String[] arguments : rtdpList)
				PolicyEvaluator.main(arguments);	
		}
		
		System.out.println("End of simulation");
	}

	protected static ArrayList<String[]> getArgsForSpudd(String problem, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +"_Interval07.net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//value" + problem + "_" + i +"_Interval07_0_0REGR.net",
				"30",
				"50",
				"160",
				"MDPIP",
				"Total",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//" + problem + "_sim_results_interval07.txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}

	protected static ArrayList<String[]> getArgsForRtdpIP(String problem, int initialProblem, int finalProblem, String type) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +"_Interval01.net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//test01_sysadmin_biring_interval01//value" + problem + "_" + i +"_Interval01_RTDPIP_" + type + ".net",
				"30",
				"50",
				"160",
				"MDPIP",
				"RTDPIP",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//test01_sysadmin_biring_interval01//" + problem + "_sim_results_" + type + "_Interval01.txt"					
			};
			
			list.add(args);
		}
		
		return list;
	}
}
