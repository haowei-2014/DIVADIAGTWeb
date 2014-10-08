package ch.unifr.gabor;

import GaborClustering.*;
import com.mathworks.toolbox.javabuilder.*;

public class GaborClustering {
	
	public void start(String strInput, String strOutput){
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
//	         n = new MWNumericArray(Double.valueOf(args[0]), MWClassID.DOUBLE);
	    //	  String strInput = args[0];
	    //	  String strOutput = args[1];

	         theMagic = new GaborCluster();

	  //       result = theMagic.cvGaborTextureSegmentRun();
	         
	   //      theMagic.cvGaborTextureSegmentRun();
	   //      System.out.println(result[0]);
	         theMagic.cvGaborTextureSegmentRun(strInput, strOutput);
	      }
	      catch (Exception e)
	      {
	         System.out.println("Exception: " + e.toString());
	      }
	      /*finally
	      {
	         MWArray.disposeArray(n);
	         MWArray.disposeArray(result);
	         theMagic.dispose();
	      }*/
	}

	public static void main(String[] args) {		
		GaborClustering gaborClustering = new GaborClustering();
		gaborClustering.start("/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/d-008.0.1091.205.507.2337.png"
				, "/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/GaborOutput.png");
	}

}
