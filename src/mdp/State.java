package mdp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import com.sun.org.apache.bcel.internal.generic.IDIV;

import add.BinaryOperKeyADD;
import add.BinaryOperation;

public class State implements Comparable<State> {
	private BigInteger identifier;
	private TreeMap<Integer, Boolean> values;
	private SuccProbabilitiesM actionSuccProbab[];

	/*
	 * public State(int numActions){ this.values=new ArrayList();
	 * actionSuccProbab=new SuccProbabilities[numActions]; //parece que hay que
	 * inicializarlo con null }
	 */

	public State(TreeMap<Integer, Boolean> values, BigInteger identifier) {
		this.identifier = identifier;
		this.values = values;
	}

	public State(TreeMap<Integer, Boolean> values, int numActions, BigInteger identifier) {
		this.identifier = identifier;
		this.values = values;
		actionSuccProbab = new SuccProbabilitiesM[numActions];
	}

	public State(TreeMap<Integer, Boolean> values, int numActions)
	{
		this(values);
		actionSuccProbab = new SuccProbabilitiesM[numActions];
	}
	
	public State(TreeMap<Integer, Boolean> values) { // Para LRTDP-IP
		BigInteger identifier = new BigInteger("0"); // take into account the position (prime) variable
		
		int i = 0;
		
		for (Integer key : values.keySet()) {
			Boolean value = values.get(key);

			if (value)
				identifier = identifier.setBit(i);
			
			i++;
		}
		
		this.identifier = identifier;
		this.values = values;
	}

	public void initActionSucc(int numActions) {
		actionSuccProbab = new SuccProbabilitiesM[numActions];
		for (int i = 0; i < numActions; i++) {
			actionSuccProbab[i] = null;
		}
	}

	public TreeMap<Integer, Boolean> getValues() {
		return values;
	}

	public SuccProbabilitiesM[] getActionSuccProbab() {
		return actionSuccProbab;
	}

	public BigInteger getIdentifier() {
		return identifier;
	}

	public String toString() {
		String part = "state: ";

		if (this.values != null) {
			ArrayList<String> vars = new ArrayList<String>();
			
			for (Integer key : values.keySet()) {
				Boolean value = values.get(key);
				
				if (value)
					vars.add(Integer.toString(key - this.values.keySet().size()));
			}
			
			part += vars;
		}
		else {
			part += identifier;
		}
		
		return part;
	}

	public int hashCode() {
		return identifier.hashCode(); // TODO: define hashCode()
	}

	public boolean equals(Object other) {
		// Not strictly necessary, but often a good optimization
		if (this == other)
			return true;

		if (!(other instanceof State))
			return false;

		State otherA = (State) other;
		return this.identifier.equals(otherA.identifier);
	}

	@Override
	public int compareTo(State o) {
		return this.getIdentifier().compareTo(o.getIdentifier());
	}
}
