package mdp;

import java.util.ArrayList;

public class SuccProbabilities { //It is better a map State probs
	private ArrayList<State>  nextStates;
	private ArrayList<Double> probs;
	
	public SuccProbabilities(){
		nextStates=new ArrayList<State>();
		probs=new ArrayList<Double>();
	}
	public ArrayList<State> getNextStates(){
		return this.nextStates;
	}
	public ArrayList<Double> getProbs(){
		return this.probs;
	}
	public String toString(){
		return "numNextStates:"+nextStates.size()+" "+probs.toString();
	}
}
