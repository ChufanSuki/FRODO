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

package org.jacop.util;

import org.jacop.core.IntVarCloneable;

/** A version of MDD that requires cloneable variables
 * @author Thomas Leaute
 */
public class MDDCloneable extends MDD {

	/**
	 * It creates an MDD. Please note that diagram argument
	 * which is potentially a very large array and can be 
	 * used across many constraints is not copied by the 
	 * constructor but used directly. 
	 * 
	 * @param vars variables involved in this multiple-value decision diagram.
	 * @param diagram an int array representation of the diagram.
	 * @param domainLimits the limits on the number of values imposed on each variable.
	 */
	public MDDCloneable(IntVarCloneable[] vars, int[] diagram, int[] domainLimits) {
		super(vars, diagram, domainLimits);
	}

	/**
	 * It creates and MDD representation given the list of variables
	 * and (dis)allowed tuples. Minimum domain limits allows artificially
	 * increase the size of the variable domain to make reuse of the same 
	 * mdd across multiple constraints possible.
	 * 
	 * @param vars variables and their order used in the MDD. 
	 * @param minimumDomainLimits it specifies the minimal number of values used for each of the variables.
	 * @param table it specifies the allowed tuples which are being converted into an MDD.
	 */
	public MDDCloneable(IntVarCloneable[] vars, int[] minimumDomainLimits, int[][] table) {
		super(vars, minimumDomainLimits, table);
	}

	/**
	 * It creates and MDD representation given the list of variables
	 * and (dis)allowed tuples. Minimum domain limits allows artificially
	 * increase the size of the variable domain to make reuse of the same 
	 * mdd across multiple constraints possible.
	 * 
	 * @param vars variables and their order used in the MDD. 
	 * @param table it specifies the allowed tuples which are being converted into an MDD.
	 */
	public MDDCloneable(IntVarCloneable[] vars, int[][] table) {
		super(vars, table);
	}

	/**
	 * It creates and MDD representation given the list of variables.
	 * The domain limits are set to be equal to the size of the variables domains.
	 * The tuples are being added separately one by one.
	 * 
	 * @param vars variables and their order used in the MDD. 
	 */
	public MDDCloneable(IntVarCloneable[] vars) {
		super(vars);
	}

	/** Constructor
	 * @param diagram 		the diagram
	 * @param domainLimits 	the domain limits
	 * @param freePosition 	the free position
	 * @param vars 			the variables
	 * @param views 		the views
	 */
	public MDDCloneable(int[] diagram, int[] domainLimits, int freePosition, IntVarCloneable[] vars, IndexDomainView[] views) {
		this.diagram = diagram;
		this.domainLimits = domainLimits;
		this.freePosition = freePosition;
		this.vars = vars;
		this.views = views;
	}

}
