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

package frodo2.solutionSpaces.JaCoP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacop.constraints.Constraint;
import org.jacop.constraints.DecomposedConstraint;
import org.jacop.constraints.DecomposedConstraintCloneableInterface;
import org.jacop.constraints.XeqCCloneable;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.SmallDenseDomain;
import org.jacop.core.StoreCloneable;
import org.jacop.core.ValueEnumeration;
import org.jacop.core.Var;

import frodo2.algorithms.AbstractProblem;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.BasicUtilitySolutionSpace;
import frodo2.solutionSpaces.DCOPProblemInterface;
import frodo2.solutionSpaces.ProblemInterface;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.UtilitySolutionSpace.SparseIterator;
import frodo2.solutionSpaces.hypercube.ScalarHypercube;

/** A DCOP problem that is defined using JaCoP variables and constraints
 * @author Thomas Leaute
 * @param <U> the type used for utility values
 */
public class JaCoPproblem  < U extends Addable<U> > extends AbstractProblem<AddableInteger, U> implements Cloneable {

	/** Used for serialization */
	private static final long serialVersionUID = 8959353326324207706L;
	
	/** The JaCoP variables, indexed by name */
	private HashMap<String, IntVarCloneable> vars = new HashMap<String, IntVarCloneable> ();
	
	/** The zero utility */
	private U zeroUtil;
	
	/** Constructor
	 * @param maximize 	whether this is a maximization problem (true) or a minimization problem (false)
	 */
	public JaCoPproblem (boolean maximize) {
		this(maximize, false, false, false);
		this.zeroUtil = super.getZeroUtility();
	}
	
	/** Constructor
	 * @param maximize 						Whether this is a maximization problem (true) or a minimization problem (false)
	 * @param publicAgents 					Whether each agent knows the identities of all agents
	 * @param mpc 							Whether to behave in MPC mode (each agent knows the identities of all agents and knows all variables)
	 * @param extendedRandNeighborhoods 	Whether neighborhood relationships between decision variables are extended through random variables
	 */
	public JaCoPproblem (boolean maximize, boolean publicAgents, boolean mpc, boolean extendedRandNeighborhoods) {
		super(maximize, publicAgents, mpc, extendedRandNeighborhoods);
	}
	
	/** @return the JaCoP variables */
	public Collection<IntVarCloneable> getJaCoPvars () {
		return this.vars.values();
	}
	
	/** Adds a JaCoP variable
	 * @param var 		the variable
	 * @param owner 	the owning agent (null if the variable is public)
	 */
	public void addVariable (IntVarCloneable var, String owner) {
		this.vars.put(var.id(), var);
		this.owners.put(var.id, owner);
		
		if (owner != null) 
			this.agents.add(owner);
		
		// Add the domain
		AddableInteger[] dom = new AddableInteger [var.domain.getSize()];
		int i = 0;
		for (ValueEnumeration iter = var.domain.valueEnumeration(); iter.hasMoreElements(); i++) 
			dom[i] = new AddableInteger (iter.nextElement());
		this.domains.put(var.id, dom);
	}
	
	/** Adds a JaCoP Constraint
	 * @param constraint 	the constraint
	 */
	public void addConstraint (Constraint constraint) {
		this.addSoftConstraint(constraint, null);
	}
	
	/** Adds a JaCoP Constraint
	 * @param constraint 	the constraint
	 * @param objVar 		the objective variable to be minimized or maximized
	 */
	public void addSoftConstraint (Constraint constraint, IntVarCloneable objVar) {
		this.addSoftConstraint(constraint, objVar, false);
	}
	
