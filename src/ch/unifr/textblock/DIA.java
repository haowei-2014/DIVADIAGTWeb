/**
 * 
 */
package ch.unifr.textblock;


import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;



public class DIA {

	public static Object RENDER_QUALITY = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
	public static boolean HIGH_QUALITY = true;

	public static int GB2312 = 0;
	public static int UTF8 = 1;
	public static int GB18030 = 2;
	public static String[] CODE = { "UTF-8", "GB18030", "GB2312" };

	public static final String[] IMAGE_TYPE = { ".png", ".jpg" };

	public static ImagePlus getImagePatch(ImagePlus img, Point center, int w,
			int h) {
		int width = img.getWidth();
		int height = img.getHeight();

		// get left and right x coordinates
		int leftX = center.x;
		if (leftX - w / 2 < 0) {
			leftX = 0;
		} else {
			leftX = leftX - w / 2;
		}
		int rightX = leftX + w;
		if (rightX > width) {
			rightX = width - 1;
		}

		// get up and down y coordinates
		int upY = center.y;
		if (upY - h / 2 < 0) {
			upY = 0;
		} else {
			upY = upY - h / 2;
		}
		int downY = upY + h;
		if (downY > height) {
			downY = height - 1;
		}

		ImagePlus cropImg = cropImage(img, leftX, upY, (rightX - leftX),
				(downY - upY));

		return cropImg;
	}

	public static File getOriginalImgFile(String resultImgPath,
			String originalImgDirPath) {
		File originalImgDir = new File(originalImgDirPath);
		if (originalImgDir.exists()) {
			File[] originalImgList = originalImgDir.listFiles();
			for (File originalImg : originalImgList) {
				String originalImgFileName = originalImg.getName();
				if (originalImgFileName.toLowerCase().endsWith(".png")
						|| originalImgFileName.toLowerCase().endsWith(".jpg")) {
					originalImgFileName = originalImgFileName.substring(0,
							originalImgFileName.length() - 4);
				}
				if (resultImgPath.contains(originalImgFileName)) {
					return originalImg;
				}
			}
		}
		return null;
	}

