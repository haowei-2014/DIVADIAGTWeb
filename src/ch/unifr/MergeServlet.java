package ch.unifr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.unifr.SplitServlet.MyPoint;
import ch.unifr.modification.MergePolygons;
import ch.unifr.modification.SplitPolygon;

import com.google.gson.Gson;

/**
 * Servlet implementation class MergeServlet
 */
@WebServlet("/MergeServlet")
public class MergeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MergeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("MergeServlet, do get");
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("MergeServlet now, Do post");

		String mergePolygon1 = request.getParameter("mergePolygon1");
		String mergePolygon2 = request.getParameter("mergePolygon2");
//		System.out.println(employees);		
		Gson gson = new Gson();
		MyPoint[] myPoints1 = gson.fromJson(mergePolygon1, MyPoint[].class);
		MyPoint[] myPoints2 = gson.fromJson(mergePolygon2, MyPoint[].class);

		System.out.println("mergePolygon1");
		for (int i = 0; i < myPoints1.length; i++){
			System.out.print(myPoints1[i].x + "  ");	
			System.out.println(myPoints1[i].y);	
		}	
		System.out.println("mergePolygon2");
		for (int i = 0; i < myPoints2.length; i++){
			System.out.print(myPoints2[i].x + "  ");	
			System.out.println(myPoints2[i].y);	
		}	
		
		
		HashMap<String, List<int[][]>> resultsMerge = MergePolygons.getResults(myPoints1, myPoints2);
	    System.out.println("Text lines extraction is done.");
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(new Gson().toJson(resultsMerge));
	}

}
