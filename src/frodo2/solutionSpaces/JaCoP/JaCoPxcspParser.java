/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2020  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

FRODO is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FRODO is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


How to contact the authors: 
<https://frodo-ai.tech>
 */

/** Package handling the interface with the JaCoP solver */
package frodo2.solutionSpaces.JaCoP;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jacop.constraints.AlldifferentCloneable;
import org.jacop.constraints.CumulativeCloneable;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.DecomposedConstraint;
import org.jacop.constraints.Diff2Cloneable;
import org.jacop.constraints.ElementIntegerCloneable;
import org.jacop.constraints.ElementVariableCloneable;
import org.jacop.constraints.ExtensionalConflictVACloneable;
import org.jacop.constraints.ExtensionalSupportSTRCloneable;
import org.jacop.constraints.LinearIntCloneable;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.IntervalDomain;
import org.jacop.core.StoreCloneable;
import org.jacop.core.Var;

import frodo2.algorithms.XCSPparser;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.DCOPProblemInterface;
import frodo2.solutionSpaces.ProblemInterface;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.crypto.AddableBigInteger;
import frodo2.solutionSpaces.hypercube.ScalarHypercube;

/** An XCSP parser that generates spaces based on JaCoP
 * @author Thomas Leaute, Arnaud Jutzeler
 * @param <U> the type used for utility values
 */
public class JaCoPxcspParser < U extends Addable<U> > extends XCSPparser<AddableInteger, U> {

	/** Constraint type */
	protected enum ConstraintType {
		/** Extensional soft constraint described by a soft relation */
		EXTENSIONAL, 
		/** Intensional constraint described by a predicate or a function */
		INTENSIONAL, 
		/** Unsupported */
		PROBABILITY, 
		/** A global constraint */
		GLOBAL 
	}

	/** The JaCoP store */
	private StoreCloneable store;

	/** Constructor
	 * @param probDoc 	the problem Document in XCSP format
	 * @param params 	the parser's parameters
	 */
	public JaCoPxcspParser(Document probDoc, Element params) {
		super(probDoc, params);

		assert this.countNCCCs == false : "NCCCs not implemented"; /// @todo Implement NCCCs?

	}

	/** Constructor from a JDOM root Element in XCSP format
	 * @param root 							the JDOM root Element in XCSP format
	 * @param countNCCCs 					Whether to count constraint checks
	 * @param spacesToIgnoreNcccs			list of spaces for which NCCCs should NOT be counted
	 * @param mpc 							Whether to behave in MPC mode
	 */
	protected JaCoPxcspParser (Element root, boolean countNCCCs, HashSet<String> spacesToIgnoreNcccs, boolean mpc) {
		super (root, countNCCCs, false, spacesToIgnoreNcccs, mpc);

		assert this.countNCCCs == false : "NCCCs not implemented"; /// @todo Implement NCCCs?

	}

	/** @see XCSPparser#newInstance(org.jdom2.Element) */
	protected JaCoPxcspParser<U> newInstance (Element instance) {
		return new JaCoPxcspParser<U> (instance, this.countNCCCs, this.spacesToIgnoreNcccs, super.mpc);
	}

	/** @see XCSPparser#setUtilClass(java.lang.Class) */
	@Override
	public void setUtilClass (Class<U> utilClass) {
		assert utilClass.equals(AddableInteger.class) || utilClass.equals(AddableBigInteger.class) : this.getClass().getSimpleName() + " does not support utilities of class " + utilClass;
		super.setUtilClass(utilClass);
	}

	/** @see XCSPparser#getSpaces(Set, boolean, boolean, Set, DCOPProblemInterface) */
	@Override
	protected List< JaCoPutilSpace<U> > getSpaces (Set<String> vars, final boolean withAnonymVars, final boolean getProbs, Set<String> forbiddenVars, 
			DCOPProblemInterface<AddableInteger, U> problem) {

		U infeasibleUtil = (super.maximize() ? super.getMinInfUtility() : super.getPlusInfUtility());

		/// @todo Reuse code. 

		ArrayList< JaCoPutilSpace<U> > result = new ArrayList< JaCoPutilSpace<U> >();

		final boolean debugLoad = false;

		// First important element of XCSP format is the specification of the domains.		
		org.jdom2.Element domains = root.getChild("domains");

		// domain is represented as a list of integers. Potentially a problem 
		// if a domain is large. However, the hypercubes will have problems too
		// so it is unlikely for variables to have large domains.
		HashMap<String, AddableInteger[]> domainsHashMap = new HashMap<String, AddableInteger[]>();

		// Reads information about variables domains.
		for (org.jdom2.Element domain : (List<org.jdom2.Element>) domains.getChildren()) {

			String name = domain.getAttributeValue("name");

			// Hashmap to associate domain names with the list of elements in that domain.
			domainsHashMap.put(name, (AddableInteger[]) this.getDomain(domain, debugLoad));
		}

		if (debugLoad)
			System.out.println(domainsHashMap);

		// Second important element in XCSP format is describing variables.
		org.jdom2.Element variables = root.getChild("variables");

		// Each variable has its list of values in their domain. 
		HashMap<String, AddableInteger[]> variablesHashMap = new HashMap<String, AddableInteger[]>();

		for (org.jdom2.Element variable : (List<org.jdom2.Element>) variables.getChildren()) {

			String name = variable.getAttributeValue("name");
			String domName = variable.getAttributeValue("domain");

			if (!getProbs && domName == null) // we don't know the domain of this variable
				return null;

			// Variables domain is specified by the name so the actual domain is obtained
			// from the hashmap describing the domains.
			variablesHashMap.put(name, domainsHashMap.get(domName));
		}

		if (debugLoad)
			System.out.println(variablesHashMap);

		// All the relations
		org.jdom2.Element relations;
		org.jdom2.Element predicates;
		org.jdom2.Element functions;
		relations = root.getChild("relations");
		predicates = root.getChild("predicates");
		functions = root.getChild("functions");
		
		ArrayList<Element> predAndFunc = new ArrayList<Element> ();
		if (predicates != null) 
			predAndFunc.addAll(predicates.getChildren());
		if (functions != null) 
			predAndFunc.addAll(functions.getChildren());

		// This element actually describes all the constraints.
		org.jdom2.Element constraints = root.getChild("constraints");

		for (Element constraint : (List<Element>) constraints.getChildren()){

			String refName = constraint.getAttributeValue("reference");
			Element relation = null;

			// If it is either a relation, a predicate or a function, we need to get the corresponding Element whereas there no such Element with global constraints
			if(!refName.startsWith("global:")){

				Element parameters = constraint.getChild("parameters");

				// Constraint in intension
				if(parameters != null){

					for(Element pred: predAndFunc){
						if(pred.getAttributeValue("name").equals(refName)){
							relation = pred;
							break;
						}
					}

					assert relation != null: "The predicate or function " + refName + " referenced by the constraint cannot be found!";

					// Constraint in extension
				}else{

					for(Element rel: (List<Element>) relations.getChildren()){
						if(rel.getAttributeValue("name").equals(refName)){
							relation = rel;
							break;
						}
					}

					assert relation != null: "The relation " + refName + " referenced by the constraint cannot be found!";

				}

			}

			this.parseConstraint(result, constraint, variablesHashMap, relation, vars, getProbs, withAnonymVars, infeasibleUtil, forbiddenVars);

		}

		return result;

	}


