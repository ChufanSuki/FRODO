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

import org.jacop.core.FailException;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Eq constraint
 * @author Thomas Leaute
 */
public class EqCloneable extends Eq implements ConstraintCloneableInterface<PrimitiveConstraint> {

	/** Constructor
	 * @param c1 	the first constraint
	 * @param c2 	the second constraint
	 */
	public EqCloneable(PrimitiveConstraint c1, PrimitiveConstraint c2) {
		super(c1, c2);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public PrimitiveConstraint cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException, FailException {
		
		// Clone the first constraint
		PrimitiveConstraint clone1 = null;
		Boolean c1feasible = null;
		try {
			clone1 = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.c1).cloneInto(targetStore);
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.c1.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		} catch (FailException e) {
			c1feasible = false;
		}
		if (clone1 == null) 
			c1feasible = true;
		
		// Clone the second constraint
		PrimitiveConstraint clone2 = null;
		try {
			clone2 = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.c2).cloneInto(targetStore);
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.c2.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		} catch (FailException e) {
			
			// c2 is inconsistent; check c1
			if (c1feasible == null) // we don't know yet whether c1 is consistent
				return new NotCloneable (clone1);
			
			else if (c1feasible) // c1 is consistent but c2 isn't 
				throw StoreCloneable.failException;
			
			else // both are inconsistent, so this constraint is consistent
				return null;
		}
		
		return new EqCloneable (clone1, clone2);
	}

}
