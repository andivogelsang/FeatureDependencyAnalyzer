package de.tum.in.i4.fda.export;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.tum.in.i4.fda.FAAnalyzer;
import de.tum.in.i4.fda.model.Component;
import de.tum.in.i4.fda.model.Feature;
import de.tum.in.i4.fda.model.FirstOrderFeatureDependency;
import de.tum.in.i4.fda.model.HighOrderFeatureDependency;

public class ExcelExporter {

  public FAAnalyzer analyzer;

  private Workbook dependencyWb;
  private Workbook technicalDepthWb;
  private Workbook treeMapWb;
  private Workbook featureHierarchyWb;
  private Sheet edgeSheet;
  private Sheet nodeSheet;
  private Sheet distributionSheet;
  private Sheet fiOrderSheet;
  private Sheet tdSheet;
  private Sheet treeMapSheet;
  private Sheet featureHierarchySheet;
  private final String dependencyWbPath = "output/adjMatrix.xlsx";
  private final String tdWbPath = "output/technicalDepthAnalysis.xlsx";
  private final String treeMapWbPath = "output/treeMap.xlsx";
  private final String featureHierarchyWbPath = "output/featureHierarchy.xlsx";

  public ExcelExporter(FAAnalyzer analyzer) {
    this.analyzer = analyzer;

    initializeDependencyWb();

    initializeTechnicalDepthAnalysisWb();

    initializeTreeMapWb();

    initializeFeatureHierarchyWb();
  }

  private void initializeTreeMapWb() {

    treeMapWb = new XSSFWorkbook();

    treeMapSheet = treeMapWb.createSheet("Components");
    treeMapSheet.createRow(0);
    treeMapSheet.getRow(0).createCell(0).setCellValue("Component_FQN");
    treeMapSheet.getRow(0).createCell(1).setCellValue("Dependencies");
    treeMapSheet.getRow(0).createCell(2).setCellValue("Size");

  }

  private void initializeFeatureHierarchyWb() {

    featureHierarchyWb = new XSSFWorkbook();

    featureHierarchySheet = featureHierarchyWb.createSheet("Feature Hierarchy");
    featureHierarchySheet.createRow(0);
    featureHierarchySheet.getRow(0).createCell(0).setCellValue("from");
    featureHierarchySheet.getRow(0).createCell(1).setCellValue("to");

  }

  private void initializeTechnicalDepthAnalysisWb() {

    technicalDepthWb = new XSSFWorkbook();

    tdSheet = technicalDepthWb.createSheet("technicalDepthProgression");
    tdSheet.createRow(0);
    tdSheet.getRow(0).createCell(0).setCellValue("Run");
    tdSheet.getRow(0).createCell(1).setCellValue("Overall Dependencies");
    tdSheet.getRow(0).createCell(2).setCellValue("Dependency Decrease");
    tdSheet.getRow(0).createCell(3).setCellValue("Number of Communal Components");
    tdSheet.getRow(0).createCell(4).setCellValue("Number of Components");

  }

  private void initializeDependencyWb() {
    dependencyWb = new XSSFWorkbook();
    edgeSheet = dependencyWb.createSheet("edgeSheet");
    edgeSheet.createRow(0);
    edgeSheet.getRow(0).createCell(0).setCellValue("Source");
    edgeSheet.getRow(0).createCell(1).setCellValue("Dest");
    edgeSheet.getRow(0).createCell(2).setCellValue("Label");
    edgeSheet.getRow(0).createCell(3).setCellValue("Contributing Components");

    nodeSheet = dependencyWb.createSheet("nodeSheet");
    nodeSheet.createRow(0);
    nodeSheet.getRow(0).createCell(0).setCellValue("Feature");

    distributionSheet = dependencyWb.createSheet("distributionSheet");
    distributionSheet.createRow(0);
    distributionSheet.getRow(0).createCell(0).setCellValue("Feature");
    distributionSheet.getRow(0).createCell(1).setCellValue("# Incoming Dependencies");
    distributionSheet.getRow(0).createCell(2).setCellValue("# Outgoing Dependencies");

    fiOrderSheet = dependencyWb.createSheet("fiOrderSheet");
    fiOrderSheet.createRow(0);
    fiOrderSheet.getRow(0).createCell(0).setCellValue("Source");
    fiOrderSheet.getRow(0).createCell(1).setCellValue("Path");
    fiOrderSheet.getRow(0).createCell(2).setCellValue("Dest");
    fiOrderSheet.getRow(0).createCell(3).setCellValue("Order");
  }

