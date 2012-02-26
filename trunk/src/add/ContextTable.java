package add;

import graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import logic.add.ADDRNode;
import logic.add.Table.T;
import mdp.State;

public class ContextTable extends Context {

    private Double currentDrop,minDrop;
	
	public ContextTable() {
		//ok//
		this.unllocatedIdNode=new Integer(0);
		nodesCache=new Hashtable();
		inverseNodesCache=new Hashtable();
		//reduceCache=new Hashtable();
		//applyCache=new Hashtable();
		//restrictCache=new Hashtable();
		remapIdCache=new Hashtable();
		//reduceRemapLeavesCache=new Hashtable();
		///Parameterized ADD
		this.labelsProdId=new Hashtable();
		this.inverseLabelsProdId=new Hashtable();
		this.unllocatedIdLabelProd=new Integer(0);
	  
	}
	
	@Override
	public Object GetNode(Integer fvar, Object fh, Object fl) {
		//ok//
		Integer id=this.getInternalNode(fvar, fh, fl);
		return id;		
	}

	@Override
	public Object apply(Object f1, Object f2, BinaryOperation op) {
		//ok//
		Object fr=op.computeResult((Integer)f1,(Integer)f2,this);
		return fr;
			
	}

	@Override
	public Double apply(Object f1, UnaryOperationSimple op) {
		//ok
		Double fr=op.computeResult(f1, this);
    	return fr;
	}

	@Override
	public void copyInNewCacheNode(Object id) {
		//ok
		Object table =  inverseNodesCache.get(id);
		if (table instanceof Table) {
			inverseNodesCacheNew.put(id, table);
			nodesCacheNew.put(table,id);
		}
		else{
			
			System.out.println("Erro in copyInNewCacheNode ContextTable: must be table");
			System.exit(0);
		}		

	}

	@Override
	public Integer getIdCache(NodeKey nodek) {
		//ok
		return (Integer) this.nodesCache.get(nodek);		
	}

	@Override
	public Integer getInternalNode(Integer fvar, Object Fh, Object Fl) {
		//ok
		// For Table: Fh and Fl can only be terminal (constant or expression)
		 if(!this.isTerminalNode(Fh) || !this.isTerminalNode(Fl)){
			 System.out.println("getInternalNode can only be called with terminalNodes");
			 System.exit(0);
		 }
		 TreeSet vars;
		 ArrayList values;
		 Table FhTable=(Table)this.inverseNodesCache.get((Integer)Fh);
		 Table FlTable=(Table)this.inverseNodesCache.get((Integer)Fl);
		 if (FhTable.vars.size()>= FlTable.vars.size()){
			 vars=new TreeSet(FhTable.vars);
		 }
		 else{
			 vars=new TreeSet(FlTable.vars);
		 }
		 vars.add(fvar);
		 
		 values=getValuesFromFhFl(FhTable,FlTable,vars);
		 
		 Table table=new Table(vars,values);
		 Integer id=this.getNextUnllocatedId();
 		 this.putNodeCache(table,id);
  		 return id;
		
	}

	private ArrayList getValuesFromFhFl(Table fhTable, Table flTable, TreeSet vars) {
		//OK//
		 ArrayList valueFh=fhTable.values;
		 ArrayList valueFl=flTable.values;
		 ArrayList values=new ArrayList(valueFh.size()+valueFl.size());
		  
		  values.addAll(valueFl);
		  values.addAll(valueFh);
		  return values;
		
	}

	@Override
	public NodeKey getNodeInverseCache(Integer f) {
		//ok		
		return (NodeKey) this.inverseNodesCache.get(f);
		
	}

	@Override
	public Object getTerminalNode(double fval) {
		//OK//
		
		 Double fvalue=new Double(fval);
		 TreeSet vars=new TreeSet();
		 ArrayList values=new ArrayList();
		 values.add(fvalue);
		 Table table=new Table(vars,values);
		 Integer id=this.getNextUnllocatedId();
		 this.putNodeCache(table,id);
		 return id;
	}
	
	
	public Integer getNextUnllocatedId(){
		//ok
    	this.unllocatedIdNode=new Integer(this.unllocatedIdNode.intValue()+1);
    	return this.unllocatedIdNode;
    }
	
