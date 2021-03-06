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

package frodo2.algorithms.dpop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;

import frodo2.algorithms.AgentInterface;
import frodo2.algorithms.SolutionCollector;
import frodo2.algorithms.dpop.UTILpropagation.SolutionMessage;
import frodo2.algorithms.varOrdering.dfs.DFSgeneration;
import frodo2.algorithms.varOrdering.dfs.DFSgeneration.DFSview;
import frodo2.communication.IncomingMsgPolicyInterface;
import frodo2.communication.Message;
import frodo2.communication.MessageType;
import frodo2.communication.Queue;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.BasicUtilitySolutionSpace;
import frodo2.solutionSpaces.DCOPProblemInterface;

/** VALUE propagation protocol
 * @author Thomas Leaute
 * @param <Val> type used for variable values
 * @todo Improve the implementation by reasoning on groups of variables to be projected together. 
 */
public class VALUEpropagation < Val extends Addable<Val> > 
implements IncomingMsgPolicyInterface<MessageType> {

	/** The type of the message telling the module to start */
	public static MessageType START_MSG_TYPE = AgentInterface.START_AGENT;
	
	/** The type of the message telling the agent finished */
	public static MessageType FINISH_MSG_TYPE = AgentInterface.AGENT_FINISHED;

	/** The type of the messages containing information about the DFS */
	public static MessageType DFS_MSG_TYPE = DFSgeneration.OUTPUT_MSG_TYPE;
	
	/** The type of the messages containing optimal conditional assignments */
	public static MessageType UTIL_MSG_TYPE = UTILpropagation.OUTPUT_MSG_TYPE;
	
	/** The type of the messages containing information about separators */
	public static MessageType SEPARATOR_MSG_TYPE = UTILpropagation.SEPARATOR_MSG_TYPE;

	/** The type of the VALUE messages */
	public static final MessageType VALUE_MSG_TYPE = new MessageType ("DPOP", "VALUEpropagation", "VALUE");
	
	/** The queue on which it should call sendMessage() */
	protected Queue queue;
	
	/** The problem */
	protected DCOPProblemInterface<Val, ?> problem;
	
	/** Whether the execution of the algorithm has started */
	protected boolean started = false;
	
	/** For each variable, the VALUE message received containing its separator's optimal assignments */
	private HashMap< String, VALUEmsg<Val> > valueMessages;
	
	/** For each variable, its separator */
	protected HashMap<String, String[]> separators;
	
	/** For each variable, the list of its children */
	protected HashMap< String, List<String> > allChildren;
	
	/** The parent for each of my variables */
	private HashMap<String, String> allParents;
	
	/** For each variable, its optimal assignment conditioned on the assignments to its separator */
	protected HashMap< String, SolutionMessage<Val> > condAssignments;
	
	/** For each variable, the file containing its optimal assignment conditioned on the assignments to its separator */
	private HashMap<String, String> condAssignmentsFiles;
	
	/** For each variable, its optimal value */
	private HashMap<String, Val> solution;
	
	/** The number of variables owned by this agents that have already sent VALUE messages to all their children */
	protected int nbrVarsDone = 0;
	
	/** If \c true, conditional optimal assignments are swapped until the VALUE message is received */
	private final boolean swap;
	
	/** How many variables there are in each cluster, identified by its root */
	private HashMap<String, Integer> clusterSizes = new HashMap<String, Integer> ();
	
	/** For each child variable, its corresponding agent */
	private HashMap<String, String> owners = new HashMap<String, String> ();
	
	/** Default constructor */
	public VALUEpropagation () {
		this.swap = false;
	}
	
	/** Manual constructor that does not use XML elements
	 * @param problem 		the problem
	 * @param swap 			if \c true, conditional optimal assignments are swapped until the VALUE message is received
	 */
	public VALUEpropagation (DCOPProblemInterface<Val, ?> problem, Boolean swap) {
		this.problem = problem;
		this.swap  = swap;
	}
	
	/** Constructor from XML elements
	 * @param problem 		description of the problem
	 * @param parameters 	the parameters of the module
	 */
	public VALUEpropagation (DCOPProblemInterface<Val, ?> problem, Element parameters) {
		this.problem = problem;
		if(parameters != null) 
			this.swap = Boolean.parseBoolean(parameters.getAttributeValue("swap"));
		else 
			this.swap = false;
	}
	
	/** Parses the problem */
	protected void init () {
		this.allChildren = new HashMap< String, List<String> > ();
		this.allParents = new HashMap<String, String> ();
		this.condAssignments = new HashMap< String, SolutionMessage<Val> > ();
		this.condAssignmentsFiles = new HashMap<String, String> ();
		this.separators = new HashMap<String, String[]> ();
		this.solution = new HashMap<String, Val> ();
		this.valueMessages = new HashMap< String, VALUEmsg<Val> > ();
		this.started = true;
	}
	
	/** Resets the state, only keeping the problem */
	public void reset () {
		this.allChildren = new HashMap< String, List<String> > ();
		this.allParents = new HashMap<String, String> ();
		this.condAssignments = new HashMap< String, SolutionMessage<Val> > ();
		this.condAssignmentsFiles = new HashMap<String, String> ();
		this.separators = new HashMap<String, String[]> ();
		this.solution = new HashMap<String, Val> ();
		this.valueMessages = new HashMap< String, VALUEmsg<Val> > ();
		this.started = false;
		this.nbrVarsDone = 0;
	}

	/** @see IncomingMsgPolicyInterface#getMsgTypes() */
	public Collection <MessageType> getMsgTypes() {
		ArrayList<MessageType> types = new ArrayList<MessageType> (6);
		types.add(START_MSG_TYPE);
		types.add(UTIL_MSG_TYPE);
		types.add(SEPARATOR_MSG_TYPE);
		types.add(VALUE_MSG_TYPE);
		types.add(DFS_MSG_TYPE);
		types.add(FINISH_MSG_TYPE);
		return types;
	}

	/** The algorithm
	 * @see IncomingMsgPolicyInterface#notifyIn(Message)
	 */
	@SuppressWarnings("unchecked")
	public void notifyIn(Message msg) {
		
		MessageType type = msg.getType();
		
		if (type.equals(FINISH_MSG_TYPE)) {
			this.reset();
			return;
		}
		
		// Parse the problem if this hasn't been done yet
		if (! this.started) 
			init();
		
		if (type.equals(UTIL_MSG_TYPE)) { // the message contains the conditional optimal value assignments to a variable
			
			// Extract the information from the message
			SolutionMessage<Val> msgCast = (SolutionMessage<Val>) msg;
			String[] vars = msgCast.getVariables();
			String clusterID = msgCast.getClusterID();
			BasicUtilitySolutionSpace< Val, ArrayList<Val> > optAssignments = msgCast.getCondOptAssignments();
			
			// Check if this variable is a root
			if (this.allParents.containsKey(clusterID) && this.allParents.remove(clusterID) == null) {
				
				// Simulate the reception of an empty VALUE message for this root variable
				this.queue.sendMessageToSelf(new VALUEmsg<Val> (clusterID, new String[0], null));
			}
			
			// Check whether the VALUE message for this variable has been received
			VALUEmsg<Val> valueMsg = valueMessages.remove(clusterID);
			if (valueMsg != null) {
				
				// Compute the optimal value for this variable, and send VALUE messages to its children (if possible)
				computeOptValAndSendVALUEmsgs(vars, optAssignments, valueMsg);
			}
			
			else { // the VALUE message for this variable has not been received yet
				
				// Store the conditional optimal assignments until we receive the VALUE message
				/// @todo Swap the optimal assignments BEFORE sending up the UTIL message, and on the fly. 
				if (this.swap) {
					try {
						// First delete possibly existing file
						String fileName = ".condAssignments_" + clusterID;
						new File (fileName).delete();
						
						// Write the message to a file
						FileOutputStream fout = new FileOutputStream(fileName);
						ObjectOutputStream oos = new ObjectOutputStream(fout);
						oos.writeObject(msgCast);
						oos.close();
						
						this.condAssignmentsFiles.put(clusterID, fileName);
					}
					catch (Exception e) {
						System.err.println("Unable to swap the conditional optimal assignments for variable " + clusterID);
						e.printStackTrace();
					}
				} else 
					condAssignments.put(clusterID, msgCast);
			}
		}

		else if (type.equals(SEPARATOR_MSG_TYPE)) { // the message contains the separator for a given child variable
			
			// Extract the information from the message
			UTILpropagation.SeparatorMessage msgCast = (UTILpropagation.SeparatorMessage) msg;
			String parent = msgCast.getParent();
			String child = msgCast.getChild();
			assert msgCast.getLowerAgent() != null;
			this.owners.put(child, msgCast.getLowerAgent());
			
			// Check whether we have already computed the optimal assignment to this variable
			if (this.solution.containsKey(parent)) {
				
				// Compute and send the VALUE message
				sendVALUEmessage(child, msgCast.getSeparator(), this.solution, valueMessages.remove(parent));
				
				// Remove the child from the list of children
				List<String> children = allChildren.get(parent);
				children.remove(child);
				
				// Check if the agent can terminate
				if (children.isEmpty()) {
					this.nbrVarsDone += this.clusterSizes.get(parent);
					if (this.nbrVarsDone >= this.problem.getNbrIntVars()) 
						this.queue.sendMessageToSelf(new Message (FINISH_MSG_TYPE));
				}
			}
			
			else { // we haven't been able to compute the optimal assignment to the parent yet
				
				// Store the separator until we have enough information to compute the corresponding VALUE message
				separators.put(child, msgCast.getSeparator());
			}			
		}
		
		else if (type.equals(VALUE_MSG_TYPE)) { // the VALUE message
			
			// Extract the information from the message
			VALUEmsg<Val> msgCast = (VALUEmsg<Val>) msg;
			String var = msgCast.getDest();
			
			// Check whether we have received the conditional optimal assignments for this variable
			SolutionMessage<Val> solMsg = null;
			if (this.swap) {
				
				// Check if there is a file to read
				String fileName = this.condAssignmentsFiles.remove(var);
				if (fileName != null) {
					try {
						// Read the message from the file
						File file = new File (fileName);
						FileInputStream fin = new FileInputStream(file);
						ObjectInputStream ois = new ObjectInputStream(fin);
						solMsg = (SolutionMessage<Val>) ois.readObject();
						ois.close();
						
						// Delete the file
						file.delete();
						
					} catch (Exception e) {
						System.err.println("Unable to read the conditional optimal assignments for variable " + var + " from file " + fileName);
						e.printStackTrace();
					}
				}
			} else 
				solMsg = condAssignments.remove(var);
			
			if (solMsg != null) {
				
				// Compute the optimal value for this variable, and send VALUE messages to its children (if possible)
				computeOptValAndSendVALUEmsgs(solMsg.getVariables(), solMsg.getCondOptAssignments(), msgCast);
			}
			
			else { // we haven't received the conditional optimal assignments for this variable yet
				
				// Store the VALUE message until we can make use of it
				valueMessages.put(var, msgCast);
			}
		}
		
		else if (type.equals(DFS_MSG_TYPE)) { // the message contains information about the children of one of my variables
			
			// Extract and record the information from the message
			DFSgeneration.MessageDFSoutput<Val, ?> msgCast = (DFSgeneration.MessageDFSoutput<Val, ?>) msg;
			DFSview<Val, ?> relationships = msgCast.getNeighbors();
			String clusterID = relationships.getID();
			this.allChildren.put(clusterID, new ArrayList<String> (relationships.getChildren()));
			this.allParents.put(clusterID, relationships.getParent());
			this.clusterSizes.put(clusterID, msgCast.getVars().length);
		}
		
	}

	/** @see IncomingMsgPolicyInterface#setQueue(Queue) */
	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	/** Instantiates a VALUE message and sends it
	 * @param child 		destination variable of the message
	 * @param separator 	variables to be mentioned in the message
	 * @param optVals 		the optimal values for some variables
	 * @param valueMsg 		VALUE message received from parent 
	 */
	@SuppressWarnings("unchecked")
	private void sendVALUEmessage(String child, String[] separator, HashMap<String, Val> optVals, VALUEmsg<Val> valueMsg) {
		
		String[] variablesIn = valueMsg.getVariables();
		Val[] valuesIn = valueMsg.getValues();
		
		// Store all the optimal values in a HashMap
		HashMap<String, Val> allOptVals = new HashMap<String, Val> ();
		for (int i = variablesIn.length - 1; i >= 0; i--) 
			allOptVals.put(variablesIn[i], valuesIn[i]);
		allOptVals.putAll(optVals);
		
		// Fill in the array of assignments
		assert separator != null;
		Val[] valuesOut = (Val[]) Array.newInstance(this.problem.getDomClass(), separator.length);
		for (int i = 0; i < separator.length; i++) 
			valuesOut[i] = allOptVals.get(separator[i]);
		
		// Send the corresponding new VALUE message
		assert this.owners.get(child) != null;
		queue.sendMessage(this.owners.get(child), new VALUEmsg<Val>(child, separator, valuesOut));
	}

	/** Compute the optimal assignments to some variables, and sends VALUE messages to children accordingly
	 * @param vars 				the variables to be optimized
	 * @param optAssignments 	the conditional optimal assignments to the variables
	 * @param valueMsg 			the VALUE message received for the variables
	 */
	private void computeOptValAndSendVALUEmsgs(String[] vars, BasicUtilitySolutionSpace< Val, ArrayList<Val> > optAssignments, 
			VALUEmsg<Val> valueMsg) {
		
		if (vars.length > 0) { // Compute the optimal assignments to the variables, record them and output them
			
			ArrayList<Val> optVals = optAssignments.getUtility(valueMsg.getVariables(), valueMsg.getValues());
			assert optVals != null : "Assignment " + Arrays.toString(valueMsg.getVariables()) + " = " + Arrays.toString(valueMsg.getValues()) + 
					" has no correspondence in \n" + optAssignments;
			
			for (int i = 0; i < vars.length; i++) {
				Val value = optVals.get(i);
				if (value != null) 
					solution.put(vars[i], value);
			}
			
			queue.sendMessage(AgentInterface.STATS_MONITOR, new SolutionCollector.AssignmentsMessage<Val> (vars, optVals));
		}
		
		// Go through the list of children 
		// For each child, check if we already know its separator 
		List<String> children = allChildren.get(valueMsg.getDest());
		assert children != null : this.allChildren + " has no children for the destination of " + valueMsg;
		for (Iterator<String> iterator = children.iterator(); iterator.hasNext(); ) {
			String child = iterator.next();
			String[] separator = separators.remove(child);
			if (separator != null) {

				// Send VALUE message to this child
				sendVALUEmessage(child, separator, this.solution, valueMsg);

				// Remove the child from the list of children
				iterator.remove();
			}
		}
		
		// Check if the agent can terminate
		if (children.isEmpty()) {
			
			// How many of this cluster's variables have been assigned values
			this.nbrVarsDone += this.clusterSizes.get(valueMsg.getDest());
			
			if (this.nbrVarsDone >= this.problem.getNbrIntVars()) 
				this.queue.sendMessageToSelf(new Message (FINISH_MSG_TYPE));
		}
	}
	
}
