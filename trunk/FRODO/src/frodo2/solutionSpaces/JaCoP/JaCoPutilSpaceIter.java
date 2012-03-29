/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2012  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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
<http://frodo2.sourceforge.net/>
*/

package frodo2.solutionSpaces.JaCoP;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

import JaCoP.core.Domain;
import JaCoP.core.IntDomain;
import JaCoP.core.IntVar;
import JaCoP.core.IntervalDomain;
import JaCoP.core.Store;
import JaCoP.search.DepthFirstSearch;
import JaCoP.search.IndomainMin;
import JaCoP.search.InputOrderSelect;
import JaCoP.search.Search;
import JaCoP.search.SolutionListener;

import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.hypercube.Hypercube;
import frodo2.solutionSpaces.hypercube.HypercubeIter;


/** A solution iterator for JaCoPutilSpace
 * @author Arnaud Jutzeler, Thomas Leaute
 * @param <U> the type used for utility values
 */
public class JaCoPutilSpaceIter < U extends Addable<U> > extends HypercubeIter<AddableInteger, U>{
	
	/** The JaCoPutilSpace we are iterating over */
	protected JaCoPutilSpace<U> space;
	
	/** The bound passed to the nextUtility() method */
	private U bound;
	
	/** If \c true, nextUtility(bound) returns only solutions whose cost is strictly lower than the bound */
	private boolean minimize;

	/** The solution listener used when calling nextUtility() with a bound */
	private SolutionListener<IntVar> solListener;
	
	/** The index of the current solution in the solution listener */
	private int solIndex = 1;

	/** The index of the utility variable in the list of variables */
	private int utilVarIndex;
	
	/** Constructor
	 * @param space 		the space over which to iterate
	 * @param variables 	the variable order for the iteration
	 * @param domains 		the domains of the variables
	 */
	public JaCoPutilSpaceIter(JaCoPutilSpace<U> space, String[] variables, AddableInteger[][] domains){
		this(space, variables, domains, (AddableInteger[]) Array.newInstance(AddableInteger.class, variables.length));
	}
	
	/** Constructor
	 * @param space 		the space over which to iterate
	 * @param variables 	the variable order for the iteration
	 * @param domains 		the domains of the variables
	 * @param assignment 	An array that will be used as the output of nextSolution()
	 */
	public JaCoPutilSpaceIter(JaCoPutilSpace<U> space, String[] variables, AddableInteger[][] domains, AddableInteger[] assignment){
		this.space = space;
		this.variables = variables;
		this.domains = domains;
		
		this.nbrVars = variables.length;
		this.solution = assignment;
		for (int i = 0; i < nbrVars; i++)
			solution[i] = domains[i][0];
		
		this.valIndexes = new int [nbrVars];
		Arrays.fill(this.valIndexes, 0);
		valIndexes[nbrVars - 1] = -1;
		
		// Compute the steps, knowing that the two variable orders may differ, and the input domains may be sub-domains of the space's domains, and in a different order
		this.steps = new int [nbrVars][];
		nbrSolLeft = 1;
		for (int i = 0; i < nbrVars; i++) {
			int domSize = domains[i].length;
			steps[i] = new int [domSize];
			nbrSolLeft *= domSize;
		}
		this.nbrSols = this.nbrSolLeft;
		
		// For each variable, compute its index in the input array
		HashMap<String, Integer> indexes = new HashMap<String, Integer> (nbrVars);
		for (int i = 0; i < nbrVars; i++) 
			indexes.put(variables[i], i);
		
		int nbrSpaceVars = space.getVariables().length;
		int step = 1;
		for (int i = nbrSpaceVars - 1; i >= 0; i--) {
			AddableInteger[] spaceDom = space.getDomain(i);
			int spaceDomSize = spaceDom.length;
			
			// Look up the index for this variable in the input variable array
			Integer index = indexes.get(space.getVariable(i));
			assert index != null : "The input array of variables " + Arrays.asList(variables) + " must contain all of the space's variables " + Arrays.asList(space.getVariables());
			
			// For each of this variable's values in the input domain array, compute its absolute incremental step in the space's utility array
			AddableInteger[] dom = domains[index];
			int domSize = dom.length;
			int[] mySteps = new int [domSize];
			for (int j = 0; j < domSize; j++) {
				AddableInteger val = dom[j];
				
				// Go through the values in the space's domain for this variable
				assert Hypercube.sub(dom, spaceDom).length == 0 : 
					"The input domain " + Arrays.asList(dom) + " for variable " + space.getVariable(i) + " is not a sub-domain of the space's: " + Arrays.asList(spaceDom);
				int myStep = 0;
				for ( ; myStep < spaceDomSize; myStep++) 
					if (val.equals(spaceDom[myStep])) 
						break;
				mySteps[j] = myStep * step;
			}
			utilIndex += mySteps[0];
			
			// Convert from absolute steps to relative steps
			int lastStep = mySteps[domSize - 1];
			for (int j = domSize - 1; j > 0; j--) 
				mySteps[j] = mySteps[j] - mySteps[j - 1];
			mySteps[0] = mySteps[0] - lastStep;
			
			steps[index] = mySteps;
			step *= spaceDomSize;
		}
		utilIndex -= steps[nbrVars - 1][0];
		
	}
	
