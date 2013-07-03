package add;
import graph.Graph;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import logic.lattice.Lattice;
import logic.lattice.Lattice.Concept;
import mdp.State;



import util.MapList;

public class ContextADD extends Context {
	//private Integer unllocatedIdApply;

    //APRICODD
    private Hashtable reduceRemapLeavesCache;
    
    private HashSet reduceInternal;
    private double currentError;
    
	public ContextADD() {
		unllocatedIdNode=new Integer(0);
		nodesCache=new Hashtable();
		inverseNodesCache=new Hashtable();
		reduceCache=new Hashtable();
		applyCache=new Hashtable();
		restrictCache=new Hashtable();
		remapIdCache=new Hashtable();
		remapIdCacheWithOut=new Hashtable();
		reduceRemapLeavesCache=new Hashtable();
		///Parameterized ADD
		this.labelsProdId=new Hashtable();
		this.inverseLabelsProdId=new Hashtable();
		this.unllocatedIdLabelProd=new Integer(0);
		this.cacheResultsSolver=new Hashtable();
		//for MDP
		this.cacheCont=new Hashtable();
		this.numCallNonLinearSolver=0;
		//for evaluator
		printCache=new Hashtable();
		reduceConvert=new Hashtable();
	}

	/*public String getInverseLabelsProdId(){
		return this.inverseLabelsProdId;
	}
	public Hashtable getLabelsProdId(){
		return this.labelsProdId;
	}*/
	
		
	/**
	 * put <node,id> in nodesCache and inverseNodesCache
	 **/
    public void putNodeCache(NodeKey node, Integer id){
    	this.nodesCache.put(node, id);
    	this.inverseNodesCache.put(id, node);
    }
    
    public void putReduceCache(Integer id1, Integer id2){
    	this.reduceCache.put(id1, id2);
    }
    public Hashtable getReduceCache(){
    	return reduceCache;
    }
    
    
    public Integer reduce(Integer F){
  
    	if(this.isTerminalNode(F)){
    		return F;
    	}
    	Integer Fr= (Integer) reduceCache.get(F);
    	if(Fr==null){
    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
    		Integer Fh=reduce(intNodeKey.getHigh());
    		Integer Fl=reduce(intNodeKey.getLower());
    		Integer Fvar= intNodeKey.getVar();
    		Fr=(Integer)GetNode(Fvar,Fh,Fl);
    		reduceCache.put(F, Fr);
    		
    	}
    	return Fr;
    }
    
    public Integer getNextUnllocatedId(){
    	this.unllocatedIdNode=new Integer(this.unllocatedIdNode.intValue()+1);
    	return this.unllocatedIdNode;
    }
    
    
    //Terminal node for ADD
    public Integer getTerminalNode(double fval){
	    Double fvalue=new Double(fval);
	    NodeKey tnodek=new TerminalNodeKeyADD(fvalue);
		//find the tnodek  
		Integer id=(Integer) this.nodesCache.get(tnodek); 
		if (id==null){
			id=this.getNextUnllocatedId();
			this.putNodeCache(tnodek,id);
		}
		return id;
    }
    //TODO:new part for parameterized
    //Terminal node for parameterizedADDs
    
    public Integer getTerminalNode(Polynomial polynomial){
        
        TerminalNodeKeyPar tnodek=new TerminalNodeKeyPar(polynomial);
    	//find the tnodek  
    	Integer id=(Integer) this.nodesCache.get(tnodek); 
    	if (id==null){
    		id=this.getNextUnllocatedId();
    		this.putNodeCache(tnodek,id);
    	}
    	return id;
        }
    //for create a simple polynomial only with one variable with its coefficient and without constant
    public Integer getTerminalNode(String varPro,Double coef){
    	Integer label1=this.getIdLabelProd(varPro);
    	Hashtable terms=new Hashtable();
    	terms.put(label1, coef);
    	Polynomial pol1= new  Polynomial(new Double(0),terms,this);
        return getTerminalNode(pol1);
    }
  //for create a simple polynomial only with one variable and without constant
    public Integer getTerminalNode(String varPro){
    	return getTerminalNode(varPro,1d);
    }
    
  
    /**
     * Return a Internal node if it is in the NodesCache else
     * make a new node and put it in the NodesCache 
     * @param fvar
     * @param fh
     * @param fl
     * @return
     */
    public Integer getInternalNode(Integer fvar, Object fh, Object fl){
    	if (fl==null ||fh==null){
    		System.out.println("some problem");
    	}
    	double max=Math.max(getMax((Integer)fl), getMax((Integer)fh));
    	double min=Math.min(getMin((Integer)fl), getMin((Integer)fh));
    	
    	NodeKey inodek=new InternalNodeKeyADD(fvar,(Integer)fh,(Integer)fl,max,min);
 		Integer id=(Integer) this.nodesCache.get(inodek);
 		if (id==null){ 
 		  id=this.getNextUnllocatedId();
 		  this.putNodeCache(inodek,id);
 		}
 		return id;
    }
    
    public double getMin(Integer node) {
    	
    	return ((NodeKey) this.inverseNodesCache.get(node)).getMin().doubleValue();
    }
    
    public double getMax(Integer node) {
    	
    	return ((NodeKey) this.inverseNodesCache.get(node)).getMax().doubleValue();
    }
    
