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

/** A cloneable version of the Disjoint constraint
 * @author Thomas Leaute
 */
public class DisjointCloneable extends Disjoint implements ConstraintCloneableInterface<DisjointCloneable> {

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 */
	public DisjointCloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles) {
		super(rectangles);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 */
	public DisjointCloneable(IntVarCloneable[][] rectangles) {
		super(rectangles);
	}

	/**
	 *
	 * @param rectangles a list of rectangles.
	 * @param doProfile should profile be computed and used.
	 * 
	 */
	public DisjointCloneable(Rectangle[] rectangles, boolean doProfile) {
		super(rectangles, doProfile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 * @param profile specifies if the profile is computed and used.
	 */
	public DisjointCloneable(ArrayList<? extends ArrayList<? extends IntVarCloneable>> rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param rectangles list of rectangles with origins and lengths in both dimensions.
	 * @param profile specifies if the profile is computed and used.
	 */
	public DisjointCloneable(IntVarCloneable[][] rectangles, boolean profile) {
		super(rectangles, profile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 */
	public DisjointCloneable(ArrayList<? extends IntVarCloneable> o1, ArrayList<? extends IntVarCloneable> o2,
			ArrayList<? extends IntVarCloneable> l1, ArrayList<? extends IntVarCloneable> l2) {
		super(o1, o2, l1, l2);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 */
	public DisjointCloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2) {
		super(o1, o2, l1, l2);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 * @param profile specifies if the profile should be computed.
	 */
	public DisjointCloneable(ArrayList<IntVarCloneable> o1, ArrayList<IntVarCloneable> o2, ArrayList<IntVarCloneable> l1, ArrayList<IntVarCloneable> l2,
			boolean profile) {
		super(o1.toArray(new IntVarCloneable [o1.size()]), o2.toArray(new IntVarCloneable [o2.size()]), 
				l1.toArray(new IntVarCloneable [l1.size()]), l2.toArray(new IntVarCloneable [l2.size()]), profile);
	}

	/**
	 * It creates a diff2 constraint.
	 * @param o1 list of variables denoting the origin in the first dimension.
	 * @param o2 list of variables denoting the origin in the second dimension.
	 * @param l1 list of variables denoting the length in the first dimension.
	 * @param l2 list of variables denoting the length in the second dimension.
	 * @param profile specifies if the profile should be computed.
	 */	
	public DisjointCloneable(IntVarCloneable[] o1, IntVarCloneable[] o2, IntVarCloneable[] l1, IntVarCloneable[] l2, boolean profile) {
		super(o1, o2, l1, l2, profile);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public DisjointCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		return new DisjointCloneable (DiffCloneable.cloneRectangles(this.rectangles, targetStore), super.doProfile);
	}

}
