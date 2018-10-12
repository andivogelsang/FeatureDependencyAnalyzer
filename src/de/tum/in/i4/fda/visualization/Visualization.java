package de.tum.in.i4.fda.visualization;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import de.tum.in.i4.fda.FAAnalyzer;
import de.tum.in.i4.fda.model.Component;
import de.tum.in.i4.fda.model.Feature;
import de.tum.in.i4.fda.model.FeatureDependency;
import de.tum.in.i4.fda.model.FirstOrderFeatureDependency;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.GraphMLWriter;

public class Visualization {

	private FAAnalyzer analyzer;

	Graph<String, MyEdge> faGraph;
	Graph<String, MyEdge> laGraph;
	private Workbook wb;
	private Sheet edgeSheet;
	private Sheet nodeSheet;
	private Sheet distributionSheet;
	private final String wbPath = "output/adjMatrix.xls";

	public Visualization(FAAnalyzer analyzer) {
		this.analyzer = analyzer;

		faGraph = new DirectedSparseMultigraph<String, MyEdge>();
		laGraph = new DirectedSparseMultigraph<String, MyEdge>();

		wb = new HSSFWorkbook();
		edgeSheet = wb.createSheet("edgeSheet");
		edgeSheet.createRow(0);
		edgeSheet.getRow(0).createCell(0).setCellValue("Source");
		edgeSheet.getRow(0).createCell(1).setCellValue("Dest");
		edgeSheet.getRow(0).createCell(2).setCellValue("Label");
		edgeSheet.getRow(0).createCell(3).setCellValue("Source Leaf Function");
		edgeSheet.getRow(0).createCell(4).setCellValue("Target Leaf Function");

		nodeSheet = wb.createSheet("nodeSheet");
		nodeSheet.createRow(0);
		nodeSheet.getRow(0).createCell(0).setCellValue("Feature");
		nodeSheet.getRow(0).createCell(1).setCellValue("PageRank");

		distributionSheet = wb.createSheet("distributionSheet");
		distributionSheet.createRow(0);
		distributionSheet.getRow(0).createCell(0).setCellValue("Feature");
		distributionSheet.getRow(0).createCell(1).setCellValue("# Incoming Dependencies");
		distributionSheet.getRow(0).createCell(2).setCellValue("# Outgoing Dependencies");
	}

	// public void visualizeMinFA() {
	// for (Feature f : analyzer.fa.features) {
	// Map<Feature, Collection<String>> dep = f.dependencies;
	// if (!dep.isEmpty()) {
	// for (Feature g : dep.keySet()) {
	// addEdge(faGraph, f.name, g.name, dep.get(g).iterator().next());
	// }
	// }
	// }
	// }

	// considers only dependencies with type "data dependency"
	public void visualizePureFA() {
		for (Feature f : analyzer.fa.features) {
			addNode(faGraph, f.name);
		}
		for (FeatureDependency dep : analyzer.fa.featureDependencies) {
			if (dep instanceof FirstOrderFeatureDependency) {
				
				addEdge(faGraph, dep.sourceFeature.name, dep.targetFeature.name, ((FirstOrderFeatureDependency) dep).sharedChannels.toString());
			}
		}

		// foldEdges(faGraph);
		// clusterGraph(faGraph);
		writeExcel(faGraph);
	}

	/*
	 * // with dependencies of type "shared component" public void public void
	 * visualizeSuperFA() { for (Feature f : FA.getInstance().fa.values()) {
	 * addNode(faGraph, f.getName()); for (Feature g :
	 * FA.getInstance().fa.values()) { if (!f.equals(g)) { for (Component ffkn :
	 * f.functions.values()) { for (Component gfkn : g.functions.values()) { if
	 * (!f.functions.containsKey(gfkn.name)) { Set<String> depSet =
	 * ffkn.dependsOn(gfkn); if (!depSet.isEmpty()) { for (String depEdge :
	 * depSet) { addEdge(faGraph, f.getName(), g.getName(), depEdge, ffkn.name,
	 * gfkn.name); } } } } } } } } // foldEdges(faGraph); //
	 * clusterGraph(faGraph); writeExcel(faGraph); }
	 */

