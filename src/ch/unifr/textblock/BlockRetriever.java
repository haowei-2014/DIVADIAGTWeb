/**
 * 
 */
package ch.unifr.textblock;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The class is used to retrieve words on a page image by analyze the vertical
 * and horizontal projection profile.
 * 
 * @author ck
 * 
 */

public class BlockRetriever {

	// public static int HOR_BORDER_WIDTH = 0;
	// public static int HOR_BORDER_HEIGHT = 0;

	// public static int VER_BORDER_WIDTH = 0;
	// public static int VER_BORDER_HEIGHT = 0;

	/**
	 * get the average background pixels value
	 * 
	 * @param imageOriginal
	 *            the original image
	 * @param imageBin
	 *            binary image
	 * @return
	 */
	public static int getBackgroundPixelColor(ImagePlus imageOriginal,
			ImagePlus imageBin) {
		int backgroundPixel = 0;
		int nbBackgroundPixel = 0;
		int backR = 0;
		int backG = 0;
		int backB = 0;
		ImageProcessor ipOriginal = imageOriginal.getProcessor();
		ImageProcessor ipBin = imageBin.getProcessor();
		for (int i = 0; i < imageOriginal.getWidth(); i++) {
			for (int j = 0; j < imageOriginal.getHeight(); j++) {
				if (ipBin.get(i, j) == OtsuBinary.BACK_PIXEL) {
					nbBackgroundPixel++;
					int c = ipOriginal.getPixel(i, j);

					int r = (c & 0xff0000) >> 16;
					int g = (c & 0x00ff00) >> 8;
					int b = (c & 0x0000ff);

					backR += r;
					backG += g;
					backB += b;
				}
			}
		}

		backR = backR / nbBackgroundPixel;
		backG = backG / nbBackgroundPixel;
		backB = backB / nbBackgroundPixel;

		backgroundPixel = ((backR & 0xff) << 16) | ((backG & 0xff) << 8)
				| backB & 0xff;

		return backgroundPixel;
	}

	public static double getAverageVerticalInterneDist(ImagePlus imageOriginal) {
		int width = imageOriginal.getWidth();
		int height = imageOriginal.getHeight();

		ImagePlus imageBin = imageOriginal;

		// binarization
		if (imageBin.getType() == ImagePlus.COLOR_256
				|| imageBin.getType() == ImagePlus.COLOR_RGB
				|| imageBin.getType() == ImagePlus.GRAY32
				|| imageBin.getType() == ImagePlus.GRAY16
				|| imageBin.getType() == ImagePlus.GRAY8) {
			OtsuBinary ob = new OtsuBinary();
			imageBin = ob.binarization(imageOriginal);
		}

		int startY = height / 8;
		int endY = height / 4;

		// create vertical projection profile
		Projection verProj = new Projection(imageBin);
		int[] horProjHist = verProj.getHorizontalProjection(startY, endY + 1,
				1, false);

		// find peaks
		// int peakY = startY;
		// int footY = startY;
		// int peakValue = 0;
		/*
		 * for (int i = startY + 1; i <= endY; i++) { if (horProjHist[i] >
		 * horProjHist[peakY]) { peakY = i; peakValue = horProjHist[i]; } if
		 * (horProjHist[i] < horProjHist[footY]) { footY = i; } }
		 */

		ArrayList<Integer> gapDist = new ArrayList<Integer>();

		int startX = width / 8;
		int endX = width / 4;

		ImageProcessor ip = imageBin.getProcessor();

		for (int i = startX; i <= startX + 1; i++) {
			for (int j = startY; j <= endY; j++) {
				boolean find = false;
				int k = j + 1;
				if (ip.getPixel(i, j) == OtsuBinary.BACK_PIXEL
						&& ip.getPixel(i, k) == OtsuBinary.FORE_PIXEL) {
					k = k + 1;
					while (!find && k <= endY - 1) {
						if (ip.getPixel(i, k) == OtsuBinary.BACK_PIXEL) {
							find = true;
							gapDist.add(k - j);
							j = k;
						} else {
							k++;
						}
					}
				} else {
					i++;
				}
			}
		}

		double averageDist = 0.0;
		if (gapDist.size() > 0) {
			for (Integer dist : gapDist) {
				averageDist += dist;
			}
			averageDist = averageDist * 1.0 / gapDist.size();
		}

		return averageDist;
	}

