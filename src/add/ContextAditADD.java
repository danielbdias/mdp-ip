package add;
import graph.Graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import javax.activity.InvalidActivityException;

import mdp.State;

public class ContextAditADD extends Context {
	
	//private Hashtable nodesCache;
	//private Hashtable inverseNodesCache;
	//private Hashtable reduceCache;
	
	//private Hashtable applyCache;
	private Integer unllocatedIdApply;
	
	//private Hashtable restrictCache;  //< UnaryOperationKey> -> AditArc
	
	//for pruneNodes
	TreeMap idVar2Ids;
	private double currentError;
	private Hashtable id2NewId;
	private Hashtable reduceCachePrune;
	//private Hashtable remapIdCache;
	
	
//	 Nodes (and children) to keep when flushing caches
    //private HashSet hsSpecialNodes = new HashSet();
    //private Hashtable nodesCacheNew=new Hashtable();
	//private Hashtable inverseNodesCacheNew=new Hashtable();
	
public ContextAditADD() {
		
		this.unllocatedIdNode=new Integer(0);
		this.unllocatedIdApply=new Integer(0);
		//this.unllocatedIdRestrict=new Integer(0);
		nodesCache=new Hashtable();
		inverseNodesCache=new Hashtable();
		reduceCache=new Hashtable();
		applyCache=new Hashtable();
		restrictCache=new Hashtable();
		remapIdCache=new Hashtable();
		maxCache=new Hashtable();
		//reduceRemapLeavesCache=new Hashtable();
		
		///Parameterized ADD
		this.labelsProdId=new Hashtable();
		this.inverseLabelsProdId=new Hashtable();
		this.unllocatedIdLabelProd=new Integer(0);
		this.cacheCont=new Hashtable();
			
	}



public Hashtable getMaxCache(){
	return this.maxCache;
}
public Hashtable getInverseNodesCache(){
	return this.inverseNodesCache;
}
	
/**
 * put <Aditnode,id> in nodesCache and inverseNodesCache
 **/
public void putNodeCache(NodeKey node, Integer id){
	this.nodesCache.put(node, id);
	this.inverseNodesCache.put(id, node);
}

/*public void putReduceCache(Integer id1, Integer id2){
	this.reduceCache.put(id1, id2);
}*/

public AditArc reduce(AditArc arc){
	  
	if(this.isTerminalNode(arc)){
		return arc;
	}
	//was reduced before
	AditArc arcR= (AditArc) reduceCache.get(arc.F);
	//else
	if(arcR==null){
		InternalNodeKeyAdit aditNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(arc.F);
		AditArc ArcFh=reduce(aditNodeKey.getHigh());
		AditArc ArcFl=reduce(aditNodeKey.getLower());
		Integer Fvar= aditNodeKey.getVar();
		arcR=GetNode(Fvar,ArcFh,ArcFl);
		reduceCache.put(arc.F, arcR);
	} 

	// Need to add in the offset of Arc (which we have not used yet)
	AditArc arcRCopy=new AditArc(arcR.c+arc.c,arcR.F);
	
	//arcR.c += arc.c;
	return arcRCopy;
}

public Integer getNextUnllocatedId(){
	this.unllocatedIdNode=new Integer(this.unllocatedIdNode.intValue()+1);
	return this.unllocatedIdNode;
}

/**
 * Return a Internal node if it is in the NodesCache else
 * make a new node and put it in the NodesCache 
 * @param fvar
 * @param fh
 * @param fl
 * @return
 */
public AditArc getInternalNode(Integer fvar, Object ArcFh, Object ArcFl){
	    double rmin=Math.min(((AditArc)ArcFh).c.doubleValue(), ((AditArc)ArcFl).c.doubleValue());
	    //TODO: borrar esta parte que es para verificar si alguna vez el polinomio tiene c diferente de 0
	    //if(this.isTerminalNodePar(ArcFh)&& ((InternalNodeKeyAdit)this.getNodeCache((Integer)ArcFh)).)
	    
	    double ch= ((AditArc)ArcFh).c.doubleValue()-rmin;
	    double cl= ((AditArc)ArcFl).c.doubleValue()-rmin;
	        
	    InternalNodeKeyAdit inodek=new InternalNodeKeyAdit(fvar,ch,cl, ((AditArc)ArcFh).F,((AditArc)ArcFl).F);
	    Integer id=(Integer) this.nodesCache.get(inodek);
		if (id==null){ 
		  id=this.getNextUnllocatedId();
		  this.putNodeCache(inodek,id);
		}
		AditArc result = new AditArc(rmin,id);
		return result;
}
public AditArc getTerminalNode(double c){
	
	return new AditArc(new Double(c),new Integer(0));
}


//for create a simple polynomial only with one variable with its coefficient and without constant
public AditArc getTerminalNode(String varPro,Double coef){
	//TODO: For ParAditArc
  	Integer label1=this.getIdLabelProd(varPro);
	Hashtable terms=new Hashtable();
	terms.put(label1, coef);
	Polynomial pol1= new  Polynomial(new Double(0),terms,this);
    return getTerminalNode(pol1);
}
//for create a simple polynomial only with one variable and without constant
public AditArc getTerminalNode(String varPro){
	//TODO: For ParAditArc
	return getTerminalNode(varPro,1d);
}


public AditArc getTerminalNode(Polynomial polynomial){
	//TODO: For ParAditArc
	Polynomial newPolynomial=new Polynomial(0.0,polynomial.terms,this);
	TerminalNodeKeyPar tnodek=new TerminalNodeKeyPar(newPolynomial);
  	//find the tnodek  
  	Integer id=(Integer) this.nodesCache.get(tnodek); 
  	if (id==null){
  		id=this.getNextUnllocatedId();
  		this.putNodeCache(tnodek,id);
  	}
  	return new AditArc(new Double(polynomial.c),id);
}

public AditArc getTerminalNode(Double cr,Polynomial polynomial){
	//TODO: For ParAditArc
	polynomial.c=polynomial.c+cr;
	return getTerminalNode(polynomial);
	
}

public AditArc GetNode(Integer fvar, Object ArcFh, Object ArcFl) {
	
	//if branches redundant, return child
	if(((AditArc)ArcFh).F.equals(((AditArc)ArcFl).F)&& (Math.abs(((AditArc)ArcFh).c -((AditArc)ArcFl).c) <= 1e-10d)) {
		
	   return (AditArc)ArcFl;
	}
	//make new node if not in cache
	AditArc  arc=this.getInternalNode(fvar, ArcFh, ArcFl);
	
	return arc;
}



public boolean isTerminalNode(Object A){
	if  (isTerminalNodeAditADD(A)||isTerminalNodePar(A)){
		return true;
	}
	return false;
}
//TODO: new for Paramet

public boolean isTerminalNodeAditADD(Object A){
	return (((AditArc)A).F.compareTo(new Integer(0))==0);
}
public boolean isTerminalNodePar(Object A){
	if (this.inverseNodesCache.get(((AditArc)A).F) instanceof TerminalNodeKeyPar)
		return true;
	else
		return false;    	
}






public NodeKey getNodeInverseCache(Integer f){
	return (NodeKey) this.inverseNodesCache.get(f);
}

///////////////VIEW METHODS//////////

	
	
