package ch.unifr;

import matlabcontrol.*;

public class TextLinesGaborMatlab {
	
	public void textLinesExtraction() throws MatlabConnectionException, MatlabInvocationException{
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
       proxy.eval("addpath('/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb')");
       proxy.feval("cvGaborTextureSegmentRun");
       proxy.eval("rmpath('/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb')");

       // close connection
       proxy.disconnect();
       System.out.println("textLinesGaborMatlab is Done!");
	}

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		TextLinesGaborMatlab textlinesExtraction = new TextLinesGaborMatlab();
		textlinesExtraction.textLinesExtraction();
	}
}