	/** @see HypercubeIter#iter() */
	@Override
	protected int iter(){
		
		final AddableInteger[][] myDoms = this.domains;
		final int[] myValIndexes = this.valIndexes;
		final AddableInteger[] mySol = this.solution;
		final int[][] mySteps = this.steps;
		int myUtilIndex = this.utilIndex;
		
		// Iterates over the variables (in reversed order) to find the next one(s) to be iterated
		int varIndex = this.nbrVars - 1;
		for ( ; varIndex >= 0; varIndex--) {
			
			// Check if we have exhausted all values in the domain of the varIndex'th variable
			AddableInteger[] dom = myDoms[varIndex];
			int valIndex = myValIndexes[varIndex];
			if (valIndex == dom.length - 1) {
				
				// Reset the variable to its first domain value
				myValIndexes[varIndex] = 0;
				mySol[varIndex] = dom[0];
				myUtilIndex += mySteps[varIndex][0];
				
				// Increment the previous variable
				continue;
			}
			
			else { // increment the value for this variable
				valIndex = ++myValIndexes[varIndex];
				mySol[varIndex] = dom[valIndex];
				myUtilIndex += mySteps[varIndex][valIndex];
				break;
			}
		}
		
		this.utility = space.getUtility(myUtilIndex);
		this.nbrSolLeft--;
		
		return this.utilIndex = myUtilIndex;
	}
	
	/** @see HypercubeIter#setCurrentUtility(java.lang.Object) */
	@Override
	public void setCurrentUtility(U util) {
		utility = util;
		space.setUtility(this.utilIndex, util);
	}
	
	/** @see HypercubeIter#getCurrentUtility() */
	@Override
	public U getCurrentUtility() {
		return this.utility;
	}

	/** @see HypercubeIter#nextUtility() */
	@Override
	public U nextUtility() {
		
		// Return null if there are no more solutions
		if (this.nbrSolLeft <= 0) {
			this.utility = null;
			this.solution = null;
			return null;
		}
		
		this.iter();
		
		return this.utility;
	}

	/** @see HypercubeIter#nextUtility(frodo2.solutionSpaces.Addable, boolean) */
	@SuppressWarnings("unchecked")
	@Override
	public U nextUtility(U bound, final boolean minimize) {
		
		if (! this.hasNext()) 
			return null;
		
		// Check if the space is scalar
		if(this.space.getNumberOfVariables() == 0) {
			
			// Check if the utility is strictly better than the input bound
			if ((minimize ? this.space.getUtility(0).compareTo(bound) < 0 : this.space.getUtility(0).compareTo(bound) > 0)) 
				return this.nextUtility();
			
			// No more solutions
			this.nbrSolLeft = 0;
			this.utility = null;
			this.solution = null;
			return null;
		}
		
		// Check whether we have already started iterating
		if (this.bound == null) { // the iteration must be initialized
			this.bound = bound;
			this.minimize = minimize;
			
			// Find all solutions and return the first
			this.findAllSolutions();
			if (this.solListener == null) { // no solution found
				this.nbrSolLeft = 0;
				this.utility = null;
				this.solution = null;
				return null;
			}
			assert solListener.solutionsNo() > 0: "There is no solution";
			Domain[] sol = solListener.getSolution(this.solIndex);
			for(int i = nbrVars - 1; i >= 0; i--){
				assert sol[i].singleton(): "The domain of the solution is not a singleton";
				this.solution[i] = new AddableInteger(sol[i].valueEnumeration().nextElement());
			}
			try {
				this.utility = (U) this.space.defaultUtil.getClass().getConstructor(int.class).newInstance(sol[this.utilVarIndex].valueEnumeration().nextElement());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!this.minimize) 
				this.utility = this.utility.flipSign();
			this.utility = this.utility.add(this.space.defaultUtil);
			this.solIndex++;

			return utility;
			
		} else { // the iteration has already started
			assert minimize == this.minimize : "Changing of bound direction not currently supported";
			assert (minimize ? bound.compareTo(this.bound) <= 0 : bound.compareTo(this.bound) >= 0) : "Unsupported bound relaxation; new bound " + bound + " is worse than new bound: " + this.bound;
			
			do{
				// Check if a solution remains
				if (this.solIndex > this.solListener.solutionsNo()) { // no more solutions
					this.nbrSolLeft = 0;
					this.utility = null;
					this.solution = null;
					return null;
				}
				
				// Return the next solution
				Domain[] sol = solListener.getSolution(this.solIndex);
				for(int i = nbrVars - 1; i >= 0; i--){
					assert sol[i].singleton(): "The domain of the solution is not a singleton";
					this.solution[i] = new AddableInteger(sol[i].valueEnumeration().nextElement());
				}
				try {
					this.utility = (U) this.space.defaultUtil.getClass().getConstructor(int.class).newInstance(sol[this.utilVarIndex].valueEnumeration().nextElement());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!this.minimize) 
					this.utility = this.utility.flipSign();
				this.utility = this.utility.add(this.space.defaultUtil);
				this.solIndex++;
				
			} while (minimize ? this.utility.compareTo(bound) > 0 : this.utility.compareTo(bound) < 0);
			
			return utility;
		}
	}
	
