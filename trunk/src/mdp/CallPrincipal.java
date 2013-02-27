package mdp;

import generator.NavigationGen;
import generator.TriangleTireWorldGen;

public class CallPrincipal {

	/**
	 * @param args
	 */
	
	public static void main(String args[])
	{
//		int[][] coordinates = { { 3, 2 }, { 4, 2 }, { 3, 3 }, { 5, 2 }, { 3, 4 }, { 3, 5 }, 
//				                { 3, 6 }, { 4, 5 }, { 3, 7 }, { 4, 6 }, { 5, 5 }, { 4, 7 },
//				                { 5, 6 }, { 5, 7 }, { 6, 6 } };
		
		int[][] coordinates = { { 7, 7 }, { 8, 8 }, { 9, 9 }, { 10, 10 }, { 11, 11 } };
		
		for (int[] coordinate : coordinates) {
			int numberOfLines = coordinate[0];
			int numberOfColumns = coordinate[1];
			int numberOfVariables = numberOfLines * numberOfColumns;
			
			String[] anotherArgs = {
					"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//navigation_" + numberOfVariables  + ".net",
					Integer.toString(numberOfLines),
					Integer.toString(numberOfColumns),
					"0.9",
					"0.01"
				};
			
			NavigationGen.main(anotherArgs);
		}
		
		//int[] instances = { 1, 2, 3, 4, 5, 6 };
//		int[] instances = { 7, 8, 9, 10, 11, 12, 13, 14, 15 };
//		
//		for (int i : instances) {
//			int numberOfVariables = (((3 + i) * (2 + i)) / 2) + 2;
//			
//			String[] anotherArgs = {
//					"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//triangle_tireworld_" + numberOfVariables  + ".net",
//					Integer.toString(i),
//					"0.9",
//					"0.01"
//				};
//			
//			TriangleTireWorldGen.main(anotherArgs);
//		}
	}
}
