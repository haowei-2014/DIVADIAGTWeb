/**
 * 
 */
package ch.unifr.textblock;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class GrayScaler {

	public static double WEIGHT_R = 0.299;
	public static double WEIGHT_G = 0.587;
	public static double WEIGHT_B = 0.114;

	public void convert(String imageOriginalPath, String imageDestPath) {
		ImagePlus imageNew = this.convert(imageOriginalPath, WEIGHT_R,
				WEIGHT_G, WEIGHT_B);
		FileSaver fs = new FileSaver(imageNew);
		fs.saveAsJpeg(imageDestPath);
	}

	public void convert(String imageOriginalPath, String imageDestPath,
			double wR, double wG, double wB) {
		ImagePlus imageNew = this.convert(imageOriginalPath, wR, wG, wB);
		FileSaver fs = new FileSaver(imageNew);
		fs.saveAsJpeg(imageDestPath);
	}

	public ImagePlus convert(String imagePath) {
		return this.convert(imagePath, WEIGHT_R, WEIGHT_G, WEIGHT_B);
	}

	public ImagePlus convert(Image originalImage) {
		return this.convert(originalImage, WEIGHT_R, WEIGHT_G, WEIGHT_B);
	}

	public ImagePlus convert(String imagePath, double wR, double wG, double wB) {
		try {
			BufferedImage image = ImageIO.read(new File(imagePath));
			return convert(image, wR, wG, wB);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ImagePlus convert(Image originalImage, double wR, double wG,
			double wB) {
		ImagePlus ipOriginal = new ImagePlus("", originalImage);
		ColorProcessor cp = (ColorProcessor) ipOriginal.getProcessor();
		cp.setWeightingFactors(wR, wG, wB);
		ImageProcessor ipGray = cp.convertToByte(true);

		// create new grayscale image and copy the original
		// grayscale image to it
		ImagePlus imgNew = NewImage.createByteImage("new grayscale image",
				ipOriginal.getWidth(), ipOriginal.getHeight(), 1,
				NewImage.FILL_BLACK);
		ImageProcessor newIp = imgNew.getProcessor();
		newIp.copyBits(ipGray, 0, 0, Blitter.COPY);

		return imgNew;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GrayScaler gs = new GrayScaler();
		gs.convert("/home/ck/Desktop/3.jpg", "/home/ck/Desktop/3-gray.jpg");
	}

}