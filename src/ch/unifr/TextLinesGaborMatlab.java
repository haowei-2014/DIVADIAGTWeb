package ch.unifr;

import java.io.File;
import matlabcontrol.*;

public class TextLinesGaborMatlab {
	public static void textLinesExtraction(String gaborInput, String gaborOutput) throws MatlabConnectionException,
			MatlabInvocationException {
		// create proxy
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setUsePreviouslyControlledSession(true)
				.setMatlabLocation("/usr/local/MATLAB/R2013b/bin/matlab")
				.build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = factory.getProxy();
		// call builtin function
		proxy.eval("disp('hello world')");
		// call user-defined function (must be on the path)
		System.out.println("Matlab is reading image: " + gaborInput);
		proxy.eval("addpath('/home/hao/workspace/DIVADIAWeb2/DIVADIAWI/GaborFilters')");
		proxy.feval("cvGaborTextureSegmentRun", gaborInput, gaborOutput);
		
		 /*proxy.feval("cvGaborTextureSegmentRun",
		 "/home/hao/Eclipse/eclipse/tmpData/_744_313_GaborInput.png",
		 "/home/hao/Eclipse/eclipse/tmpData/_744_313_GaborOutput.png");*/
		 
		proxy.eval("rmpath('/home/hao/workspace/DIVADIAWeb2/DIVADIAWI/GaborFilters')");
		// close connection
		proxy.disconnect();
		System.out.println("textLinesGaborMatlab is Done!");
	}

	public static void main(String[] args) throws MatlabConnectionException,
			MatlabInvocationException {
		textLinesExtraction("", "");
	}
}