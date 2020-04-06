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

package org.jacop.constraints.table;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version Table constraint
 * @author Thomas Leaute
 */
public class TableCloneable extends Table implements ConstraintCloneableInterface<TableCloneable> {

    /**
     * It constructs a table constraint.
     *
     * @param list   the variables in the scope of the constraint.
     * @param tuples the tuples which define alloed values.
     */
	public TableCloneable(IntVarCloneable[] list, int[][] tuples) {
		super(list, tuples);
	}

    /**
     * It constructs a table constraint.
     *
     * @param list                the variables in the scope of the constraint.
     * @param tuples              the tuples which define alloed values.
     * @param reuseTuplesArgument specifies if the table of tuples should be used directly without copying.
     */
	public TableCloneable(IntVarCloneable[] list, int[][] tuples, boolean reuseTuplesArgument) {
		super(list, tuples, reuseTuplesArgument);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public TableCloneable cloneInto(StoreCloneable targetStore) throws FailException {
		
		// Clone the list
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw Store.failException;
		
		// Clone the tuples
		final int nbrVars = x2.length;
		int[][] tuples2 = new int [this.tuple.length][nbrVars];
		for (int i = this.tuple.length - 1; i >= 0; i--) 
			System.arraycopy(this.tuple[i], 0, tuples2[i], 0, nbrVars);
		
		return new TableCloneable (x2, tuples2);
	}

}
