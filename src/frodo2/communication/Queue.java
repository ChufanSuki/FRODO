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

/** This package contains classes that take care of communication between queues */
package frodo2.communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import frodo2.algorithms.AgentInterface;
import frodo2.algorithms.StatsReporter;
import frodo2.communication.sharedMemory.QueueIOPipe;
import frodo2.controller.Controller;
import frodo2.daemon.Daemon;
import frodo2.solutionSpaces.ProblemInterface;

/** A queue
 * @author Thomas Leaute 
 */

public class Queue implements Runnable {
	
	/** The list of incoming messages waiting to be processed */
	private BlockingQueue<MessageWrapper> inbox;
	
	/** List of input pipes */
	protected Set <QueueInputPipeInterface> inputs;
	
	/** Map associating an output pipe to each destination ID */
	protected HashMap <Object, QueueOutputPipeInterface> outputs;
	
	/** A monitor used to measure the sizes of the messages */
	protected MsgSizeMonitor monitor;
	
	/** A list of listeners notified of incoming objects 
	 * 
	 * These listeners are called whenever there is a new incoming object, in order to decide what to do with it. 
	 * There is a list of listeners for each message type. 
	 */
	protected HashMap<MessageType, ArrayList< IncomingMsgPolicyInterface<MessageType> > > inPolicies;
	
	/** A list of listeners notified of outgoing messages 
	 * 
	 * These listeners are called whenever there is a new outgoing message, in order to decide what to do with it. 
	 * There is a list of listeners for each message type. 
	 */
	protected HashMap<MessageType, ArrayList< OutgoingMsgPolicyInterface<MessageType> > > outPolicies;
	
	/** Lock for outPolicies field*/
	private final ReentrantLock outPolicies_lock = new ReentrantLock();
	
	/** Used to tell the thread to stop */
	private boolean keepGoing = true;
	
	/** The queue's thread */
	private Thread myThread;
	
	/** The problem used to obtain the current NCCC count */
	protected ProblemInterface<?, ?> problem;
	
	/** Whether to measure the number of messages and the total amount of information sent */
	protected final boolean measureMsgs;
	
	/** For each message type, the number of messages sent of that type */
	protected HashMap<MessageType, Integer> msgNbrs;
	
	/** The number of messages sent to each other agent */
	private HashMap<Object, Integer> msgNbrsSent;
	
	/** For each message type, the total amount of information sent in messages of that type, in bytes */
	protected HashMap<MessageType, Long> msgSizes;
	
	/** The amount of information sent to each other agent, in bytes */
	private HashMap<Object, Long> msgSizesSent;
	
	/** For each message type, the size (in bytes) of the largest message of that type */
	protected HashMap<MessageType, Long> maxMsgSizes;
	
	/** lock for input field */
	private final ReentrantLock input_lock = new ReentrantLock();
	
	/** lock for output field*/
	private final ReentrantLock output_lock = new ReentrantLock();
	
	/** lock for inPolicies field*/
	private final ReentrantLock inPolicies_lock = new ReentrantLock();
	
	/** The last wrapped message to have been received */
	protected MessageWrapper msgWrap;
	
	/** Empty constructor that does absolutely \b nothing */
	protected Queue () {
		this.measureMsgs = true;
	}
	
	/** Constructor
	 * @param measureMsgs 			whether to measure the number of messages and the total amount of information sent 
	 */
	public Queue (boolean measureMsgs) {
		this (measureMsgs, true);
	}
	
