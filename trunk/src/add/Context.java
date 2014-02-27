package add;
import graph.Graph;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import util.Pair;
import logic.lattice.Lattice;
import mdp.Action;
import mdp.Config;
import mdp.PolynomialTransform;
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
    protected Hashtable reduceCacheMaxPar=new Hashtable();
    public static String  NAME_FILE_AMPL = Config.getConfig().getAmplTempFile();
    public final static String  NAME_FILE_AMPL_BOUNDS = Config.getConfig().getAmplBoundTempFile();
    
    HashMap probBound=new HashMap();  // String prob -> array[lower,upper]
    
    public long linearSolverElapsedTime = 0;
    public int reuseCacheIntNode=0,clash=0, noclash=0,contReuse=0,contNoReuse=0,numberReducedToValue=0,contReuseUsingLattice=0;
    public Hashtable<String, Double> currentValuesProb=new Hashtable<String, Double>();     //  idProb--> valProb
    public Hashtable<String, Double> probSample;     //  idProb--> valProb
    
    protected Hashtable currentDirectionList=new Hashtable();
    protected Hashtable cacheResultsSolver=new Hashtable();
    
    //public final static String  NAME_FILE_AMPL="C:\\cygwin\\home\\karina\\ADD\\ADD\\reportsMDPIP\\temporal.mod";
    public final static String  SOLVER="option solver minos;";
	
	 // For printing
    public static DecimalFormat _df = new DecimalFormat("#.######", new DecimalFormatSymbols(Locale.ENGLISH));
    
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
	public int numCallNonLinearSolver=0;
	public int numCallLinearSolver=0;
	 
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
		 int start = 0;
		 
		 if (s.startsWith("-")) start++;
		 
		 for (int i = start; i < s.length(); i++) {
			 if (!Character.isDigit(s.charAt(i)))
				 return false;
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
	
		protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS) {
			this.createFileAMPL(objective, NAME_FILE_CONTRAINTS, "min", null);
		}
		
		protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS, HashMap<String, List<String>> constraintsPerParameter) {
			this.createFileAMPL(objective, NAME_FILE_CONTRAINTS, "min", constraintsPerParameter);
		}
		
		protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS, HashMap<String, List<String>> constraintsPerParameter, String[] params) {
			this.createFileAMPL(objective, NAME_FILE_CONTRAINTS, "min", constraintsPerParameter, params);
		}
		
		protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS, String optimizationType) {
			this.createFileAMPL(objective, NAME_FILE_CONTRAINTS, optimizationType, null);
		}
		
		protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS, String optimizationType, 
	    		HashMap<String, List<String>> constraintsPerParameter) {
			
		}
		
	    protected void createFileAMPL(String objective, String NAME_FILE_CONTRAINTS, String optimizationType, 
	    		HashMap<String, List<String>> constraintsPerParameter, String[] params) {
	    	BufferedWriter out = null;
	    	BufferedReader input = null;
	    	
	     	try {
	     		String extension = NAME_FILE_AMPL.substring(NAME_FILE_AMPL.lastIndexOf('.'));	
	     		String fileWithoutExtension = NAME_FILE_AMPL.replace(extension, "");
	     		
	     		if (fileWithoutExtension.contains("_"))
	     			fileWithoutExtension = fileWithoutExtension.replace(fileWithoutExtension.substring(fileWithoutExtension.lastIndexOf('_')), "");
	     		
	     		NAME_FILE_AMPL = fileWithoutExtension + "_" + System.currentTimeMillis() + extension;
	     		
				out = new BufferedWriter(new FileWriter(NAME_FILE_AMPL));
				
				if (params == null)
					writeVarObjective(objective, out, optimizationType);
				else
					writeVarObjective(objective, out, optimizationType, params);
				
				//constraints print
				if (constraintsPerParameter == null) {
					input = new BufferedReader(new FileReader(NAME_FILE_CONTRAINTS));
					String line = null;
		            while (( line = input.readLine()) != null){
		            	out.append(line);
		                out.append(System.getProperty("line.separator"));
		            }	
				}
				else {
					int restrictionCount = 1;
					
					for (String parameter : constraintsPerParameter.keySet()) {						
						for (String line : constraintsPerParameter.get(parameter)) {
							out.append("subject to r" + restrictionCount + ":  " + line);
			                out.append(System.getProperty("line.separator"));
			                restrictionCount++;
						}
					}
				}
	            
				if (params == null)
					writeProbFounded(out);
				else
					writeProbFounded(out, params);
				
	     	} catch (IOException e) {
	     		System.out.println("Problem with the creation AMPL file.");
	         	System.err.println("Error: " + e);
	         	e.printStackTrace(System.err);
	         	System.exit(0);
	        }
	     	finally 
	     	{
				try {
					if (input != null) input.close();
					if (out != null) out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	     	}

	 	}
	    
	    private void writeVarObjective(String objective, BufferedWriter out) throws IOException {
	    	this.writeVarObjective(objective, out, "min");
	    }
	    
		private void writeVarObjective(String objective, BufferedWriter out, String optimizationType) throws IOException {
			String optimization = "minimize";
			if (optimizationType.equals("max"))
				optimization = "maximize";
			
	    	out.write(SOLVER);
            out.append(System.getProperty("line.separator"));
            Iterator it=listVarProb.iterator();
            while(it.hasNext()){
           	 out.append("var p" + it.next() + ">=0, <=1;");
           	 out.append(System.getProperty("line.separator"));
            }
            
            out.append(optimization + " obj: " + objective + ";");
            out.append(System.getProperty("line.separator"));
			
		}
		
		private void writeVarObjective(String objective, BufferedWriter out, String optimizationType, String[] parameters) throws IOException {
			String optimization = "minimize";
			if (optimizationType.equals("max"))
				optimization = "maximize";
			
	    	out.write(SOLVER);
            out.append(System.getProperty("line.separator"));
            
            for (String param : parameters) {
            	out.append("var " + param + ">=0, <=1;");
              	out.append(System.getProperty("line.separator"));	
			}
            
            out.append(optimization + " obj: " + objective + ";");
            out.append(System.getProperty("line.separator"));
			
		}

		private void writeProbFounded(BufferedWriter out, String[] parameters) throws IOException {
	    	out.append("solve;");
            out.append(System.getProperty("line.separator"));
            
            for (String param : parameters) {
           	 	out.append("print '" + param + "',"+ param+";");
           	 	out.append(System.getProperty("line.separator"));
            }
			
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
	    
	    private Double callSolver(boolean nonLinearSolverCall) {
	    	if (nonLinearSolverCall)
	    		this.numCallNonLinearSolver++;
	    	else
	    		this.numCallLinearSolver++;
	    	 
	    	Double obj = null;
 
	    	try {
	    		// Open files for reading and writing
	    		Process pros = Runtime.getRuntime().exec("ampl " + NAME_FILE_AMPL);
	    		 
	    		BufferedReader process_out = new BufferedReader(new InputStreamReader(pros.getInputStream()));
	    		
	    		PrintWriter    process_in  = new PrintWriter(pros.getOutputStream(), true);
	    		process_in.close(); // Need to close input stream so process exits!!!
	    		
	    		currentValuesProb = new Hashtable<String, Double>();
				currentValuesW = new Hashtable<String, Double>();
	    		
	    		ArrayList<String> lines = new ArrayList<String>();
	    		
	    		// Provide input to process (could come from any stream)
	    		String temp = null;

	    		while ((temp = process_out.readLine()) != null)
	    			lines.add(temp);    		

				process_out.close();

				pros.waitFor();
	    		
				//parse the process output
				
	    		final int PARSE_START 		= 0;
	    		final int OBJECTIVE_READ 	= 1;
	    		
	    		int currentState = PARSE_START;
	    		
	    		for (String line : lines) {
					if (currentState == PARSE_START) {
						if (line.contains("objective")) {
							if (line.endsWith(".")) 
								line = line.substring(0, line.length() - 1);
							
							int pos = line.lastIndexOf(" ");
							obj = Double.valueOf(line.substring(pos)); //pos + characters of objective + 1
							currentState = OBJECTIVE_READ;
						}
					}
					else if (currentState == OBJECTIVE_READ) {		
						boolean startsWithP = line.matches("p[0-9].+");
						boolean startsWithW = line.matches("w[0-9].+");
						
						if (startsWithP || startsWithW) {
							String[] splittedLine = line.split(" ");
							
							if (splittedLine != null && splittedLine.length >= 2) {
								String parameterName = splittedLine[0].substring(1);
								String parameterValueAsString = splittedLine[1];
								
								double parameterValue = Double.valueOf(parameterValueAsString);
								
								if (startsWithP)
									currentValuesProb.put(parameterName, parameterValue);
								else if (startsWithW)
									currentValuesW.put(parameterName, parameterValue);
							}
						}
					}
				}
	    		
				if (pros.exitValue() != 0)
				{
					for (String line : lines)
						System.err.println(line);
					
					return null;
				}
				else
				{
					new File(NAME_FILE_AMPL).delete();
					return obj;
				}
				
	    	 } catch (InterruptedException ie) {
	    		 ie.printStackTrace(System.err);
	    		 return null;
	    	 } catch (IOException ioe) {
	    		 ioe.printStackTrace(System.err);
	    		 return null;
	    	 }
	    }
	    
	    /**
	     * call the non linear solver and fill currentValuesProb with the probabilities  
	     */
	    public Double callNonLinearSolver() {
	    	return callSolver(true);
		}
	    
	    public Double callLinearSolver() {
	    	return callSolver(false);
		}
	    
	    public String[] getParameterFromPolynomial(Polynomial polynomial) {
			Set<String> parameters = new HashSet<String>();
			
			Object[] vars = polynomial.getTerms().keySet().toArray();
			
			for (Object var : vars) {
				String paramsWithComma = this.getLabelProd((Integer) var);
				
				String[] params = paramsWithComma.split(",");
				
				for (String param : params)
					parameters.add("p" + param);			
			}
			
			return parameters.toArray(new String[0]);
		}
	    
	    //the parameter is ParADD and the result is an ADD
	    public abstract Object doMinCallOverNodes(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt);
	    
	    public Object doMinCallOverNodes(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt, 
	    		HashMap<String, List<ArrayList>> constraintsPerParameter) {

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
	    			 //filter params
	    			 HashMap<String, List<String>> constraints = new HashMap<String, List<String>>();
	    			 
	    			 for (String parameter : getParameterFromPolynomial(node.getPolynomial())) {
	    				 List<String> lines = new ArrayList<String>();
	    				 
	    				 for (ArrayList item : constraintsPerParameter.get(parameter)) {
	    					 String line = "";
	    					 
	    					 for (int i=0;i< item.size();i++)
	    						 line += item.get(i);
	    					 
	    					 line += ";";
		    				 
		    				 lines.add(line);
						 }
	    				 
	    				 constraints.put(parameter, lines);
					 }
	    			 
	    			 createFileAMPL(node.getPolynomial().toString(this,"p"),NAME_FILE_CONTRAINTS, constraints);
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
	    		 Object Fh = doMinCallOverNodes(intNodeKey.getHigh(),NAME_FILE_CONTRAINTS,pruneAfterEachIt, constraintsPerParameter);
	    		 Object Fl = doMinCallOverNodes(intNodeKey.getLower(),NAME_FILE_CONTRAINTS,pruneAfterEachIt, constraintsPerParameter);
	    		 Integer Fvar= intNodeKey.getVar();
	    		 Fr=(Integer)this.GetNode(Fvar,Fh,Fl);
	    		 reduceCacheMinPar.put(VDD, Fr);
	    	 } else	{
	    		 reuseCacheIntNode++;
	    	 }
	    	 
	    	 ///////////////////////////////////////////////////////////////////////////////////////////////////
	    	 return Fr;//could be integer or AditArc
	     }
	    
	    //test for modified spudd
	    public Object doMinCallOverNodes2(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt, 
	    		HashMap<String, List<ArrayList>> constraintsPerPolytope) {

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
	    			 //filter params
	    			 HashMap<String, List<String>> constraints = new HashMap<String, List<String>>();
	    			 
	    			 String cacheKey = "";
	    			 
	    			 String[] params = getParameterFromPolynomial(node.getPolynomial());
	    			 Arrays.sort(params);
	    			 
	    			 for (String parameter : params)
	    				 cacheKey += (parameter + ".");
	    			 
	    			 List<String> lines = new ArrayList<String>();
    				 
    				 for (ArrayList item : constraintsPerPolytope.get(cacheKey)) {
    					 String line = "";
    					 
    					 for (int i=0;i< item.size();i++)
    						 line += item.get(i);
    					 
    					 line += ";";
	    				 
	    				 lines.add(line);
					 }
    				 
    				 constraints.put("", lines);
	    			 
	    			 createFileAMPL(node.getPolynomial().toString(this,"p"),NAME_FILE_CONTRAINTS, constraints, params);
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
	    		 Object Fh = doMinCallOverNodes2(intNodeKey.getHigh(),NAME_FILE_CONTRAINTS,pruneAfterEachIt, constraintsPerPolytope);
	    		 Object Fl = doMinCallOverNodes2(intNodeKey.getLower(),NAME_FILE_CONTRAINTS,pruneAfterEachIt, constraintsPerPolytope);
	    		 Integer Fvar= intNodeKey.getVar();
	    		 Fr=(Integer)this.GetNode(Fvar,Fh,Fl);
	    		 reduceCacheMinPar.put(VDD, Fr);
	    	 } else	{
	    		 reuseCacheIntNode++;
	    	 }
	    	 
	    	 ///////////////////////////////////////////////////////////////////////////////////////////////////
	    	 return Fr;//could be integer or AditArc
	     }
	    
	    protected Object callNonLinearSolverObjectiveIP(TerminalNodeKeyPar node, String name_file_contraints) {
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
	    
	    public abstract Object doMaxCallOverNodes(Object VDD, String NAME_FILE_CONTRAINTS, boolean pruneAfterEachIt);
	     
		 public abstract int contNumberNodes(Object valueiDD);
///////////////////////////////////////////////////////////////////
		 //create file in order to obtain a bound of each probability
		 public HashMap createBoundsProb(String NAME_FILE_CONTRAINTS) {
			 probBound = new HashMap();
			 Iterator it = listVarProb.iterator();
             
			 while (it.hasNext()){
            	 String prob = (String) it.next();
            	 
            	 createFileAMPL("p" + prob, NAME_FILE_CONTRAINTS);
            	 Double objP = callNonLinearSolver();
            	 
            	 createFileAMPL("-p" + prob, NAME_FILE_CONTRAINTS);
            	 Double objN = callNonLinearSolver();
            	 
            	 ArrayList bounds = new ArrayList();
            	 bounds.add(Math.min(Math.abs(objP), Math.abs(objN)));
            	 bounds.add(Math.max(Math.abs(objP), Math.abs(objN)));
                 probBound.put(prob, bounds);
             }
			 
             return probBound;
		 }
		 
		 //	create file in order to get probabilities for RTDP-IP sample
		 public void getProbSampleCallingSolver(String NAME_FILE_CONTRAINTS_GREATERZERO) {
			 
			 createFileAMPL("0", NAME_FILE_CONTRAINTS_GREATERZERO);
			 
			 Double obj = callNonLinearSolver(); //fill   currentValuesProb and currentValuesW
			 
			 if (obj == null) {
				 System.out.println("createProbSample: Problems with the solver. It returns null");
				 System.exit(0);
			 }
			 
             probSample = new Hashtable(currentValuesProb);	
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
        
    	    	
 		
		
	 	  public Double getValueForStateInADD(Integer F, HashMap state, HashMap nextState, Integer xiprime, Object valXiprime){
		  
	    	if(isTerminalNode(F)){
	    		return ((TerminalNodeKeyADD)inverseNodesCache.get(F)).getValue();
	    	}
	    	InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)inverseNodesCache.get(F);
	    	Integer Fvar= intNodeKey.getVar();
	    	
	    	Object val = state.get(Fvar);
	    	
	    	if (val == null)//var is in the nextState
	    		val = (Integer) nextState.get(Fvar);	
	    	
	    	if (val == null && Fvar != null && Fvar.compareTo(xiprime) == 0)
	    		val = valXiprime;

	    	if (val == null) {
	    		System.out.println("There is not  variable Fvar ");
	    		System.exit(0);
	    	}
	    	
	    	Boolean valAsBoolean = false;
	    	
	    	if (val instanceof Boolean)
	    		valAsBoolean = (Boolean) val;
	    	else if (val instanceof Integer) {
	    		Integer valAsInt = (Integer) val;
	    		valAsBoolean = (valAsInt == 1);
	    	}
	    	
	    	Double reward = null;
	    	
	    	if (valAsBoolean) 
	    		reward = getValueForStateInADD(intNodeKey.getHigh(), state, nextState, xiprime, valXiprime);
	    	else
	    		reward = getValueForStateInADD(intNodeKey.getLower(), state, nextState, xiprime, valXiprime);
	    	
	    	return reward;
  	  }
	 	  
	private double[] getRandomCoefficients(int numberOfCoefficients) {
		double[] coefficients = new double[numberOfCoefficients];
		
		for (int i = 0; i < coefficients.length; i++) {
			double coefficient = Math.random();
			double signal = Math.random();
			
			if (signal > 0.5)
				coefficients[i] = coefficient;
			else
				coefficients[i] = -coefficient;
		}
		
		return coefficients;
	}
	
	private String generateRandomObjective() {
		double[] coefficients = this.getRandomCoefficients(listVarProb.size());
		
		String objective = "";
  		
		Iterator iter = listVarProb.iterator();
		
		for (int i = 0; i < coefficients.length; i++) {
			String prob = (String) iter.next();
			
			if (objective.isEmpty())
				objective = (coefficients[i] + "*p" + prob);
			else
				objective = (coefficients[i] + "*p" + prob + "+" + objective);
		}
		
		return objective;
	}
	
	public Hashtable<String, Double> sampleProbabilitiesSubjectTo(String NAME_FILE_CONTRAINTS) {
		String objective = generateRandomObjective();
  		
		Hashtable<String, Double> randomProbabilities = new Hashtable<String, Double>();
		
		if (objective != null && !objective.isEmpty()) {
			
			long initialTime = System.currentTimeMillis();
			
			createFileAMPL(objective, NAME_FILE_CONTRAINTS, "min");
			callLinearSolver();
			
			Hashtable<String, Double> firstPoint = currentValuesProb;
			
			createFileAMPL(objective, NAME_FILE_CONTRAINTS, "max");
			
			callLinearSolver();
			
			Hashtable<String, Double> secondPoint = currentValuesProb;
			
			double signedRandomNumber = Math.random();
			signedRandomNumber = (Math.random() > 0.5 ? signedRandomNumber : -signedRandomNumber);
			
			for (String key : firstPoint.keySet()) {
				double mean = (firstPoint.get(key) + secondPoint.get(key)) / 2;
				double halfDist = Math.abs(firstPoint.get(key) - secondPoint.get(key)) / 2;

				double pointDimensionValue = mean + halfDist * signedRandomNumber;
				randomProbabilities.put(key, pointDimensionValue);
			}
			
			this.probSample = randomProbabilities;
			long elapsedTime = System.currentTimeMillis() - initialTime;
			this.linearSolverElapsedTime += elapsedTime;
		}
		
		return randomProbabilities;
	}
	
	public Hashtable<String, Double> getProbabilitiesSubjectTo(String NAME_FILE_CONTRAINTS, Polynomial poly) {
		String objective = poly.toString(this, "p");
		
		createFileAMPL(objective, NAME_FILE_CONTRAINTS, "min");
  		callNonLinearSolver();
  		
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
	public abstract Object getProbCPTForStateInContextEnum(Integer F,State state,Integer xiprime,Boolean valXiprime, int numVars);

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
	 public void createFileAMPLMP(String objective, String NAME_FILE_CONTRAINTS_MP, String NAME_FILE_CONTRAINTS,
			 ArrayList listBasisFunctions, HashSet nameNewVariables) {
		 try {
			 BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_AMPL));
	         out.write(SOLVER);
	         out.append(System.getProperty("line.separator"));
	         
	         Iterator it=listVarProb.iterator();
	         
	         while (it.hasNext()) {
	        	 out.append("var p" + it.next() + ">=0, <=1;");
	        	 out.append(System.getProperty("line.separator"));
	         }
	         
	         //print w
	         for (int i = 0; i <= listBasisFunctions.size(); i++){
	        	 out.append("var w" + i + ">=-1000, <=1000;");
	        	 out.append(System.getProperty("line.separator"));      	            	 
	         }
	         
	         //print newVariables
	         Iterator itvar = nameNewVariables.iterator();
	         
	         while (itvar.hasNext()){
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
	         
	         while (it1.hasNext()){
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
	 
	 /**
	  * Enumerate PADD leaves building a list of pairs, where the first item is a partial state 
	  * and the second item is a polynomial
	  * @param id PADD id
	  * @return List of leaves in a PADD
	  */
	 public List<Pair> enumeratePolyInLeaves(int id) {
		 List<Pair> leaves = new ArrayList<Pair>();
		 
		 this.enumeratePolyInLeaves(id, leaves, new TreeMap<Integer, Boolean>());
		 
		 return leaves;
	 }
	 
	 private void enumeratePolyInLeaves(int id, List<Pair> leaves, TreeMap<Integer, Boolean> assign) {
		 Boolean b;
		 
		 NodeKey cur = this.getNodeInverseCache(id);
		
		 if (cur instanceof InternalNodeKey) {    			
			 InternalNodeKey ni = (InternalNodeKey) cur;    
			 
			 Integer var_id = ni.var;
		
			 assign.put(var_id, false);
			 enumeratePolyInLeaves((Integer) ni.getLower(), leaves, assign);
			 
			 assign.put(var_id, true);
			 enumeratePolyInLeaves((Integer) ni.getHigh(), leaves, assign);
		     
			 assign.remove(var_id);
			 return;
		 }
		 
		 //If get here, cur will be an ADDDNode, ADDBNode		 
		 if (cur instanceof TerminalNodeKeyPar) {
			 Polynomial poly = ((TerminalNodeKeyPar) cur).getPolynomial();  // the result is a poly 
			 
			 TreeMap<Integer, Boolean> partialState = new TreeMap<Integer, Boolean>(assign);
			 leaves.add(new Pair(partialState, poly));
		 }
	 }
	 
	 public void changePolyInLeaves(int id, PolynomialTransform transformFunction) {		 
		 this.changePolyInLeaves(id, new TreeMap<Integer, Boolean>(), transformFunction);
	 }
	 
	 private void changePolyInLeaves(int id, TreeMap<Integer, Boolean> assign, PolynomialTransform transformFunction) {
		 Boolean b;
		 
		 NodeKey cur = this.getNodeInverseCache(id);
		
		 if (cur instanceof InternalNodeKey) {    			
			 InternalNodeKey ni = (InternalNodeKey) cur;    
			 
			 Integer var_id = ni.var;
		
			 assign.put(var_id, false);
			 changePolyInLeaves((Integer) ni.getLower(), assign, transformFunction);
			 
			 assign.put(var_id, true);
			 changePolyInLeaves((Integer) ni.getHigh(), assign, transformFunction);
		     
			 assign.remove(var_id);
			 return;
		 }
		 
		 //If get here, cur will be an ADDDNode, ADDBNode		 
		 if (cur instanceof TerminalNodeKeyPar) {
			 Polynomial poly = ((TerminalNodeKeyPar) cur).getPolynomial();  // the result is a poly 
			 
			 ((TerminalNodeKeyPar) cur).setPolynomial(transformFunction.transform(poly));
		 }
	 }
	 
	 public void enumeratePaths(int id, ADDLeafOperation leaf_op) {
		 enumeratePaths(id, leaf_op, new TreeMap<Integer, Boolean>());
	 }
	 
	 private void enumeratePaths(int id, ADDLeafOperation leaf_op, TreeMap<Integer, Boolean> assign) {

		Boolean b;
		 
		NodeKey cur = this.getNodeInverseCache(id);

	    if (cur instanceof InternalNodeKey) {    	
	    	/*
	    	 * int level = ((Integer) _hmGVarToLevel.get(new Integer(
                                        ((ADDINode) cur)._nTestVarID))).intValue();
                        Integer var_id = (Integer)_alOrder.get(level);
                        String var = (String)_hmID2VarName.get(var_id);

                        ADDINode ni = (ADDINode) cur;
	    	 * */
	    	
	    	InternalNodeKey ni = (InternalNodeKey) cur;    
	    	 
            Integer var_id = ni.var;

            assign.put(var_id, false);
            enumeratePaths((Integer) ni.getLower(), leaf_op, assign);
             
            assign.put(var_id, true);
            enumeratePaths((Integer) ni.getHigh(), leaf_op, assign);
             
            assign.remove(var_id);
            return;
	    }
         
		// If get here, cur will be an ADDDNode, ADDBNode
	    Object leaf_val = null;
	     
		if (cur instanceof TerminalNodeKeyADD)
			leaf_val = ((TerminalNodeKeyADD) cur).getValue(); //the result is a Double
		else if (cur instanceof TerminalNodeKeyPar)
			leaf_val = ((TerminalNodeKeyPar) cur).getPolynomial();  // the result is a poly
	     
		leaf_op.processADDLeaf(assign, leaf_val);
	}
}