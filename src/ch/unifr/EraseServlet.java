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

import ch.unifr.SplitServlet.MyPoint;
import ch.unifr.modification.ErasePolygon;
import ch.unifr.modification.SplitPolygon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.bcel.internal.generic.Type;

/**
 * Servlet implementation class SplitServlet
 */
@WebServlet("/EraseServlet")
public class EraseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EraseServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("EraseServlet, do get");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("EraseServlet now, Do post");
		int xErase = Integer.parseInt(request.getParameter("xErase"));
		int yErase = Integer.parseInt(request.getParameter("yErase"));
		
		System.out.println("xErase is: " + xErase);
		System.out.println("yErase is: " + yErase);

		String erasePolygon = request.getParameter("erasePolygon");	
		Gson gson = new Gson();
		MyPoint[] points = gson.fromJson(erasePolygon, MyPoint[].class);
	
		HashMap<String, List<int[][]>> resultsSplit = ErasePolygon.getResults(points, xErase, yErase);
	    System.out.println("Text lines erase is done.");
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(new Gson().toJson(resultsSplit));
	}
}
