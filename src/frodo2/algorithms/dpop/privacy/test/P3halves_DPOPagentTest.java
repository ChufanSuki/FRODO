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

package frodo2.algorithms.dpop.privacy.test;

import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import frodo2.algorithms.AgentFactory;
import frodo2.algorithms.Solution;
import frodo2.algorithms.XCSPparser;
import frodo2.algorithms.dpop.DPOPsolver;
import frodo2.algorithms.dpop.UTILpropagation;
import frodo2.algorithms.dpop.privacy.CollaborativeDecryption;
import frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver;
import frodo2.algorithms.dpop.privacy.VariableObfuscation;
import frodo2.algorithms.dpop.privacy.test.FakeCryptoScheme.FakeEncryptedInteger;
import frodo2.algorithms.test.AllTests;
import frodo2.algorithms.varOrdering.dfs.DFSgenerationWithOrder;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.AddableLimited;
import frodo2.solutionSpaces.AddableReal;
import frodo2.solutionSpaces.crypto.AddableBigInteger;
import frodo2.solutionSpaces.crypto.CryptoScheme;
import frodo2.solutionSpaces.crypto.ElGamalBigInteger;
import frodo2.solutionSpaces.crypto.ElGamalScheme;
import junit.extensions.RepeatedTest;
import junit.framework.TestSuite;

/** A JUnit test for the P3/2-DPOP agent
 * @author Thomas Leaute
 * @param <V> the type used for variable values
 * @param <E> The class used for encrypted values
 */
public class P3halves_DPOPagentTest < V extends Addable<V>, E extends AddableLimited<AddableInteger, E> > extends P2_DPOPagentTest<V, E> {

	/** Constructor
	 * @param domClass 		The class used for variable values
	 * @param schemeClass 	The class of the CryptoScheme
	 * @param classOfE 		The class used for encrypted values
	 * @param mergeBack 	Whether to enable the merging of back-edges
	 * @param minNCCCs 		Whether to minimize the NCCC
	 * @param useTCP 		Whether to use TCP pipes
	 */
	public P3halves_DPOPagentTest(Class<V> domClass, Class<? extends CryptoScheme<AddableInteger, E, ?>> schemeClass,
			Class<E> classOfE, boolean mergeBack, boolean minNCCCs, boolean useTCP) {
		super(domClass, schemeClass, classOfE, mergeBack, minNCCCs, useTCP, true, 0);
	}
	
	/** @return the test suite for this test */
	public static TestSuite suite () {
		TestSuite testSuite = new TestSuite ("Tests for the P3/2-DPOP agent");
		
		TestSuite testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with FakeCryptoScheme with mergeBack");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, FakeEncryptedInteger> (AddableInteger.class, FakeCryptoScheme.class, FakeEncryptedInteger.class, true, false, false), 12000));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with FakeCryptoScheme with mergeBack and TCP pipes");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, FakeEncryptedInteger> (AddableInteger.class, FakeCryptoScheme.class, FakeEncryptedInteger.class, true, false, true), 100));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with FakeCryptoScheme with mergeBack and with minNCCCs");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, FakeEncryptedInteger> (AddableInteger.class, FakeCryptoScheme.class, FakeEncryptedInteger.class, true, true, false), 12000));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with FakeCryptoScheme with mergeBack with real-valued variables");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableReal, FakeEncryptedInteger> (AddableReal.class, FakeCryptoScheme.class, FakeEncryptedInteger.class, true, false, false), 25000));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with FakeCryptoScheme without mergeBack");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, FakeEncryptedInteger> (AddableInteger.class, FakeCryptoScheme.class, FakeEncryptedInteger.class, false, false, false), 5000));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with ElGamalScheme with mergeBack");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, ElGamalBigInteger> (AddableInteger.class, ElGamalScheme.class, ElGamalBigInteger.class, true, false, false), 250));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with ElGamalScheme with mergeBack and TCP pipes");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, ElGamalBigInteger> (AddableInteger.class, ElGamalScheme.class, ElGamalBigInteger.class, true, false, true), 100));
		testSuite.addTest(testTmp);
		
		testTmp = new TestSuite ("Tests for P3/2-DPOP vs DPOP with ElGamalScheme without mergeBack");
		testTmp.addTest(new RepeatedTest (new P3halves_DPOPagentTest<AddableInteger, ElGamalBigInteger> (AddableInteger.class, ElGamalScheme.class, ElGamalBigInteger.class, false, false, false), 250));
		testSuite.addTest(testTmp);
		
		return testSuite;
	}

	/** @see P2_DPOPagentTest#test() */
	@Override
	public void test() throws JDOMException, IOException {
		
		//Create new random problem
		Document problem = AllTests.createRandProblem(maxVar, maxEdge, maxAgent, true, 0, 0.5);
		XCSPparser<V, AddableBigInteger> parser = new XCSPparser<V, AddableBigInteger>(problem);
		parser.setUtilClass(AddableBigInteger.class);
		
		// Set the CryptoScheme and the mergeBack and minNCCCs flags
		Document agentDesc = XCSPparser.parse(AgentFactory.class.getResourceAsStream("/frodo2/algorithms/dpop/privacy/P1.5-DPOPagent.xml"), false);
		for (Element module : (List<Element>) agentDesc.getRootElement().getChild("modules").getChildren()) {
			String className = module.getAttributeValue("className");
			if (className.equals(CollaborativeDecryption.class.getName())) {
				
				Element schemeElmt = module.getChild("cryptoScheme");
				schemeElmt.setAttribute("className", this.schemeClass.getName());
				
			} else if (className.equals(VariableObfuscation.class.getName()))
				module.setAttribute("mergeBack", Boolean.toString(this.mergeBack));
								
			else if (className.equals(UTILpropagation.class.getName())) 
				module.setAttribute("minNCCCs", Boolean.toString(this.minNCCCs));
				
			else if (className.equals(DFSgenerationWithOrder.class.getName())) 
				module.setAttribute("minIncr", "2");
		}
		
		// Compute both solutions
		Solution<V, AddableBigInteger> solution = new P3halves_DPOPsolver<V> (agentDesc, this.domClass, this.useTCP)
			.solve(problem, parser.getNbrVars(), 300000L);
		Solution<V, AddableBigInteger> dpopSolution = new DPOPsolver<V, AddableBigInteger>(this.domClass, AddableBigInteger.class).solve(problem, parser.getNbrVars());
		
		assertNotNull ("P3/2-DPOP timed out", solution);
				
		// Verify the utilities of the solutions found by P3/2-DPOP and DPOP
		assertEquals("P2-DPOP's and DPOP's utilities are different", dpopSolution.getUtility(), solution.getUtility());
		
		// Verify that P3/2-DPOP's chosen assignments indeed have the correct utility
		if (! this.domClass.equals(AddableReal.class)) 
			assertEquals("The chosen assignments' utility differs from the actual utility", 
					solution.getUtility(), parser.getUtility(solution.getAssignments()).getUtility(0));
		
		// Check that the reported utility is correct 
		assertEquals("The actual utility differs from the reported utility", solution.getUtility(), solution.getReportedUtil());		
	}
	
	

}
