package add;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class Polynomial {
	
	
	Double c;
	Hashtable terms;   //Integer(idProd)--> Double (coef)
	double currentError=0d;
	Context contextPol;  //context of which this action is a part
	 public Polynomial(Double c,Hashtable terms,Context context) {
			this.c=c;
			this.terms=terms;
			this.contextPol=context;
	}
	public Hashtable getTerms(){
		return terms;
	}
	public Double getC(){
		return c;
	}

	
	public Polynomial(Polynomial pol,Context context){
		this.c=pol.c;
		this.terms=new Hashtable(pol.terms);
		currentError=0d;
		this.contextPol=context;
	}

	public boolean equals(Object other) {
	        // Not strictly necessary, but often a good optimization
	        if (this == other)
	          return true;
	        if (!(other instanceof Polynomial))
	          return false;
	        Polynomial otherA = (Polynomial) other;
	        
	    	return (Math.abs(c - otherA.c) <= 1e-10d && termsEqual(otherA.terms));
	      }
	  public int hashCode() {
	 	    
			return (int)((Double.doubleToLongBits(c) >>> 25)+1)+termsHashCode();

	    }
	
	private int termsHashCode() {
	    int code=0;
		Iterator it=terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			code=code+(int)((Double.doubleToLongBits(((Double)terms.get(id)))>>> 25)+1)+id.hashCode();
		}		
		return code;
	}
	private boolean termsEqual(Hashtable terms2) {
		if(this.terms.size()!=terms2.size()){
			return false;
		}
		Iterator it=this.terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			if(!terms2.containsKey(id)){
				return false;
			}
			if(Math.abs(((Double)this.terms.get(id)) - ((Double)terms2.get(id)))> 1e-10d){
				return false;
			}
		}		
		return true;
	}
	
	public String toString(Context context,String nameVariable) {
		String stringPol=new String(Context._df.format(c));
		Iterator it=terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			//stringPol= stringPol +"+"+terms.get(id).toString() +"P"+id.toString();
			stringPol= stringPol +"+"+Context._df.format((Double)terms.get(id))+"*"+parseProb(((Context)context).getLabelProd(id),nameVariable);
		}
  	
		return stringPol;
	}
	
	public String toStringCoef(Context context) {
		String stringPol=new String(Context._df.format(c));
		Iterator it=terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			stringPol= stringPol +Context._df.format((Double)terms.get(id))+"  ";
		}
  	
		return stringPol;
	}
	
	
	public String toString() {
		if(this.contextPol!=null){
			return this.toString(this.contextPol,"p");
		}
		return this.toString();
			
	}
	
	
	public String toStringWithOutC(ContextADD context) {
		String stringPol=new String();
		Iterator it=terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			stringPol= stringPol +Context._df.format((Double)terms.get(id))+"*"+parseProb(((Context)context).getLabelProd(id),"p")+"+";
		}
		return stringPol.substring(0, stringPol.length()-1);
	}
	

	private String parseProb(String labelProd,String nameVariable) {
		String result=new String();
		String[] tokensLabel = labelProd.split(",");
		for (int i = 0; i < tokensLabel.length; ++i) {  
			result=result+nameVariable+tokensLabel[i]+"*";
		}
		return result.substring(0, result.length()-1);
	}




	public Polynomial sumPolynomial(Polynomial c2) {
		Hashtable resultTerms=new Hashtable(this.terms);
		Iterator it=c2.terms.keySet().iterator();
		while (it.hasNext()){
			  Integer id=(Integer)it.next();
			  Double coefThis=(Double)this.terms.get(id);
			  Double coefC2=(Double)c2.terms.get(id);
			  if (coefThis!=null){
				  if (Math.abs(coefThis+coefC2 - 0d) > 1e-10d){
					  resultTerms.put(id, coefThis+coefC2);
				  }
				  else
				  {
					  resultTerms.remove(id);
	
				  }
		      }
			  else{
					  resultTerms.put(id, coefC2);
			  }
		}		
		Polynomial resultPol=new Polynomial(this.c+c2.c,resultTerms,this.contextPol);
		return resultPol;
	}
	public Polynomial subPolynomial(Polynomial c2) {
		Hashtable resultTerms=new Hashtable(this.terms);
		Iterator it=c2.terms.keySet().iterator();
		while (it.hasNext()){
			  Integer id=(Integer)it.next();
			  Double coefThis=(Double)this.terms.get(id);
			  Double coefC2=(Double)c2.terms.get(id);
			  if (coefThis!=null){
				  resultTerms.put(id, coefThis-coefC2);
		      }
			  else{
				  resultTerms.put(id, -coefC2);
			  }
		}		
		Polynomial resultPol=new Polynomial(this.c-c2.c,resultTerms,this.contextPol);
		return resultPol;
		
		
	}
	
	
 
	public Polynomial prodPolynomial(Polynomial c2,Context context) {
		
				
		Hashtable resultTerms=new Hashtable();
		//multiplying the constants c
		Iterator it2Pol=c2.terms.keySet().iterator();
		if ( Math.abs(this.c - 0d) > 1e-10d){
			while (it2Pol.hasNext()){
				Integer id2=(Integer)it2Pol.next();
				Double coef2=(Double)c2.terms.get(id2);
				resultTerms.put(id2,this.c*coef2);			  
			}
		}
		Iterator itPol=this.terms.keySet().iterator();
		
		if (Math.abs(c2.c - 0d) > 1e-10d){
			while (itPol.hasNext()){
				Integer id=(Integer)itPol.next();
				Double coef=(Double)this.terms.get(id);
				Double coefResult=(Double)resultTerms.get(id);
				if (coefResult!=null){
					resultTerms.put(id,coefResult+c2.c*coef);  
				}
				else{
					resultTerms.put(id,c2.c*coef);  
				}
			}
		}
		//multiplying the terms 
	
		
		Iterator it=this.terms.keySet().iterator();
		while (it.hasNext()){
			Integer id=(Integer)it.next();
			Iterator it2=c2.terms.keySet().iterator();
			while (it2.hasNext()){
				Integer id2=(Integer)it2.next();
				Double coef=(Double)this.terms.get(id);
				Double coef2=(Double)c2.terms.get(id2);
				if((Math.abs(coef - 0d) > 1e-10d ||Math.abs(coef2 - 0d) > 1e-10d)){
					Integer resultId=getIdProd(id,id2,context);
					Double coefResult=(Double)resultTerms.get(resultId);
					if (coefResult!=null){
						resultTerms.put(resultId,coefResult+coef*coef2);  
					}
					else{
						resultTerms.put(resultId,coef*coef2);
					}
				}
			}
		}
		Polynomial resultPol=new Polynomial(this.c*c2.c,resultTerms,context);
		return resultPol;
		
	}