	public static int[][] getSegClassMatrix(String textLineSegResultFile,
			int width, int height) {

		int[][] classMatrix = new int[width][height];

		if (!(new File(textLineSegResultFile)).exists()) {
			System.out.println(textLineSegResultFile + " does not exist.");
			return classMatrix;
		}

		BufferedReader br = null;
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(textLineSegResultFile));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] strs = sCurrentLine.split(",");
				if (strs.length >= 4) {
					int predictClassIndex = Integer.parseInt(strs[3]);
					classMatrix[Integer.parseInt(strs[0])][Integer
							.parseInt(strs[1])] = predictClassIndex;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return classMatrix;
	}

	public static int[][] getGroundTruthMatrix(String textLineSegResultFile,
			int width, int height) {

		int[][] classMatrix = new int[width][height];

		if (!(new File(textLineSegResultFile)).exists()) {
			System.out.println(textLineSegResultFile + " does not exist.");
			return classMatrix;
		}

		BufferedReader br = null;
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(textLineSegResultFile));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] strs = sCurrentLine.split(",");
				if (strs.length >= 4) {
					int classIndex = Integer.parseInt(strs[2]);
					classMatrix[Integer.parseInt(strs[0])][Integer
							.parseInt(strs[1])] = classIndex;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return classMatrix;
	}

	public static ArrayList<Point> getClassifiedTextblockPixels(
			String binImagePath, double scaleX, double scaleY) {

		ArrayList<Point> points = new ArrayList<Point>();

		ImagePlus imgBin = new ImagePlus(binImagePath);

		BufferedImage originalImage = (BufferedImage) imgBin.getImage();
		int targetWidth = (int) Math.ceil(originalImage.getWidth() * scaleX);
		int targetHeight = (int) Math.ceil(originalImage.getHeight() * scaleY);
		BufferedImage scaledImage = DIA.getScaledInstance(originalImage,
				targetWidth, targetHeight,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC, DIA.HIGH_QUALITY);
		ImagePlus scaledImp = new ImagePlus("scaled image", scaledImage);

		ImageProcessor ipS = scaledImp.getProcessor();

		int width = ipS.getWidth();
		int height = ipS.getHeight();

		// TODO: pay attention of the order of each pixel
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int[] rgb = DIA.getRGB(ipS.getPixel(i, j));
				if (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0) {
					points.add(new Point(i, j));
				}
			}
		}

		return points;
	}

	public static int getGreenOrRedPixelsNeighbor(ImagePlus image, int x,
			int y, int[] windowSize) {
		ImageProcessor ip = image.getProcessor();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int nb = 0;
		for (int i = -windowSize[0]; i <= windowSize[0]; i++) {
			for (int j = -windowSize[1]; j <= windowSize[1]; j++) {
				int posX = x + i;
				int posY = y + j;
				if (i != 0 && j != 0 && posX >= 0 && posX < width && posY >= 0
						&& posY < height) {
					int[] rgb = DIA.getRGB(ip.get(posX, posY));
					if (DIA.isGreen(rgb) || DIA.isRed(rgb)) {
						nb += 1;
					}
				}
			}
		}
		return nb;
	}


	public static ImagePlus scaleImage(ImagePlus image, double scaleX,
			double scaleY) {
		if (image == null) {
			throw new IllegalArgumentException("image is null");
		}
		BufferedImage originalImage = image.getBufferedImage();

		int targetWidth = (int) Math.ceil((new Double(originalImage.getWidth()
				* scaleX)));
		int targetHeight = (int) Math.ceil((new Double(originalImage
				.getHeight()
				* scaleY)));

		// BufferedImage scaledImage = DIA.getScaledInstance(originalImage,
		// targetWidth, targetHeight,
		// RenderingHints.VALUE_INTERPOLATION_BICUBIC, DIA.HIGH_QUALITY);

		BufferedImage scaledImage = DIA.getScaledInstance(originalImage,
				targetWidth, targetHeight,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
				DIA.HIGH_QUALITY);

		ImagePlus scaledImp = new ImagePlus("scaled image", scaledImage);

		return scaledImp;
	}

	public static boolean isYellow(int[] rgb) {
		if (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 255)
			return true;
		return false;
	}

	public static boolean isRed(int[] rgb) {
		if (rgb[0] == 255 && rgb[1] == 0 && rgb[2] == 0)
			return true;
		return false;
	}

	public static boolean isGreen(int[] rgb) {
		if (rgb[0] == 0 && rgb[1] == 255 && rgb[2] == 0)
			return true;
		return false;
	}

	public static ImagePlus compareTextLineSegResults(ImagePlus originalImage,
			ImagePlus img1, ImagePlus img2) {
		ImageProcessor ip1 = img1.getProcessor();
		ImageProcessor ip2 = img2.getProcessor();

		ImageProcessor ipCompare = originalImage.getProcessor().duplicate();
		int correctedModifiedPixels = 0;
		int missModifiedPixels = 0;

		// get number of error points
		int nbErrorPoints = 0;
		for (int i = 0; i < ip1.getWidth(); i++) {
			for (int j = 0; j < ip1.getHeight(); j++) {
				int[] rgb1 = DIA.getRGB(ip1.getPixel(i, j));
				if (DIA.isRed(rgb1)) {
					nbErrorPoints++;
				}
			}
		}

		for (int i = 0; i < ip1.getWidth() && i < ip2.getWidth(); i++) {
			for (int j = 0; j < ip1.getHeight() && j < ip2.getHeight(); j++) {
				int[] rgb1 = DIA.getRGB(ip1.getPixel(i, j));
				int[] rgb2 = DIA.getRGB(ip2.getPixel(i, j));

				if (DIA.isRed(rgb1) && DIA.isGreen(rgb2)) {
					correctedModifiedPixels++;
					ipCompare.setColor(Color.green);
					ipCompare.drawDot(i, j);
				} else if (DIA.isGreen(rgb1) && DIA.isRed(rgb2)) {
					missModifiedPixels++;
					ipCompare.setColor(Color.red);
					ipCompare.drawDot(i, j);
				}
				// else if(DIA.isRed(rgb1) && DIA.isRed(rgb2)) {
				// ipCompare.setColor(Color.red);
				// ipCompare.drawDot(i, j);
				// }
			}
		}

		double rate = 0.0;
		if (nbErrorPoints != 0) {
			rate = correctedModifiedPixels * 1.0 / nbErrorPoints;
		}

		System.out.println("correct classified pixels: "
				+ correctedModifiedPixels);
		System.out.println("miss classified pixels: " + missModifiedPixels);
		System.out.println("correction rate: " + rate);

		return new ImagePlus("compare image", ipCompare);
	}


	public static Point[] getPoints(Blob blob, int width, int height) {
		ArrayList<Point> pointArray = new ArrayList<Point>();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Point p = new Point(i, j);
				if (blob.getOuterContour().contains(p)) {
					pointArray.add(p);
				}
			}
		}

		Point[] points = new Point[pointArray.size()];
		int i = 0;
		for (Point p : pointArray) {
			points[i] = p;
			i++;
		}

		return points;
	}

	private static BufferedImage getImage(int width, int height, byte[] data,
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

	public static ImagePlus getBinaryImage(float[][] data, String imageFilePath) {
		// create binary image
		ImagePlus imagePlus = new ImagePlus(imageFilePath);
		ImageProcessor ip = imagePlus.getProcessor().duplicate();
		int width = ip.getWidth();
		int height = ip.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (data[i][j] == 0f) {
					ip.setColor(Color.black);
					ip.drawDot(i, j);
				} else {
					ip.setColor(Color.white);
					ip.drawDot(i, j);
				}
			}
		}

		return getBinaryImage(new ImagePlus("new image", ip));
	}

	public static ImagePlus getBinaryImage(ImagePlus ipOriginal) {
		BufferedImage srcImage = ipOriginal.getBufferedImage();

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		// Gray scaling
		GrayScaler gs = new GrayScaler();
		ImagePlus grayImage = gs.convert(ipOriginal.getImage());

		// Get raw image data
		Raster raster = ((BufferedImage) grayImage.getImage()).getData();
		DataBuffer buffer = raster.getDataBuffer();

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);

		ImagePlus newImg = new ImagePlus("new image", getImage(width, height,
				srcData, "new image"));

		return newImg;
	}

	public static boolean isNeighbor(Point pCenter, Point pNeighbor,
			int horizontalSize, int verticalSize) {

		// same point
		if (pCenter.x == pNeighbor.x && pCenter.y == pNeighbor.y) {
			return false;
		}

		if (Math.abs(pCenter.x - pNeighbor.x) <= horizontalSize
				&& Math.abs(pCenter.y - pNeighbor.y) <= verticalSize) {
			return true;
		}

		return false;
	}

	public static void saveLibsvmSamples2File(String libSvmFilePath,
			String destFilePath, ArrayList<Integer> featureIndexArray) {
		if (!(new File(libSvmFilePath)).exists()) {
			System.err.println(libSvmFilePath + " does not exist. ");
			return;
		}
		if (featureIndexArray == null || featureIndexArray.isEmpty()) {
			System.err.println("feature index array is null or empty.");
			return;
		}

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			// create new destination file
			File destFile = new File(destFilePath);
			DIA.deleteFile(destFilePath);
			destFile.createNewFile();
			FileWriter fw = new FileWriter(destFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			// read feature file
			br = new BufferedReader(new FileReader(libSvmFilePath));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				HashMap<Integer, ArrayList<Double>> sample = DIA
						.getFeatureVectorFromLibSVMFormat(sCurrentLine);
				if (sample != null && !sample.keySet().isEmpty()) {
					Iterator<Integer> itKey = sample.keySet().iterator();
					if (itKey.hasNext()) {
						int classIndex = itKey.next();
						ArrayList<Double> featureArray = sample.get(classIndex);
						double[] features = DIA.getArray(featureArray);

						if (features.length > featureIndexArray.size()) {
							ArrayList<Double> newFeatureArray = new ArrayList<Double>();
							for (Integer featureIndex : featureIndexArray) {
								if (featureIndex < features.length) {
									newFeatureArray.add(features[featureIndex]);
								}
							}

							HashMap<Integer, ArrayList<Double>> newSample = new HashMap<Integer, ArrayList<Double>>();

							newSample.put(classIndex, newFeatureArray);
							DIA.save2LibSvmFile(newSample, bw);
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void save2LibSvmFile(
			ArrayList<HashMap<Integer, ArrayList<Double>>> samples,
			String destLibSvmFile) {
		try {
			if (samples != null) {
				File destFile = new File(destLibSvmFile);
				DIA.deleteFile(destLibSvmFile);

				destFile.createNewFile();

				FileWriter fw = new FileWriter(destFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				for (HashMap<Integer, ArrayList<Double>> sample : samples) {
					DIA.save2LibSvmFile(sample, bw);
				}

				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * randomly select N samples in the file
	 * 
	 * @param libSvmFilePath
	 * @param nbSamples
	 * @return
	 */
	public static ArrayList<HashMap<Integer, ArrayList<Double>>> getSamples(
			String libSvmFilePath, int nbSamples, int maxInterval) {
		if (!(new File(libSvmFilePath)).exists()) {
			System.err.println(libSvmFilePath + " does not exist. ");
			return null;
		}

		int nbRows = getNbRows(libSvmFilePath);
		if (nbSamples > nbRows) {
			nbSamples = nbRows;
		}

		ArrayList<HashMap<Integer, ArrayList<Double>>> samples = null;

		samples = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(libSvmFilePath));

			Random randomGenerator = new Random();
			int interval = 1 + randomGenerator.nextInt(maxInterval);

			int currentNumber = 0;
			while ((sCurrentLine = br.readLine()) != null
					&& samples.size() < nbSamples) {
				currentNumber++;
				if (currentNumber % interval == 0) {
					HashMap<Integer, ArrayList<Double>> sample = DIA
							.getFeatureVectorFromLibSVMFormat(sCurrentLine);
					samples.add(sample);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return samples;
	}

	public static void saveLibsvmSamples2File(String libSvmFilePath,
			String destFilePath, int nbSamples) {
		if (!(new File(libSvmFilePath)).exists()) {
			System.err.println(libSvmFilePath + " does not exist. ");
			return;
		}

		int nbRows = getNbRows(libSvmFilePath);
		if (nbSamples > nbRows) {
			nbSamples = nbRows;
		}

		ArrayList<HashMap<Integer, ArrayList<Double>>> samples = null;
		boolean loop = true;
		int randomSeed = 20;
		while (loop) {
			samples = new ArrayList<HashMap<Integer, ArrayList<Double>>>();
			BufferedReader br = null;
			try {

				String sCurrentLine;
				br = new BufferedReader(new FileReader(libSvmFilePath));

				Random randomGenerator = new Random();
				int interval = 1;
				if (randomSeed > 1) {
					do {
						interval = randomGenerator.nextInt(randomSeed);
					} while (interval == 0);
				} else {
					loop = false;
				}
				int currentNumber = 0;
				while ((sCurrentLine = br.readLine()) != null
						&& samples.size() < nbSamples) {
					currentNumber++;
					if (currentNumber % interval == 0) {
						HashMap<Integer, ArrayList<Double>> sample = DIA
								.getFeatureVectorFromLibSVMFormat(sCurrentLine);
						samples.add(sample);
					}
				}

				if (samples.size() >= nbSamples) {
					loop = false;
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			randomSeed = randomSeed - 1;
		}

		try {
			if (samples != null) {
				File destFile = new File(destFilePath);
				DIA.deleteFile(destFilePath);

				destFile.createNewFile();

				FileWriter fw = new FileWriter(destFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				for (HashMap<Integer, ArrayList<Double>> sample : samples) {
					DIA.save2LibSvmFile(sample, bw);
				}

				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save2LibSvmFile(
			HashMap<Integer, ArrayList<Double>> sample, BufferedWriter bw) {

		try {

			if (bw != null && sample != null && !sample.keySet().isEmpty()) {

				StringBuffer strBuf = new StringBuffer();
				Iterator<Integer> itKey = sample.keySet().iterator();
				if (itKey.hasNext()) {
					int classIndex = itKey.next();
					strBuf.append(classIndex + " ");
					double[] features = DIA.getArray(sample.get(classIndex));

					if (features != null && features.length > 0) {
						for (int i = 0; i < features.length; i++) {
							strBuf.append((i + 1) + ":" + features[i]);
							if (i != features.length - 1) {
								strBuf.append(" ");
							}
						}
						bw.write(strBuf.toString().toString() + "\n");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getNbSamples(String libSvmFiles) {
		File file = new File(libSvmFiles);
		int nbRows = 0;
		if (file.exists()) {
			BufferedReader br = null;
			try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader(libSvmFiles));
				while ((sCurrentLine = br.readLine()) != null) {
					HashMap<Integer, ArrayList<Double>> sample = DIA
							.getFeatureVectorFromLibSVMFormat(sCurrentLine);
					if (sample != null && !sample.keySet().isEmpty()) {
						nbRows++;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return nbRows;
	}

	public static int getNbRows(String filePath) {
		File file = new File(filePath);
		int nbRows = 0;
		if (file.exists()) {
			BufferedReader br = null;
			try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader(filePath));
				while ((sCurrentLine = br.readLine()) != null) {
					nbRows++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return nbRows;
	}

	public static int[] getIntArray(String str, String delimiter) {
		if (str != null && !str.isEmpty()) {
			String[] strs = str.trim().split(delimiter);
			int[] intArray = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				intArray[i] = Integer.parseInt(strs[i].trim());
			}
			return intArray;
		}
		return new int[0];
	}

	public static double[] getDoubleArray(String str, String delimiter) {
		if (str != null && !str.isEmpty()) {
			String[] strs = str.trim().split(delimiter);
			double[] doubleArray = new double[strs.length];
			for (int i = 0; i < strs.length; i++) {
				doubleArray[i] = Double.parseDouble(strs[i].trim());
			}
			return doubleArray;
		}
		return new double[0];
	}

	public static String getString(int[] array, String delimiter) {
		if (array == null) {
			return "";
		}
		String strs = "";
		for (int k = 0; k < array.length; k++) {
			strs += String.valueOf(array[k]);
			if (k != array.length - 1) {
				strs += delimiter;
			}
		}
		return strs;
	}

	public static String getFileDirPath(String filePath) {
		if (filePath.contains(File.separator)) {
			if (filePath.endsWith(File.separator)) {
				return filePath;
			}
			String dirPath = filePath.substring(0, filePath
					.lastIndexOf(File.separator));
			dirPath += File.separator;
			return dirPath;
		}
		return "";
	}

	public static void saveArff2File(String srcArffPath, String destFilePath) {
		BufferedReader br = null;
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(srcArffPath));

			FileWriter fw = new FileWriter(destFilePath);

			DIA.deleteFile(destFilePath);
			File destFile = new File(destFilePath);
			destFile.createNewFile();

			BufferedWriter bw = new BufferedWriter(fw);

			boolean write = false;
			while ((sCurrentLine = br.readLine()) != null) {
				if (write) {
					bw.write(sCurrentLine + "\n");
				}
				if (sCurrentLine.contains("@data")) {
					write = true;
				}
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void save2Arff(String srcFilePath, String arffFilePath,
			String delimiter) {
		File srcFile = new File(srcFilePath);
		if (!srcFile.exists()) {
			System.err.println("file " + srcFile + " does not exits. ");
			return;
		}

		BufferedReader br = null;
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(srcFilePath));

			int nbAttributes = 0;
			// get number of attribute
			if ((sCurrentLine = br.readLine()) != null) {
				String[] strs = sCurrentLine.split(delimiter);
				nbAttributes = strs.length;
			}

			if (nbAttributes > 0) {
				File arffFile = new File(arffFilePath);

				// if file doesnt exists, then create it
				if (!arffFile.exists()) {
					arffFile.createNewFile();
				}

				FileWriter fw = new FileWriter(arffFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				// 1. % Title: e.g. Iris Plants Database
				bw.write("% Title: " + DIA.getFileName(srcFilePath) + "\n");

				// 2. relation: e.g. @RELATION iris
				bw.write("@relation" + " " + DIA.getFileName(srcFilePath)
						+ "\n");

				// 3. feature attributes: e.g. @ATTRIBUTE sepallength REAL
				for (int i = 0; i < nbAttributes; i++) {
					bw.write("@attribute" + " " + i + " " + "real" + "\n");
				}

				// 4. class attribute: e.g. @ATTRIBUTE class
				// {Iris-setosa,Iris-versicolor,Iris-virginica}
				// String[] classNames = getClassArray(dataSet);
				// StringBuffer strBuf = new StringBuffer();
				// for (int i = 0; i < classNames.length; i++) {
				// strBuf.append(classNames[i]);
				// if (i < classNames.length - 1) {
				// strBuf.append(",");
				// }
				// }
				// bw.write("@attribute" + " " + "class" + " " + "{"
				// + strBuf.toString() + "}" + "\n");

				// 5. data: e.g. @data
				bw.write("@data" + "\n");
				br = new BufferedReader(new FileReader(srcFilePath));
				// get number of attribute

				while ((sCurrentLine = br.readLine()) != null) {
					String[] strs = sCurrentLine.split(delimiter);
					if (strs.length == nbAttributes) {
						for (int i = 0; i < strs.length - 1; i++) {
							bw.write(strs[i] + ",");
						}
						bw.write(strs[strs.length - 1] + "\n");
					}

				}

				bw.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	
	public static String getFileName(String filePath) {
		String fileName = filePath;
		if (fileName.contains(File.separator)) {
			fileName = fileName
					.substring(fileName.lastIndexOf(File.separator) + 1);
		}
		return fileName;
	}

	

	public static double[][] getDataSet(String filePath, String delimiter) {
		ArrayList<double[]> dataMatrix = new ArrayList<double[]>();

		BufferedReader br = null;

		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			int length = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] strs = sCurrentLine.trim().split(delimiter);
				double[] vals = new double[strs.length];
				for (int i = 0; i < strs.length; i++) {
					vals[i] = Double.parseDouble(strs[i]);
				}
				dataMatrix.add(vals);
				length = vals.length;
			}

			double[][] matrix = new double[dataMatrix.size()][length];
			int i = 0;
			for (double[] val : dataMatrix) {
				matrix[i] = val;
				i++;
			}

			return matrix;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	public static void showTimeMessage(double startTime, int nbPixelProcessed,
			int nbPixels) {
		double timeUsed = System.currentTimeMillis() - startTime;
		timeUsed = timeUsed * 1.0 / 3600;
		double timeNeed = (nbPixels - nbPixelProcessed)
				* (timeUsed / nbPixelProcessed);
		double percentage = nbPixelProcessed * 100.0 / nbPixels;
		System.out.println(nbPixelProcessed + "/" + nbPixels + " " + percentage
				+ "% pixles have been processed.");
		System.out.println("time used: " + timeUsed + " sec. time need: "
				+ timeNeed + " sec.");
	}

//	public static String[] getImageFilesPath(String imageDirPath) {
//		ArrayList<String> fileArray = new ArrayList<String>();
//		File dir = new File(imageDirPath);
//		if (dir.exists()) {
//			File[] fileList = dir.listFiles();
//			for (File file : fileList) {
//				try {
//					String filePath = file.getCanonicalPath();
//					if (DIA.isImageFile(filePath)) {
//						fileArray.add(filePath);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return getArray(fileArray);
//	}

//	public static String[] getArray(ArrayList<String> strArrayList) {
//		String[] strs = new String[strArrayList.size()];
//		int i = 0;
//		for (String str : strArrayList) {
//			strs[i] = str;
//			i++;
//		}
//		return strs;
//	}

//	public static int[] getArray(ArrayList<Integer> intArray) {
//		int[] array = new int[intArray.size()];
//		int i = 0;
//		for (Integer value : intArray) {
//			array[i] = value;
//			i++;
//		}
//		return array;
//	}

	public static void change2White(String originalImgPath, String destImgPath) {
		if ((new File(originalImgPath)).exists()) {
			ImagePlus originalImp = new ImagePlus(originalImgPath);
			ImageProcessor originalIp = originalImp.getProcessor();
			ImageProcessor destIp = originalIp.duplicate();
			int width = originalIp.getWidth();
			int height = originalIp.getHeight();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int[] pixelValues = new int[3];
					pixelValues = destIp.getPixel(i, j, pixelValues);
					if (pixelValues[0] != 0 || pixelValues[1] != 0
							|| pixelValues[2] != 0) {
						destIp.putPixel(i, j, new int[] { 255, 255, 255 });
					}
				}
			}
			ImagePlus destImp = new ImagePlus("black image", destIp);
			destImp.show();
			String extension = getFileExtension(destImgPath);
			if (extension.isEmpty()) {
				extension = "png";
			}
			saveImage(destImp.getBufferedImage(), extension, destImgPath);
		}
	}

	public static void createBlackImage(String originalImgPath,
			String destImgPath) {
		if ((new File(originalImgPath)).exists()) {
			ImagePlus originalImp = new ImagePlus(originalImgPath);
			ImageProcessor originalIp = originalImp.getProcessor();
			ImageProcessor destIp = originalIp.duplicate();
			int width = originalIp.getWidth();
			int height = originalIp.getHeight();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					destIp.putPixel(i, j, new int[] { 0, 0, 0 });
				}
			}
			ImagePlus destImp = new ImagePlus("black image", destIp);
			String extension = getFileExtension(destImgPath);
			if (extension.isEmpty()) {
				extension = "png";
			}
			saveImage(destImp.getBufferedImage(), extension, destImgPath);
		}
	}

	public static void createBlackImage(String originalImgPath,
			String destImgPath, int[] whiteRect) {
		if ((new File(originalImgPath)).exists()) {
			ImagePlus originalImp = new ImagePlus(originalImgPath);
			ImageProcessor originalIp = originalImp.getProcessor();
			ImageProcessor destIp = originalIp.duplicate();
			int width = originalIp.getWidth();
			int height = originalIp.getHeight();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					destIp.putPixel(i, j, new int[] { 0, 0, 0 });
				}
			}
			for (int i = whiteRect[0]; i <= whiteRect[2]; i++) {
				for (int j = whiteRect[1]; j <= whiteRect[3]; j++) {
					destIp.putPixel(i, j, new int[] { 255, 255, 255 });
				}
			}
			ImagePlus destImp = new ImagePlus("black image", destIp);
			// destImp.show();
			String extension = getFileExtension(destImgPath);
			if (extension.isEmpty()) {
				extension = "png";
			}
			saveImage(destImp.getBufferedImage(), extension, destImgPath);
		}
	}

	public static double[] getArray(ArrayList<Double> arrayList) {
		if (arrayList != null) {
			double[] array = new double[arrayList.size()];
			int i = 0;
			for (Double data : arrayList) {
				array[i] = data;
				i++;
			}
			return array;
		}
		return null;
	}



	public static void executePython(String pythonScriptPath,
			ArrayList<String> args) {
		// String pythonScriptPath = "grabcut.py";
		// String[] cmd = new String[7];
		// // cmd[0] = "python2.7";
		// cmd[0] = "python";
		// cmd[1] = pythonScriptPath;
		// cmd[2] = "/media/volume1/messi5.jpg";
		// cmd[3] = "50";
		// cmd[4] = "50";
		// cmd[5] = "450";
		// cmd[6] = "290";

		String[] cmd = new String[args.size()];
		for (int i = 0; i < cmd.length; i++) {
			cmd[i] = args.get(i);
		}

		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr;
		try {
			pr = rt.exec(cmd);
			// retrieve output from python script
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr
					.getInputStream()));
			String line = "";
			while ((line = bfr.readLine()) != null) {
				// display each output line form python script
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void cropFile(String originalFilePath, String destFilePath,
			int nbLine, int interval) {

		try {
			FileReader fileReader = new FileReader(originalFilePath);
			BufferedReader br = new BufferedReader(fileReader);

			if (!(new File(destFilePath)).exists()) {
				(new File(destFilePath)).createNewFile();
			}
			FileWriter fileWriter = new FileWriter(destFilePath);
			BufferedWriter bw = new BufferedWriter(fileWriter);

			int nbTotalLine = 0;
			String line;
			int nbLineWritten = 0;
			while ((line = br.readLine()) != null && nbLineWritten < nbLine) {
				nbTotalLine++;
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(interval) + 1;
				int i = 0;
				while ((line = br.readLine()) != null && i < randomInt) {
					i++;
					nbTotalLine++;
				}
				if (line != null) {
					bw.write(line + "\n");
					nbLineWritten++;
				}
			}

			br.close();
			bw.close();

			System.out.println("total number lines: " + nbTotalLine);
			System.out.println(nbLineWritten + " lines have been copied to "
					+ destFilePath);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static File getFeatureFile(String imageFile, File[] featureFileList) {
		// find correspanding feature file
		File testFeatureFile = null;
		for (int j = 0; j < featureFileList.length; j++) {
			String testFeatureName = featureFileList[j].getName();
			if (testFeatureName.contains(imageFile)) {
				testFeatureFile = featureFileList[j];
				return testFeatureFile;
			}
		}

		return null;
	}


	public static HashMap<Integer, ArrayList<Double>> getFeatureVectorFromLibSVMFormat(
			String line) {
		HashMap<Integer, ArrayList<Double>> featureMap = new HashMap<Integer, ArrayList<Double>>();

		StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

		// get class index
		int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

		int m = st.countTokens() / 2;

		// get max feature dimension
		int maxIndex = Integer.MIN_VALUE;
		for (int j = 0; j < m; j++) {
			int index = DIA.atoi(st.nextToken());
			double value = DIA.atof(st.nextToken());
			if (index > maxIndex) {
				maxIndex = index;
			}
		}

		double[] featureArray = new double[maxIndex];
		for (int j = 0; j < featureArray.length; j++) {
			featureArray[j] = 0.0;
		}

		st = new StringTokenizer(line, " \t\n\r\f:");
		classIndex = (int) Math.round(DIA.atof(st.nextToken()));
		for (int j = 0; j < m; j++) {
			int index = DIA.atoi(st.nextToken());
			double value = DIA.atof(st.nextToken());
			index = index - 1;
			featureArray[index] = value;
		}

		ArrayList<Double> featureVector = new ArrayList();
		for (double value : featureArray) {
			featureVector.add(value);
		}

		featureMap.put(classIndex, featureVector);

		return featureMap;
	}

	public static int getFeatureDimension(String featureFilePath) {
		int featureDimension = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					featureFilePath));
			String line;
			if ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
				// get class index
				int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

				// extract feature vector
				int m = st.countTokens() / 2;
				ArrayList<Double> featureVector = new ArrayList<Double>();
				for (int j = 0; j < m; j++) {
					int index = DIA.atoi(st.nextToken());
					double value = DIA.atof(st.nextToken());
					featureDimension = index;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featureDimension;
	}

	public static void saveMean(String svmFilePath, String destFilePath) {
		try {
			BufferedReader br = null;

			br = new BufferedReader(new FileReader(svmFilePath));

			String line;
			int totalNbLine = 0;
			while ((line = br.readLine()) != null) {
				totalNbLine++;
			}

			HashMap<Integer, ArrayList<Double>> featureMap = new HashMap<Integer, ArrayList<Double>>();
			HashMap<Integer, Integer> nbElemMap = new HashMap<Integer, Integer>();

			br = new BufferedReader(new FileReader(svmFilePath));
			int nbLine = 0;
			double startTime = System.currentTimeMillis();

			// calculate total
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				// while ((line = br.readLine()) != null && nbLine < 1000) {
				nbLine++;

				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

				// get class index
				int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

				// extract feature vector
				int m = st.countTokens() / 2;
				ArrayList<Double> featureVec = new ArrayList<Double>();
				for (int j = 0; j < m; j++) {
					int index = DIA.atoi(st.nextToken());
					featureVec.add(DIA.atof(st.nextToken()));
				}
				// System.out.println(featureVec.size());

				if (featureMap.containsKey(classIndex)) {
					ArrayList<Double> feature = featureMap.get(classIndex);
					ArrayList<Double> newFeature = new ArrayList<Double>();

					if (feature.size() == featureVec.size()) {
						int i = 0;
						for (Double value : feature) {
							// System.out.println(value + "+" +
							// featureVec.get(i)
							// + "=" + (value + featureVec.get(i)));
							newFeature.add(value + featureVec.get(i));
							i++;
						}
						featureMap.put(classIndex, newFeature);
					} else {
						// System.err.println(classIndex+" "+feature.size() +
						// " "
						// + featureVec.size());
					}

				} else {
					featureMap.put(classIndex, featureVec);
				}

				if (nbElemMap.containsKey(classIndex)) {
					int nbElem = nbElemMap.get(classIndex);
					nbElem += 1;
					nbElemMap.put(classIndex, nbElem);
				} else {
					nbElemMap.put(classIndex, 1);
				}

				if (nbLine % 100000 == 0) {
					double time = System.currentTimeMillis() - startTime;
					time = time / 1000.0 / 60.0;
					double timeRest = (time / nbLine) * (totalNbLine - nbLine);
					double percentage = nbLine * 1.0 / totalNbLine;
					System.out
							.println(nbLine
									+ "/"
									+ totalNbLine
									+ " "
									+ percentage
									+ "%  of lines have been processed.  time used(min): "
									+ time + " time need(min): " + timeRest);
				}
			}

			// calculate mean
			for (Integer classIndex : featureMap.keySet()) {
				if (nbElemMap.containsKey(classIndex)) {
					int nbElem = nbElemMap.get(classIndex);
					ArrayList<Double> featureVec = featureMap.get(classIndex);
					ArrayList<Double> newFeatureVec = new ArrayList<Double>();
					for (Double value : featureVec) {
						value = value * 1.0 / nbElem;
						// DecimalFormat df = new DecimalFormat("#.####");
						// value = Double.valueOf(df.format(value));
						newFeatureVec.add(value);
					}
					featureMap.put(classIndex, newFeatureVec);
				}
			}

			// save to file
			File destFile = new File(destFilePath);
			if (destFile.exists()) {
				destFile.delete();
			}
			destFile.createNewFile();

			FileWriter fw = new FileWriter(destFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (Integer classIndex : featureMap.keySet()) {
				StringBuffer strBuf = new StringBuffer();
				strBuf.append(classIndex + " ");
				ArrayList<Double> feature = featureMap.get(classIndex);
				int i = 0;
				for (Double value : feature) {
					strBuf.append((i + 1) + ":" + value);
					if (i < feature.size() - 1) {
						strBuf.append(" ");
					} else {
						strBuf.append("\n");
					}
					i++;
				}
				// System.out.println(strBuf.toString());
				bw.write(strBuf.toString());
			}

			bw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static HashMap<Integer, double[][]> getFeaturematrixFromSvmFile(
			String svmFile) {
		HashMap<Integer, double[][]> featureMatrixMap = new HashMap<Integer, double[][]>();

		HashMap<Integer, int[]> classNbDim = new HashMap<Integer, int[]>();
		// get #dimension
		// get #lines
		try {
			BufferedReader br = new BufferedReader(new FileReader(svmFile));
			String line;
			int totalNbLine = 0;
			while ((line = br.readLine()) != null) {
				totalNbLine++;
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

				// get class index
				int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

				// extract feature vector
				int m = st.countTokens() / 2;
				int index = 0;
				for (int j = 0; j < m; j++) {
					index = DIA.atoi(st.nextToken());
					double value = DIA.atof(st.nextToken());
				}

				// array[0]: #feature dimension
				// array[1]: #samples
				int[] array = new int[2];
				array[0] = index;
				array[1] = 1;

				if (classNbDim.containsKey(classIndex)) {
					int nbDimension = classNbDim.get(classIndex)[0];
					if (index > nbDimension) {
						nbDimension = index;
					}
					int nbSamples = classNbDim.get(classIndex)[1];
					nbSamples += 1;

					array[0] = nbDimension;
					array[1] = nbSamples;
				}
				classNbDim.put(classIndex, array);

			}
			br.close();

			for (Integer classIndex : classNbDim.keySet()) {
				int[] array = classNbDim.get(classIndex);
				double[][] featureMatrix = new double[array[1]][array[0]];
				// System.out.println(featureMatrix.length + " "
				// + featureMatrix[0].length);
				featureMatrixMap.put(classIndex, featureMatrix);
			}

			HashMap<Integer, Integer> currentSampleIndex = new HashMap<Integer, Integer>();
			br = new BufferedReader(new FileReader(svmFile));
			int currentLine = 0;
			double startTime = System.currentTimeMillis();
			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

				// get class index
				int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

				int rowIndex = 0;
				if (currentSampleIndex.containsKey(classIndex)) {
					rowIndex = currentSampleIndex.get(classIndex);
					currentSampleIndex.put(classIndex, rowIndex + 1);
				} else {
					currentSampleIndex.put(classIndex, 1);
				}

				double[][] featureMatrix = featureMatrixMap.get(classIndex);
				int m = st.countTokens() / 2;
				for (int j = 0; j < m; j++) {
					int colIndex = DIA.atoi(st.nextToken());
					colIndex -= 1;
					double value = DIA.atof(st.nextToken());
					featureMatrix[rowIndex][colIndex] = value;
				}

				featureMatrixMap.put(classIndex, featureMatrix);

				currentLine++;
				// if (currentLine % 1000000 == 0) {
				// double time = System.currentTimeMillis() - startTime;
				// time = time * 1.0 / 1000 / 60;
				// double timeNeed = (time / currentLine)
				// * (totalNbLine - currentLine);
				// double percentage = currentLine * 1.0 / totalNbLine;
				// System.out.println(currentLine + "/" + totalNbLine
				// + " lines percentage"
				// + "% have been processed. time used(min):" + time
				// + " time need(min):" + timeNeed);
				// }
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featureMatrixMap;
	}

	public static double[] getMeanVector(double[][] matrix) {
		double[] meanVec = new double[matrix[0].length];
		for (int i = 0; i < meanVec.length; i++) {
			meanVec[i] = 0.0;
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				meanVec[j] += matrix[i][j];
			}
		}
		for (int i = 0; i < meanVec.length; i++) {
			meanVec[i] = meanVec[i] / matrix.length;
		}
		return meanVec;
	}

	public static HashMap<Integer, ArrayList<ArrayList<Double>>> getFeatureVectorsFromSvmFile(
			String svmFile) throws Exception {
		if (!(new File(svmFile)).exists()) {
			throw new Exception("File doesn't exist.");
		}

		HashMap<Integer, ArrayList<ArrayList<Double>>> featureMap = new HashMap<Integer, ArrayList<ArrayList<Double>>>();

		BufferedReader br = null;
		br = new BufferedReader(new FileReader(svmFile));
		String line;
		int totalNbLine = 0;
		while ((line = br.readLine()) != null) {
			totalNbLine++;
		}
		br.close();

		br = new BufferedReader(new FileReader(svmFile));
		int nbLine = 0;
		double startTime = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			// while ((line = br.readLine()) != null && nbLine < 1000) {
			nbLine++;

			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

			// get class index
			int classIndex = (int) Math.round(DIA.atof(st.nextToken()));

			// extract feature vector
			int m = st.countTokens() / 2;
			ArrayList<Double> featureVector = new ArrayList<Double>();
			for (int j = 0; j < m; j++) {
				int index = DIA.atoi(st.nextToken());
				featureVector.add(DIA.atof(st.nextToken()));
			}

			ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
			if (featureMap.containsKey(classIndex)) {
				features = featureMap.get(classIndex);
			}

			// System.out.println(featureVector.size());
			features.add(featureVector);
			featureMap.put(classIndex, features);

			// if (nbLine % 100 == 0) {
			// double time = System.currentTimeMillis() - startTime;
			// time = time / 1000.0 / 60.0;
			// double timeRest = (time / nbLine) * (totalNbLine - nbLine);
			// System.out.println(nbLine + "/" + totalNbLine
			// + " time used(min): " + time + " time need(min): "
			// + timeRest);
			// }
		}

		br.close();
		// System.out.println(featureMap.keySet().size()+" "+nbLine);
		// System.out.println();

		return featureMap;
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static void saveImage(BufferedImage image, String type,
			String destPath) {
		if (image == null) {
			System.err.println("image is null");
			return;
		}

		try {
			// ImageIO.write((BufferedImage) resultImg.getImage(), "png",
			// new File(resultImgFilePath));
			ImageIO.write(image, type, new File(destPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getWorkingDir() {
		String workingDir = System.getProperty("user.dir");
		if (!workingDir.endsWith(File.separator)) {
			workingDir += File.separator;
		}
		return workingDir;
	}

	public static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	public static int atoi(String s) {
		return Integer.parseInt(s);
	}

	public static int[] getInts(String arg) {
		String[] strs = getStrings(arg);
		int[] array = new int[strs.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = Integer.parseInt(strs[i]);
		}
		return array;
	}

	public static String[] getStrings(String arg) {
		String[] strs;
		arg = arg.trim();
		if (arg.contains(",")) {
			strs = arg.split(",");
		} else {
			strs = new String[1];
			strs[0] = arg;
		}
		return strs;
	}

	public static boolean isColorImage(ImagePlus imp) {
		if (imp != null) {
			if (imp.getType() == ImagePlus.COLOR_256
					|| imp.getType() == ImagePlus.COLOR_RGB) {
				return true;
			}
		}

		return false;
	}

	public static boolean isGrayImage(ImagePlus imp) {
		if (imp != null) {
			if (imp.getType() == ImagePlus.GRAY32
					|| imp.getType() == ImagePlus.GRAY16
					|| imp.getType() == ImagePlus.GRAY8) {
				return true;
			}
		}

		return false;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
		// '-' and decimal.
	}

	public static void saveFeatureToSvmFileFormat(String fileName,
			ArrayList<Double> features, int classIndex) {

		try {

			// System.out.println(font);
			if (classIndex >= 0 && features.size() > 0) {
				// Create file
				BufferedWriter out = new BufferedWriter(new FileWriter(
						fileName, true));

				StringBuffer strBuf = new StringBuffer();
				strBuf.append(classIndex + " ");
				int i = 0;
				for (Double value : features) {
					String str = (i + 1) + ":" + value + " ";
					strBuf.append(str);
					i++;
				}
				strBuf.append("\n");

				// System.out.println(strBuf.toString());
				String startStr = strBuf.toString().substring(0, 1);
				if (isNumeric(startStr)) {
					out.write(strBuf.toString());
				}

				out.close();
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println(e.toString());
			e.getMessage();
		}
	}

	public static Rectangle scaleRectangle(Rectangle rect, double scaleX,
			double scaleY) {
		// AffineTransform af = AffineTransform.getScaleInstance(scaleX,
		// scaleY);
		AffineTransform tx = new AffineTransform();
		tx.scale(scaleX, scaleY);

		Shape shape = tx.createTransformedShape(rect);

		return shape.getBounds();
	}

	public static ArrayList<File> getImageFile(String filePath) {
		File file = new File(filePath);
		ArrayList<File> files = new ArrayList<File>();
		if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				try {
					files.addAll(getImageFile(fileList[i].getCanonicalPath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (int i = 0; i < IMAGE_TYPE.length; i++) {
				if (filePath.endsWith(IMAGE_TYPE[i])) {
					files.add(file);
				}
			}
		}

		return files;
	}


	public static boolean isImageFile(String fileName) {

		for (int i = 0; i < IMAGE_TYPE.length; i++) {
			if (fileName.toLowerCase().endsWith(IMAGE_TYPE[i])) {
				return true;
			}
		}

		return false;
	}

	public static String chineseCharacterEncode(String str, int code)
			throws UnsupportedEncodingException {
		Base64 base64 = new Base64();
		String encode = new String(base64.decode(str.getBytes()), CODE[code]);
		return encode;
	}

	public static float[] getHsvFromRgb(int[] rgb) {
		float[] hsv = new float[3];

		hsv = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv);

		return hsv;
	}

	public static int[] getRGB(int clr) {
		int[] rgb = new int[3];
		int red = (clr & 0x00ff0000) >> 16;
		int green = (clr & 0x0000ff00) >> 8;
		int blue = clr & 0x000000ff;
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
		return rgb;
	}

	public static void deleteFile(String fileName) {
		// A File object to represent the filename
		File f = new File(fileName);

		// Make sure the file or directory exists and isn't write protected
		if (!f.exists()) {
			// System.err.println(fileName+" does not exist.");
			return;
			// throw new IllegalArgumentException(
			// "Delete: no such file or directory: " + fileName);
		}

		if (!f.canWrite()) {
			System.err.println(fileName + " can't be written.");
			return;
			// throw new IllegalArgumentException("Delete: write protected: "
			// + fileName);
		}

		// If it is a directory, make sure it is empty
		if (f.isDirectory()) {
			DIA.deleteDir(f);
			// String[] files = f.list();
			// if (files.length > 0) {
			// throw new IllegalArgumentException(
			// "Delete: directory not empty: " + fileName);
			// }
		}

		// Attempt to delete it
		boolean success = f.delete();

		if (!success) {
			System.err.println(fileName + " deletion failed.");
			// throw new IllegalArgumentException("Delete: deletion failed");
		}
	}

	public static boolean deleteDir(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDir(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static String getStrFromFile(String filePath) {
		BufferedReader br = null;

		StringBuffer strBuf = new StringBuffer();

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				strBuf.append(sCurrentLine);
				strBuf.append("\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return strBuf.toString();
	}

	public static void write2File(String filename, String str, boolean append) {

		File file = new File(filename);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// System.out.println("write log to " + filename);
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(filename, append));
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param hist
	 *            projection profile histogram
	 * @param interval
	 *            block distance
	 * @param threshold
	 *            no white block #pixels
	 * @param replaceValue
	 *            replace white block #pixels value
	 * @return
	 */
	public static int[] RLSA(int[] hist, int interval, int threshold,
			int replaceValue) {

		int[] newHist = Arrays.copyOf(hist, hist.length);

		int startPos = 0;
		while (startPos < newHist.length - interval) {
			int currentPos = startPos;
			int len = 0;
			while (currentPos < newHist.length
					&& newHist[currentPos] <= threshold) {
				currentPos++;
				len++;
			}
			if (len < interval) {
				for (int i = startPos; i < currentPos; i++) {
					newHist[i] = replaceValue;
				}
			}
			if (len != 0) {
				startPos += len;
			} else {
				startPos++;
			}
		}

		return newHist;
	}

	public static double[] toDouble(int[] intArray) {
		double[] doubleArray = new double[intArray.length];

		for (int i = 0; i < doubleArray.length; i++) {
			doubleArray[i] = (double) intArray[i];
		}

		return doubleArray;
	}

	public static int[] copy(int[] array) {
		int[] newArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

//	public static void showProjectionHistogram(int[] histogram) {
//		int[] newArray = copy(histogram);
//		Arrays.sort(newArray);
//
//		DiaHistogramFrame frame = new DiaHistogramFrame(toDouble(histogram),
//				histogram.length, 0, newArray[newArray.length - 1]);
//
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setBounds(10, 10, 800, 500);
//		frame.setTitle("projection");
//		frame.setVisible(true);
//
//	}

	public static ImagePlus addBorder(ImagePlus imp, int border,
			int backgroundPixel) {
		ImageProcessor ipOriginal = imp.getProcessor();
		int width = imp.getWidth();
		int height = imp.getHeight();

		ImagePlus newImp = IJ.createImage("bordered image", width + 2 * border,
				height + 2 * border, imp.getSlice(), imp.getBitDepth());
		ImageProcessor ip = newImp.getProcessor();

		for (int i = 0; i < ip.getWidth(); i++) {
			for (int j = 0; j < ip.getHeight(); j++) {
				ip.putPixel(i, j, backgroundPixel);
			}
		}

		for (int i = 0; i < ipOriginal.getWidth(); i++) {
			for (int j = 0; j < ipOriginal.getHeight(); j++) {
				ip.putPixel(i + border, j + border, ipOriginal.getPixel(i, j));
			}
		}

		return new ImagePlus("bordered image", ip);
	}

	public static String getFileExtension(String filePath) {
		if (!filePath.contains(".")) {
			return "";
		} else {
			if (filePath.endsWith(".")) {
				return "";
			} else {
				String fileExtension = filePath.substring(filePath
						.lastIndexOf(".") + 1);
				return fileExtension;
			}
		}
	}

	public static ImagePlus cropImage(ImagePlus imp, int x, int y, int w, int h) {

		ImageProcessor ip = imp.getProcessor();
		ip.setRoi(x, y, w, h);
		ip = ip.crop();
		return new ImagePlus("cropped image", ip);

		/*
		 * BufferedImage image = imp.getBufferedImage();
		 * 
		 * image = image.getSubimage(x, y, w, h);
		 * 
		 * return new ImagePlus("cropped image", image);
		 */
	}

	public static ImagePlus cropImage2(ImagePlus imp, int x, int y, int w, int h) {

		ImageProcessor ip = imp.getProcessor();
		BufferedImage image = ip.getBufferedImage();
		image = image.getSubimage(x, y, w, h);

		return new ImagePlus("cropped image", image);
	}

	public static ImagePlus imageCrop(ImagePlus imp, int cropX, int cropY,
			int targetWidth, int targetHeight) {
		ImageProcessor ip = imp.getProcessor();
		ip.setRoi(cropX, cropY, targetWidth, targetHeight);
		ip = ip.crop();
		return new ImagePlus("cropped image", ip);
	}

//	public static void showFrequenceHistogram(double[] dataSet,
//			int maxFrequence, String plotTitle, String xaxis, String yaxis,
//			PlotOrientation orientation, int width, int height) {
//
//		HistogramDataset dataset = new HistogramDataset();
//		dataset.setType(HistogramType.FREQUENCY);
//		dataset.addSeries("Histogram", dataSet, maxFrequence);
//		// String plotTitle = "Histogram";
//		// String xaxis = "image width";
//		// String yaxis = "Run";
//		// PlotOrientation orientation = PlotOrientation.VERTICAL;
//		boolean show = false;
//		boolean toolTips = false;
//		boolean urls = false;
//
//		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis,
//				yaxis, dataset, orientation, show, toolTips, urls);
//
//		// int width = 500;
//		// int height = 300;
//
//		JFrame frame = new JFrame();
//		ChartPanel cpanel = new ChartPanel(chart);
//		frame.getContentPane().add(cpanel, BorderLayout.CENTER);
//		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setBounds(10, 10, 500, 500);
//		frame.setTitle("vertical projection");
//		frame.setVisible(true);
//		/*
//		 * try { ChartUtilities.saveChartAsPNG(new File("histogram.PNG"), chart,
//		 * width, height); } catch (IOException e) { }
//		 */
//	}

	/**
	 * Convenience method that returns a scaled instance of the provided {@code
	 * BufferedImage}.
	 * 
	 * @param img
	 *            the original image to be scaled
	 * @param targetWidth
	 *            the desired width of the scaled instance, in pixels
	 * @param targetHeight
	 *            the desired height of the scaled instance, in pixels
	 * @param hint
	 *            one of the rendering hints that corresponds to {@code
	 *            RenderingHints.KEY_INTERPOLATION} (e.g. {@code
	 *            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR}, {@code
	 *            RenderingHints.VALUE_INTERPOLATION_BILINEAR}, {@code
	 *            RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality
	 *            if true, this method will use a multi-step scaling technique
	 *            that provides higher quality than the usual one-step technique
	 *            (only useful in down-scaling cases, where {@code targetWidth}
	 *            or {@code targetHeight} is smaller than the original
	 *            dimensions, and generally only when the {@code BILINEAR} hint
	 *            is specified)
	 * @return a scaled version of the original {@codey BufferedImage}
	 */
	public static BufferedImage getScaledInstance(BufferedImage img,
			int targetWidth, int targetHeight, Object hint,
			boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			// g2.setRenderingHint(RenderingHints.VALUE_INTERPOLATION_BICUBIC,
			// hint);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public static Polygon getPolygon(Rectangle rectangle) {
		int[] xpoints = new int[4];
		xpoints[0] = rectangle.x;
		xpoints[1] = rectangle.width;
		xpoints[2] = xpoints[1];
		xpoints[3] = xpoints[0];

		int[] ypoints = new int[4];
		ypoints[0] = rectangle.y;
		ypoints[1] = ypoints[0];
		ypoints[2] = rectangle.y + rectangle.height;
		ypoints[3] = rectangle.y + rectangle.height;

		Polygon poly = new Polygon(xpoints, ypoints, xpoints.length);

		return poly;
	}

	public static ImagePlus removeNoise(ImagePlus imp, int radius,
			double percentagePixel) {
		ImageProcessor original = imp.getProcessor();
		ImageProcessor copy = original.duplicate();

		// ImagePlus newImp = new ImagePlus("", imp.getImage());
		// ImageProcessor copy = newImp.getProcessor();

		/*
		 * for (int i = 0; i <= original.getWidth() - 1; i++) { for (int j = 0;
		 * j <= original.getHeight() - 1; j++) {
		 * 
		 * if (original.getPixel(i, j) != Projection.FORE_PIXEL &&
		 * original.getPixel(i, j) != Projection.BACK_PIXEL) {
		 * System.err.println(i + " " + j + " " + original.getPixel(i, j)); } }
		 * }
		 */

		for (int i = radius; i <= original.getWidth() - 1 - radius; i++) {
			for (int j = radius; j <= original.getHeight() - 1 - radius; j++) {
				int minPixel = Integer.MAX_VALUE;
				int nbForePixel = 0;
				if (copy.getPixel(i, j) == OtsuBinary.FORE_PIXEL) {
					for (int w = -radius; w <= radius; w++) {
						for (int h = -radius; h <= radius; h++) {
							if (copy.getPixel(i + w, j + h) == OtsuBinary.FORE_PIXEL) {
								nbForePixel++;
							}
						}
					}

					int nbPixel = (2 * radius + 1) * (2 * radius + 1);
					double forePixelPercentage = nbForePixel * 1.0 / nbPixel;

					// System.out.println(forePixelPercentage);

					// if(nbForePixel < 5) {
					if (forePixelPercentage < percentagePixel) {
						original.putPixel(i, j, OtsuBinary.BACK_PIXEL);
					}
				}

			}
		}

		return new ImagePlus("new image", original);
	}

	/*
	 * public static BufferedImage getScaleImage(BufferedImage originalImage,
	 * double scaleX, double scaleY) { int w = originalImage.getWidth(); int h =
	 * originalImage.getHeight(); BufferedImage after = new BufferedImage(w, h,
	 * BufferedImage.TYPE_INT_ARGB); AffineTransform at = new AffineTransform();
	 * at.scale(scaleX, scaleY); AffineTransformOp scaleOp = new
	 * AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR); after =
	 * scaleOp.filter(originalImage, after);
	 * 
	 * return after; }
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String imagePath = "/media/volume1/hisdoc/data/st-gallen/testing/csg562-041.png";

		
		// DIA.save2Arff("/media/volume1/hisdoc/parzival/pca/testingFeature.txt",
		// "/media/volume1/hisdoc/parzival/pca/testingFeature.arff", " ");

		// DIA.change2White("/media/volume1/d-006-textline-1-mask-1.png",
		// "/media/volume1/d-006-textline-1-mask-2.png");
		// DIA.change2White("/media/volume1/d-006-textline-2-gc-mask-2.png",
		// "/media/volume1/d-006-textline-2-mask-3.png");
		// DIA.createBlackImage("/media/volume1/d-006-textline-1.png",
		// "/media/volume1/d-006-textline-1-mask.png");
		// DIA.createBlackImage("/media/volume1/d-006-textline-2.png",
		// "/media/volume1/d-006-textline-2-mask-2.png", new
		// int[]{10,5,395,45});
		// DIA.createBlackImage("/media/volume1/messi5.jpg",
		// "/media/volume1/messi5-mask.jpg");

		// String originalFilePath =
		// "/media/volume1/hisdoc/parzival/test.feature.RIU_LBP2/RIU_LBP2/4.5.8-16.1-2.1000.5.1.1.svm.scale.orig";
		// String destFilePath =
		// "/media/volume1/hisdoc/parzival/test.feature.RIU_LBP2/RIU_LBP2/4.5.8-16.1-2.1000.5.1.1.svm.scale";
		//
		// DIA.cropFile(originalFilePath, destFilePath, 50000, 10);

	}

}

