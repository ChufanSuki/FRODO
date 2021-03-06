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

/** Tests for the JaCoP-based spaces */
package frodo2.solutionSpaces.JaCoP.tests;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.extensions.RepeatedTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;

import frodo2.algorithms.AbstractDCOPsolver;
import frodo2.algorithms.Solution;
import frodo2.algorithms.XCSPparser;
import frodo2.algorithms.adopt.ADOPTsolver;
import frodo2.algorithms.afb.AFBsolver;
import frodo2.algorithms.asodpop.ASODPOPsolver;
import frodo2.algorithms.dpop.DPOPsolver;
import frodo2.algorithms.dpop.privacy.P2_DPOPsolver;
import frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver;
import frodo2.algorithms.dpop.privacy.P_DPOPsolver;
import frodo2.algorithms.localSearch.dsa.DSAsolver;
import frodo2.algorithms.localSearch.mgm.MGMsolver;
import frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver;
import frodo2.algorithms.maxsum.MaxSumSolver;
import frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver;
import frodo2.algorithms.odpop.ODPOPsolver;
import frodo2.algorithms.reformulation.ProblemRescaler;
import frodo2.algorithms.synchbb.SynchBBsolver;
import frodo2.algorithms.test.AllTests;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.JaCoP.JaCoPutilSpace;
import frodo2.solutionSpaces.JaCoP.JaCoPxcspParser;
import frodo2.solutionSpaces.crypto.AddableBigInteger;

/** JUnit test for JaCoPutilSpace
 * @author Arnaud Jutzeler, Thomas Leaute
 * @param <U> class of utility values
 *
 */
public class JaCoPtests < U extends Addable<U> > extends TestCase {

	/** The description file of the agent */
	private Document agentDescDoc;

	/** The solver used */
	private Class<? extends AbstractDCOPsolver<AddableInteger, U, ?>> solverClass;

	/** If we use the TCP pipes or not */
	private boolean useTCP;

	/** The optimization type of random problems */
	private boolean maximize;

	/** The problem file */
	private File probFile;
	
	/** The different algorithms to be tested */
	private enum Algorithm
	{
	    /** DPOP */
	    DPOP,
	    
	    /** P-DPOP */
	    P_DPOP, 
	    
	    /** P3/2-DPOP */
	    P3halves_DPOP, 
	    
	    /** P2-DPOP */
	    P2_DPOP, 
	    
	    /** ASO-DPOP */
	    ASODPOP,
	    
	    /** O-DPOP */
	    ODPOP,
	    
	    /** SynchBB */
	    SYNCHBB,
	    
	    /** ADOPT */
	    ADOPT,
	    
	    /** DSA */
	    DSA,
	    
	    /** MGM */
	    MGM, 
	    
	    /** MGM2 */
	    MGM2, 
	    
	    /** MPC-DisCSP4 */
	    MPC_DisCSP4, 
	    
	    /** MPC-DisWCSP4 */
	    MPC_DisWCSP4, 
	    
	    /** Max-Sum */
	    MAXSUM, 
	    
	    /** MB-DPOP */
	    MB_DPOP, 
	    
	    /** AFB */
	    AFB
	}
	
	/** The algorithm being tested */
	private Algorithm currentAlgorithm;

	/** The class of utility values */
	private final Class<U> classOfU;

	/** Constructor
	 * @param name 			name of the test method
	 * @param algorithm		the algorithm tested
	 * @param agentDescFile	the description file of the agent
	 * @param solverClass	the class of the tested solver
	 * @param classOfU 		the class of utility values
	 * @param useTCP			if we test with TCP pipes or not
	 * @param maximize		the optimization type of random problems
	 */
	public JaCoPtests(String name, Algorithm algorithm, String agentDescFile, 
			Class<? extends AbstractDCOPsolver<AddableInteger, U, ?>> solverClass, Class<U> classOfU, 
					boolean useTCP, boolean maximize) {
		this (name, algorithm, agentDescFile, solverClass, classOfU, useTCP, maximize, null);
	}

