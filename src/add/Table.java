package add;

import graph.Graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class Table extends NodeKey{
	TreeSet vars;
	ArrayList values;
	public Table(TreeSet vars,	ArrayList values) {
	  this.vars=vars;
	  this.values=values;
	}
	public Table(Table table) {
		this.vars=new TreeSet(table.vars);
		this.values=new ArrayList(table.values);
		//this.vars.addAll(table.vars);
		//this.values.addAll(table.values);
	}
	public ArrayList getValues(){
		return values;
	}
	public TreeSet getVars(){
		return vars;
	}
	public String toString(Context context){
		String res= new String("Vars:  "+vars.toString()+ " Values:  ");
		for(int i=0;i<values.size();i++){
			if(values.get(i) instanceof Polynomial){
				res=res+((Polynomial)values.get(i)).toString(context,"p")+";  ";
			}
			else{
				res=res+values.get(i).toString()+";  ";
			}
		}
		return res;
	}
	
	
	@Override
	public void toGraph(Graph g, Context fTree) {
		// TODO for TABLE
		System.out.println("NOT IMPLEMENTED FOR TABLE, see toGVizRecord");
		System.exit(0);
	
	}
	
	public String toGVizRecord(Context context) {

		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		// Now print table
		sb1.append("Vars: ");
		Iterator it = vars.iterator();
		while (it.hasNext()) {
			sb1.append( (Integer)it.next() + " ");
		}
		for (int pos = 0; pos < Math.pow(2, vars.size()); pos++) {
			int  assigment[]=getAssigment(pos,vars.size() );
			// Now show the assignment
			sb1.append("|" + assigmenttoString(assigment));
			if(this.values.get(pos) instanceof Polynomial){
				sb2.append("|" + ((Polynomial) this.values.get(pos)).toString(context,"p"));
			}
			else{
				sb2.append("|" + (Context._df.format((Double) this.values.get(pos))));
			}
		}
		return "{" + sb1 + "}|{" + sb2 + "}";

	}
	
	
	
	private StringBuffer assigmenttoString(int[] assigment) {
		StringBuffer res=new StringBuffer();
		for(int i=0;i<assigment.length;i++){
			res.append(assigment[i]+" ");
		}
		return res;
	}
	private int[] getAssigment(int pos, int numberVariables) {
		int  assigment[]= new int[numberVariables];
		for(int j=numberVariables-1;j>=0;j--){
		   assigment[j]= pos%2;
		   pos=pos/2;         
		}
		return assigment;
	}
	/*public void printCoef(Context context, String  NAME_FILE_COEF) {
		// TODO Auto-generated method stub
 	try {
 		
        BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_COEF));
        
		for(int i=0;i<values.size();i++){
			if(values.get(i) instanceof Polynomial){
				out.write(((Polynomial)values.get(i)).toStringCoef(context));
				out.append(System.getProperty("line.separator"));
			}
		}
        
         out.close();
    } catch (IOException e) {
    	System.out.println("Problem with the creation of the coefficient file");
    	System.exit(0);
    }

	}*/
	
}
