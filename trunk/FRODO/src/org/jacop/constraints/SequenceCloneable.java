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
import org.jacop.core.IntervalDomain;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Sequence constraint
 * @author Thomas Leaute
 */
public class SequenceCloneable extends Sequence implements ConstraintCloneableInterface<SequenceCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/**
	 * It creates a Sequence constraint. 
	 * 
	 * @param list variables which assignment is constrained by Sequence constraint. 
	 * @param set set of values which occurrence is counted within each sequence.
	 * @param q the length of the sequence
	 * @param min the minimal occurrences of values from set within a sequence.
	 * @param max the maximal occurrences of values from set within a sequence.
	 */
	public SequenceCloneable(IntVarCloneable[] list, IntervalDomain set, int q, int min, int max) {
		super(list, set, q, min, max);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public SequenceCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the list
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new SequenceCloneable (
				list2, 
				this.set.clone(), 
				this.q, 
				this.min, 
				this.max);
	}

	/** @see DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<IntVar> arguments() {
		return Arrays.asList(this.list);
	}

	/** @see DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

}
