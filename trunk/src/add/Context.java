package add;
import graph.Graph;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import logic.lattice.Lattice;
import mdp.Action;
import mdp.Config;
import mdp.State;
public abstract class Context {
//	operation constants
	public final static Sum SUM =new Sum();
	public final static Prod PROD =new Prod();
	public final static SimpleProd SIMPLE_PROD = new SimpleProd();
	public final static Sub SUB=new Sub();
	public final static Max MAX=new Max();
	public final static Min MIN=new Min();
	public final static MaxValue MAXVALUE=new MaxValue();
	public final static MinValue MINVALUE=new MinValue();
	public final static Restrict RESTRICT_HIGH=new Restrict(); 
	public final static Restrict RESTRICT_LOW=new Restrict();
	public final static SumOut SUMOUT=new SumOut();
	public static final Double ZERO=new Double(0.0);
	public static final Double ONE=new Double(1.0);
	public static final Polynomial ZEROPOLYNOMIAL=new Polynomial(0d,new Hashtable(),null);
	public static final Polynomial ONEPOLYNOMIAL=new Polynomial(1d,new Hashtable(),null);
	/////////////////
	protected Integer unllocatedIdNode;
	protected Hashtable nodesCache; //NodeKey-->id
	protected Hashtable inverseNodesCache;  //id--> NodeKey
	protected Hashtable reduceCache;
	protected Hashtable applyCache;
    protected Hashtable restrictCache;//< UnaryOperationKey> -> Integer-id  for ADD//< UnaryOperationKey> -> AditArc for AditADD 
	protected Hashtable remapIdCache;
	protected Hashtable remapIdCacheWithOut;
	protected Hashtable maxCache=new Hashtable();  // AditArc --> double  only for AditADDContext
	
	//Parameterized ADD
    protected Hashtable labelsProdId; // String (label product of probabilities)--> Integer(id) 
    protected Hashtable inverseLabelsProdId; //Integer(id) --> String (label product of probabilities)
    protected Integer unllocatedIdLabelProd;
    protected Hashtable reduceCacheMinPar=new Hashtable();
    public final static String  NAME_FILE_AMPL = Config.getConfig().getAmplTempFile();
    public final static String  NAME_FILE_AMPL_BOUNDS = Config.getConfig().getAmplBoundTempFile();
    
    HashMap probBound=new HashMap();  // String prob -> array[lower,upper]
    
    public int reuseCacheIntNode=0,clash=0, noclash=0,contReuse=0,contNoReuse=0,numberReducedToValue=0,contReuseUsingLattice=0;
    public Hashtable<String, Double> currentValuesProb=new Hashtable<String, Double>();     //  idProb--> valProb
    public Hashtable<String, Double> probSample;     //  idProb--> valProb
    
    
    protected Hashtable currentDirectionList=new Hashtable();
    protected Hashtable cacheResultsSolver=new Hashtable();
    
    //public final static String  NAME_FILE_AMPL="C:\\cygwin\\home\\karina\\ADD\\ADD\\reportsMDPIP\\temporal.mod";
    public final static String  SOLVER="option solver minos;";
	
	 // For printing
    public static DecimalFormat _df = new DecimalFormat("#.######");
    
    ///For evaluator
    protected Hashtable printCache;
    public Hashtable reduceConvert;;
    ///////////////
    
//	 Nodes (and children) to keep when flushing caches
    protected HashSet hsSpecialNodes = new HashSet();
    protected Hashtable nodesCacheNew=new Hashtable();
	protected Hashtable inverseNodesCacheNew=new Hashtable();
    
    
    public abstract NodeKey getNodeInverseCache(Integer f);
    public abstract Integer getIdCache(NodeKey nodek);
    public abstract Object getTerminalNode(double fval);
    
    public abstract Object getInternalNode(Integer fvar, Object Fh, Object Fl);
  	public abstract Object GetNode(Integer fvar, Object fh, Object fl);
	public abstract boolean isTerminalNode(Object f);
	

	