	public Graph toGraph(Object current)
	{
		Graph g = new Graph(true /* directed */, false /* bottom-to-top */,
				true /* multi-links */);
		//((AditNodeKey) this.getNodeInverseCache(current.F)).toGraph(g,this);  //id->AditNodeKey
		   ((AditArc)current).toGraph(g,this, "ROOT");  //id->AditNodeKey
		return g;
	}

	@Override
	public Integer getIdCache(NodeKey nodek) {
		if (nodek == null) return 0;
		return (Integer) this.nodesCache.get(nodek);
	
	}
	/*public InternalNodeKeyAdit getNodeCache(Integer id) {
		
		return (InternalNodeKeyAdit) this.inverseNodesCache.get(id);
	
	}	*/
	
//////////////////////APPLY METHODS UNARY OPERATIONS//////////  
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
     * @return  AditArc
     */
	public Object apply(Object f1, Object f2, BinaryOperation op) {
		
		//System.out.println("Apply: " + f1 + ", " + f2 + ", " + op);
		  
	

		
		//check if result can be immediately computed
		AditArc fr=op.computeResult((AditArc)f1,(AditArc)f2,this);
		if (fr != null ){
		
	 

			return fr;
		}
		//get normalized key
		 AditArcPair arcPair= getNormCacheKey((AditArc)f1,(AditArc)f2,op);
		//check if result already in apply cache
		 
		fr=this.getIfIsInApplyCache(arcPair.Arc1, arcPair.Arc2, op);
		
	
		
		Integer var=null,f1var=null,f2var=null;
	    if(fr==null){//so recurse
	    	if(!this.isTerminalNode((AditArc)f1)){
	    		if(!this.isTerminalNode((AditArc)f2)){
	    			f1var=((InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f1).F)).getVar();
	    			f2var=((InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f2).F)).getVar();
	    			if (f1var.compareTo(f2var)<0){
	    				var=f1var;
	    			}
	    			else{
	    				var=f2var;
	    			}
	    		}
	    		else
	    		{   f1var=((InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f1).F)).getVar();
	    			var=f1var;
	    		}
	    	}
	    	else{
	    		f2var=((InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f2).F)).getVar();
	    		var=f2var;
	    	}
            //set up nodes for recursion
	    	if (var==null){ 
	    		System.out.println("Something wrong in reduce");
	    	}
	    	Integer flv1,fhv1,flv2,fhv2;
	    	double clv1,chv1,clv2,chv2;
	    	if ((!this.isTerminalNode((AditArc)f1))&& var.equals(f1var)){
	    		InternalNodeKeyAdit node=(InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f1).F);
	    	    flv1=node.getLower().F;
	    	    fhv1=node.getHigh().F;
	    	    clv1=arcPair.Arc1.c.doubleValue()+node.getLower().c.doubleValue();
	    	    chv1=arcPair.Arc1.c.doubleValue()+node.getHigh().c.doubleValue();
  
	    	}
	    	else{
	    		flv1=fhv1=((AditArc)f1).F;
	    		clv1=chv1=arcPair.Arc1.c.doubleValue();
	    	}
	    	if ((!this.isTerminalNode((AditArc)f2))&& var.equals(f2var)){
	    		InternalNodeKeyAdit node=(InternalNodeKeyAdit)this.getNodeInverseCache(((AditArc)f2).F);
	    	    flv2=node.getLower().F;
	    	    fhv2=node.getHigh().F;
	    	    clv2=arcPair.Arc2.c.doubleValue()+node.getLower().c.doubleValue();
	    	    chv2=arcPair.Arc2.c.doubleValue()+node.getHigh().c.doubleValue();
	    	    
	    	}
	    	else{
	    		flv2=fhv2=((AditArc)f2).F;
	    		clv2=chv2=arcPair.Arc2.c.doubleValue();
	    	}
	    	Object newArcLowv1=new AditArc(clv1,flv1);
	    	Object newArcLowv2=new AditArc(clv2,flv2);
	    	Object newArcHighv1=new AditArc(chv1,fhv1);
	    	Object newArcHighv2=new AditArc(chv2,fhv2);
	    	
	    	Object fl=this.apply(newArcLowv1,newArcLowv2, op);	
	    	Object fh=this.apply(newArcHighv1,newArcHighv2, op);
	    	
	    	fr=this.GetNode(var, (AditArc)fh, (AditArc)fl);
	 
	    	//Put result in apply cache and return
	    	//AditArc newArcF1=new AditArc(arcPair.Arc1.c,((AditArc)f1).F);
	    	//AditArc newArcF2=new AditArc(arcPair.Arc2.c,((AditArc)f2).F);
	    	//this.putResultApplyCache(newArcF1, newArcF2, op, fr);
	    	this.putResultApplyCache(arcPair, op, fr);
	    }
	    AditArc result = modifyResult(fr,op,(AditArc)f1,(AditArc)f2);
	  
	    
	 

	    
	    return result;
	}
	
	private void putResultApplyCache(AditArcPair arcPair, BinaryOperation op, AditArc fr) {
		    BinaryOperKeyAditADD nodeOp=new BinaryOperKeyAditADD(arcPair.Arc1,arcPair.Arc2,op);
			this.applyCache.put(nodeOp,fr);
			
			

	
}
	private AditArc getIfIsInApplyCache(AditArc arc1, AditArc arc2, BinaryOperation op) {

	    BinaryOperKeyAditADD nodeOp=new BinaryOperKeyAditADD(arc1,arc2,op);
		//find the nodeOp 
		AditArc arc=(AditArc) this.applyCache.get(nodeOp); 
	    
		return arc;
		
	}


	private AditArc modifyResult(AditArc fr,BinaryOperation op,AditArc f1, AditArc f2) {
		
		//System.out.println("Calling modifyResult for " + op);
		AditArc result = op.modifyResult(fr,this,f1,f2);
	
		
	
	   	return result;
		
	}
	private AditArcPair getNormCacheKey(AditArc f1, AditArc f2, BinaryOperation op) {
		
	    AditArcPair arcPair= op.getNormCacheKey(f1,f2,this);
	   	return arcPair;
	}
	/*
	public void putResultApplyCache(AditArc f1, AditArc f2, BinaryOperation op, AditArc fr){
		   BinaryOperKeyAditADD nodeOp=new BinaryOperKeyAditADD(f1,f2,op);
			this.applyCache.put(nodeOp,fr);
	 }*/
	