//TODO: it is only to prove if multiplication is working
	//despues tengo que rehacer ese metodo para que pida al contexto el id
private Integer getIdProd(Integer id, Integer id2, Context context) {
	//find ids in the list of labelProd
	String label=context.getLabelProd(id);
	String label2=context.getLabelProd(id2);
	//create the ordered label of the product
	String labelProd=createOrderedLabelProd(label, label2);
	return context.getIdLabelProd(labelProd);
	//return Integer.valueOf((id.toString()+id2.toString()));	
}




private String createOrderedLabelProd(String label, String label2) {
	String labelProd=new String();
	String[] tokensLabel = label.split(",");
	String[] tokensLabel2 = label2.split(",");
	int lengthRes=tokensLabel.length+tokensLabel2.length;
	String[] A=new String[lengthRes];
	String[] B=new String[lengthRes];
	//interacala
	int i,j,k;
	for (i = 0; i < tokensLabel.length; ++i) {  
		B[i]=tokensLabel[i];
	}
	for (j = tokensLabel2.length-1; j>=0; --j){
		B[i]=tokensLabel2[j];
	    i++;	
	}
	i=0;
	j=lengthRes-1;
	
	for(k = 0; k < lengthRes; ++k){
		if(Integer.parseInt(B[i])<=Integer.parseInt(B[j])){
			A[k]=B[i];
			i++;
		}
		else{
			A[k]=B[j];
			j--;
		}
	}
	if (lengthRes>=1){
		labelProd=A[0];
	}
	for(k = 1; k < lengthRes; ++k){
		labelProd=labelProd+","+A[k];	
	}
	return labelProd;
}