	//operations
	public abstract Object apply(Object f1, Object f2, BinaryOperation op);
	public abstract Double apply(Object f1, UnaryOperationSimple op);
	
	
    //for restrict
	public abstract Object reduceRestrict(Integer idVar,UnaryOperationComplex op,Object id);
	public Object apply(Integer idVar,UnaryOperationComplex op,Object id) {
		 return op.computeResult(idVar,op,id,this);
		 
	}  
	//for MDP
	public abstract Object remapIdWithPrime(Object id, HashMap hmPrimeRemap);
	public abstract Object remapIdWithOutPrime(Object F, HashMap hmPrime2IdRemap);
	protected Hashtable cacheCont;
	public int numCallSolver=0;
	 
	//for APRICODD
	public abstract Object pruneNodesValue(Object valueiDD, double mergeError);
	 public  double mergeError = -1 ;
	 public int typeAproxPol;
	 public Lattice lattice;
	//for Parameterized ADD
	public boolean workingWithParameterized=false;
	public boolean workingWithParameterizedBef=false;
	public TreeSet listVarProb=new TreeSet(new IntegerComparator());// it is created in buildAction (buildDDFromUnorderedTree
	public abstract Object getTerminalNode(String varPro,Double coef);
	public abstract Object getTerminalNode(String varPro);
	public abstract Object getTerminalNode(Polynomial polynomial);
	
	//for RTDP
	public HashSet reduceCacheWeighted;
	
	//for MP
	public ArrayList listVarWeight=new ArrayList();
	public Hashtable<String,Double> currentValuesW=new Hashtable<String,Double>();     //  idW--> valW
	
	// Designate/remove/clear nodes to persist through flushing
	
    public void clearSpecialNodes() {
	 hsSpecialNodes.clear();
    }
    public void addSpecialNode(Object n) {
    	try {
    	if (n == null) throw new Exception("addSpecialNode: null");
    	} catch (Exception e) {
    		System.out.println(e);
    		e.printStackTrace();
    		System.exit(1);
    	}
	  hsSpecialNodes.add(n);
    }
    
    /**
     * Clear the temporal hashMap
     * reduceCache, applyCache, restrictCache and remapIdCache
     * Copy the nodesCache
     */
    public void flushCaches() {
			System.out.print("[FLUSHING CACHES... ");
		// Can always clear these

			reduceCache=new Hashtable();
			applyCache=new Hashtable();
			restrictCache=new Hashtable();
			remapIdCache=new Hashtable();	
			maxCache=new Hashtable();
		// Set up temporary alternates to these HashMaps

		nodesCacheNew=new Hashtable();
		inverseNodesCacheNew=new Hashtable();
			
		// Copy over 'special' nodes then set new maps
		System.out.println(hsSpecialNodes);
		Iterator i = hsSpecialNodes.iterator();
		while (i.hasNext()) {
			
			copyInNewCacheNode( i.next());
		}
		nodesCache=nodesCacheNew;
		inverseNodesCache=inverseNodesCacheNew;
		
		Runtime.getRuntime().gc();
	}
    public abstract void copyInNewCacheNode(Object id);    
   
    
    //viewing
	public abstract Graph toGraph(Object current);
	public void view(Object current) {
		Graph g = toGraph(current);
		g.launchViewer(1300, 770);
	}
	
	 
	 //reading a file and put in the MDP
	 