	public Object GetNode(Integer fvar, Object fh, Object fl) {
		
		//if branches redundant, return child
		if(fl.equals(fh))
		   return fl;
		//make new node if not in cache
		Integer id=this.getInternalNode(fvar, fh, fl);
		return id;
	}

	
    public boolean isTerminalNode(Object f){
    	if (this.inverseNodesCache.get(f) instanceof TerminalNodeKey)
    		return true;
    	else
    		return false;    	
    }
    //TODO: new for Paramet
    public boolean isTerminalNodeADD(Object f){
    	if (this.inverseNodesCache.get(f) instanceof TerminalNodeKeyADD)
    		return true;
    	else
    		return false;    	
    }
    public boolean isTerminalNodePar(Object f){
    	if (this.inverseNodesCache.get(f) instanceof TerminalNodeKeyPar)
    		return true;
    	else
    		return false;    	
    }
    
    
    public NodeKey getNodeInverseCache(Integer f){
    	return (NodeKey) this.inverseNodesCache.get(f);
    }
    
    public Integer getIdCache(NodeKey nodek){
    	return (Integer) this.nodesCache.get(nodek);
    }
    /**
     * Apply the operation op to the operand represented as ADD
     * return  a Double 
     * @param f1 operand 1
     * @param op operator
     * @return
     */
    public Double apply(Object f1, UnaryOperationSimple op){
    	Double fr=op.computeResult(f1, this);
    	return fr;
    }
	
  //////////////////////APPLY METHODS BINARY OPERATIONS//////////  
    /**
     * Apply the operation op to two operands represented as ADD
     * and return the result ADD
     * @param f1 operand 1
     * @param f2  operand 2
     * @param op  operator: sum, prod, subs
     * @return
     */
	public Object apply(Object f1, Object f2, BinaryOperation op) {
		
		//check if result can be immediately computed
		Object fr=op.computeResult((Integer)f1,(Integer)f2,this);
		if (fr != null ){
			return fr;
		}
		//check if result already in apply cache
		fr=this.getIfIsInApplyCache((Integer)f1, (Integer)f2, op);
		Integer var=null,f1var=null,f2var=null;
	    if(fr==null){
	    	if(!this.isTerminalNode((Integer)f1)){
	    		if(!this.isTerminalNode((Integer)f2)){
	    			f1var=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f1)).getVar();
	    			f2var=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f2)).getVar();
	    			if (f1var.compareTo(f2var)<0){
	    				var=f1var;
	    			}
	    			else{
	    				var=f2var;
	    			}
	    		}
	    		else
	    		{   f1var=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f1)).getVar();
	    			var=f1var;
	    		}
	    	}
	    	else{
	    		f2var=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f2)).getVar();
		    	var=f2var;
	    	}
            //set up nodes for recursion
	    	if (var==null){ 
	    		System.out.println("Something wrong");
	    		System.exit(0);
	    	}
	    	Object flv1,fhv1,flv2,fhv2;
	    	if ((!this.isTerminalNode((Integer)f1))&& var.equals(f1var)){
	    	    flv1=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f1)).getLower();
	    	    fhv1=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f1)).getHigh();
	    	}
	    	else{
	    		flv1=fhv1=f1;
	    	}
	    	if ((!this.isTerminalNode((Integer)f2))&& var.equals(f2var)){
	    	    flv2=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f2)).getLower();
	    	    fhv2=((InternalNodeKeyADD)this.getNodeInverseCache((Integer)f2)).getHigh();
	    	}
	    	else{
	    		flv2=fhv2=f2;
	    	}	
	    	Object fl=this.apply(flv1, flv2, op);	
	    	Object fh=this.apply(fhv1, fhv2, op);
	    	fr=this.GetNode(var, (Integer)fh, (Integer)fl);
	    	//Put result in apply cache and return
	        this.putResultApplyCache((Integer)f1, (Integer)f2, op, (Integer)fr);    	
	    }
	    return fr;
	}

	
	
	public Hashtable getApplyCache(){
		return this.applyCache;
	}
	
	
	
	/* public Integer getNextUnllocatedIdApply(){
	    	this.unllocatedIdApply=new Integer(this.unllocatedIdApply.intValue()+1);
	    	return this.unllocatedIdApply;
	    }*/
	 public Integer getIfIsInApplyCache(Integer f1, Integer f2, BinaryOperation op){
		    
		    BinaryOperKeyADD nodeOp=new BinaryOperKeyADD(f1,f2,op);
			//find the tnodek in the tree 
			Integer id=(Integer) this.applyCache.get(nodeOp); 
			return id;
		    }
	 public void putResultApplyCache(Integer f1, Integer f2, BinaryOperation op, Integer fr){
		   BinaryOperKeyADD nodeOp=new BinaryOperKeyADD(f1,f2,op);
			this.applyCache.put(nodeOp,fr);
	 }
	
