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

package org.jacop.constraints.knapsack;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Knapsack constraint
 * @author Thomas Leaute
 */
public class KnapsackCloneable extends Knapsack implements ConstraintCloneableInterface<KnapsackCloneable> {
	
	/** The profit variable */
	private IntVarCloneable profit;
	
	/** The capacity variable */
	private IntVarCloneable capacity;

	/** Constructor
	 * @param items 	the items in the knapsack
	 * @param cap 		the capacity 
	 * @param profit 	the profit
	 */
	public KnapsackCloneable(KnapsackItem[] items, IntVarCloneable cap, IntVarCloneable profit) {
		super(items, cap, profit);
		this.profit = profit;
		this.capacity = cap;
	}

	/**
	 * 
	 * It constructs the knapsack constraint. 
	 * 
	 * @param profits the list of profits, each for the corresponding item no.
	 * @param weights the list of weights, each for the corresponding item no.
	 * @param quantity finite domain variable specifying allowed values for the vars.
	 * @param knapsackCapacity finite domain variable specifying the capacity limit of the knapsack.
	 * @param knapsackProfit finite domain variable defining the profit 
	 */
	public KnapsackCloneable(int[] profits, int[] weights, IntVarCloneable[] quantity, IntVarCloneable knapsackCapacity, IntVarCloneable knapsackProfit) {
		super(profits, weights, quantity, knapsackCapacity, knapsackProfit);
		this.profit = knapsackProfit;
		this.capacity = knapsackCapacity;
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public KnapsackCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the KnapsackItem array
		KnapsackItem[] items2 = new KnapsackItem [this.items.length];
		KnapsackItem item;
		for (int i = items2.length - 1; i >= 0; i--) {
			item = this.items[i];

			IntVarCloneable quantity2 = targetStore.findOrCloneInto((IntVarCloneable) item.quantity);
			if (quantity2.dom().isEmpty()) 
				throw StoreCloneable.failException;
			
			items2[i] = new KnapsackItem (quantity2, item.weight, item.profit);
		}
		
		IntVarCloneable knapsackProfit2 = targetStore.findOrCloneInto((IntVarCloneable) this.profit);
		if (knapsackProfit2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable knapsackCapacity2 = targetStore.findOrCloneInto((IntVarCloneable) this.capacity);
		if (knapsackCapacity2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new KnapsackCloneable (items2, knapsackCapacity2, knapsackProfit2);
	}

}