	public void putNodeCache(Table table, Integer id){
		//ok
    	this.nodesCache.put(table, id);
    	this.inverseNodesCache.put(id, table);
    }
	
	
//	for Parameterized ADD
	@Override
	//for create a simple polynomial only with one variable and without constant
    public Integer getTerminalNode(String varPro,Double coef){
		//ok
    	Integer label1=this.getIdLabelProd(varPro);
    	Hashtable terms=new Hashtable();
    	terms.put(label1, coef);
    	Polynomial pol1= new  Polynomial(new Double(0),terms,this);
        return getTerminalNode(pol1);
    }
	
	 public Integer getTerminalNode(String varPro){
	    	return getTerminalNode(varPro,1d);
	    }

	@Override
	public Integer getTerminalNode(Polynomial polynomial) {
		///OK//
		 TreeSet vars=new TreeSet();
		 ArrayList values=new ArrayList();
		 values.add(polynomial);
		 Table table=new Table(vars,values);
		 Integer id=this.getNextUnllocatedId();
		 this.putNodeCache(table,id);
		 return id;
	}

	@Override
	public boolean isTerminalNode(Object f) {
		//ok		
		Table table=(Table)this.inverseNodesCache.get(f);
		if ( table.vars.size()==0)
    		return true;
    	else
    		return false;
	}

	@Override
	public Object pruneNodesValue(Object valueiDD, double mergeError) {
		Table table=(Table)inverseNodesCache.get(valueiDD);
		Table minTableDrop=new Table(table);
		Table antMinTableDrop=new Table(table);
		
		Double sumError=0d;
		do{
			antMinTableDrop=new Table(minTableDrop);
			minTableDrop=getMinTableDrop(minTableDrop);
			sumError=sumError+minDrop;
		}while(sumError<mergeError && minTableDrop.vars.size()>1);
		
		Integer idNew=this.getNextUnllocatedId();
	   	this.putNodeCache(antMinTableDrop, idNew);
     	return idNew;

	}
	
	
	
	private Table getMinTableDrop(Table table) {
		minDrop=Double.POSITIVE_INFINITY;
		Table minTableDrop=null;
		Iterator it=table.vars.iterator();
		while (it.hasNext()){
			Table tableDrop=getTableDrop((Integer)it.next(),table);
			if (Math.min(minDrop, this.currentDrop)==currentDrop){
				minTableDrop=tableDrop;
				minDrop=currentDrop;
			}
		}	
		return minTableDrop;
	}

	//
	private Table getTableDrop(Integer varId, Table table) {
		Double maxDropVar=Double.NEGATIVE_INFINITY;
		TreeSet varsNew=new TreeSet(table.vars);
		varsNew.remove(varId);
		ArrayList valuesNew=new ArrayList();
		Table tableNew=new Table(varsNew,valuesNew);
		int sizeNew=(int)Math.pow(2, varsNew.size());
		Double  prom[]=new Double[sizeNew];
		for(int pos=0;pos<table.values.size();pos++){
			Hashtable varAssigment=this.getVarAssigment(pos, table.vars.size(), table.vars);
			Double value=(Double)table.values.get(pos);
			int index=getIndexTable(varAssigment,tableNew);
			if(prom[index]==null){
				prom[index]=value;
			}
			else{
				prom[index]=(prom[index]+value)/2;
				maxDropVar=Math.max(maxDropVar, Math.abs(prom[index]-value));
			}
		}
		this.currentDrop=maxDropVar;
		for(int i=0;i<sizeNew;i++){
		     valuesNew.add(prom[i]);	
		}
		return tableNew;
	}
	
	public Hashtable getVarAssigment(int pos, int numberVariables,TreeSet varsNew) {
		Hashtable res=new Hashtable();
		
		int  assigment[]=getAssigment(pos,numberVariables);
		Iterator it=varsNew.iterator();
		int i=0;
		while (it.hasNext()){
			    res.put(it.next(),assigment[i]);
			    i=i+1;
			    
		}
		return res;
	}
	
	public int[] getAssigment(int pos, int numberVariables) {
		int  assigment[]= new int[numberVariables];
		for(int j=numberVariables-1;j>=0;j--){
		   assigment[j]= pos%2;
		   pos=pos/2;         
		}
		return assigment;
	}
	
