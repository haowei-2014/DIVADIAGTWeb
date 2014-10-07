package ch.unifr;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.unifr.gabor.*;

public class Step2Gabor {
	public HashMap<String, List<int[][]>> results;
	public ArrayList<Polygon> polygonsGT = new ArrayList<Polygon>();
	
	public HashMap<String, List<int[][]>> getResults(){
		TextLineExtraction tle = new TextLineExtraction();
		polygonsGT = tle.start();
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
		step2Gabor.getResults();
	}
}