//for merge nodes
public double evaluateWith1Abs(Polynomial pol2) {
	Polynomial subPol=this.subPolynomial(pol2);
	double sum=0;
	Iterator it=subPol.terms.keySet().iterator();
	while (it.hasNext()){
		  Integer id=(Integer)it.next();
		  Double coef=(Double)subPol.terms.get(id);
		  sum=sum+Math.abs(coef);
	}		
    return sum+subPol.c;
}
public Polynomial divSimplePolynomial(double d) {
	Hashtable resultTerms=new Hashtable();
	Iterator it=this.terms.keySet().iterator();
	while (it.hasNext()){
		  Integer id=(Integer)it.next();
		  Double coefThis=(Double)this.terms.get(id);
		  resultTerms.put(id, coefThis/d);
	}		
	Polynomial resultPol=new Polynomial(this.c/d,resultTerms,this.contextPol);
	return resultPol;
}


public Polynomial avgPolynomial(Polynomial pol2) {
	Polynomial resultPol=this.sumPolynomial(pol2);
	resultPol=resultPol.divSimplePolynomial(2);
	return resultPol;
	}

public double evalWithListValues(Hashtable Values,Context context) {
	double sum=0;
	Iterator it=this.terms.keySet().iterator();
	while (it.hasNext()){
		  double multTerm=1;
		  Integer id=(Integer)it.next();
		  Double coef=(Double)this.terms.get(id);
		  String labelsProd=(String)context.getLabelProd(id);
		  String[] tokensLabel = labelsProd.split(",");
		  for (int i = 0; i < tokensLabel.length; ++i) {
			  multTerm=multTerm*(Double)Values.get(tokensLabel[i]);
		  }
		  sum=sum+coef*multTerm;
	}		
    return sum+this.c;
}


// Remove all terms with negative coef... if | sum coefs | < error return that poly else return original
public Polynomial aproxPol_Simple(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	 Polynomial newPolynomial=new Polynomial(this,context);
	 newPolynomial.currentError=0d;
	 Iterator it2=this.terms.keySet().iterator();
   	 while (it2.hasNext()){
   			  Integer idProd=(Integer)it2.next();
   			  double coef=(Double)newPolynomial.terms.get(idProd);
   			  if (coef<0d){
   				  newPolynomial.currentError+=-coef;
   				  newPolynomial.terms.remove(idProd);
   			  }
   	 }
	
   	 if (newPolynomial.currentError <= mergeError) {
   		 //System.out.println("Pruned poly");
   		 return newPolynomial;
   	 } else {
   		//System.out.println("Did not prune poly: " + newPolynomial.currentError + " : " + mergeError);
   		 return aproxPolPos(listVarProb,context, idsClash, mergeError);
   	 }
}
	
public Polynomial aproxPolPos(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	 Polynomial newPolynomial=new Polynomial(this,context);
	 newPolynomial.currentError=0d;
	 Iterator it2=this.terms.keySet().iterator();
  	 while (it2.hasNext()){
  			  Integer idProd=(Integer)it2.next();
  			  double coef=(Double)newPolynomial.terms.get(idProd);
  			  if (coef>0d){
  				  newPolynomial.currentError+=coef;
  				  newPolynomial.terms.remove(idProd);
  			  }
  	 }
	
  	 if (newPolynomial.currentError <= mergeError) {
  		 //System.out.println("Pruned poly");
  		 return newPolynomial;
  	 } else {
  		//System.out.println("Did not prune poly: " + newPolynomial.currentError + " : " + mergeError);
  		 return this;
  	 }
}
//aproximate by upper and lower
public Polynomial aproxPol(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	//TODO: working in it
	/*
	if (!context.containsClash(context.currentDirectionList)){
		return this;
		
	}*/
	Polynomial newPolynomial=new Polynomial(this,context);
	newPolynomial.currentError=0d;
	Iterator it2=this.terms.keySet().iterator();
	//percorre terms
	while (it2.hasNext() && newPolynomial.currentError<mergeError){
		Integer idProd=(Integer)it2.next();
		double coef=(Double)newPolynomial.terms.get(idProd);
		//find the term
		String labelsProd=(String)context.getLabelProd(idProd);
		String[] tokensLabel = labelsProd.split(",");
		//si alguna de las probabilidades esta en idsClash encuentra el d y el error
		Double error=new Double(0);
		ArrayList parValueErro=null;
		if (context.containsClash(context.currentDirectionList)){
			parValueErro=computeValueClash(tokensLabel,context.probBound,coef,idsClash,error);
		}
		else{
			parValueErro=computeValueNoClash(tokensLabel,context.probBound,coef,idsClash,error);
		}
        if(parValueErro !=null){
	         double sum= newPolynomial.currentError+((Double)parValueErro.get(1));
	         if (sum<= mergeError){
        	
        	 newPolynomial.terms.remove(idProd);
        	 newPolynomial.c=newPolynomial.c+((Double)parValueErro.get(0));
        	 newPolynomial.currentError=sum;
        	 //context.currentDirectionList=newPolynomial.constructDirectionList(listVarProb,context,idsClash);
	         }
	    }
	}
	return newPolynomial;
}



	
	
	