	/** Parses a constraint
	 * @param spaces 				the list of spaces to which the constraint should be added
	 * @param constraint 			the XCSP description of the constraint
	 * @param variablesHashMap 		the domain of each variable
	 * @param relation				the relation, predicate or function referenced by the constraint
	 * @param vars 					if \c null, returns all constraints; otherwise, returns only the constraints involving at least one variable in \a vars
	 * @param getProbs 				if \c true, returns the probability spaces (ignoring \a withAnonymVars); else, returns the solution spaces
	 * @param withAnonymVars 		whether constraints involving variables with unknown owners should be taken into account
	 * @param infeasibleUtil 		the infeasible utility
	 * @param forbiddenVars 		any space involving any of these variables will be ignored
	 * @todo Reuse code. 
	 */
	public void parseConstraint(ArrayList< JaCoPutilSpace<U> > spaces, Element constraint, 
			HashMap<String, AddableInteger[]> variablesHashMap, Element relation, 
			Set<String> vars, final boolean getProbs, final boolean withAnonymVars, U infeasibleUtil, Set<String> forbiddenVars) {

		String name = constraint.getAttributeValue("name");
		String owner = constraint.getAttributeValue("agent");

		//int arity = Integer.parseInt(constraint.getAttributeValue("arity"));
		String scope = constraint.getAttributeValue("scope").trim();

		Pattern pattern = Pattern.compile("\\s+");

		String[] varNames = pattern.split(scope);

		// Skip this constraint if it does not involve any variables of interest (or if we want all constraints)
		if (vars != null && Collections.disjoint(vars, Arrays.asList(varNames)))
			return;

		// Skip this constraint if if involves any of the forbidden variables
		if (forbiddenVars != null) 
			for (String varName : varNames) 
				if (forbiddenVars.contains(varName)) 
					return;

		AddableInteger[][] variables_domain = (AddableInteger[][]) Array.newInstance(variablesHashMap.values().iterator().next().getClass(), varNames.length);
		int no = -1;
		for (String n : varNames) {

			// If required, ignore the constraint if its scope contains variables with unknown owners
			if (!getProbs && !withAnonymVars && this.getOwner(n) == null) 
				return;

			no++;
			variables_domain[no] = variablesHashMap.get(n);
			assert variables_domain[no] != null : "Unknown domain for variable `" + n + "'";
		}
		
		if (this.store == null) 
			this.createStore();

		// Parse the constraint
		ArrayList<Constraint> constraints = new ArrayList<Constraint> ();
		ArrayList< DecomposedConstraint<Constraint> > decompCons = new ArrayList< DecomposedConstraint<Constraint> > ();
		ArrayList<IntVarCloneable> utilVars = new ArrayList<IntVarCloneable> ();
		String relName;
		if (constraint.getAttributeValue("reference").startsWith("global:")) // global constraint
			JaCoPxcspParser.parseGlobalConstraint(constraint, this.store, constraints, decompCons, utilVars);
		
		else if ((relName = relation.getName()).equals("relation")) { // relation
			assert (Integer.valueOf(constraint.getAttributeValue("arity")) 
					== Integer.valueOf(relation.getAttributeValue("arity"))) : 
						"The relation referenced by the constraint is not of the same arity!";
			JaCoPxcspParser.parseRelation(constraint, relation, this.store, constraints, decompCons, utilVars);

		} else if (relName.equals("predicate") || relName.equals("function")) // predicate or function
			JaCoPxcspParser.parsePredicate(constraint, relation, this.store, constraints, decompCons, utilVars);

		else 
			assert false: "unknown XCSP contraint reference " + relName;
		
		// Look up the variables in the store
		IntVarCloneable[] storeVars = new IntVarCloneable [varNames.length];
		for (int i = varNames.length - 1; i >= 0; i--) {
			storeVars[i] = (IntVarCloneable) store.findVariable(varNames[i]);
			assert storeVars[i] != null : "Variable `" + varNames[i] + "' not found in the store";
		}

		JaCoPutilSpace<U> current = 
				new JaCoPutilSpace<U> (name, owner, constraints, decompCons, utilVars, storeVars, variables_domain, super.maximize(), infeasibleUtil);

		spaces.add(current);
	}

