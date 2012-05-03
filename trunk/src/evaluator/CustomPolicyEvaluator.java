package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ArrayList<String[]> spuddList = getArgsForSpudd("uni_ring_IP", 1, 6);
//		
//		for (String[] arguments : spuddList)
//			PolicyEvaluator.main(arguments);
		
		for (String type : new String[] { "onequarter"/*, "tenpercent", "fivepercent"*/ }) {
			ArrayList<String[]> rtdpList = getArgsForRtdpIP("uni_ring_IP", 4, 6, type);
			
			for (String[] arguments : rtdpList)
				PolicyEvaluator.main(arguments);	
		}
		
		System.out.println("End of simulation");
	}

	protected static ArrayList<String[]> getArgsForSpudd(String problem, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (String simulationNature : new String[] { /*"Stationary", "NonStationary", "GlobalMyopicAdversarial", */ "LocalMyopicAdversarial" }) {
			for (int i = initialProblem; i <= finalProblem; i++) {
				String[] args = {
						"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i + ".net",
						"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//sysadmin_uniring//value" + problem + "_" + i + "_0_0REGR.net",
						"30",
						"50",
						"160",
						"MDPIP",
						"Total",
						"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//" + problem + "_sim_results_" + simulationNature + ".txt",
						simulationNature
				};

				list.add(args);
			}
		}
		
		return list;
	}

	protected static ArrayList<String[]> getArgsForRtdpIP(String problem, int initialProblem, int finalProblem, String type) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (String simulationNature : new String[] { /*"Stationary", "NonStationary", "GlobalMyopicAdversarial",*/ "LocalMyopicAdversarial" }) {
			for (int i = initialProblem; i <= finalProblem; i++) {
				String[] args = {
					"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +".net",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//test_01_sysadmin_uniring//value" + problem + "_" + i +"_RTDPIP_" + type + ".net",
					"30",
					"50",
					"160",
					"MDPIP",
					"RTDPIP",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//" + problem + "_sim_results_" + type + "_" + simulationNature + ".txt",
					simulationNature
				};
				
				list.add(args);
			}
		}
		
		return list;
	}
}
