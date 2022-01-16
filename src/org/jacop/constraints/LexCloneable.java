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
import java.util.Arrays;

import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Lex constraint
 * @author Thomas Leaute
 */
public class LexCloneable extends Lex implements ConstraintCloneableInterface<LexCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

    /**
     * It creates a lexicographical order for vectors x[i], i.e. 
     * forall i, exists j : x[i][k] = x[i+1][k] for k \< j and x[i][k] \<= x[i+1][k]
     * for k \>= j
     *
     * vectors x[i] does not need to be of the same size.
     *
     * @param x vector of vectors which assignment is constrained by Lex constraint. 
     */
	public LexCloneable(IntVarCloneable[][] x) {
		super(x);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

    /**
     * It creates a lexicographical order for vectors x[i], i.e. 
     * forall i, exists j : x[i][k] = x[i+1][k] for k \< j and x[i][k] \<= x[i+1][k]
     * for k \>= j
     *
     * vectors x[i] does not need to be of the same size.
     *
     * @param x 	vector of vectors which assignment is constrained by Lex constraint. 
     * @param lt 	defines if we require Lex_{\<} (lt = false) or Lex_{\<=} (lt = true)
     */
	public LexCloneable(IntVarCloneable[][] x, boolean lt) {
		super(x, lt);
		this.id = this.getClass().getSimpleName() + idNbr++;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public LexCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the x double array
		IntVarCloneable[][] x2 = new IntVarCloneable [this.x.length][];
		IntVar[] xi, x2i;
		for (int i = this.x.length - 1; i >= 0; i--) {
			xi = this.x[i];
			x2i = x2[i] = new IntVarCloneable [xi.length];
			for (int j = xi.length - 1; j >= 0; j--) 
				if ((x2i[j] = targetStore.findOrCloneInto((IntVarCloneable) xi[j])).dom().isEmpty()) 
					throw StoreCloneable.failException;
		}
		
		return new LexCloneable (x2, this.lexLT);
	}

	/** @see DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public ArrayList<IntVar> arguments() {
		
		ArrayList<IntVar> args = new ArrayList<IntVar> ();
		
		for (int i = this.x.length - 1; i >= 0; i--) 
			args.addAll(Arrays.asList(this.x[i]));
		
		return args;
	}

	/** @see DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}
}