	/** Initializes the JaCoP store with the variables */
	private void createStore() {
		
		// Instantiate the store
		this.store = new StoreCloneable ();
		
		// Parse the domains
		HashMap<String, IntervalDomain> allIntDoms = new HashMap<String, IntervalDomain> ();
		String values;
		IntervalDomain dom;
		for (Element domainElmt : this.root.getChild("domains").getChildren("domain")) {
			
			values = domainElmt.getText().trim();
			
			// Instantiate the domain (yet to be filled)
			allIntDoms.put(domainElmt.getAttributeValue("name"), dom = new IntervalDomain ());

			// Loop through the intervals
			Pattern pattern = Pattern.compile("\\s+");
			String[] intervals = pattern.split(values);
			pattern = Pattern.compile("\\.\\.");
			String[] parts;
			int intVal;
			for (String interval : intervals) {

				// Skip whitespace separators
				if (interval.equals(""))
					continue;

				// Check whether this is an interval or a singleton
				parts = pattern.split(interval);
				switch (parts.length) {
				
				case 1: // singleton
					dom.addDom(new IntervalDomain (intVal = Integer.parseInt(parts[0].trim()), intVal));
					break;
					
				case 2: // interval
					dom.addDom(new IntervalDomain (Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
					break;
					
				default: // should not happen
					System.err.println("Syntax error while parsing the following domain: " + values);
				}
			}
		}
		
		// Parse the variables
		for (Element varElmt : this.root.getChild("variables").getChildren("variable")) {
			
			// Loop up the domain
			dom = allIntDoms.get(varElmt.getAttributeValue("domain"));
			assert dom != null : "Definition of domain `" + varElmt.getAttributeValue("domain") + "' not found";
			
			new IntVarCloneable (store, varElmt.getAttributeValue("name"), dom.clone());
		}
	}

	/** @see XCSPparser#groundVars(String[], Addable[]) */
	@Override
	public Document groundVars (String[] vars, AddableInteger[] values) {

		/// @todo TBC
		assert false : "Not yet implemented!";
	return null;
	}

	/** @see XCSPparser#switchMaxMin(int) */
	@Override
	public Document switchMaxMin(int shiftInt) {

		/// @todo TBC
		assert false : "Not yet implemented!";
	return null;
	}

	/** @see XCSPparser#reset(ProblemInterface) */
	@Override
	public void reset(ProblemInterface<AddableInteger, U> newProblem) {

		/// @todo TBC
		assert false : "Not yet implemented!";
	}

	/** 
	 * @see DCOPProblemInterface#getUtility(Map, boolean) 
	 * @todo Test this method 
	 * @todo Reuse code. 
	 */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> getUtility (Map<String, AddableInteger> assignments, final boolean withAnonymVars) {

		UtilitySolutionSpace<AddableInteger, U> output = new  ScalarHypercube<AddableInteger, U>(this.getZeroUtility(), this.getInfeasibleUtil(), new AddableInteger [0].getClass());

		// Extract all spaces
		List< ? extends UtilitySolutionSpace< AddableInteger,  U> > spaces = this.getSolutionSpaces(withAnonymVars);

		// Go through the list of spaces
		for (UtilitySolutionSpace< AddableInteger,  U> space : spaces) {

			// Slice the space over the input assignments
			ArrayList<String> vars = new ArrayList<String> (space.getNumberOfVariables());
			for (String var : space.getVariables()) 
				if (assignments.containsKey(var)) 
					vars.add(var);
			int nbrVars = vars.size();
			AddableInteger[] values = (AddableInteger[]) Array.newInstance(this.valInstance.getClass(), nbrVars);
			for (int i = 0; i < nbrVars; i++) 
				values[i] = assignments.get(vars.get(i));
			UtilitySolutionSpace< AddableInteger, U> slice = space.slice(vars.toArray(new String[nbrVars]), values);

			// Join the slice with the output
			output = output.join(slice);
		}
		
		return output;
	}


	/**
	 * @param constraint		the jdom element describing the constraint
	 * @param relation			the jdom element describing the relation
	 * @param store				the store where the variables exist and in which we want to impose the extensional soft constraints
	 * @param constraints 		a list to which Constraints will be added
	 * @param decompCons 		a list to which DecomposeConstraints will be added
	 * @param utilVars			the list of utility variables to which we need to add the utility variable of this constraint
	 */
	public static void parseRelation(Element constraint, Element relation, StoreCloneable store, 
			List<Constraint> constraints, List< DecomposedConstraint<Constraint> > decompCons, ArrayList<IntVarCloneable> utilVars){

		String semantics = relation.getAttributeValue("semantics");

		String constraintName = constraint.getAttributeValue("name");

		// Extract the variables in the constraint
		String scope = constraint.getAttributeValue("scope");

		Pattern pattern = Pattern.compile("\\s+");
		String[] varNames = pattern.split(scope);
		int nbTuples = Integer.parseInt(relation.getAttributeValue("nbTuples"));
		String name = relation.getAttributeValue("name");
		int arity = Integer.parseInt(relation.getAttributeValue("arity"));

		String tuplesString = relation.getText();

		pattern = Pattern.compile("\\|");
		String[] tuples = pattern.split(tuplesString);
		assert nbTuples == 0 || tuples.length == nbTuples : "Relation `" + name + "' has nbTuples == " + nbTuples + 
		" but its description actually contains " + tuples.length + " tuples";

		pattern = Pattern.compile("\\s+");

		// The relation defines a soft extensional constraint
		if(semantics.equals("soft")){

			// JaCoP Variables = variables of the hypercube + the utility variable
			IntVarCloneable[] vars = new IntVarCloneable[varNames.length + 1];

			IntVarCloneable v;
			for(int i = 0; i < varNames.length; i++){
				v = (IntVarCloneable)store.findVariable(varNames[i]);

				// All these variables must be in the store
				assert v != null : "problem: variable " + varNames[i] + " not found in the store!";

				vars[i] = v;
			}

			String defaultCost = relation.getAttributeValue("defaultCost");
			assert nbTuples > 0 || defaultCost != null : "Relation `" + name + "' has nbTuples == " + nbTuples + " and no default cost";
			
			if(nbTuples == 0){
				tuples = new String[0];
			}
			
			Pattern patternColon = Pattern.compile(":");
			pattern = Pattern.compile("\\s+");

			// The domain of the utility variable
			IntervalDomain utilDom = new IntervalDomain (nbTuples);

			// The list that will contain all the supported tuples of the extensional contraint
			ArrayList<int[]> solutions = new ArrayList<int[]>();
			int currentUtil = 0;
			boolean isInfinity = false;

			// In this case, a default utility does not exist or is infeasible.
			// We just have to put all the tuples given explicitly that are feasible in the extensional constraint
			if(defaultCost == null || defaultCost.equals("-infinity") || defaultCost.equals("infinity")){

				ext: for (int i = 0; i < nbTuples; i++){
					if (tuples[i].contains(":")) {
						String[] pair = patternColon.split(tuples[i]);
						tuples[i] = pair[1];
						if(pair[0].trim().equals("-infinity") || pair[0].trim().equals("infinity")){
							isInfinity = true;
						}else{
							currentUtil = parseInt(pair[0].trim());
							isInfinity = false;
						}
					}

					// If the utility represents a feasible value, we need to put the tuple in the extensional constraint
					if(!isInfinity){

						int[] tuple = new int[arity + 1];

						String[] vals = pattern.split(tuples[i].trim());

						assert vals.length == varNames.length : "The tuple " + tuples[i].trim() + " does not specify a value for every variable!";
						
						for (int j = 0; j < varNames.length; j++) {
							tuple[j] = parseInt(vals[j]);

							// the value of the variable is no more contained in its domain (due to a previous slice operation)
							if(!vars[j].dom().contains(tuple[j]))
								continue ext;
						}

						tuple[arity] = currentUtil;

						utilDom.addDom(new IntervalDomain (currentUtil, currentUtil));

						solutions.add(tuple);
					}	
				}

				// In that case, a default utility does exist and it represents a feasible value.
				// We have to put all the tuples given explicitly that are feasible as well as those not specified that have the default utility in the extensional constraint
			}else{
				// As the ExtensionalSupport constraint in JaCoP are not PrimitiveConstraint we cannot use control constraints such as IfThenElse, Or, ...
				// I do not see any tricks to avoid building the complete list of tuples when the default utility is specified and is not equal to the infeasible utility.

				int defaultUtil = parseInt(defaultCost);
				// Add the default value to the domain of the utility variable
				utilDom.addDom(new IntervalDomain (defaultUtil, defaultUtil));

				// Compute the number of solutions
				int nbrSol = 1;
				for(int i = 0; i < varNames.length; i++){
					nbrSol *= vars[i].dom().getSize();
				}

				// We construct the HashMaps that will speed up the index computation of tuples
				@SuppressWarnings("unchecked")
				HashMap<Integer, Integer>[] steps_hashmaps = new HashMap[varNames.length];
				HashMap<Integer, Integer > steps;
				int step = nbrSol;
				IntDomain domain;
				int domain_size;

				// for every variable in the list of variables.
				for(int i = 0; i < varNames.length; i++) {
					//the domain of the ith variable in the list of variables
					domain = vars[i].dom();
					//size of the domain of the ith variable in the array of variables
					domain_size = domain.getSize();

					//the smallest step of the current variable is equivalent to the smallest step of the previous variable 
					//divided by the size of the domain of the current variable
					step = step / domain_size;

					//hashmap that maps a value of a variable to a step in the utility values "values" array
					steps = new HashMap<Integer, Integer>(domain_size);
					for( int j = 0, step_tmp = 0;  j < domain_size;  j++, step_tmp += step )
						steps.put(domain.getElementAt(j), step_tmp);

					steps_hashmaps[ i ] = steps;
				}

				// The array of utilities
				int[] utils = new int[nbrSol];
				Arrays.fill(utils, defaultUtil);
				int[] tuple = new int[arity + 1];

				// We iterate over every explicitly stated tuple
				ext: for(int i = 0; i < nbTuples; i++){

					if (tuples[i].contains(":")) {
						String[] pair = patternColon.split(tuples[i]);
						tuples[i] = pair[1];
						if(pair[0].trim().equals("-infinity") || pair[0].trim().equals("infinity")){
							isInfinity = true;
						}else{
							currentUtil = parseInt(pair[0].trim());
							isInfinity = false;
						}
					}
					
					String[] vals = pattern.split(tuples[i].trim());
					
					assert vals.length == varNames.length : "The tuple " + tuples[i].trim() + " does not specify a value for every variable!";
					
					for (int j = 0; j < varNames.length; j++) {
						tuple[j] = parseInt(vals[j]);

						// the value of the variable is no more contained in its domain (due to a previous slice operation)
						if(!vars[j].dom().contains(tuple[j]))
							continue ext;
					}

					// We calculate the index of this tuple
					int index = 0;
					for(int j = 0; j < varNames.length; j++){
						HashMap<Integer, Integer> steps_hashmap = steps_hashmaps[j];
						assert steps_hashmap != null: "The steps HashMap corresponding to the variable " +  vars[j].id + " has not been initialized!";
						Integer incr = steps_hashmap.get(tuple[j]);
						assert incr != null: "The steps HashMap corresponding to the variable " +  vars[j].id + " does not contain an entry for the value " + tuple[j];
						
						index += incr;
					}

					// We save its utility in the array
					if(isInfinity){
						utils[index] = Integer.MAX_VALUE;
					}else{
						utilDom.addDom(new IntervalDomain (currentUtil, currentUtil));
						utils[index] = currentUtil;
					}
				}


				int[] indexes = new int[varNames.length];
				Arrays.fill(indexes, 0);
				boolean hasIncr;

				// We iterate over our list of solutions and put in the extensional support constraint only the feasible ones
				for(int i = 0; i < nbrSol; i++){
					hasIncr = false;
					if(utils[i] != Integer.MAX_VALUE){
						tuple = new int[arity + 1];
						for (int j = 0; j < varNames.length; j++){
							domain = vars[j].dom();
							tuple[j] = domain.getElementAt(indexes[j]);
						}
						tuple[arity] = utils[i];
						solutions.add(tuple);
					}

					// Get the correct values of the variables for the next tuple
					for (int j = varNames.length-1; j >= 0; j--){
						domain = vars[j].dom();
						if(!hasIncr){
							if(indexes[j] == domain.getSize()-1){
								indexes[j] = 0;
							}else{
								indexes[j]++;
								hasIncr = true;
							}
						}
					}
				}
			}
			
			// Check if the utility variable for this space has already been created
			IntVarCloneable currentUtilVar = (IntVarCloneable) store.findVariable("util_" + constraintName);
			if (currentUtilVar == null) // variable not found; create it 
				currentUtilVar = new IntVarCloneable (store, "util_" + constraintName, utilDom);

			// If the relation can only have the infeasible utility, the utility variable has an empty domain
			if(!utilDom.isEmpty()) 
				utilVars.add(currentUtilVar);
			
			vars[arity] = currentUtilVar;

			if (relation.getAttribute("hypercube") != null) // use a hypercube-based constraint
				constraints.add(new ExtensionalSupportHypercube (store, vars, solutions.toArray(new int [solutions.size()][arity+1])));
			else 
				constraints.add(new ExtensionalSupportSTRCloneable(vars, solutions.toArray(new int[solutions.size()][arity+1])));

		}else{ // hard constraint

			// JaCoP Variables = variables of the hypercube + the utility variable
			IntVarCloneable[] vars = new IntVarCloneable[varNames.length];

			IntVarCloneable v;
			for(int i = 0; i < varNames.length; i++){
				v = (IntVarCloneable)store.findVariable(varNames[i]);

				// All these variables must be in the store
				assert v != null : "problem: variable " + varNames[i] + " not found in the store!";

				vars[i] = v;
			}

			// The list that will contain all the supported tuples of the extensional contraint
			ArrayList<int[]> solutions = new ArrayList<int[]>();

			ext: for (int i = 0; i < nbTuples; i++){

				int[] tuple = new int[arity];

				String[] vals = pattern.split(tuples[i].trim());
				
				assert vals.length == varNames.length : "The tuple " + tuples[i].trim() + " does not specify a value for every variable!";

				for (int j = 0; j < varNames.length; j++) {
					tuple[j] = parseInt(vals[j]);

					// if the value of the variable in the current tuple is not contained in the domain this variable,
					// we do not need to add the tuple in the constraint. Besides ExtensionalConflict would crash.
					// This can happen after a slice operation on a space for example.
					if(!vars[j].dom().contains(tuple[j])){
						continue ext;
					}

				}

				solutions.add(tuple);
			}

			// The relation indicates the supported tuples
			if(semantics.equals("supports")) 
				constraints.add(new ExtensionalSupportSTRCloneable(vars, solutions.toArray(new int[solutions.size()][arity])));

			// The relation indicates the conflicting tuples
			else if(semantics.equals("conflicts") && solutions.size() > 0) 
				constraints.add(new ExtensionalConflictVACloneable(vars, solutions.toArray(new int[solutions.size()][arity])));

			// Unknown semantic
			else 
				System.out.println("The semantics of the relation " + name + " are not valid");
		}
	}

	/**
	 * @param constraint	the jdom element describing the constraint
	 * @param predicate		the jdom element describing the predicate or function
	 * @param store			the store where the variables exist and in which we want to impose the intentional hard constraints
	 * @param constraints 	a list to which Constraints will be added
	 * @param decompCons 	a list to which DecomposeConstraints will be added
	 * @param utilVars		the list of utility variables to which we need to add the utility variable of this constraint (if it is a soft constraint)
	 * @todo test   
	 */
	public static void parsePredicate(Element constraint, Element predicate, StoreCloneable store, 
			List<Constraint> constraints, List< DecomposedConstraint<Constraint> > decompCons, ArrayList<IntVarCloneable> utilVars){
		assert constraint.getChild("parameters") != null;
		assert predicate.getChild("parameters") != null;
		assert predicate.getChild("expression") != null;
		assert predicate.getChild("expression").getChild("functional") != null;
		
		Predicate pred = new Predicate(constraint.getChildText("parameters"), predicate.getChildText("parameters"), predicate.getChild("expression").getChildText("functional"), store);
		decompCons.add(pred);

		if (pred.utilVar != null) 
			utilVars.add(pred.utilVar);
	}
	
	/** Parses a constraint and imposes it
	 * @param constraint	the jdom element describing the global constraint
	 * @param store			the store where the variables exist
	 * @throws FailException thrown constraint imposition resulted in infeasibility
	 */
	public static void imposeGlobalConstraint (Element constraint, StoreCloneable store) 
			throws FailException {
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint> ();
		ArrayList< DecomposedConstraint<Constraint> > decompCons = new ArrayList< DecomposedConstraint<Constraint> > ();
		
		/// @todo Add support for utility variables
		parseGlobalConstraint(constraint, store, constraints, decompCons, null);
		
		for (Constraint cons : constraints) 
			store.impose(cons);
		for (DecomposedConstraint<Constraint> cons : decompCons) 
			store.imposeDecomposition(cons);
	}
	
	/** Parses a predicate and imposes it
	 * @param constraint		the jdom element describing the constraint
	 * @param predicate			the jdom element describing the predicate
	 * @param store				the store where the variables exist and in which we want to impose the extensional soft constraints
	 * @param utilVars			the list of utility variables to which we need to add the utility variable of this constraint
	 * @throws FailException 	thrown constraint imposition resulted in infeasibility
	 */
	public static void imposePredicate (Element constraint, Element predicate, StoreCloneable store, ArrayList<IntVarCloneable> utilVars) 
			throws FailException {
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint> ();
		ArrayList< DecomposedConstraint<Constraint> > decompCons = new ArrayList< DecomposedConstraint<Constraint> > ();
		
		parsePredicate(constraint, predicate, store, constraints, decompCons, utilVars);
		
		for (Constraint cons : constraints) 
			store.impose(cons);
		for (DecomposedConstraint<Constraint> cons : decompCons) 
			store.imposeDecomposition(cons);
	}

	/** Parses a constraint without imposing it
	 * @param constraint	the jdom element describing the global constraint
	 * @param store			the store where the variables exist
	 * @param constraints 	a list to which Constraints will be added
	 * @param decompCons 	a list to which DecomposeConstraints will be added
	 * @param utilVars 		a list to which utility variables will be added
	 */
	public static void parseGlobalConstraint(Element constraint, StoreCloneable store, 
			List<Constraint> constraints, List< DecomposedConstraint<Constraint> > decompCons, List<IntVarCloneable> utilVars){

		// Extract the variables in the constraint
		String scope = constraint.getAttributeValue("scope");
		Pattern pattern = Pattern.compile("\\s+");
		String[] varNames = pattern.split(scope);

		int arity = Integer.parseInt(constraint.getAttributeValue("arity"));
		String refName = constraint.getAttributeValue("reference");

		if(refName.equals("global:weightedSum")){

			String parameters = constraint.getChild("parameters").getText().replace('\n', ' ').trim();
			pattern = Pattern.compile("\\[(.*)\\]\\s*(-?\\d+)");
			Pattern pattern2 = Pattern.compile("\\{\\s*(-?\\d+)\\s+(\\S+)\\s*\\}");

			ArrayList<IntVarCloneable> vars = new ArrayList<IntVarCloneable>();
			ArrayList<Integer> weights = new ArrayList<Integer>();

			Matcher m = pattern.matcher(parameters);
			m.find();
			int rightHandVal = parseInt(m.group(2));

			m = pattern2.matcher(m.group(1));
			
			Pattern constantPat = Pattern.compile("-?\\d+");
			
			int i = 0;
			IntVarCloneable v;
			for(; m.find(); i++){
				
				// constant parameter
				if(constantPat.matcher(m.group(2)).matches()){
					int val = parseInt(m.group(2));
					v = new IntVarCloneable (store, val, val);
				// variable name
				}else{
					assert i < arity : "The weightedSum constraint `" + constraint.getAttributeValue("name") + "' contains more terms than its arity (" + constraint.getAttributeValue("arity") + ")";
					assert Arrays.asList(varNames).contains(m.group(2)): "mismatch between the constraint scope and the variables in parameters!";

					v = (IntVarCloneable) store.findVariable(m.group(2));
					// All these variables must be in the store
					assert v != null: "The variable " + m.group(2) + " cannot be found in the store!";
				}
				vars.add(v);
				weights.add(parseInt(m.group(1)));
			}

			IntVarCloneable rightHandVar = new IntVarCloneable (store, "rhs_" + new Object().hashCode(), rightHandVal, rightHandVal);
			vars.add(rightHandVar);
			weights.add(-1);

			String atom = ((Element)constraint.getChild("parameters").getChildren().get(0)).getName();
			String op = "";

			if(atom.equals("eq")){

				op = "==";

			}else if(atom.equals("ne")){

				op = "!=";

			}else if(atom.equals("ge")){

				op = ">="; 

			}else if(atom.equals("gt")){

				op = ">";

			}else if(atom.equals("le")){

				op = "<=";

			}else if(atom.equals("lt")){

				op = "<";

			}else{
				assert false: "atom " + atom + " not recognized!";
			}

			constraints.add(new LinearIntCloneable(vars, weights, op, 0));

		}else if(refName.equals("global:allDifferent")){
			
			// Extract the parameters of the constraint
			assert constraint.getChild("parameters") != null : "No parameters passed to the constraint " + constraint.getAttributeValue("name");
			String params = constraint.getChildText("parameters").replace('\n', ' ').trim();
			params = params.substring(1, params.length() - 1).trim(); // removing the brackets
			pattern = Pattern.compile("\\s+");
			String[] paramVarNames = pattern.split(params);
			
			Pattern constantPat = Pattern.compile("-?\\d+");
			
			// find JaCoP variables
			IntVarCloneable[] vars = new IntVarCloneable[paramVarNames.length];

			IntVarCloneable v;
			for(int i = 0; i < paramVarNames.length; i++){
				// constant parameter
				if(constantPat.matcher(paramVarNames[i]).matches()){
					int val = parseInt(paramVarNames[i]);
					v = new IntVarCloneable(store, val, val);
				// variable name
				}else{	
					assert Arrays.asList(varNames).contains(paramVarNames[i]): "mismatch between the constraint scope and the variables in parameters!";
					v = (IntVarCloneable)store.findVariable(paramVarNames[i]);
				
					// All these variables must be in the store
					assert v != null : "problem: variable " + paramVarNames[i] + " not found in the store! Note: constant parameters not supported in allDifferent";
				}
				vars[i] = v;
			}

			constraints.add(new AlldifferentCloneable(vars));
		
		} else if (refName.equals("global:diff2")) {
			
			// Parse the parameters
			String params = constraint.getChildText("parameters").replace('\n', ' ').trim();
			params = params.substring(1, params.length() - 1).trim(); // removing the brackets
			
			// Overall pattern for the parameters: a whitespace-delimited list of rectangles, 
			// each rectangle being a bracketed, whitespace-delimited list of 2 items: 
			// 1) a curly-bracketed, whitespace-delimited list of 2 origin variables; 
			// 2) a curly-bracketed, whitespace-delimited list of 2 size variables. 
			pattern = Pattern.compile("\\[(.*)\\]"); // each rectangle is delimited by brackets
			Matcher matcher = pattern.matcher(params);
			Pattern curlyPat = Pattern.compile("\\{(.*)\\}\\s+\\{(.*)\\}"); // each sublist in a rectangle is delimited by curly brackets
			Matcher curlyMat;
			Pattern constPat = Pattern.compile("-?\\d+"); // a constant (vs. a variable)
			
			// Loop over the rectangles
			ArrayList< ArrayList<IntVarCloneable> > rectangles = new ArrayList< ArrayList<IntVarCloneable> > ();
			while(matcher.find()) {
				curlyMat = curlyPat.matcher(matcher.group(1));
				curlyMat.find();
				String[] origins = curlyMat.group(1).split("\\s+");
				String[] sizes = curlyMat.group(2).split("\\s+");
				String[][] allVars = new String[][] {origins, sizes};
				
				assert origins.length == sizes.length : 
					"Invalid XCSP description of a rectangle (inconsistent numbers of dimensions): " + matcher.group(1);
				assert origins.length == 2: "Expected 2 dimensions, encountered " + origins.length;
				
				ArrayList<IntVarCloneable> rectVars = new ArrayList<IntVarCloneable> (2 * origins.length);
				rectangles.add(rectVars);
				for (String[] vars : allVars) {
					for (String var : vars) {
						
						// Create auxiliary grounded variables to replace constants
						/// @todo Reuse code by creating a method for this
						if (constPat.matcher(var).matches()) {
							int constant = parseInt(var);
							rectVars.add(new IntVarCloneable (store, constant, constant));
						} else {
							assert store.findVariable(var) != null : "Unknown variable: " + var;
							rectVars.add((IntVarCloneable) store.findVariable(var));
						}
					}
				}
			}
			
			constraints.add(new Diff2Cloneable (rectangles));
			
		} else if (refName.equals("global:cumulative")) {
			
			// Parse the parameters
			String parameters = constraint.getChild("parameters").getText().replace('\n', ' ').trim();
			
			// A bracketed list, an operator element (<eq/> or <le/>), and the limit (variable or integer)
			pattern = Pattern.compile("\\[(.*)\\]\\s*(\\S+)");
			Matcher m = pattern.matcher(parameters);
			m.find();
			String limit = m.group(2);
			
			// Get the variable for the limit
			IntVarCloneable limitVar;
			Pattern constPat = Pattern.compile("-?\\d+"); // a constant (vs. a variable)
			/// @todo Reuse code by creating a method for this
			if (constPat.matcher(limit).matches()) { // a constant
				int constant = parseInt(limit);
				limitVar = new IntVarCloneable (store, constant, constant);
			} else {
				assert store.findVariable(limit) != null : "Unknown variable: " + limit;
				limitVar = (IntVarCloneable) store.findVariable(limit);
			}
			
			
			// Check whether the limit is tight
			assert constraint.getChild("parameters").getChildren().size() == 1 : "Single operator <eq/> or <le/> not found in " + constraint;
			String opName = constraint.getChild("parameters").getChildren().get(0).getName();
			assert opName.equals("eq") || opName.equals("le") : "Unsupported operator <" + opName + "/> (expected <eq/> or <le/>)";
			final boolean tight = "eq".equals(opName);
			
			
			// Go through the list of tasks
			parameters = m.group(1).trim();
			parameters = parameters.substring(1, parameters.length() - 1); // remove the first and last curly brackets
			String[] tasks = parameters.split("\\}\\s*\\{");
			final int nbrTasks = tasks.length;
			IntVarCloneable[] starts = new IntVarCloneable [nbrTasks];
			IntVarCloneable[] durations = new IntVarCloneable [nbrTasks];
			IntVarCloneable[] resources = new IntVarCloneable [nbrTasks];
			IntVarCloneable[][] vars = new IntVarCloneable[][] { starts, durations, resources };
			for (int i = nbrTasks - 1; i >= 0; i--) { // for each task
				String[] task = tasks[i].trim().split("\\s+");
				
				for (int j = 0; j <= 2; j++) { // for each variable
					String var = task[j];
					
					/// @todo Reuse code by creating a method for this
					if (constPat.matcher(var).matches()) { // a constant
						int constant = parseInt(var);
						vars[j][i] = new IntVarCloneable (store, constant, constant);
					} else {
						assert store.findVariable(var) != null : "Unknown variable: " + var;
						vars[j][i] = (IntVarCloneable) store.findVariable(var);
					}
				}
			}
			
			constraints.add(new CumulativeCloneable (starts, durations, resources, limitVar, true, true, tight));
			
		} else if (refName.equals("global:element")) {
			
			// Parse the parameters
			String parameters = constraint.getChild("parameters").getText().replace('\n', ' ').trim();
			
			// A variable, bracketed list, and a variable or constant
			pattern = Pattern.compile("(\\S+)\\s*\\[(.*)\\]\\s*(\\S+)");
			Matcher m = pattern.matcher(parameters);
			m.find();
			String indexVarStr = m.group(1).trim();
			String listStr = m.group(2).trim();
			String varStr = m.group(3).trim();
			
			// Parse the index variable
			assert store.findVariable(indexVarStr) != null : "Unknown variable: " + indexVarStr;
			IntVarCloneable indexVar = (IntVarCloneable) store.findVariable(indexVarStr);
			
			// Parse the other variable
			IntVarCloneable var;
			Pattern constPat = Pattern.compile("-?\\d+"); // a constant (vs. a variable)
			/// @todo Reuse code by creating a method for this
			if (constPat.matcher(varStr).matches()) { // a constant
				int constant = parseInt(varStr);
				var = new IntVarCloneable (store, constant, constant);
			} else {
				assert store.findVariable(varStr) != null : "Unknown variable: " + varStr;
				var = (IntVarCloneable) store.findVariable(varStr);
			}
			
			// Parse the list
			String[] strList = listStr.split("\\s+");
			final int listSize = strList.length;
			
			// Check whether the list only contains constants 
			boolean onlyConst = true;
			for (int i = 0; i < listSize && onlyConst; i++) 
				onlyConst = onlyConst && constPat.matcher(strList[i]).matches();
			
			if (onlyConst) { // use ElementInteger
				
				ArrayList<Integer> list = new ArrayList<Integer> (listSize);
				for (int i = 0; i < listSize; i++) 
					list.add(parseInt(strList[i]));
				
				store.impose(new ElementIntegerCloneable(indexVar, list, var));
				
			} else { // use ElementVariable
				
				IntVarCloneable[] list = new IntVarCloneable [listSize];
				String elem;
				Pattern intervPat = Pattern.compile("-?\\d+\\.\\.-?\\d+"); // a constant followed by two dots followed by a constant
				for (int i = 0; i < listSize; i++) {
					
					if (constPat.matcher(elem = strList[i].trim()).matches()) { // a constant
						int constant = parseInt(elem);
						list[i] = new IntVarCloneable (store, constant, constant);
					} else if (intervPat.matcher(elem).matches()) { // an interval 
						String[] interval = elem.split("\\.\\.");
						list[i] = new IntVarCloneable (store, parseInt(interval[0]), parseInt(interval[1]));
					} else { // a variable
						assert store.findVariable(elem) != null : "Unknown variable: " + elem;
						list[i] = (IntVarCloneable) store.findVariable(elem);
					}
				}
				
				store.impose(new ElementVariableCloneable (indexVar, list, var));
			}
			
			//store.impose(new Cumulative (starts, durations, resources, limitVar, true, true, tight));
			
		} else {
			System.err.println("The global constraint " + constraint.getAttributeValue("reference") + " is not supported");
			System.exit(2);
		}
	}

	/** Rescales the problem
	 * @param multiply 	multiplies all costs/utilities by \a multiply
	 * @param add 		after multiplying all costs/utilities by \a multiply (if required), adds \a add
	 */
	public void rescale(U multiply, U add) {

		Element relations = this.root.getChild("relations");

		if(relations != null){

			// Modify each relation
			for (Element relElmt : (List<Element>) relations.getChildren()) {

				// Take care of the default cost
				String defaultCost = relElmt.getAttributeValue("defaultCost");
				if (defaultCost != null) 
					relElmt.setAttribute("defaultCost", multiply.fromString(defaultCost.trim()).multiply(multiply).add(add).toString());

				// Take care of each utility/cost in the list of tuples
				StringBuilder builder = new StringBuilder ();
				for (Iterator<String> iter = Arrays.asList(relElmt.getText().split("\\|")).iterator(); iter.hasNext(); ) {

					String tuple = iter.next();
					String[] split = tuple.split(":");
					assert split.length > 0 && split.length <= 2 : "Incorrect tuple format: " + tuple;
					if (split.length > 1) // there is a utility specified
						builder.append(multiply.fromString(split[0].trim()).multiply(multiply).add(add) + ":" + split[1]);
					else 
						builder.append(split[0]);

					if (iter.hasNext()) 
						builder.append("|");
				}
				relElmt.setText(builder.toString());
			}
		}
		
		
		Element functions = this.root.getChild("functions");
		
		if (functions != null) {
			
			// Modify each function
			for (Element funcElmt : (List<Element>) functions.getChildren()) {

				// Get the functional expression expr and encapsulate it inside add(add, mul(multipy, expr))
				Element functionalElmt = funcElmt.getChild("expression").getChild("functional");
				functionalElmt.setText("add(" + add + ", mul(" + multiply + ", " + functionalElmt.getText() + "))");
			}
		}
	}


	/** @see XCSPparser#getSubProblem(String) */
	@Override
	public JaCoPxcspParser<U> getSubProblem (String agent) {

		/// @todo Could it be possible to reuse more code from the superclass?

		// Extract the set of variables owned by the agent
		HashSet<Element> varElmts = new HashSet<Element> ();
		for (Element var : (List<Element>) root.getChild("variables").getChildren()) 
			if (agent.equals(var.getAttributeValue("agent"))) 
				varElmts.add(var);

		// Create the XCSP instance element
		Element instance = new Element ("instance");

		// Create the "presentation" element
		Element presentation = new Element ("presentation");
		instance.addContent(presentation);
		presentation.setAttribute("name", agent);
		presentation.setAttribute("maximize", Boolean.toString(this.maximize()));
		presentation.setAttribute("format", "XCSP 2.1_FRODO");
		
		// Create the agents
		Element agents = new Element ("agents");
		agents.setAttribute("self", agent);
		instance.addContent(agents);
		HashSet<String> knownAgents = new HashSet<String> ();
		knownAgents.add(agent);
		if (this.mpc || this.publicAgents) // the agent is supposed to know all the agents
			knownAgents.addAll(this.getAgents());

		// Create the domains
		Element domains = new Element ("domains");
		instance.addContent(domains);

		// Create the variables
		Element variables = new Element ("variables");
		instance.addContent(variables);
		HashSet<String> varNames = new HashSet<String> (varElmts.size()); // internal variables and relevant external variables
		for (Element varElmt : varElmts) {
			varNames.add(varElmt.getAttributeValue("name"));
		}
		
		// In MPC mode, all variables are public
		if (this.mpc) 
			varElmts.addAll((List<Element>) root.getChild("variables").getChildren());

		// Create the constraints
		Element constraints = new Element ("constraints");
		HashSet<String> relationNames = new HashSet<String> ();
		HashSet<String> predicateNames = new HashSet<String> ();
		HashSet<String> probNames = new HashSet<String> ();
		HashSet<String> constNames = new HashSet<String> ();

		// Go through the list of constraints several times until we are sure we have identified all variables that should be known to this agent
		HashMap< String, HashSet<String> > varScopes = new HashMap< String, HashSet<String> > ();
		int nbrVars;
		do {
			nbrVars = varNames.size();

			// Go through the list of all constraints in the overall problem
			for (Element constraint : (List<Element>) root.getChild("constraints").getChildren()) {

				// Skip this constraint if it has already been added
				String constName = constraint.getAttributeValue("name");
				if (constNames.contains(constName)) 
					continue;

				// Get the list of variables in the scope of the constraint
				HashSet<String> scope = new HashSet<String> (Arrays.asList(constraint.getAttributeValue("scope").split("\\s+")));

				String refName = constraint.getAttributeValue("reference");

				ConstraintType cons = ConstraintType.EXTENSIONAL;

				// Check the nature of the constraint

				// Global constraint
				if(constraint.getAttributeValue("reference").startsWith("global:")){

					cons =  ConstraintType.GLOBAL;

					// Constraint in intension
				}else if(constraint.getChild("parameters") != null){

					cons = ConstraintType.INTENSIONAL;

				}

				// Check if the agent is not supposed to know the constraint
				String constOwner = constraint.getAttributeValue("agent");
				if (! "PUBLIC".equals(constOwner) && constOwner != null && ! constOwner.equals(agent)) {
					
					if (! this.mpc) { // record the variable scopes
						for (String var : scope) {
							HashSet<String> varScope = varScopes.get(var);
							if (varScope == null) {
								varScope = new HashSet<String> ();
								varScopes.put(var, varScope);
							}
							varScope.add(constOwner);
						}
					}
					
					continue;
				}

				// If any of the variables in the scope is owned by this agent or the constraint is a probability law that must be known to the agent, 
				// add the constraint to the list of constraints
				final boolean knownConst = "PUBLIC".equals(constOwner) || agent.equals(constOwner);
				for (String var : scope) {
					if (knownConst || varNames.contains(var)) {


						switch (cons) {
						// Skip this variable if it is apparently not necessary for the agent to know this constraint
						case PROBABILITY: // probability space
							if (! this.isRandom(var)) 
								continue;
							probNames.add(refName);
							break;

						case EXTENSIONAL:
							if (!this.extendedRandNeighborhoods && this.isRandom(var))
								continue;
							relationNames.add(refName);
							break;

						case INTENSIONAL: 
							if (!this.extendedRandNeighborhoods && this.isRandom(var))
								continue;
							predicateNames.add(refName);
							break;

						default:
							break;
						}

						constraints.addContent((Element) constraint.clone());
						constNames.add(constName);

						// Add all variables in the scope to the list of variables known to this agent
						for (Element varElmt : (List<Element>) root.getChild("variables").getChildren()) {
							String varName = varElmt.getAttributeValue("name");
							if (scope.contains(varName)) {
								varElmts.add(varElmt);
								if (varElmt.getAttributeValue("agent") == null) 
									varNames.add(varName);
							}
						}

						break;
					}
				}
			}
		} while (nbrVars != varNames.size()); // loop as long as another variable has been added to the list of known variables

		// Set the number of constraints
		constraints.setAttribute("nbConstraints", Integer.toString(constraints.getContentSize()));
		
		// Add the agents that own constraints over shared variables and my own variables
		for (Element constraint : (List<Element>) root.getChild("constraints").getChildren()) {
			
			// Get the list of variables in the scope of the constraint
			HashSet<String> scope = new HashSet<String> (Arrays.asList(constraint.getAttributeValue("scope").split("\\s+")));
			
			// Check whether the constraint owner should be known to the agent because the constraint scope involves a variable they share
			String constOwner = constraint.getAttributeValue("agent");
			if (! "PUBLIC".equals(constOwner) && constOwner != null && ! constOwner.equals(agent)) {
				for (String var : scope) {
					if (! this.isRandom(var) && varNames.contains(var)) { // skip random variables and unknown variables
						String varOwner = this.getOwner(var);
						if (varOwner == null || varOwner.equals(agent)) { // the variable is shared or owned by this agent
							knownAgents.add(constOwner);
							break;
						}
					}
				}
			}
		}

		// Add the domains of the variables
		HashSet<String> domNames = new HashSet<String> ();
		for (Element varElmt : varElmts) {

			String domName = varElmt.getAttributeValue("domain");
			if (! domNames.add(domName)) // domain already added to the list of domains
				continue;
			for (Element domain : (List<Element>) root.getChild("domains").getChildren()) {
				if (domName.equals(domain.getAttributeValue("name"))) {
					domains.addContent((Element) domain.clone());
					break;
				}
			}
		}

		// Set the number of domains
		domains.setAttribute("nbDomains", Integer.toString(domNames.size()));

		// Add all variables known to this agent
		variables.setAttribute("nbVariables", Integer.toString(varElmts.size()));
		for (Element varElmt : varElmts) {
			Element newVarElmt = (Element) varElmt.clone();
			variables.addContent(newVarElmt);
			
			// Check the owner of this variable
			String owner = varElmt.getAttributeValue("agent");
			if (owner != null) 
				knownAgents.add(owner);
			else if (! "random".equals(varElmt.getAttributeValue("type"))) { // shared variable; set its agent scope
				HashSet<String> varScope = varScopes.get(varElmt.getAttributeValue("name"));
				if (varScope != null) {
					String scope = "";
					for (String neigh : varScope) 
						scope += neigh + " ";
					newVarElmt.setAttribute("scope", scope);
				}
			}
		}
		
		// Fill in the list of agents
		agents.setAttribute("nbAgents", Integer.toString(knownAgents.size()));
		for (Element agentElmt : (List<Element>) this.root.getChild("agents").getChildren()) 
			if (knownAgents.contains(agentElmt.getAttributeValue("name"))) 
				agents.addContent((Element) agentElmt.clone());

		Element elmt;
		int maxConstraintArity = 0;

		
		// Create the relations (if the original problem contained any)
		if (root.getChild("relations") != null) {

			// Create the relations
			elmt = new Element ("relations");
			instance.addContent(elmt);

			// Go through the list of all relations in the overall problem
			for (Element relation : (List<Element>) root.getChild("relations").getChildren()) {

				// Add the relation to the list of relations if it is referred to by any of this agent's constraints
				if (relationNames.remove(relation.getAttributeValue("name"))) {
					elmt.addContent((Element) relation.clone());
					maxConstraintArity = Math.max(maxConstraintArity, Integer.parseInt(relation.getAttributeValue("arity")));
				}
			}
			elmt.setAttribute("nbRelations", Integer.toString(elmt.getContentSize()));
		}

		if (! relationNames.isEmpty()) 
			this.foundUndefinedRelations(relationNames);

		
		// Create the probabilities (if the original problem contained any)
		if (root.getChild("probabilities") != null) {
			elmt = new Element ("probabilities");
			instance.addContent(elmt);
			elmt.setAttribute("nbProbabilities", Integer.toString(probNames.size()));

			// Go through the list of all probabilities in the overall problem
			for (Element probability : (List<Element>) root.getChild("probabilities").getChildren()) {

				// Add the probability to the list of probabilities if it is referred to by any of this agent's constraints
				if (probNames.remove(probability.getAttributeValue("name"))) {
					elmt.addContent((Element) probability.clone());
					maxConstraintArity = Math.max(maxConstraintArity, Integer.parseInt(probability.getAttributeValue("arity")));
				}
			}
		}

		if (! probNames.isEmpty()) 
			System.err.println("Undefined probabilities: " + probNames);

		
		// Create the predicates (if the original problem contained any)
		if (root.getChild("predicates") != null) {
			elmt = new Element ("predicates");
			instance.addContent(elmt);

			// Go through the list of all predicates in the overall problem
			for (Element predicate : (List<Element>) root.getChild("predicates").getChildren()) {

				// Add the predicate to the list of predicates if it is referred to by any of this agent's constraints
				if (predicateNames.remove(predicate.getAttributeValue("name"))) {
					elmt.addContent((Element) predicate.clone());
				}
			}
			elmt.setAttribute("nbPredicates", Integer.toString(elmt.getContentSize()));
		}

		
		// Create the functions (if the original problem contained any)
		if (root.getChild("functions") != null) {
			elmt = new Element ("functions");
			instance.addContent(elmt);

			// Go through the list of all functions in the overall problem
			for (Element function : (List<Element>) root.getChild("functions").getChildren()) {

				// Add the function to the list of functions if it is referred to by any of this agent's constraints
				if (predicateNames.remove(function.getAttributeValue("name"))) {
					elmt.addContent((Element) function.clone());
				}
			}
			elmt.setAttribute("nbFunctions", Integer.toString(elmt.getContentSize()));
		}

		if (! predicateNames.isEmpty()) 
			System.err.println("Undefined predicates or functions: " + predicateNames);

		
		// Set the maxConstraintArity
		presentation.setAttribute("maxConstraintArity", Integer.toString(maxConstraintArity));

		// Add the "constraints" element after the "relations" and "probabilities" element
		instance.addContent(constraints);
		
		JaCoPxcspParser<U> out = newInstance (instance);
		return out;
	}
	
	/** @see XCSPparser#foundUndefinedRelations(java.util.HashSet) */
	@Override
	protected void foundUndefinedRelations(HashSet<String> relationNames) {
		System.err.println("Undefined relations: " + relationNames);
	}
	
	/** Attempts to parse a string as an int, warning for double truncation
	 * @param str 	the input string 
	 * @return 		the parsed int, possibly truncated
	 */
	private static int parseInt (String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) { // not an int; try to parse as a double and truncate
			assert warnTruncation(str);
			return (int) Double.parseDouble(str);
		}
	}

	/** Warns for truncation
	 * @param str 	the string
	 * @return true
	 */
	static private boolean warnTruncation (String str) {
		System.err.println("WARNING! Attempting to parse `" + str + "' as a double and truncate it to an int");
		return true;
	}
	
	/** @see XCSPparser#parse() */
	@Override
	public JaCoPproblem<U> parse() {
		
		JaCoPproblem<U> problem = new JaCoPproblem<U> (this.maximize(), this.publicAgents, this.mpc, this.extendedRandNeighborhoods);
		problem.setUtilClass(this.utilClass);
		
		// Add the agents
		problem.setAgent(this.getAgent());
		for (String agent : this.getAgents()) 
			problem.addAgent(agent);
		
		// Create the store and its variables
		this.createStore();
		
		// Add the variables
		for (Var var : this.store.vars) {
			if (var == null) break;
			problem.addVariable((IntVarCloneable) var, this.getOwner(var.id()));
		}
		
		// Add the constraints
		for (UtilitySolutionSpace<AddableInteger, U> space : this.getSolutionSpaces()) 
			problem.addSolutionSpace(space);
		
		return problem;
	}
}


