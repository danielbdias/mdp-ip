package add;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class PrincipalPoly {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
			
		ContextADD T1=new ContextADD();
		Integer l1=T1.getIdLabelProd("2,4,6");
		Integer l2=T1.getIdLabelProd("2,4,7");
		Integer l3=T1.getIdLabelProd("2,3,9");
		Integer l4=T1.getIdLabelProd("4,8");
		T1.listVarProb.add("2");
		T1.listVarProb.add("4");
		T1.listVarProb.add("6");
		T1.listVarProb.add("7");
		T1.listVarProb.add("3");
		T1.listVarProb.add("9");
		T1.listVarProb.add("8");
		Hashtable t=new Hashtable();
		t.put(l1, 1.3d);
		t.put(l2, -0.5d);
		t.put(l3, -0.5d);
		t.put(l4, -2d);
		Polynomial p1= new  Polynomial(new Double(5),t,T1);
		System.out.println(p1.toString(T1,"p"));
		ArrayList idsClash=new ArrayList();
		Hashtable dirList=p1.constructDirectionList(T1.listVarProb,T1, idsClash);
		Polynomial p2=p1.aproxPol(T1.listVarProb,T1, idsClash, 2.3);
		
		System.out.println(p1.toString(T1,"p"));
		System.out.println(p2.toString(T1,"p"));
		Integer label1=T1.getIdLabelProd("1");
		Integer label2=T1.getIdLabelProd("2");
		Integer label4=T1.getIdLabelProd("2");
		Integer label5=T1.getIdLabelProd("8");
		Integer labelborrar=T1.getIdLabelProd("2020");
		Integer label3=T1.getIdLabelProd("3,4");
		
		
		Hashtable terms=new Hashtable();
		terms.put(label1, 7d);
		terms.put(label2, 8d);
		terms.put(label3, 3d);
		Polynomial pol1= new  Polynomial(new Double(5),terms,T1);
		System.out.println(pol1.toString(T1,"p"));
		Hashtable terms2=new Hashtable();
		terms2.put(label4, 3d);
		terms2.put(label5, 5d);
		Polynomial pol2= new  Polynomial(new Double(4),terms2,T1);
		System.out.println(pol2.toString(T1,"p"));
		Polynomial pol3=pol1.sumPolynomial(pol2);
		
		Polynomial pol4=pol1.subPolynomial(pol2);
		
		
		Polynomial pol5=pol1.prodPolynomial(pol2,T1);
		System.out.println(pol5.toString(T1,"p"));
		
		Polynomial polC=new Polynomial(5.0,new Hashtable(),T1);
		 Integer NodeA=T1.getTerminalNode(pol1);
		 Integer NodeB=T1.getTerminalNode(polC);
		 Integer NodeC=T1.getInternalNode(new Integer(2), NodeA, NodeB);
		 Integer NodeD=T1.getInternalNode(new Integer(1), NodeC, NodeB);
		 //T1.view(NodeD);
		 Integer Dr=T1.reduce(NodeD);
		 T1.view(Dr);
		 Integer NodeR=T1.getTerminalNode(pol2);
		 Polynomial polS=new Polynomial(2.0,new Hashtable(),T1);
		 Integer NodeS=T1.getTerminalNode(polS);
		 Integer NodeT=T1.getInternalNode(new Integer(2), NodeS, NodeR);
		 Integer DrT=T1.reduce(NodeT);
		 T1.view(DrT);
		 Object D=T1.apply(Dr, DrT, Context.SUM);
	     T1.view(D);
	     Object K=T1.apply(Dr, DrT, Context.PROD);
	     T1.view(K);
		
	}
}