public Polynomial aproxPol_Upper_Lower_OnlyProbClash(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	
	if (!context.containsClash(context.currentDirectionList)){
		return this;
	}
	return aproxPol_Upper_Lower_OnlyProbIdsRemove(listVarProb,context,  idsClash,  mergeError,true);
}

public Polynomial aproxPol_Upper_Lower_OnlyProbIdsRemove(TreeSet listVarProb,Context context, ArrayList idsRemove, double mergeError,boolean usingIdsClash) {
	Polynomial newPolynomial=new Polynomial(this,context);
	newPolynomial.currentError=0d;
	Iterator it2=this.terms.keySet().iterator();
	//percorre terms
	while (it2.hasNext() && newPolynomial.currentError<mergeError){
		Integer idProd=(Integer)it2.next();
		double coef=(Double)newPolynomial.terms.get(idProd);
		//find the term
		String labelsProd=(String)context.getLabelProd(idProd);
		String[] tokensLabel = labelsProd.split(",");
		//if some of the probabilities is in the idsRemove find the new d and error
		Double error=new Double(0);
		ArrayList parTermErro=computeNewTerm(tokensLabel,context.probBound,coef,idsRemove,error);
		// parTermError is: [0]=dValue [1]=newLabelsProd [2]=error
		
        if(parTermErro !=null){
	         double sum= newPolynomial.currentError+((Double)parTermErro.get(2));
	         if (sum<= mergeError){
        	
	        	 newPolynomial.terms.remove(idProd);
	        	 if(((String)parTermErro.get(1)).compareTo("")!=0){
	        		 // if exists a term that has the same product of probabilities sum the coefficients else add the new term to the polynomial
	        		Integer id= context.getIdLabelProd((String)parTermErro.get(1));
	        		if(newPolynomial.terms.get(id)!=null){
	        			newPolynomial.terms.put(id, (Double)newPolynomial.terms.get(id)+(Double)parTermErro.get(0));
	        		}
	        		else{
	           	     newPolynomial.terms.put(id,parTermErro.get(0));
	        		}
	        	 }
	        	 else{
	        		 newPolynomial.c=newPolynomial.c+(Double)parTermErro.get(0);
	        	 }
	        	 newPolynomial.currentError=sum;
	        	 if(usingIdsClash){//it is possible that without this term, the polynomial no clash
	        		 context.currentDirectionList=newPolynomial.constructDirectionList(listVarProb,context,idsRemove);
	        	 }
	         }
        }
	}
	return newPolynomial;
}