		/**
		 * Build an ADD from a list (node is a list, high comes first for internal
		 * nodes)
		 * (C1                      ---list.get(0)
		 *     (C4 (0.95) (0.475)   ---list.get(1)
		 *     (C4 (0.0475)(0.0238))---list.get(2)
		 * 
		 * [                         ---list.get(0)     
		 * 1                         ---list.get(1)
		 * ]                         ---list.get(2)
		 * 
		 **/
	 public Object buildDDFromUnorderedTree(ArrayList list, TreeMap tmVar2ID,Integer firstVarId,HashMap varId2DependPrimeList) {

		 Object o = list.get(0);
		 if (o instanceof String && HasOnlyDigits((String) o)) {
			 double val = (new BigInteger((String) o)).doubleValue();
			 return this.getTerminalNode(val);
			 
		 } else if (o instanceof BigDecimal) {
			 double val = ((BigDecimal) o).doubleValue();
			 return this.getTerminalNode(val);
		 }//new else for Parameterized 
		 else if (o instanceof String && ((String)o).contains("[") ){
			  workingWithParameterized=true;
			  //System.out.println(list.get(1));
			  //System.out.println(list.get(2));
			  //System.out.println(list.get(3));
			  
			  if(list.size() == 5  && ((String)list.get(3)).contains("p")){
				  Double coef=null;
				  if(list.get(1) instanceof String && HasOnlyDigits((String) list.get(1))){
					  coef= (new BigInteger((String) list.get(1))).doubleValue();
				  }
				  else if(list.get(1) instanceof BigDecimal){
					  coef= ((BigDecimal) list.get(1)).doubleValue();
				  }
				  else{
					  System.out.println("Line"+ list.get(1));
					  System.out.println("BuidingFromUnOrderedTree: Error reading the file"); 
					  System.exit(0);
					  return null;
				  }
				  String prob=(String)list.get(3);
				  String varProb=prob.substring(1);
				  this.listVarProb.add(varProb);
				  return this.getTerminalNode(varProb,coef);
			  } 
//			could be BigInteger? NO, because the reward specification now NOT contain [
			  else if(list.get(1) instanceof String && HasOnlyDigits((String) list.get(1))) {
				  double val = (new BigInteger((String) list.get(1))).doubleValue();
				  Polynomial polynomial=new Polynomial(val,new Hashtable(),this);
				  System.out.println("Error in buildDDFromUnorderedTree: The reward never have []");
				  System.exit(0);
				  return this.getTerminalNode(polynomial);
			  }		 
			  else if(list.get(1) instanceof BigDecimal){
				  double val = ((BigDecimal) list.get(1)).doubleValue();
				  Polynomial polynomial=new Polynomial(val,new Hashtable(),this);
				  return this.getTerminalNode(polynomial);
			  }
			   
			 
			  
			  else{
				 System.out.println("Line"+ list.get(1));
				 System.out.println("BuidingFromUnOrderedTree: Error reading the file"); 
				 System.exit(0);
				 return null;
			  }
		 } else {//is a variable name
			 String var = (String) o;
			 Integer varId = ((Integer) tmVar2ID.get(var));
			 //If there are syncronic arcs create the varId2DependPrimeList
			 if (var.contains("'") && firstVarId!=null){
				 ArrayList dependPrimeList=(ArrayList)varId2DependPrimeList.get(firstVarId);
				 if (dependPrimeList==null){
					 dependPrimeList=new ArrayList();
				 }
				 dependPrimeList.add(varId);
				 varId2DependPrimeList.put(firstVarId, dependPrimeList);
			 }
			 Object high=buildDDFromUnorderedTree((ArrayList) list.get(1), tmVar2ID,firstVarId,varId2DependPrimeList); //high is ordered
			 //this.view(high);
			 Object low= buildDDFromUnorderedTree((ArrayList) list.get(2), tmVar2ID,firstVarId,varId2DependPrimeList); //low is ordered
			 //this.view(low);
			 return computeFinalADDForVarIDFromHighLow(varId, high, low);  // put varId in the correct position in the new ADD
		 }
	 }
	 public static boolean HasOnlyDigits(String s) {
		 for (int i = 0; i < s.length(); i++) {
			 if (!Character.isDigit(s.charAt(i))) {
				 return false;
			 }
		 }
		 return true;
	 }
		/**
		 * Multiply high by    varID          +     low  by    varID
		 *                    /    \                           /   \
		 *                    1     0                         0     1
		 * put varId in the correct position in the new ADD
		 * @param varID
		 * @param high
		 * @param low
		 * @return
		 */
		
		public Object computeFinalADDForVarIDFromHighLow(Integer varID, Object high, Object low){
			//Get the var ADD
			//TODO: modification done			
			Object zeroTerminal;
			Object oneTerminal;
			if(!this.workingWithParameterized){
				zeroTerminal=this.getTerminalNode(ZERO);
				oneTerminal=this.getTerminalNode(ONE);
			}
			else{
				zeroTerminal=this.getTerminalNode(ZEROPOLYNOMIAL);
				oneTerminal=this.getTerminalNode(ONEPOLYNOMIAL);
			}
		    Object high_br = this.getInternalNode(varID, oneTerminal, zeroTerminal);
		    //this.view(high_br);
		    //this.view(high);
		    high_br = this.apply(high_br, high, Context.PROD);
		    //this.view(high_br);

		    // Get the !var ADD
		    Object low_br  = this.getInternalNode(varID, zeroTerminal, oneTerminal);
		   // this.view(low_br);
		   // this.view(low);
		    low_br = this.apply(low_br, low, Context.PROD);
            //this.view(low_br);
		    // Compute final ADD
		    return  this.apply(low_br, high_br, Context.SUM);
		    
		}
		public Hashtable getInverseNodesCache(){
			return inverseNodesCache;
		}
		
//		TODO:new part for parameterized
		//Parameterized ADD and Tables///////////////////////////////////////////////////////////
		