////////////RESTRICT METHODS////////////////
	@Override
	 public Object reduceRestrict(Integer idVar,UnaryOperationComplex op,Object arc){
		 //return  aditArc
		
		//  Can be more efficient if cache relies on F -> but need to modify
		//  result arc to take different constants into account.
		 UnaryOperationCKey unaryKey=new UnaryOperationCKey(idVar,op,((AditArc)arc).F);//
		 Object Fr=  this.restrictCache.get(unaryKey);
		 //System.out.println(this.restrictCache.keySet());
		 //System.out.println(unaryKey.toString());
		 if (Fr!=null){
			 // why it is no do never: because the order primes variables before 
			 //System.out.println("USO RESTRICT CACHE ======================================");
			 AditArc Fresult= new AditArc((AditArc)Fr);
			 Fresult.c=new Double(Fresult.c.doubleValue()+((AditArc)arc).c.doubleValue());
			 return Fresult;
		 }
	    //find the node in the inverseNodeCache	 
		if (this.isTerminalNode(arc)){
			return arc;
		}
		else{  
			InternalNodeKeyAdit nodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(((AditArc)arc).F);
						
			AditArc Fh= new AditArc(nodeKey.getHigh());//here I was modified the inverseNodesCache then now we make a copy o the Arc
			AditArc Fl= new AditArc(nodeKey.getLower());
			Integer Fvar= nodeKey.getVar();
			if(Fvar.compareTo(idVar)==0){
				if (op == RESTRICT_HIGH){
					((AditArc)Fh).c=new Double(((AditArc)Fh).c.doubleValue()+((AditArc)arc).c);
					return Fh;//this.reduceRestrict(idVar, op, Fh);    			     
				}	
				else if (op==RESTRICT_LOW) {
					((AditArc)Fl).c=new Double(((AditArc)Fl).c.doubleValue()+((AditArc)arc).c);
					return Fl;//this.reduceRestrict( idVar, op, Fl);
				} else {
					System.out.println("ERROR: Illegal RESTRICT operation: " + op);
					System.exit(1);
				}
			}
			// Get new low and high branches
			else{
				//System.out.println("RESTRICT 1");
				//System.exit(1);
				Object FHigh=(AditArc)this.reduceRestrict(idVar, op, Fh);
				Object FLow=(AditArc)this.reduceRestrict(idVar, op, Fl);
				/*if (FHigh.compareTo(FLow)==0){
					recurse=false;
					// in this case GetNode return   Flow
				}*/
				Fr=GetNode(Fvar,FHigh,FLow);
			}

		}
		this.restrictCache.put(unaryKey, Fr); 
		//if (!restrictCache.contains(unaryKey)) {
		//	System.out.println("RESTRICT CACHE ERROR!");
		//	System.exit(1);
		//}
		 AditArc Fresult= new AditArc((AditArc)Fr);
		((AditArc)Fresult).c=new Double(((AditArc)Fr).c.doubleValue()+((AditArc)arc).c);
			
	    return Fresult;			    		
	 }
	////////////FOR MDP
	

    /**
     * Create a prime version of the ADD
     * @param arc
     * @param hmPrimeRemap Maps non-prime IDs to their primed counterparts){
     * @return
     */
		
		public Object remapIdWithPrime(Object arc, HashMap hmPrimeRemap)  { 
	    	
	    	if(this.isTerminalNode(arc)){
	    		return arc;
	    	}
	    	Object Fr=  remapIdCache.get(arc);
	    	if(Fr==null){
	    		InternalNodeKeyAdit intNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(((AditArc)arc).F);
	    		Object Fh=remapIdWithPrime(intNodeKey.getHigh(),hmPrimeRemap);
	    		Object Fl=remapIdWithPrime(intNodeKey.getLower(),hmPrimeRemap);
	    		Integer FvarPrime=(Integer) hmPrimeRemap.get(intNodeKey.getVar());
	    		Fr=GetNode(FvarPrime,Fh,Fl);
	    		((AditArc)Fr).c=new Double(((AditArc)Fr).c.doubleValue()+((AditArc)arc).c);
	    		remapIdCache.put(arc, Fr);
	    	}
	    
	    	return Fr;
	    }
		public Object remapIdWithOutPrime(Object arc, HashMap hmPrime2IdRemap)  { 
	    	
	    	if(this.isTerminalNode(arc)){
	    		return arc;
	    	}
	    	Object Fr=  remapIdCache.get(arc);
	    	if(Fr==null){
	    		InternalNodeKeyAdit intNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(((AditArc)arc).F);
	    		Object Fh=remapIdWithOutPrime(intNodeKey.getHigh(),hmPrime2IdRemap);
	    		Object Fl=remapIdWithOutPrime(intNodeKey.getLower(),hmPrime2IdRemap);
	    		Integer Fvar=(Integer) hmPrime2IdRemap.get(intNodeKey.getVar());
	    		Fr=GetNode(Fvar,Fh,Fl);
	    		((AditArc)Fr).c=new Double(((AditArc)Fr).c.doubleValue()+((AditArc)arc).c);
	    		remapIdCache.put(arc, Fr);
	    	}
	    
	    	return Fr;
	    }
		//flushing 
		

		public void copyInNewCacheNode(Object arc) {
			/*if(!this.workingWithParameterized){	
				if (inverseNodesCacheNew.containsKey(((AditArc)arc).F)) {
					return;
				}
				Object node =  inverseNodesCache.get(((AditArc)arc).F);

				if (node != null) {//internal node
					AditArc fh = ((InternalNodeKeyAdit)node).getHigh();
					AditArc fl = ((InternalNodeKeyAdit)node).getLower();
					InternalNodeKeyAdit nodeNew=new InternalNodeKeyAdit(((InternalNodeKeyAdit)node).getVar(),fh, fl);
					inverseNodesCacheNew.put(((AditArc)arc).F, nodeNew);
					nodesCacheNew.put(nodeNew,((AditArc)arc).F);
					copyInNewCacheNode(((InternalNodeKeyAdit)node).getHigh());
					copyInNewCacheNode(((InternalNodeKeyAdit)node).getLower());
				} else if (this.isTerminalNode(arc)) {
					//TerminalNodeKey nodeNew= new TerminalNodeKey(((TerminalNodeKey)node).getValue());
					//inverseNodesCacheNew.put(arc, nodeNew);
					//nodesCacheNew.put(nodeNew,arc);

				}
			}
			else{*/
				if (inverseNodesCacheNew.containsKey(((AditArc)arc).F)) {
					return;
				}
				Object node =  inverseNodesCache.get(((AditArc)arc).F);

				if (node != null) {//internal node or terminal node
					if(node instanceof InternalNodeKeyAdit){
						AditArc fh = ((InternalNodeKeyAdit)node).getHigh();
						AditArc fl = ((InternalNodeKeyAdit)node).getLower();
						InternalNodeKeyAdit nodeNew=new InternalNodeKeyAdit(((InternalNodeKeyAdit)node).getVar(),fh, fl);
						inverseNodesCacheNew.put(((AditArc)arc).F, nodeNew);
						nodesCacheNew.put(nodeNew,((AditArc)arc).F);
						copyInNewCacheNode(((InternalNodeKeyAdit)node).getHigh());
						copyInNewCacheNode(((InternalNodeKeyAdit)node).getLower());
					}
					else{
						TerminalNodeKeyPar nodeNew=new TerminalNodeKeyPar(((TerminalNodeKeyPar)node).getPolynomial());
						inverseNodesCacheNew.put(((AditArc)arc).F, nodeNew);
						nodesCacheNew.put(nodeNew,((AditArc)arc).F);
					}

				}
				else{ //if (this.isTerminalNode(arc)) {
				//	System.out.println("copyInNewCacheNode: the node is null");				
				//TerminalNodeKey nodeNew= new TerminalNodeKey(((TerminalNodeKey)node).getValue());
				//inverseNodesCacheNew.put(arc, nodeNew);
				//nodesCacheNew.put(nodeNew,arc);
				}
			//}

		}

	    
	    public InternalNodeKeyAdit verifyIDCache() {
	    	// Go through all  values in the inverseNodesCache and verify they have values in nodesCache
	    	for (Object val : inverseNodesCache.values()) {
	    		InternalNodeKeyAdit key = (InternalNodeKeyAdit)val;
	    		if (nodesCache.get(key) == null)
	    			return key;
	    	}
	    	return null;
	    }
	    
	    //FOR APRICODD
	    @Override
		public Object pruneNodesValue(Object valueiDD, double mergeError) {
			// TODO working in it
	    	idVar2Ids=new TreeMap(Collections.reverseOrder());//reverse ordering for the ids 
	    	id2NewId=new Hashtable();
	    	reduceCachePrune=new Hashtable();
	    	buildHashInternalNodes((AditArc)valueiDD);
	    	Double sumError=0d;
	    	Integer newId=null;
	    	do{	
	    		newId=getPruneOnePairADDMap((AditArc)valueiDD,mergeError-sumError);
	    		 if (newId!=null){
	    			 sumError+=this.currentError;
	    		 }
	    	}
	    	while(sumError<=mergeError && newId!=null);
	    	return createNewADDfromMap((AditArc)valueiDD);
						
		}



