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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.DCOPProblemInterface;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.hypercube.Hypercube;

/** An abstract problem instance
 * @author Thomas Leaute
 * @param <V> the class used for variable values
 * @param <U> the class used for utility values
 */
public abstract class AbstractProblem < V extends Addable<V>, U extends Addable<U> > implements DCOPProblemInterface<V, U> {

	/** Used for serialization */
	private static final long serialVersionUID = -1050483109528194609L;

	/** Whether this is a maximization or a minimization problem */
	protected boolean maximize;
	
	/** For each variable, the name of its owner agent */
	protected Map<String, String> owners;
	
	/** The random variables */
	protected Set<String> randVars;
	
	/** The set of agents */
	protected HashSet<String> agents = new HashSet<String> ();
	
	/** The list of solution spaces */
	protected List< UtilitySolutionSpace<V, U> > spaces;

	/** The domain of each variable */
	protected Map<String, V[]> domains;
	
	/** The class used for utility values */
	@SuppressWarnings("unchecked")
	protected Class<U> utilClass = (Class<U>) AddableInteger.class;
	
	/** The name of the agent owning this subproblem */
	protected String agentName;

	/** Whether each agent knows the identities of all agents */
	protected final boolean publicAgents;
	
	/** If \c true, neighborhood relationships between decision variables are extended through random variables. 
	 * 
	 * In other words, for a given decision variable x, its neighborhood consists of: <br>
	 * - the decision variables that share constraints with x (as usual), <br>
	 * - the decision variables that are direct neighbors of any random variable that can be reached from x by a path that involves only random variables. 
	 */
	protected final boolean extendedRandNeighborhoods;
	
	/** Whether to behave in MPC mode. 
	 * 
	 * In MPC mode: 
	 * - each agent knows the identities of all agents; 
	 * - all variables are known to all agents.
	 */
	protected final boolean mpc;

	/** For each shared variable, the names of the agents in its scope */
	protected HashMap<String, Set<String>> varScopes = new HashMap<String, Set<String>> ();

	/** Constructor */
	protected AbstractProblem () {
		this(false, false, false, false);
	}
	
	/** Constructor
	 * @param maximize 						Whether this is a maximization or a minimization problem
	 * @param publicAgents 					Whether each agent knows the identities of all agents
	 * @param mpc 							Whether to behave in MPC mode (each agent knows the identities of all agents and knows all variables)
	 * @param extendedRandNeighborhoods 	Whether neighborhood relationships between decision variables are extended through random variables
	 */
	protected AbstractProblem (boolean maximize, boolean publicAgents, boolean mpc, boolean extendedRandNeighborhoods) {
		this.maximize = maximize;
		this.publicAgents = publicAgents;
		this.mpc = mpc;
		this.extendedRandNeighborhoods = extendedRandNeighborhoods;
		this.owners = new HashMap<String, String> ();
		this.randVars = new HashSet<String> ();
		this.spaces = new ArrayList< UtilitySolutionSpace<V, U> > ();
		this.domains = new HashMap<String, V[]> ();
	}
	
	/** @see java.lang.Object#toString() */
	@Override
	public String toString () {
		
		StringBuilder builder = new StringBuilder (this.getClass().getSimpleName());
		
		if (this.agentName != null) 
			builder.append("\n\t agent: ").append(this.agentName);
		
		builder.append("\n\t agents: ").append(this.agents);
		
		builder.append("\n\t variables: ");
		for (Map.Entry<String, V[]> entry : this.domains.entrySet()) 
			builder.append("\n\t\t ").append(entry.getKey()).append("\t").append(Arrays.toString(entry.getValue()));

		builder.append("\n\t owners: ");
		for (Map.Entry<String, String> entry : this.owners.entrySet()) {
			builder.append("\n\t\t ").append(entry.getKey());
			if (entry.getValue() != null) 
				builder.append("\t is owned by \t").append(entry.getValue());
		}
		
		builder.append("\n\t random variables: ").append(this.randVars);

		builder.append("\n\t extendedRandNeighborhoods = ").append(this.extendedRandNeighborhoods);
		builder.append("\n\t maximize = ").append(this.maximize);
		builder.append("\n\t mpc = ").append(this.mpc);
		
		builder.append("\n\t publicAgents = ").append(this.publicAgents);

		builder.append("\n\t utilClass = ").append(this.utilClass.getSimpleName());
		
		for (Map.Entry< String, Set<String> > entry : this.varScopes.entrySet()) 
			builder.append("\n\t\t ").append(entry.getKey()).append("\t has scope \t").append(entry.getValue());
		
		builder.append("\n\t spaces = ").append(this.spaces);
		
		return builder.toString();
	}
	
