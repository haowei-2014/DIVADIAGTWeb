package ch.unifr;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import ch.unifr.gabor.CommonFunctions;

import com.google.gson.Gson;

/**
 * Servlet implementation class AutoSegmentServlet
 */
@WebServlet("/AutoSegmentServlet")
public class AutoSegmentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static String oldImageName = ""; 
       
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

		// read parameters
		System.out.println("Do post");
		String imageURL = request.getParameter("imageURL");
		String imageName = request.getParameter("imageName");		
		int top = Integer.parseInt(request.getParameter("top"));
		int bottom = Integer.parseInt(request.getParameter("bottom"));
		int left = Integer.parseInt(request.getParameter("left"));
		int right = Integer.parseInt(request.getParameter("right"));
		int linkingRectWidth = Integer.parseInt(request.getParameter("linkingRectWidth"));
		int linkingRectHeight = Integer.parseInt(request.getParameter("linkingRectHeight"));
		
		Info info = new Info(imageURL, imageName, top, bottom, left, right, linkingRectWidth, linkingRectHeight);
				
		if (imageURL == null){
			System.out.println("imageURL is null.");
		}
		System.out.println("imageName: " + imageName + ", left: " + left + ", top: " + top);
		System.out.println("DoPost is executed.");
		// delete old data
		if (oldImageName.equals(info.imageName) == false){
			File folderDelete = new File (info.filePath);
			CommonFunctions.deleteFolder(folderDelete);
			oldImageName = info.imageName;
		}
		HashMap<String, List<int[][]>> result = AutoSegment.start(info);
	    System.out.println("Text lines extraction is done.");
	    	  
	    /*MySQLConnection mySQLConnection = new MySQLConnection();
		try {
			mySQLConnection.insert(imageName, imageURL);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		 
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(new Gson().toJson(result));
	}

}
