package mdp;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import add.ADDLeafOperation;
import add.Polynomial;

class StateEnumerator implements ADDLeafOperation {
	public StateEnumerator(List<Integer> variables) {
		this.variables = variables;
	}
	
	private List<State> states = new ArrayList<State>();
	
	private List<Integer> variables = null;
	
	public List<State> getStates() {
		return states;
	}

	@Override
	public void processADDLeaf(TreeMap<Integer, Boolean> assign, Object value) {
		if (value instanceof Double) {
			double valueAsDouble = (Double) value;
			if (valueAsDouble <= 0.0) return;
		}
		else if (value instanceof Polynomial) {
			Polynomial valueAsPolynomial = (Polynomial) value;

			if (valueAsPolynomial.getTerms().size() == 0 && valueAsPolynomial.getC() <= 0.0) return;
		}
		
		this.generateStatesForIncompletePath(assign);
	}

	private void generateStatesForIncompletePath(TreeMap<Integer, Boolean> assign) {
		List<Integer> missingVariables = new ArrayList<Integer>();
		
		for (Integer var : this.variables) {			
			if (!assign.containsKey(var))
				missingVariables.add(var);
		}
		
		if (missingVariables.size() > 0)
			this.generateStatesForIncompletePath(assign, missingVariables, 0);
		else {
			State s = new State(new TreeMap<Integer, Boolean>(assign));
			states.add(s);
		}
	}

	private void generateStatesForIncompletePath(TreeMap<Integer, Boolean> assign, List<Integer> missingVariables, int i) {
		if (i >= missingVariables.size()) {
			State s = new State(new TreeMap<Integer, Boolean>(assign));
			states.add(s);
		}
		else {
			Integer variable = missingVariables.get(i);
			
			assign.put(variable, true);
			generateStatesForIncompletePath(assign, missingVariables, i+1);
			
			assign.put(variable, false);
			generateStatesForIncompletePath(assign, missingVariables, i+1);
		}
	}
}