	/** @see DCOPProblemInterface#getAgent() */
	public String getAgent() {
		return this.agentName;
	}
	
	/** @see DCOPProblemInterface#getVariables(java.lang.String) */
	public Set<String> getVariables (final String owner) {

		Set<String> out = new HashSet<String> ();

		for (String var : this.getVariables()) {
			String owner2 = this.getOwner(var);
			if (owner == null && owner2 == null 
					|| owner != null && owner.equals(owner2)) 
				out.add(var);
		}

		if (owner == null) // also add the random variables
			for (String randVar : this.randVars) 
				out.add(randVar);

		return out;
	}
	
	/** @see DCOPProblemInterface#getDomain(java.lang.String) */
	public V[] getDomain(String var) {
		
		V[] dom = this.domains.get(var);
		
		if (dom == null) 
			return null; 
		else 
			return dom.clone();
	}

	/** @see DCOPProblemInterface#getDomainSize(java.lang.String) */
	public int getDomainSize(String var) {
		
		V[] dom = this.getDomain(var);
		if (dom == null) 
			return -1;
		else 
			return dom.length;
	}

	/** @see DCOPProblemInterface#getNeighborhoods() */
	public Map<String, Set<String>> getNeighborhoods() {
		return this.getNeighborhoods(this.getAgent());
	}

	/** @see DCOPProblemInterface#getAnonymNeighborhoods() */
	public Map<String, Set<String>> getAnonymNeighborhoods() {
		return this.getAnonymNeighborhoods(this.getAgent());
	}

	/** @see DCOPProblemInterface#getAnonymNeighborhoods(java.lang.String) */
	@Override
	public Map<String, Set<String>> getAnonymNeighborhoods(String agent) {
		return this.getNeighborhoods(agent, true, true);
	}

	/** @see DCOPProblemInterface#getNeighborhoodSizes() */
	public Map<String, Integer> getNeighborhoodSizes() {
		return this.getNeighborhoodSizes(this.getAgent());
	}

	/** @see DCOPProblemInterface#getNeighborhoodSizes(java.lang.String) */
	@Override
	public Map<String, Integer> getNeighborhoodSizes(String agent) {

		Map<String, Integer> out = new HashMap<String, Integer> ();

		// Go through the list of neighbors of each of the agent's variables
		for (Map.Entry< String, ? extends Collection<String> > neighborhood : getNeighborhoods(agent).entrySet()) 
			out.put(neighborhood.getKey(), neighborhood.getValue().size());

		return out;
	}

	/** @see DCOPProblemInterface#getNbrNeighbors(java.lang.String) */
	public int getNbrNeighbors(String var) {
		return this.getNbrNeighbors(var, false);
		}
	
	/** @see DCOPProblemInterface#getNbrNeighbors(java.lang.String, boolean) */
	@Override
	public int getNbrNeighbors(String var, boolean withAnonymVars) {
		return this.getNeighborVars(var, withAnonymVars).size();
		}

	/** @see DCOPProblemInterface#getNeighborVars(java.lang.String) */
	public HashSet<String> getNeighborVars(String var) {
		return this.getNeighborVars(var, false);
	}
	
