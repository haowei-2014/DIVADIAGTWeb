package ch.unifr.gabor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class CommonFunctions {
	/** This function is to convert the values of pixels on the image to either 0 or 255.
	 * @param inputImage
	 * @return
	 */
	public static BufferedImage convertImage(BufferedImage inputImage) {
		BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);
		int[] iArray=new int[1];
		
		WritableRaster raster = outputImage.getRaster(); 
		for (int i = 0; i < inputImage.getHeight(); i++)
			for (int j = 0; j < inputImage.getWidth(); j++) {
				if (inputImage.getRaster().getSample(j,i, 0) < 125)
				{
					iArray[0] = 0;
					raster.setPixel(j, i, iArray);
				}
				else{
					iArray[0] = 255;
					raster.setPixel(j, i, iArray);
				}	
			}
		return outputImage;
	}
	
	/** adjust the position of polygon, as the polygon obtained by ijblob has an offset which is
	 * one pixel to the right and one pixel to the bottom.
	 * @param inputP
	 * @return
	 */
	public static Polygon adjustPolygon(Polygon inputP){
		Polygon outputP = new Polygon();
		int [] dstXPoints = new int[inputP.xpoints.length];
		int [] dstYPoints = new int[inputP.ypoints.length];
		for (int i = 0; i < dstXPoints.length; i++)
			dstXPoints[i] = inputP.xpoints[i] - 1;
		for (int i = 0; i < dstYPoints.length; i++)
			dstYPoints[i] = inputP.ypoints[i] - 1;
		
		outputP.xpoints = dstXPoints;
		outputP.ypoints = dstYPoints;
		outputP.npoints = inputP.npoints;		
		return outputP;
	}
	
	public static ArrayList<Point> getBoundaryPoints(Polygon p){
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < p.npoints; i++)
		{
			points.add(new Point(p.xpoints[i], p.ypoints[i]));
		}
		return points;
	}
	
	/** Draw rectangles and polygons.
	 * @param path
	 * @param img
	 * @param rects
	 * @param polygons
	 * @param name
	 */
	public static void drawBoundaries(String path, BufferedImage img, ArrayList<Rectangle> rects, 
			ArrayList<Polygon> polygons, ArrayList<Polygon> GT, String name){
		BufferedImage imgShow = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgShow.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		if (rects != null) {
			g2d.setColor(Color.red);
			for (Rectangle rect : rects)
				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		if (polygons != null) {
			g2d.setColor(Color.blue);
			for (Polygon p : polygons)
				g2d.drawPolygon(p);
		}
		if (GT != null){
			g2d.setColor(Color.red);
			for (Polygon p : GT)
				g2d.drawPolygon(p);
		}
		g2d.dispose();
		try {
			File file = new File(path + name);
			if (file.exists()) {
				file.delete();
			}
			ImageIO.write(imgShow, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void drawRects(String path, BufferedImage img, ArrayList<Rectangle> rects, String name){
		BufferedImage imgShow = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgShow.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		if (rects != null) {
//			g2d.setColor(Color.red);
			g2d.setColor(Color.black);
			for (Rectangle rect : rects){
//				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
				g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
			}
		}
		try {
			File file = new File(path + name);
			if (file.exists()) {
				file.delete();
			}
			ImageIO.write(imgShow, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
