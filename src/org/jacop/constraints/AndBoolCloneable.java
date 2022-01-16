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
import java.util.List;

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.core.Var;

/** A cloneable version of the AndBool constraint 
 * @author Thomas Leaute
 */
public class AndBoolCloneable extends AndBool implements ConstraintCloneableInterface<AndBoolCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

	/** Constructor 
	 * @param vars 		the list of variables
	 * @param result 	the result variable
	 */
	public AndBoolCloneable(IntVarCloneable[] vars, IntVarCloneable result) {
		super(vars, result);
		this.id = this.getClass().getSimpleName() + idNbr++;
		
        IntVarCloneable[] r = filter(vars);

        if (r == null)
            c = new XeqCCloneable(result, 0);
        else if (r.length == 1)
            c = new XeqYCloneable(r[0], result);
        else if (r.length == 2) 
            c = new AndBoolSimpleCloneable(r[0], r[1], result);
        else
            c = new AndBoolVectorCloneable(r, result);
	}

	/** Filters the input list of variables to remove the ones with min() == 1
	 * @param xs the variables
	 * @return a filtered list, or null of one of the variables has max() == 0
	 */
	public static IntVarCloneable[] filter(IntVarCloneable[] xs) {
		
        List<IntVarCloneable> result = new ArrayList<IntVarCloneable> ();
        for (IntVarCloneable x : xs)
            if (x.max() == 0)
                return null;
            else if (x.min() == 1)
                continue;
            else
                result.add(x);

        return result.toArray(new IntVarCloneable [result.size()]);
    }

	/**
     * It constructs and constraint on variables.
     * @param a parameters variable.
     * @param result variable.
     */
	public AndBoolCloneable(ArrayList<IntVarCloneable> a, IntVarCloneable result) {
		this(a.toArray(new IntVarCloneable [a.size()]), result);
	}

    /**
     * It constructs and constraint on variables.
     * @param a parameter variable.
     * @param b parameter variable.
     * @param result variable.
     */
	public AndBoolCloneable(IntVarCloneable a, IntVarCloneable b, IntVarCloneable result) {
		this(new IntVarCloneable[] {a, b}, result);
	}

	/** 
	 * @see ConstraintCloneableInterface#cloneInto(StoreCloneable) 
	 * @throws CloneNotSupportedException if the underlying constraint is not cloneable (which should not happen)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AndBoolCloneable cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException, FailException {
		
		AndBoolCloneable out = new AndBoolCloneable(new IntVarCloneable [0], new IntVarCloneable (targetStore));
		out.c = ((ConstraintCloneableInterface<PrimitiveConstraint>) this.c).cloneInto(targetStore);
		
		if (out.c == null) // the constraint is already consistent
			return null;
		
		return out;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<? extends Var> arguments() {
		return new ArrayList<Var> (this.c.arguments());
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}
	
}