////////////RESTRICT METHODS////////////////
	
	 public Integer reduceRestrict(Integer idVar,UnaryOperationComplex op,Object id){
		 UnaryOperationCKey unaryKey=new UnaryOperationCKey(idVar,op,id);
		 Integer Fr= (Integer) this.restrictCache.get(unaryKey);
		 if (Fr!=null){
			 return Fr;
		 }
		 //boolean recurse =true; 
		 //find the node in the inverseNodeCache	 
		 NodeKey nodeKey=(NodeKey)inverseNodesCache.get(id);
		 //Double val=null;
		 if (nodeKey instanceof TerminalNodeKey){
			 return (Integer)id;
		 }
		 else{  //instanceof InternalNodeKey
			 Integer Fh= ((InternalNodeKeyADD)nodeKey).getHigh();
			 Integer Fl=((InternalNodeKeyADD)nodeKey).getLower();
			 Integer Fvar= ((InternalNodeKeyADD)nodeKey).getVar();
			 if(Fvar.compareTo(idVar)==0){
				 if (op == RESTRICT_HIGH){
					 return this.reduceRestrict(idVar, op, Fh);    			     
				 }	
				 else { //op==RESTRICT_LOW
					 return this.reduceRestrict( idVar, op, Fl);
				 }
			 }
			 // Get new low and high branches
			 else{
				 Integer FHigh=this.reduceRestrict(idVar, op, Fh);
				 Integer FLow=this.reduceRestrict(idVar, op, Fl);
			/*	 if (FHigh.compareTo(FLow)==0){
					 recurse=false;
					 // in this case GetNode return   Flow
				 }*/
				 Fr=(Integer)GetNode(Fvar,FHigh,FLow);//sera siempre un nodo interno, creo que no???
			 }

		 }	
		 this.restrictCache.put(unaryKey, Fr); 		
		 return Fr;			    		
	 }
	 		
 
		 
	 ///////////////VIEW METHODS//////////
	/* public void view(Integer current) {
			Graph g = toGraph(current);
			g.launchViewer(1300, 770);
		}*/
		
		
		public Graph toGraph(Object current)
		{
			Graph g = new Graph(true /* directed */, false /* bottom-to-top */,
					true /* multi-links */);
			this.getNodeInverseCache((Integer)current).toGraph(g,this);
			return g;
		}  

		
	/*	//preOrder algorithm for printTree.
		public void preorder(Integer current)
		{
			if (this.isTerminalNode(current)){
				System.out.println(((TerminalNodeKey) this.getNodeInverseCache(current)).getValue());
			}
			else
			{
				InternalNodeKey intNodeKey=(InternalNodeKey)this.getNodeInverseCache(current);
				System.out.println(intNodeKey.getVar());
				preorder(intNodeKey.getHigh());
				preorder(intNodeKey.getLower());
			}
		}
		*/
		////////////FOR MDP
			

       /**
        * Create a prime version of the ADD
        * @param F
        * @param hmPrimeRemap Maps non-prime IDs to their primed counterparts){
        * @return
        */
		
		public Object remapIdWithPrime(Object F, HashMap hmPrimeRemap)  { 
	    	
	    	if(this.isTerminalNode(F)){
	    		return F;
	    	}
	    	Object Fr=  remapIdCache.get(F);
	    	if(Fr==null){
	    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
	    		Object Fh=remapIdWithPrime(intNodeKey.getHigh(),hmPrimeRemap);
	    		Object Fl=remapIdWithPrime(intNodeKey.getLower(),hmPrimeRemap);
	    		Integer FvarPrime=(Integer) hmPrimeRemap.get(intNodeKey.getVar());
	    		Fr=GetNode(FvarPrime,Fh,Fl);
	    		remapIdCache.put(F, Fr);
	    	}
	    	return Fr;
	    }
		
		
		public Object remapIdWithOutPrime(Object F, HashMap hmPrime2IdRemap)  { 
	    	
	    	if(this.isTerminalNode(F)){
	    		return F;
	    	}
	    	Object Fr=  remapIdCacheWithOut.get(F);
	    	if(Fr==null){
	    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
	    		Object Fh=remapIdWithOutPrime(intNodeKey.getHigh(),hmPrime2IdRemap);
	    		Object Fl=remapIdWithOutPrime(intNodeKey.getLower(),hmPrime2IdRemap);
	    		Integer Fvar=(Integer) hmPrime2IdRemap.get(intNodeKey.getVar());
	    		Fr=GetNode(Fvar,Fh,Fl);
	    		remapIdCacheWithOut.put(F, Fr);
	    	}
	    	return Fr;
	    }


	//flushing 
	

	    public void copyInNewCacheNode(Object id) {
			if (inverseNodesCacheNew.containsKey(id)) {
				return;
			}
			Object node =  inverseNodesCache.get(id);
			
			if (node instanceof InternalNodeKeyADD) {
				Integer fh = ((InternalNodeKeyADD)node).getHigh();
				Integer fl = ((InternalNodeKeyADD)node).getLower();
				InternalNodeKeyADD nodeNew=new InternalNodeKeyADD(((InternalNodeKeyADD)node).getVar(),
						fh,fl,
		    			Math.max(getMax(fl), getMax(fh)),
		    			Math.min(getMin(fl), getMin(fh)));//to set max and min
				inverseNodesCacheNew.put(id, nodeNew);
				nodesCacheNew.put(nodeNew,id);
				copyInNewCacheNode(((InternalNodeKeyADD)node).getHigh());
				copyInNewCacheNode(((InternalNodeKeyADD)node).getLower());
			} else if (node instanceof TerminalNodeKey) {
				//TODO: new for Parameterized 
				TerminalNodeKey nodeNew=null;
				if (node instanceof TerminalNodeKeyADD){
				        nodeNew= new TerminalNodeKeyADD(((TerminalNodeKeyADD)node).getValue());
				}
				else if (node instanceof TerminalNodeKeyPar){
				        nodeNew= new TerminalNodeKeyPar(((TerminalNodeKeyPar)node).getPolynomial());
				}
				else{
					System.out.println("Erro in copyInNewCacheNode ADDContext: type of terminal node doesn't exist");
					System.exit(0);
				}
				inverseNodesCacheNew.put(id, nodeNew);
				nodesCacheNew.put(nodeNew,id);
				
			}
		}    
	    
	    //FOR APRICODD
	    
		public Integer pruneNodesValue(Object id, double mergeError) {
            this.mergeError=mergeError;
            System.out.println("mergeError:"+this.mergeError);
            reduceRemapLeavesCache=new Hashtable();
            reduceInternal=new HashSet();
			HashSet hsLeaves = new HashSet();
			collectLeavesADD(id, hsLeaves);
			HashMap finalMap = compressLeaves(hsLeaves);
			return reduceRemapLeaves(id, finalMap);
		}
	
		//reduceRemapLeaves only work when compressLeaves was called before
		//when the groups of nodes are formed
		private Integer reduceRemapLeaves(Object id, HashMap finalMap) {
			
			Integer Fr= (Integer) reduceRemapLeavesCache.get(id);
	    	if(Fr==null){
	    		NodeKey nodeKey=(NodeKey)inverseNodesCache.get(id);
	    		if(!this.isTerminalNode(id)){
	    		
		    		Integer Fh=reduceRemapLeaves(((InternalNodeKeyADD)nodeKey).getHigh(),finalMap);
		    		Integer Fl=reduceRemapLeaves(((InternalNodeKeyADD)nodeKey).getLower(),finalMap);
		    		Integer Fvar= ((InternalNodeKeyADD)nodeKey).getVar();
		    		Fr=(Integer)GetNode(Fvar,Fh,Fl);
		    		reduceRemapLeavesCache.put(id, Fr);		    	
		    	}
	    		else{// find terminal node in the finalMap
	    			TerminalNodeKey newNodeKey=(TerminalNodeKey)finalMap.get(nodeKey);
	    			if (newNodeKey!=null){
	    				Fr=(Integer) nodesCache.get(newNodeKey);
	    			}
	    			else{
	    				return (Integer)id;
	    			}
	    			
	    		}
	
	    		
	    	}
	    	return Fr;
		}
		
		
		public Integer pruneNodesValueVer2(Object id, double mergeError) {
            this.mergeError=mergeError;
            System.out.println("mergeError:"+this.mergeError);
            reduceRemapLeavesCache=new Hashtable();
            reduceInternal=new HashSet();
			ArrayList hsLeaves = new ArrayList();
			collectLeavesADD(id, hsLeaves);
			HashMap finalMap = compressLeavesVer2(hsLeaves);
			return reduceRemapLeavesVer2(id, finalMap);
		}
		
	    //find recursively in the FinalMap when the pair of nodes are formed
		private Integer reduceRemapLeavesVer2(Object id, HashMap finalMap) {
			Integer Fr= (Integer) reduceRemapLeavesCache.get(id);
	    	if(Fr==null){
	    		NodeKey nodeKey=(NodeKey)inverseNodesCache.get(id);
	    		if(!this.isTerminalNode(id)){
	    		
		    		Integer Fh=reduceRemapLeavesVer2(((InternalNodeKeyADD)nodeKey).getHigh(),finalMap);
		    		Integer Fl=reduceRemapLeavesVer2(((InternalNodeKeyADD)nodeKey).getLower(),finalMap);
		    		Integer Fvar= ((InternalNodeKeyADD)nodeKey).getVar();
		    		Fr=(Integer)GetNode(Fvar,Fh,Fl);
		    		reduceRemapLeavesCache.put(id, Fr);		    	
		    	}
	    		else{// find terminal node in the finalMap recursively
	    			TerminalNodeKey newNodeKey=findNodeInFinalMapRec(finalMap,(TerminalNodeKey)nodeKey);
	    			if (newNodeKey!=null){
	    				Fr=(Integer) nodesCache.get(newNodeKey);
	    			}
	    			else{
	    				return (Integer)id;
	    			}
	    			
	    		}
	    	}
	    	return Fr;
		}

		private TerminalNodeKey findNodeInFinalMapRec(HashMap finalMap, TerminalNodeKey nodeKey) {
			while(finalMap.get(nodeKey)!=null){
				
				nodeKey=(TerminalNodeKey)finalMap.get(nodeKey);
			}
			return nodeKey;
		}
		
		private HashMap compressLeavesVer2(ArrayList array) {
			HashMap finalMap = new HashMap();
			 while(array.size()>=2 && this.mergeError>0){  
				  TerminalNodeKey nodeToMerge=(TerminalNodeKey)array.get(0);
				  findParPrune(nodeToMerge, array, finalMap);
			 }		
			return finalMap;
		}
		
		private boolean findParPrune(TerminalNodeKey nodeToMerge, ArrayList array, HashMap finalMap) {
			 
             array.remove(0);
             for(int i=0; i<array.size();i++){
            	 boolean merged=mergeTerminalNode(nodeToMerge,(TerminalNodeKey)array.get(i),i,finalMap,array);
            	 if (merged==true) return true;
             }
             return false;
              
		}

		private boolean mergeTerminalNode(TerminalNodeKey nodeToMerge, TerminalNodeKey otherNode, int i,HashMap finalMap, ArrayList array) {
			double avg,totalErro;
			if (nodeToMerge instanceof TerminalNodeKeyADD){
				avg=(((TerminalNodeKeyADD)nodeToMerge).getValue().doubleValue() + ((TerminalNodeKeyADD)otherNode).getValue().doubleValue())/2;
				totalErro= Math.abs(((TerminalNodeKeyADD)nodeToMerge).getValue().doubleValue()-avg)+Math.max(nodeToMerge.errorMergeNode,otherNode.errorMergeNode);
				
				if (totalErro <= this.mergeError) {
					this.mergeError=this.mergeError-totalErro;
					TerminalNodeKeyADD newNode= (TerminalNodeKeyADD) this.inverseNodesCache.get(this.getTerminalNode(avg));
					newNode.errorMergeNode=totalErro;
					finalMap.put(nodeToMerge, newNode);
					finalMap.put(otherNode, newNode);
					array.remove(i);
					array.add(newNode);
					return true;
				}
			}
			if (nodeToMerge instanceof TerminalNodeKeyPar){
				Polynomial avgPol=((TerminalNodeKeyPar)nodeToMerge).getPolynomial().avgPolynomial(((TerminalNodeKeyPar)otherNode).getPolynomial());
				totalErro=((TerminalNodeKeyPar)nodeToMerge).getPolynomial().evaluateWith1Abs(avgPol)+Math.max(nodeToMerge.errorMergeNode,otherNode.errorMergeNode);
				if (totalErro <= this.mergeError) {
					this.mergeError=this.mergeError-totalErro;
					TerminalNodeKeyPar newNode= (TerminalNodeKeyPar) this.inverseNodesCache.get(this.getTerminalNode(avgPol));
					newNode.errorMergeNode=totalErro;
					finalMap.put(nodeToMerge, newNode);
					finalMap.put(otherNode, newNode);
					array.remove(i);
					array.add(newNode);
					return true;
				}
			}
			return false;
		}

		public void collectLeavesADD(Object id, ArrayList array) {
			Object node=this.inverseNodesCache.get(id);
			if(node instanceof TerminalNodeKey){
				((TerminalNodeKey)node).errorMergeNode=0;
				if (!array.contains(node)){
					array.add(node);
				}
			} 
			else {//internal node
				if(!reduceInternal.contains(id)){
					InternalNodeKeyADD nodeInternal = (InternalNodeKeyADD) node;
					collectLeavesADD(nodeInternal.getHigh(), array);
					collectLeavesADD(nodeInternal.getLower(), array);
					reduceInternal.add(id);
				}
			}
		}
		
		

		/**
	     * Collect all Terminal Nodes of the ADD identified
	     * by id and put it in the hsLeaves
	     * @param id
	     * @param hsLeaves
	     */
		public void collectLeavesADD(Object id, HashSet hsLeaves) {
			Object node=this.inverseNodesCache.get(id);
			if(node instanceof TerminalNodeKey){
				hsLeaves.add(node);
			} 
			else {//internal node
				if(!reduceInternal.contains(id)){
					InternalNodeKeyADD nodeInternal = (InternalNodeKeyADD) node;
					collectLeavesADD(nodeInternal.getHigh(), hsLeaves);
					collectLeavesADD(nodeInternal.getLower(), hsLeaves);
					reduceInternal.add(id);
				}
			}
		}
		
		
		
		
		/**
		 * Create a new hash with nodeOld-> avgNode
		 * @param hsLeaves
		 * @return
		 */
		public HashMap compressLeaves(HashSet hsLeaves) {

			ArrayList groupNodes = new ArrayList();
			MapList remapGroupWithNode = new MapList();
			HashMap finalMap = new HashMap();
			//creating the groups of nodes
			Iterator i = hsLeaves.iterator();
			groupNodes.clear();
			if (i.hasNext()){
				TerminalNodeKey first= (TerminalNodeKey)i.next();
				if (first instanceof TerminalNodeKeyADD){
					if (((TerminalNodeKeyADD)first).getValue().compareTo(ZERO)==0){
						finalMap.put(first, first);
					}
					else{
						groupNodes.add(first);
					}
				}
				else if(first instanceof TerminalNodeKeyPar){
					if (((TerminalNodeKeyPar)first).getPolynomial().equals(ZEROPOLYNOMIAL)){
						finalMap.put(first, first);
					}
					else{
						groupNodes.add(first);
					}
						
				}
				
			}

			boolean findGroup;
			while (i.hasNext()) {
				findGroup=false;
				TerminalNodeKey node1 = (TerminalNodeKey) i.next();
				if (node1 instanceof TerminalNodeKeyADD && ((TerminalNodeKeyADD)node1).getValue().compareTo(ZERO)==0){
					finalMap.put(node1, node1);
					findGroup=true;
				}
				if (node1 instanceof TerminalNodeKeyPar && ((TerminalNodeKeyPar)node1).getPolynomial().equals(ZEROPOLYNOMIAL)){
					finalMap.put(node1, node1);
					findGroup=true;
				}
				
				
				Iterator j = groupNodes.iterator();
				while(findGroup == false && j.hasNext()) {
					TerminalNodeKey groupNode = (TerminalNodeKey) j.next();
					if (groupNode instanceof TerminalNodeKeyADD){
						if (Math.abs(((TerminalNodeKeyADD)node1).getValue().doubleValue() - ((TerminalNodeKeyADD)groupNode).getValue().doubleValue()) <= this.mergeError/2) {
							remapGroupWithNode.putValue(groupNode,node1); 
							findGroup = true;
						}
					}
					if (groupNode instanceof TerminalNodeKeyPar){
						if (Math.abs(((TerminalNodeKeyPar)node1).getPolynomial().evaluateWith1Abs(((TerminalNodeKeyPar)groupNode).getPolynomial())) <= this.mergeError/2) {
							remapGroupWithNode.putValue(groupNode,node1); 
							findGroup = true;
						}
					}

				}
				if (findGroup == false)
					groupNodes.add(node1);
			}
			//computing the avg and create the finalMap
			finalMap=computeAvgCreateFinalMap(groupNodes,remapGroupWithNode);
			return finalMap;
		}
		private HashMap computeAvgCreateFinalMap(ArrayList groupNodes, MapList remapGroupWithNode) {
			Iterator i = groupNodes.iterator();
			HashMap finalMap = new HashMap();
			while (i.hasNext() && this.mergeError>0) {
				TerminalNodeKey groupNode = (TerminalNodeKey) i.next();
				ArrayList subGroup = remapGroupWithNode.getValues(groupNode);
				TerminalNodeKey newNode;
				if(!this.workingWithParameterized){
					newNode=computeAvgADD(groupNode,subGroup);
				}
				else{
					newNode=computeAvgPar(groupNode,subGroup);
				}
				finalMap.put(groupNode, newNode);
				Iterator k = subGroup.iterator();
				while (k.hasNext()){
					finalMap.put(((TerminalNodeKey) k.next()), newNode);
				}
				this.mergeError=this.mergeError-this.currentError;
			}
			return finalMap;

		}

		private TerminalNodeKeyPar computeAvgPar(TerminalNodeKey groupNode, ArrayList subGroup) {
			 Polynomial sumGroup = new Polynomial(((TerminalNodeKeyPar)groupNode).getPolynomial(),this);
			Iterator j = subGroup.iterator();
			while(j.hasNext()) {
				TerminalNodeKeyPar n = (TerminalNodeKeyPar) j.next();
				sumGroup=sumGroup.sumPolynomial(n.getPolynomial());					
			}
			Polynomial avg=sumGroup.divSimplePolynomial((double) (1 + subGroup.size()));
			
			TerminalNodeKeyPar newNode= (TerminalNodeKeyPar) this.inverseNodesCache.get(this.getTerminalNode(avg));
//			calculating the error
//			TODO: working in it
			
			double error=((TerminalNodeKeyPar)groupNode).getPolynomial().evaluateWith1Abs(avg);
			Iterator k = subGroup.iterator();
			while(k.hasNext()) {
				TerminalNodeKeyPar n = (TerminalNodeKeyPar) k.next();
				double oneError=n.getPolynomial().evaluateWith1Abs(avg);
				if (Math.max(error, oneError)==oneError){
					error=oneError;
				}	
			}
			this.currentError=error;	
			return newNode;
		}

		private TerminalNodeKeyADD computeAvgADD(TerminalNodeKey groupNode, ArrayList subGroup) {
			double sumGroup = ((TerminalNodeKeyADD)groupNode).getValue();
			Iterator j = subGroup.iterator();
			while(j.hasNext()) {
				TerminalNodeKeyADD n = (TerminalNodeKeyADD) j.next();
				sumGroup=sumGroup+n.getValue();					
			}
			double avg=sumGroup/(double) (1 + subGroup.size());
			TerminalNodeKeyADD newNode= (TerminalNodeKeyADD) this.inverseNodesCache.get(this.getTerminalNode(avg));
			//calculating the error
			double error=Math.abs(((TerminalNodeKeyADD)groupNode).getValue()-avg);
			Iterator k = subGroup.iterator();
			while(k.hasNext()) {
				TerminalNodeKeyADD n = (TerminalNodeKeyADD) k.next();
				double oneError=Math.abs(n.getValue()-avg);
				if (Math.max(error, oneError)==oneError){
					error=oneError;
				}	
				
			}
			this.currentError=error;	
			return newNode;
		}

	

	     
	 

		//For  Generating problems
	 	public void dumpToTree(Integer id, String var_prefix, PrintWriter ps,DecimalFormat df, int level) {
            NodeKey node =this.getNodeInverseCache(id);
	 		if (node instanceof InternalNodeKeyADD){
	 			InternalNodeKeyADD interNode=(InternalNodeKeyADD)node;
	 			ps.print("\n" + tab(level) + "(" + var_prefix + interNode.getVar() + " ");
	 			dumpToTree(interNode.getHigh(), var_prefix, ps, df, level + 1);
	 			dumpToTree(interNode.getLower(), var_prefix, ps, df, level + 1);
	 			ps.print(") ");
	 			
	 		}
	 		else{
	 			if(!this.workingWithParameterized){
		 			TerminalNodeKeyADD d = (TerminalNodeKeyADD) node;
		 			ps.print("(" + df.format(d.getValue()) + ") ");
	 			}
	 			else{
	 				TerminalNodeKeyPar d = (TerminalNodeKeyPar) node;
		 			ps.print("([" + d.getPolynomial().toStringWithOutC(this) + "]) ");
	 			}
	 				
	 		}
	 	}
	 	//For Generating traffic problems
	 	
	 	
        public void dumpToTree(int id, Map names, PrintWriter ps, DecimalFormat df, int level) {

        	NodeKey node =this.getNodeInverseCache(id);
        	//ADDNode node = getNode(id);
    		if (node instanceof InternalNodeKeyADD){
        //if (node instanceof ADDINode) {
    			InternalNodeKeyADD interNode=(InternalNodeKeyADD)node;
                //ADDINode i = (ADDINode) node;
                ps.print("\n" + tab(level) + "(" + names.get(interNode.getVar()) + " ");//names.get(i._nGlobalID) 
                dumpToTree(interNode.getHigh(), names, ps, df, level + 1);
                dumpToTree(interNode.getLower(), names, ps, df, level + 1);
                ps.print(") ");
        } else {
        	if(!this.workingWithParameterized){     	
        		TerminalNodeKeyADD d = (TerminalNodeKeyADD) node;
                ps.print("(" + df.format(d.getValue()) + ") ");
        	}
 			else{
 				TerminalNodeKeyPar d = (TerminalNodeKeyPar) node;
	 			ps.print("([" + d.getPolynomial().toStringWithOutC(this) + "]) ");
 			}
        	
        }
}


	 	public String tab(int len) {
			StringBuffer sb = new StringBuffer("   ");
			for (int i = 0; i < len; i++, sb.append("   "))
				;
			return sb.toString();
		}

		@Override
		public int contNumberNodes(Object F) {
			    this.cacheCont.clear();
			    //contLeaf=0;
		        Integer id=reduceCacheCont((Integer)F);
		        return cacheCont.size();
		}

		private Integer reduceCacheCont(Integer F) {
		 	if(this.isTerminalNode(F)){
		 		//System.out.println(((TerminalNodeKeyADD)this.inverseNodesCache.get(F)).getValue());
		 		Integer idTerminalNode= (Integer) cacheCont.get(F);
		 		if(idTerminalNode==null){
		 		    // contLeaf=contLeaf+1;
		 		  cacheCont.put(F,0);
		 		}
	    		return F;
	    	}
	    	Integer Fr= (Integer) cacheCont.get(F);
	    	if(Fr==null){
	    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
	    		Integer Fh=reduceCacheCont(intNodeKey.getHigh());
	    		Integer Fl=reduceCacheCont(intNodeKey.getLower());
	    		Integer Fvar= intNodeKey.getVar();
	    		Fr=(Integer)GetNode(Fvar,Fh,Fl);
	    		cacheCont.put(F, Fr);
	    		
	    	}
	    	return Fr;
			
		}	
		
	    public Object doMaxCallOverNodes(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt) {

			if (this.isTerminalNode(VDD)) { 
				TerminalNodeKeyPar node = (TerminalNodeKeyPar) this.getInverseNodesCache().get(VDD);
				
				Polynomial polynomial = node.getPolynomial(); 
				
				if (polynomial.getTerms().size() == 0)
					return this.getTerminalNode(polynomial.getC());
				
				/////////////////////OBJECTIVE-IP PRUNE/////////////////////////////////////////////
				if (pruneAfterEachIt)
					return callNonLinearSolverObjectiveIP(node, NAME_FILE_CONTRAINTS);
				//////////////////////////////////////////////////////////////////////////////////////
				else { 
					//////Call solver with the polynomial//////////////////////////////////////////
					createFileAMPL(polynomial.toString(this,"p"), NAME_FILE_CONTRAINTS, "max");
					
					Double obj = callNonLinearSolver();
					
					//after this I have the currentValuesProb
					contNoReuse++;

					if (obj == null) {
						System.out.println("doMaxCallOverNodes: Problems with the solver it return null");
						System.exit(0);
					}

					return this.getTerminalNode(obj);
				}
			}

			/////////////////////recursive call for each ADD branch////////////////////////////////////////////
			Integer Fr = (Integer) reduceCacheMaxPar.get(VDD);

			if (Fr == null) {
				InternalNodeKey intNodeKey = (InternalNodeKey) this.getInverseNodesCache().get(VDD);
				Object Fh = doMaxCallOverNodes(intNodeKey.getHigh(), NAME_FILE_CONTRAINTS, pruneAfterEachIt);
				Object Fl = doMaxCallOverNodes(intNodeKey.getLower(), NAME_FILE_CONTRAINTS, pruneAfterEachIt);
				Integer Fvar = intNodeKey.getVar();
				Fr = (Integer) this.GetNode(Fvar, Fh, Fl);
				reduceCacheMaxPar.put(VDD, Fr);
			} else	{
				reuseCacheIntNode++;
			}
 
			///////////////////////////////////////////////////////////////////////////////////////////////////
			return Fr;//could be integer or AditArc
	    }
		
