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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.MostConstrainedDynamic;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;

import frodo2.solutionSpaces.Addable;

/** A wrapper around a JaCoP DepthFirstSearch that is used to run this search in a new thread
 * 
 * @author Arnaud Jutzeler, Thomas Leaute
 * @param <U> The class used for utility values
 *
 */
public class JaCoPiterSearch < U extends Addable<U> > implements Runnable {

	/** The JaCoP store */
	private StoreCloneable store;
	
	/** The JaCoP variables */
	private IntVarCloneable[] vars;
	
	/** The JaCoP variables whose projection has been requested */
	private IntVarCloneable[] projectedVars;
	
	/** The utility variable */
	private IntVarCloneable utilVar;
	
	/** The last solution found */
	private int[] solution;
	
	/** The utility of the last solution found */
	private U utility;
	
	/** The infeasible utility */
	final private U infeasibleUtil;
	
	/** The current bound */
	private int currentBound;
	
	/** The lock used to ensure that only a single thread is running at a time  */
	private Lock lock;
	
	/** The condition used to signal this thread to continue the search until a next solution is found */
	private Condition nextAsked;
	
	/** The condition used to signal the main thread that a new solution has been found and has been delivered */
	private Condition nextDelivered;
	
	/** JaCoP's search strategy */
	DepthFirstSearch<IntVarCloneable> search;


	/**	Constructor
	 * @param store				The JaCoP store
	 * @param vars				The variables over which we iterate
	 * @param projectedVars		The variable whose projection has been requested
	 * @param utilVar			The utility variable
	 * @param lock				The lock that guarantees the mutual exclusion in the execution of the search thread and the iterator thread
	 * @param nextAsked			The condition used to signal the search to continue the search
	 * @param nextDelivered		The condition used to signal the iterator thread that a new solution has been found
	 * @param infeasibleUtil 	The infeasible utility
	 */
	public JaCoPiterSearch(StoreCloneable store, IntVarCloneable[] vars, IntVarCloneable[] projectedVars, IntVarCloneable utilVar, Lock lock, Condition nextAsked, Condition nextDelivered, 
			U infeasibleUtil){
		this.store = store;
		this.vars = vars;
		this.projectedVars = projectedVars;
		this.utilVar = utilVar;
		this.lock = lock;
		this.nextAsked = nextAsked;
		this.nextDelivered = nextDelivered;
		this.currentBound = Integer.MAX_VALUE;
		this.solution = null;
		this.infeasibleUtil = infeasibleUtil;
	}
	
	/** @see java.lang.Runnable#run() */
	public void run() {
		IterSolutionListener<U> solListener = new IterSolutionListener<U> (this, store, projectedVars, utilVar, nextAsked, nextDelivered, this.infeasibleUtil);
		
		// Search for all solutions strictly better than the bound
		search = new DepthFirstSearch<IntVarCloneable> ();
		search.setSolutionListener(solListener);
		search.respectSolutionListenerAdvice = true;
		search.getSolutionListener().recordSolutions(false);
		search.getSolutionListener().searchAll(true);
		search.setAssignSolution(false);
		search.setPrintInfo(false);
		
		// Acquire the lock
		lock.lock();
		
		try{
			
			// Start the depth first search
			search.labeling(store, new SimpleSelect<IntVarCloneable> (this.vars, 
					new SmallestDomain<IntVarCloneable>(), new MostConstrainedDynamic<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), utilVar);	
			
			// We set null as the current solution to inform that the search has finished
			this.solution = null;
			this.utility = this.infeasibleUtil;
			
			// Wake up the iterator
			this.nextDelivered.signal();
			
		// A RuntimeException can be used to exit the run() method and then kill the thread
		}catch(RuntimeException e){
			 // Do nothing, just exit the run function
		}finally{
			
			// Release the lock
			lock.unlock();
		}
		
		search = null;
	}
	
	/** The method used by the SolutionListener to record the last solution found
	 * @param lastSolution	The last solution
	 * @param lastUtility	The utility of the last solution
	 */
	public void setSolution(int[] lastSolution, U lastUtility){
		this.solution = lastSolution;
		this.utility = lastUtility;
	}
	
	/** The method used by the iterator to get the last solution found
	 * @return the last solution found
	 */
	public int[] getSolution() {
		return solution;
	}
	
	/** The method used by the iterator to get the utility of last solution found
	 * @return the utility of the last solution found
	 */
	public U getUtility() {
		return utility;
	}
	
	/** The method used by the iterator to set the new bound
	 * @param newBound	The new bound
	 */
	public void setNewBound(int newBound) {
		this.currentBound = newBound;
	}
	
	/** The method used by the SolutionLIstener to get the current bound
	 * @return the current bound
	 */
	public int getCurrentBound() {
		return currentBound;
	}
}