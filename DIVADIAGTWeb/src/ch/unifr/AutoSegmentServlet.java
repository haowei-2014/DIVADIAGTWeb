package ch.unifr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import com.google.gson.Gson;

/**
 * Servlet implementation class AutoSegmentServlet
 */
@WebServlet("/AutoSegmentServlet")
public class AutoSegmentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AutoSegmentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Here");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		HashMap<String, List<int[][]>> results = new HashMap<String, List<int[][]>>();	
		String imageURL = request.getParameter("imageURL");
		String imageName = request.getParameter("imageName");
		if (imageURL == null){
			System.out.println("imageURL is null.");
		}
		System.out.println("imageName: " + imageName);
		System.out.println("DoPost is executed.");
		
		// text blocks extraction using projection method
		Step1Projection projectMethod = new Step1Projection(imageURL);
		HashMap<String, List<int[][]>> resultsStep1 = projectMethod.getResults();
	    System.out.println("Text blocks extraction is done.");
	    
	    // text lines extraction using Gabor filters
	    TextLinesExtraction textlinesExtraction = new TextLinesExtraction();
		try {
			textlinesExtraction.textLinesExtraction();
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    Step2Gabor step2Gabor = new Step2Gabor();
	    HashMap<String, List<int[][]>> resultsStep2 = step2Gabor.getResults();
	    results.putAll(resultsStep1);
	    results.putAll(resultsStep2);
	    String json = new Gson().toJson(results);
	    System.out.println("Text lines extraction is done.");
	    
	  
	    MySQLConnection mySQLConnection = new MySQLConnection();
		try {
			mySQLConnection.insert(imageName, imageURL);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	 
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json);
	}

}