	/** Adds a JaCoP Constraint
	 * @param constraint 		the constraint
	 * @param objVar 			the objective variable to be minimized or maximized
	 * @param excludeObjVar 	whether to exclude the objective variable from the variables in scope of the output JaCoPutilSpace
	 */
	private void addSoftConstraint (Constraint constraint, IntVarCloneable objVar, final boolean excludeObjVar) {
			
		assert ! this.hasConstraint (constraint.id()) : "A constraint already exists with the same name `" + constraint.id() + "'";
		
		// Record the objective variable, if any
		ArrayList<IntVarCloneable> utilVars;
		if (objVar == null) 
			utilVars = new ArrayList<IntVarCloneable> (0);
		else {
			utilVars = new ArrayList<IntVarCloneable> (1);
			utilVars.add(objVar);
		}
		
		// Look up the variables and construct their domains
		Set<? extends Var> jacopVars = constraint.arguments();
		HashMap<String, AddableInteger[]> allVars = new HashMap<String, AddableInteger[]> (jacopVars.size());
		
		int nbrMyVars = jacopVars.size();
		if (excludeObjVar && objVar != null && jacopVars.contains(objVar)) 
			nbrMyVars--; // excluding the objective variable from the space
		IntVarCloneable[] myVars = new IntVarCloneable [nbrMyVars];
		
		int i = 0;
		for (Var jacopVar : jacopVars) 
			if (! excludeObjVar || ! jacopVar.equals(objVar)) // check whether we should exclude this objective variable from the space
				allVars.put((myVars[i++] = (IntVarCloneable)jacopVar).id(), this.buildDomain(jacopVar));
		
		super.spaces.add(new JaCoPutilSpace<U> (constraint.id(), Arrays.asList(constraint), 
				new ArrayList< DecomposedConstraint<Constraint> > (0), utilVars, allVars, myVars, new IntVarCloneable [0], new IntVarCloneable [0], 
				super.maximize, this.zeroUtil, super.getInfeasibleUtil()));
	}
	
	/** Constructs an AddableInteger[] domain for an input JaCoP variable
	 * @param jacopVar 	the JaCoP variable
	 * @return an AddableInteger[] domain
	 */
	private AddableInteger[] buildDomain(Var jacopVar) {
		
		int i = jacopVar.dom().getSize();
		AddableInteger[] out = new AddableInteger [i];
		
		i = 0;
		for (ValueEnumeration iter = jacopVar.dom().valueEnumeration(); iter.hasMoreElements(); )
			out[i++] = new AddableInteger (iter.nextElement());
		
		return out;
	}
	
	/** Adds a JaCoP DecomposedConstraint
	 * @param constraint 	the constraint
	 */
	public void addDecompConstraint (DecomposedConstraintCloneableInterface constraint) {
		this.addSoftDecompConstraint(constraint, null);
	}
		
	/** Adds a JaCoP soft DecomposedConstraint
	 * @param constraint	the constraint
	 * @param objVar 		the objective variable to be minimized or maximized (may be null)
	 */
	@SuppressWarnings("unchecked")
	public void addSoftDecompConstraint (DecomposedConstraintCloneableInterface constraint, IntVarCloneable objVar) {
		
		assert ! this.hasConstraint (constraint.id()) : "A constraint already exists with the same name `" + constraint.id() + "'";
		
		// Record the objective variable, if any
		ArrayList<IntVarCloneable> utilVars;
		if (objVar == null) 
			utilVars = new ArrayList<IntVarCloneable> (0);
		else {
			utilVars = new ArrayList<IntVarCloneable> (1);
			utilVars.add(objVar);
		}
		
		// Look up the variables and construct their domains
		List<? extends Var> jacopVars = constraint.arguments();
		HashMap<String, AddableInteger[]> allVars = new HashMap<String, AddableInteger[]> (jacopVars.size());
		IntVarCloneable[] myVars = new IntVarCloneable [jacopVars.size()];
		int i = 0;
		for (Var jacopVar : jacopVars) 
			allVars.put((myVars[i++] = (IntVarCloneable)jacopVar).id(), this.buildDomain(jacopVar));
		
		super.spaces.add(new JaCoPutilSpace<U> (constraint.id(), new ArrayList<Constraint> (0), Arrays.asList((DecomposedConstraint<Constraint>) constraint), 
				utilVars, allVars, myVars, new IntVarCloneable [0], new IntVarCloneable [0], 
				super.maximize, this.zeroUtil, super.getInfeasibleUtil()));
	}
	