	public Object getValueTable(Hashtable varAssigment, Table table1) {//return a Poly or a Double
	    int index=getIndexTable(varAssigment,table1);
		return table1.values.get(index);
	}
	public int getIndexTable(Hashtable varAssigment, Table table1) {
		TreeSet varsPrime=table1.vars;
		int n=varsPrime.size();
		int index=0;
		//ArrayList assigPrime=new ArrayList(n);
		Iterator it=varsPrime.iterator();
		while(it.hasNext()){
			int val=(Integer)varAssigment.get(it.next());
			//assigPrime.add(val);//it is no necessary
			index=index+val*(int)Math.pow(2, n-1);
			n=n-1;
		}
		return index;
	}
	

	@Override
	public Object reduceRestrict(Integer idVar, UnaryOperationComplex op,Object id) {
       //ok
		
		Table table=(Table)inverseNodesCache.get(id);
		
		 if (this.isTerminalNode(id)){
			 return (Integer)id;
		 }
		 else{  //instanceof InternalNodeKey
			 TreeSet varsNew=new TreeSet(table.vars);
			 varsNew.remove(idVar);
			 ArrayList valuesNew=new ArrayList();
			 Integer pos=getPositionVar(idVar,table.vars);
			 int twoPower=(int) Math.pow(2,table.vars.size()-1-pos);
			 int i=0;
			 while (i<table.values.size()){
			      if (op == RESTRICT_LOW){
					  for(int j=0; j<twoPower;j++){
						  valuesNew.add(table.values.get(i));  
					      i=i+1;
					  }
					  i=i+twoPower;
				  }
			      else if (op == RESTRICT_HIGH){
			    	  i=i+twoPower;
			       	  for(int j=0; j<twoPower;j++){
						  valuesNew.add(table.values.get(i));  
					      i=i+1;
					  }
					 
			      }
			      else{
				    System.out.println("not exist this type of RESTRICT");
				    System.exit(0);
			      }
			 }
			 Table newTable=new Table(varsNew,valuesNew);
			 Integer idNew=this.getNextUnllocatedId();
		   	this.putNodeCache(newTable, idNew);
	    	return idNew;

		 }
 	}

	private Integer getPositionVar(Integer idVar, TreeSet vars) {
		int i=0;
		Iterator it=vars.iterator();
		while(it.hasNext()){
			if (idVar.compareTo((Integer)it.next())==0){
				return i;
			}
			i++;
		}
		return null;
	}

	@Override
	public Object remapIdWithPrime(Object id, HashMap hmPrimeRemap) {
		//ok
		if(this.isTerminalNode(id)){
    		return id;
    	}
		    TreeSet varsPrime=new TreeSet();
    		Table table=(Table)inverseNodesCache.get(id);
    		Iterator it=table.vars.iterator();
    		while (it.hasNext()){
    			 Integer var=(Integer)it.next();
    			 Integer FvarPrime=(Integer) hmPrimeRemap.get(var);
    			 varsPrime.add(FvarPrime);
    		}
    		ArrayList valuesPrime=new ArrayList(table.values);
    		Table tablePrime=new Table(varsPrime,valuesPrime);
    		Integer idPrime=this.getNextUnllocatedId();
    		this.putNodeCache(tablePrime, idPrime);
    		return idPrime;
		
	}
	public Object remapIdWithOutPrime(Object id, HashMap hmPrime2IdRemap) {
	 System.out.println("not implemented remapIdWithOutPrime  for tables ");
	 System.exit(1);
	 return null;	
	}
	@Override
	public Graph toGraph(Object current) {
		Graph g = new Graph(true /* directed */, false /* bottom-to-top */,
				true /* multi-links */);
		g.addNode("t");
		Table table=(Table)this.getNodeInverseCache((Integer)current);
		g.addNodeShape("t", "record");
		g.addNodeLabel("t", table.toGVizRecord(this));
		//g.launchViewer(1300, 770);
		return g;
		
	}
	
//	 the parameter is ParADD and the result is an ADD
    public Object doMinCallOverNodes(Object VDD,String NAME_FILE_CONTRAINTS,boolean pruneAfterEachIt) {
	            //TODO : working in it
   	    		Table table=(Table)this.getInverseNodesCache().get(VDD);
   	    		TreeSet varsNew=new TreeSet(table.vars);
   	    		ArrayList valuesNew=new ArrayList();
   	    		Iterator it=table.values.iterator();
   	    		while(it.hasNext()){
   	    			 Polynomial pol=(Polynomial) it.next();
//   	    		if the node not have probabilities then not call the nonlinear solver
   	    		     if (pol.getTerms().size()==0){
   	    		    	 valuesNew.add( pol.getC());   	    		    	 
   	    		     }
//   	    		else call the nonlinear solver
   	    		    else{
	   	    		    this.createFileAMPL(pol.toString(this,"p"), NAME_FILE_CONTRAINTS) ;
	   	    		    Double obj=callNonLinearSolver();
	    	    		if (obj==null){
	    	    			System.out.println("Problems with the solver");
	    	    			System.exit(0);
	    	    		}
	    	    		valuesNew.add( obj);
   	    		    } 
   	    		}
   	    		Table newTable=new Table(varsNew,valuesNew);
   	    		Integer idNew=this.getNextUnllocatedId();
   	 	   		this.putNodeCache(newTable, idNew);
   	 	   		return idNew;
   	    	
	}

