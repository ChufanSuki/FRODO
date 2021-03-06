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

package frodo2.algorithms.duct;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import frodo2.algorithms.Solution;
import frodo2.communication.MessageType;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableReal ;

/**
 * Solution class for the family of DUCT algorithms
 * 
 * @author brammertottens
 *
 * @param <V>	type used for domain values
 */
public class DUCTSolution <V extends Addable<V>> extends Solution<V, AddableReal > {

	/** The final bound on the solution quality */
	AddableReal finalBound;
	
	/** Constructor 
	 * @param nbrVars 			the number of variables in the problem
	 * @param reportedUtil 		the reported optimal utility
	 * @param trueUtil 			the true optimal utility
	 * @param finalBound		the final bound on the solution quality
	 * @param assignments 		the optimal assignments
	 * @param nbrMsgs			The total number of messages sent
	 * @param totalMsgSize		The total message size
	 * @param maxMsgSize 		the size (in bytes) of the largest message
	 * @param ncccCount 		the ncccs used
	 * @param timeNeeded 		the time needed to solve the problem
	 * @param moduleEndTimes 	each module's end time
	 */
	public DUCTSolution (int nbrVars, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, int nbrMsgs, long totalMsgSize, long maxMsgSize, long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes) {
		this(nbrVars, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, null, null, null, totalMsgSize, null, null, null, maxMsgSize, null, ncccCount, timeNeeded, -1, moduleEndTimes, -1, 0);
	}
	/** Constructor 
	 * @param nbrVariables		the total number of variables in the problem
	 * @param reportedUtil 		the reported optimal utility
	 * @param trueUtil 			the true optimal utility
	 * @param finalBound		the final bound on the solution quality
	 * @param assignments 		the optimal assignments
	 * @param nbrMsgs			The total number of messages sent
	 * @param totalMsgSize		The total message size
	 * @param maxMsgSize 		the size (in bytes) of the largest message
	 * @param ncccCount 		the ncccs used
	 * @param timeNeeded 		the time needed to solve the problem
	 * @param moduleEndTimes 	each module's end time
	 * @param numberOfCoordinationConstraints the number of constraints that contain variables that are owned by different agents
	 */
	public DUCTSolution (int nbrVariables, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, int nbrMsgs, long totalMsgSize, long maxMsgSize, 
			long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes, int numberOfCoordinationConstraints) {
		this(nbrVariables, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, null, null, null, totalMsgSize, null, null, null, maxMsgSize, null, ncccCount, timeNeeded, -1, moduleEndTimes, -1, numberOfCoordinationConstraints);
	}
	
	/** Constructor 
	 * @param nbrVariables		the total number of variables in the problem
	 * @param reportedUtil 		the reported optimal utility
	 * @param trueUtil 			the true optimal utility
	 * @param finalBound		the final bound on the solution quality
	 * @param assignments 		the optimal assignments
	 * @param nbrMsgs			The total number of messages sent
	 * @param totalMsgSize		The total message size
	 * @param maxMsgSize 		the size (in bytes) of the largest message
	 * @param ncccCount 		the ncccs used
	 * @param timeNeeded 		the time needed to solve the problem
	 * @param moduleEndTimes 	each module's end time
	 * @param numberOfCoordinationConstraints the number of constraints that contain variables that are owned by different agents
	 * @param treeWidth 		the width of the tree on which the algorithm has run 
	 */	
	public DUCTSolution (int nbrVariables, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, int nbrMsgs, long totalMsgSize, long maxMsgSize, long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes, int numberOfCoordinationConstraints, int treeWidth) {
		this(nbrVariables, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, null, null, null, totalMsgSize, null, null, null, maxMsgSize, null, ncccCount, timeNeeded, -1, moduleEndTimes, treeWidth, numberOfCoordinationConstraints);
	}
	
