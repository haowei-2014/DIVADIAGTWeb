package ch.unifr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import ch.unifr.textblock.Block;
import ch.unifr.textblock.BlockRetriever;

import com.google.gson.Gson;

public class Step1Projection {
	
	public HashMap<String, List<int[][]>> results;
	private BufferedImage image = null; 
	
	public Step1Projection(String imageURL) {	
		if (imageURL.startsWith("data:image")){
			imageURL = imageURL.replace("data:image/png;base64,", "");
			
	        byte[] imageByte;
	        try {
	            BASE64Decoder decoder = new BASE64Decoder();
	            imageByte = decoder.decodeBuffer(imageURL);
	            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
	            image = ImageIO.read(bis);
	            bis.close();          
	            File outputfile = new File("saved.png");
	            ImageIO.write(image, "png", outputfile);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		} else {
			URL url = null;
			try {
				url = new URL(imageURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
			try {
				image = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
        
        int w = image.getWidth();
        int h = image.getHeight();
        System.out.println("Width is: " + w + ", Height is: " + h);
	}
	
	public HashMap<String, List<int[][]>> getResults(){
		ArrayList<Block> blockList = BlockRetriever.start(image);
		results = new HashMap<String, List<int[][]>>();	
		List<int[][]> textBlocksList = new ArrayList<int[][]>();
		
		for (int i = 0; i < blockList.size(); i++){
			textBlocksList.add(new int[][] {
				{blockList.get(i).x, blockList.get(i).y}, 
				{blockList.get(i).x + blockList.get(i).width, blockList.get(i).y}, 
				{blockList.get(i).x + blockList.get(i).width, blockList.get(i).y + blockList.get(i).height},
				{blockList.get(i).x, blockList.get(i).y + blockList.get(i).height}});
		}

	//	textBlocksList.add(new int[][] {{100, 300}, {400, 665}, {1000, 900}});
		results.put("textBlocks", textBlocksList);	
		return results;
	}
	
	public void cropTextBlock(int top, int bottom, int left, int right){	
		BufferedImage textBlock = image.getSubimage(left, top, right-left, bottom-top);
		try {
		    File outputfile = new File("/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/manualTextBlockInput.png");
		    ImageIO.write(textBlock, "png", outputfile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Step1Projection projectMethod = new Step1Projection("");
		HashMap<String, List<int[][]>> results = projectMethod.getResults();
		System.out.println(results);
	}

}
