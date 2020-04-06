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
import java.util.HashSet;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;
import org.jacop.core.Var;

/** A cloneable version of the Assignment constraint
 * @author Thomas Leaute
 */
public class AssignmentCloneable extends Assignment implements ConstraintCloneableInterface<AssignmentCloneable> {

	/**
	 * It constructs an Assignment constraint with shift equal 0. It
	 * enforces relation - d[x[j]] = i and x[d[i]] = j.
	 * @param xs arraylist of x variables
	 * @param ds arraylist of d variables
	 */
	public AssignmentCloneable(ArrayList<? extends IntVarCloneable> xs, ArrayList<? extends IntVarCloneable> ds) {
		super(xs, ds);
	}

	/**
	 * It constructs an Assignment constraint with shift equal 0. It
	 * enforces relation - d[x[i]] = i and x[d[i]] = i.
	 * @param xs array of x variables
	 * @param ds array of d variables
	 */
	public AssignmentCloneable(IntVarCloneable[] xs, IntVarCloneable[] ds) {
		super(xs, ds);
	}

	/**
	 * It enforces the relationship x[d[i]-min]=i+min and
	 * d[x[i]-min]=i+min. 
	 * @param xs arraylist of variables x
	 * @param ds arraylist of variables d
	 * @param min shift
	 */
	public AssignmentCloneable(ArrayList<? extends IntVarCloneable> xs, ArrayList<? extends IntVarCloneable> ds, int min) {
		super(xs, ds, min);
	}

	/**
	 * It enforces the relationship x[d[i]-min]=i+min and
	 * d[x[i]-min]=i+min. 
	 * @param xs array of variables x
	 * @param ds array of variables d
	 * @param min shift
	 */
	public AssignmentCloneable(IntVarCloneable[] xs, IntVarCloneable[] ds, int min) {
		super(xs, ds, min);
	}

	/**
	 * It enforces the relationship x[d[i]-shiftX]=i+shiftD and
	 * d[x[i]-shiftD]=i+shiftX. 
	 * @param xs array of variables x
	 * @param ds array of variables d
	 * @param shiftX a shift of indexes in X array.
	 * @param shiftD a shift of indexes in D array.
	 * 
	 * @note Original code copied from JaCoP 4.4 and patched for bugs
	 */
	public AssignmentCloneable(IntVarCloneable[] xs, IntVarCloneable[] ds, int shiftX, int shiftD) {
		super(xs, ds, shiftX, shiftD);
	}

	/**
	 * It enforces the relationship x[d[i]-shiftX]=i+shiftD and
	 * d[x[i]-shiftD]=i+shiftX. 
	 * @param xs arraylist of variables x
	 * @param ds arraylist of variables d
	 * @param shiftX shift for parameter xs
	 * @param shiftD shift for parameter ds
	 */
	public AssignmentCloneable(ArrayList<? extends IntVarCloneable> xs, ArrayList<? extends IntVarCloneable> ds, int shiftX, int shiftD) {
		super(xs, ds, shiftX, shiftD);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public AssignmentCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the first IntVar array
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw Store.failException;
		
		// Clone the second IntVar array
		IntVarCloneable[] d2 = new IntVarCloneable [this.d.length];
		for (int i = this.d.length - 1; i >= 0; i--) 
			if ((d2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.d[i])).dom().isEmpty()) 
				throw Store.failException;
		
		return new AssignmentCloneable (
				x2, 
				d2, 
				this.shiftX, 
				this.shiftD);
	}

	/** 
	 * @see Assignment#arguments() 
	 * @note Original code copied from JaCoP 4.4 and patched for bugs
	 */
	@Override
	public HashSet<Var> arguments() {

		HashSet<Var> variables = new HashSet<Var>(
				x.length + d.length);
		
		for (IntVar var : this.x) 
			variables.add(var);
		for (IntVar var : this.d) 
			variables.add(var);

		return variables;
	}

}