		public Integer getNextUnllocatedIdLabelProd(){
	    	this.unllocatedIdLabelProd=new Integer(this.unllocatedIdLabelProd.intValue()+1);
	    	return this.unllocatedIdLabelProd;
	    }
			
	        
	    public Integer getIdLabelProd(String labelProd){

	        //find labelProd  
	    	Integer id=(Integer) this.labelsProdId.get(labelProd); 
	    	if (id==null){
	    		id=this.getNextUnllocatedIdLabelProd();
	    		this.putLabelsProdId(labelProd,id);
	    	}
	    	return id;
	    }
	      
	    
	    public void putLabelsProdId(String label, Integer id){
	    	this.labelsProdId.put(label, id);
	    	this.inverseLabelsProdId.put(id, label);
	    }

		public String getLabelProd(Integer id) {
			return (String)this.inverseLabelsProdId.get(id);
		}
		//for parameterized
	
			
		
	    protected void createFileAMPL(String objective,String NAME_FILE_CONTRAINTS) {
	 		
	     	try {
	     		
	             BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_AMPL));
	             writeVarObjective(objective,out);
	             BufferedReader input= new BufferedReader(new FileReader(NAME_FILE_CONTRAINTS));
	             String line = null;
	             while (( line = input.readLine()) != null){
	                 out.append(line);
	                 out.append(System.getProperty("line.separator"));
	             }
	             
	             //print the probabilities founded by the solver
	             writeProbFounded(out);
	             input.close();
	             out.close();
	         } catch (IOException e) {
	         	System.out.println("Problem with the creation AMPL file");
	         	System.exit(0);
	         }

	 	}
	    
	    
	    
		private void writeVarObjective(String objective, BufferedWriter out) throws IOException {
	    	out.write(SOLVER);
            out.append(System.getProperty("line.separator"));
            Iterator it=listVarProb.iterator();
            while(it.hasNext()){
           	 out.append("var p" + it.next() + ">=0, <=1;");
           	 out.append(System.getProperty("line.separator"));
            }
            
            out.append("minimize obj: " + objective + ";");
            out.append(System.getProperty("line.separator"));
			
		}

	    private void writeProbFounded(BufferedWriter out) throws IOException {
	    	out.append("solve;");
            out.append(System.getProperty("line.separator"));
	    	Iterator it1=listVarProb.iterator();
            while(it1.hasNext()){
           	 String pnumber=(String)it1.next();
           	 out.append("print 'p" + pnumber + "',p"+ pnumber+";");
           	 out.append(System.getProperty("line.separator"));
            }
			
		}
		

	    
	    //for Paramereterized   
	    /**
	     * call the non linear solver and fill currentValuesProb with the probabilities  
	     */
	     public Double callNonLinearSolver() {
	    	// TODO Auto-generated method stub
	    	 this.numCallSolver=this.numCallSolver+1;
	    	 Double obj=null;
	    	 try {
	             // Open files for reading and writing
	             //BufferedReader fis_reader = new BufferedReader(_rReader);
	             Process pros = Runtime.getRuntime().exec("ampl "+NAME_FILE_AMPL);
	             BufferedReader process_out = new BufferedReader(new InputStreamReader(pros.getInputStream()));
	             PrintWriter    process_in  = new PrintWriter(pros.getOutputStream(), true);

	             // Provide input to process (could come from any stream)
	             String line = null;
	             //while ((line = fis_reader.readLine()) != null) {
	             //      process_in.println(line);
	             //}
	             //fis_reader.close();
	             process_in.close(); // Need to close input stream so process exits!!!

	             // Get output from process (can also be used by BufferedReader to get
	             // line-by-line... see how fis_reader is constructed).
	             boolean foundObj=false;
	             while (!foundObj && (line = process_out.readLine()) != null ) {
	                 // process line
	            	 //System.out.println(line);
	            	 int pos=line.indexOf("objective");
	                 if ( pos>= 0) {
	                      	 
	                     obj=Double.valueOf(line.substring(pos+10));//pos+characters of objective +1
	                  //   System.out.println(pros.hashCode() + "objective: "+obj);
	                     foundObj=true;
	                 }
	                 
	                 
	                 
	             }
	             //TODO: print the probabilities
	             currentValuesProb=new Hashtable<String,Double>();
	             currentValuesW=new Hashtable<String,Double>();
	             double valProb,valW;
	             while ((line = process_out.readLine()) != null) {
	                 // process line
	            	// System.out.println(pros.hashCode() + "output: " + line);
	            	 int posP=line.indexOf("p");
	            	 int posW=line.indexOf("w");
	            	 int posS=line.indexOf(" ");
	                 if ( posP>= 0) {
	                	 valProb=Double.valueOf(line.substring(posS+1));
	                	 String idProb=line.substring(posP+1,posS);
	                     currentValuesProb.put(idProb, valProb);
	                 }
	                 else if (posW>=0){
	                	 valW=Double.valueOf(line.substring(posS+1));
	                	 String idW=line.substring(posW+1,posS);
	                     currentValuesW.put(idW, valW);
	                 }
	             }
	             process_out.close();
	             //pros.getErrorStream().close();
                //System.out.println(pros.hashCode() + "waiting");
	             //System.out.print("Waiting for Vampire [" + _fQueryTime + "s, "  + _nClausesGen + " clause]...");
                
	             pros.waitFor();
	             //pros.
	             //System.out.println("Exit code: " + pros.exitValue() + ": " + pros.hashCode() + "objective:"+obj+foundObj);

	             if (pros.exitValue() != 0) {
	            	 return null;
	             } else
	            	 return obj;

	         } catch (InterruptedException ie) {
	                 System.out.println(ie);
	                 System.out.println("interrupted");
             
	                 return null;
	         } catch (IOException ioe) {
	                 System.out.println(ioe);
	                System.out.println("ioexception");
	                 return null;
	         }
	     	
		}
	     