private Object createNewADDfromMap(AditArc arc) {
	if(this.isTerminalNode(arc)){
		return arc;
	}
	//was reduced before
	AditArc arcR= (AditArc) reduceCachePrune.get(arc.F);
	//else
	if(arcR==null){
		InternalNodeKeyAdit aditNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(arc.F);
		Integer idFh=getIdFromId2NewId(aditNodeKey.getHigh().F);
		Integer idFl=getIdFromId2NewId(aditNodeKey.getLower().F);
		//InternalNodeKeyAdit aditNodeKey=((InternalNodeKeyAdit)inverseNodesCache.get(idFh)).
		//InternalNodeKeyAdit aditNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(idFl);
		AditArc ArcFh=(AditArc)createNewADDfromMap(new AditArc(aditNodeKey.getHigh().c,idFh));
		AditArc ArcFl=(AditArc)createNewADDfromMap(new AditArc(aditNodeKey.getLower().c,idFl));
		Integer Fvar= aditNodeKey.getVar();  //Review
		arcR=GetNode(Fvar,ArcFh,ArcFl);
		reduceCachePrune.put(arc.F, arcR);
	} 

	// Need to add in the offset of Arc (which we have not used yet)
	AditArc arcRCopy=new AditArc(arcR.c+arc.c,arcR.F);
	return arcRCopy;		
	}



