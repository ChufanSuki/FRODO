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

import java.util.Arrays;
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Stretch constraint
 * @author Thomas Leaute
 */
public class StretchCloneable extends Stretch implements ConstraintCloneableInterface<StretchCloneable>, DecomposedConstraintCloneableInterface {
	
	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/** Constructor 
	 * @param dom 				the domain of values allowed for the variables
	 * @param minSubSegLength 	for each allowed variable value, the minimum length of any sequence of this value
	 * @param maxSubSegLength 	for each allowed variable value, the maximum length of any sequence of this value
	 * @param vars 				the variables being stretched
	 */
	public StretchCloneable(int[] dom, int[] minSubSegLength, int[] maxSubSegLength, IntVarCloneable[] vars) {
		super(dom, minSubSegLength, maxSubSegLength, vars);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}
	
	/** @see java.lang.Object#toString() */
	@Override
	public String toString () {
		
		StringBuilder builder = new StringBuilder (this.getClass().getSimpleName());
		
		builder.append("(values = ").append(Arrays.toString(this.values)).append(", ");
		builder.append("min = ").append(Arrays.toString(this.min)).append(", ");
		builder.append("max = ").append(Arrays.toString(this.max)).append(", ");
		builder.append("x = ").append(Arrays.toString(this.x)).append(")");
		
		return builder.toString();
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public StretchCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new StretchCloneable (
				this.values.clone(), 
				this.min.clone(), 
				this.max.clone(), 
				x2);
	}

	/** @see DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<IntVar> arguments() {
		return Arrays.asList(this.x);
	}


	/** @see DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

}