	/** Constructor 
	 * @param nbrVars 							the number of variables in the problem
	 * @param reportedUtil 						the reported optimal utility
	 * @param trueUtil 							the true optimal utility
	 * @param finalBound						the final bound on the solution quality
	 * @param assignments 						the optimal assignments
	 * @param nbrMsgs							The total number of messages sent
	 * @param msgsNbrsSentPerAgent 				the number of message sent by each agent
	 * @param msgsNbrsReceivedPerAgent 			the number of messages received by each agent
	 * @param msgNbrs							The number of messages sent per message type
	 * @param totalMsgSize						The total message size
	 * @param msgSizes 							The amount of information sent per message type
	 * @param msgSizesSentPerAgent 				the amount of information sent by each agent, in bytes
	 * @param msgSizesReveivedPerAgent 			the amount of information received by each agent, in bytes
	 * @param maxMsgSize 						the size (in bytes) of the largest message
	 * @param maxMsgSizes 						for each message type, the size (in bytes) of the largest message of that type
	 * @param ncccCount 						the ncccs used
	 * @param timeNeeded 						the time needed to solve the problem
	 * @param moduleEndTimes 					each module's end time
	 */
	public DUCTSolution (int nbrVars, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, 
			int nbrMsgs, TreeMap<MessageType, Integer> msgNbrs, TreeMap<Object, Integer> msgsNbrsSentPerAgent, TreeMap<Object, Integer> msgsNbrsReceivedPerAgent, 
			long totalMsgSize, TreeMap<MessageType, Long> msgSizes, TreeMap<Object, Long> msgSizesSentPerAgent, TreeMap<Object, Long> msgSizesReveivedPerAgent, 
			long maxMsgSize, TreeMap<MessageType, Long> maxMsgSizes, long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes) {
		this(nbrVars, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, msgNbrs, msgsNbrsSentPerAgent, msgsNbrsReceivedPerAgent, totalMsgSize, msgSizes, msgSizesSentPerAgent, msgSizesReveivedPerAgent, maxMsgSize, maxMsgSizes, ncccCount, timeNeeded, -1, moduleEndTimes, -1, 0);
	}
	
	/** Constructor 
	 * @param nbrVariables						the total number of variables in the problem
	 * @param reportedUtil 						the reported optimal utility
	 * @param trueUtil 							the true optimal utility
	 * @param finalBound						the final bound on the solution quality
	 * @param assignments 						the optimal assignments
	 * @param nbrMsgs							The total number of messages sent
	 * @param msgNbrs							The number of messages sent per message type
	 * @param msgsNbrsSentPerAgent 				the number of message sent by each agent
	 * @param msgsNbrsReceivedPerAgent 			the number of messages received by each agent
	 * @param totalMsgSize						The total message size
	 * @param msgSizes 							The amount of information sent per message type
	 * @param msgSizesSentPerAgent 				the amount of information sent by each agent, in bytes
	 * @param msgSizesReveivedPerAgent 			the amount of information received by each agent, in bytes
	 * @param maxMsgSize 						the size (in bytes) of the largest message
	 * @param maxMsgSizes 						for each message type, the size (in bytes) of the largest message of that type
	 * @param ncccCount 						the ncccs used
	 * @param timeNeeded 						the time needed to solve the problem
	 * @param moduleEndTimes 					each module's end time
	 * @param numberOfCoordinationConstraints 	the number of constraints that contain variables that are owned by different agents
	 */
	public DUCTSolution (int nbrVariables, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, 
			int nbrMsgs, TreeMap<MessageType, Integer> msgNbrs, TreeMap<Object, Integer> msgsNbrsSentPerAgent, TreeMap<Object, Integer> msgsNbrsReceivedPerAgent, 
			long totalMsgSize, TreeMap<MessageType, Long> msgSizes, TreeMap<Object, Long> msgSizesSentPerAgent, TreeMap<Object, Long> msgSizesReveivedPerAgent, 
			long maxMsgSize, TreeMap<MessageType, Long> maxMsgSizes, long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes, int numberOfCoordinationConstraints) {
		this(nbrVariables, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, msgNbrs, msgsNbrsSentPerAgent, msgsNbrsReceivedPerAgent, totalMsgSize, msgSizes, msgSizesSentPerAgent, msgSizesReveivedPerAgent, maxMsgSize, maxMsgSizes, ncccCount, timeNeeded, -1, moduleEndTimes, -1, numberOfCoordinationConstraints);
	}
	
