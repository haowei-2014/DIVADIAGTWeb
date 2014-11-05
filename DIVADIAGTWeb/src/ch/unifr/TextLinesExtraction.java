package ch.unifr;

import matlabcontrol.*;

public class TextLinesExtraction {
	
	public void textLinesExtraction() throws MatlabConnectionException, MatlabInvocationException{
		// create proxy
        MatlabProxyFactoryOptions options =
           new MatlabProxyFactoryOptions.Builder()
               .setUsePreviouslyControlledSession(true)
               .setMatlabLocation("/usr/local/MATLAB/R2012a/bin/matlab")
               .build();
       MatlabProxyFactory factory = new MatlabProxyFactory(options);
       MatlabProxy proxy = factory.getProxy();

       // call builtin function
       proxy.eval("disp('hello world')");

       // call user-defined function (must be on the path)
       proxy.eval("addpath('/home/weih/Work/divadiaweb/GaborFilters_web')");
       proxy.feval("cvGaborTextureSegmentRun");
       proxy.eval("rmpath('/home/weih/workspace/DIVADIAWeb4/DIVADIAGTWeb/WorkData/')");

       // close connection
       proxy.disconnect();
       System.out.println("Done!");
	}

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		TextLinesExtraction textlinesExtraction = new TextLinesExtraction();
		textlinesExtraction.textLinesExtraction();
	}
}
