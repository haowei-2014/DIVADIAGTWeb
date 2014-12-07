package ch.unifr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class Test {
	
	public static void showEnvironmentVariables(){
		System.out.println("hello");

		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			System.out.format("%s=%s%n", envName, env.get(envName));
		}
	}
	
	public static void callTerminal() throws IOException{
		/*String command= "xterm";
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(command);*/
		
//		Process p = Runtime.getRuntime().exec(new String[]{"bash","-c","ls /home/hao"});
		
		String s = null;
		 
        try {
             
        // run the Unix "ps -ef" command
            // using the Runtime exec method:
 //           Process p = Runtime.getRuntime().exec("ls");
             
        	Process p = Runtime.getRuntime().exec(new String[] {
        			"matlab"
 //       			"addpath('/home/hao/work/DIVADIAGTWeb/GaborFilters')",
  //      			"1+1"
					  });
        	
        	
        	
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
 
            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
 
            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
             
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
             
            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			callTerminal();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