	/** Constructor that takes in a File
	 * @param name 			name of the test method
	 * @param algorithm		the algorithm tested
	 * @param agentDescFile	the description file of the agent
	 * @param solverClass	the class of the tested solver
	 * @param classOfU 		the class of utility values
	 * @param useTCP			if we test with TCP pipes or not
	 * @param maximize		the optimization type of random problems
	 * @param probFile 		the problem file
	 */
	public JaCoPtests(String name, Algorithm algorithm, String agentDescFile, 
			Class<? extends AbstractDCOPsolver<AddableInteger, U, ?>> solverClass, Class<U> classOfU, 
					boolean useTCP, boolean maximize, File probFile) {
		super(name);

		try {
			this.agentDescDoc = XCSPparser.parse(agentDescFile, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.currentAlgorithm = algorithm;
		this.solverClass = solverClass;
		this.classOfU = classOfU;
		this.useTCP = useTCP;
		this.maximize = maximize;
		this.probFile = probFile;
	}

	/** @return the test suite */
	@SuppressWarnings("unchecked")
	public static TestSuite suite () {
		TestSuite suite = new TestSuite ("Tests for the JaCoP spaces");
		
		suite.addTest(createSuite(Algorithm.DPOP, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new DPOPsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/dpop/DPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.P_DPOP, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableBigInteger, ?>>) new P_DPOPsolver<AddableInteger> ().getClass(), 
				AddableBigInteger.class, "src/frodo2/algorithms/dpop/privacy/P-DPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.P3halves_DPOP, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableBigInteger, ?>>) new P3halves_DPOPsolver<AddableInteger> ().getClass(), 
				AddableBigInteger.class, "src/frodo2/algorithms/dpop/privacy/P1.5-DPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.P2_DPOP, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new P2_DPOPsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/dpop/privacy/P2-DPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.ASODPOP,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new ASODPOPsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/asodpop/ASODPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.ODPOP,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new ODPOPsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/odpop/ODPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.SYNCHBB,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new SynchBBsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/synchbb/SynchBBagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.ADOPT,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new ADOPTsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/adopt/ADOPTagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.DSA,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new DSAsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/localSearch/dsa/DSAagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.MGM,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MGMsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/localSearch/mgm/MGMagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.MGM2,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MGM2solver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/localSearch/mgm/mgm2/MGM2agentJaCoP.xml"));

		suite.addTest(createSuite(Algorithm.MPC_DisCSP4,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MPC_DisWCSP4solver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/mpc_discsp/MPC-DisCSP4_JaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.MPC_DisWCSP4,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MPC_DisWCSP4solver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/mpc_discsp/MPC-DisWCSP4_JaCoP.xml"));

		suite.addTest(createSuite(Algorithm.MAXSUM,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MaxSumSolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/maxsum/MaxSumAgentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.MAXSUM,  (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new MaxSumSolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/maxsum/MaxSumAgentPerturbedJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.MB_DPOP, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new DPOPsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/dpop/memory/MB-DPOPagentJaCoP.xml"));
		
		suite.addTest(createSuite(Algorithm.AFB, (Class<? extends AbstractDCOPsolver<AddableInteger, AddableInteger, ?>>) new AFBsolver<AddableInteger, AddableInteger> ().getClass(), 
				AddableInteger.class, "src/frodo2/algorithms/afb/AFBagentJaCoP.xml"));

		return suite;
	}


	/** @see junit.framework.TestCase#setUp() */
	public void setUp () throws Exception {
		super.setUp();

	}


	/** Tests on a random problem in extension
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testPureExtensionalProblem() throws Exception {
		
		DPOPsolver<AddableInteger, U> dpopSolver = new DPOPsolver<AddableInteger, U>(AddableInteger.class, this.classOfU);

		Document problemDoc = XCSPparser.parse(this.probFile.getCanonicalFile(), false);

		Solution<AddableInteger, U> dpopSol = dpopSolver.solve(problemDoc, 10000L);
		assertFalse("DPOP timed out", dpopSol == null);
		
		AbstractDCOPsolver<AddableInteger, U, ?> jacopSolver = solverClass.getConstructor(Document.class, boolean.class).newInstance(this.agentDescDoc, this.useTCP);
		Solution<AddableInteger, U> jaCoPSol;
		switch (this.currentAlgorithm) {
		case MPC_DisWCSP4:
			XCSPparser<AddableInteger, U> parser = new XCSPparser<AddableInteger, U> (problemDoc);
			parser.setUtilClass(this.classOfU);
			final int nbrConstraints = parser.getSolutionSpaces().size();
			jaCoPSol = ((MPC_DisWCSP4solver<AddableInteger, U>)jacopSolver)
				.solve(problemDoc, false, 300000L, 10 * nbrConstraints, 10 * nbrConstraints * parser.getAgents().size());
			break;
		case P2_DPOP:
			parser = new XCSPparser<AddableInteger, U> (problemDoc);
			parser.setUtilClass(this.classOfU);
			jaCoPSol = ((P2_DPOPsolver<AddableInteger, U>) jacopSolver).solve(problemDoc, 60000L, 1);
			break;
		default:
			jaCoPSol = jacopSolver.solve(problemDoc, 300000L);
		}
		assertFalse("Timed out on " + this.probFile.getName(), jaCoPSol == null);

		// Incomplete solver
		if(currentAlgorithm == Algorithm.DSA || currentAlgorithm == Algorithm.MGM || currentAlgorithm == Algorithm.MGM2 || this.currentAlgorithm == Algorithm.MAXSUM){
			assertFalse(jaCoPSol.getUtility() == null);
			if(maximize){
				assertTrue (dpopSol.getUtility().compareTo(jaCoPSol.getUtility()) >= 0);
			}else{
				assertTrue (dpopSol.getUtility().compareTo(jaCoPSol.getUtility()) <= 0);
			}
		// Complete solver	
		}else{
			assertEquals (problemDoc.getRootElement().getChild("presentation").getAttributeValue("name"), dpopSol.getUtility(), jaCoPSol.getUtility());
			// If the two solutions have the same utility but are different we test that the solution obtained with the agents using JaCoP is valid
			if(!dpopSol.getAssignments().equals(jaCoPSol.getAssignments())){
				XCSPparser<AddableInteger, U> parser = new XCSPparser<AddableInteger, U>(problemDoc);
				parser.setUtilClass(this.classOfU);
				assertEquals(jaCoPSol.getUtility(), parser.getUtility(jaCoPSol.getAssignments()).getUtility(0));
			}
		}
	}
	
	/** Tests on a random problem in extension
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testRandomExtensionalProblem() throws Exception {

		DPOPsolver<AddableInteger, U> dpopSolver = new DPOPsolver<AddableInteger, U>(AddableInteger.class, this.classOfU);

		Document problemDoc;

		// We set a maximum complexity for the random problem as a function of the algorithm
		switch(currentAlgorithm)
		{
		    case DPOP: 
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case MB_DPOP: 
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case P_DPOP: 
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case P3halves_DPOP: 
		    	problemDoc = AllTests.createRandProblem(4, 6, 5, maximize, 0);
		    	break;
		    case P2_DPOP: 
		    	problemDoc = AllTests.createRandProblem(3, 3, 3, maximize, 0, 10);
		    	break;
		    case ASODPOP:
		    	problemDoc = AllTests.createRandProblem(4, 10, 5, maximize, 0);
		    	break;
		    case ODPOP:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case SYNCHBB:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case ADOPT:
		    	problemDoc = AllTests.createRandProblem(5, 10, 5, maximize, 0);
		        break;
		    case DSA:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case MGM:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case MGM2:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    case MPC_DisCSP4:
		    	problemDoc = AllTests.createRandProblem(5, 10, 4, false, +1, 0);
		    	break;
		    case MPC_DisWCSP4:
		    	problemDoc = AllTests.createRandProblem(3, 3, 3, maximize, 0, 4);
		    	break;
		    case MAXSUM:
		    	problemDoc = AllTests.createRandProblem(5, 10, 5, maximize, 0);
		    	break;
		    case AFB:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);
		    	break;
		    default:
		    	problemDoc = AllTests.createRandProblem(7, 20, 5, maximize, 0);   
		}

		XCSPparser<AddableInteger, U> parser = new XCSPparser<AddableInteger, U> (problemDoc);
		parser.setUtilClass(this.classOfU);
		Solution<AddableInteger, U> dpopSol = dpopSolver.solve(problemDoc, parser.getNbrVars(), 10000L);
		assertFalse("DPOP timed out", dpopSol == null);

		// Fix the ProblemRescaler's shift
		int shift = 0;
		for (Element module : (List<Element>) this.agentDescDoc.getRootElement().getChild("modules").getChildren()) {
			if (module.getAttributeValue("className").equals(ProblemRescaler.class.getName())) {
				
				// Compute the minimum required shift
				for (UtilitySolutionSpace<AddableInteger, U> space : parser.getSolutionSpaces()) {
					if (maximize) 
						shift = Math.max(shift, Math.max(0, space.blindProjectAll(true).intValue()));
					else 
						shift = Math.max(shift, - Math.min(0, space.blindProjectAll(false).intValue()));
				}
				
				module.setAttribute("shift", Integer.toString(shift));
				break;
			}
		}
		
		AbstractDCOPsolver<AddableInteger, U, ?> jacopSolver = solverClass.getConstructor(Document.class, boolean.class).newInstance(this.agentDescDoc, this.useTCP);
		Solution<AddableInteger, U> jaCoPSol;
		switch (this.currentAlgorithm) {
		case MPC_DisWCSP4:
			final int nbrConstraints = parser.getSolutionSpaces().size();
			jaCoPSol = ((MPC_DisWCSP4solver<AddableInteger, U>)jacopSolver)
				.solve(problemDoc, false, 300000L, 10 * nbrConstraints, 10 * nbrConstraints * parser.getAgents().size());
			break;
		case P2_DPOP:
			jaCoPSol = ((P2_DPOPsolver<AddableInteger, U>) jacopSolver).solve(problemDoc, parser.getNbrVars(), 60000L, shift + 10 * parser.getSolutionSpaces().size());
			break;
		default:
			jaCoPSol = jacopSolver.solve(problemDoc, parser.getNbrVars(), 60000L);
		}
		assertFalse("Timed out", jaCoPSol == null);

		// Incomplete solver
		if(currentAlgorithm == Algorithm.DSA || currentAlgorithm == Algorithm.MGM || currentAlgorithm == Algorithm.MGM2 || this.currentAlgorithm == Algorithm.MAXSUM){
			assertFalse(jaCoPSol.getUtility() == null);
			if(maximize){
				assertTrue (dpopSol.getUtility().compareTo(jaCoPSol.getUtility()) >= 0);
			}else{
				assertTrue (dpopSol.getUtility().compareTo(jaCoPSol.getUtility()) <= 0);
			}
		// Complete solver	
		}else{
			assertEquals (dpopSol + "\n!=\n" + jaCoPSol, dpopSol.getUtility(), jaCoPSol.getUtility());
			// If the two solutions have the same utility but are different we test that the solution obtained with the agents using JaCoP is valid
			if(!dpopSol.getAssignments().equals(jaCoPSol.getAssignments())) 
				assertEquals(jaCoPSol.getUtility(), parser.getUtility(jaCoPSol.getAssignments()).getUtility(0));
		}
	}

	/** Tests on problems in intension (predicates and global constraints)
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void testPureIntensionalProblem() throws Exception {
		AbstractDCOPsolver<AddableInteger, U, ?> jacopDCOPsolver = solverClass.getConstructor(Document.class, boolean.class).newInstance(this.agentDescDoc, this.useTCP);

		Document problemDoc = XCSPparser.parse(this.probFile.getCanonicalFile(), false);
		addRandomOwners(problemDoc, 3);
		
		Solution<AddableInteger, U> jacopDCOPsol;
		switch (this.currentAlgorithm) {
		case MPC_DisWCSP4:
			XCSPparser<AddableInteger, U> parser = new XCSPparser<AddableInteger, U> (problemDoc);
			parser.setUtilClass(this.classOfU);
			jacopDCOPsol = ((MPC_DisWCSP4solver<AddableInteger, U>)jacopDCOPsolver)
				.solve(problemDoc, false, 300000L, 1, parser.getAgents().size());
			break;
		case P2_DPOP:
			parser = new XCSPparser<AddableInteger, U> (problemDoc);
			parser.setUtilClass(this.classOfU);
			jacopDCOPsol = ((P2_DPOPsolver<AddableInteger, U>) jacopDCOPsolver).solve(problemDoc, 500000L, 1);
			break;
		default:
			jacopDCOPsol = jacopDCOPsolver.solve(problemDoc, 300000L);
		}
		assertFalse("Timed out on " + this.probFile.getName(), jacopDCOPsol == null);

		U utility = jacopDCOPsol.getUtility();

		U centralizedJaCoPresult = solveCentralizedProblem(problemDoc);
		
		// Incomplete solver
		if(currentAlgorithm == Algorithm.DSA || currentAlgorithm == Algorithm.MGM || currentAlgorithm == Algorithm.MGM2 || this.currentAlgorithm == Algorithm.MAXSUM){
			if(maximize){
				assertTrue (centralizedJaCoPresult.compareTo(utility) >= 0);
			}else{
				assertTrue (centralizedJaCoPresult.compareTo(utility) <= 0);
			}
		// Complete solver	
		}else{
			assertEquals (this.probFile.getName(), centralizedJaCoPresult, utility);
			if(!(centralizedJaCoPresult.equals(AddableInteger.MinInfinity.MIN_INF) || centralizedJaCoPresult.equals(AddableInteger.PlusInfinity.PLUS_INF))){
				// Test if the solution found is correct
				checkSolutionCentralizedProblem(jacopDCOPsol.getAssignments(), problemDoc);
			}
		}
	}

	/** Add a random owner to variables if they do not have already one
	 * @param problemDoc	the description file of the problem
	 * @param maxAgent		the maximum number of possible agents
	 */
	private void addRandomOwners(Document problemDoc, int maxAgent){
		Random rand = new Random(System.currentTimeMillis());
		assert maxAgent > 0;
		maxAgent = rand.nextInt(maxAgent - 1) + 2;
		Set<String> agents = new HashSet<String> ();
		String owner;
		for(Element variable: (List<Element>) problemDoc.getRootElement().getChild("variables").getChildren()){
			if(variable.getAttribute("agent") == null){
				owner = Integer.toString(rand.nextInt(maxAgent));
				variable.setAttribute("agent", owner);
				agents.add(owner);
			}
		}
		
		Element agentsElmt = new Element ("agents");
		problemDoc.getRootElement().addContent(agentsElmt);
		agentsElmt.setAttribute("nbAgents", Integer.toString(agents.size()));
		for (String agent : agents) {
			Element elmt = new Element ("agent");
			agentsElmt.addContent(elmt);
			elmt.setAttribute("name", agent);
		}

		problemDoc.getRootElement().getChild("presentation").setAttribute("format", "XCSP 2.1_FRODO");
	}

	/** Solves a pure intensional problem with JaCoP
	 * We get the variables and predicates with the JaCoPxcspParser class that we assume is correct (see JaCoPxcspParserTest)
	 * @param problemDoc	the description file of the pure intensional problem
	 * @return				the solution of the CSP
	 * @throws IllegalAccessException thrown when failing to create a utility instance
	 * @throws InstantiationException thrown when failing to create a utility instance
	 * @throws NoSuchMethodException thrown when failing to create a utility instance
	 * @throws InvocationTargetException thrown when failing to create a utility instance
	 */
	@SuppressWarnings({ "unchecked" })
	private U solveCentralizedProblem(Document problemDoc) 
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

		Element params = new Element("parser");
		params.setAttribute("parserClass", "frodo2.solutionSpaces.JaCoP.JaCoPxcspParser");
		params.setAttribute("displayGraph", "false");
		params.setAttribute("domClass", "frodo2.solutionSpaces.AddableInteger");
		params.setAttribute("utilClass", this.classOfU.getName());
		params.setAttribute("DOTrenderer", "");
		params.setAttribute("countNCCCs", "false");

		JaCoPxcspParser<U> parser = new JaCoPxcspParser<U> (problemDoc, params);

		AddableInteger[] domain;
		Set<String> vars = parser.getVariables();

		HashMap<String, AddableInteger[]> varsSet = new HashMap<String,AddableInteger[]>(vars.size());
		for(String var: vars){
			domain = parser.getDomain(var);
			varsSet.put(var, domain);
		}

		U infeasibleUtil;
		if(maximize){
			infeasibleUtil = this.classOfU.getConstructor().newInstance().getMinInfinity();
		}else{
			infeasibleUtil = this.classOfU.getConstructor().newInstance().getPlusInfinity();
		}


		HashMap<String, Element> constraints = new HashMap<String, Element>();
		for(Element cons: (List<Element>)problemDoc.getRootElement().getChild("constraints").getChildren()){
			constraints.put(cons.getAttributeValue("name"), cons);
		}
		HashMap<String, Element> references = new HashMap<String, Element>();
		Element relations = problemDoc.getRootElement().getChild("relations");
		if(relations != null){
			for(Element rel: (List<Element>)relations.getChildren()){
				references.put(rel.getAttributeValue("name"), rel);
			}
		}
		Element predicates =  problemDoc.getRootElement().getChild("predicates");
		if(predicates != null){
			for(Element pred: (List<Element>)predicates.getChildren()){
				references.put(pred.getAttributeValue("name"), pred);
			}
		}

		// Parse all the constraints
		ArrayList< JaCoPutilSpace<U> > spaces = new ArrayList< JaCoPutilSpace<U> > ();
		HashSet<String> forbiddenVars = new HashSet<String> ();
		for (Element constraint : constraints.values()) 
			parser.parseConstraint(spaces, constraint, varsSet, references.get(constraint.getAttributeValue("reference")), vars, 
					false, false, infeasibleUtil, forbiddenVars);
		
		// Join all the constraints and impose them 
		JaCoPutilSpace<U> join = spaces.remove(0);
		if (! spaces.isEmpty()) 
			join = (JaCoPutilSpace<U>) join.join(spaces.toArray(new JaCoPutilSpace [spaces.size()]));
		if (! join.imposeConstraints()) 
			return infeasibleUtil;
		
		StoreCloneable store = join.getStore();
		
		IntVarCloneable[] jacopVars = new IntVarCloneable[vars.size()];
		int n = 0;
		for(String var: vars){
			// Find the JaCoP variable
			jacopVars[n] = (IntVarCloneable) store.findVariable(var);
			assert jacopVars[n] != null: "Variable " + var + " not found in the store!";
			n++;

		}

		IntVarCloneable utilVar = (IntVarCloneable) store.findVariable("util_total");
		assert utilVar != null: "Variable " + "util_total" + " not found in the store!";

		// Optimization search
		Search<IntVarCloneable> search = new DepthFirstSearch<IntVarCloneable> ();
		search.getSolutionListener().recordSolutions(true);
		search.setAssignSolution(false);

		// Debug information
		search.setPrintInfo(false);

		boolean result = search.labeling(store, 
				new SimpleSelect<IntVarCloneable> (jacopVars, new SmallestDomain<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), 
				utilVar);

		U costValue;

		if(!result){
			// The solution given in argument is inconsistent!
			costValue = infeasibleUtil;
		}else{

			int cost = search.getCostValue();

			// If it is a maximization problem
			if(this.maximize == true){
				cost *= -1;
			}

			costValue = this.classOfU.getConstructor().newInstance().fromInt(cost);
		}
		
		return costValue;
	}


	/** Check with JaCoP if the assignment given in argument is a valid solution of the pure intensional problem
	 * We get the variables and predicates with the JaCoPxcspParser class that we assume is correct (see JaCoPxcspParserTest)
	 * @param assignment	the assignment of variable that we want to check
	 * @param problemDoc	the description file of the pure intensional problem
	 * @return				true if the assignment is a solution of the CSP
	 * @throws IllegalAccessException thrown when failing to create a utility instance
	 * @throws InstantiationException thrown when failing to create a utility instance
	 * @throws NoSuchMethodException thrown when failing to create a utility instance
	 * @throws InvocationTargetException thrown when failing to create a utility instance
	 */
	@SuppressWarnings("unchecked")
	private boolean checkSolutionCentralizedProblem(Map<String, AddableInteger> assignment, Document problemDoc) 
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

		Element params = new Element("parser");
		params.setAttribute("parserClass", "frodo2.solutionSpaces.JaCoP.JaCoPxcspParser");
		params.setAttribute("displayGraph", "false");
		params.setAttribute("domClass", "frodo2.solutionSpaces.AddableInteger");
		params.setAttribute("utilClass", this.classOfU.getName());
		params.setAttribute("DOTrenderer", "");
		params.setAttribute("countNCCCs", "false");

		JaCoPxcspParser<U> parser = new JaCoPxcspParser<U> (problemDoc, params);

		AddableInteger[] domain;
		Set<String> vars = parser.getVariables();

		HashMap<String, AddableInteger[]> varsSet = new HashMap<String, AddableInteger[]>(vars.size());
		for(String var: vars){
			assert assignment.get(var) != null : "No assignment to variable `" + var + "' in " + assignment;
			domain = new AddableInteger[] {assignment.get(var)};
			varsSet.put(var, domain);
		}

		U infeasibleUtil;
		if(maximize){
			infeasibleUtil = this.classOfU.getConstructor().newInstance().getMinInfinity();
		}else{
			infeasibleUtil = this.classOfU.getConstructor().newInstance().getPlusInfinity();
		}


		HashMap<String, Element> constraints = new HashMap<String, Element>();
		for(Element cons: (List<Element>)problemDoc.getRootElement().getChild("constraints").getChildren()){
			constraints.put(cons.getAttributeValue("name"), cons);
		}
		HashMap<String, Element> references = new HashMap<String, Element>();
		Element relations = problemDoc.getRootElement().getChild("relations");
		if(relations != null){
			for(Element rel: (List<Element>)relations.getChildren()){
				references.put(rel.getAttributeValue("name"), rel);
			}
		}
		Element predicates =  problemDoc.getRootElement().getChild("predicates");
		if(predicates != null){
			for(Element pred: (List<Element>)predicates.getChildren()){
				references.put(pred.getAttributeValue("name"), pred);
			}
		}
		
		// Parse all the constraints
		ArrayList< JaCoPutilSpace<U> > spaces = new ArrayList< JaCoPutilSpace<U> > ();
		HashSet<String> forbiddenVars = new HashSet<String> ();
		for (Element constraint : constraints.values()) 
			parser.parseConstraint(spaces, constraint, varsSet, references.get(constraint.getAttributeValue("reference")), vars, 
					false, false, infeasibleUtil, forbiddenVars);
		
		// Join all the constraints and impose them 
		JaCoPutilSpace<U> join = spaces.remove(0);
		if (! spaces.isEmpty()) 
			join = (JaCoPutilSpace<U>) join.join(spaces.toArray(new JaCoPutilSpace [spaces.size()]));
		if (! join.imposeConstraints()) 
			return false;
		
		StoreCloneable store = join.getStore();
		
		IntVarCloneable[] jacopVars = new IntVarCloneable[vars.size()];
		int n = 0;
		for(String var: vars){
			// Find the JaCoP variable
			jacopVars[n] = (IntVarCloneable) store.findVariable(var);
			assert jacopVars[n] != null: "Variable " + var + " not found in the store!";
			n++;

		}

		IntVarCloneable utilVar = (IntVarCloneable) store.findVariable("util_total");
		assert utilVar != null: "Variable " + "util_total" + " not found in the store!";

		// Optimization search
		Search<IntVarCloneable> search = new DepthFirstSearch<IntVarCloneable> ();
		search.getSolutionListener().recordSolutions(true);
		search.setAssignSolution(false);

		// Debug information
		search.setPrintInfo(false);

		boolean result = search.labeling(store, new SimpleSelect<IntVarCloneable> (jacopVars, new SmallestDomain<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()));

		return result;
	}
	
	/**
	 * @param algorithm			the name of the tested algorithm
	 * @param solverClass		the class of the solver corresponding to the tested algorithm
	 * @param classOfU 			the class of utility values
	 * @param AgentDescFile		the agent description file that use the JaCoPxcspParser and the tested algorithm
	 * @return		the testSuite containing the tests with every combination of parameters: minimization/maximization,
	 * 				with/without TCP pipes, on random extensional/intensional/global constrained problems
	 */
	private static < U extends Addable<U> > TestSuite createSuite(Algorithm algorithm, 
			Class<? extends AbstractDCOPsolver<AddableInteger, U, ?>> solverClass, Class<U> classOfU, String AgentDescFile){
		
		TestSuite algoTestSuite = new TestSuite ("Tests " + algorithm + " using JaCoP solution spaces");
		
		TestSuite tmp = new TestSuite ("Minimization problems with extensional constraints without TCP pipes");
		tmp.addTest(new RepeatedTest (new JaCoPtests<U> ("testRandomExtensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, false, false), 200));
		algoTestSuite.addTest(tmp);

		if (algorithm != Algorithm.MPC_DisCSP4) {
			tmp = new TestSuite ("Maximization problems with extensional constraints without TCP pipes");
			tmp.addTest(new RepeatedTest (new JaCoPtests<U> ("testRandomExtensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, false, true), 200));
			algoTestSuite.addTest(tmp);
		}

		if (algorithm == Algorithm.DPOP || algorithm == Algorithm.MB_DPOP || algorithm == Algorithm.P_DPOP || algorithm == Algorithm.P3halves_DPOP 
				|| algorithm == Algorithm.P2_DPOP || algorithm == Algorithm.ADOPT || algorithm == Algorithm.AFB || algorithm == Algorithm.MPC_DisWCSP4) {
			tmp = new TestSuite ("Minimization problems with extensional constraints and TCP pipes");
			tmp.addTest(new RepeatedTest (new JaCoPtests<U> ("testRandomExtensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, true, false), 200));
			algoTestSuite.addTest(tmp);

			tmp = new TestSuite ("Maximization problems with extensional constraints and TCP pipes");
			tmp.addTest(new RepeatedTest (new JaCoPtests<U> ("testRandomExtensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, true, true), 200));
			algoTestSuite.addTest(tmp);
		}
		
		tmp = new TestSuite ("Problems with constraints in extensions with different semantics, and syntaxic subtleties");
		for (File probFile : new File("src/frodo2/solutionSpaces/JaCoP/tests/Instances/Ext").listFiles())
			if (probFile.isFile()) 
				if (! probFile.getName().contains("party_ext") || (algorithm != Algorithm.P3halves_DPOP && algorithm != Algorithm.P2_DPOP)) 
					tmp.addTest(new JaCoPtests<U> ("testPureExtensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, false, false, probFile));
		algoTestSuite.addTest(tmp);
		
		///@todo create a real set of test problems
		HashMap<String, File> allIntProblems = new HashMap<String, File>();
		for (File probFile : new File("src/frodo2/solutionSpaces/JaCoP/tests/Instances/Int").listFiles())
			allIntProblems.put(probFile.getName(), probFile);
		
		// We remove the problems that are too difficult for specific algorithms
		switch(algorithm)
		{
		    case DPOP:
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case P_DPOP:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("series-sat2.xml");
		    	allIntProblems.remove("series-unsat2.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case P3halves_DPOP:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("series-sat2.xml");
		    	allIntProblems.remove("series-unsat2.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case P2_DPOP:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-sat2.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("series-unsat2.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case ASODPOP:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case ODPOP:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case SYNCHBB:
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	break;
		    case ADOPT:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		        break;
		    case DSA:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case MGM:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case MGM2:
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case MPC_DisCSP4:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-sat2.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("series-unsat2.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case MPC_DisWCSP4:
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-sat2.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("series-unsat2.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case MAXSUM: 
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	
		    	break;
		    case MB_DPOP: 
		    	allIntProblems.remove("series-sat.xml");
		    	allIntProblems.remove("series-unsat.xml");
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-5-5-3.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-6-6-3.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-7-7-3.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	break;
		    case AFB:
		    	allIntProblems.remove("queenAttacking-3.xml");
		    	allIntProblems.remove("queenAttacking-4.xml");
		    	allIntProblems.remove("cc-5-5-2.xml");
		    	allIntProblems.remove("cc-6-6-2.xml");
		    	allIntProblems.remove("cc-7-7-2.xml");
		    	allIntProblems.remove("cc-8-8-2.xml");
		    	allIntProblems.remove("cc-8-8-3.xml");
		    	
		    	break;
		    default:	 
		}
		
		tmp = new TestSuite ("Problems with intensional constraints without TCP pipes");
		for (File probFile : allIntProblems.values())
			if (probFile.isFile()) 
				tmp.addTest(new JaCoPtests<U> ("testPureIntensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, false, false, probFile));
		algoTestSuite.addTest(tmp);

		if (algorithm == Algorithm.DPOP || algorithm == Algorithm.MB_DPOP || algorithm == Algorithm.P_DPOP || algorithm == Algorithm.P3halves_DPOP 
				|| algorithm == Algorithm.P2_DPOP || algorithm == Algorithm.ADOPT || algorithm == Algorithm.AFB || algorithm == Algorithm.MPC_DisWCSP4) {
			tmp = new TestSuite ("Problems with intensional constraints and TCP pipes");
			for (File probFile : allIntProblems.values())
				if (probFile.isFile()) 
					tmp.addTest(new JaCoPtests<U> ("testPureIntensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, true, false, probFile));
			algoTestSuite.addTest(tmp);
		}
		
		HashMap<String, File> allGlbProblems = new HashMap<String, File>();
		for (File probFile : new File("src/frodo2/solutionSpaces/JaCoP/tests/Instances/Glb").listFiles())
			allGlbProblems.put(probFile.getName(), probFile);
		
		// We remove the problems that are too difficult for specific algorithms
		switch(algorithm)
		{
		    case DPOP:
		    	
		    	break;
		    case P_DPOP:
		    	allGlbProblems.remove("glb1.xml");
		    	
		    	break;
		    case P3halves_DPOP:
		    	allGlbProblems.remove("glb1.xml");
		    	
		    	break;
		    case P2_DPOP:
		    	allGlbProblems.remove("glb1.xml");
		    	allGlbProblems.remove("glb2.xml");
		    	allGlbProblems.remove("glb3.xml");
		    	allGlbProblems.remove("glb4.xml");
		    	
		    	break;
		    case ASODPOP:
		    	
		    	break;
		    case ODPOP:
		    	
		    	break;
		    case SYNCHBB:

		    	break;
		    case ADOPT:

		        break;
		    case DSA:
	
		    	break;
		    case MGM:

		    	break;
		    case MGM2:

		    	break;
		    case MPC_DisCSP4:
		    	allGlbProblems.remove("glb1.xml");
		    	allGlbProblems.remove("glb2.xml");
		    	allGlbProblems.remove("glb3.xml");
		    	allGlbProblems.remove("glb4.xml");
		    	break;
		    case MPC_DisWCSP4:
		    	allGlbProblems.remove("glb1.xml");
		    	allGlbProblems.remove("glb2.xml");
		    	allGlbProblems.remove("glb3.xml");
		    	allGlbProblems.remove("glb4.xml");
		    	break;
		    case MAXSUM: 
		    	allGlbProblems.remove("glb3.xml");
		    	allGlbProblems.remove("glb4.xml");
		    	
		    	break;
		    case MB_DPOP: 
		    	
		    	break;
		    case AFB:
		    	
		    	break;
		    default:	 
		}

		tmp = new TestSuite ("Problems with global constraints without TCP pipes");
		for (File probFile : allGlbProblems.values())
			if (probFile.isFile()) 
				tmp.addTest(new JaCoPtests<U> ("testPureIntensionalProblem", algorithm, AgentDescFile, solverClass, classOfU, false, false, probFile));
		algoTestSuite.addTest(tmp);

		if (algorithm == Algorithm.DPOP || algorithm == Algorithm.MB_DPOP || algorithm == Algorithm.P_DPOP || algorithm == Algorithm.P3halves_DPOP 
				|| algorithm == Algorithm.P2_DPOP || algorithm == Algorithm.ADOPT || algorithm == Algorithm.AFB || algorithm == Algorithm.MPC_DisWCSP4) {
			tmp = new TestSuite ("Problems with global constraints and TCP pipes");
			for (File probFile : allGlbProblems.values())
				if (probFile.isFile()) 
					tmp.addTest(new JaCoPtests<U> ("testPureIntensionalProblem", algorithm, AgentDescFile , solverClass, classOfU, true, false, probFile));
			algoTestSuite.addTest(tmp);
		}
		
		return algoTestSuite;
	}

	/** @see junit.framework.TestCase#tearDown() */
	protected void tearDown () throws Exception {
		super.tearDown();
	}

}