//		 the parameter is ParADD and the result is an ADD
	    public Object doMinCallOverNodes(Object VDD,String NAME_FILE_CONTRAINTS,boolean pruneAfterEachIt) {

	    	 if(this.isTerminalNode(VDD)){ 
	    		 TerminalNodeKeyPar node=(TerminalNodeKeyPar)this.getInverseNodesCache().get(VDD);
	    		 if(node.getPolynomial().getTerms().size()==0){ //if the node does not have probabilities then we do not call the nonlinear solver
	    			 return this.getTerminalNode(node.getPolynomial().getC());
	    		 }
	    		 /////////////////////OBJECTIVE-IP PRUNE/////////////////////////////////////////////
	    		 if (pruneAfterEachIt){
	    			 return callNonLinearSolverObjectiveIP(node, NAME_FILE_CONTRAINTS);
	    		 }
	    		 //////////////////////////////////////////////////////////////////////////////////////
	    		 else{ //////Call solver with the polynomial//////////////////////////////////////////
	    			 createFileAMPL(node.getPolynomial().toString(this,"p"),NAME_FILE_CONTRAINTS);
	    			 Double obj=callNonLinearSolver();
	    			 //after this I have the currentValuesProb
	    			 contNoReuse++;
	    			 if (obj==null){
	    				 System.out.println("doMinCallOverNodes: Problems with the solver it return null");
	    				 System.exit(0);
	    			 }
	    			 return this.getTerminalNode(obj);
	    		 }
	    	 }
	    	 /////////////////////recursive call for each ADD branch////////////////////////////////////////////
	    	 Integer Fr = (Integer) reduceCacheMinPar.get(VDD);
	    	 if (Fr == null){
	    		 InternalNodeKey intNodeKey = (InternalNodeKey) this.getInverseNodesCache().get(VDD);
	    		 Object Fh = doMinCallOverNodes(intNodeKey.getHigh(),NAME_FILE_CONTRAINTS,pruneAfterEachIt);
	    		 Object Fl = doMinCallOverNodes(intNodeKey.getLower(),NAME_FILE_CONTRAINTS,pruneAfterEachIt);
	    		 Integer Fvar= intNodeKey.getVar();
	    		 Fr=(Integer)this.GetNode(Fvar,Fh,Fl);
	    		 reduceCacheMinPar.put(VDD, Fr);
	    	 } else	{
	    		 reuseCacheIntNode++;
	    	 }
	    	 
	    	 ///////////////////////////////////////////////////////////////////////////////////////////////////
	    	 return Fr;//could be integer or AditArc
	     }
	
	    private Object callNonLinearSolverObjectiveIP(TerminalNodeKeyPar node, String name_file_contraints) {
	    	return this.callNonLinearSolverObjectiveIP(node, name_file_contraints, "min");
	    }
	    
		private Object callNonLinearSolverObjectiveIP(TerminalNodeKeyPar node, String name_file_contraints, String optimization) {
//			 construct a hashtable from the polynomial
			 ArrayList currentIdsClash=new ArrayList();
			 currentDirectionList=node.getPolynomial().constructDirectionList(listVarProb,this,currentIdsClash);//it is necessary to calculate currentIdsClash
			 //System.out.println(node.getPolynomial().toString(this));
			 /////////// record how much of the error budget this consumed
			 Polynomial newPolAprox=null;
			 if(this.typeAproxPol==1){
				 newPolAprox=node.getPolynomial().aproxPol_Upper_Lower_OnlyProbClash(listVarProb, this, currentIdsClash, mergeError/2d);
			 }
			 else{
				 newPolAprox=node.getPolynomial().aproxPol(listVarProb, this, currentIdsClash, mergeError/2d);	
			 }
			 if(newPolAprox.getTerms().size()==0){
				 numberReducedToValue++;
				 return this.getTerminalNode(newPolAprox.getC());
			 }
			 if(newPolAprox.currentError>mergeError/2d){
				 System.out.println("Error:   "+newPolAprox.currentError+mergeError/2d);
				 System.out.println("IT MUST NOT HAPPEN: doMinCallOverNodes");
				 System.exit(0);
			 }
			 createFileAMPL(newPolAprox.toString(this,"p"),name_file_contraints, optimization);
			 //createFileAMPL(node.getPolynomial().toString(this),NAME_FILE_CONTRAINTS);

			 Double obj=callNonLinearSolver();  			 //after this we have the currentValuesProb
			 contNoReuse++;
			 if (obj==null){
				 System.out.println("doMinCallOverNodes: Problems with the solver it return null");
				 System.exit(0);
			 }
			 return this.getTerminalNode(obj);
			
		}

		private ArrayList getArrayDiff(String currDir, String name) {
			ArrayList arrayDiff=new ArrayList();
			int index= IndexOfDifference(currDir,name);
			Object arrayVarProb[]=this.listVarProb.toArray();
			arrayDiff.add(arrayVarProb[index]);
			return arrayDiff;
		}

		private int IndexOfDifference(String str1, String str2) {

			if (str1 == str2) {
				return -1;
			}
			if (str1 == null || str2 == null) {
				return 0;
			}
			int i;
			for (i = 0; i < str1.length() && i < str2.length(); ++i) {
				if (str1.charAt(i) != str2.charAt(i)) {
					break;
				}
			}
			if (i < str2.length() || i < str1.length()) {
				return i;
			}
			return -1;

			
		}

		private String createStringCurDir(Hashtable currentDirectionList) {
			String currDir="";
		 Iterator it=listVarProb.iterator();
		 while(it.hasNext()){
				currDir=currDir+(String)currentDirectionList.get(it.next());
			}
			return currDir;
		} 
	     
		////////////////////For factored RTDP BRTDP///////////////////////////////////////////////
		public  Object getValuePolyForStateInContext(Integer F,TreeMap<Integer, Boolean> state,Integer xiprime,Boolean valXiprime){ //New method for RTDPIP

			if(isTerminalNode(F)){
				Object node =  inverseNodesCache.get(F);
				if (node instanceof TerminalNodeKeyADD){
			        return ((TerminalNodeKeyADD)node).getValue(); //the result is a Double
			    }
			    else if (node instanceof TerminalNodeKeyPar){
			        return ((TerminalNodeKeyPar)node).getPolynomial(); // the result is a poly
			    }
				
				//return ((TerminalNodeKeyADD)inverseNodesCache.get(F)).getValue(); //the result is a Double
			}
			InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
			Integer Fvar= intNodeKey.getVar();
			Boolean val=state.get(Fvar);
			if(val==null && Fvar!=null && Fvar.compareTo(xiprime)==0){
				val=valXiprime;
			}
			if (val==null){
				System.out.println("There is not  variable Fvar ");
				System.exit(0);
			}
			Object leafValue=null;
			if (val==true){
				leafValue=getValuePolyForStateInContext(intNodeKey.getHigh(),state,xiprime,valXiprime);
			}
			else if (val==false){
				leafValue=getValuePolyForStateInContext(intNodeKey.getLower(),state,xiprime,valXiprime);
			}

			return leafValue;
		}

		public  Double getValueForStateInContext(Integer F,TreeMap<Integer, Boolean> state,Integer xiprime,Boolean valXiprime){

			if(isTerminalNode(F)){
				return ((TerminalNodeKeyADD)inverseNodesCache.get(F)).getValue(); //the result is a Double
			}
			InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
			Integer Fvar= intNodeKey.getVar();
			Boolean val=state.get(Fvar);
			if(val==null && Fvar!=null && Fvar.compareTo(xiprime)==0){
				val=valXiprime;
			}
			if (val==null){
				System.out.println("There is not  variable Fvar ");
				System.exit(0);
			}
			Double leafValue=null;
			if (val==true){
				leafValue=getValueForStateInContext(intNodeKey.getHigh(),state,xiprime,valXiprime);
			}
			else if (val==false){
				leafValue=getValueForStateInContext(intNodeKey.getLower(),state,xiprime,valXiprime);
			}

			return leafValue;
		}

		public void dump(Object valueiDD,String NAME_FILE_VALUE) {
			try {
	     		
	             BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_VALUE));

	             String  valueiDDString=getString((Integer)valueiDD);        
	             out.write(valueiDDString);
	             out.append(System.getProperty("line.separator"));
	             
	             out.close();
	         } catch (IOException e) {
	         	System.out.println("Problem with the creation Value file");
	         	System.exit(0);
	         }
		}

		@Override
		public Double getProbCPTForStateInContextEnum(Integer F, State state, Integer xiprime, Boolean valXiprime,int numVars) {
			System.out.println("getValueForStateInContext with State parameter was not implemented for ContextADD");
			System.exit(1);
			return null;
		}

		@Override
		public Double getRewardForStateInContextEnum(Integer F, State state, int numVars) {
			System.out.println("getRewardForStateInContext with State parameter was not implemented for ContextADD");
			System.exit(1);
			return null;
			
		}
		  		 	
}