	/** Find all solutions strictly better than the current bound */
	private void findAllSolutions () {
		
		// Get the JaCoP store
		Store store = this.space.getStore();
		if (store == null) {
			store = this.space.createStore();
			if (! store.consistency()) // no feasible solution exists
				return;
		}
		
		// Order of variables: 1) normal variables; 2) projected variables; 3) utility variable
		this.utilVarIndex = nbrVars + space.getProjectedVars().length;
		IntVar[] allVars = new IntVar[utilVarIndex + 1];

		// Find all normal variables
		String[] orderedVars = this.variables;
		int n = 0;
		for(int i = 0; i < orderedVars.length; i++){
			
			// Construct the domain
			IntervalDomain jacopDom;
			AddableInteger[] dom = this.domains[i];
			jacopDom = new IntervalDomain (dom.length);
			for (AddableInteger val : dom){
				jacopDom.addDom(new IntervalDomain (val.intValue(), val.intValue()));
			}
			
			allVars[n] = (IntVar) store.findVariable(orderedVars[i]);
			
			// This variable does not exist in the space, we need to create it in the store
			if(allVars[n] == null){
				// Construct the JaCoP variable
				allVars[n] = new IntVar (store, orderedVars[i], jacopDom);
			}else{
				// We update the domain of the variable
				IntDomain newDom = allVars[n].dom().intersect(jacopDom);
				
				// No feasible value for this variable
				if(newDom.isEmpty()){
					return;
				}else{
					allVars[n].dom().in(store.level, allVars[n], newDom);
				}
			}
			
			n++;
		}

		// Find all projected variables
		for(String var: space.getProjectedVars()){
			allVars[n] = (IntVar) store.findVariable(var);
			assert var != null: "Variable " + var + " not found in the store!";
			n++;
		}

		// Find the utility variable
		IntVar utilVar = (IntVar) store.findVariable("util_total"); /// @bug Potential name clash with a user-specified variable name
		assert utilVar != null: "Variable " + "util_total" + " not found in the store!";
		allVars[utilVarIndex] = utilVar;
		
		// Record the current store level so that we can later restore the state
		final int lvlReminder = store.level;

		// Change the store level to be able to restrict the utility variable in an reversible manner
		store.setLevel(lvlReminder+1);

		// Restrict the domain of the utility variable so that all solutions are strictly better than the bound
		int localBound = bound.subtract(this.space.defaultUtil).intValue() - 1;
		if (this.minimize){
			if(utilVar.domain.min() > localBound)
				return;
			utilVar.domain.in(lvlReminder + 1, utilVar, 
					utilVar.domain.min(), localBound);
		}else{ // maximizing
			if(utilVar.domain.max() < localBound)
				return;
			utilVar.domain.in(lvlReminder + 1, utilVar, 
					localBound, utilVar.domain.max());
		}
		if (! store.consistency()) // no feasible solution exists
			return;

		// Search for all solutions strictly better than the bound
		Search<IntVar> search = new DepthFirstSearch<IntVar> ();
		search.getSolutionListener().recordSolutions(true);
		search.getSolutionListener().searchAll(true);
		search.setAssignSolution(false);
		search.setPrintInfo(false);

		/// @bug Projected variables are not being projected
		assert this.space.getProjectedVars().length == 0 : "Iteration over a space with projected variables is currently unsupported";
		
		if (! search.labeling(store, new InputOrderSelect<IntVar> (store, allVars, new IndomainMin<IntVar>()))) // no solution found
			return;

		// Restore the state of the store before the call to the method
		for(int k = store.level; k > lvlReminder; k--) 
			store.removeLevel(k);
		store.setLevel(lvlReminder);
		
		// Record all the solutions
		this.solListener = search.getSolutionListener();
	}

}
