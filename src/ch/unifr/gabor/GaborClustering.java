package ch.unifr.gabor;

import GaborClustering.*;
import com.mathworks.toolbox.javabuilder.*;

public class GaborClustering {
	
	public static void start(String strInput, String strOutput){
		MWNumericArray n = null;
	      Object[] result = null;
	      GaborCluster theMagic = null;

	      if (strInput == null || strOutput == null)
	      {
	        System.out.println("Error: must input two string parameters");
	        return;
	      }
	      try
	      {
	         theMagic = new GaborCluster();
	         theMagic.cvGaborTextureSegmentRun(strInput, strOutput);
	      }
	      catch (Exception e)
	      {
	         System.out.println("Exception: " + e.toString());
	      }
	}
	

	public static void main(String[] args) {		
		GaborClustering.start("/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/d-008.0.1091.205.507.2337.png"
				, "/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/GaborOutput.png");
	}

}
