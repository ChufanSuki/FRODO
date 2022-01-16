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

/** A cloneable version of the IfThen constraint
 * @author Thomas Leaute
 */
public class IfThenCloneable extends IfThen implements ConstraintCloneableInterface<PrimitiveConstraint>{

	/** Constructor
	 * @param condC 	the IF constraint
	 * @param thenC 	the THEN constraint
	 */
	public IfThenCloneable(PrimitiveConstraint condC, PrimitiveConstraint thenC) {
		super(condC, thenC);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@SuppressWarnings("unchecked")
	@Override
	public PrimitiveConstraint cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException {
		
		// Clone the IF constraint
		PrimitiveConstraint ifClone = null;
		Boolean ifIsTrue = null;
		try {
			ifClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.condC).cloneInto(targetStore);
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.condC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		} catch (FailException e) {
			return null; // the condition is not consistent, so this constraint is always consistent
		}
		if (ifClone == null) 
			ifIsTrue = true;
		
		// Clone the THEN constraint
		PrimitiveConstraint thenClone = null;
		try {
			thenClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.thenC).cloneInto(targetStore);
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.thenC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		} catch (FailException e) {
			
			// THEN is inconsistent; check IF
			if (ifIsTrue == null) // we don't know yet whether IF is consistent
				return new NotCloneable (ifClone);
			
			else { // IF is consistent but THEN isn't 
				assert ifIsTrue;
				throw StoreCloneable.failException;
			}
		}

		return new IfThenCloneable (ifClone, thenClone);
	}

}
