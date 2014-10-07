package ch.unifr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TextNonTextClustering {
	
	public static BufferedImage img = null;
	public static BufferedImage imgOutput = null;
	public static int width, height;
	public static float[][] features = null;
	public static int[][] labels = null;
	public static int[][] imageValues = null;
	public static float meanFeatures = 0;
	public static int slidingWindowHeight = 25;
	public static int slidingWindowWidth = 25;
	public static int[][] slidingWindow = new int[slidingWindowHeight][slidingWindowWidth];
		
	public void init(String file){
		try {
		    img = ImageIO.read(new File(file));
		    imgOutput = ImageIO.read(new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		width = img.getWidth();
		height = img.getHeight();
		features = new float[height][width];
		labels = new int[height][width];
		imageValues = new int[height][width];
		
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				int rgb = img.getRGB(j, i);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);
				int gray = (r + g + b) / 3;
				imageValues[i][j] = gray;
			}
		} 
	}
	
	public void featureExtraction (BufferedImage img) {
		int slidingWindowHeightRadius = (slidingWindowHeight-1)/2;
		int slidingWindowWidthRadius = (slidingWindowWidth-1)/2;
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				
				for (int iWindow = i-slidingWindowHeightRadius; iWindow <= i+slidingWindowHeightRadius ;iWindow++){
					for (int jWindow = j-slidingWindowWidthRadius; jWindow <= j+slidingWindowWidthRadius ;jWindow++){
						if (iWindow >= 0 && jWindow >= 0 && iWindow < height && jWindow < width){
							features[i][j] += imageValues[i][j];
						}
					}
				}
			}
		}
		
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				features[i][j] /= slidingWindowHeight*slidingWindowWidth;
				meanFeatures += features[i][j];
			}
		} 
		meanFeatures /= height*width;
		System.out.println(meanFeatures);
	}
	
	public void computeLabels(){
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				if(features[i][j] > meanFeatures*1.2)
					labels[i][j] = 1;
				else
					labels[i][j] = 0;
			}
		} 
	}
	
	public void createOutput(){
		
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++){
				if (labels[i][j] == 1){
					Color myColor = new Color(255, 255, 255); 
					int rgb=myColor.getRGB();
					imgOutput.setRGB(j, i, rgb);
				} else {
					Color myColor = new Color(0, 0, 0); 
					int rgb=myColor.getRGB();
					imgOutput.setRGB(j, i, rgb);
				}
			}
		} 
		
		File outputfile = new File("imgOutput.png");
        try {
			ImageIO.write(imgOutput, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TextNonTextClustering tnt = new TextNonTextClustering();
		tnt.init("/home/weih/Work/divadiaweb/GaborFilters_web/GaborInput.jpg");
		tnt.featureExtraction(img);
		tnt.computeLabels();
		tnt.createOutput();
		System.out.println("Done");
	}

}