private Integer getIdFromId2NewId(Integer id) {
	
	while(id2NewId.get(id)!=null){
		
		id=(Integer)id2NewId.get(id);
	}
	return id;
}



private Integer getPruneOnePairADDMap(AditArc valueiDD, double mergeErrorCurrent) {
   Iterator it= idVar2Ids.keySet().iterator();
   while (it.hasNext()){
	   Integer idVar=(Integer)it.next();
	   ArrayList array=(ArrayList)idVar2Ids.get(idVar);
	   if(array.size()>=2){
		   
		   while(array.size()>=2){  //while because probably there are other pairs that can be merged
			   Integer idToMerge=(Integer)array.get(0);
			   Integer newId=findParPrune(idToMerge, array, mergeErrorCurrent);
			   if (newId!=null){
				    return newId;
			   }
		   }
		   
	   }
   }
   return null;		
}


private Integer findParPrune(Integer idToMerge, ArrayList array,double mergeErrorCurrent){
//TODO: working on it
array.remove(0);
InternalNodeKeyAdit intNodeMerge=(InternalNodeKeyAdit)inverseNodesCache.get(idToMerge);
for(int i=0;i<array.size();i++){
	   
	   InternalNodeKeyAdit intNode=(InternalNodeKeyAdit)inverseNodesCache.get(array.get(i));
	  
	   
	   AditArc highNMerge=intNodeMerge.getHigh();
	   AditArc highN=intNode.getHigh();
	   AditArc lowerNMerge=intNodeMerge.getLower();
	   AditArc lowerN=intNode.getLower();
	  
	   //Find transitively
	   Integer idhMerge=getIdFromId2NewId(highNMerge.F);
	   Integer idhN=getIdFromId2NewId(highN.F);
	   Integer idlMerge=getIdFromId2NewId(lowerNMerge.F);
	   Integer idlN=getIdFromId2NewId(lowerN.F);
	   
	   
	  // boolean equalBranches=(highNMerge.F.compareTo(highN.F)==0 && lowerNMerge.F.compareTo(lowerN.F)==0);
	   boolean equalBranches=(idhMerge.compareTo(idhN)==0 && idlMerge.compareTo(idlN)==0);
	   
	   if(equalBranches &&sameOBranch(highNMerge,highN,lowerNMerge,lowerN)){
		   if ((Math.abs(highNMerge.c-0d)<= 1e-10d &&Math.abs(highNMerge.c-highN.c)<= 1e-10d)){
			   double err=Math.abs(lowerNMerge.c-((lowerNMerge.c+lowerN.c)/2d));
			   if (err<=mergeErrorCurrent){
				   Integer newId=createNewNode(intNodeMerge,intNode);
				   currentError= err;
				   if(idToMerge.compareTo(newId)!=0){
					   id2NewId.put(idToMerge, newId);
				   }
				   if(((Integer)array.get(i)).compareTo(newId)!=0){
					   id2NewId.put(array.get(i), newId);
				   }
				   //TODO: verificar que newId no este en id2NewId tanto como key quanto como value para evitar los lazos infinitos
				   array.remove(i);
				   array.add(newId);
				   return newId;
			   }
		   }
		   else{
			   double err=Math.abs(highNMerge.c-((highNMerge.c+highN.c)/2d));
			   if (err<=mergeErrorCurrent){
				   Integer newId=createNewNode(intNodeMerge,intNode);
				   currentError= err;
				   if(idToMerge.compareTo(newId)!=0){
					   id2NewId.put(idToMerge, newId);
				   }
				   if(((Integer)array.get(i)).compareTo(newId)!=0){
					   id2NewId.put(array.get(i), newId);
				   }
				   array.remove(i);
				   array.add(newId);
				   return newId;
			   }
			   
		   }
		   
		   
	   }
	   
}
return null;
}

