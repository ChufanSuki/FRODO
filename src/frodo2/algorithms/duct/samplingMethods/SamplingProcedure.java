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

package frodo2.algorithms.duct.samplingMethods;

import frodo2.algorithms.duct.SearchNode;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableReal;

/**
 * @author Brammert Ottens, Oct 24, 2011
 * @param <V> type used for domain values
 * 
 */
public abstract class SamplingProcedure <V extends Addable<V>> {
	
	/**
	 * 
	 * Method samples the distribution \c dist
	 * 
	 * @author Brammert Ottens, Oct 24, 2011
	 * @param dist the distribution to sample
	 * @return	the value that is to be sampled
	 */
	public int sampling(SearchNode<V> dist) {
		if(dist.feasible)
			dist.actionFrequencies[dist.maxBoundIndex]++;
		
		return dist.maxBoundIndex;
	}
	
	/**
	 * Processes the last received sample
	 * and determines the appropriate statistics
	 * 
	 * @author Brammert Ottens, Oct 24, 2011
	 * @param dist the distribution
	 * @param infeasibleUtility utility for infeasible assignments
	 * @param maximize \c true when maximizing, and \c false when minimizing
	 */
	public abstract void processSample(SearchNode<V> dist, AddableReal infeasibleUtility, final boolean maximize);
	
}
