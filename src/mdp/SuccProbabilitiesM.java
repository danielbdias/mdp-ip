package mdp;

import java.util.HashMap;
import add.Polynomial;

public class SuccProbabilitiesM { 
	private HashMap<State, Double>  nextStatesProbs;
	
	private HashMap<State, Polynomial>  nextStatesPoly;
		
	public SuccProbabilitiesM(){
		nextStatesProbs = new HashMap<State, Double>();
		nextStatesPoly = new HashMap<State, Polynomial>();
	}
	
	public  HashMap<State, Double> getNextStatesProbs(){
		return this.nextStatesProbs;
	}
	
	public  HashMap<State, Polynomial> getNextStatesPoly(){
		return this.nextStatesPoly;
	}
	
	public String toString(){
		return "numNextStates:"+nextStatesProbs.size()+" "+nextStatesProbs.toString();
	}
}
