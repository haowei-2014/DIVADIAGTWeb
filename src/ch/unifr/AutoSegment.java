package ch.unifr;

import java.util.HashMap;
import java.util.List;

/**
 * @author hao
 * 
 * This class incorporates Step1Projection and Step2Gabor. It is the whole process.
 *
 */
public class AutoSegment {
	
	

	public static HashMap<String, List<int[][]>> start(Info info) {
		HashMap<String, List<int[][]>> result;

		// text blocks extraction using projection method
		Step1Projection step1Projection = new Step1Projection();
		step1Projection.initImage(info);
		step1Projection.cropTextBlock(info.top, info.bottom, info.left,
				info.right);

		System.out.println("Text blocks extraction is done.");
		Step2Gabor step2Gabor = new Step2Gabor();
		result = step2Gabor.getResults(
				info.left, info.top, info.linkingRectWidth, info.linkingRectHeight, info);

		return result;
	}

	public static void main(String[] args) {

	}

}
