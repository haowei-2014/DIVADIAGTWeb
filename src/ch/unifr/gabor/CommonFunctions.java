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
	
	public static void drawRects(String path, BufferedImage img, ArrayList<LinkCCs.RectangleClass> rects, String name){
		BufferedImage imgShow = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgShow.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.setColor(Color.black);
		
		for (LinkCCs.RectangleClass rect : rects) {
			if (rect.leftPolygon == null || rect.rightPolygon == null)
				continue;

			// get the left top and left bottom points where the rectangle intersects its left polygon
			ArrayList<Point> leftPoints = new ArrayList<Point>();
			int numberPoints = rect.leftPolygon.npoints;
			for (int i = 0; i < numberPoints; i++) {
				leftPoints.add(new Point(rect.leftPolygon.xpoints[i],
						rect.leftPolygon.ypoints[i]));
			}
			int leftTop = 10000;
			int leftBottom = 0;
			for (Point point : leftPoints) {
				if (point.x == rect.rectangle.x && leftTop > point.y) {
					leftTop = point.y;
				}
				if (point.x == rect.rectangle.x && leftBottom < point.y) {
					leftBottom = point.y;
				}
			}
			// in case that the linking rectangle completely contains its left polygon.
			if (leftTop == 10000){
				for (Point point : leftPoints) {
					if (point.x == rect.leftPolygon.getBounds().x + rect.leftPolygon.getBounds().width/2 && leftTop > point.y) {
						leftTop = point.y;
					}
					if (point.x == rect.leftPolygon.getBounds().x + rect.leftPolygon.getBounds().width/2 && leftBottom < point.y) {
						leftBottom = point.y;
					}
				}
			}

			// get the right top and right bottom points where the rectangle intersects its right polygon
			ArrayList<Point> rightPoints = new ArrayList<Point>();
			numberPoints = rect.rightPolygon.npoints;
			for (int i = 0; i < numberPoints; i++) {
				rightPoints.add(new Point(rect.rightPolygon.xpoints[i],
						rect.rightPolygon.ypoints[i]));
			}
			int rightTop = 10000;
			int rightBottom = 0;
			for (Point point : rightPoints) {
				if (point.x == (rect.rectangle.x + rect.rectangle.width)
						&& rightTop > point.y) {
					rightTop = point.y;
				}
				if (point.x == (rect.rectangle.x + rect.rectangle.width)
						&& rightBottom < point.y) {
					rightBottom = point.y;
				}
			}
			// in case that the linking rectangle completely contains its right polygon.
			if (rightTop == 10000){
				for (Point point : rightPoints) {
					if (point.x == rect.rightPolygon.getBounds().x + rect.rightPolygon.getBounds().width/2 && leftTop > point.y) {
						leftTop = point.y;
					}
					if (point.x == rect.rightPolygon.getBounds().x + rect.rightPolygon.getBounds().width/2 && leftBottom < point.y) {
						leftBottom = point.y;
					}
				}
			}

			Polygon linkingPolygon = new Polygon();
			linkingPolygon.addPoint(rect.rectangle.x, leftTop);
			linkingPolygon.addPoint(rect.rectangle.x + rect.rectangle.width,
					rightTop);
			linkingPolygon.addPoint(rect.rectangle.x + rect.rectangle.width,
					rightBottom);
			linkingPolygon.addPoint(rect.rectangle.x, leftBottom);
			g2d.fillPolygon(linkingPolygon);
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
	
	// link the polygon pair (left and right one) with a polygon.
	public static BufferedImage drawRects(BufferedImage img, Polygon polygonLeft, Polygon polygonRight, Rectangle rect) {
		BufferedImage imgShow = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgShow.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.setColor(Color.black);

		// get the left top and left bottom points where the rectangle intersects its left polygon
		ArrayList<Point> leftPoints = new ArrayList<Point>();
		int numberPoints = polygonLeft.npoints;
		for (int i = 0; i < numberPoints; i++) {
			leftPoints.add(new Point(polygonLeft.xpoints[i],
					polygonLeft.ypoints[i]));
		}
		int leftTop = 10000;
		int leftBottom = 0;
		for (Point point : leftPoints) {
			if (point.x == rect.x && leftTop > point.y) {
				leftTop = point.y;
			}
			if (point.x == rect.x && leftBottom < point.y) {
				leftBottom = point.y;
			}
		}
		// in case that the linking rectangle completely contains its left polygon.
		if (leftTop == 10000) {
			for (Point point : leftPoints) {
				if (point.x == polygonLeft.getBounds().x
						+ polygonLeft.getBounds().width / 2
						&& leftTop > point.y) {
					leftTop = point.y;
				}
				if (point.x == polygonLeft.getBounds().x
						+ polygonLeft.getBounds().width / 2
						&& leftBottom < point.y) {
					leftBottom = point.y;
				}
			}
		}
		if (leftTop < 0)
			leftTop = 0;

		// get the right top and right bottom points where the rectangle intersects its right polygon
		ArrayList<Point> rightPoints = new ArrayList<Point>();
		numberPoints = polygonRight.npoints;
		for (int i = 0; i < numberPoints; i++) {
			rightPoints.add(new Point(polygonRight.xpoints[i],
					polygonRight.ypoints[i]));
		}
		int rightTop = 10000;
		int rightBottom = 0;
		for (Point point : rightPoints) {
			if (point.x == (rect.x + rect.width) && rightTop > point.y) {
				rightTop = point.y;
			}
			if (point.x == (rect.x + rect.width) && rightBottom < point.y) {
				rightBottom = point.y;
			}
		}
		// in case that the linking rectangle completely contains its right polygon.
		if (rightTop == 10000) {
			for (Point point : rightPoints) {
				int tmpX = polygonRight.getBounds().x
						+ polygonRight.getBounds().width / 2;
				if (point.x == tmpX
						&& rightTop > point.y) {
					rightTop = point.y;
				}
				if (point.x == tmpX
						&& rightBottom < point.y) {
					rightBottom = point.y;
				}
			}
		}
		if (rightTop < 0)
			rightTop = 0;
		
		// shrink the linking polygon by lowering its top vertexes and lifting its bottom vertexes. 
		if (leftBottom > leftTop + 6)
		{
			leftBottom -= 3;
			leftTop += 3;
		}
		if (rightBottom > rightTop + 6)
		{
			rightBottom -= 3;
			rightTop += 3;
		}

		// fill the polygon with the black color
		Polygon linkingPolygon = new Polygon();
		linkingPolygon.addPoint(rect.x, leftTop);
		linkingPolygon.addPoint(rect.x + rect.width, rightTop);
		linkingPolygon.addPoint(rect.x + rect.width, rightBottom);
		linkingPolygon.addPoint(rect.x, leftBottom);
		g2d.fillPolygon(linkingPolygon);
		
//		g2d.setColor(Color.green);
//		g2d.drawPolygon(linkingPolygon);
//		g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
/*		
		if (rightTop == 10000){
			g2d.setColor(Color.red);
			g2d.fillPolygon(polygonLeft);
			g2d.fillPolygon(polygonRight);
			g2d.setColor(Color.green);
			g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
		}*/
		return imgShow;
	}
	
	public static void drawRects(BufferedImage img, ArrayList<Rectangle> rects, String name){
		BufferedImage imgShow = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgShow.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		if (rects != null) {
			g2d.setColor(Color.red);
//			g2d.setColor(Color.black);
			for (Rectangle rect : rects){
				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
//				g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
			}
		}
		try {
			File file = new File("/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/" + name);
			if (file.exists()) {
				file.delete();
			}
			ImageIO.write(imgShow, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
