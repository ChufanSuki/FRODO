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

package frodo2.algorithms;

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

import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.AddableReal;
import frodo2.solutionSpaces.BasicUtilitySolutionSpace;
import frodo2.solutionSpaces.DCOPProblemInterface;
import frodo2.solutionSpaces.ProblemInterface;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.hypercube.Hypercube;
import frodo2.solutionSpaces.hypercube.ScalarHypercube;

/** A ProblemInterface that does not require the use of the XCSP format
 * @author Thomas Leaute
 * @param <V> the class used for variable values
 * @param <U> the class used for utility values
 * @todo Add support for NCCCs
 */
public class Problem < V extends Addable<V>, U extends Addable<U> > extends AbstractProblem <V, U> {
	
	/** Used for serialization */
	private static final long serialVersionUID = -7670751554969143041L;

	/** The NCCC count */
	private long ncccCount;
	
	/** For each random variable, its probability space */
	private HashMap< String, UtilitySolutionSpace<V, U> > probSpaces = new HashMap< String, UtilitySolutionSpace<V, U> > ();

	/** The class of variable values */
	@SuppressWarnings("unchecked")
	private Class<V> domClass = (Class<V>) AddableInteger.class;

	/** Constructor
	 * @param maximize 	Whether this is a maximization or a minimization problem
	 */
	public Problem (boolean maximize) {
		this(maximize, false, false, false);
	}
	
	/** Constructor
	 * @param maximize 						Whether this is a maximization or a minimization problem
	 * @param publicAgents 					Whether each agent knows the identities of all agents
	 * @param mpc 							Whether to behave in MPC mode (each agent knows the identities of all agents and knows all variables)
	 * @param extendedRandNeighborhoods 	Whether neighborhood relationships between decision variables are extended through random variables
	 */
	public Problem (boolean maximize, boolean publicAgents, boolean mpc, boolean extendedRandNeighborhoods) {
		super(maximize, publicAgents, mpc, extendedRandNeighborhoods);
	}
	
	/** Constructor for a minimization problem
	 * @param agentName 	the name of the agent owning this subproblem
	 * @param agents 		the agents
	 * @param owners 		for each variable, the name of its owner agent
	 * @param domains 		the domain of each variable
	 * @param randVars 		the random variables
	 * @param spaces 		the list of solution spaces
	 * @param probSpaces 	the probability space for each random variable
	 * @param varScopes 	the variable scopes
	 * @param domClass 		tbe class of variable values
	 * @param utilClass 	the utility class
	 */
	public Problem (String agentName, Set<String> agents, Map<String, String> owners, Map<String, V[]> domains, Set<String> randVars, 
			List< ? extends UtilitySolutionSpace<V, U> > spaces, Map< String, ? extends UtilitySolutionSpace<V, U> > probSpaces, 
			Map<String, Set<String>> varScopes, Class<V> domClass, Class<U> utilClass) {
		this(agentName, agents, owners, domains, randVars, spaces, probSpaces, varScopes, domClass, utilClass, false);
	}
	
	/** Constructor 
	 * @param agentName 	the name of the agent owning this subproblem
	 * @param agents 		the agents
	 * @param owners 		for each variable, the name of its owner agent
	 * @param domains 		the domain of each variable
	 * @param randVars 		the random variables
	 * @param spaces 		the list of solution spaces
	 * @param probSpaces 	the probability space for each random variable
	 * @param varScopes 	the variable scopes
	 * @param domClass 		tbe class of variable values
	 * @param utilClass 	the utility class
	 * @param maximize 		whether this is a maximization or a minimization problem
	 */
	public Problem (String agentName, Set<String> agents, Map<String, String> owners, Map<String, V[]> domains, Set<String> randVars, 
			List< ? extends UtilitySolutionSpace<V, U> > spaces, Map< String, ? extends UtilitySolutionSpace<V, U> > probSpaces, 
			Map<String, Set<String>> varScopes, Class<V> domClass, Class<U> utilClass, boolean maximize) {
		this(maximize);
		this.reset(agentName, agents, owners, domains, randVars, spaces, probSpaces, varScopes, domClass, utilClass, maximize);
	}
	
