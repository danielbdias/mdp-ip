package add;

public class PrincipalADD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 ContextADD T1=new ContextADD();
		 
		 Integer NodeA=T1.getTerminalNode(1);
		 Integer NodeB=T1.getTerminalNode(0);
		 Integer NodeC=T1.getInternalNode(new Integer(2), NodeA, NodeB);
		 Integer NodeD=T1.getInternalNode(new Integer(1), NodeC, NodeB);
		 //T1.view(NodeD);
		 Integer Dr=T1.reduce(NodeD);
		 //T1.view(Dr);
		 //////////////////////
		 
		 Integer Node4=T1.getTerminalNode(0);
		 Integer Node5=T1.getTerminalNode(1);
		 Integer Node2=T1.getInternalNode(new Integer(2), Node4, Node5);
		 Integer Node3=T1.getInternalNode(new Integer(2), Node4, Node5);
		 Integer Node1=T1.getInternalNode(new Integer(1), Node2, Node3);
		 		 
		 //T1.view(Node1);
		 
		 Integer Node1R=T1.reduce(Node1);
		 
		 System.out.println("Reduce");
		 //T1.view(Node1R);
				
		 /////////////////////////
		  
		 System.out.println("Sumando");
	     
	     //Integer Result=T1.apply(Dr, Node1R, ADDContext.SUM);
	     //T1.view(Dr);
	     //T1.view(Node1R);
	     //T1.view(Result);
	     //Integer ResultSumXi=getSumADD(T1, 1);
	     //T1.view(ResultSumXi);
	     
	     //Integer ResultSum2Xi=getSumADDExponential(T1, 2);
	     //T1.view(ResultSum2Xi);
	     
/*	     Integer ResultSubXi=getSubADD(T1, 1);
	     T1.view(ResultSubXi);*/
	     
	     //Double max=(Double)T1.apply(ResultSum2Xi,ADDContext.MAXVALUE);
	     //System.out.println(max);
	     //Double min=(Double)T1.apply(ResultSum2Xi,ADDContext.MINVALUE);
	     //System.out.println(min);
	     
	     //Integer result=T1.apply(new Integer(1), ADDContext.RESTRICT_LOW, ResultSum2Xi);
	     //T1.view(result);
	     ///////
	     Integer A=T1.getTerminalNode(6);
		 Integer B=T1.getTerminalNode(2);
		 Integer C=T1.getTerminalNode(10);
		 Integer D=T1.getTerminalNode(20);
		 
		 Integer E=T1.getInternalNode(new Integer(2), A, B);
		 Integer F=T1.getInternalNode(new Integer(1), E, C);
		 Integer G=T1.getInternalNode(new Integer(1), E, D);
		 Integer H=T1.getInternalNode(new Integer(0), F, G);
		 
		 T1.view(H);
		 //Integer Hr=T1.apply(new Integer(1), ADDContext.RESTRICT_LOW, H);
		 Object  Hr=T1.apply(new Integer(1), ContextADD.SUMOUT, H);
		 T1.view((Integer)Hr);
	     
	}
	public static  Object getSumADD(Context T1, int n) {
		Object result = T1.getTerminalNode(0d);
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object  Node5=T1.getTerminalNode(1);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			result=T1.apply(result, Node, ContextADD.SUM);
		}
		return result;
	}
	public static Object  getSumADDExponential(Context T1, int n) {
		Object result = T1.getTerminalNode(0);
		double val=1;
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object Node5=T1.getTerminalNode(val);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			//T1.view(Node);
			result=T1.apply(result, Node, ContextADD.SUM);
			val=val*2;
	}
		return result;
	}

/*	public static Object getSubADD(Context T1, int n) {
		Object result = T1.getTerminalNode(0);
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object Node5=T1.getTerminalNode(1);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			//T1.view(Node);
			result=T1.apply(result, Node, ADDContext.SUB);
		}
		return result;
	}
*/

	
}