private ArrayList computeNewTerm(String[] tokensLabel, HashMap probBound, double coef, ArrayList idsRemove, Double error) {
	ArrayList parTermError=new ArrayList();
	double prodUpper=1,prodLower=1,prodNoClashUpper=1;
	//if some of the probabilities of the term  is in the idsRemove, find d and the error
	String newLabelsProd="";
	boolean existTermsNoClash=false;
	for (int i=0;i<tokensLabel.length;i++){
		if (idsRemove.contains(tokensLabel[i])){
				prodUpper=prodUpper*((Double)((ArrayList)probBound.get(tokensLabel[i])).get(1));
				prodLower=prodLower*((Double)((ArrayList)probBound.get(tokensLabel[i])).get(0));
		}
		else{
			newLabelsProd=newLabelsProd+tokensLabel[i]+",";
			prodNoClashUpper=prodNoClashUpper+((Double)((ArrayList)probBound.get(tokensLabel[i])).get(1));
			existTermsNoClash=true;
		}
	}
	if(existTermsNoClash){
		newLabelsProd=newLabelsProd.substring(0,newLabelsProd.length()-1);//falta ver si es -1 o no para sacar la coma
	}
			Double dValue=coef*(prodUpper+prodLower)/2d;
			error=Math.abs((prodUpper-prodLower)*coef/2*prodNoClashUpper);
			parTermError.add(dValue);
			parTermError.add(newLabelsProd);
			parTermError.add(error);
			
			return parTermError;
}
private ArrayList computeValueClash(String[] tokensLabel, HashMap probBound, double coef, ArrayList idsClash, Double error) {
	ArrayList parValueError=new ArrayList();
	double prodUpper=1,prodLower=1;
//	si alguna de las probabilidades esta en idsClash encuentra el d y el error
	for (int i=0;i<tokensLabel.length;i++){
		if (idsClash.contains(tokensLabel[i])){
			for(int j=0;j<tokensLabel.length;j++){
				prodUpper=prodUpper*((Double)((ArrayList)probBound.get(tokensLabel[j])).get(1));
				prodLower=prodLower*((Double)((ArrayList)probBound.get(tokensLabel[j])).get(0));
			}
			Double dValue=coef*(prodUpper+prodLower)/2d;
			error=Math.abs((prodUpper-prodLower)*coef/2);
			parValueError.add(dValue);
			parValueError.add(error);
			
			return parValueError;
			
		}
	}
	return null;

}

private ArrayList computeValueNoClash(String[] tokensLabel, HashMap probBound, double coef, ArrayList idsClash, Double error) {
	ArrayList parValueError=new ArrayList();
	double prodUpper=1,prodLower=1;


		for(int j=0;j<tokensLabel.length;j++){
			prodUpper=prodUpper*((Double)((ArrayList)probBound.get(tokensLabel[j])).get(1));
			prodLower=prodLower*((Double)((ArrayList)probBound.get(tokensLabel[j])).get(0));
		}
		Double dValue=coef*(prodUpper+prodLower)/2d;
		error=Math.abs((prodUpper-prodLower)*coef/2);
		parValueError.add(dValue);
		parValueError.add(error);

		return parValueError;

}
public Polynomial aproxPol_Complex(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	//TODO: working in it
	 Polynomial newPolynomial=new Polynomial(this,context);
	 HashMap positiveSum=new HashMap();
	 HashMap negativeSum=new HashMap();
    newPolynomial.currentError=0d;
    double parcialError;
    newPolynomial.sumPosNegClash(idsClash,positiveSum,negativeSum,context);
    while(idsClash.size()>0 && newPolynomial.currentError<=mergeError){
         String idMin=newPolynomial.calculateMinPosNeg(idsClash,positiveSum,negativeSum);
    	
         double posMin=(Double)positiveSum.get(idMin);
         double negMin=Math.abs((Double)negativeSum.get(idMin));
         
         /*if(posMin>mergeError-newPolynomial.currentError && negMin>mergeError-newPolynomial.currentError){
        	 return newPolynomial;
         }*/
         //double newMergeError=mergeError-newPolynomial.currentError;
         
         if(posMin<negMin){
       	  
       	  parcialError=newPolynomial.removeTerms(idMin,mergeError,context,true);
         }
         else{
       	  parcialError=newPolynomial.removeTerms(idMin,mergeError,context,false);
         }
         newPolynomial.currentError=newPolynomial.currentError+parcialError;
        /* if(newPolynomial.currentError>1.25){
 			System.out.println("impossible");
 		}*/
         idsClash.remove(idMin);
         positiveSum.clear();
         negativeSum.clear();
         newPolynomial.sumPosNegClash(idsClash,positiveSum,negativeSum,context);
         
         newPolynomial.removeIdClashWithZeros(idsClash,positiveSum,negativeSum);
    }
    return newPolynomial;
    
}