	/** Checks whether this problem contains a space with the input name
	 * @param name 	the name of the space
	 * @return whether this problem contains a space with the input name; returns false if the input name is null
	 */
	private boolean hasConstraint(String name) {
		
		if (name == null) 
			return false;
		
		for (UtilitySolutionSpace<AddableInteger, U> space : super.spaces) 
			if (name.equals(space.getName())) 
				return true;
		
		return false;
	}

	/** @see ProblemInterface#reset(ProblemInterface) */
	@Override
	public void reset(ProblemInterface<AddableInteger, U> newProblem) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see ProblemInterface#setDomClass(java.lang.Class) */
	@Override
	public void setDomClass(Class<AddableInteger> domClass) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see ProblemInterface#getDomClass() */
	@Override
	public Class<AddableInteger> getDomClass() {
		return AddableInteger.class;
	}
	
	/** @see ProblemInterface#multipleTypes() */
	@Override
	public boolean multipleTypes() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** 
	 * @see ProblemInterface#incrNCCCs(long) 
	 * @warning It doesn't make sense to count constraint checks in JaCoP, so this method doesnt' do anything. 
	 */
	@Override
	public void incrNCCCs(long incr) { }

	/** 
	 * @see ProblemInterface#getNCCCs() 
	 * @note Not supported by JaCoP, always returns -1
	 */
	@Override
	public long getNCCCs() {
		return -1;
	}

	/** 
	 * @see ProblemInterface#setNCCCs(long) 
	 * @warning Does nothing
	 */
	@Override
	public void setNCCCs(long ncccs) { }

	/** @see DCOPProblemInterface#getExtVars() */
	@Override
	public Set<String> getExtVars() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#addVariable(java.lang.String, java.lang.String, java.lang.String) */
	@Override
	public boolean addVariable(String name, String owner, String domain) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see DCOPProblemInterface#addVariable(java.lang.String, java.lang.String, Addable[]) */
	@Override
	public boolean addVariable(String name, String owner, AddableInteger[] domain) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see DCOPProblemInterface#rescale(Addable, Addable) */
	@Override
	public void rescale(U multiply, U add) {

		for (int i = this.spaces.size() - 1; i >= 0; i--) 
			this.spaces.set(i, this.spaces.get(i).rescale(add, multiply));
	}