	public static double getAverageHorizontalInterneDist(ImagePlus imageOriginal) {
		int width = imageOriginal.getWidth();
		int height = imageOriginal.getHeight();

		ImagePlus imageBin = imageOriginal;

		// imageBin.show();
		// binarization
		if (imageBin.getType() == ImagePlus.COLOR_256
				|| imageBin.getType() == ImagePlus.COLOR_RGB
				|| imageBin.getType() == ImagePlus.GRAY32
				|| imageBin.getType() == ImagePlus.GRAY16
				|| imageBin.getType() == ImagePlus.GRAY8) {
			OtsuBinary ob = new OtsuBinary();
			imageBin = ob.binarization(imageOriginal);
		}

		// imageBin.show();

		int startX = width / 4;
		int endX = width / 2;

		// create vertical projection profile
		Projection verProj = new Projection(imageBin);
		int[] verProjHist = verProj.getVerticalProjection(startX, endX + 1, 1,
				false);

		// find peaks
		int peakX = startX;
		int footX = startX;
		int peakValue = 0;
		for (int i = startX + 1; i <= endX; i++) {
			if (verProjHist[i] > verProjHist[peakX]) {
				peakX = i;
				peakValue = verProjHist[i];
			}
			if (verProjHist[i] < verProjHist[footX]) {
				footX = i;
			}
		}

		/*
		 * for (int i = startX + 1; i <= endX; i++) { if (verProjHist[i] <
		 * peakValue && verProjHist[i] > verProjHist[peakX]) { peakX = i; } }
		 */

		ArrayList<Integer> gapDist = new ArrayList<Integer>();

		startX = footX;
		endX = peakX;
		if (peakX < footX) {
			startX = peakX;
			endX = footX;
		}

		int startY = height / 4;
		int endY = height / 2;

		ImageProcessor ip = imageBin.getProcessor();

		for (int j = startY; j <= endY; j++) {
			for (int i = startX + 1; i <= endX - 1; i++) {
				boolean find = false;
				int k = i + 1;
				while (!find && k <= endX - 1) {
					if (ip.getPixel(i, j) != ip.getPixel(k, j)) {
						find = true;
						gapDist.add(k - i);
						i = k;
					} else {
						k++;
					}
				}
			}
		}

		double averageDist = 0.0;
		if (gapDist.size() > 0) {
			for (Integer dist : gapDist) {
				averageDist += dist;
				// System.out.println(dist);
			}
			// System.out.println("size: "+gapDist.size());
			averageDist = averageDist * 1.0 / gapDist.size();
		}

		// System.out.println(averageDist);

		return averageDist;
	}

