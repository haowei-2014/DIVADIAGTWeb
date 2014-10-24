package ch.unifr.gabor;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class DrawGT {
	public String pathName;
	public String fileName;
	public String originalName = TextLineExtraction.originalName;

	public DrawGT(String pathName) {
		this.pathName = pathName;
	}

	/**
	 * This function is: (1) expand the polygon, lift the upper boundary and
	 * lower the bottom boundary. (2) pick up every a few points on the original
	 * polygon boundary and construct a new polygon
	 * 
	 * @param polygon
	 * @return
	 */
	public Polygon adjustPolygon(Polygon polygon) {
		ArrayList<Point> points = new ArrayList<Point>();
		int numberPoints = polygon.npoints;
		for (int i = 0; i < numberPoints; i++) {
			points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
		}

		// find the leftmost and rightmost x coordinates
		int minX = 1000000;
		int maxX = 0;
		for (int i = 0; i < numberPoints; i++) {
			if (points.get(i).x > maxX) {
				maxX = points.get(i).x;
			}
			if (points.get(i).x < minX) {
				minX = points.get(i).x;
			}
		}

		int expand = 3; // Within a line, lift the upper boundary and lower the bottom boundary
		for (int i = minX; i <= maxX; i++) {
			ArrayList<Point> commonXPoints = new ArrayList<Point>();
			for (int j = 0; j < points.size(); j++) {
				if (points.get(j).x == i) {
					commonXPoints.add(points.get(j));
				}
			}
			int mean = 0;
			for (int j = 0; j < commonXPoints.size(); j++) {
				mean += commonXPoints.get(j).y;
			}
			mean /= commonXPoints.size();
			for (int j = 0; j < commonXPoints.size(); j++) {
				if (commonXPoints.get(j).y < mean) {
					commonXPoints.get(j).y -= expand; // lift
				} else {
					commonXPoints.get(j).y += expand; // lower
				}
			}
		}
		int interval = 7; // pick up points every interval points
		int newNumberPoints = (numberPoints - 1) / interval + 1; // number of boundary points on the new polygon
		int[] xNewPoints = new int[(numberPoints - 1) / interval + 1];
		int[] yNewPoints = new int[(numberPoints - 1) / interval + 1];
		int newIndex = 0;
		for (int i = 0; i < numberPoints; i++) {
			if (i % interval == 0) {
				xNewPoints[newIndex] = points.get(i).x;
				yNewPoints[newIndex] = points.get(i).y;
				newIndex++;
			}
		}
		return new Polygon(xNewPoints, yNewPoints, newNumberPoints);
	}

	public ArrayList<Polygon> start() {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(this.pathName + "linking.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		img = CommonFunctions.convertImage(img);
		ImagePlus imp = new ImagePlus("test", img);
		ManyBlobs allBlobs = new ManyBlobs(imp); // Extended ArrayList
		allBlobs.findConnectedComponents(); // Start the Connected Component
		allBlobs = allBlobs.filterBlobs(100, 100000, Blob.GETENCLOSEDAREA);
		System.out.println("AllBlobs size: " + allBlobs.size());

		BufferedImage original = null;
		try {
			original = ImageIO.read(new File(pathName + originalName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int horizontalOffset = (original.getWidth() - img.getWidth()) / 2;
		int verticalOffset = (original.getHeight() - img.getHeight()) / 2;
		ArrayList<Polygon> polygonsGT = new ArrayList<Polygon>();

		for (int i = 0; i < allBlobs.size(); i++) {
			Polygon adjustedPolygon = CommonFunctions.adjustPolygon(allBlobs
					.get(i).getOuterContour());
			adjustedPolygon.translate(horizontalOffset + 1, verticalOffset + 1); // -1 is the error of ijblob.
			adjustedPolygon = adjustPolygon(adjustedPolygon);
			polygonsGT.add(adjustedPolygon);
		}
		CommonFunctions.drawBoundaries(pathName, original, null, polygonsGT, null, "segmentation1.png");
		return polygonsGT;
	}

	public static void main(String[] args) {

		DrawGT drawGT = new DrawGT("E:\\HisDoc project\\Gabor_filter\\projection\\case1\\");
		ArrayList<Polygon> polygonsGT = drawGT.start();
		System.out.println("Done!");
	}

}