//		 the parameter is ParADD and the result is an ADD
	     public abstract Object doMinCallOverNodes(Object VDD,String NAME_FILE_CONTRAINTS,boolean pruneAfterEachIt);
		 public abstract int contNumberNodes(Object valueiDD);
///////////////////////////////////////////////////////////////////
		 //create file in order to obtain a bound of each probability
		 public HashMap createBoundsProb(String NAME_FILE_CONTRAINTS) {
			 probBound=new HashMap();
			 Iterator it=listVarProb.iterator();
             while(it.hasNext()){
            	 String prob=(String)it.next();
            	 createFileAMPL("p"+prob,NAME_FILE_CONTRAINTS);
            	 Double objP=callNonLinearSolver();
            	 createFileAMPL("-p"+prob,NAME_FILE_CONTRAINTS);
            	 Double objN=callNonLinearSolver();
            	 ArrayList bounds=new ArrayList();
            	 bounds.add(Math.min(Math.abs(objP),Math.abs(objN)));
            	 bounds.add(Math.max(Math.abs(objP),Math.abs(objN)));
                 probBound.put(prob,bounds );
             }
             return probBound;

		}
		 
		 
		 //	create file in order to get probabilities for RTDP-IP sample
		 public void getProbSampleCallingSolver(String NAME_FILE_CONTRAINTS_GREATERZERO) {
			 
			 createFileAMPL("0",NAME_FILE_CONTRAINTS_GREATERZERO);
			 Double obj=callNonLinearSolver(); //fill   currentValuesProb and currentValuesW
			 if (obj==null){
				 System.out.println("createProbSample: Problems with the solver. It returns null");
				 System.exit(0);
			 }
             probSample=new Hashtable(currentValuesProb);	
            
		}
 
		 

	     public static boolean containsClash(Hashtable dirlist) {
	    	 for (Object val : dirlist.values() ) {
	    		 if ("C".equals(val)) return true;
	    	 }
	    	 return false;
	     }
		abstract public void dump(Object valueiDD,String NAME_FILE_VALUE);
	
		
        public String getString(Integer valueiDD){
        	String Fr= (String) printCache.get(valueiDD);
        	if(Fr!=null){
        		return null;
        	}	

        	if(this.isTerminalNode(valueiDD)){
        		TerminalNodeKey terminal=(TerminalNodeKey)inverseNodesCache.get(valueiDD);
        		if (terminal instanceof TerminalNodeKeyADD){
        			printCache.put(valueiDD, "printed");
        			return Integer.toString(valueiDD)+" "+((TerminalNodeKeyADD)terminal).getValue();
        		}
        		else if (terminal instanceof TerminalNodeKeyPar){
        			printCache.put(valueiDD, "printed");
        			return Integer.toString(valueiDD)+" "+((TerminalNodeKeyPar)terminal).getPolynomial().toString();
        		}
        	}

        	InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(valueiDD);
        	String h=getString(intNodeKey.getHigh());
        	String l=getString(intNodeKey.getLower());
        	Integer Fvar= intNodeKey.getVar();
        	printCache.put(valueiDD, "printed");
        	String res="";
        	if (h!=null){
        		res=res+h+"\n";
        	}
        	
        	if (l!=null){
        		res=res+l+"\n";
        	}
        	return  res+Integer.toString(valueiDD)+" "+ Fvar +" "+intNodeKey.getHigh()+" " +intNodeKey.getLower();
        }
        
    	    	
 		
		
	 	  public Double getValueForStateInADD(Integer F,HashMap state,HashMap nextState,Integer xiprime,Integer valXiprime){
		  
	    	if(isTerminalNode(F)){
	    		return ((TerminalNodeKeyADD)inverseNodesCache.get(F)).getValue();
	    	}
	    	InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
	    	Integer Fvar= intNodeKey.getVar();
	    	Integer val=(Integer)state.get(Fvar);
	    	if(val==null){//var is in the nextState
	    	        val= (Integer)nextState.get(Fvar);	
	    	}
	    	if(val==null && Fvar!=null && Fvar.compareTo(xiprime)==0){
	    		 val=valXiprime;
	    	}
	    	if (val==null){
	    		System.out.println("There is not  variable Fvar ");
	    		System.exit(0);
	    	}
	    	Double reward=null;
	    	if (val==1){
	    	       reward=getValueForStateInADD(intNodeKey.getHigh(),state,nextState,xiprime,valXiprime);
	    	}
	    	else if (val==0){
	    		   reward=getValueForStateInADD(intNodeKey.getLower(),state,nextState,xiprime,valXiprime);
	    	}
	    	
	    	return reward;
  	  }
  	  public  Hashtable sampleProbabilitiesSubjectTo(String NAME_FILE_CONTRAINTS) {
  		  //HashMap probSample=new HashMap();
  		  Iterator it=listVarProb.iterator();
  		  String objective="";
  		  while(it.hasNext()){
  			  String prob=(String)it.next();
  			  double w=Math.random();
  			  double sign=Math.random();
  			  int signW=1;
  			  if (sign<=0.5){
  				  signW=-1;
  			  }
  			  if (objective==""){
  				objective=w*signW+"*p"+prob;
  			  }
  			  else{
  				  objective=w*signW+"*p"+prob+"+"+objective;
  			  }
  			  
  		  }
  		  createFileAMPL(objective,NAME_FILE_CONTRAINTS);
  		  Double obj=callNonLinearSolver(); //it creates the currentValueProbabilities
  		  return currentValuesProb;
		  

	}
  	public Integer convertCPT(Integer F, Hashtable sampleProbabilities) {
		if(this.isTerminalNode(F)){
			//evaluate the parameterized leave with the sampleProbabilities 
    		return evaluate(F,sampleProbabilities);
    	}
    	Integer Fr= (Integer) reduceConvert.get(F);
    	if(Fr==null){
    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
    		Integer Fh=convertCPT(intNodeKey.getHigh(),sampleProbabilities);
    		Integer Fl=convertCPT(intNodeKey.getLower(),sampleProbabilities);
    		Integer Fvar= intNodeKey.getVar();
    		Fr=(Integer)GetNode(Fvar,Fh,Fl);
    		reduceConvert.put(F, Fr);
    		
    	}
    	return Fr;
		
	}
	private Integer evaluate(Integer F, Hashtable sampleProbabilities) {
		TerminalNodeKeyPar node=(TerminalNodeKeyPar)this.getInverseNodesCache().get(F);
		double evalWithValues=node.getPolynomial().evalWithListValues(sampleProbabilities,this);
    	return (Integer)this.getTerminalNode(evalWithValues); 
	}
	
	public  Object readValueFunction(String NAME_FILE_VALUE) {
		Object valueRes=null;
		HashMap fileId2NewId=new HashMap();
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(NAME_FILE_VALUE));

			String line = null;
			while (( line = input.readLine()) != null){
				String[] list = line.split(" ");
				Integer id=null;
				if (list.length==2){
					//create a terminal node
					Object o = list[1];
					if (o instanceof String && HasOnlyDigits((String) o)) {
						double val = (new BigInteger((String) o)).doubleValue();
						id=(Integer)this.getTerminalNode(val);
					} else {
						Double val = Double.valueOf((String)o);
						id=(Integer)this.getTerminalNode(val);
					}
					fileId2NewId.put(Integer.valueOf(list[0]), id);
					valueRes=id; // faltaba esta linea
				}
				else if (list.length==4){
					//create a internal node
					 Integer varId = Integer.valueOf(list[1]);
					 Object high= fileId2NewId.get(Integer.valueOf(list[2]));
					 Object low= fileId2NewId.get(Integer.valueOf(list[3]));
					 id=(Integer) this.computeFinalADDForVarIDFromHighLow(varId, high, low);
					 fileId2NewId.put(Integer.valueOf(list[0]), id);
					 valueRes=id;
				}
				else{
					System.out.println("Problem with File Value format");
					System.exit(0);
				}

			}
			input.close();
		} catch (IOException e) {
			System.out.println("Problem reading File Value");
			System.exit(0);
		}	

		return valueRes;


	}
