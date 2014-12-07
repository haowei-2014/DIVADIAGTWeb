package ch.unifr.modification;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import ch.unifr.Step1Projection;
import ch.unifr.SplitServlet.MyPoint;
import ch.unifr.gabor.CommonFunctions;
import ch.unifr.gabor.DrawGT;


public class SplitPolygon {

	public static BufferedImage image;
	public static Rectangle polygonBound;
	public static Polygon pNew1;
	public static Polygon pNew2;
	public static ArrayList<Polygon> polygonsGT  = new ArrayList<Polygon>();
	public static HashMap<String, List<int[][]>> results = new HashMap<String, List<int[][]>>();
	
	public static void generatePolygonImage (MyPoint[] points){		
		int[] xpoints = new int[points.length];
		int[] ypoints = new int[points.length];
		for (int i = 0; i < points.length; i++){
			xpoints[i] = points[i].x;
			ypoints[i] = points[i].y;
		}	
		Polygon polygon = new Polygon(xpoints, ypoints, points.length);
		polygonBound = polygon.getBounds();
		
		int[] xNewPoints = new int[points.length];
		int[] yNewPoints = new int[points.length];
		for (int i = 0; i < points.length; i++){
			xNewPoints[i] = xpoints[i] - polygonBound.x;
			yNewPoints[i] = ypoints[i] - polygonBound.y;
		}
		Polygon newPolygon = new Polygon(xNewPoints, yNewPoints, points.length);
		
		image = new BufferedImage(polygonBound.width, polygonBound.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect ( 0, 0, image.getWidth(), image.getHeight());
		g2d.setColor(Color.black);
		g2d.fillPolygon(newPolygon);
		
		try {
			File file = new File(Step1Projection.filePath + "splitgeneratePolygonImage.png");
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("generatePolygonImage is finished.");
	}
	
	public static void splitPolygon (int xSplit, int ySplit){
		xSplit -= polygonBound.x;
		ySplit -= polygonBound.y;
		// height of the splitting rectangle	
		int heightRect = 20;
		int incrementalWidth = 20;
		int xRect = xSplit - incrementalWidth/2;
		int yRect = ySplit - heightRect/2;
		// increase the width of the linking rectangle until the polygon is split
		for (int widthRect = 60; widthRect < 1500; widthRect+=incrementalWidth) {
			System.out.println("Splitting rectangle width: " + widthRect);
			xRect = xRect - incrementalWidth/2;
			Graphics2D g2d = image.createGraphics();
			g2d.setColor(Color.white);
			g2d.fillRect(xRect, yRect, widthRect, heightRect);
			if (xRect < 0)
				xRect = 0;
			if (yRect < 0)
				yRect = 0;
			if (xSplit + widthRect/2 >= polygonBound.width)
				widthRect = polygonBound.width - xRect;
			if (ySplit + heightRect/2 >= polygonBound.height)
				heightRect = polygonBound.height - yRect;
			g2d.fillRect(xRect, yRect, widthRect, heightRect);
//			g2d.fillRect(xSplit-5, ySplit-2, 10, 4);
			
			try {
				File file = new File(Step1Projection.filePath + "splitsplitPolygon.png");
				ImageIO.write(image, "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (generate2NewPolygons())
				break;
		}
		System.out.println("splitPolygon is finished.");
	}
	
/*	for (int j = 0; j < commonXPoints.size(); j++) {
		if (upper && commonXPoints.get(j).y > mean)
			commonXPoints.get(j).y += expand; // lower
		if (!upper && commonXPoints.get(j).y < mean)
			commonXPoints.get(j).y -= expand; // lower
	}*/
	
	public static boolean generate2NewPolygons(){
		image = CommonFunctions.convertImage(image);	
		ImagePlus imp = new ImagePlus("test", image);
		ManyBlobs allBlobs = new ManyBlobs(imp); // Extended ArrayList				
		allBlobs.findConnectedComponents(); // Start the Connected Component
		allBlobs = allBlobs.filterBlobs(500,1000000000, Blob.GETENCLOSEDAREA); 
		System.out.println("AllBlobs size: " + allBlobs.size());
		if (allBlobs.size() != 2)
			return false;
		pNew1 =  CommonFunctions.adjustPolygon(allBlobs.get(0).getOuterContour());
		pNew2 =  CommonFunctions.adjustPolygon(allBlobs.get(1).getOuterContour());
		pNew1 = DrawGT.adjustPolygon(pNew1);
		pNew2 = DrawGT.adjustPolygon(pNew2);
		if (pNew1.getBounds().y < pNew2.getBounds().y){	
			pNew1.translate(0, 2); 
			pNew2.translate(0, -2); 
			DrawGT.separateTwoPolygons(pNew1, pNew2);
		} else {
			pNew1.translate(0, -2); 
			pNew2.translate(0, 2); 
			DrawGT.separateTwoPolygons(pNew2, pNew1);
		}
		polygonsGT.add(pNew1);
		polygonsGT.add(pNew2);	
		
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.black);
		g2d.fillPolygon(pNew1);
		g2d.fillPolygon(pNew2);		
		try {
			File file = new File(Step1Projection.filePath + "splitgenerate2NewPolygons.png");
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("generate2NewPolygons is finished.");
		return true;
	}

	public static HashMap<String, List<int[][]>> getResults(MyPoint[] myPoints, int xSplit, int ySplit){
		polygonsGT.clear();
		results.clear();
		generatePolygonImage (myPoints);
		splitPolygon (xSplit, ySplit);
//		generate2NewPolygons();
		
		List<int[][]> splitPolygonsList = new ArrayList<int[][]>();
		for (Polygon polygon : polygonsGT){
			polygon.translate(polygonBound.x, polygonBound.y); 
			int[][] points = new int[polygon.npoints][2]; 
			for (int i = 0; i < polygon.npoints; i++){
				points[i][0] = polygon.xpoints[i];
				points[i][1] = polygon.ypoints[i];
			}
			splitPolygonsList.add(points);
		}		
		results.put("textLines", splitPolygonsList);	
		return results;
	}

	public static void main(String[] args) {
	}

}