private void sumPosNegClash(ArrayList idsClash, HashMap positiveSum, HashMap negativeSum, Context context) {
	// TODO Auto-generated method stub
	for(int i=0;i< idsClash.size();i++){
   	 String idClash=(String)idsClash.get(i);
   	 positiveSum.put(idClash,0d);
   	 negativeSum.put(idClash,0d);
   	 Iterator it2=this.terms.keySet().iterator();
   	 while (it2.hasNext()){
   			  Integer idProd=(Integer)it2.next();
   			  double coefThis=(Double)this.terms.get(idProd);
   			  //find the term
   			  String labelsProd=(String)context.getLabelProd(idProd);
   			  int pos=labelsProd.indexOf(idClash);
   			  if (pos >=0){
   				  if (coefThis>0){
   					  positiveSum.put(idClash, ((Double)positiveSum.get(idClash))+coefThis);
   				  }
   				  else{
   					  negativeSum.put(idClash, ((Double)negativeSum.get(idClash))+coefThis);
   				  }
   					  
   			  }
   	 }
    }
   
}
private String calculateMinPosNeg(ArrayList idsClash,HashMap positiveSum, HashMap negativeSum) {
	double min=Double.POSITIVE_INFINITY,newMin;
	String minId=null;
	for(int i=0;i< idsClash.size();i++){
	   	 String idClash=(String)idsClash.get(i);
	   	 newMin=Math.min((Double)positiveSum.get(idClash),Math.abs((Double)negativeSum.get(idClash)));
	   	 if(newMin<min){
	   		 min=newMin;
	   		 minId=idClash;
	   	 }
	}
	return minId;
}



public Polynomial aproxPol_Old(TreeSet listVarProb,Context context, ArrayList idsClash, double mergeError) {
	
	 Polynomial newPolynomial=new Polynomial(this,context);
	 HashMap positiveCount=new HashMap();
	 HashMap negativeCount=new HashMap();
     newPolynomial.currentError=0;
     double parcialError;
     newPolynomial.countPosNegClash(idsClash,positiveCount,negativeCount,context);
     while(idsClash.size()>0 && newPolynomial.currentError<mergeError){
          String idMax=newPolynomial.calculateMaxDiv(idsClash,positiveCount,negativeCount);
          int pos=(Integer)positiveCount.get(idMax);
          int neg=(Integer)negativeCount.get(idMax);
          if(pos<neg){
        	  
        	  parcialError=newPolynomial.removeTerms(idMax,mergeError-newPolynomial.currentError,context,true);
          }
          else{
        	  parcialError=newPolynomial.removeTerms(idMax,mergeError-newPolynomial.currentError,context,false);
          }
          newPolynomial.currentError=newPolynomial.currentError+parcialError;
          idsClash.remove(idMax);
          positiveCount.clear();
          negativeCount.clear();
          newPolynomial.countPosNegClash(idsClash,positiveCount,negativeCount,context);
          newPolynomial.removeIdClashWithZeros(idsClash,positiveCount,negativeCount);
          
     }
     return newPolynomial;
     
}


