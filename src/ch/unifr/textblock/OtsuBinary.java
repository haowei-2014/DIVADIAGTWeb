/**
 * 
 */
package ch.unifr.textblock;

import ij.ImagePlus;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class OtsuBinary {

	// public static int FORE_PIXEL = 255;
	// public static int BACK_PIXEL = 0;

	public static int FORE_PIXEL = 0;
	public static int BACK_PIXEL = 255;

	public int threshold;

	public ImagePlus binarization(String originalImagePath) {
		ImagePlus originalImage = new ImagePlus(originalImagePath);
		ImagePlus binImage = this.binarization(originalImage);
		return binImage;
	}

	public ImagePlus binarization(ImagePlus ipOriginal) {
		BufferedImage srcImage = ipOriginal.getBufferedImage();

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		// Gray scaling
		GrayScaler gs = new GrayScaler();
		ImagePlus grayImage = gs.convert(ipOriginal.getImage());

		// Get raw image data
		Raster raster = ((BufferedImage) grayImage.getImage()).getData();
		DataBuffer buffer = raster.getDataBuffer();

		int type = buffer.getDataType();
		if (type != DataBuffer.TYPE_BYTE) {
			System.err.println("Wrong image data type");
			System.exit(1);
		}
		if (buffer.getNumBanks() != 1) {
			System.err.println("Wrong image data format");
			System.exit(1);
		}

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);

		// Sanity check image
		if (width * height != srcData.length) {
			System.err
					.println("Unexpected image data size. Should be greyscale image");
			System.exit(1);
		}

		byte[] dstData = new byte[srcData.length];

		// Create Otsu Thresholder
		OtsuThresholder thresholder = new OtsuThresholder();
		threshold = thresholder.doThreshold(srcData, dstData);

		// System.out.println("otsu threshold: " + threshold);

		for (int i = 0; i < srcData.length; i++) {
			dstData[i] = (byte) (((0xFF & srcData[i]) >= threshold) ? (byte) BACK_PIXEL
					: FORE_PIXEL);
		}

		ImagePlus newImg = new ImagePlus("new image", this.getImage(width,
				height, dstData, "new image"));

		return newImg;
	}

	private BufferedImage getImage(int width, int height, byte[] data,
			String title) {
		DataBufferByte dataBuffer = new DataBufferByte(data, data.length, 0);

		PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(
				DataBuffer.TYPE_BYTE, width, height, 1, width, new int[] { 0 });
		ColorSpace colourSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ComponentColorModel colourModel = new ComponentColorModel(colourSpace,
				new int[] { 8 }, false, false, Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE);

		WritableRaster raster = Raster.createWritableRaster(sampleModel,
				dataBuffer, null);

		return new BufferedImage(colourModel, raster, false, null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String originalImagePath = "imgs/csg562-023.png";
		ImagePlus ipOriginal = new ImagePlus(originalImagePath);
		ipOriginal.show();

		GrayScaler gs = new GrayScaler();
		ImagePlus ipGray = gs.convert(ipOriginal.getImage());
		ipGray.show();

		OtsuBinary otsuBin = new OtsuBinary();
		ImagePlus ipBin = otsuBin.binarization(originalImagePath);
		ipBin.show();

	}

}
