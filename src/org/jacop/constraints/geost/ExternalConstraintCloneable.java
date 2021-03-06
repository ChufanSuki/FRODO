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

package org.jacop.constraints.geost;

import java.util.Map;

/** A cloneable version of ExternalConstraint
 * @author Thomas Leaute
 */
public interface ExternalConstraintCloneable extends ExternalConstraint {

	/** Creates a "clone" of this constraint, expressed over FDVs that can be retrieved from the input map
	 * @param objMap 		the FDVs, indexed by their IDs
	 * @return a "clone" of this constraint
	 */
	public ExternalConstraintCloneable cloneAndReplace(Map<Integer, GeostObjectCloneable> objMap);
}
