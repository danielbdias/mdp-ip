package mdp;

import generator.*;

public class CallPrincipal {

	/**
	 * @param args
	 */
	
	public static void main(String args[])
	{
//		int[][] coordinates = { { 3, 2 }, { 4, 2 }, { 3, 3 }, { 5, 2 }, { 3, 4 }, { 3, 5 }, 
//				                { 3, 6 }, { 4, 5 }, { 3, 7 }, { 4, 6 }, { 5, 5 }, { 4, 7 },
//				                { 5, 6 }, { 5, 7 }, { 6, 6 }, { 7, 7 }, { 8, 8 }, { 9, 9 }, 
//				                { 10, 10 }, { 11, 11 } };
		
//		int[][] coordinates = { { 3, 2 }, { 4, 2 }, { 3, 3 }, { 5, 2 }, { 3, 4 }, { 3, 5 }, 
//                { 3, 6 }, { 4, 5 }, { 3, 7 }, { 4, 6 }, { 5, 5 }, { 4, 7 },
//                { 5, 6 }, { 5, 7 }, { 6, 6 }, { 7, 7 } };
		
//		for (int[] coordinate : coordinates) {
//			int numberOfLines = coordinate[0];
//			int numberOfColumns = coordinate[1];
//			int numberOfVariables = numberOfLines * numberOfColumns;
//			
//			String[] anotherArgs = {
//					"D://Users//Daniel//workspace//mdp-ip//problemsMDPIP//ssp_navigation_" + numberOfVariables  + ".net",
//					Integer.toString(numberOfLines),
//					Integer.toString(numberOfColumns),
//					"1.0",
//					"0.01",
//					"0.1",
//					"1.0"
//				};
//			
//			NavigationGen.main(anotherArgs);
//		}
		
//		int[] instances = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
//		int[] instances = { 1, 2, 3, 4, 5 };
		//int[] instances = { 2 };
		
//		for (int i : instances) {
//			int numberOfVariables = TriangleTireWorldGen.getNumberOfVariablesForInstance(i);
//			
//			String[] anotherArgs = {
//					"D://Users//Daniel//workspace//mdp-ip//problemsMDPIP//ssp_triangle_tireworld_" + numberOfVariables  + ".net",
//					Integer.toString(i),
//					"1.0",
//					"0.01"
//				};
//			
//			TriangleTireWorldGen.main(anotherArgs);
//		}
		
//		for (int i : instances) {
//			int numberOfVariables = RelaxedTriangleTireWorldGen.getNumberOfVariablesForInstance(i);
//			
//			String[] anotherArgs = {
//					"D://Users//Daniel//workspace//mdp-ip//problemsMDPIP//ssp_relaxed_triangle_tireworld_" + numberOfVariables  + ".net",
//					Integer.toString(i),
//					"1.0",
//					"0.01"
//				};
//			
//			RelaxedTriangleTireWorldGen.main(anotherArgs);
//		}
		
		int[] computers = { 2, 3, 4, 5, 6, 7, 8 };
		
		for (int i : computers) {		
			String[] anotherArgs = {
					"D://Users//Daniel//workspace//mdp-ip//problemsMDPIP//ssp_uni_ring_IP_" + i  + ".net",
					Integer.toString(i),
					"1.0",
					"0.01"
				};
			
			SSPSysAdminUniRingGen.main(anotherArgs);
		}
	}
}
