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

package org.jacop.constraints.binpacking;

import java.util.ArrayList;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.binpacking.Binpacking;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Binpacking constraint
 * @author Thomas Leaute
 */
public class BinpackingCloneable extends Binpacking implements ConstraintCloneableInterface<BinpackingCloneable> {

	/**
	 * It constructs the binpacking constraint for the supplied variable.
	 * @param bin which are constrained to define bin for item i.
	 * @param load which are constrained to define load for bin i.
	 * @param w which define size of item i.
	 */
	public BinpackingCloneable(IntVarCloneable[] bin, IntVarCloneable[] load, int[] w) {
		super(bin, load, w);
	}

	/**
	 * It constructs the binpacking constraint for the supplied variable.
	 * @param bin which are constrained to define bin for item i.
	 * @param load which are constrained to define load for bin i.
	 * @param w which define size of item i.
	 */
	public BinpackingCloneable(ArrayList<? extends IntVarCloneable> bin, ArrayList<? extends IntVarCloneable> load, int[] w) {
		super(bin, load, w);
	}

	/**
	 * It constructs the binpacking constraint for the supplied variable.
	 * @param bin which are constrained to define bin for item i.
	 * @param load which are constrained to define load for bin i.
	 * @param w which define size of item i.
	 * @param minBin minimal index of a bin; overwrite the value provided by minimal index of variable bin 
	 */
	public BinpackingCloneable(IntVarCloneable[] bin, IntVarCloneable[] load, int[] w, int minBin) {
		super(bin, load, w, minBin);
	}

	/**
	 * It constructs the binpacking constraint for the supplied variable.
	 * @param bin which are constrained to define bin for item i.
	 * @param load which are constrained to define load for bin i.
	 * @param w which define size of item i.
	 * @param minBin minimal index of a bin; overwrite the value provided by minimal index of variable bin 
	 */
	public BinpackingCloneable(ArrayList<? extends IntVarCloneable> bin, ArrayList<? extends IntVarCloneable> load, int[] w, int minBin) {
		super(bin, load, w, minBin);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public BinpackingCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable[] bin2 = new IntVarCloneable [this.item.length];
		int[] w2 = new int [this.item.length];
		for (int i = this.item.length - 1; i >= 0; i--) {
			if ((bin2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.item[i].bin)).dom().isEmpty()) 
				throw Store.failException;
			w2[i] = this.item[i].weight;
		}
		
		IntVarCloneable[] load2 = new IntVarCloneable [this.load.length];
		for (int i = this.load.length - 1; i >= 0; i--) 
			if ((load2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.load[i])).dom().isEmpty()) 
				throw Store.failException;
		
		return new BinpackingCloneable (bin2, load2, w2);
	}
	
}