	/** @see DCOPProblemInterface#setDomain(java.lang.String, Addable[]) */
	@Override
	public void setDomain(String var, AddableInteger[] dom) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see DCOPProblemInterface#setOwner(java.lang.String, java.lang.String) */
	@Override
	public boolean setOwner(String var, String owner) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see DCOPProblemInterface#getAnonymNeighborhoods() */
	@Override
	public Map<String, Set<String>> getAnonymNeighborhoods() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#getAnonymNeighborhoods(java.lang.String) */
	@Override
	public Map<String, Set<String>> getAnonymNeighborhoods(String agent) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#getProbabilitySpaces() */
	@Override
	public List<? extends UtilitySolutionSpace<AddableInteger, U>> getProbabilitySpaces() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#getProbabilitySpaces(java.lang.String) */
	@Override
	public List<? extends UtilitySolutionSpace<AddableInteger, U>> getProbabilitySpaces(
			String var) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#setProbSpace(java.lang.String, java.util.Map) */
	@Override
	public void setProbSpace(String var, Map<AddableInteger, Double> prob) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see DCOPProblemInterface#removeSpace(java.lang.String) */
	@Override
	public boolean removeSpace(String name) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see DCOPProblemInterface#addSolutionSpace(UtilitySolutionSpace) */
	@Override
	public boolean addSolutionSpace(UtilitySolutionSpace<AddableInteger, U> space) {
		
		if (this.hasConstraint (space.getName())) 
			return false;
		
		if (space instanceof JaCoPutilSpace) {
			super.spaces.add(space);
			return true;
		}
		
		// Retrieve the variables
		final int nbrVars = space.getNumberOfVariables();
		IntVarCloneable[] intVars = new IntVarCloneable [nbrVars + 1]; // the last variable is the utility variable
		int i = 0;
		for (String varName : space.getVariables()) {
			IntVarCloneable intVar = this.vars.get(varName);
			assert intVar != null : "Variable `" + varName + "' not found in the list of variables: " + this.vars;
			intVars[i++] = intVar;
		}
		
		SmallDenseDomain utilDom = new SmallDenseDomain ();
		
		// Iterate through the feasible tuples
		ArrayList<int[]> tuples = new ArrayList<int[]> ();
		SparseIterator<AddableInteger, U> iter = space.sparseIter(space.getVariables());
		AddableInteger[] sol = null;
		int util;
		while ((sol = iter.nextSolution()) != null) {
			
			int[] tuple = new int [nbrVars + 1];
			for (i = nbrVars - 1; i >= 0; i--) 
				tuple[i] = sol[i].intValue();
			util = tuple[nbrVars] = iter.getCurrentUtility().intValue();
			utilDom.addDom(new SmallDenseDomain (util, util));
			tuples.add(tuple);
		}
		
		// Create the util variable
		StoreCloneable store = new StoreCloneable ();
		IntVarCloneable objVar = new IntVarCloneable (store, "util_" + space.getName(), utilDom);
		intVars[nbrVars] = objVar;
		this.addSoftConstraint(new ExtensionalSupportHypercube (store, intVars, tuples.toArray(new int [tuples.size()][nbrVars + 1])), objVar, true);
				
		return true;
	}

	/** @see DCOPProblemInterface#getUtility(java.util.Map, boolean) */
	@SuppressWarnings("unchecked")
	@Override
	public UtilitySolutionSpace<AddableInteger, U> getUtility(
			Map<String, AddableInteger> assignments, boolean withAnonymVars) {
		
		// Join all spaces
		List<? extends UtilitySolutionSpace<AddableInteger, U>> spaces = this.getSolutionSpaces(withAnonymVars);
		UtilitySolutionSpace<AddableInteger, U> join;
		switch (spaces.size()) {
		case 0:
			return new ScalarHypercube<AddableInteger, U> (this.getZeroUtility(), this.getInfeasibleUtil(), AddableInteger[].class);
		case 1:
			join = spaces.get(0);
			break;
		default:
			join = spaces.remove(0).join(spaces.toArray(new UtilitySolutionSpace [spaces.size() - 1]));
		}
		
		// Convert the assignment map to two arrays
		final int assSize = assignments.size();
		String[] vars = new String [assSize];
		AddableInteger[] values = new AddableInteger [assSize];
		int i = 0;
		for (Map.Entry<String, AddableInteger> entry : assignments.entrySet()) {
			vars[i] = entry.getKey();
			values[i++] = entry.getValue();
		}
		
		return join.slice(vars, values);
	}

