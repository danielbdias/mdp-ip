package mdp;

import generator.TriangleTireWorldGen;

public class CallPrincipal {

	/**
	 * @param args
	 */
	
	public static void main(String args[])
	{
		for (int i = 0; i < 5; i++) {
			int numberOfVariables = 2 + (1 + (i + 3)) * (i + 3) / 2; //2 + arithmethic progression sum from 1 to i, steps 1
			
			String[] anotherArgs = {
				"//home//daniel//workspaces//java//mdpip//ADD//problemsMDPIP//tests//triangle_tireworld_" + numberOfVariables  + ".net",
				Integer.toString(i+1),
				"0.9",
				"0.01"
			};
			
			TriangleTireWorldGen.main(anotherArgs);
		}		
	}
}
