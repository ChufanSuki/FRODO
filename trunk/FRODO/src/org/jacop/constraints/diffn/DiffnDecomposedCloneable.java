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

package org.jacop.constraints.diffn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.DecomposedConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.core.Var;

/** A cloneable version of the DiffnDecomposed constraint
 * @author Thomas Leaute
 */
public class DiffnDecomposedCloneable extends DiffnDecomposed 
implements ConstraintCloneableInterface<DiffnDecomposedCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

    /**
     * It specifies a diff constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     */
	public DiffnDecomposedCloneable(IntVarCloneable[][] rectangle) {
		super(rectangle);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

    /**
     * It constructs a diff constraint.
     *
     * @param origin1 list of variables denoting origin of the rectangle in the first dimension.
     * @param origin2 list of variables denoting origin of the rectangle in the second dimension.
     * @param length1 list of variables denoting length of the rectangle in the first dimension.
     * @param length2 list of variables denoting length of the rectangle in the second dimension.
     */
	public DiffnDecomposedCloneable(IntVarCloneable[] origin1, IntVarCloneable[] origin2, IntVarCloneable[] length1, IntVarCloneable[] length2) {
		super(origin1, origin2, length1, length2);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

    /**
     * It specifies a diffn constraint.
     *
     * @param rectangle list of rectangles which can not overlap in at least one dimension.
     */
	public DiffnDecomposedCloneable(List<? extends List<? extends IntVarCloneable>> rectangle) {
		super(rectangle);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

    /**
     * It constructs a diff constraint.
     *
     * @param x  list of variables denoting origin of the rectangle in the first dimension.
     * @param y  list of variables denoting origin of the rectangle in the second dimension.
     * @param lx list of variables denoting length of the rectangle in the first dimension.
     * @param ly list of variables denoting length of the rectangle in the second dimension.
     */
	public DiffnDecomposedCloneable(List<? extends IntVarCloneable> x, List<? extends IntVarCloneable> y, List<? extends IntVarCloneable> lx,
			List<? extends IntVarCloneable> ly) {
		super(x, y, lx, ly);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<? extends Var> arguments() {
		
		ArrayList<IntVar> out = new ArrayList<IntVar> (this.x.length + this.y.length + this.lx.length + this.ly.length);
		out.addAll(Arrays.asList(this.x));
		out.addAll(Arrays.asList(this.y));
		out.addAll(Arrays.asList(this.lx));
		out.addAll(Arrays.asList(this.ly));
		
		return out;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public DiffnDecomposedCloneable cloneInto(StoreCloneable targetStore)
			throws FailException {
		
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable[] y2 = new IntVarCloneable [this.y.length];
		for (int i = this.y.length - 1; i >= 0; i--) 
			if ((y2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.y[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable[] lx2 = new IntVarCloneable [this.lx.length];
		for (int i = this.lx.length - 1; i >= 0; i--) 
			if ((lx2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.lx[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable[] ly2 = new IntVarCloneable [this.ly.length];
		for (int i = this.ly.length - 1; i >= 0; i--) 
			if ((ly2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.ly[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new DiffnDecomposedCloneable (x2, y2, lx2, ly2);
	}

}
