package ch.unifr.gabor;

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

import javax.imageio.ImageIO;

/**
 *  Link CCs within the same line together by small rectangles. Draw contour obtained by our algorithm 
 *  and ground truth. 
 * @author WeiH
 *
 */
public class LinkCCs {
	public String pathName = null;
	public String originalName = null;
	public int width = 0;
	public enum Orientation {LEFT, RIGHT}
	// inner class including blobs within the same line
    public class line {
    	public ArrayList<Blob> lineBlobs = new ArrayList<Blob>();
    	ArrayList<Rectangle> linkingRects = new ArrayList<Rectangle>(); 
    	public Blob leftmostBlob;
    	public Blob rightmostBlob;
    	public Rectangle leftmostRect;
    	public Rectangle rightmostRect;
    	public int length;
    	public boolean regular;
    	
    	line(Blob b){
    		lineBlobs.add(b);
    		leftmostBlob = b;
    		rightmostBlob = b;
    		length = 0;
    		regular = false;
    	}
    	
    	public ArrayList<Polygon> getPolygons(){
    		ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    		for (Blob b : lineBlobs){
    			polygons.add(b.getOuterContour());
    			}    		
    		return polygons;
    	}
    }
    
    public LinkCCs(String pathName, String originalName) {
    	this.pathName = pathName;
    	this.originalName = originalName;
    }
	
	/** compute the left or right linking rectangle of the CC
	 * @param blob
	 * @param orientation
	 * @return
	 */
	public Rectangle computeRect(Blob blob, Orientation orientation) {
		Rectangle rect = new Rectangle();
		Rectangle boundingRect = CommonFunctions.adjustPolygon(
				blob.getOuterContour()).getBounds();
		rect.width = 40;
		rect.height = 10;
		rect.y = boundingRect.y + boundingRect.height/2 - rect.height/2;
		if (orientation == Orientation.LEFT){
			rect.x = boundingRect.x + 10 - rect.width;
			if (rect.x < 0)    // out of the left boundary
				return null;
		}
		else{
			rect.x = boundingRect.x + boundingRect.width - 10;
			if (rect.x + rect.width >= width)     // out of the right boundary
				return null;
		}
		return rect;
	}
	 
