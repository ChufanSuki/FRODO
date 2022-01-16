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

import org.jacop.constraints.DecomposedConstraint;
import org.jacop.core.FailException;
import org.jacop.core.StoreCloneable;

/** A constraint is clonable if a state-less copy of it can be created into a target store
 * @author Thomas Leaute
 * @param <C> the class of the constraint
 */
public interface ConstraintCloneableInterface < C extends DecomposedConstraint<? extends Constraint> > {

	/** Creates a new constraint that is a (state-less) clone of this one, 
	 * but expressed on the variables with the same names found in the input store
	 * 
	 * @note The constraint is not automatically imposed into the input target store. 
	 * @note If a variable in this constraint's arguments is not found in the target store, it is automatically created there. 
	 * 
	 * @param targetStore 					the store into which this constraint should be cloned
	 * @return a state-less clone of this constraint, expressed over variables of the input store, or null if the constraint would be trivially consistent
	 * @throws CloneNotSupportedException 	if this constraint depends on underlying constraints that are themselves not cloneable
	 * @throws FailException 				if a variable with an empty domain is encountered
	 */
	public abstract C cloneInto (StoreCloneable targetStore) throws CloneNotSupportedException, FailException;
	
}