private void removeIdClashWithZeros(ArrayList idsClash, HashMap positiveCount, HashMap negativeCount) {
	for(int i=0;i<idsClash.size();i++){
	 String idClash=(String)idsClash.get(i);	
	 if(((Double)positiveCount.get(idClash)).compareTo(0d)==0 || ((Double)negativeCount.get(idClash)).compareTo(0d)==0 ){
    	  idsClash.remove(idClash);
     }
	}
}
private double removeTerms(String idMax,  double mergeError,Context context,boolean positive) {
 	 double parcialError=0;  
 	 Hashtable termsClone = (Hashtable)this.terms.clone();
	 Iterator it2=termsClone.keySet().iterator();
   	 while (it2.hasNext()){
   			  Integer idProd=(Integer)it2.next();
   			  double coefThis=(Double)this.terms.get(idProd);
   			  //find the term
   			  String labelsProd=(String)context.getLabelProd(idProd);
   			  int pos=labelsProd.indexOf(idMax);
   			  if (pos >=0){
   				  if (coefThis>0 && this.currentError+parcialError+Math.abs(coefThis)<=mergeError && positive){
   					  this.terms.remove(idProd);
   					  parcialError=parcialError+Math.abs(coefThis);
   				  }
				  if (coefThis<0 && this.currentError+parcialError+Math.abs(coefThis)<=mergeError && !positive){
   					  this.terms.remove(idProd);
   					  parcialError=parcialError+Math.abs(coefThis);
   				  }
   				  
   				  
   				  
   			  }
   	 }
	 return parcialError;
	
}
private String calculateMaxDiv(ArrayList idsClash,HashMap positiveCount, HashMap negativeCount) {
	int max=Integer.MIN_VALUE,newMax;
	String maxId=null;
	for(int i=0;i< idsClash.size();i++){
	   	 String idClash=(String)idsClash.get(i);
	   	 newMax=Math.max((Integer)positiveCount.get(idClash)/(Integer)negativeCount.get(idClash),(Integer)negativeCount.get(idClash)/(Integer)positiveCount.get(idClash));
	   	 if(newMax>max){
	   		 max=newMax;
	   		 maxId=idClash;
	   	 }
	}
	return maxId;
}
private void countPosNegClash(ArrayList idsClash, HashMap positiveCount, HashMap negativeCount, Context context) {
	// TODO Auto-generated method stub
	for(int i=0;i< idsClash.size();i++){
   	 String idClash=(String)idsClash.get(i);
   	 positiveCount.put(idClash,0);
   	 negativeCount.put(idClash,0);
   	 Iterator it2=this.terms.keySet().iterator();
   	 while (it2.hasNext()){
   			  Integer idProd=(Integer)it2.next();
   			  double coefThis=(Double)this.terms.get(idProd);
   			  //find the term
   			  String labelsProd=(String)context.getLabelProd(idProd);
   			  int pos=labelsProd.indexOf(idClash);
   			  if (pos >=0){
   				  if (coefThis>0){
   					  positiveCount.put(idClash, ((Integer)positiveCount.get(idClash))+1);
   				  }
   				  else{
   					  negativeCount.put(idClash, ((Integer)negativeCount.get(idClash))+1);
   				  }
   					  
   			  }
   	 }
    }
   
}
/**
 * construnct Direction List and the idsClash
 * @param listVarProb
 * @param context
 * @param idsClash
 * @return
 */
public Hashtable constructDirectionList(TreeSet listVarProb,Context context, ArrayList idsClash) {
	
	idsClash.clear();
	Hashtable dirList=new Hashtable();
	 Iterator it=listVarProb.iterator();
     while(it.hasNext()){
    	 dirList.put(it.next(),"u");
     }
	Iterator it2=this.terms.keySet().iterator();
	while (it2.hasNext()){
		  Integer id=(Integer)it2.next();
		  double coefThis=(Double)this.terms.get(id);
		  //find the term
		  String labelsProd=(String)context.getLabelProd(id);
		  String[] tokensLabel = labelsProd.split(",");
		  for (int i = 0; i < tokensLabel.length; ++i) {
			  if(coefThis<0 && ((String)dirList.get(tokensLabel[i])).compareTo("u")==0){ // == 'U'
				  dirList.put(tokensLabel[i],"+");
			  }
			  else if (coefThis>0 && ((String)dirList.get(tokensLabel[i])).compareTo("u")==0){
				  dirList.put(tokensLabel[i],"-");
			  }
			  else if(coefThis<0 && ((String)dirList.get(tokensLabel[i])).compareTo("-")==0){
				  dirList.put(tokensLabel[i],"C");
				  idsClash.add(tokensLabel[i]);
				  
			  }
			  else if(coefThis>0 && ((String)dirList.get(tokensLabel[i])).compareTo("+")==0){
				  dirList.put(tokensLabel[i],"C");
				  idsClash.add(tokensLabel[i]);
				  
			  } 
				  
		  }
	}		
	
	return dirList;
	
}


 



}






