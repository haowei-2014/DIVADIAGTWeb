package ch.unifr;

import java.io.File;

/**
 * @author hao
 * Information about the processed image
 */
public class Info {
	
	public String imageURL;
	public String imageName;
	public int top;
	public int bottom;
	public int left;
	public int right;
	public int linkingRectWidth;
	public int linkingRectHeight;		
	public String filePath = null;
	public String prefix = null;
	public String gaborInput = null;
	public String gaborOutput = null;
	
	Info(String imageURL, String imageName, int top, int bottom, int left, int right, int linkingRectWidth, int linkingRectHeight){
		this.imageURL = imageURL;
		this.imageName = imageName;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.linkingRectWidth = linkingRectWidth;
		this.linkingRectHeight = linkingRectHeight;
		
		filePath = System.getProperty("user.dir") + File.separator + "tmpData" + File.separator;
		prefix = "_" + left + "_" + top +"_";
		gaborInput = prefix +"GaborInput.png";	
		gaborOutput = gaborInput.replace("GaborInput", "GaborOutput");
	}
	
	

}
