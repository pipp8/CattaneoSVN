import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.InputMeasure;
import valworkbench.measures.internal.WCSS.InputWCSS;
import valworkbench.measures.internal.WCSS.WCSSGeneric;

public class Test {

	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, IOException, Exception {
		
		
		//Load Data
	    String fName="CNSRat.txt";
		DataMatrix dataMatrix = new DataMatrix();
		dataMatrix.loadFromFile(fName);
		
		int kMin=2,  kMax=10;
		String outputPath="out"+File.separator; 
		String algorithmPath="./HLink.o"; 
		String algCommandLine="<inputfile> <nofcluster> A <outputfile>";
		String algInputPath = fName;
		String algOutputPath = "out"+File.separator; 
		boolean predictNOfCluster = false;
		
		InputWCSS InputwCSSGeneric = new InputWCSS(kMin, kMax, algorithmPath, algCommandLine, outputPath, algInputPath, algOutputPath, predictNOfCluster); 
		WCSSGeneric wCSSGeneric = new WCSSGeneric(dataMatrix, InputwCSSGeneric);
		System.out.println("Start Computing");
		wCSSGeneric.computeMeasure();
		System.out.println("End Computing");
	}

}
