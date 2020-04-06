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

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A clonable version of JaCoP's SumInt constraint
 * @author Thomas Leaute
 */
public class SumIntCloneable extends org.jacop.constraints.SumInt implements ConstraintCloneableInterface<SumIntCloneable> {

	/** Constructor
	 * @param list 		the list of variables being summed
	 * @param rel 		the relation name
	 * @param sum 		the total sum variable
	 */
	public SumIntCloneable(IntVarCloneable[] list, String rel, IntVarCloneable sum) {
		super(list, rel, sum);
	}

	/** Constructor
	 * @param list 		the list of variables being summed
	 * @param rel 		the relation name
	 * @param sum 		the total sum variable
	 */
	public SumIntCloneable(ArrayList<? extends IntVarCloneable> list, String rel, IntVarCloneable sum) {
		super(list, rel, sum);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public SumIntCloneable cloneInto(StoreCloneable targetStore) 
			throws CloneNotSupportedException, FailException {
		
		// Clone the X
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		IntVarCloneable sum2 = targetStore.findOrCloneInto((IntVarCloneable) this.sum);
		if (sum2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new SumIntCloneable (x2, this.rel2String(), sum2);
	}

}