	/**
	 * 
	 * @param imageOriginal
	 *            color image
	 * @param maxVerBlockDist
	 * @param minVerBlockNbPixels
	 * @param minBlockWidth
	 * @param maxHorBlockDist
	 * @param minHorBlockNbPixels
	 * @param minBlockHeight
	 * @param border
	 * @param showBlock
	 * @return
	 */
	public ArrayList<Block> getBlocks(ImagePlus imageOriginal,
			int maxVerBlockDist, int minVerBlockNbPixels, int minBlockWidth,
			int maxHorBlockDist, int minHorBlockNbPixels, int minBlockHeight,
			int border, boolean showBlock, boolean showVerticalProjectionHist,
			boolean showHorizontalProjectionHist) {
		ArrayList<Block> blockList = new ArrayList<Block>();

		ImagePlus imageBin = imageOriginal;
		int width = imageBin.getWidth();
		int height = imageBin.getHeight();

		// TODO: check is image is binary, otherwise do binarization
		if (DIA.isColorImage(imageBin) || DIA.isGrayImage(imageBin)) {
			OtsuBinary ob = new OtsuBinary();
			imageBin = ob.binarization(imageOriginal);
		}

		// get average background pixel value
		int backgroundPixel = this.getBackgroundPixelColor(imageOriginal,
				imageBin);

		// create vertical projection profile
		Projection verProj = new Projection(imageBin);
		int[] verProjHist = verProj.getVerticalProjection(0, width, 1,
				showVerticalProjectionHist);

		// Run-Length smoothing algorithm
		verProjHist = DIA.RLSA(verProjHist, maxHorBlockDist,
				minVerBlockNbPixels, minVerBlockNbPixels + 1);
		// verProjHist = DIA.RLSA(verProjHist, Projection.VER_BLOCK_DIST,
		// Projection.MIN_VER_NO_WHITE_BLOCK_NB_PIXEL - 1,
		// Projection.MIN_VER_NO_WHITE_BLOCK_NB_PIXEL + 1);

		// get vertical blocks
		ArrayList<Block> verticalBlocks = verProj.getVerticalNoWhiteBlock(
				verProjHist, minVerBlockNbPixels, minBlockWidth);
		// ArrayList<Block> verticalBlocks = verProj.getVerticalNoWhiteBlock(
		// verProjHist, Projection.MIN_VER_NO_WHITE_BLOCK_NB_PIXEL,
		// Projection.MIN_NO_WHITE_BLOCK_WIDTH);

		// TODO: how about others kind of documents
		// inverse the blocks, because in Chinese document people read from
		// right to left
		ArrayList<Block> newVerticalBlocks = new ArrayList<Block>();
		for (int i = verticalBlocks.size() - 1; i >= 0; i--) {
			newVerticalBlocks.add(verticalBlocks.get(i));
		}
		verticalBlocks = newVerticalBlocks;

		for (Block verBlock : verticalBlocks) {
			// get vertical block
			ImagePlus verImp = DIA.cropImage(imageBin, verBlock.x, 0,
					verBlock.width, height);

			// horizontal projection
			Projection horProj = new Projection(verImp);
			int[] horProjHist = horProj.getHorizontalProjection(0,
					verImp.getHeight(), 1, showHorizontalProjectionHist);

			// Run-Length smoothing algorithm
			horProjHist = DIA.RLSA(horProjHist, maxVerBlockDist,
					minHorBlockNbPixels, minHorBlockNbPixels + 1);
			// horProjHist = DIA.RLSA(horProjHist, Projection.HOR_BLOCK_DITS,
			// Projection.MIN_HOR_NO_WHITE_BLOCK_NB_PIXEL - 1,
			// Projection.MIN_HOR_NO_WHITE_BLOCK_NB_PIXEL + 1);

			ArrayList<Block> horizontalBlocks = horProj
					.getHorizontalNoWhiteBlock(horProjHist,
							minHorBlockNbPixels, minBlockHeight);
			// ArrayList<Block> horizontalBlocks = horProj
			// .getHorizontalNoWhiteBlock(horProjHist,
			// Projection.MIN_HOR_NO_WHITE_BLOCK_NB_PIXEL,
			// Projection.MIN_NO_WHITE_BLOCK_HEIGHT);
			for (Block horBlock : horizontalBlocks) {

				// ImagePlus wordImp = DIA.imageCrop(verImp, 0, horBlock.y,
				// verImp.getWidth(), horBlock.height);

				//ImagePlus blockImp = DIA.cropImage(imageOriginal, verBlock.x,
				//		horBlock.y, verImp.getWidth(), horBlock.height);
				
				ImagePlus blockImp = DIA.cropImage(imageOriginal, verBlock.x - border,
						horBlock.y - border, verImp.getWidth() + 2*border, horBlock.height + 2*border);

				// post-processing
				blockImp = removeWhiteBorder(blockImp);
				//blockImp = DIA.addBorder(blockImp, border, backgroundPixel);
				if (showBlock) {
					blockImp.show();
				}

				Block block = new Block();
				block.x = verBlock.x - border;
				block.height = horBlock.height + 2*border;
				block.y = horBlock.y - border;
				block.width = verBlock.width + 2*border;

				blockList.add(block);
			}

		}

		return blockList;
	}

