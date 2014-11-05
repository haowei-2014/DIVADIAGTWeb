/**
 * 
 */
package ch.unifr.textblock;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * 
 * @author ck
 * 
 */
public class Projection {
	// input a binary image!
	// TODO: check image is binary, other binarize it.
	public ImagePlus image;

	// public static int FORE_PIXEL = 0;
	// public static int BACK_PIXEL = 255;

	// threshold
	// public final static int MIN_VER_NO_WHITE_BLOCK_NB_PIXEL = 100;

	// public final static int MIN_NO_WHITE_BLOCK_WIDTH = 100;

	// public final static int MIN_HOR_NO_WHITE_BLOCK_NB_PIXEL = 5;

	// public final static int MIN_NO_WHITE_BLOCK_HEIGHT = 50;

	public final static int HOR_BORDER_WIDTH = 0;
	public final static int HOR_BORDER_HEIGHT = 0;

	// public final static int MAX_VER_BLOCK_DIST = 5;

	// public final static int MAX_HOR_BLOCK_DIST = 8;

	public Projection(String imagePath) {
		this.image = new ImagePlus(imagePath);
	}

	public Projection(ImagePlus image) {
		this.image = image;
	}

	/**
	 * get blocks by counting #black pixels in each lines horizontally.
	 * 
	 * @param hist
	 *            horizontal histogram of black pixels
	 * @param minHorBlockNbPixels
	 *            minimal black pixels that a block should contains in each of
	 *            lines
	 * @param minBlockHeight
	 *            minimal block height
	 * @return
	 */
	public ArrayList<Block> getHorizontalNoWhiteBlock(int[] hist,
			int minHorBlockNbPixels, int minBlockHeight) {
		ArrayList<Block> blocks = new ArrayList<Block>();

		if (this.image == null)
			return blocks;

		int height = this.image.getHeight();

		int start = HOR_BORDER_HEIGHT;

		while (start < height - 1) {
			int len = 0;
			int currentPos = start;

			while (currentPos < height - 1
					&& hist[currentPos] >= minHorBlockNbPixels) {
				// while (currentPos < height - 1
				// && hist[currentPos] >= MIN_HOR_NO_WHITE_BLOCK_NB_PIXEL) {
				len++;
				currentPos++;
			}

			if (len >= minBlockHeight) {
				// if (len >= MIN_NO_WHITE_BLOCK_HEIGHT) {
				if (len + HOR_BORDER_HEIGHT <= height - 1) {
					len += HOR_BORDER_HEIGHT;
				}
				Block block = new Block();
				block.y = start;
				block.height = len;
				start += len;
				blocks.add(block);
			} else {
				start += 1;
			}
		}

		return blocks;
	}

	/**
	 * get blocks by counting #black pixels in each lines vertically.
	 * 
	 * @param hist
	 *            vertical black pixels histogram
	 * @param minVerBlockNbPixels
	 *            minimal black pixels that a block should contains in each
	 *            column.
	 * @param minBlockWidth
	 *            minimal block width
	 * @return
	 */
	public ArrayList<Block> getVerticalNoWhiteBlock(int[] hist,
			int minVerBlockNbPixels, int minBlockWidth) {
		ArrayList<Block> blocks = new ArrayList<Block>();

		if (this.image == null)
			return blocks;

		int width = this.image.getWidth();

		int start = 0;

		while (start < width - 1) {
			int len = 0;
			int currentPos = start;

			while (currentPos < width - 1
					&& hist[currentPos] >= minVerBlockNbPixels) {
				// while (currentPos < width - 1
				// && hist[currentPos] >= MIN_VER_NO_WHITE_BLOCK_NB_PIXEL) {
				len++;
				currentPos++;
			}

			if (len >= minBlockWidth) {
				// if (len >= MIN_NO_WHITE_BLOCK_WIDTH) {
				Block block = new Block();
				block.x = start;
				block.width = len;
				start += len;
				blocks.add(block);
			} else {
				start += 1;
			}
		}

		return blocks;
	}

	/**
	 * create horizontal projection histogram array
	 * 
	 * @param start
	 *            image starting position
	 * @param end
	 *            image end position
	 * @param interval
	 *            interval of pixels
	 * @param showHistogram
	 *            display the histogram or not
	 * @return
	 */
	public int[] getHorizontalProjection(int start, int end, int interval,
			boolean showHistogram) {
		if (this.image == null || interval <= 0 || start < 0 || end <= 0
				|| start >= end)
			return null;

		ImageProcessor ip = this.image.getProcessor();
		int width = this.image.getWidth();
		int height = this.image.getHeight();

		int[] hist = new int[height];

		int numberForePixels = 0;
		for (int i = start; i < end && i < height; i += interval) {
			for (int j = 0; j < width; j++) {
				if (ip.getPixel(j, i) == OtsuBinary.FORE_PIXEL) {
					hist[i] += 1;
					numberForePixels++;
				}
			}
		}

		// TODO: show histogram does not work.
		int[] hitsCopy = Arrays.copyOf(hist, hist.length);
		Arrays.sort(hitsCopy);
		int maxFrequence = hitsCopy[hitsCopy.length - 1];

		if (showHistogram) {
			double[] horHistDataset = new double[numberForePixels];
			int index = 0;
			for (int i = start; i < end; i += interval) {
				for (int j = 0; j < width; j++) {
					if (ip.getPixel(j, i) == OtsuBinary.FORE_PIXEL) {
						horHistDataset[index++] = (double) i;
					}
				}
			}

//			 DIA.showFrequenceHistogram(horHistDataset, maxFrequence,
//			 "image horizontal projection", "height", "run", PlotOrientation.HORIZONTAL, 800, 600);
		}

		return hist;
	}

	/**
	 * create vertical projection histogram array
	 * 
	 * @param start
	 *            image starting position
	 * @param end
	 *            image end position
	 * @param interval
	 *            interval of pixels
	 * @param showHistogram
	 *            display the histogram or not
	 * @return
	 */
	public int[] getVerticalProjection(int start, int end, int interval,
			boolean showHistogram) {
		if (this.image == null || interval <= 0 || start < 0 || end <= 0
				|| start >= end)
			return null;

		ImageProcessor ip = this.image.getProcessor();

		int numberForePixels = 0;
		// int len = (int) Math.round((end - start + 1) * 1.0 / interval);
		int[] hist = new int[this.image.getWidth()];

		for (int i = start; i < end && i < this.image.getWidth(); i += interval) {
			for (int j = 0; j < this.image.getHeight(); j++) {
				if (ip.getPixel(i, j) == OtsuBinary.FORE_PIXEL) {
					hist[i] += 1;
					numberForePixels++;
				}
			}
		}

		//TODO: show histogram 
		int[] hitsCopy = Arrays.copyOf(hist, hist.length);
		Arrays.sort(hitsCopy);
		int maxFrequence = hitsCopy[hitsCopy.length - 1];

		if (showHistogram) {
			double[] verHistDataset = new double[numberForePixels];
			int index = 0;
			for (int i = start; i < end; i += interval) {
				for (int j = 0; j < this.image.getHeight(); j++) {
					if (ip.getPixel(i, j) == OtsuBinary.FORE_PIXEL) {
						verHistDataset[index++] = (double) i;
					}
				}
			}

			// DIA.showFrequenceHistogram(verHistDataset, maxFrequence,
			// "image vertical projection", "width", "run",
			// PlotOrientation.VERTICAL, 800, 600);
		}

		return hist;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
