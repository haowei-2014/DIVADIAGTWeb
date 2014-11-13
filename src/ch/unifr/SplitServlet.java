package ch.unifr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.unifr.modification.SplitPolygon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.bcel.internal.generic.Type;

/**
 * Servlet implementation class SplitServlet
 */
@WebServlet("/SplitServlet")
public class SplitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SplitServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SplitServlet now, Do post");
		int xSplit = Integer.parseInt(request.getParameter("xSplit"));
		int ySplit = Integer.parseInt(request.getParameter("ySplit"));
		
		System.out.println("xSplit is: " + xSplit);
		System.out.println("ySplit is: " + ySplit);

		String splitPolygon = request.getParameter("splitPolygon");
//		System.out.println(employees);		
		Gson gson = new Gson();
		MyPoint[] points = gson.fromJson(splitPolygon, MyPoint[].class);

		for (int i = 0; i < points.length; i++){
			System.out.print(points[i].x + "  ");	
			System.out.println(points[i].y);	
		}		
		HashMap<String, List<int[][]>> resultsSplit = SplitPolygon.getResults(points, xSplit, ySplit);
	    System.out.println("Text lines extraction is done.");
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(new Gson().toJson(resultsSplit));
	}
	
	public class MyPoint{
	   public int x;
	   public int y;
	}
}