	/** @see DCOPProblemInterface#getNeighborVars(java.lang.String, boolean) */
	public HashSet<String> getNeighborVars(String var, boolean withAnonymVars) {

		HashSet<String> out = new HashSet<String> ();

		LinkedList<String> pending = new LinkedList<String> (); // variable(s) whose direct neighbors will be returned
		pending.add(var);
		HashSet<String> done = new HashSet<String> ();
		do {
			// Retrieve the next pending variable
			String var2 = pending.poll();
			if (! done.add(var2)) // we have already processed this variable
				continue;

			// Go through the list of constraint scopes
			for (UtilitySolutionSpace<V, U> space : this.spaces) {

				// Check if var2 is in the scope				
				String[] scope = space.getVariables().clone();
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >= 0) {

					// Go through the list of variables in the scope
					for (String neighbor : scope) {

						// Check if the neighbor doesn't have any owner agent
						if (null != this.getOwner(neighbor)) // non-null owner
							out.add(neighbor);

						else { // the neighbor is not owned by any agent

							// Add it to the list of neighbors if we are interested in neighbors without owners
							if (withAnonymVars) 
								out.add(neighbor);

							// Later look for its own neighbors if we want extended neighborhoods
							if (this.extendedRandNeighborhoods) 
								pending.add(neighbor);
						}
					}
				}
			}
		} while (! pending.isEmpty());

		// Remove the variable itself from its list of neighbors
		out.remove(var);

