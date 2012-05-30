/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2012  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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
<http://frodo2.sourceforge.net/>
*/

/** A graph coloring problem generator */
package frodo2.benchmarks.graphcoloring;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import frodo2.algorithms.RandGraphFactory;
import frodo2.algorithms.RandGraphFactory.Edge;
import frodo2.algorithms.RandGraphFactory.Graph;

/** A graph coloring problem generator
 * @author Thomas Leaute
 */
public class GraphColoring {

	/** Generates a random graph coloring problem and writes it to a file
	 * @param args 	[-soft] nbrNodes density nbrColors [stochNodeRatio]
	 * @throws IOException 	if an error occurs while attempting to write the output file
	 */
	public static void main(String[] args) throws IOException {
		
		// The GNU GPL copyright notice
		System.out.println("FRODO  Copyright (C) 2008-2012  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek\n" +
				"This program comes with ABSOLUTELY NO WARRANTY.\n" +
				"This is free software, and you are welcome to redistribute it\n" +
				"under certain conditions. \n");
		
		if (args.length < 3 || args.length > 7) {
			System.out.println("Usage: " + GraphColoring.class.getSimpleName() + " [-i] [-soft] [-mpc] nbrNodes density nbrColors [stochNodeRatio]\n" +
					"\t -i [optional]               if present, the output problem is expressed in intensional form\n" +
					"\t -soft [optional]            if present, the output is a Max-DisCSP instead of a DisCSP with hard constraints (default is DisCSP)\n" +
					"\t -mpc [optional]             if present, also outputs an alternative problem formulation in which all constraints are public\n" +
					"\t nbrNodes                    the number of nodes\n" +
					"\t density                     the fraction of pairs of nodes that are neighbors of each other\n" +
					"\t nbrColors                   the number of colors\n" +
					"\t stochNodeRatio [optional]   the fraction of nodes whose color is uncontrollable; the output is a StochDCOP (default is 0)");
			System.exit(1);
		}
		
		ArrayList<String> args2 = new ArrayList<String> (Arrays.asList(args));
		
		boolean intensional = args2.remove("-i");
		boolean soft = args2.remove("-soft");
		boolean mpc = args2.remove("-mpc");

		final int nbrNodes = Integer.parseInt(args2.get(0));
		assert nbrNodes >= 2 : "To few nodes (" + nbrNodes + ")";
		final double density = Double.parseDouble(args2.get(1));
		assert density >= 0 && density <= 1 : "The input density is not between 0 and 1: " + density;
		final int nbrColors = Integer.parseInt(args2.get(2));
		assert nbrColors > 0 : "The number of colors must be positive (" + nbrColors + ")";
		
		// Look for the ratio of stochastic nodes
		int nbrStochNodes = 0;
		if (args2.size() >= 4) 
			nbrStochNodes = (int) (nbrNodes * Double.parseDouble(args2.get(3)));
		assert nbrStochNodes >= 0 && nbrStochNodes <= nbrNodes : "Incorrect ratio of stochastic nodes";
		
		// Generate the random graph
		Graph graph = RandGraphFactory.getSizedRandGraph(nbrNodes, (int) (density * nbrNodes * (nbrNodes - 1) / 2.0), 0);
//		new DOTrenderer ("graph", graph.toString(), "twopi");
		
		// Generate the XCSP representation and write it to a file
		Document problem = generateProblem(graph, nbrColors, false, soft, nbrStochNodes, intensional);
		new XMLOutputter(Format.getPrettyFormat()).output(problem, new FileWriter ("graphColoring.xml"));
		System.out.println("Wrote graphColoring.xml");

		if (mpc) {
			problem = generateProblem(graph, nbrColors, true, soft, nbrStochNodes, intensional);
			new XMLOutputter(Format.getPrettyFormat()).output(problem, new FileWriter ("graphColoring_MPC.xml"));
			System.out.println("Wrote graphColoring_MPC.xml");
		}
	}
	