	/** Constructor 
	 * @param nbrVariables						the total number of variables in the problem
	 * @param reportedUtil 						the reported optimal utility
	 * @param trueUtil 							the true optimal utility
	 * @param finalBound						the final bound on the solution quality
	 * @param assignments 						the optimal assignments
	 * @param nbrMsgs							The total number of messages sent
	 * @param msgNbrs							The number of messages sent per message type
	 * @param msgsNbrsSentPerAgent 				the number of message sent by each agent
	 * @param msgsNbrsReceivedPerAgent 			the number of messages received by each agent
	 * @param totalMsgSize						The total message size
	 * @param msgSizes 							The amount of information sent per message type
	 * @param msgSizesSentPerAgent 				the amount of information sent by each agent, in bytes
	 * @param msgSizesReveivedPerAgent 			the amount of information received by each agent, in bytes
	 * @param maxMsgSize 						the size (in bytes) of the largest message
	 * @param maxMsgSizes 						for each message type, the size (in bytes) of the largest message of that type
	 * @param ncccCount 						the ncccs used
	 * @param timeNeeded 						the time needed to solve the problem
	 * @param moduleEndTimes 					each module's end time
	 * @param treeWidth 						the width of the tree on which the algorithm has run
	 * @param numberOfCoordinationConstraints 	the number of constraints that contain variables that are owned by different agents
	 */
	public DUCTSolution (int nbrVariables, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, 
			int nbrMsgs, TreeMap<MessageType, Integer> msgNbrs, TreeMap<Object, Integer> msgsNbrsSentPerAgent, TreeMap<Object, Integer> msgsNbrsReceivedPerAgent, 
			long totalMsgSize, TreeMap<MessageType, Long> msgSizes, TreeMap<Object, Long> msgSizesSentPerAgent, TreeMap<Object, Long> msgSizesReveivedPerAgent, 
			long maxMsgSize, TreeMap<MessageType, Long> maxMsgSizes, long ncccCount, long timeNeeded, HashMap<String, Long> moduleEndTimes, int treeWidth, int numberOfCoordinationConstraints) {
		this(nbrVariables, reportedUtil, trueUtil, finalBound, assignments, nbrMsgs, msgNbrs, msgsNbrsSentPerAgent, msgsNbrsReceivedPerAgent, totalMsgSize, msgSizes, msgSizesSentPerAgent, msgSizesReveivedPerAgent, maxMsgSize, maxMsgSizes, ncccCount, timeNeeded, -1, moduleEndTimes, treeWidth, numberOfCoordinationConstraints);
	}
	
	/** Constructor 
	 * @param nbrVariables						the total number of variables in the problem
	 * @param reportedUtil 						the reported optimal utility
	 * @param trueUtil 							the true optimal utility
	 * @param finalBound						the final bound on the solution quality
	 * @param assignments 						the optimal assignments
	 * @param nbrMsgs							The total number of messages sent
	 * @param msgNbrs							The number of messages sent per message type
	 * @param msgsNbrsSentPerAgent 				the number of message sent by each agent
	 * @param msgsNbrsReceivedPerAgent 			the number of messages received by each agent
	 * @param totalMsgSize						The total message size
	 * @param msgSizes 							The amount of information sent per message type
	 * @param msgSizesSentPerAgent 				the amount of information sent by each agent, in bytes
	 * @param msgSizesReveivedPerAgent 			the amount of information received by each agent, in bytes
	 * @param maxMsgSize 						the size (in bytes) of the largest message
	 * @param maxMsgSizes 						for each message type, the size (in bytes) of the largest message of that type
	 * @param ncccCount 						the ncccs used
	 * @param timeNeeded 						the time needed to solve the problem
	 * @param cumulativeTime					the cumulative time needed by all the agents to terminate
	 * @param moduleEndTimes 					each module's end time
	 * @param treeWidth 						the width of the tree on which the algorithm has run
	 * @param numberOfCoordinationConstraints 	the number of constraints that contain variables that are owned by different agents
	 */
	public DUCTSolution (int nbrVariables, AddableReal reportedUtil, AddableReal trueUtil, AddableReal finalBound, Map<String, V> assignments, 
			int nbrMsgs, TreeMap<MessageType, Integer> msgNbrs, TreeMap<Object, Integer> msgsNbrsSentPerAgent, TreeMap<Object, Integer> msgsNbrsReceivedPerAgent, 
			long totalMsgSize, TreeMap<MessageType, Long> msgSizes, TreeMap<Object, Long> msgSizesSentPerAgent, TreeMap<Object, Long> msgSizesReveivedPerAgent, 
			long maxMsgSize, TreeMap<MessageType, Long> maxMsgSizes, long ncccCount, long timeNeeded, long cumulativeTime, HashMap<String, Long> moduleEndTimes, int treeWidth, int numberOfCoordinationConstraints) {
		super(nbrVariables, reportedUtil, trueUtil, assignments, nbrMsgs, msgNbrs, msgsNbrsSentPerAgent, msgsNbrsReceivedPerAgent, totalMsgSize, msgSizes, msgSizesSentPerAgent, msgSizesReveivedPerAgent, maxMsgSize, maxMsgSizes, ncccCount, timeNeeded, cumulativeTime, moduleEndTimes, treeWidth, numberOfCoordinationConstraints);
		this.finalBound = finalBound;
	}
	
	/**
	 * @author Brammert Ottens, Dec 29, 2011
	 * @return the final bound on the solution quality
	 */
	public AddableReal getFinalBound() {
		return this.finalBound;
	}
	
	/** @see java.lang.Object#toString() */
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder (super.toString());
		
		if (this.finalBound != null) {
			builder.append("\n");
			builder.append("\n\t- final bound: \t:" + this.finalBound);
		}
		
		return builder.toString();
	}
}
