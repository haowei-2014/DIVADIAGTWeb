package ch.unifr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;

import com.google.gson.Gson;

public class Step1Projection {
	
	public HashMap<String, List<int[][]>> results;
	private BufferedImage image = null; 
	
	public Step1Projection(String imageURL) {
//		System.out.println("getImage");     
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
        
        int w = image.getWidth();
        int h = image.getHeight();
        System.out.println("Width is: " + w + ", Height is: " + h);
	}
	
	public HashMap<String, List<int[][]>> getResults(){
		results = new HashMap<String, List<int[][]>>();	
		List<int[][]> textBlocksList = new ArrayList<int[][]>();
		List<int[][]> pageList = new ArrayList<int[][]>();
/*		textBlocksList.add(new int[][] {{100, 200}, {300, 465}, {800, 900}});
		textBlocksList.add(new int[][] {{300, 300}, {400, 665}, {1000, 900}});
		pageList.add(new int[][] {{110, 280}, {800, 440}, {550, 900}});*/
		results.put("textBlocks", textBlocksList);	
		results.put("page", pageList);
		return results;
	}
	
	public static void main(String[] args){
		Step1Projection projectMethod = new Step1Projection("");
		HashMap<String, List<int[][]>> results = projectMethod.getResults();
		System.out.println(results);
	}

}