	private void foldEdges(Graph<String, MyEdge> g) {
		for (String src : g.getVertices()) {
			for (String des : g.getVertices()) {
				Collection<MyEdge> edgeSet = g.findEdgeSet(src, des);
				if (!edgeSet.isEmpty()) {
					String edgeSpec = "";
					for (MyEdge e : edgeSet) {
						edgeSpec += e.getLabel() + ", ";
						g.removeEdge(e);
					}
					g.addEdge(new MyEdge(edgeSpec, src, des), src, des);
				}
			}
		}
	}

	private void writeExcel(Graph<String, MyEdge> g) {
		int row = -1;
		FileOutputStream fileOut;
		for (MyEdge e : g.getEdges()) {
			row = edgeSheet.getLastRowNum() + 1;
			edgeSheet.createRow(row);
			edgeSheet.getRow(row).createCell(0).setCellValue(e.getSource());
			edgeSheet.getRow(row).createCell(1).setCellValue(e.getDest());
			edgeSheet.getRow(row).createCell(2).setCellValue(e.getLabel());
			if (e.getSourceFunction() != null) {
				edgeSheet.getRow(row).createCell(3).setCellValue(e.getSourceFunction());
			}
			if (e.getTargetFunction() != null) {
				edgeSheet.getRow(row).createCell(4).setCellValue(e.getTargetFunction());
			}
		}

		PageRank<String, MyEdge> ranker = new PageRank<String, MyEdge>(g, 0.15);
		ranker.evaluate();

		row = -1;
		for (String v : g.getVertices()) {
			row = nodeSheet.getLastRowNum() + 1;
			nodeSheet.createRow(row);
			nodeSheet.getRow(row).createCell(0).setCellValue(v);
			nodeSheet.getRow(row).createCell(1).setCellValue(ranker.getVertexScore(v));
		}
		try {
			fileOut = new FileOutputStream(wbPath);
			wb.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addNode(Graph<String, MyEdge> g, String name) {
		g.addVertex(name);
	}

	public void addEdge(Graph<String, MyEdge> g, String from, String to, String label) {
		g.addEdge(new MyEdge(label, from, to), from, to);
	}

	public void addEdge(Graph<String, MyEdge> g, String from, String to, String label, String sourceFunction,
			String targetFunction) {
		g.addEdge(new MyEdge(label, from, to, sourceFunction, targetFunction), from, to);
	}

	public void visualizeLA() {
		Collection<Component> fkns = new HashSet<Component>();
		for (Component c : analyzer.fa.components) {
			addNode(laGraph, c.name);
			fkns.add(c);
		}
		for (Component c1 : analyzer.fa.components) {
			for (Component c2 : analyzer.fa.components) {
				for (String out : c1.outputs) {
					if (c2.inputs.contains(out))
						addEdge(laGraph, c1.name, c2.name, out);
				}
			}
		}
		foldEdges(laGraph);
		writeExcel(laGraph);
	}

	public void clusterGraph(Graph<String, MyEdge> g) {
		VoltageClusterer<String, MyEdge> c = new VoltageClusterer<String, MyEdge>(g, 5);
		Collection<Set<String>> clusters = c.cluster(5);
		// EdgeBetweennessClusterer<String, MyEdge> c = new
		// EdgeBetweennessClusterer<String, MyEdge>(
		// 15);
		// Set<Set<String>> clusters = c.transform(g);
		int clustercount = 0;
		for (Set<String> cluster : clusters) {
			clustercount++;
			String clustername = "cluster" + clustercount;
			faGraph.addVertex(clustername);
			for (String n : cluster) {
				faGraph.addEdge(new MyEdge("", clustername, n), clustername, n);
			}
		}
	}

	public void toGraphML(Graph<String, MyEdge> g) {
		GraphMLWriter<String, MyEdge> graphWriter = new GraphMLWriter<String, MyEdge>();

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("model/graph.graphml")));
			graphWriter.save(g, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showDependencyDistribution() {
		// only works after visualizePureFa
		int row = -1;
		FileOutputStream fileOut;
		for (String n : faGraph.getVertices()) {
			row = distributionSheet.getLastRowNum() + 1;
			distributionSheet.createRow(row);
			distributionSheet.getRow(row).createCell(0).setCellValue(n);
			distributionSheet.getRow(row).createCell(1).setCellValue(faGraph.inDegree(n));
			distributionSheet.getRow(row).createCell(2).setCellValue(faGraph.outDegree(n));
		}
		try {
			fileOut = new FileOutputStream(wbPath);
			wb.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