	/** Generates a problem instance
	 * @param soft 				whether to make it a DisCSP (\c false) or a Max-DisCSP (\c true)
	 * @param nbrNodes 			total number of nodes
	 * @param density 			graph density
	 * @param nbrColors 		number of colors
	 * @param nbrStochNodes 	number of uncontrollable nodes
	 * @param intensional 		whether the output should be intensional
	 * @return a problem instance
	 */
	public static Document generateProblem (boolean soft, int nbrNodes, double density, int nbrColors, int nbrStochNodes, boolean intensional) {
		
		Graph graph = RandGraphFactory.getSizedRandGraph(nbrNodes, (int) (density * nbrNodes * (nbrNodes - 1) / 2.0), 0);
		return GraphColoring.generateProblem(graph, nbrColors, false, soft, nbrStochNodes, intensional);
	}

	/** Generates an XCSP representation of a graph coloring problem
	 * @param graph 						the underlying graph
	 * @param nbrColors 					the number of colors
	 * @param publicInteragentConstraints 	whether inter-agent constraints should be public
	 * @param soft 							whether the output should be a Max-DisCSP
	 * @param nbrStochNodes 				the desired number of stochastic nodes
	 * @param intensional 					whether the output should be intensional
	 * @return An XCSP-formatted Document
	 */
	public static Document generateProblem(Graph graph, final int nbrColors, final boolean publicInteragentConstraints, final boolean soft, final int nbrStochNodes, final boolean intensional) {
		
		// Create the root element
		Element probElement = new Element ("instance");
		probElement.setAttribute("noNamespaceSchemaLocation", "src/frodo2/algorithms/XCSPschema" + (soft || !intensional ? "" : "JaCoP") + ".xsd", 
				Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
		
		// Create the "presentation" element
		Element elmt = new Element ("presentation");
		probElement.addContent(elmt);
		elmt.setAttribute("name", "graphColoring");
		elmt.setAttribute("maxConstraintArity", "2");
		elmt.setAttribute("maximize", "false");
		elmt.setAttribute("format", "XCSP 2.1_FRODO");
		
		// Create the "agents" element
		elmt = new Element ("agents");
		probElement.addContent(elmt);
		final int nbrNodes = graph.nodes.size();
		elmt.setAttribute("nbAgents", Integer.toString(nbrNodes - nbrStochNodes));
		for (int varID = nbrStochNodes; varID < nbrNodes; varID++) {
			Element subElmt = new Element ("agent");
			elmt.addContent(subElmt);
			subElmt.setAttribute("name", "a" + varID);
		}

		// Create the "domains" element
		elmt = new Element ("domains");
		probElement.addContent(elmt);
		elmt.setAttribute("nbDomains", "1");
		Element subElmt = new Element ("domain");
		elmt.addContent(subElmt);
		subElmt.setAttribute("name", "colors");
		subElmt.setAttribute("nbValues", Integer.toString(nbrColors));
		subElmt.addContent("1.." + Integer.toString(nbrColors));

		// Create the "variables" element
		elmt = new Element ("variables");
		probElement.addContent(elmt);
		elmt.setAttribute("nbVariables", Integer.toString(graph.nodes.size()));
		for (int varID = 0; varID < nbrNodes; varID++) {
			subElmt = new Element ("variable");
			elmt.addContent(subElmt);
			subElmt.setAttribute("name", "n" + varID);
			subElmt.setAttribute("domain", "colors");
			if (varID < nbrStochNodes) 
				subElmt.setAttribute("type", "random");
			else 
				subElmt.setAttribute("agent", "a" + varID);
		}
		
		if (soft || !intensional) { // extensional

			// Create the "relations" element
			elmt = new Element ("relations");
			probElement.addContent(elmt);
			elmt.setAttribute("nbRelations", "1");

			subElmt = new Element ("relation");
			elmt.addContent(subElmt);
			subElmt.setAttribute("name", "neq");
			subElmt.setAttribute("semantics", "soft");
			subElmt.setAttribute("arity", "2");
			subElmt.setAttribute("defaultCost", "0");
			subElmt.setAttribute("nbTuples", Integer.toString(nbrColors));

			StringBuilder builder = new StringBuilder (soft ? "1: " : "infinity: ");
			for (int i = 1; i < nbrColors; i++) 
				builder.append(Integer.toString(i) + " " + i + " | ");
			builder.append(Integer.toString(nbrColors) + " " + nbrColors);
			subElmt.setText(builder.toString());

		} else { // pure satisfaction, intensional

			// Create the "predicates" element
			elmt = new Element ("predicates");
			probElement.addContent(elmt);
			elmt.setAttribute("nbPredicates", "1");

			subElmt = new Element ("predicate");
			elmt.addContent(subElmt);
			subElmt.setAttribute("name", "neq");

			elmt = new Element ("parameters");
			subElmt.addContent(elmt);
			elmt.setText("int X int Y");

			elmt = new Element ("expression");
			subElmt.addContent(elmt);

			subElmt = new Element ("functional");
			elmt.addContent(subElmt);
			subElmt.setText("ne(X, Y)");
		}
		
		if (nbrStochNodes > 0) {
			
			// Create the "probabilities" element
			elmt = new Element ("probabilities");
			probElement.addContent(elmt);
			elmt.setAttribute("nbProbabilities", Integer.toString(nbrStochNodes));
			
			for (int varID = 0; varID < nbrStochNodes; varID++) {
				
				subElmt = new Element ("probability");
				elmt.addContent(subElmt);
				subElmt.setAttribute("name", "n" + varID + "proba");
				subElmt.setAttribute("semantics", "soft");
				subElmt.setAttribute("arity", "1");
				subElmt.setAttribute("nbTuples", Integer.toString(nbrColors));
				
				// Choose a random probability distribution
				StringBuilder builder = new StringBuilder ();
				double[] probas = new double [nbrColors];
				double sum = 0.0;
				for (int i = 0; i < nbrColors; i++) {
					probas[i] = Math.random();
					sum += probas[i];
				}
				for (int i = 0; i < nbrColors - 1; i++) 
					builder.append(Double.toString(probas[i] / sum) + ": " + (i+1) + " | ");
				builder.append(Double.toString(probas[nbrColors-1] / sum) + ": " + nbrColors);
				subElmt.setText(builder.toString());
			}
		}
		
		// Create the "constraints" element
		Element conElmt = new Element ("constraints");
		probElement.addContent(conElmt);
		
		// Go through all edges in the graph
		for (Edge edge : graph.edges) {
			
			// Skip this constraint if it involves two random variables
			if (Integer.parseInt(edge.source) < nbrStochNodes && Integer.parseInt(edge.dest) < nbrStochNodes) 
				continue;
			
			elmt = new Element ("constraint");
			conElmt.addContent(elmt);
			
			final String n1 = "n" + edge.source;
			final String n2 = "n" + edge.dest;
			
			elmt.setAttribute("name", n1 + "_neq_" + n2);
			elmt.setAttribute("scope", n1 + " " + n2);
			elmt.setAttribute("arity", "2");
			elmt.setAttribute("reference", "neq");
			if (publicInteragentConstraints) 
				elmt.setAttribute("agent", "PUBLIC");
			
			if (! soft && intensional) {
				subElmt = new Element ("parameters");
				elmt.addContent(subElmt);
				subElmt.setText(n1 + " " + n2);
			}
		}
		
		// Add the probability distributions
		for (int varID = 0; varID < nbrStochNodes; varID++) {
			final String varName = "n" + varID;
			
			elmt = new Element ("constraint");
			conElmt.addContent(elmt);
			elmt.setAttribute("name", varName + "dist");
			elmt.setAttribute("scope", varName);
			elmt.setAttribute("arity", "1");
			elmt.setAttribute("reference", varName + "proba");
		}

		conElmt.setAttribute("nbConstraints", Integer.toString(conElmt.getContentSize()));

		return new Document (probElement);
	}

}
