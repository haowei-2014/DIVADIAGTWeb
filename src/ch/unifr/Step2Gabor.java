package ch.unifr;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import ch.unifr.gabor.*;

public class Step2Gabor {
	public static HashMap<String, List<int[][]>> results;
	public static ArrayList<Polygon> polygonsGT = new ArrayList<Polygon>();
	public static int linkingRectWidth;
	public static int linkingRectHeight;
	public static String gaborOutput; // file name of the gabor output image
	
	public static HashMap<String, List<int[][]>> getResults(int offsetX, int offsetY, int linkingRectWidth, int linkingRectHeight){
		Step2Gabor.linkingRectHeight = linkingRectHeight;
		Step2Gabor.linkingRectWidth = linkingRectWidth;
		
		
		gaborOutput = Step1Projection.gaborInput.replace("GaborInput", "GaborOutput");
		// use jar of Gabor filter
		/*		GaborClustering.start(Step1Projection.filePath + Step1Projection.gaborInput
				, Step1Projection.filePath + gaborOutput);*/
		
		// use matlab directly
		try {
			TextLinesGaborMatlab.textLinesExtraction();
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
		Step2Gabor.getResults(0, 0, 0, 0);
	}
}