	/**
	 * 
	 * @param originalImage binary image
	 * @return
	 */
	public static ImagePlus removeWhiteBorder(ImagePlus originalImage) {
		ImagePlus newImage = originalImage;
		
		//TODO: check if input image is binary, otherwise do binarization
		OtsuBinary ob = new OtsuBinary();
		newImage = ob.binarization(newImage);

		// remove border vertical white space
		Projection verticalProjection = new Projection(newImage);
		int[] verHist = verticalProjection.getVerticalProjection(0,
				newImage.getWidth(), 1, false);
		int leftPosX = 0;
		boolean find = false;
		while (!find && leftPosX < verHist.length) {
			if (verHist[leftPosX] > 0) {
				find = true;
			} else {
				leftPosX++;
			}
		}
		int rightPosX = verHist.length - 1;
		find = false;
		while (!find && rightPosX >= 0) {
			if (verHist[rightPosX] > 0) {
				find = true;
			} else {
				rightPosX--;
			}
		}

		Projection horizontalProjection = new Projection(newImage);
		int[] horHist = horizontalProjection.getHorizontalProjection(0,
				newImage.getHeight(), 1, false);
		int upPosY = 0;
		find = false;
		while (!find && upPosY < horHist.length) {
			if (horHist[upPosY] > 0) {
				find = true;
			} else {
				upPosY++;
			}
		}
		int downPosY = horHist.length - 1;
		find = false;
		while (!find && downPosY >= 0) {
			if (horHist[downPosY] > 0) {
				find = true;
			} else {
				downPosY--;
			}
		}

		if (leftPosX < rightPosX && upPosY < downPosY) {
			newImage = DIA.cropImage(originalImage, leftPosX, upPosY, rightPosX
					- leftPosX, downPosY - upPosY);
		}

		return newImage;
	}

	public static double getAverageWordInternHorizontalDistance(
			String imageDirPath) {
		double averageDist = 0.0;
		int nbImage = 0;

		ArrayList<File> imageFiles = DIA.getImageFile(imageDirPath);

		for (File file : imageFiles) {
			try {
				double dist = BlockRetriever
						.getAverageHorizontalInterneDist(new ImagePlus(file
								.getCanonicalPath()));
				if (dist > 0) {
					nbImage++;
					averageDist += dist;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (nbImage > 0) {
			averageDist = averageDist * 1.0 / nbImage;
		}

		return averageDist;
	}

	public static double getAverageWordInternVerticalDistance(
			String imageDirPath) {
		double averageDist = 0.0;
		int nbImage = 0;

		ArrayList<File> imageFiles = DIA.getImageFile(imageDirPath);

		for (File file : imageFiles) {
			try {
				double dist = BlockRetriever
						.getAverageVerticalInterneDist(new ImagePlus(file
								.getCanonicalPath()));
				if (dist > 0) {
					nbImage++;
					averageDist += dist;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (nbImage > 0) {
			averageDist = averageDist * 1.0 / nbImage;
		}

		return averageDist;
	}

	/**
	 * @param args
	 */
	
	public static ArrayList<Block> start(BufferedImage img) {
		// String imagePath = "imgs/csg562-023.png";
		String imagePath = "/home/hao/workspace/DIVADIAWeb2/DIVADIAGTWeb/WorkData/originalImages/csg562-006.png";

		// double averageDist = BlockRetriever
		// .getAverageWordInternVerticalDistance(imageDirPath);
		// System.out.println(averageDist);
		// averageDist = BlockRetriever
		// .getAverageWordInternHorizontalDistance(imageDirPath);
		// System.out.println(averageDist);

//		ImagePlus imageOriginal = new ImagePlus("", img) ;
		ImagePlus imageOriginal = new ImagePlus(imagePath) ;
		OtsuBinary ob = new OtsuBinary();
		ImagePlus imageBin = ob.binarization(imagePath);
//		ImagePlus imageBin = ob.binarization(imageOriginal);
		ImageProcessor ip = imageBin.getProcessor();
		imageBin.show();

		// System.out.println(ip.getPixel(10, 10));

		BlockRetriever wr = new BlockRetriever();

		// ImagePlus imageOriginal,
		// int maxVerBlockDist, int minVerBlockNbPixels, int minBlockWidth,
		// int maxHorBlockDist, int minHorBlockNbPixels, int minBlockHeight,
		// int border, boolean showBlock, boolean showVerticalProjectionHist,
		// boolean showHorizontalProjectionHist

		ArrayList<Block> blockList = wr.getBlocks(imageOriginal, 50, 600, 200,
				50, 100, 500, 50, true, false, false);
		return blockList;
	}
	public static void main(String[] args) {
		start(null);
	}
}
