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

/** A cloneable version of the IfThenElse constraint
 * @author Thomas Leaute
 */
public class IfThenElseCloneable extends IfThenElse implements ConstraintCloneableInterface<PrimitiveConstraint> {

	/** Constructor
	 * @param condC 	the IF constraint
	 * @param thenC 	the THEN constraint
	 * @param elseC 	the ELSE constraint
	 */
	public IfThenElseCloneable(PrimitiveConstraint condC, PrimitiveConstraint thenC, PrimitiveConstraint elseC) {
		super(condC, thenC, elseC);
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
			ifIsTrue = false;
		}
		if (ifClone == null) 
			ifIsTrue = true;
		
		// Clone the THEN constraint
		PrimitiveConstraint thenClone = null;
		try {
			thenClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.thenC).cloneInto(targetStore);
		} catch (ClassCastException e) {
			throw new CloneNotSupportedException (this.thenC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
			
		} catch (FailException e) {	// THEN is inconsistent; return AND( NOT(IF), ELSE )
			
			if (ifIsTrue == null) { // we don't know yet whether IF is consistent
				
				// Clone the ELSE constraint
				PrimitiveConstraint elseClone = null;
				try {
					elseClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.elseC).cloneInto(targetStore);
				} catch (ClassCastException e2) {
					throw new CloneNotSupportedException (this.elseC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
				} catch (FailException e2) {
					
					// ELSE is inconsistent
					throw StoreCloneable.failException;
				}
				if (elseClone == null) // ELSE is consistent  
					return new NotCloneable (ifClone);

				return new AndCloneable (new NotCloneable (ifClone), elseClone);
				
			} else if (ifIsTrue) // IF is consistent
				throw StoreCloneable.failException;
			
			else { // IF is inconsistent; return ELSE
				
				// Clone the ELSE constraint
				PrimitiveConstraint elseClone = null;
				try {
					elseClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.elseC).cloneInto(targetStore);
				} catch (ClassCastException e2) {
					throw new CloneNotSupportedException (this.elseC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
				} catch (FailException e2) {
					
					// ELSE is inconsistent
					throw StoreCloneable.failException;
				}
				if (elseClone == null) // ELSE is consistent  
					return null;

				return elseClone;
			}
		}
		
		if (thenClone == null) { // THEN is consistent; return IFTHEN( NOT(IF), ELSE )
			
			// Clone the ELSE constraint
			PrimitiveConstraint elseClone = null;
			try {
				elseClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.elseC).cloneInto(targetStore);
			} catch (ClassCastException e2) {
				throw new CloneNotSupportedException (this.elseC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
			} catch (FailException e2) {
				
				// ELSE is inconsistent; return IF
				return ifClone;
			}
			if (elseClone == null) // ELSE is consistent, so this constraint is consistent
				return null;

			return new IfThenCloneable (new NotCloneable (ifClone), elseClone);
		}
		
		// We don't know yet whether IF and THEN are consistent
		
		// Clone the ELSE constraint
		PrimitiveConstraint elseClone = null;
		try {
			elseClone = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.elseC).cloneInto(targetStore);
		} catch (ClassCastException e2) {
			throw new CloneNotSupportedException (this.elseC.getClass() + " does not implement " + ConstraintCloneableInterface.class);
		} catch (FailException e2) {
			
			// ELSE is inconsistent; return AND( IF, THEN )
			return new AndCloneable (ifClone, thenClone);
		}
		if (elseClone == null) // ELSE is consistent; return IFTHEN( IF, THEN )
			return new IfThenCloneable (ifClone, thenClone);

		return new IfThenElseCloneable (ifClone, thenClone, elseClone);
	}

}