	@Override
	public int contNumberNodes(Object idTable) {
		
		Table table=(Table)this.getInverseNodesCache().get(idTable);
		return table.values.size();
	}
	
	
	/////////////For factored RTDP BRTDP//////////////////////////

	//////////////For Enumerate RTDP BRTDP//////////////////////////////////////////////
	public void dump(Object valueiDD,String NAME_FILE_VALUE) {
		System.out.println(" dump was not implemented for ContextAditADD");
		System.exit(1);
	}

	@Override
	public Double getValueForStateInContext(Integer F, TreeMap<Integer, Boolean> state, Integer xiprime, Boolean valXiprime) {
		
		System.out.println("getValueForStateInContext with State as TreeMap parameter was not implemented for ContextTable");
		System.exit(1);
		return null;
	}
	@Override
	
	public Double getProbCPTForStateInContextEnum(Integer F,State state,Integer xiprime,Boolean valXiprime,int numVars){
		//compute the position of each variable in state into the CPT table that has first xiprime and then the other variables
		// and return the probabilitity
		Table table=(Table)inverseNodesCache.get(F);
		int posFinal=0;
		int firstIdVarState=state.getValues().keySet().iterator().next(); //it is necessary because state can have prime or non-prime variables
		for (int j=0;j<state.getValues().size();j++){
				
			Integer idVarState=firstIdVarState+j;
			Integer idVar=numVars+j+1;
			
			Boolean val=state.getValues().get(idVarState); //was j
			Integer pos=getPositionVar(idVar,table.vars); // because in the CPT the variables are non-prime
			if (pos!=null && val){
				posFinal=posFinal+((int)Math.pow(2,table.vars.size()-1-pos));
			}
		}
		//compute the position of xiprime variable
		if(xiprime!=null){
			Integer pos=getPositionVar(xiprime,table.vars);
			if (valXiprime){
				posFinal=posFinal+((int)Math.pow(2,table.vars.size()-1-pos));
			}
		}
		/*else{
			System.out.println("xiprime must be not null");
			System.exit(1);
			return null;
		}*/
		return (Double) table.values.get(posFinal);
	}
	
	public Double getRewardForStateInContextEnum(Integer F, State state, int numVars) {
		
		return getProbCPTForStateInContextEnum(F,state,null,null,numVars);
		
	}

	@Override
	public Object getValuePolyForStateInContext(Integer F, TreeMap<Integer, Boolean> state, Integer xiprime, Boolean valXiprime) {
		// TODO Auto-generated method stub
		System.out.println("Not implemented for Context Table");
		return null;
	}

	/* This method is not used because the reward can have less variables than the variables in the state
	@Override
	public Double getRewardForStateInContextEnum(Integer F, State state, int numVars) {
		   Table table=(Table)inverseNodesCache.get(F);
           int pos=state.getIdentifier();
           if (table.vars.size()!=numVars){
        	   System.out.println(table.vars);
        	   System.out.println("Reward table does not have all variables");
        	   System.exit(0);
           }
           return (Double) table.values.get(pos);
	
	
	}*/

	
}