	/** This function is to search the blobs leftwards and rightwards within the same line
	 * @param img
	 */
	public void start(BufferedImage img){	
		width = img.getWidth();
		img = CommonFunctions.convertImage(img);	
		ImagePlus imp = new ImagePlus("test", img);
		ManyBlobs allBlobs = new ManyBlobs(imp); // Extended ArrayList				
		allBlobs.findConnectedComponents(); // Start the Connected Component
		allBlobs = allBlobs.filterBlobs(100,100000, Blob.GETENCLOSEDAREA); 
		System.out.println("AllBlobs size: " + allBlobs.size());		
		ArrayList<line> lines = new ArrayList<line>();
		ArrayList<Blob> remainingBlobs = new ArrayList<Blob>();  // Blobs to be processed.
		remainingBlobs.addAll(allBlobs);
		ArrayList<Rectangle> linkingRects = new ArrayList<Rectangle>(); // small rectangles linking the neighboring CCs
		Rectangle leftRect = new Rectangle();   // left linking rectangle of the CC
		Rectangle rightRect = new Rectangle();  // right linking rectangle of the CC
				
		while (remainingBlobs.size() > 0) {
			System.out.println("RemainingBlobs size: " + remainingBlobs.size());			
			Boolean continueLeft = true;
			Boolean continueRight = true;
			Blob initialBlob = remainingBlobs.get(0);
			Blob rightCC = initialBlob;
			Blob leftCC = initialBlob;
			line oneLine = this.new line(initialBlob);		
			leftRect = computeRect(initialBlob, Orientation.LEFT);
			rightRect = computeRect(initialBlob, Orientation.RIGHT);
			if (leftRect == null)
				continueLeft = false;
			if (rightRect == null)
				continueRight = false;
			if (leftRect != null) {
				linkingRects.add(leftRect);
				oneLine.linkingRects.add(leftRect);
				oneLine.leftmostRect = leftRect;
			}
			if (rightRect != null) {
				linkingRects.add(rightRect);
				oneLine.linkingRects.add(rightRect);
				oneLine.rightmostRect = rightRect;
			}
			
			remainingBlobs.remove(initialBlob);	  // the initialBlob will no longer be processed		
			Blob previousLeftCC = initialBlob;
			Blob previousRightCC = initialBlob;
			
			// detect neighboring CC leftwards
			while (continueLeft && remainingBlobs.size() > 0) {
				for (Blob blob : remainingBlobs) {
					Polygon p = CommonFunctions.adjustPolygon(blob
							.getOuterContour());
					previousLeftCC = leftCC;
					if (p.intersects(leftRect)) {
						leftCC = blob;
						leftRect = computeRect(leftCC, Orientation.LEFT);
						if (leftRect == null) {
							continueLeft = false;
							break;
						}
						linkingRects.add(leftRect);
						oneLine.linkingRects.add(leftRect);
						remainingBlobs.remove(leftCC);
						oneLine.lineBlobs.add(leftCC);
						oneLine.leftmostBlob = leftCC;
						oneLine.leftmostRect = leftRect;
						break;
					}
				}
				// if the previous LeftCC is the same with the current LeftCC, it means that the detection didn't
				// move leftwards. Thus stop.
				if (previousLeftCC == leftCC) { 
					continueLeft = false;
					oneLine.linkingRects.remove(oneLine.leftmostRect);
					}
			}

			// detect neighboring CC rightwards
			while (continueRight && remainingBlobs.size() > 0) {
				for (Blob blob : remainingBlobs) {
					Polygon p = CommonFunctions.adjustPolygon(blob
							.getOuterContour());
					previousRightCC = rightCC;
					if (p.intersects(rightRect)) {
						rightCC = blob;
						rightRect = computeRect(rightCC, Orientation.RIGHT);
						if (rightRect == null) {
							continueRight = false;
							break;
						}
						linkingRects.add(rightRect);
						oneLine.linkingRects.add(rightRect);
						remainingBlobs.remove(rightCC);
						oneLine.lineBlobs.add(rightCC);
						oneLine.rightmostBlob = rightCC;
						oneLine.rightmostRect = rightRect;
						break;
					}
				}
				if (previousRightCC == rightCC){
					continueRight = false;
					oneLine.linkingRects.remove(oneLine.rightmostRect);
					}
			}
			oneLine.length = oneLine.rightmostBlob.getOuterContour().getBounds().x + 
					oneLine.rightmostBlob.getOuterContour().getBounds().width -
					oneLine.leftmostBlob.getOuterContour().getBounds().x;
			if (oneLine.length > 150)
				oneLine.regular = true;
			lines.add(oneLine);
		}
		System.out.println("Loop is finished!");
		System.out.println("Lines number: " + lines.size());
		
		for (int i = 0; i < lines.size(); i++)
			System.out.println("Line " + i + " has " + lines.get(i).lineBlobs.size() + " blobs.");
		
		drawRects(img, linkingRects, lines);
		System.out.println("Done!");
	}	
		
	/** draw the linking rectangles
	 * @param img
	 * @param linkingRects
	 * @param lines
	 */
	public void drawRects(BufferedImage img, ArrayList<Rectangle> linkingRects, ArrayList<line> lines){	
		ArrayList<Rectangle> regularLineRects = new ArrayList<Rectangle>();
		for (line l : lines){
			if (l.regular)
				regularLineRects.addAll(l.linkingRects);		
		}			
		CommonFunctions.drawRects(pathName, img, regularLineRects, "linking.png");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkCCs linkCCs = new LinkCCs("E:\\HisDoc project\\Gabor_filter\\projection\\case1\\", 
				"d-007.1.910.133.506.2358.png");
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("E:\\HisDoc project\\Gabor_filter\\projection\\case1\\" 
		+ "segmentationProjectionNext.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		linkCCs.start(img);
	}
}