		return out;
	}
	
	/** @see DCOPProblemInterface#getNumberOfCoordinationConstraints() */
	public int getNumberOfCoordinationConstraints() {
		
		int count = 0;
		
		spaceLoop: for (UtilitySolutionSpace<V, U> space : this.spaces) {
			
			String[] vars = space.getVariables();
			
			if (vars.length <= 1) 
				continue;
			
			String firstAgent = null;
			for (String var : vars) {
				String agent = this.owners.get(var);
				
				if (agent == null) 
					continue;
				else if (firstAgent == null) 
					firstAgent = agent;
				else if (! firstAgent.equals(agent)) {
					count++;
					continue spaceLoop;
				}
			}
		}
		
		return count;
	}

	/** @see DCOPProblemInterface#getAgentNeighborhoods() */
	public Map< String, Set<String> > getAgentNeighborhoods () {
		return this.getAgentNeighborhoods(agentName);
	}

	/** @see DCOPProblemInterface#getAgentNeighborhoods(java.lang.String) */
	public Map< String, Set<String> > getAgentNeighborhoods (String agent) {
		
		Set<String> vars = this.getVariables();
		Map< String, Set<String> > out = new HashMap< String, Set<String> > (vars.size());
		for (String var : vars) {
			String owner = this.getOwner(var);
			if (agent == null || agent.equals(owner) || owner == null && ! this.isRandom(var)) 
				out.put(var, this.getAgentNeighbors(var));
		}

		return out;
	}

	/** Returns the neighboring agents of the input variable
	 * @param var 	the variable
	 * @return 		the variable's neighboring agents
	 */
	private HashSet<String> getAgentNeighbors (String var) {
		
		HashSet<String> out = new HashSet<String> ();
		
		LinkedList<String> pending = new LinkedList<String> (); // variable(s) whose direct agent neighbors will be returned
		pending.add(var);
		HashSet<String> done = new HashSet<String> ();
		do {
			// Retrieve the next pending variable
			String var2 = pending.poll();
			if (! done.add(var2)) // we have already processed this variable
				continue;
			
			// Go through the list of constraint scopes
			for (UtilitySolutionSpace<V, U> space : this.spaces) {

				// Check if var2 is in the scope
				String[] scope = space.getVariables().clone();
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >= 0) {

					// If the constraint has a specific owner, add it to the set of agents
					String consOwner = space.getOwner();
					if ("PUBLIC".equals(consOwner)) 
						consOwner = null;
					if (consOwner != null) 
						out.add(consOwner);
					
					// Go through the list of variables in the scope
					for (String neighbor : scope) {

						// Check if the neighbor is random
						if (! this.isRandom(neighbor)) { // not random
							String varOwner = this.getOwner(neighbor);
							if (varOwner != null) 
								out.add(varOwner);
						} else if (this.extendedRandNeighborhoods)
							pending.add(neighbor); // later look for this random neighbor's own neighbors
					}
				}
			}
		} while (! pending.isEmpty());
		
		// Add the variable's scope if present
		Set<String> scope = this.getScope(var);
		if (scope != null) 
			out.addAll(scope);

		// Remove the owner agent from the list of neighbors
		out.remove(this.getOwner(var));

		return out;
	}

	/** @see DCOPProblemInterface#getVarScopes() */
	@Override
	public Map< String, Set<String> > getVarScopes() {
		
		HashMap< String, Set<String> > out = new HashMap< String, Set<String> > (this.varScopes.size());
		
		for (Map.Entry<String, Set<String>> entry : this.varScopes.entrySet()) 
			out.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
		
		return out;
	}

	/** Returns the agent scope of the input anonymous variable
	 * @param var 	the variable
	 * @return the agent scope of the variable
	 */
	protected Set<String> getScope(String var) {
		
		Set<String> scope = this.varScopes.get(var);
		
		if (scope == null) 
			return null;
		else 
			return Collections.unmodifiableSet(scope);
	}

	/** Sets the name of the agent owning this subproblem
	 * @param name 	the name of the agent
	 */
	public void setAgent (String name) {
		this.agentName = name;
	}
	
	/** @see DCOPProblemInterface#getAgents() */
	public Set<String> getAgents() {
		return Collections.unmodifiableSet(this.agents);
	}

	/** @return -INF if we are maximizing, +INF if we are minimizing */
	protected U getInfeasibleUtil () {
		
		// Check whether we are minimizing or maximizing
		if (this.maximize) 
			return this.getMinInfUtility();
		else 
			return this.getPlusInfUtility();
	}

	/** @see DCOPProblemInterface#getUtilClass() */
	@Override
	public Class<U> getUtilClass() {
		return this.utilClass;
	}

	/** @see DCOPProblemInterface#getMinInfUtility() */
	public U getMinInfUtility() {
		try {
			return (U) utilClass.getConstructor().newInstance().getMinInfinity();
			
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
			System.err.println("Failed calling the nullary constructor for the class " + utilClass.getName() + " used for utility values");
			e.printStackTrace();
			return null;
		}
	}

	/** @see DCOPProblemInterface#getPlusInfUtility() */
	public U getPlusInfUtility() {
		try {
			return (U) utilClass.getConstructor().newInstance().getPlusInfinity();
			
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
			System.err.println("Failed calling the nullary constructor for the class " + utilClass.getName() + " used for utility values");
			e.printStackTrace();
			return null;
		}
	}

	/** @see DCOPProblemInterface#getNbrVars() */
	public int getNbrVars() {
		return this.getVariables().size();
	}
	
	/** @see DCOPProblemInterface#getNbrVars(java.lang.String) */
	public int getNbrVars (String agent) {
		
		int nbr = 0;
		for (Map.Entry<String, String> entry : this.owners.entrySet()) 
			if (agent == entry.getValue() // in case agent == null
				|| agent.equals(entry.getValue())) 
				nbr++;
		
		return nbr;
	}
	
	/** @see DCOPProblemInterface#getNbrIntVars() */
	public int getNbrIntVars() {
		return this.getMyVars().size();
	}

	/** @see DCOPProblemInterface#getOwners() */
	public Map<String, String> getOwners() {
		
		HashMap<String, String> out = new HashMap<String, String> (this.owners.size());
		
		// Skip null owners
		for (Map.Entry<String, String> entry : this.owners.entrySet()) 
			if (entry.getValue() != null) 
				out.put(entry.getKey(), entry.getValue());
		
		return out;
	}

	/** @see DCOPProblemInterface#getOwner(java.lang.String) */
	public String getOwner(String var) {
		return this.owners.get(var);
	}
	
	/** @see DCOPProblemInterface#isRandom(java.lang.String) */
	@Override
	public boolean isRandom(String var) {
		return this.randVars.contains(var);
	}

	/** Sets the agent scope of a shared variable
	 * @param var 		the variable
	 * @param scope 	the names of the agents in the variable's scope
	 */
	protected void setVarScope (String var, Set<String> scope) {
		assert this.getOwner(var) == null : "Setting the agent scope of a non-shared variable: " + var;
		this.varScopes.put(var, scope);
	}

	/** @see DCOPProblemInterface#getSolutionSpaces() */
	public List< ? extends UtilitySolutionSpace<V, U> > getSolutionSpaces() {
		return this.getSolutionSpaces(false);
	}

	/** @see DCOPProblemInterface#getSolutionSpaces(boolean) */
	public List< ? extends UtilitySolutionSpace<V, U> > getSolutionSpaces(boolean withAnonymVars) {
		return this.getSolutionSpaces((String) null, withAnonymVars, null);
	}

	/** @see DCOPProblemInterface#getSolutionSpaces(String, boolean) */
	public List< ? extends UtilitySolutionSpace<V, U> > getSolutionSpaces(String var, boolean withAnonymVars) {
		return this.getSolutionSpaces(var, withAnonymVars, null);
	}
	
	/** @see DCOPProblemInterface#getSolutionSpaces(java.lang.String, java.util.Set) */
	public List<? extends UtilitySolutionSpace<V, U>> getSolutionSpaces(String var, Set<String> forbiddenVars) {
		return this.getSolutionSpaces(var, false, forbiddenVars);
	}

	/** @see DCOPProblemInterface#getSolutionSpaces(java.lang.String, boolean, java.util.Set) */
	public List<? extends UtilitySolutionSpace<V, U>> getSolutionSpaces(String var, final boolean withAnonymVars, Set<String> forbiddenVars) {
		
		HashSet<String> vars = null;
		if(var != null) {
			vars = new HashSet<String>();
			vars.add(var);
		}
		return this.getSolutionSpaces(vars, withAnonymVars, forbiddenVars);
	}
	
	/** @see DCOPProblemInterface#getSolutionSpaces(java.util.Set, boolean, java.util.Set) */
	public List<? extends UtilitySolutionSpace<V, U>> getSolutionSpaces(Set<String> vars, boolean withAnonymVars, Set<String> forbiddenVars) {
		
		// Return null if not all domains are known yet
		for (V[] dom : this.domains.values()) 
			if (dom == null) 
				return null;
		
		// Get rid of all undesired spaces
		List< UtilitySolutionSpace<V, U> > out = new ArrayList< UtilitySolutionSpace<V, U> > ();
		spaceLoop: for (UtilitySolutionSpace<V, U> space : this.spaces) {
			
			// Skip this space if it does not include any of the input variables
			if (vars != null && Collections.disjoint(vars, Arrays.asList(space.getVariables()))) 
				continue;
			
			// Skip this space if it involves a variable with unknown owner and if we don't want such variables
			if (! withAnonymVars) 
				for (String var2 : space.getVariables()) 
					if (this.owners.get(var2) == null) 
						continue spaceLoop;
			
			// Skip this space if it involves any of the forbidden variables
			if (forbiddenVars != null) 
				for (String var2: space.getVariables()) 
					if (forbiddenVars.contains(var2)) 
						continue spaceLoop;
			
			out.add(space);
		}		
		return out;
	}
	
	/** 
	 * @see DCOPProblemInterface#getUtility(java.util.Map) 
	 * @todo Test this method. 
	 */
	public UtilitySolutionSpace<V, U> getUtility (Map<String, V> assignments) {
		return this.getUtility(assignments, false);
	}
	
	/** @see DCOPProblemInterface#getVariables() */
	public Set<String> getVariables() {
		
		HashSet<String> vars = new HashSet<String> (this.owners.size());

		for (Map.Entry<String, String> entry : this.owners.entrySet()) 
			if (entry.getValue() != null) 
				vars.add(entry.getKey());
		
		return vars;
	}
	
	/** @see DCOPProblemInterface#getMyVars() */
	public Set<String> getMyVars() {
		
		Set<String> myVars = new HashSet<String> ();
		
		if (this.agentName == null) 
			return myVars;
		
		for (Map.Entry<String, String> entry : this.owners.entrySet()) 
			if (this.agentName.equals(entry.getValue()))
				myVars.add(entry.getKey());
		
		return myVars;
	}

	/** @see DCOPProblemInterface#getAnonymVars() */
	public Set<String> getAnonymVars() {
		
		HashSet<String> out = new HashSet<String> ();
		
		// Go through all variables in all spaces
		for (UtilitySolutionSpace<V, U> space : this.spaces) 
			for (String var : space.getVariables()) 
				if (this.owners.get(var) == null) 
					out.add(var);
		
		return out;
	}

	/** @see DCOPProblemInterface#getRandVars() */
	@Override
	public Set<String> getRandVars() {
		return Collections.unmodifiableSet(this.randVars);
	}

	/** @see DCOPProblemInterface#getAllVars() */
	public Set<String> getAllVars() {
		HashSet<String> out = new HashSet<String> (this.getVariables());
		out.addAll(this.getAnonymVars());
		return out;
	}

	/** @see DCOPProblemInterface#getZeroUtility() */
	public U getZeroUtility() {
		try {
			return (U) utilClass.getConstructor().newInstance().getZero();
			
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
			System.err.println("Failed calling the nullary constructor for the class " + utilClass.getName() + " used for utility values");
			e.printStackTrace();
			return null;
		}
	}

	/** @see DCOPProblemInterface#maximize() */
	public boolean maximize() {
		return this.maximize;
	}

	/** @see DCOPProblemInterface#setMaximize(boolean) */
	public void setMaximize(final boolean maximize) {
		
		if (this.maximize != maximize) {
			final U inf = (maximize ? this.getMinInfUtility() : this.getPlusInfUtility());
			for (UtilitySolutionSpace<V, U> space : this.spaces) 
				space.setInfeasibleUtility(inf);
		}
		
		this.maximize = maximize;
	}

	/** @see DCOPProblemInterface#setUtilClass(java.lang.Class) */
	public void setUtilClass (Class<U> utilClass) {
		this.utilClass = utilClass;
	}

	/** @see DCOPProblemInterface#addAgent(java.lang.String) */
	@Override
	public boolean addAgent(String agent) {
		return this.agents.add(agent);
	}

	/** @see DCOPProblemInterface#getNeighborhoods(java.lang.String) */
	@Override
	public Map<String, Set<String>> getNeighborhoods(String agent) {
		return this.getNeighborhoods(agent, false, false);
		}

	/** @see DCOPProblemInterface#getNeighborhoods(java.lang.String, boolean, boolean) */
	@Override
	public Map<String, Set<String>> getNeighborhoods(String agent, boolean withAnonymVars, boolean onlyAnonymVars) {

		// For each variable that this agent owns, a collection of neighbor variables
		Map< String, Set<String> > neighborhoods = new HashMap< String, Set<String> > ();

		// Go through the list of variables owned by the input agent (or through all variables if the input agent is null)
		for (String var : (agent == null ? this.getVariables() : this.getVariables(agent))) {

			// Get the neighbors of this variable
			HashSet<String> neighbors = this.getNeighborVars(var, onlyAnonymVars || withAnonymVars);
			neighborhoods.put(var, neighbors);

			// Remove the non-anonymous variables if required
			if (onlyAnonymVars) 
				for (Iterator<String> iter = neighbors.iterator(); iter.hasNext(); ) 
					if (! this.isRandom(iter.next())) 
						iter.remove();
		}

		return neighborhoods;
	}

	/** @see DCOPProblemInterface#addUnarySpace(String, String, Addable[], Addable[]) */
	@Override
	public UtilitySolutionSpace<V, U> addUnarySpace(String name, String var, V[] dom, U[] utils) {
		
		@SuppressWarnings("unchecked")
		V[][] doms = (V[][]) Array.newInstance(dom.getClass(), 1);
		doms[0] = dom;
		
		Hypercube<V, U> out = new Hypercube<V, U> (new String[] {var}, doms, utils, this.getInfeasibleUtil(), this);
		out.setName(name);
		
		this.addSolutionSpace(out);
		
		return out;
	}

}
