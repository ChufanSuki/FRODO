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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.MostConstrainedDynamic;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SimpleSolutionListener;
import org.jacop.search.SmallestDomain;

import frodo2.solutionSpaces.Addable;

/** This solution listener is a part of the JaCoPutilSpace iterator that allows us to simulate a JaCoP master/slave search combination
 * where the slave is an optimization search while the master is not.
 * 
 * @author Arnaud Jutzeler, Thomas Leaute
 * @param <U> The class of utility values
 *
 */
public class IterSolutionListener < U extends Addable<U> > extends SimpleSolutionListener<IntVarCloneable> {

	/** The master search */
	private JaCoPiterSearch<U> search;

	/** The JaCoP store */
	private StoreCloneable store;

	/** The JaCoP variables whose projection has been requested */
	private IntVarCloneable[] projectedVars;

	/** The utility variable */
	private IntVarCloneable utilVar;
	
	/** The infeasible utility */
	private final U infeasibleUtil;

	/** The condition used to signal this thread to continue the search until a next solution is found */
	private Condition nextAsked;

	/** The condition used to signal the main thread that a new solution has been found and has been delivered */
	private Condition nextDelivered;

	/** Constructor
	 * @param search			The JaCoPSearch that start the search
	 * @param store				The JaCoP store in which the search is performed
	 * @param projectedVars		The projected variables that we need to optimize in a slave search
	 * @param utilVar			The utility variable
	 * @param nextAsked			The condition used to signal the search to continue the search
	 * @param nextDelivered		The condition used to signal the iterator thread that a new solution has been found
	 * @param infeasibleUtil 	The infeasible utility
	 */
	public IterSolutionListener(JaCoPiterSearch<U> search, StoreCloneable store, IntVarCloneable[] projectedVars, IntVarCloneable utilVar, Condition nextAsked, Condition nextDelivered, 
			U infeasibleUtil){
		this.search = search;
		this.store = store;
		this.projectedVars = projectedVars;
		this.utilVar = utilVar;
		this.nextAsked = nextAsked;
		this.nextDelivered = nextDelivered;
		this.infeasibleUtil = infeasibleUtil;
	}
	
	/**
	 * @see org.jacop.search.SimpleSolutionListener#executeAfterSolution(org.jacop.search.Search, org.jacop.search.SelectChoicePoint)
	 */
	@Override
	public boolean executeAfterSolution(Search<IntVarCloneable> search, SelectChoicePoint<IntVarCloneable> select) {
		
		boolean returnCode = super.executeAfterSolution(search, select);
		
		Map<IntVarCloneable, Integer> position = select.getVariablesMapping();
		IntVarCloneable[] vars = new IntVarCloneable[position.size()];
		for (Iterator<IntVarCloneable> itr = position.keySet().iterator(); itr.hasNext();) {
			IntVarCloneable current = itr.next();	
			vars[position.get(current)] = current;
		}
		
		parentSolutionNo = new int[1];
		
		// Increase the store level for the slave search
		int level = store.level;
		store.setLevel(level+1);
		
		Search<IntVarCloneable> slaveSearch = new DepthFirstSearch<IntVarCloneable> ();
		slaveSearch.setSolutionListener(new SimpleSolutionListener<IntVarCloneable>());
		slaveSearch.getSolutionListener().recordSolutions(false);
		slaveSearch.getSolutionListener().searchAll(false);
		slaveSearch.setAssignSolution(true);
		slaveSearch.setPrintInfo(false);
		
		// We need to project some variables
		boolean feasible = false;
		if(projectedVars.length != 0){
			
			feasible = slaveSearch.labeling(store, new SimpleSelect<IntVarCloneable> (this.projectedVars, 
					new SmallestDomain<IntVarCloneable>(), new MostConstrainedDynamic<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), utilVar);
			
			if (feasible && ! utilVar.singleton()) // the util var is not yet grounded
				feasible = slaveSearch.labeling(store, 
						new SimpleSelect<IntVarCloneable> (new IntVarCloneable[] { utilVar }, new SmallestDomain<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), 
						utilVar);
			
		// There is no delayed projection to perform
		} else 
			feasible = slaveSearch.labeling(store, 
					new SimpleSelect<IntVarCloneable> (new IntVarCloneable[] { utilVar }, new SmallestDomain<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), 
					utilVar);
				
		// Record the current solution, and inform the main thread
		if (feasible) {
			assert utilVar.dom().min() < this.search.getCurrentBound() : utilVar.dom().min() + " < " + this.search.getCurrentBound();
			int[] currentSolution = new int[vars.length];

			for (int i = 0; i < vars.length; i++) {
				assert ! feasible || vars[i].singleton(): "Variable " + vars[i].id + " is not grounded in the solution";
				currentSolution[i] = vars[i].min();
			}

			// Give the next solution to the iterator
			assert ! feasible || utilVar.singleton(): "The utility variable is not grounded in the solution";
			this.search.setSolution(currentSolution, this.infeasibleUtil.fromInt(utilVar.dom().min()));
		}
		else { // infeasible
			
			// Restore the store level to before the slave search
			for(int k = store.level; k > level; k--){
				store.removeLevel(k);
			}
			store.setLevel(level);
			
			// Increment the cost value, which will otherwise incorrectly be decremented by the DepthFirstSearch 
			((DepthFirstSearch<IntVarCloneable>) search).costValue++;
			
			return false;
		}

		// Wake up the iterator
		this.nextDelivered.signal();

		try{
			// Freeze the search
			this.nextAsked.await(); /// @bug handle spurious wakeups

		// The thread was interrupted, we want to exit it
		}catch(InterruptedException e){
			throw new RuntimeException("suicide");
		}
		
		// Restore the store level
		for(int k = store.level; k > level; k--){
			store.removeLevel(k);
		}
		
		store.setLevel(level);
		
		// Reset JaCoP's cost bound to the one passed to the iterator
		this.search.search.costValue = this.search.getCurrentBound();
		
		return returnCode;
	}
}
