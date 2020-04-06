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

package org.jacop.constraints;

import java.util.ArrayList;
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the SumWeight constraint
 * @author Thomas Leaute
 */
public class SumWeightCloneable extends SumWeight implements ConstraintCloneableInterface<SumWeightCloneable> {

    /**
     * It specifies a list of variables being summed.
     */
    final private IntVarCloneable list[];

    /**
     * It specifies a list of weights associated with the variables being summed.
     */
    final private int weights[];
    
    /** The sum variable originally  passed to the constructor */
    final private IntVarCloneable sum;

    /**
     * It specifies value to which SumWeight is equal to.
     */
    final private long equalTo;

	/** Constructor
	 * @param list 		the list of variables
	 * @param weights 	the list of weights
	 * @param sum 		the total sum variable
	 */
	public SumWeightCloneable(IntVarCloneable[] list, int[] weights, IntVarCloneable sum) {
		super(list, weights, sum);
		this.list = list;
		this.weights = weights;
		this.sum = sum;
		this.equalTo = 0;
	}

    /** Constructor
     * @param list    the list of varibales
     * @param weights the list of weights
     * @param equalTo the value to which SumWeight is equal to.
     */
	public SumWeightCloneable(IntVarCloneable[] list, int[] weights, int equalTo) {
		super(list, weights, equalTo);
		this.list = list;
		this.weights = weights;
		this.sum = null;
		this.equalTo = equalTo;
	}

	/** Constructor
     * @param variables variables which are being multiplied by weights.
     * @param weights   weight for each variable.
     * @param sum       variable containing the sum of weighted variables.
	 */
	public SumWeightCloneable(List<? extends IntVarCloneable> variables, List<Integer> weights, IntVarCloneable sum) {
		super(variables, weights, sum);
		this.list = variables.toArray(new IntVarCloneable [variables.size()]);
		this.weights = weights.stream().mapToInt(i -> i).toArray();
		this.sum = sum;
		this.equalTo = 0;
	}

	/** Constructor
	 * @param list 		the list of variables
	 * @param weights 	the list of weights
	 * @param sum 		the total sum variable
	 */
	public SumWeightCloneable(ArrayList<? extends IntVarCloneable> list, ArrayList<Integer> weights, IntVarCloneable sum) {
		super(list, weights, sum);
		this.list = list.toArray(new IntVarCloneable [list.size()]);
		this.weights = weights.stream().mapToInt(i -> i).toArray();
		this.sum = sum;
		this.equalTo = 0;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public SumWeightCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		if (this.sum != null) {
			
			IntVarCloneable sum2 = targetStore.findOrCloneInto((IntVarCloneable) this.sum);
			if (sum2.dom().isEmpty()) 
				throw StoreCloneable.failException;
			
			return new SumWeightCloneable (list2, this.weights.clone(), sum2);
		} else 
			return new SumWeightCloneable (list2, this.weights.clone(), (int) this.equalTo);
	}

}
