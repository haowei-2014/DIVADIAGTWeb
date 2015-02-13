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


public class ErasePolygon {

	public static BufferedImage image;
	public static Rectangle polygonBound;
	public static Polygon pNew;
	public static ArrayList<Polygon> polygonsGT  = new ArrayList<Polygon>();
	public static HashMap<String, List<int[][]>> results = new HashMap<String, List<int[][]>>();
	public static String filePath = System.getProperty("user.dir") + File.separator;
	
	// get the bound of the polygon, and write the polygon as an image to the disk
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
			File file = new File(filePath + "splitgeneratePolygonImage.png");
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("generatePolygonImage is finished.");
	}
	
	public static void erasePolygon (int xErase, int yErase){
		xErase -= polygonBound.x;
		yErase -= polygonBound.y;		
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(xErase-1, 0, 3, polygonBound.height);
		
		try {
			System.out.println("filePath: " + filePath);
			File file = new File(filePath + "erasePolygon.png");
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		image = CommonFunctions.convertImage(image);	
		ImagePlus imp = new ImagePlus("test", image);
		ManyBlobs allBlobs = new ManyBlobs(imp); // Extended ArrayList				
		allBlobs.findConnectedComponents(); // Start the Connected Component
		allBlobs = allBlobs.filterBlobs(50,1000000000, Blob.GETENCLOSEDAREA); 
		System.out.println("AllBlobs size: " + allBlobs.size());
		if (allBlobs.size() != 2){
			System.out.println("allBlobs.size is not equal to 2!");
			}
		else {
			if (allBlobs.get(0).getAreaConvexHull() > allBlobs.get(1).getAreaConvexHull()){
				pNew =  CommonFunctions.adjustPolygon(allBlobs.get(0).getOuterContour());
			} else
				pNew =  CommonFunctions.adjustPolygon(allBlobs.get(1).getOuterContour());	
			pNew = DrawGT.interpolatePolygon(pNew);
			polygonsGT.add(pNew);
		}
	}

	public static HashMap<String, List<int[][]>> getResults(MyPoint[] myPoints, int xErase, int yErase){
		polygonsGT.clear();
		results.clear();
		generatePolygonImage (myPoints);
		erasePolygon (xErase, yErase);
		
		List<int[][]> erasePolygonsList = new ArrayList<int[][]>();
		for (Polygon polygon : polygonsGT){
			polygon.translate(polygonBound.x, polygonBound.y); 
			int[][] points = new int[polygon.npoints][2]; 
			for (int i = 0; i < polygon.npoints; i++){
				points[i][0] = polygon.xpoints[i];
				points[i][1] = polygon.ypoints[i];
			}
			erasePolygonsList.add(points);
		}		
		results.put("textLines", erasePolygonsList);	
		return results;
	}

	public static void main(String[] args) {
	}

}
