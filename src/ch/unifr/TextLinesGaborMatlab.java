package ch.unifr;

import java.io.File;

import matlabcontrol.*;

public class TextLinesGaborMatlab {
	
	public static void textLinesExtraction() throws MatlabConnectionException, MatlabInvocationException{
		// create proxy
        MatlabProxyFactoryOptions options =
           new MatlabProxyFactoryOptions.Builder()
               .setUsePreviouslyControlledSession(true)
               .setMatlabLocation("/usr/local/MATLAB/R2013b/bin/matlab")
               .build();
        
       MatlabProxyFactory factory = new MatlabProxyFactory(options);
       MatlabProxy proxy = factory.getProxy();

       // call builtin function
       proxy.eval("disp('hello world')");
       
       // call user-defined function (must be on the path)
       proxy.eval("addpath('/home/hao/workspace/DIVADIAWeb2/DIVADIAWI/GaborFilters')");
       proxy.feval("cvGaborTextureSegmentRun", Step1Projection.filePath + Step1Projection.gaborInput, 
    		   Step1Projection.filePath + Step2Gabor.gaborOutput);
/*       proxy.feval("cvGaborTextureSegmentRun", "/home/hao/Eclipse/eclipse/d-008.png_944_325_GaborInput.png", 
    		   "/home/hao/Eclipse/eclipse/d-008.png_944_325_GaborOutput.png");*/
       proxy.eval("rmpath('/home/hao/workspace/DIVADIAWeb2/DIVADIAWI/GaborFilters')");
       
       // close connection
       proxy.disconnect();
       System.out.println("textLinesGaborMatlab is Done!");
	}

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		textLinesExtraction();
	}
}