////////////////////////////////////FOR Factored RTDP BRTDP////////////////////////////////////////////////////////
	
	public Object insertValueInDD(Object F, TreeMap<Integer, Boolean> state, double value, Iterator it,HashMap hmPrimeRemap) {
		Object Fh, Fl;
		if (!it.hasNext()){//means that we are in a leaf then we need to replace the value
			return getTerminalNode(value);
		}
		Integer var=(Integer)it.next();
		Boolean val=state.get(var);	
		Integer varPrime=(Integer) hmPrimeRemap.get(var);
		if(varPrime==null){ // this if was inserted for RTDPEnum and BRTDPEnum because we want to serve for states with prime or non-prime variables
			varPrime=var;
		}
		
		if(this.isTerminalNode(F)){
			// means that we need to create the nodes with the remain variables
	
			if (val==true){
				Fh = insertValueInDD(F, state, value, it,hmPrimeRemap);
				Fl = F;
			}
			else {//val=false
				Fh = F;
				Fl = insertValueInDD(F, state, value, it,hmPrimeRemap);
			}
			return GetNode(varPrime,Fh,Fl);
		}

		//Integer Fr= (Integer) reduceCache.get(F);

		//if(Fr==null){
		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
		Integer Fvar= intNodeKey.getVar();
		if(Fvar.compareTo(varPrime)==0){
			if (val==true){
				Fh = insertValueInDD(intNodeKey.getHigh(), state, value, it,hmPrimeRemap);
				Fl = intNodeKey.getLower();
			}
			else {//val=false
				Fh = intNodeKey.getHigh();
				Fl = insertValueInDD(intNodeKey.getLower(), state, value, it,hmPrimeRemap);
			}	
			return GetNode(varPrime,Fh,Fl);

		}
		//Fvar.compareTo(var)!=0
		if (val==true){
			Fh = insertValueInDD(F, state, value, it,hmPrimeRemap);
			Fl = F;
		}
		else {//val=false
			Fh = F;
			Fl = insertValueInDD(F, state, value, it,hmPrimeRemap);
		}
		return GetNode(varPrime,Fh,Fl);

		//reduceCache.put(F, Fr);
		//}
	}

	public abstract Object getValuePolyForStateInContext(Integer F,TreeMap<Integer, Boolean> state,Integer xiprime,Boolean valXiprime);
	public abstract Double getValueForStateInContext(Integer F,TreeMap<Integer, Boolean> state,Integer xiprime,Boolean valXiprime);
	public abstract Double getRewardForStateInContextEnum(Integer F,State state,int numVars);
	public abstract Double getProbCPTForStateInContextEnum(Integer F,State state,Integer xiprime,Boolean valXiprime, int numVars);
	
	

	public void setProbWeightVGap(Object F,TreeMap<Integer, Boolean> state, TreeMap iD2ADD) {
		// TODO: if F is not dirty, just return without updating further
		//       make sure to mark node as not dirty after update
	   	if(isTerminalNode(F)){
	   		TerminalNodeKeyADD node=((TerminalNodeKeyADD)inverseNodesCache.get(F));
	   		node.setprobWeightH(node.getValue());
	   		node.setprobWeightL(0);
	   		return;
    	}
    	if(!reduceCacheWeighted.contains(F)){
     		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
     		setProbWeightVGap(intNodeKey.getHigh(),state,iD2ADD);
    		setProbWeightVGap(intNodeKey.getLower(),state,iD2ADD);
    		Integer Fvar= intNodeKey.getVar();
    		Object cpt_a_xiprime=iD2ADD.get(Fvar);
    		if(cpt_a_xiprime==null){
    			System.out.println("Prime var not found");
    			System.exit(1);
    		}
    		double probTrue=getValueForStateInContext((Integer)cpt_a_xiprime,state,Fvar,true);
			double probFalse=1-probTrue;
			NodeKey H=(NodeKey)inverseNodesCache.get(intNodeKey.getHigh());
			NodeKey L=(NodeKey)inverseNodesCache.get(intNodeKey.getLower());
			double weightH=probTrue*(H.getprobWeightH()+H.getprobWeightL());
			double weightL=probFalse*(L.getprobWeightH()+L.getprobWeightL());
    		intNodeKey.setprobWeightH(weightH);
    		intNodeKey.setprobWeightL(weightL);
    		reduceCacheWeighted.add(F);
    	}
    
		return;
	}
		
	///////////////////for MP//////////////////////////////////////////////////////////////////////////////
	 public void createFileAMPLMP(String objective,String NAME_FILE_CONTRAINTS_MP, String NAME_FILE_CONTRAINTS,ArrayList listBasisFunctions, HashSet nameNewVariables) {
	 		
	     	try {
	     		
	             BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_AMPL));
	             out.write(SOLVER);
	             out.append(System.getProperty("line.separator"));
	             Iterator it=listVarProb.iterator();
	             while(it.hasNext()){
	            	 out.append("var p" + it.next() + ">=0, <=1;");
	            	 out.append(System.getProperty("line.separator"));
	             }
	             //print w
	             for(int i=0;i<=listBasisFunctions.size();i++){
	            	 out.append("var w" + i + ">=-1000, <=1000;");
	            	 out.append(System.getProperty("line.separator"));      	            	 
	             }
	             //print newVariables
	             Iterator itvar=nameNewVariables.iterator();
	             while(itvar.hasNext()){
	            	 out.append("var " + itvar.next()+";");
	            	 out.append(System.getProperty("line.separator"));   
	             }
	              
	             //print obj
	             out.append("minimize obj: " + objective + ";");
	             out.append(System.getProperty("line.separator"));
	             
	             //copy the original contraints
	             BufferedReader input2= new BufferedReader(new FileReader(NAME_FILE_CONTRAINTS));
	             String line2 = null;
	             while (( line2 = input2.readLine()) != null){
	                 out.append(line2);
	                 out.append(System.getProperty("line.separator"));
	             }
	             
	             
	             //copy the new set of constraints
	             BufferedReader input= new BufferedReader(new FileReader(NAME_FILE_CONTRAINTS_MP));
	             String line = null;
	             while (( line = input.readLine()) != null){
	                 out.append(line);
	                 out.append(System.getProperty("line.separator"));
	             }

	             
	             
	             
	             
	             out.append("solve;");
	             out.append(System.getProperty("line.separator"));
	             //print the probabilities founded
	             Iterator it1=listVarProb.iterator();
	             while(it1.hasNext()){
	            	 String pnumber=(String)it1.next();
	            	 out.append("print 'p" + pnumber + "',p"+ pnumber+";");
	            	 out.append(System.getProperty("line.separator"));
	             }
	             //print w founded
	             for(int i=0;i<=listBasisFunctions.size();i++){
	            	 out.append("print 'w" + i + "',w"+ i+";");
	            	 out.append(System.getProperty("line.separator")); 
	             }
	             
	             input.close();
	             out.close();
	         } catch (IOException e) {
	         	System.out.println("Problem with the creation AMPL file for MP");
	         	System.exit(0);
	         }

	 	}

}