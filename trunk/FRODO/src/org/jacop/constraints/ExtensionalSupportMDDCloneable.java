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
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.util.IndexDomainView;
import org.jacop.util.MDDCloneable;

/** A cloneable version of the ExtensionalSupportMDD constraint
 * @author Thomas Leaute
 */
public class ExtensionalSupportMDDCloneable extends ExtensionalSupportMDD implements ConstraintCloneableInterface<ExtensionalSupportMDDCloneable> {

	/**
	 * It creates an extensional constraint.
	 * @param diagram multiple-valued decision diagram describing allowed tuples.
	 */
	public ExtensionalSupportMDDCloneable(MDDCloneable diagram) {
		super(diagram);
	}

	/** Constructor
	 * @param vars 		the variables
	 * @param table 	the table
	 */
	public ExtensionalSupportMDDCloneable(IntVarCloneable[] vars, int[][] table) {
		this(new MDDCloneable (vars, table));
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public ExtensionalSupportMDDCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVar[] vars = this.mdd.vars;
		MDDCloneable mdd = (MDDCloneable) this.mdd;

		// Clone the variables and the IndexDomainViews
		IntVarCloneable[] vars2 = new IntVarCloneable [vars.length];
		IndexDomainView[] views2 = new IndexDomainView [vars.length];
		IntVarCloneable var2;
		for (int i = vars.length - 1; i >= 0; i--) {
			if ((vars2[i] = var2 = targetStore.findOrCloneInto((IntVarCloneable) vars[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
			views2[i] = new IndexDomainView (var2, true);
		}
		
		// Create the domainLimits based on the domain sizes of the cloned variables
		int[] domainLimits2 = new int [vars2.length];
		for (int i = vars2.length - 1; i >= 0; i--) 
			domainLimits2[i] = vars2[i].dom().getSize();
		
		return new ExtensionalSupportMDDCloneable (new MDDCloneable (mdd.diagram.clone(), domainLimits2, mdd.freePosition, vars2, views2));
	}

}