private Integer createNewNode(InternalNodeKeyAdit intNodeMerge, InternalNodeKeyAdit intNode) {
	   AditArc highNMerge=intNodeMerge.getHigh();
	   AditArc highN=intNode.getHigh();
	   AditArc lowerNMerge=intNodeMerge.getLower();
	   AditArc lowerN=intNode.getLower();
	   AditArc arcFh=new AditArc((highNMerge.c+highN.c)/2,highNMerge.F);
	   AditArc arcFl=new AditArc((lowerNMerge.c+lowerN.c)/2,lowerNMerge.F);	   
       AditArc arc=this.GetNode(intNodeMerge.var,arcFh, arcFl);
       return arc.F;
       
}



private boolean sameOBranch(AditArc highNMerge, AditArc highN, AditArc lowerNMerge, AditArc lowerN) {
	if(((Math.abs(highNMerge.c-0d)<= 1e-10d &&Math.abs(highNMerge.c-highN.c)<= 1e-10d) &&(Math.abs(lowerNMerge.c-0d)<= 1e-10d && Math.abs(lowerNMerge.c-lowerN.c)<= 1e-10d))){
		System.out.println("It must not happen");
		System.exit(0);
	}
	return ((Math.abs(highNMerge.c-0d)<= 1e-10d &&Math.abs(highNMerge.c-highN.c)<= 1e-10d) ||(Math.abs(lowerNMerge.c-0d)<= 1e-10d && Math.abs(lowerNMerge.c-lowerN.c)<= 1e-10d));
	
	
}







