package de.tum.in.i4.fda.test;



import junit.framework.TestCase;

import org.junit.Test;

import de.tum.in.i4.fda.model.FA;
import de.tum.in.i4.fda.model.Feature;
import de.tum.in.i4.fda.parser.BMWParser;

public class ParserTest extends TestCase{
	
	BMWParser parser;
	FA fa;
	
	@Override 
	public void setUp() throws Exception
	  {
		fa=new FA();
		parser = new BMWParser(fa);
	    parser.parse();
	  }

	@Test
	public void testNumberOfFeatures() {
		assertEquals("No correct number of Features are parsed; expected", 142, fa.features.size());
	}
	
	@Test
	public void testACC() {
		Feature acc = fa.getFeatureByName("Active Cruise Control Stop and Go (ACC S&G)");
		assertEquals("ACC has not the correct number of components", 36, fa.fcMapping.get(acc).size());
		assertEquals("ACC Function ACC_Zielobjektauswahl_fuer_RadarOnly_und_Fusion has not the correct number of inputs", 30, fa.getComponentByName("ACC_Zielobjektauswahl_fuer_RadarOnly_und_Fusion").inputs.size());
		assertEquals("ACC Function ACC_Zielobjektauswahl_fuer_RadarOnly_und_Fusion has not the correct number of outputs", 37, fa.getComponentByName("ACC_Zielobjektauswahl_fuer_RadarOnly_und_Fusion").outputs.size());
	}
	
	@Test
	public void testDTC() {
		Feature dtc = fa.getFeatureByName("Dynamic Traction Control (DTC)");
		assertEquals("DTC has not the correct number of functions", 13, fa.fcMapping.get(dtc).size());
		assertEquals("DTC Function ASC-Modul_kommunal has not the correct number of inputs", 18, fa.getComponentByName("ASC-Modul_kommunal").inputs.size());
		assertEquals("DTC Function ASC-Modul_kommunal has not the correct number of outputs", 2, fa.getComponentByName("ASC-Modul_kommunal").outputs.size());
	}
	
	@Test
	public void testFeatureInterface() {
		Feature ls = fa.getFeatureByName("Leerlauf-Segeln");
		Feature acc = fa.getFeatureByName("Active Cruise Control Stop and Go (ACC S&G)");
		assertTrue("Status_Segeln_Antrieb_2 is not part of the inputs of Active Cruise Control Stop and Go (ACC S&G)", acc.getInputs().contains("Status_Segeln_Antrieb_2"));
		assertTrue("Status_Segeln_Antrieb_2 is not part of the outputs of Leerlauf-Segeln", ls.getOutputs().contains("Status_Segeln_Antrieb_2"));
	}
	


	

}
