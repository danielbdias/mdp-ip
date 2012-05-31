package evaluator;

import java.util.ArrayList;

public class CustomPolicyEvaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//"Stationary", "NonStationary", "GlobalMyopicAdversarial", "LocalMyopicAdversarial"
				
		ArrayList<String[]> policyEvaluationList = new ArrayList<String[]>();
		
		for (String simulationNature : new String[] { "NonStationary", "GlobalMyopicAdversarial" }) {
//			policyEvaluationList.addAll(getArgsForApricodd("uni_ring_IP", simulationNature, "uni_ring_ip", "uai2012//uni_ring_IP_apricodd", 1, 8));
			
//			policyEvaluationList.addAll(getArgsForSpudd("traffic", simulationNature, "traffic_old", "uai2012//traffic", 3, 6));
//			policyEvaluationList.addAll(getArgsForSpudd("uni_ring_IP", simulationNature, "sysadmin_uniring", "uai2012//uni_ring_min", 1, 8));
//			policyEvaluationList.addAll(getArgsForSpudd("bi_ring_IP", simulationNature, "sysadmin_biring", "uai2012//bi_ring_rand", 1, 8));
//			policyEvaluationList.addAll(getArgsForSpudd("bi_ring_IP", simulationNature, "sysadmin_biring", "uai2012//bi_ring_min", 1, 8));
			
//			for (String rtdpType : new String[] { "onequarter", "tenpercent", "fivepercent" }) {
//				policyEvaluationList.addAll(getArgsForRtdp("traffic", simulationNature, "test01_traffic_old", "uai2012//traffic", rtdpType, 3, 6));
//				policyEvaluationList.addAll(getArgsForRtdp("uni_ring_IP", simulationNature, "test_01_sysadmin_uniring", "uai2012//uni_ring_min", rtdpType, 1, 8));
//				policyEvaluationList.addAll(getArgsForRtdp("bi_ring_IP", simulationNature, "test_04_sysadmin_biring", "uai2012//bi_ring_rand", rtdpType, 1, 8));
//				policyEvaluationList.addAll(getArgsForRtdp("bi_ring_IP", simulationNature, "test_01_sysadmin_biring", "uai2012//bi_ring_min", rtdpType, 1, 8));	
//			}
			
			//TODO: pensar no uniring com intervalos de imprecisão
		}
		
		for (String[] arguments : policyEvaluationList)
			PolicyEvaluator.main(arguments);
		
		System.out.println("End of simulation");
	}

	protected static ArrayList<String[]> getArgsForSpudd(String problem, String simulationNature, 
			String inputFolder, String outputFolder, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
					"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i + ".net",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//spudd//" + inputFolder + "//value" + problem + "_" + i + "_0_0REGR.net",
					"2",
					"50",
					"160",
					"MDPIP",
					"Total",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//" + outputFolder + "//" + problem + "_sim_results_spuddip_" + simulationNature + ".txt",
					simulationNature
			};

			list.add(args);
		}
			
		return list;
	}

	protected static ArrayList<String[]> getArgsForApricodd(String problem, String simulationNature, 
			String inputFolder, String outputFolder, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
					"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i + ".net",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//apricodd//" + inputFolder + "//value" + problem + "_" + i + "_0_15APRI.net",
					"2",
					"50",
					"160",
					"MDPIP",
					"Total",
					"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//" + outputFolder + "//" + problem + "_sim_results_apricodd_" + simulationNature + ".txt",
					simulationNature
			};

			list.add(args);
		}
			
		return list;
	}
	
	protected static ArrayList<String[]> getArgsForRtdp(String problem, String simulationNature, 
			String inputFolder, String outputFolder, String type, int initialProblem, int finalProblem) {
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		for (int i = initialProblem; i <= finalProblem; i++) {
			String[] args = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//" + problem + "_" + i +".net",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//rtdpip//" + inputFolder + "//value" + problem + "_" + i +"_RTDPIP_" + type + ".net",
				"30",
				"50",
				"160",
				"MDPIP",
				"RTDPIP",
				"//home//daniel//workspaces//java//mdpip//ADD//reportsMDPIP//results//" + outputFolder + "//" + problem + "_sim_results_rtdpip_" + type + "_" + simulationNature + ".txt",
				simulationNature
			};
			
			list.add(args);
		}
		
		return list;
	}
}