private AditArc buildHashInternalNodes(AditArc arc) {
 	if(this.isTerminalNode(arc)){
 		return arc;
	}
	AditArc arcR= (AditArc) cacheCont.get(arc.F);
	if(arcR==null){
		InternalNodeKeyAdit intNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(arc.F);
		AditArc Fh=buildHashInternalNodes(intNodeKey.getHigh());
		AditArc Fl=buildHashInternalNodes(intNodeKey.getLower());
		Integer Fvar= intNodeKey.getVar();
		arcR=GetNode(Fvar,Fh,Fl);
		ArrayList list=(ArrayList)idVar2Ids.get(Fvar);
		if (list==null){
			list=new ArrayList();
						
		}
		list.add(arc.F);
		
		idVar2Ids.put(Fvar,list);

		cacheCont.put(arc.F, arcR);
		
	}
	return arcR;
	
}	



		public AditArc doMaxCallOverNodes(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt) {
			System.out.println("method not implemented");
			System.exit(-1);
			
			return null;
		}


		//		 the parameter is ParADD and the result is an ADD
	     public AditArc doMinCallOverNodes(Object VDD,String NAME_FILE_CONTRAINTS,boolean pruneAfterEachIt) {
		              
	    	    	if(this.isTerminalNode(VDD)){
	    	    		TerminalNodeKeyPar node=(TerminalNodeKeyPar)this.getInverseNodesCache().get(((AditArc) VDD).F);

	    	    		//if the node not have probabilities then not call the nonlinear solver
	    	    		if(node.getPolynomial().getTerms().size()==0){
	    	    			return this.getTerminalNode(node.getPolynomial().getC()+((AditArc) VDD).c);//ultima modificacion
	    	    		}
	    	    		//else call de nonlinear solver
	    	    		createFileAMPL(node.getPolynomial().toString(this,"p"),NAME_FILE_CONTRAINTS);
	    	    		Double obj=callNonLinearSolver();
	    	    		if (obj==null){
	    	    			System.out.println("doMinCallOverNodes: Problems with the solver ");
	    	    			System.exit(0);
	    	    		}
	    	    		return this.getTerminalNode(obj+((AditArc) VDD).c);//ultima modificacion
	    	    	}
	    	    	AditArc arcR=(AditArc) reduceCacheMinPar.get(((AditArc) VDD).F);
	    	    	
	    	    	if(arcR==null){
	    	    		InternalNodeKey	intNodeKey=(InternalNodeKey)this.getInverseNodesCache().get(((AditArc) VDD).F);
	    	    		
	    	    		AditArc Fh=doMinCallOverNodes(intNodeKey.getHigh(),NAME_FILE_CONTRAINTS,pruneAfterEachIt);
	    	    		AditArc Fl=doMinCallOverNodes(intNodeKey.getLower(),NAME_FILE_CONTRAINTS,pruneAfterEachIt);
	    	    		Integer Fvar= intNodeKey.getVar();
	    	    		arcR=GetNode(Fvar,Fh,Fl);
    	    			reduceCacheMinPar.put(((AditArc) VDD).F, arcR);
	    	    	}
	    	    	//arcR.c+=((AditArc) VDD).c;
	    	    	AditArc arcRCopy=new AditArc(arcR.c+((AditArc) VDD).c,arcR.F);
	    	    	return arcRCopy;
	    	 
		} 
	    
	
	    



		@Override
		public int contNumberNodes(Object arc) {
		    this.cacheCont.clear();
		    //contLeaf=0;
	        AditArc arc2=reduceCacheCont((AditArc)arc);
	        return cacheCont.size()+1;
	        
		}

	
		
		
		private AditArc reduceCacheCont(AditArc arc) {
		 	if(this.isTerminalNode(arc)){
		 		//System.out.println(((TerminalNodeKeyADD)this.inverseNodesCache.get(F)).getValue());
		 		return arc;
	    	}
	    	AditArc arcR= (AditArc) cacheCont.get(arc.F);
	    	if(arcR==null){
	    		InternalNodeKeyAdit intNodeKey=(InternalNodeKeyAdit)inverseNodesCache.get(arc.F);
	    		AditArc Fh=reduceCacheCont(intNodeKey.getHigh());
	    		AditArc Fl=reduceCacheCont(intNodeKey.getLower());
	    		Integer Fvar= intNodeKey.getVar();
	    		arcR=GetNode(Fvar,Fh,Fl);
	    		cacheCont.put(arc.F, arcR);
	    		
	    	}
	    	return arcR;
			
		}



		@Override
		public Double getValueForStateInContext(Integer F, TreeMap<Integer, Boolean> state, Integer xiprime, Boolean valXiprime) {
			System.out.println("getValueForStateInContext was not implemented for ContextAditADD");
			System.exit(1);
			return null;
		}



		@Override
		public void dump(Object valueiDD, String NAME_FILE_VALUE) {
			System.out.println(" dump was not implemented for ContextAditADD");
			System.exit(1);
			
		}



		@Override
		public Double getProbCPTForStateInContextEnum(Integer F, State state, Integer xiprime, Boolean valXiprime,int numVars) {
			System.out.println("getValueForStateInContext was not implemented for ContextAditADD");
			System.exit(1);
			return null;
		}	

		@Override
		public Double getRewardForStateInContextEnum(Integer F, State state, int numVars) {
			System.out.println("getRewardForStateInContext with State parameter was not implemented for ContextAditADD");
			System.exit(1);
			return null;
			
		}



		@Override
		public Object getValuePolyForStateInContext(Integer F, TreeMap<Integer, Boolean> state, Integer xiprime, Boolean valXiprime) {
			// TODO Auto-generated method stub
			System.out.println("Not implemented for AditADD");
			return null;
		}

}