  public void exportFeatureDependencies() {
    int edgeSheetRow = -1;
    int nodeSheetRow = -1;
    FileOutputStream fileOut;

    for (Feature f : analyzer.fa.features) {
      // write node sheet
      nodeSheetRow = nodeSheet.getLastRowNum() + 1;
      nodeSheet.createRow(nodeSheetRow);
      nodeSheet.getRow(nodeSheetRow).createCell(0).setCellValue(f.name);

      // write edgeSheet
      String sourceFeature = f.name;
      for (Feature g : analyzer.getOutgoingFeatures(f)) {
        String targetFeature = g.name;
        Collection<String> contributingComponents = new HashSet<String>();
        Collection<String> contributingSignals = new HashSet<String>();
        for (FirstOrderFeatureDependency dep : analyzer.getFirstOrderDependenciesForFeatures(f,
            g)) {
          contributingComponents.add(dep.sourceComponent.name);
          contributingSignals.addAll(dep.sharedChannels);
        }
        edgeSheetRow = edgeSheet.getLastRowNum() + 1;
        edgeSheet.createRow(edgeSheetRow);
        edgeSheet.getRow(edgeSheetRow).createCell(0).setCellValue(sourceFeature);
        edgeSheet.getRow(edgeSheetRow).createCell(1).setCellValue(targetFeature);
        edgeSheet.getRow(edgeSheetRow).createCell(2).setCellValue(contributingSignals.toString());
        edgeSheet.getRow(edgeSheetRow).createCell(3)
            .setCellValue(contributingComponents.toString());
      }
    }

    try {
      fileOut = new FileOutputStream(dependencyWbPath);
      dependencyWb.write(fileOut);
      fileOut.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void exportTechnicalDebtAnalysis(List<Integer> dependencies,
      List<Integer> communalComponents, List<Integer> components) {
    int tdSheetRow = -1;
    FileOutputStream fileOut;
    int run = 0;

    for (int nrDep : dependencies) {
      tdSheetRow = tdSheet.getLastRowNum() + 1;
      tdSheet.createRow(tdSheetRow);

      tdSheet.getRow(tdSheetRow).createCell(0).setCellValue(run);
      tdSheet.getRow(tdSheetRow).createCell(1).setCellValue(nrDep);
      if (run > 0) {
        int dependencyDecrease = dependencies.get(run - 1) - nrDep;
        tdSheet.getRow(tdSheetRow).createCell(2).setCellValue(dependencyDecrease);
      }
      tdSheet.getRow(tdSheetRow).createCell(3).setCellValue(communalComponents.get(run));
      tdSheet.getRow(tdSheetRow).createCell(4).setCellValue(components.get(run));
      run++;
    }
    try {
      fileOut = new FileOutputStream(tdWbPath);
      technicalDepthWb.write(fileOut);
      fileOut.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void exportTreeMap() {
    int treeMapSheetRow = -1;
    FileOutputStream fileOut;

    for (Component c : analyzer.fa.components) {
      Feature f = analyzer.getRandomContainingFeature(c);
      if (f != null) {
        treeMapSheetRow = treeMapSheet.getLastRowNum() + 1;
        treeMapSheet.createRow(treeMapSheetRow);
        treeMapSheet.getRow(treeMapSheetRow).createCell(0).setCellValue(f.fqn + "." + c.name);
        Integer nrDep = analyzer.getNumberOfDependencies(c);
        treeMapSheet.getRow(treeMapSheetRow).createCell(1).setCellValue(nrDep);
        Integer size = analyzer.getNumberOfChannels(c);
        treeMapSheet.getRow(treeMapSheetRow).createCell(2).setCellValue(size);
      }
    }

    try {
      fileOut = new FileOutputStream(treeMapWbPath);
      treeMapWb.write(fileOut);
      fileOut.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void exportFIOrderAnalysis() {
    int fiOrderSheetRow = -1;
    FileOutputStream fileOut;

    for (Feature f : analyzer.fa.features) {
      for (Feature g : analyzer.getOutgoingFeatures(f)) {
        if (!analyzer.getFirstOrderDependenciesForFeatures(f, g).isEmpty()) {
          fiOrderSheetRow = fiOrderSheet.getLastRowNum() + 1;
          fiOrderSheet.createRow(fiOrderSheetRow);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(0).setCellValue(f.name);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(2).setCellValue(g.name);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(3).setCellValue(1);
        }

        for (HighOrderFeatureDependency dep : analyzer.getHighOrderDependenciesForFeatures(f, g)) {
          fiOrderSheetRow = fiOrderSheet.getLastRowNum() + 1;
          fiOrderSheet.createRow(fiOrderSheetRow);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(0).setCellValue(f.name);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(1).setCellValue(
              dep.connectingFeatures.stream().map(cf -> cf.name).collect(Collectors.joining(",")));
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(2).setCellValue(g.name);
          fiOrderSheet.getRow(fiOrderSheetRow).createCell(3).setCellValue(dep.order);
        }
      }
    }

    try {
      fileOut = new FileOutputStream(dependencyWbPath);
      dependencyWb.write(fileOut);
      fileOut.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void exportFeatureHierarchy() {

    Collection<String> hierarchyTuples = new HashSet<String>();
    for (Feature f : analyzer.fa.features) {
      String[] nodes = f.fqn.split("\\.");
      hierarchyTuples.add("origin@"+ nodes[0]);
      for (int i = 1; i < nodes.length; i++) {
        hierarchyTuples.add(nodes[i - 1]+ "@" +nodes[i]);
      }
    }    
    
    // export the graph
    int fhSheetRow = -1;
    FileOutputStream fileOut;

    for (String tuple : hierarchyTuples) {
      fhSheetRow = featureHierarchySheet.getLastRowNum() + 1;
      featureHierarchySheet.createRow(fhSheetRow);
      featureHierarchySheet.getRow(fhSheetRow).createCell(0).setCellValue(tuple.split("@")[0]);
      featureHierarchySheet.getRow(fhSheetRow).createCell(1).setCellValue(tuple.split("@")[1]);
    }

    try {
      fileOut = new FileOutputStream(featureHierarchyWbPath);
      featureHierarchyWb.write(fileOut);
      fileOut.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
