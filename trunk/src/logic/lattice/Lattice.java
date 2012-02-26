package logic.lattice;

import util.*;

import graph.Graph;

import java.io.*;
import java.util.*;

// A simple lattice of conjunctive concepts
public class Lattice {
	
	public class Concept {
		public IntSet a_links_par = new IntSet();
		public IntSet c_links_par = new IntSet();
		public IntSet s_links_par = new IntSet();
		public IntSet a_links_cld = new IntSet();
		public IntSet c_links_cld = new IntSet();
		public IntSet s_links_cld = new IntSet();
		public String _sName = null;
		public int    _nID   = -1;
		
		public Concept(String name, int id) {
			_sName = name;
			_nID   = id;
		}
		
		public String toString() {
			return _sName;
		}
		
		public boolean isPrimitive() {
			return c_links_par.count == 0;
		}
		
		public int hashCode() {
			return _sName.hashCode();
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Lattice.Concept))
				return false;
			return ((Lattice.Concept)o)._nID == _nID;
		}
		
		public String getLongDescription() {
			StringBuilder sb = new StringBuilder();
			sb.append("#" + _nID + ":name='" + this + "'");
			for (int i = 0; i < a_links_par.count; i++) {
				sb.append(" a:" + getConcept(a_links_par.l[i]));
			}
			for (int i = 0; i < c_links_par.count; i++) {
				sb.append(" c:" + getConcept(c_links_par.l[i]));
			}
			for (int i = 0; i < s_links_par.count; i++) {
				sb.append(" s:" + getConcept(s_links_par.l[i]));
			}
			return sb.toString();
		}	
	}

	public int _IDCount;
	public HashMap<String,Integer>  _str2int;
	public Concept[] _int2concept;
	
	public Lattice() {
		// Use direct array lookup to avoid expense of hash lookup
		_int2concept = new Concept[4];
		_str2int     = new HashMap<String,Integer>();
		_IDCount = 0;
	}
	
	public int addConcept(String name) {
		Integer id = null;
		if ((id = _str2int.get(name)) != null) {
			System.out.println("WARNING: Concept already exists: '" + name + "'");
			return id;
		} else {
			_str2int.put(name, _IDCount);
			
			// Double the array size?
			if (_int2concept.length == _IDCount) {
				Concept[] temp = new Concept[_IDCount << 1];
				System.arraycopy(_int2concept, 0, temp, 0, _IDCount);
				_int2concept = temp;
			}
			_int2concept[_IDCount] = new Concept(name, _IDCount);
			return _IDCount++;
		}
	}
	
	public Concept getConcept(int id) {
		return _int2concept[id];
	}
	
	public int getConceptID(String name) {
		Integer id = _str2int.get(name);
		return id == null ? -1 : id;
	}

	public HashSet<Concept> getConcepts(IntSet concs) {
		
		HashSet<Concept> concepts = new HashSet<Concept>();
		for (int i = 0; i < concs.count; i++) {
			concepts.add(_int2concept[concs.l[i]]);
		}
		return concepts;
	}
	
	public void addAxiom(int concept_sub, int[] concept_super) {
		for (int csuper : concept_super)
			addAxiom(concept_sub, csuper);
	}

	public void addAxiom(int concept_sub, int concept_super) {
		Concept csup = _int2concept[concept_super];
		if (csup == null) {
			System.out.println("ERROR: No concept for id: " + concept_super);
			new Exception().printStackTrace(System.out);
			System.exit(1);
		}		
		Concept ccld = _int2concept[concept_sub];
		if (ccld == null) {
			System.out.println("ERROR: No concept for id: " + concept_sub);
			new Exception().printStackTrace(System.out);
			System.exit(1);
		}
		if (ccld.c_links_par.count != 0 || csup.c_links_par.count != 0) {
			System.out.println("WARNING: Adding axiom to conjunctive concepts: '" 
					+ concept_sub + "' => '" + concept_super + "'");
		}
		ccld.a_links_par.add(concept_super);
		csup.a_links_cld.add(concept_sub);
	}
	
	// Add a conjunctive concept and insert it into lattice (while maintaining minimality)
	public void addConj(int concept_id, int[] conj_parents) {
		
		// Add all c-links
		for (int conj_parent : conj_parents) {
			addConjunctsInternal(concept_id, conj_parent);
		}
		
		// Get most-specific parents and most-general children
		IntSet mss = getMSS(concept_id);
		IntSet mgs = getMGS(concept_id);
		
		// Maintain a minimal lattice: remove links between mgs and mss
		for (int i = 0; i < mss.count; i++) {
			int id_par = mss.l[i];
			Concept c_par = _int2concept[id_par];
			for (int j = 0; j < mgs.count; j++) {
				int id_cld = mgs.l[j];
				Concept c_cld = _int2concept[id_cld];
				
				c_par.s_links_cld.remove(id_cld);
				c_cld.s_links_par.remove(id_par);
			}
		}
		
		// Connect mss to concept if c_par not already a parent
		_mss_visited.clear();
		collectAllParents(_mss_visited, concept_id);
		Concept conc = _int2concept[concept_id];
		for (int i = 0; i < mss.count; i++) {
			int id_par = mss.l[i];
			if (_mss_visited.contains(id_par))
				continue;
			Concept c_par = _int2concept[id_par];

			if (!conc.a_links_par.contains(id_par) 
				&& !conc.c_links_par.contains(id_par) 
				&& !conc.s_links_par.contains(id_par)) {
				c_par.s_links_cld.add(concept_id);
				conc.s_links_par.add(id_par);
			}
		}
		
		// Connect mgs to concept if id_cld not already a child
		_mgs_visited.clear();
		collectAllChildren(_mgs_visited, concept_id);
		for (int j = 0; j < mgs.count; j++) {
			int id_cld = mgs.l[j];
			if (_mgs_visited.contains(id_cld))
				continue;
			Concept c_cld = _int2concept[id_cld];
			
			if (!conc.a_links_cld.contains(id_cld) 
				&& !conc.c_links_par.contains(id_cld) 
				&& !conc.s_links_cld.contains(id_cld)) {
				c_cld.s_links_par.add(id_cld);
				conc.s_links_cld.add(id_cld);
			}
		}
	}

	// Add a conjunctive concept and insert it into lattice (while maintaining minimality)
	public IntSet getMSSNoInsert(int[] conj_parents) {
		
		// Add all c-links
		String name = "_temp_";
		int concept_id = addConcept(name);
		for (int conj_parent : conj_parents) {
			addConjunctsInternal(concept_id, conj_parent);
		}
		
		// Get most-specific parents
		IntSet mss = getMSS(concept_id);
		
		// Remove temporary concept from taxonomy
		for (int conj_parent : conj_parents) {
			removeConjunctsInternal(concept_id, conj_parent);
		}
		_str2int.remove(name);
		_int2concept[--_IDCount] = null;

		return (IntSet)mss.clone();
	}

	
	private void addConjunctsInternal(int concept, int conj_parent) {
		Concept csup = _int2concept[conj_parent];
		if (csup == null) {
			System.out.println("ERROR: No concept for id: " + conj_parent);
			System.out.println("       Did you allocate enough primitives?");
			new Exception().printStackTrace(System.out);
			System.exit(1);
		}
		Concept ccld = _int2concept[concept];
		if (ccld == null) {
			System.out.println("ERROR: No concept for id: " + concept);
			System.out.println("       Did you allocate enough primitives?");
			new Exception().printStackTrace(System.out);
			System.exit(1);
		}
		if (ccld.a_links_par.count != 0) {
			System.out.println("WARNING: Adding conjunctive definition to axiomatized concept: '" 
					+ concept + "' => '" + conj_parent + "'");
		}
		ccld.c_links_par.add(conj_parent);
		csup.c_links_cld.add(concept);
	}
	
	private void removeConjunctsInternal(int concept, int conj_parent) {
		Concept csup = _int2concept[conj_parent];
		Concept ccld = _int2concept[concept];
		ccld.c_links_par.remove(conj_parent);
		csup.c_links_cld.remove(concept);
	}
	
	// NOTE: Can be much more efficient with in-node marking, not done for now
	//       See unpublished work by Sanner (2002, 2003) for details
	// Reuse some data structures to avoid reallocation
	// OK, since code is non re-entrant
	private IntSet _mss_visited = new IntSet();
	private IntSet _mss = new IntSet();
	//private IntSet _filtered_mss = new IntSet();
	private LinkedList<Integer> frontier_mss = new LinkedList<Integer>();
	public IntSet getMSS(int concept_id) {
		
		_mss.clear();
		_mss_visited.clear();
		collectAllParents(_mss_visited, concept_id);
		_mss_visited.remove(concept_id);
		frontier_mss.clear();
		for (int i = 0; i < _mss_visited.count; i++)
			frontier_mss.add(_mss_visited.l[i]);
		
		// For each element on frontier
		_mss_visited.clear();
		while (!frontier_mss.isEmpty()) {
			int id = frontier_mss.removeFirst();
			if (_mss_visited.contains(id) || id == concept_id)
				continue;
			_mss_visited.add(id);
			Concept cur = _int2concept[id];
			
			// Find all non-primitive children of id and add to frontier if a subsumer
			boolean child_added = false;
			for (int i = 0; i < cur.c_links_cld.count; i++) {
				int cld_id = cur.c_links_cld.l[i];
				if (satisfiesConjDef(_mss_visited, cld_id)) {
					frontier_mss.addLast(cld_id);
					child_added = true;
				}
			}

			// If no child added, it is an MSS if no visited S, or A children
			if (!child_added) {
				boolean visited_child_a_or_s_link = false;
				for (int i = 0; i < cur.a_links_cld.count && !visited_child_a_or_s_link; i++)
					visited_child_a_or_s_link = _mss_visited.contains(cur.a_links_cld.l[i]);
				for (int i = 0; i < cur.s_links_cld.count && !visited_child_a_or_s_link; i++)
					visited_child_a_or_s_link = _mss_visited.contains(cur.s_links_cld.l[i]);
				if (!visited_child_a_or_s_link) {
					_mss.add(id);
					
					// Remove the parents of _mss just in case
					// Change to *all* to be optimal (but expensive)
					_temp_intset.clear();
					collectImmParents(_temp_intset, id);
					for (int j = 0; j < _temp_intset.count; j++)
						_mss.remove(_temp_intset.l[j]); 
				}
			}
		}
		
		// Filter the MSS's (?)
		//_filtered_mss.clear();
		//for (int i = 0; i < _mss.count; i++) {
		//	int potential_mss = _mss.l[i];
		//	_filtered_mss.add();
		//}
		
		return _mss;
	}

	// NOTE: Can be much more efficient with in-node marking, not done for now
	//       See unpublished work by Sanner (2002, 2003) for details
	private IntSet _mgs_visited = new IntSet();
	private IntSet _mgs = new IntSet();
	private IntSet _mgs_temp = new IntSet();
	//private IntSet _filtered_mgs = new IntSet();
	private LinkedList<Integer> frontier_mgs = new LinkedList<Integer>();
	public IntSet getMGS(int concept_id) {
		
		// Get all c-links
		Concept cur = _int2concept[concept_id];
		
		// Get children of one parent
		_mgs.clear();
		IntSet clinks = cur.c_links_cld;
		frontier_mgs.clear();
		frontier_mgs.add(clinks.l[0]);
		
		// For each element on frontier
		_mgs_visited.clear();
		while (!frontier_mgs.isEmpty()) {
			int cld_id = frontier_mgs.removeFirst();
			if (_mgs_visited.contains(cld_id) || cld_id == concept_id)
				continue;
			_mgs_visited.add(cld_id);
		
			// For each child, look up and see if all c-links are contained,
			// if so, add to MGS set
			// else, add children to frontier
			_mgs_temp.clear();
			collectAllParents(_mgs_temp, cld_id);
			boolean satisfies_mgs = true;
			for (int i = 0; i < clinks.count && satisfies_mgs; i++)
				satisfies_mgs = _mgs_temp.contains(clinks.l[i]);
			
			if (satisfies_mgs) {
				_mgs.add(cld_id);
								
				// Remove the parents of _mss just in case
				// Change to *all* to be optimal (but expensive)
				_temp_intset.clear();
				collectImmChildren(_temp_intset, cld_id);
				for (int j = 0; j < _temp_intset.count; j++)
					_mgs.remove(_temp_intset.l[j]); 

			} else {
				collectImmChildren(_mgs_temp, cld_id);
				for (int i = 0; i < _mgs_temp.count; i++)
					frontier_mgs.addLast(_mgs_temp.l[i]);
			}
		}
		
		return _mgs;
	}
	
	private IntSet _temp_intset = new IntSet();
	
	public boolean hasParent(int conc_id, int par_id) {
		_temp_intset.clear();
		collectAllParents(_temp_intset, conc_id);
		return _temp_intset.contains(conc_id);
	}

	public boolean hasChild(int conc_id, int par_id) {
		_temp_intset.clear();
		collectAllChildren(_temp_intset, conc_id);
		return _temp_intset.contains(conc_id);
	}

	public boolean satisfiesConjDef(IntSet parents, int conc_id) {
		
		Concept c = _int2concept[conc_id];
		for (int i = 0; i < c.c_links_par.count; i++) {
			if (!parents.contains(c.c_links_par.l[i])) 
				return false;
		}
		return true;
	}
	
	// Assuming self is a parent
	public IntSet getAllParents(int par_id) {
		IntSet par = new IntSet();
		collectAllParents(par, par_id);
		return par;
	}
	
	public void collectAllParents(IntSet par, int par_id) {
		
		if (par.contains(par_id)) return;
		par.add(par_id);
		
		Concept c = _int2concept[par_id];
		for (int i = 0; i < c.a_links_par.count; i++) {
			int id = c.a_links_par.l[i];
			collectAllParents(par, id);
		}
		for (int i = 0; i < c.c_links_par.count; i++) {
			int id = c.c_links_par.l[i];
			collectAllParents(par, id);
		}
		for (int i = 0; i < c.s_links_par.count; i++) {
			int id = c.s_links_par.l[i];
			collectAllParents(par, id);
		}
	}
		
	// Assuming self is a child
	public IntSet getAllChildren(int cld_id) {
		IntSet cld = new IntSet();
		collectAllChildren(cld, cld_id);
		return cld;
	}

	public void collectAllChildren(IntSet cld, int cld_id) {
		if (cld.contains(cld_id)) return;
		cld.add(cld_id);
		
		Concept c = _int2concept[cld_id];
		for (int i = 0; i < c.a_links_par.count; i++) {
			int id = c.a_links_par.l[i];
			collectAllChildren(cld, id);
		}
		for (int i = 0; i < c.c_links_par.count; i++) {
			int id = c.c_links_par.l[i];
			collectAllChildren(cld, id);
		}
		for (int i = 0; i < c.s_links_par.count; i++) {
			int id = c.s_links_par.l[i];
			collectAllChildren(cld, id);
		}
	}
	
	public IntSet getImmParents(int par_id) {
		IntSet par = new IntSet();
		collectImmParents(par, par_id);
		return par;
	}

	public IntSet collectImmParents(IntSet parents, int conc_id) {
		
		//IntSet parents = new IntSet();
		Concept c = _int2concept[conc_id];
		for (int i = 0; i < c.a_links_par.count; i++) {
			parents.add(c.a_links_par.l[i]);
		}
		for (int i = 0; i < c.c_links_par.count; i++) {
			parents.add(c.c_links_par.l[i]);
		}
		for (int i = 0; i < c.s_links_par.count; i++) {
			parents.add(c.s_links_par.l[i]);
		}
		return parents;
	}

	public IntSet getImmChildren(int cld_id) {
		IntSet cld = new IntSet();
		collectImmChildren(cld, cld_id);
		return cld;
	}

	public IntSet collectImmChildren(IntSet children, int conc_id) {
		
		//IntSet children = new IntSet();
		Concept c = _int2concept[conc_id];
		for (int i = 0; i < c.a_links_par.count; i++) {
			children.add(c.a_links_par.l[i]);
		}
		for (int i = 0; i < c.c_links_par.count; i++) {
			children.add(c.c_links_par.l[i]);
		}
		for (int i = 0; i < c.s_links_par.count; i++) {
			children.add(c.s_links_par.l[i]);
		}
		return children;
	}
	
	public void printContent(PrintStream ps) {
		for (int id = 0; id < _IDCount; id++) {
			Concept c = getConcept(id);
			ps.println(c.getLongDescription());
		}
	}
	
	public Graph getGraph(boolean suppress_primitive) {
		Graph g = new Graph();
		g.setBottomToTop(true);
		g.setMultiEdges(true); // Note: still does not allow cyclic edges
		
		for (int id = 0; id < _IDCount; id++) {
			Concept c = getConcept(id);
			if (suppress_primitive && c.isPrimitive())
				continue;
			g.addNodeLabel(c._sName, "#" + id + " : " + c._sName);
			g.addNodeColor(c._sName, c.isPrimitive() ? "salmon" : "lightblue");
			g.addNodeShape(c._sName, "square");
			g.addNodeStyle(c._sName, "filled");
		}
		
		for (int id = 0; id < _IDCount; id++) {
			Concept c = getConcept(id);
			if (suppress_primitive && c.isPrimitive())
				continue;
			
			for (int i = 0; i < c.a_links_par.count; i++) {
				Concept c1 = getConcept(c.a_links_par.l[i]);
				if (suppress_primitive && c1.isPrimitive())
					continue;
				g.addUniLink(c._sName, c1._sName, "black", "solid", "a");
			}
			for (int i = 0; i < c.c_links_par.count; i++) {
				Concept c1 = getConcept(c.c_links_par.l[i]);
				if (suppress_primitive && c1.isPrimitive())
					continue;
				g.addUniLink(c._sName, c1._sName, "black", "solid", "c");
			}
			for (int i = 0; i < c.s_links_par.count; i++) {
				Concept c1 = getConcept(c.s_links_par.l[i]);
				if (suppress_primitive && c1.isPrimitive())
					continue;
				g.addUniLink(c._sName, c1._sName, "black", "dashed", "s");
			}			
		}

		return g;
	}
	
	/*****************************************************************************
	 * TODO: Karina... only need to understand from here below to build / use lattice 
	 *****************************************************************************/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Lattice l = new Lattice();
	
		// Add primitive concepts to lattice (support keys up to length 3)
		// Make sure you add enough primitives for your key length!
		for (int i = 1; i <= 3; i++)
			AddPrimitive(l, i);
		
		// Add keys to lattice (do this when permanently inserting dirlist key into cache)
		AddKey(l, "uuu");
		AddKey(l, "uu+");
		AddKey(l, "u-u");
		AddKey(l, "u+u");
		AddKey(l, "+uu");
		AddKey(l, "u++");
		AddKey(l, "+u+");
		AddKey(l, "-u+");
		AddKey(l, "+u-");
		AddKey(l, "++u");
		AddKey(l, "+++");
		AddKey(l, "---");

		// Text display of lattice
		//l.printContent(System.out);
		
		// Check if a key is in the lattice (i.e., check if a dirlist already in cache)
		System.out.println("Key '+++' is in the lattice: " + (l.getConceptID("+++") >= 0));
		System.out.println("Key '--u' is in the lattice: " + (l.getConceptID("--u") >= 0));
		
		// Get immediate non-primitive parent generalizations from lattice
		// i.e., get closest keys
		System.out.println("Non-primitive immediate parents of '--u':");
		for (Concept c : GetKeyParents(l, "--u"))
			System.out.println(" - " + c);
		System.out.println("Non-primitive immediate parents of 'u+-':");
		for (Concept c : GetKeyParents(l, "u+-"))
			System.out.println(" - " + c);

		// Display lattice with primitive concepts
		Graph g1 = l.getGraph(false /* suppress primitive concepts */); 
		g1.launchViewer();
		
		// Display lattice without primitive concepts
		Graph g2 = l.getGraph(true /* suppress primitive concepts */); 
		g2.launchViewer();
		
	}

	public static void AddPrimitive(Lattice l, int i) {
		int u1 = l.addConcept("u" + i);
		int p1 = l.addConcept("+" + i);
		l.addAxiom(p1, u1);
		int m1 = l.addConcept("-" + i);
		l.addAxiom(m1, u1);
	}
	
	// Permanently add a key to lattice (do this when inserting dirlist key into cache)
	public static void AddKey(Lattice l, String key) {
		//System.out.println("Adding key: " + key);
		char[] key_chars = key.toCharArray();
		int id = l.addConcept(key);
		
		IntArray parents = new IntArray();
		for (int i = 1; i <= key_chars.length; i++) {
			parents.add(l.getConceptID(key_chars[i-1] + "" + i));
		}
		l.addConj(id, parents.toArray());
	}
	
	// Get a key's parents without adding it to lattice (do this when checking
	// to see if a parent dirlist key can be reused)
	public static HashSet<Concept> GetKeyParents(Lattice l, String key) {
		
		int cur_id = -1;
		if ((cur_id = l.getConceptID(key)) >= 0) {
			System.out.println("WARNING: Key is already in cache, retrieving known parents!");
			System.out.println("         Should not need to lookup parents if key already in cache.");
			HashSet<Concept> unfiltered_parents = l.getConcepts(l.getImmParents(cur_id));
			HashSet<Concept> to_return = new HashSet<Concept>();
			for (Concept c : unfiltered_parents)
				if (!c.isPrimitive())
					to_return.add(c);
		}
		
		// Key not in cache, get its MSS set without actually inserting it
		char[] key_chars = key.toCharArray();
		
		IntArray parents = new IntArray();
		for (int i = 1; i <= key_chars.length; i++) {
			String parent_name = key_chars[i-1] + "" + i;
			int parent_id = l.getConceptID(parent_name);
			if (parent_id < 0) {
				System.out.println("ERROR: could not find '" + parent_name + "'");
				new Exception().printStackTrace();
				System.exit(1);
			}
			parents.add(parent_id);
		}
		IntSet mss = l.getMSSNoInsert(parents.toArray());
		HashSet<Concept> unfiltered_mss = l.getConcepts(mss);
		HashSet<Concept> to_return = new HashSet<Concept>();
		for (Concept c : unfiltered_mss)
			if (!c.isPrimitive())
				to_return.add(c);
		
		return to_return;
	}
}