	/** Constructor
	 * @param measureMsgs 	whether to measure the number of messages and the total amount of information sent 
	 * @param init 			whether the init() method should be called
	 */
	public Queue (boolean measureMsgs, boolean init) {
		
		if (init) 
			init();
		
		this.measureMsgs = measureMsgs;
		if (this.measureMsgs) {
			this.msgNbrs = new HashMap<MessageType, Integer> ();
			this.msgNbrsSent = new HashMap<Object, Integer> ();
			this.msgSizes = new HashMap<MessageType, Long> ();
			this.msgSizesSent = new HashMap<Object, Long> ();
			this.maxMsgSizes = new HashMap<MessageType, Long> ();
			try {
				this.monitor = new MsgSizeMonitor ();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Helper function called by the constructors to avoid code redundancy */
	private void init () {
		inbox = new LinkedBlockingQueue<MessageWrapper> ();
		inputs = new HashSet <QueueInputPipeInterface> ();
		outputs = new HashMap <Object, QueueOutputPipeInterface> ();
		
		inPolicies = new HashMap <MessageType, ArrayList< IncomingMsgPolicyInterface<MessageType> > > ();
		ArrayList< IncomingMsgPolicyInterface<MessageType> > policies = new ArrayList< IncomingMsgPolicyInterface<MessageType> >();
		inPolicies.put(MessageType.ROOT, policies);
		
		outPolicies = new HashMap <MessageType, ArrayList< OutgoingMsgPolicyInterface<MessageType> > > ();
		ArrayList< OutgoingMsgPolicyInterface<MessageType> > policiesOut = new ArrayList< OutgoingMsgPolicyInterface<MessageType> >();
		outPolicies.put(MessageType.ROOT, policiesOut);
		
		myThread = new Thread (this, "Queue");
		myThread.setDaemon(true);
		myThread.start();
	}
	
	/** Reminds the queue to close the given pipe when it is killed
	 * @param input pipe to be closed
	 */
	public void toBeClosedUponKill (QueueInputPipeInterface input) {
		try {
			input_lock.lock();
			inputs.add(input);
		} finally {
			input_lock.unlock();
		}
	}
	
	/** Removes the given input pipe from the list of input pipes
	 * @param input the input pipe to be removed
	 */
	public void removeInputPipe (QueueInputPipeInterface input) {
		try {
			input_lock.lock();
			inputs.remove(input);
		} finally {
			input_lock.unlock();
		}
	}
	
	/** Adds an output pipe to this queue
	 * @param recipient ID of the recipient
	 * @param output output pipe corresponding to the recipient
	 * @warning If there already exists a known recipient with name \a name, its pipe will be replaced by \a output. 
	 */
	public void addOutputPipe (Object recipient, QueueOutputPipeInterface output) {
		try {
			output_lock.lock();
			outputs.put(recipient, output);
		} finally {
			output_lock.unlock();
		}
	}
	
	/** Returns the output pipe corresponding to the given recipient
	 * @param recipient the recipient
	 * @return its corresponding output pipe, or \c null if none
	 */
	public QueueOutputPipeInterface getOutputPipe (Object recipient) {
		QueueOutputPipeInterface out = null;
		try {
			output_lock.lock();
			out = outputs.get(recipient);
		} finally {
			output_lock.unlock();
		}
		return out;
	}
	
	/**
	 * Removes the output pipe corresponding to the given recipient
	 * @author Brammert Ottens
	 * @param recipient the recipient
	 */
	public void removeOutputPipe(Object recipient) {
		try {
			output_lock.lock();
			outputs.remove(recipient);
		} finally {
			output_lock.unlock();
		}
	}
	
	/** Adds a new output pipe to the provided queue 
	 * @param recipient ID of the recipient
	 * @param queue recipient
	 * @return the created output pipe
	 * @warning If there already exists a known recipient with name \a recipient, its pipe will be replaced. 
	 */
	public QueueIOPipe addOutputQueue (Object recipient, Queue queue) {
		QueueIOPipe pipe = new QueueIOPipe (queue);
		this.addOutputPipe(recipient, pipe);
		return pipe;
	}
	
	/** Adds a listener to be notified of new incoming messages
	 * 
	 * It prompts the listener for the types of messages it wants to listen to. 
	 * @param policy Incoming object policy to be added
	 */
	public void addIncomingMessagePolicy (IncomingMsgPolicyInterface <MessageType> policy) {
		this.addIncomingMessagePolicy(policy.getMsgTypes(), policy);
	}
	
	/** Adds a listener to be notified of new incoming messages
	 * @param msgTypes 		the message types the policy should be registered for
	 * @param policy 		Incoming object policy to be added
	 */
	public void addIncomingMessagePolicy (Collection<MessageType> msgTypes, IncomingMsgPolicyInterface <MessageType> policy) {
		for (MessageType type : msgTypes) {
			addIncomingMessagePolicy (type, policy);
		}
	}
	
	/** Adds a listener to be notified of new incoming messages of type \a type
	 * @param type the type of messages
	 * @param policy incoming object policy to be used
	 */
	public void addIncomingMessagePolicy (MessageType type, IncomingMsgPolicyInterface <MessageType> policy) {
		try {
			inPolicies_lock.lock();
			ArrayList< IncomingMsgPolicyInterface<MessageType> > policies = inPolicies.get(type);
			
			if (policies == null) { // We don't know this message type yet
				policies = new ArrayList< IncomingMsgPolicyInterface<MessageType> >();
				inPolicies.put(type, policies);
				policies.add(policy);
				
			} else if (! policies.contains(policy)) 
				policies.add(policy); 
		} finally {
			inPolicies_lock.unlock();
		}
		policy.setQueue(this);
	}
	
	/** Adds a listener to be notified of new outgoing messages
	 * 
	 * It prompts the listener for the types of messages it wants to listen to. 
	 * @param policy 	outgoing message policy to be added
	 */
	public void addOutgoingMessagePolicy (OutgoingMsgPolicyInterface <MessageType> policy) {
		this.addOutgoingMessagePolicy(policy.getMsgTypes(), policy);
	}
	
	/** Adds a listener to be notified of new outgoing messages
	 * @param msgTypes 		the message types the policy should be registered for
	 * @param policy 		outgoing message policy to be added
	 */
	public void addOutgoingMessagePolicy (Collection<MessageType> msgTypes, OutgoingMsgPolicyInterface <MessageType> policy) {
		for (MessageType type : msgTypes) {
			addOutgoingMessagePolicy (type, policy);
		}
	}
	
	/** Adds a listener to be notified of new outgoing messages of type \a type
	 * @param type 		the type of messages
	 * @param policy 	outgoing object policy to be used
	 */
	public void addOutgoingMessagePolicy (MessageType type, OutgoingMsgPolicyInterface <MessageType> policy) {
		try {
			outPolicies_lock.lock();
			ArrayList< OutgoingMsgPolicyInterface<MessageType> > policies = outPolicies.get(type);
			
			if (policies == null) { // We don't know this message type yet
				policies = new ArrayList< OutgoingMsgPolicyInterface<MessageType> >();
				outPolicies.put(type, policies);
				policies.add(policy);
				
			} else if (! policies.contains(policy)) 
				policies.add(policy); 
		} finally {
			outPolicies_lock.unlock();
		}
		policy.setQueue(this);
	}
	
	/** Adds the input message to this queue's inbox.
	 * @param msg the message
	 */
	public void addToInbox (MessageWrapper msg) {
		try {
			inbox.put(msg);
		} catch (InterruptedException e) { } // should never happen because the queue has infinite capacity
	}

	/** Sends a message to a specified recipient
	 * @param to recipient of the message
	 * @param msg message to be sent
	 */
	public void sendMessage(Object to, Message msg) {

		// Discard the message if one of the outgoing listeners requires it
		if (this.notifyOutListeners(this.problem != null ? this.problem.getAgent() : null, msg, Arrays.asList(to))) 
			return;
		
		MessageWrapper msgWrap = new MessageWrapper(msg);
		
		// update the nccc counter
		if(this.problem != null) 
			msgWrap.setNCCCs(this.problem.getNCCCs());
		
		sendMessage(to, msgWrap);
	}
	
	/** Notifies the incoming message listeners of a message
	 * @param msg 		the message
	 * @param toAgent 	ID of the destination agent
	 */
	protected void notifyInListeners (Message msg, Object toAgent) {
		
		try {
			inPolicies_lock.lock();
			
			// Notify the listeners for this message type and its ancestors
			for (MessageType type = msg.getType(); type != null; type = type.getParent()) {
				ArrayList< IncomingMsgPolicyInterface<MessageType> > modules = inPolicies.get(type);
				if (modules != null) {
					ArrayList< IncomingMsgPolicyInterface<MessageType> > policies = new ArrayList< IncomingMsgPolicyInterface<MessageType> > (modules);
					for (IncomingMsgPolicyInterface<MessageType> module : policies) // iterate over a copy in case a listener wants to add more listeners
						module.notifyIn(msg, toAgent);
				}
			}
			
		} finally {
			inPolicies_lock.unlock();
		}
	}
	
	/** Notifies the outgoing message listeners of a message
	 * @param fromAgent 	the sender agent
	 * @param msg 			the message
	 * @param toAgents 		the destination agents
	 * @return \c true if the message should be discarded
	 */
	protected boolean notifyOutListeners (Object fromAgent, Message msg, Collection<? extends Object> toAgents) {
		
		boolean discard = false;
		try {
			this.outPolicies_lock.lock();
			
			// Notify the listeners registered for this message's type and its ancestors
			assert msg != null;
			assert this.outPolicies != null;
			for (MessageType type = msg.getType(); type != null; type = type.getParent()) {
				ArrayList< OutgoingMsgPolicyInterface<MessageType> > modules = this.outPolicies.get(type); /// @bug very rarely throws a NullPointerException
				if (modules != null) 
					for (java.util.Iterator< OutgoingMsgPolicyInterface<MessageType> > iter = modules.iterator(); !discard && iter.hasNext(); )
						if (iter.next().notifyOut(fromAgent, msg, toAgents) == OutgoingMsgPolicyInterface.Decision.DISCARD) 
							discard = true;
			}

			
		} finally {
			this.outPolicies_lock.unlock();
		}
		
		return discard;
	}
	
	/**
	 * Send a message that has already been wrapped
	 * @param to		The destination of the message
	 * @param msgWrap	The wrapped message
	 * @warning Does not notify outgoing message listeners. 
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void sendMessage (Object to, MessageWrapper msgWrap) {
		QueueOutputPipeInterface outPipe;
		
		try {
			output_lock.lock();
			outPipe = outputs.get(to);
			assert (outPipe != null) : "Trying to send a message to an unknown recipient `" + to + "'; the message is:\n" + msgWrap;
			
			// Record statistics about this message
			if (this.measureMsgs) 
				if (! this.inputs.contains(outPipe)) 
					this.recordStats(to, msgWrap.getMessage());
			
			// Send the message
			outPipe.pushMessage(msgWrap);
		} finally {
			output_lock.unlock();
		}
	}
	
	/** Records statistics about the input message
	 * @param to 		recipient
	 * @param msg 		message
	 */
	protected void recordStats (Object to, Message msg) {
		
		// Don't count this message if it was sent to the stats monitor, the controller or to the daemon 
		if (to.equals(AgentInterface.STATS_MONITOR) || to.equals(Controller.CONTROLLER) || to.equals(Daemon.DAEMON)) 
			return;
		
		// Increment nbrMsgs
		MessageType type = msg.getType();
		Integer nbr = this.msgNbrs.get(type);
		if (nbr == null) 
			this.msgNbrs.put(type, 1);
		else 
			this.msgNbrs.put(type, nbr + 1);
		
		nbr = this.msgNbrsSent.get(to);
		if (nbr == null) 
			this.msgNbrsSent.put(to, 1);
		else 
			this.msgNbrsSent.put(to, nbr + 1);
		
		// Increment msgSizes
		Long totalSize = this.msgSizes.get(type);
		Long sizeTo = this.msgSizesSent.get(to);
		Long maxSize = this.maxMsgSizes.get(type);
		try {
			Long size = this.monitor.getMsgSize(to, msg);
			
			if (totalSize == null) 
				this.msgSizes.put(type, size);
			else 
				this.msgSizes.put(type, totalSize + size);
			
			if (sizeTo == null) 
				this.msgSizesSent.put(to, size);
			else 
				this.msgSizesSent.put(to, sizeTo + size);
			
			if (maxSize == null || size > maxSize) 
				this.maxMsgSizes.put(type, size);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/** Sends a message to itself
	 * @param msg message to be sent
	 */
	public void sendMessageToSelf(Message msg) {
		
		// Discard the message if one of the outgoing listeners requires it
		String agentName = this.problem != null ? this.problem.getAgent() : null;
		if (this.notifyOutListeners(agentName, msg, Arrays.asList(agentName))) 
			return;
		
		MessageWrapper msgWrap = new MessageWrapper(msg);
		if(this.problem != null) 
			msgWrap.setNCCCs(this.problem.getNCCCs());
		
		addToInbox(msgWrap);
	}
	
	/** Sends a message to all specified recipients
	 * @param <T> 			the type used for recipient IDs
	 * @param recipients 	the recipients
	 * @param msg 			the message to be sent
	 */
	public <T extends Object> void sendMessageToMulti (Collection <T> recipients, Message msg) {

		// Discard the message if one of the outgoing listeners requires it
		if (this.notifyOutListeners(this.problem != null ? this.problem.getAgent() : null, msg, recipients)) 
			return;

		MessageWrapper msgWrap = new MessageWrapper(msg);
		if(this.problem != null) 
			msgWrap.setNCCCs(this.problem.getNCCCs());

		for (Object recipient : recipients) 
			sendMessage(recipient, msgWrap);
	}
	
	/** Kills the queue, making it unusable */
	public void end () {
		keepGoing = false;
		myThread.interrupt();
		try {
			input_lock.lock();
			for (QueueInputPipeInterface input : inputs) 
				input.close();
		} finally {
			input_lock.unlock();
		}
		output_lock.lock();
		if (this.measureMsgs) 
			monitor.close();
		output_lock.unlock();
	}
	
	/** Continuously processes all messages in the inbox
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (keepGoing) {

			// Retrieve the next message, blocking while there is no message
			try {
				msgWrap = this.inbox.take(); // blocks
				
			} catch (InterruptedException e) { // the end() method has been called
				return;
			}
			Message msg = msgWrap.getMessage();
			
//			System.out.println(msg);
			
			// make sure the nccc counter is consistent with the nccc stamp
			// of this message
			this.updateNCCCs(msgWrap.getNCCCs());
			
			// Notify the incoming object policies of the message
			this.notifyInListeners(msg, this.problem != null ? this.problem.getAgent() : null);
		}
	}

	/** Completely removes the input policy from all lists of listeners
	 * @param policy the policy to be removed
	 */
	public void deleteIncomingMessagePolicy (IncomingMsgPolicyInterface <MessageType> policy) {
		try {
			inPolicies_lock.lock();
			
			// Go through the list of policies, regardless of the message type
			for (ArrayList< IncomingMsgPolicyInterface<MessageType> > policies : inPolicies.values()) 
				policies.remove(policy);

		} finally {
			inPolicies_lock.unlock();
		}
	}

	/** Delete all StatsReporters */
	public void deleteStatsReporters() {
		try {
			this.inPolicies_lock.lock();
			
			// Go through the list of policies, regardless of the message type
			for (ArrayList< IncomingMsgPolicyInterface<MessageType> > policies : inPolicies.values()) 
				for (Iterator< IncomingMsgPolicyInterface<MessageType> > iter = policies.iterator(); iter.hasNext(); ) 
					if (iter.next() instanceof StatsReporter) 
						iter.remove();
			
		} finally {
			this.inPolicies_lock.unlock();
		}
	}
	
	/** Completely removes the outgoing message policy from all lists of listeners
	 * @param policy 	the policy to be removed
	 */
	public void deleteOutgoingMessagePolicy (OutgoingMsgPolicyInterface <MessageType> policy) {
		try {
			outPolicies_lock.lock();
			
			// Go through the list of policies, regardless of the message type
			for (ArrayList< OutgoingMsgPolicyInterface<MessageType> > policies : outPolicies.values()) 
				policies.remove(policy);

		} finally {
			outPolicies_lock.unlock();
		}
	}

	/** Generates a String representation in DOT format of a network of queues
	 * @param queues list of queues in the network
	 * @return DOT representation for the network
	 * @note Only works with QueueIOPipes. 
	 */
	public static String networkToDOT (Queue[] queues) {
		StringBuilder out = new StringBuilder ("digraph {\n");
		for (int i = 0; i < queues.length; i++) {
			queues[i].toDOT(out, i);
		}
		out.append("}");
		return out.toString();
	}
	
	/**
	 * Removes all messages from the inbox.
	 */
	public void cleanQueue() {
		inbox.clear();
	}
	
	/** Sets the problem that is queried for NCCCs
	 * @param problem 	the problem
	 */
	public void setProblem (ProblemInterface<?, ?> problem) {
		this.problem = problem;
	}
	
	/** Updates the queue's NCCC counter to the input count if the input is greater
	 * @param msgNCCCs 	input NCCC count
	 */
	public void updateNCCCs (long msgNCCCs) {
		
		if(this.problem != null) 
			if(msgNCCCs > this.problem.getNCCCs())
				this.problem.setNCCCs(msgNCCCs);
	}
	
	/** @return Get the current time of this queue, in nanoseconds since some fixed, arbitrary epoch */
	public long getCurrentTime() {
		return System.nanoTime();
	}
	
	/** @return the size of the inbox*/
	public int getInboxSize() {
		return this.inbox.size();
	}
	
	/**
	 * @return The message wrapper of the last message received
	 */
	public MessageWrapper getCurrentMessageWrapper() {
		return this.msgWrap;
	}
	
	/** Creates a fragment of DOT representation for this queue and its pipes
	 * @param out StringBuilder
	 * @param ID ID to be used to represent this queue
	 */
	private void toDOT (StringBuilder out, int ID) {
		
		// Print the queue itself 
		out.append("\n\t" + ID + " [shape = record];\n");
		
		// Print output pipes
		for (Map.Entry<Object, QueueOutputPipeInterface> output : outputs.entrySet()) {
			
			// Print the pipe as a point
			out.append("\t\"" + output.getValue().toDOT() + "\" [shape = \"point\" label = \"\"];\n");
			
			// Print recipient ID
			out.append("\t" + ID + " -> \"" + output.getValue().toDOT() + "\" [label = \"" + output.getKey() + "\"];\n");
		}
		
		// Print input pipes
		for (QueueInputPipeInterface pipe : inputs) {
			out.append("\t" + "\"" + pipe.toDOT() + "\"" + " -> \"" + ID + "\";\n");
		}
	}
	
	/** @return for each message type, the number of messages sent of that type */
	public HashMap<MessageType, Integer> getMsgNbrs() {
		return msgNbrs;
	}

	/** @return the number of messages sent to each other agent */
	public HashMap<Object, Integer> getMsgNbrsSent() {
		return msgNbrsSent;
	}

	/** @return for each message type, the total amount of information sent in messages of that type, in bytes */
	public HashMap<MessageType, Long> getMsgSizes() {
		return msgSizes;
	}
	
	/** @return the amount of information sent to each other agent, in bytes */
	public HashMap<Object, Long> getMsgSizesSent() {
		return msgSizesSent;
	}

	/** @return for each message type, the size (in bytes) of the largest message of that type */
	public HashMap<MessageType, Long> getMaxMsgSizes() {
		return maxMsgSizes;
	}
	
	/** Resets the metrics statistics */
	public void resetStats () {
		
		if (this.measureMsgs) {
			this.msgNbrs = new HashMap<MessageType, Integer> ();
			this.msgNbrsSent = new HashMap<Object, Integer> ();
			this.msgSizes = new HashMap<MessageType, Long> ();
			this.msgSizesSent = new HashMap<Object, Long> ();			
			this.maxMsgSizes = new HashMap<MessageType, Long> ();
		}
		if(this.problem != null) 
			msgWrap.setNCCCs(-1);
	}
	
	/** Closes the MsgSizeMonitor and creates a new one
	 * 
	 * Makes the queue forget all previously sent data, hereby resetting the one-time serialization overheads. 
	 */
	public void resetMsgSizeMonitor () {
		
		if (this.measureMsgs) {
			this.monitor.close();
			try {
				this.monitor = new MsgSizeMonitor ();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