	/** @see AbstractProblem#toString() */
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder(super.toString());
		
		builder.append("\n\t probSpaces = ").append(this.probSpaces);
		
		return builder.toString();
	}
	
	/** @see DCOPProblemInterface#reset(ProblemInterface) */
	public void reset(ProblemInterface<V, U> newProblem) {
		
		if (newProblem instanceof Problem) {
			
			Problem<V, U> prob = (Problem<V, U>) newProblem;
			this.reset(prob.agentName, prob.agents, prob.owners, prob.domains, prob.randVars, prob.spaces, prob.probSpaces, 
					prob.varScopes, prob.domClass, prob.utilClass, prob.maximize);
			
		} else 
			System.err.println("Unknown problem class: " + newProblem.getClass());
		
	}
	
	/** Resets the problem 
	 * @param agentName 	the name of the agent owning this subproblem
	 * @param agents 		the agents
	 * @param owners 		for each variable, the name of its owner agent
	 * @param domains 		the domain of each variable
	 * @param randVars 		the random variables
	 * @param spaces 		the list of solution spaces
	 * @param probSpaces 	the probability space for each random variable
	 * @param varScopes 	the variable scopes
	 * @param domClass 		tbe class of variable values
	 * @param utilClass 	the utility class
	 * @param maximize 		whether this is a maximization or a minimization problem
	 */
	public void reset (String agentName, Set<String> agents, Map<String, String> owners, Map<String, V[]> domains, Set<String> randVars, 
			List< ? extends UtilitySolutionSpace<V, U> > spaces, Map< String, ? extends UtilitySolutionSpace<V, U> > probSpaces, 
			Map<String, Set<String>> varScopes, Class<V> domClass, Class<U> utilClass, boolean maximize) {
		this.agentName = agentName;
		this.agents = new HashSet<String> (agents);
		this.owners = new HashMap<String, String> (owners);
		
		this.domains = new HashMap<String, V[]> (domains.size());
		for (Map.Entry<String, V[]> entry : domains.entrySet()) 
			this.domains.put(entry.getKey(), entry.getValue().clone());
		
		this.randVars = new HashSet<String> (randVars);
		this.spaces = new ArrayList< UtilitySolutionSpace<V, U> > (spaces);
		this.probSpaces = new HashMap< String, UtilitySolutionSpace<V, U> > (probSpaces);
		
		this.varScopes = new HashMap< String, Set<String> > (varScopes.size());
		for (Map.Entry< String, Set<String> > entry : varScopes.entrySet()) 
			this.varScopes.put(entry.getKey(), new HashSet<String> (entry.getValue()));
		
		this.domClass = domClass;
		this.utilClass = utilClass;
		this.maximize = maximize;
	}
	
	/** @see ProblemInterface#setDomClass(java.lang.Class) */
	public void setDomClass(Class<V> domClass) {
		this.domClass = domClass;
	}
	
	/** @see ProblemInterface#getDomClass() */
	@Override
	public Class<V> getDomClass() {
		return this.domClass;
	}
	
	/** Sets the name of the agent
	 * @param agent 	the name of the agent
	 */
	public void setAgent (String agent) {
		this.agentName = agent;
	}

	/** @see DCOPProblemInterface#getExtVars() */
	public Set<String> getExtVars() {
		
		HashSet<String> out = new HashSet<String> ();
		
		if (this.agentName == null) 
			return out;

		for (Map.Entry<String, String> entry : this.owners.entrySet()) {
			String owner = entry.getValue();
			if (owner != null && ! this.agentName.equals(owner)) 
				out.add(entry.getKey());
		}
		
		return out;
	}

	/** @see DCOPProblemInterface#addVariable(java.lang.String, java.lang.String, java.lang.String) */
	public boolean addVariable(String name, String owner, String domain) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see DCOPProblemInterface#addVariable(java.lang.String, java.lang.String, V[]) */
	public boolean addVariable(String name, String owner, V[] domain) {
		
		// Check if a variable with the same name already exists
		if (this.domains.containsKey(name)) 
			return false;
		
		this.owners.put(name, owner);
		if (owner != null) 
			this.agents.add(owner);
		this.domains.put(name, domain.clone());
		
		return true;
	}
	
	/** Adds a random variable
	 * @param name 		the name of the variable
	 * @param domain 	the domain of the variable
	 * @return true if the variable was added; false if it already exists
	 */
	public boolean addRandomVar (String name, V[] domain) {
		
		if (this.randVars.contains(name)) 
			return false; 
		
		this.randVars.add(name);
		this.domains.put(name, domain.clone());
		
		return true;
	}

	/** @see DCOPProblemInterface#setOwner(java.lang.String, java.lang.String) */
	public boolean setOwner(String var, String owner) {
		
		if (! this.owners.containsKey(var)) 
			return false; // unknown variable
		
		this.owners.put(var, owner);
		if (owner != null) 
			this.agents.add(owner);
		return true;
	}
	
	/** @see DCOPProblemInterface#setDomain(java.lang.String, V[]) */
	public void setDomain(String var, V[] dom) {

		// Return immediately if no samples are provided
		if (dom.length == 0) 
			return;

		// If dom contains several times the same value, it must be reduced.
		ArrayList<V> domReduced = new ArrayList<V> (dom.length);
		HashMap<V, Double> weights = new HashMap<V, Double> ();
		double weightIncr = 1.0 / dom.length;
		for (V val : dom) {
			Double w = weights.get(val);
			if (w != null) { // redundant value
				weights.put(val, w + weightIncr);
			} else { // first time we see this value
				weights.put(val, weightIncr);
				domReduced.add(val);
			}
		}
		
		this.setDomain(var, domReduced, weights);
	}

	/** Sets the domain of a variable
	 * @param var 		the variable
	 * @param domain 	its new domain
	 * @param weights 	normalized weights for each value in the new domain (used only if the variable is random)
	 */
	@SuppressWarnings("unchecked")
	private void setDomain(String var, ArrayList<V> domain, Map<V, Double> weights) {

		int nbrVals = domain.size();
		V[] dom = domain.toArray((V[]) Array.newInstance(domain.get(0).getClass(), nbrVals));
		Arrays.sort(dom);

		// If the variable is unknown, add it to the problem, treating it as a random variable
		boolean isRandom;
		if (! super.domains.containsKey(var)) {
			this.randVars.add(var);
			isRandom = true;
		} else 
			isRandom = this.randVars.contains(var);

		// If var is a random variable, its probability law must be updated accordingly. 
		if (isRandom) {

			// Create a new probability space
			String[] vars = new String[] { var };

			V[][] doms = (V[][]) Array.newInstance(dom.getClass(), 1);
			doms[0] = dom;

			AddableReal[] utils = new AddableReal [nbrVals];
			for (int i = 0; i < nbrVals; i++) 
				utils[i] = new AddableReal (weights.get(dom[i]));

			this.probSpaces.put(var, new Hypercube<V, U> (vars, doms, utils, null));
		}
		
		this.domains.put(var, dom);
	}
	
	/** @see DCOPProblemInterface#getProbabilitySpaces() */
	public List< UtilitySolutionSpace<V, U> > getProbabilitySpaces() {
		return new ArrayList< UtilitySolutionSpace<V, U> > (this.probSpaces.values());
	}

	/** @see DCOPProblemInterface#getProbabilitySpacePerRandVar() */
	@Override
	public Map< String, ? extends UtilitySolutionSpace<V, U> > getProbabilitySpacePerRandVar() {
		return Collections.unmodifiableMap(this.probSpaces);
	}

	/** @see DCOPProblemInterface#getProbabilitySpaces(java.lang.String) */
	public List< UtilitySolutionSpace<V, U> > getProbabilitySpaces(String var) {
		
		UtilitySolutionSpace<V, U> prob = this.probSpaces.get(var);
		
		if (prob != null) 
			return Arrays.asList(prob);
		else 
			return new ArrayList< UtilitySolutionSpace<V, U> > ();
	}
	
	/** @see DCOPProblemInterface#setProbSpace(java.lang.String, java.util.Map) */
	public void setProbSpace(String var, Map<V, Double> prob) {

		// Extract the variable's domain while computing the sum of the weights
		ArrayList<V> domain = new ArrayList<V> (prob.size());
		Double norm = 0.0;
		for (Map.Entry<V, Double> entry : prob.entrySet()) {
			domain.add(entry.getKey());
			norm += entry.getValue();
		}

		// Renormalize the weights
		for (Map.Entry<V, Double> entry : prob.entrySet()) {
			entry.setValue(entry.getValue() / norm);
		}

		this.setDomain(var, domain, prob);
	}

	/** @see DCOPProblemInterface#removeSpace(java.lang.String) */
	public boolean removeSpace(String name) {
		
		assert name != null : "The provided name is null";
		
		for (Iterator< ? extends UtilitySolutionSpace<V, U> > iter = this.spaces.iterator(); iter.hasNext(); ) {
			if (name.equals(iter.next().getName())) {
				iter.remove();
				return true;
			}
		}
		
		return false;
	}
	
	/** @see DCOPProblemInterface#addSolutionSpace(UtilitySolutionSpace) */
	public boolean addSolutionSpace(UtilitySolutionSpace<V, U> space) {
		this.spaces.add(space);
		return true;
	}
	
	/** Adds a probability space for a given variable
	 * @param randVar 	the name of the random variable
	 * @param prob 		the probabiliy space
	 */
	public void addProbabilitySpace (String randVar, UtilitySolutionSpace<V, U> prob) {
		this.probSpaces.put(randVar, (UtilitySolutionSpace<V, U>) prob);
	}

	/** @see DCOPProblemInterface#incrNCCCs(long) */
	public void incrNCCCs (long incr) {
		this.ncccCount += incr;
	}
	
	/** @see DCOPProblemInterface#setNCCCs(long) */
	public void setNCCCs (long ncccs) {
		this.ncccCount = ncccs;
	}
	
	/** @see DCOPProblemInterface#getNCCCs() */
	public long getNCCCs () {
		return this.ncccCount;
	}
	
	/** @see DCOPProblemInterface#rescale(Addable, Addable) */
	public void rescale(U multiply, U add) {
		
		for (UtilitySolutionSpace<V, U> space : this.spaces) 
			for (UtilitySolutionSpace.Iterator<V, U> iter = space.iterator(); iter.hasNext(); ) 
				iter.setCurrentUtility(iter.nextUtility().multiply(multiply).add(add));
	}
	
	/** 
	 * @see DCOPProblemInterface#getUtility(Map, boolean) 
	 * @todo Test this method 
	 */
	@SuppressWarnings("unchecked")
	public UtilitySolutionSpace<V, U> getUtility(Map<String, V> assignments, boolean withAnonymVars) {
		
		Class<? extends V[]> classOfDom = (Class<? extends V[]>) Array.newInstance(this.domClass, 0).getClass();
		U zero = this.getZeroUtility();
		UtilitySolutionSpace<V, U> output = new ScalarHypercube<V, U> (zero, this.getInfeasibleUtil(), classOfDom);
		
		// Extract all hypercubes
		List< ? extends UtilitySolutionSpace<V, U> > hypercubes = this.getSolutionSpaces(withAnonymVars);
		
		// Go through the list of hypercubes
		for (UtilitySolutionSpace<V, U> hypercube : hypercubes) {
			
			// Slice the hypercube over the input assignments
			ArrayList<String> vars = new ArrayList<String> (hypercube.getNumberOfVariables());
			for (String var : hypercube.getVariables()) 
				if (assignments.containsKey(var)) 
					vars.add(var);
			int nbrVars = vars.size();
			V[] values = (V[]) Array.newInstance(classOfDom.getComponentType(), nbrVars);
			for (int i = 0; i < nbrVars; i++) 
				values[i] = assignments.get(vars.get(i));
			UtilitySolutionSpace<V, U> slice = hypercube.slice(vars.toArray(new String[nbrVars]), values);
			
			// Join the slice with the output
			output = output.join(slice);
		}
		
		return output;
	}
	
	/** @see DCOPProblemInterface#getExpectedUtility(Map) */
	public UtilitySolutionSpace<V, U> getExpectedUtility(Map<String, V> assignments) {

		// Compute the utility, as a function of the random variables
		UtilitySolutionSpace<V, U> util = (UtilitySolutionSpace<V, U>) this.getUtility(assignments, true);

		// Compute the expectation over the random variables 
		HashMap< String, UtilitySolutionSpace<V, U> > distributions = 
			new HashMap< String, UtilitySolutionSpace<V, U> > ();
		for (UtilitySolutionSpace<V, U> probSpace : this.getProbabilitySpaces()) 
			distributions.put(probSpace.getVariable(0), probSpace);
		if (! distributions.isEmpty()) 
			util = util.expectation(distributions);
		
		return util;
	}

	/** 
	 * @see DCOPProblemInterface#getParamUtility(java.util.Map) 
	 * @todo Test this method.
	 */
	public UtilitySolutionSpace<V, U> getParamUtility (Map< String[], BasicUtilitySolutionSpace< V, ArrayList<V> > > assignments) {

		@SuppressWarnings("unchecked")
		Class<? extends V[]> classOfDom = (Class<? extends V[]>) Array.newInstance(assignments.values().iterator().next().getUtility(0).get(0).getClass(), 0).getClass();
		U zero = this.getZeroUtility();
		UtilitySolutionSpace<V, U> output = new ScalarHypercube<V, U> (zero, this.getInfeasibleUtil(), classOfDom);

		// Go through the list of spaces
		for (UtilitySolutionSpace<V, U> space : this.spaces) {

			// Compose the space with each input assignment
			UtilitySolutionSpace<V, U> composition = space;
			for (Map.Entry< String[], BasicUtilitySolutionSpace< V, ArrayList<V> > > entry : assignments.entrySet()) 
				composition = composition.compose(entry.getKey(), entry.getValue());

			// Join the composition with the output
			output = output.join(composition);
		}

		return output;
	}

	/** 
	 * @see ProblemInterface#getSubProblem(java.lang.String) 
	 * @todo Test this method. 
	 */
	public Problem<V, U> getSubProblem(String agent) {
		
		Problem<V, U> out = new Problem<V, U> (this.maximize, publicAgents, mpc, extendedRandNeighborhoods);
		out.setDomClass(domClass);
		out.setUtilClass(utilClass);

		// Get the set of variables owned by the agent
		HashSet<String> vars = new HashSet<String> (this.getVariables(agent)); // will eventually contain internal variables and relevant external variables	

		// Create the list of agents
		out.setAgent(agent);
		HashSet<String> knownAgents = new HashSet<String> ();
		knownAgents.add(agent);
		if (this.mpc || this.publicAgents) // the agent is supposed to know all the agents
			knownAgents.addAll(this.getAgents());

		// In MPC mode, all variables are public
		if (this.mpc) 
			vars.addAll(this.getVariables());

		// Gather the constraints and probability spaces
		HashSet< UtilitySolutionSpace<V, U> > outSpaces = new HashSet< UtilitySolutionSpace<V, U> > ();
		HashSet< UtilitySolutionSpace<V, U> > outProbs = new HashSet< UtilitySolutionSpace<V, U> > ();

		// Go through the list of constraints several times until we are sure we have identified all variables that should be known to this agent
		HashMap< String, HashSet<String> > varScopes = new HashMap< String, HashSet<String> > ();
		int nbrVars;
		do {
			nbrVars = vars.size();

			// Go through the list of all constraints in the overall problem
			for (UtilitySolutionSpace<V, U>  space : super.spaces) {

				// Skip this constraint if it has already been added
				if (outSpaces.contains(space)) 
					continue;

				// Get the list of variables in the scope of the constraint
				HashSet<String> scope = new HashSet<String> (Arrays.asList(space.getVariables()));

				// Check if the agent is not supposed to know the constraint
				String constOwner = space.getOwner();
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
					if (knownConst || vars.contains(var)) {

						// Skip this variable if it is apparently not necessary for the agent to know this constraint
						if (!this.extendedRandNeighborhoods && this.isRandom(var))
							continue;
						outSpaces.add(space);
						UtilitySolutionSpace<V, U> clone = space.clone();
						if (space.countsCCs()) 
							clone.setProblem(out);
						out.addSolutionSpace(clone);

						// Add all variables in the scope to the list of variables known to this agent
						for (String var2 : space.getVariables()) {
							String owner2 = this.getOwner(var2);
							out.addVariable(var2, owner2, space.getDomain(var2));
							if (owner2 == null) 
								vars.add(var2);
						}

						break;
					}
				}
			}

			// Go through the list of all probability spaces in the overall problem
			for (UtilitySolutionSpace<V, U> space : this.probSpaces.values()) {

				// Skip this probability space if it has already been added
				if (outProbs.contains(space)) 
					continue;

				// Get the list of variables in the scope of the probability space
				HashSet<String> scope = new HashSet<String> (Arrays.asList(space.getVariables()));

				// Check if the agent is not supposed to know the probability space
				String constOwner = space.getOwner();
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
				// add the probability space to the list of probability spaces
				final boolean knownConst = "PUBLIC".equals(constOwner) || agent.equals(constOwner);
				for (String var : scope) {
					if (knownConst || vars.contains(var)) {

						// Skip this variable if it is apparently not necessary for the agent to know this probability space
						if (! this.isRandom(var)) 
							continue;
						outProbs.add(space);
						out.probSpaces.put(var, space.clone());

						// Add all variables in the scope to the list of variables known to this agent
						for (String var2 : space.getVariables()) {
							String owner2 = this.getOwner(var2);
							out.addVariable(var2, owner2, space.getDomain(var2));
							if (owner2 == null) 
								vars.add(var2);
						}

						break;
					}
				}
			}
			
		} while (nbrVars != vars.size()); // loop as long as another variable has been added to the list of known variables

		// Add the agents that own constraints over shared variables and my own variables
		for (UtilitySolutionSpace<V, U> space : super.spaces) {
			
			// Get the list of variables in the scope of the constraint
			HashSet<String> scope = new HashSet<String> (Arrays.asList(space.getVariables()));
			
			// Check whether the constraint owner should be known to the agent because the constraint scope involves a variable they share
			String constOwner = space.getOwner();
			if (! "PUBLIC".equals(constOwner) && constOwner != null && ! constOwner.equals(agent)) {
				for (String var : scope) {
					if (! this.isRandom(var) && vars.contains(var)) { // skip random variables and unknown variables
						String varOwner = this.getOwner(var);
						if (varOwner == null || varOwner.equals(agent)) { // the variable is shared or owned by this agent
							knownAgents.add(constOwner);
							break;
						}
					}
				}
			}
		}

		// Add all variables known to this agent
		for (String var : vars) {
			
			if (this.isRandom(var)) {
				out.addRandomVar(var, this.getDomain(var));
				continue;
			}
			
			String owner = this.getOwner(var);
			out.addVariable(var, owner, this.getDomain(var));
			
			// Check the owner of this variable
			if (owner != null) 
				knownAgents.add(owner);
			else { // shared variable; set its agent scope
				HashSet<String> varScope = varScopes.get(var);
				if (varScope != null) 
					out.varScopes.put(var, new HashSet<String> (varScope));
			}
		}
		
		// Fill in the list of agents
		out.agents.addAll(knownAgents);

		return out;
	}

	/** 
	 * @see frodo2.solutionSpaces.ProblemInterface#multipleTypes()
	 */
	public boolean multipleTypes() {
		return false;
	}

	/** @see DCOPProblemInterface#ground(java.lang.String, Addable) */
	@SuppressWarnings("unchecked")
	@Override
	public void ground(String var, V val) {
		
		// Construct the domains
		V[] dom = this.getDomain(var);
		V[][] doms = (V[][]) Array.newInstance(dom.getClass(), 1);
		doms[0] = dom;

		// Construct the utilities
		U zero = this.getZeroUtility();
		U[] utils = (U[]) Array.newInstance(zero.getClass(), dom.length);
		U inf = (this.maximize() ? this.getMinInfUtility() : this.getPlusInfUtility());
		Arrays.fill(utils, inf);
		for (int i = dom.length - 1; i >= 0; i--) {
			if (dom[i].equals(val)) {
				utils[i] = zero;
				break;
			}
		}

		// Add the constraint
		Hypercube<V, U> equality = new Hypercube<V, U> (new String[] {var}, doms, utils, inf, this);
		equality.setName(var + "=" + val);
		this.addSolutionSpace(equality);
	}
	
}
