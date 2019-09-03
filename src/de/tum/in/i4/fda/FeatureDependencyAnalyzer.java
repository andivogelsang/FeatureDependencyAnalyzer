package de.tum.in.i4.fda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import de.tum.in.i4.fda.export.ExcelExporter;
import de.tum.in.i4.fda.model.FA;
import de.tum.in.i4.fda.parser.BMWParser;
import de.tum.in.i4.fda.parser.BMW_AIDA_Parser;
import de.tum.in.i4.fda.parser.MANPhevosParser;

public class FeatureDependencyAnalyzer {
  
  private static final Logger LOGGER = Logger.getLogger(FeatureDependencyAnalyzer.class.getName());

  
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FA fa = new FA();
		FAAnalyzer analyzer = new FAAnalyzer(fa);
		ExcelExporter exporter = new ExcelExporter(analyzer);
		

		// Parse input file
		parseInputFile(fa,analyzer);

		//Preprocess data
		analyzer.removeEmptyFA();
		
		
		//Perform some analysis
		performModularityAnalysis(analyzer,exporter);
		
		performFeatureDependencyAnalysis(analyzer,exporter);
		
		//performComponentDistributionAnalysis(analyzer,exporter);
		
		
		
		//performFIOrderAnalysis(analyzer,exporter);

		//performTDAnalysis(analyzer, exporter);

	}

  private static void performModularityAnalysis(FAAnalyzer analyzer, ExcelExporter exporter) {
    analyzer.computeCohesion();
    
  }

  private static void performComponentDistributionAnalysis(FAAnalyzer analyzer,
      ExcelExporter exporter) {
	  analyzer.computePositionInFeature();
	  
	  //analyzer.printPositionData();
	  
	  exporter.exportComponentDistributionAnalysis();
    
  }

  private static void performFIOrderAnalysis(FAAnalyzer analyzer, ExcelExporter exporter) {
		//analyzer.computeSimpleHighOrderDependencies();
		analyzer.computeHighOrderDependencies();
		
		analyzer.printFIOrderReport();
		
		exporter.exportFIOrderAnalysis();
		
	}

	private static void performFeatureDependencyAnalysis(FAAnalyzer analyzer, ExcelExporter exporter) {
		analyzer.calcDependencies();
		analyzer.printDependencyReport();
		
		exporter.exportFeatureDependencies();
		exporter.exportFeatureHierarchy();
		exporter.exportTreeMap();
	}

	private static void performTDAnalysis(FAAnalyzer analyzer, ExcelExporter exporter) {
		List<Integer> dependencies = new ArrayList<Integer>();
		List<Integer> communalComponents = new ArrayList<Integer>();
		List<Integer> components = new ArrayList<Integer>();
		
		dependencies.add(analyzer.getNumberOfDependencies());
		communalComponents.add(analyzer.getNumberOfCommunalComponents());
		components.add(analyzer.getNumberOfComponents());
    
		while (Integer.valueOf(analyzer.getNumberOfDependencies())>0){
			analyzer.extractNCommunalComponents(1);
			dependencies.add (analyzer.getNumberOfDependencies());
			communalComponents.add(analyzer.getNumberOfCommunalComponents());
			components.add(analyzer.getNumberOfComponents());
		}
	  LOGGER.info(Arrays.toString(dependencies.toArray()));
	  LOGGER.info(Arrays.toString(communalComponents.toArray()));

		analyzer.printDependencyReport();


		exporter.exportTechnicalDebtAnalysis(dependencies,communalComponents,components);
	}

	private static void parseInputFile(FA fa, FAAnalyzer analyzer) {
		//Parsing input file 
		//BMWParser parser = new BMWParser(fa);
		//MANPhevosParser parser = new MANPhevosParser(fa);
	  BMW_AIDA_Parser parser = new BMW_AIDA_Parser(fa);
		parser.parse();
		
		LOGGER.info("Number of Features: " + analyzer.numberOfFeatures());
		LOGGER.info("Number of Components: " + analyzer.numberOfComponents());
	}

}
