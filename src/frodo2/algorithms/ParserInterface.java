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

package frodo2.algorithms;

import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.ProblemInterface;

/** Interface for a parser that produces a ProblemInterface
 * @author Thomas Leaute
 * @param <V> the type of variable values
 * @param <U> the type of utility/cost values
 */
public interface ParserInterface < V extends Addable<V>, U extends Addable<U> > {
	
	/** @return the parsed problem instance */
	public ProblemInterface<V, U> parse ();

}