	/** @see DCOPProblemInterface#getExpectedUtility(java.util.Map) */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> getExpectedUtility(
			Map<String, AddableInteger> assignments) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#getParamUtility(java.util.Map) */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> getParamUtility(
			Map<String[], BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>>> assignments) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see DCOPProblemInterface#getSubProblem(java.lang.String) */
	@Override
	public JaCoPproblem<U> getSubProblem(String agent) {
		
		assert agent != null : "Null agent";
		
		JaCoPproblem<U> out = new JaCoPproblem<U> (this.maximize, this.publicAgents, this.mpc, this.extendedRandNeighborhoods);
		out.setAgent(agent);
		out.setUtilClass(super.utilClass);
		
		// Create the agents
		out.agents.add(agent);
		if (this.mpc || this.publicAgents) // the agent is supposed to know all the agents
			out.agents.addAll(this.getAgents());
		
		// Create the variables
		HashSet<String> outVars = new HashSet<String> ();
		for (String var : this.getVariables(agent)) 
			outVars.add(var);
		HashSet<String> relevantVars = new HashSet<String> (outVars); // internal variables and relevant external variables
		
		// In MPC mode, all variables are public
		if (this.mpc) {
			outVars.addAll(this.getVariables()); // all variables with a known owner
			outVars.addAll(this.getVariables(null)); // all variables without an owner
		}
		
		// Go through the list of constraints several times until 
		// we have identified all variables that should be known to this agent
		HashSet<String> constNames = new HashSet<String> ();
		HashMap< String, HashSet<String> > varScopes = new HashMap< String, HashSet<String> > ();
		int nbrVars;
		do {
			nbrVars = relevantVars.size();
			
			// Go through the list of all constraints in the overall problem
			for (UtilitySolutionSpace<AddableInteger, U> constraint : super.spaces) {
				
				// Skip this constraint if it has already been added
				String constName = constraint.getName();
				if (constNames.contains(constName)) 
					continue;

				// Get the list of variables in the scope of the constraint
				HashSet<String> scope = new HashSet<String> (Arrays.asList(constraint.getVariables()));
				
				// Check if the agent is not supposed to know the constraint
				String constOwner = constraint.getOwner();
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

				// If any of the variables in the scope is owned by this agent, add the constraint to the list of constraints
				final boolean knownConst = "PUBLIC".equals(constOwner) || agent.equals(constOwner);
				for (String var : scope) {
					if (knownConst || relevantVars.contains(var)) {
						
						out.spaces.add(constraint.clone());
						constNames.add(constName);
						
						// Add all variables in the scope to the list of variables known to this agent
						outVars.addAll(scope);
						for (String varName : scope) 
							if (this.getOwner(varName) == null) 
								relevantVars.add(varName);
						
						break;
					}
				}
			}
		} while (nbrVars != relevantVars.size()); // loop as long as another variable has been added to the list of known variables

		// Add the agents that own constraints over shared variables and my own variables
		for (UtilitySolutionSpace<AddableInteger, U> space : this.spaces) {
			
			String[] scope = space.getVariables();
			
			// Check whether the constraint owner should be known to the agent because the constraint scope involves a variable they share
			String constOwner = space.getOwner();
			if (! "PUBLIC".equals(constOwner) && constOwner != null && ! constOwner.equals(agent)) {
				for (String var : scope) {
					if (relevantVars.contains(var)) { // skip random variables and unknown variables
						String varOwner = this.getOwner(var);
						if (varOwner == null || varOwner.equals(agent)) { // the variable is shared or owned by this agent
							out.agents.add(constOwner);
							break;
						}
					}
				}
			}
		}
		
		// Add all variables known to this agent
		for (String var : outVars) {
			String owner = this.getOwner(var);
			out.addVariable(this.vars.get(var), owner);
			
			// Check the owner of this variable
			if (owner != null) 
				out.agents.add(owner);
			else { // shared variable; set its agent scope
				HashSet<String> varScope = varScopes.get(var);
				if (varScope != null) 
					this.setVarScope(var, varScope);
			}
		}
		
		return out;
	}

	/** @see AbstractProblem#setUtilClass(java.lang.Class) */
	@Override
	public void setUtilClass(Class<U> utilClass) {
		super.setUtilClass(utilClass);
		this.zeroUtil = super.getZeroUtility();
	}

	/** @see DCOPProblemInterface#ground(java.lang.String, Addable) */
	@Override
	public void ground(String varName, AddableInteger val) {
		
		IntVarCloneable var = this.vars.get(varName);
		assert var != null : "Unknown variable: " + varName;
		
		this.addConstraint(new XeqCCloneable (var, val.intValue()));
	}

	/** @see DCOPProblemInterface#getProbabilitySpacePerRandVar() */
	@Override
	public Map<String, ? extends UtilitySolutionSpace<AddableInteger, U>> getProbabilitySpacePerRandVar() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

}
