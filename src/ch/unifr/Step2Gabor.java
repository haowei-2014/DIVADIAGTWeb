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
	public int linkingRectWidth;
	public int linkingRectHeight;
	public String gaborOutput; // file name of the gabor output image
	
	public HashMap<String, List<int[][]>> getResults(int offsetX, int offsetY, 
			int linkingRectWidth, int linkingRectHeight, Info info){
		linkingRectHeight = linkingRectHeight;
		linkingRectWidth = linkingRectWidth;
		
		
		gaborOutput = info.gaborOutput;
		// use jar of Gabor filter
		/*		GaborClustering.start(Step1Projection.filePath + Step1Projection.gaborInput
				, Step1Projection.filePath + gaborOutput);*/
		
		// use matlab directly
		try {
			TextLinesGaborMatlab.textLinesExtraction(info.filePath + info.gaborInput , info.filePath + info.gaborOutput);
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			e.printStackTrace();
		}
		
		TextLineExtraction tle = new TextLineExtraction();
		System.out.println("offsetX is: " + offsetX + ", offsetY is: " + offsetY);
		polygonsGT = tle.start(offsetX, offsetY, info);
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
		
//		step2Gabor.getResults(0, 0, 0, 0, );
	}
}
