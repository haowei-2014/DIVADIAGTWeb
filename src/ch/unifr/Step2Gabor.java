package ch.unifr;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import ch.unifr.gabor.*;

public class Step2Gabor {
	public HashMap<String, List<int[][]>> results;
	public ArrayList<Polygon> polygonsGT = new ArrayList<Polygon>();
	public static int linkingRectWidth;
	public static int linkingRectHeight;
	
	public HashMap<String, List<int[][]>> getResults(int offsetX, int offsetY, int linkingRectWidth, int linkingRectHeight){
		Step2Gabor.linkingRectHeight = linkingRectHeight;
		Step2Gabor.linkingRectWidth = linkingRectWidth;
		
		/*GaborClustering gaborClustering = new GaborClustering();
		gaborClustering.start("/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/d-008.0.1091.205.507.2337.png"
				, "/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/GaborOutput.png");*/
		
		TextLinesGaborMatlab textlinesExtraction = new TextLinesGaborMatlab();
		try {
			textlinesExtraction.textLinesExtraction();
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			e.printStackTrace();
		}
		
		TextLineExtraction tle = new TextLineExtraction();
		polygonsGT = tle.start(offsetX, offsetY);
		results = new HashMap<String, List<int[][]>>();	
		List<int[][]> textLinesList = new ArrayList<int[][]>();

		for (Polygon polygon : polygonsGT){
			int[][] points = new int[polygon.npoints][2]; 
			for (int i = 0; i < polygon.npoints; i++){
				points[i][0] = polygon.xpoints[i];
				points[i][1] = polygon.ypoints[i];
			}
			textLinesList.add(points);
		}
		
		results.put("textLines", textLinesList);	
		return results;
	}

	public static void main(String[] args) {
		Step2Gabor step2Gabor = new Step2Gabor();
		step2Gabor.getResults(0, 0, 0, 0);
	}
}
