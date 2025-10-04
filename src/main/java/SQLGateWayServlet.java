import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.*;

@WebServlet("/sqlGateway")
public class SQLGateWayServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String sqlStatement = request.getParameter("sqlStatement");
        String sqlResult = "";

        try {
            // Lấy DataSource từ context.xml qua JNDI
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Project");

            try (Connection connection = ds.getConnection();
                 Statement statement = connection.createStatement()) {

                // parse SQL string
                sqlStatement = sqlStatement.trim();
                if (sqlStatement.length() >= 6) {
                    String sqlType = sqlStatement.substring(0, 6);

                    if (sqlType.equalsIgnoreCase("select")) {
                        try (ResultSet resultSet = statement.executeQuery(sqlStatement)) {
                            sqlResult = SQLUtil.getHtmlTable(resultSet);
                        }
                    } else {
                        int i = statement.executeUpdate(sqlStatement);
                        if (i == 0) { // DDL
                            sqlResult = "<p>The statement executed successfully.</p>";
                        } else { // INSERT, UPDATE, DELETE
                            sqlResult = "<p>The statement executed successfully.<br>"
                                    + i + " row(s) affected.</p>";
                        }
                    }
                }
            }

        } catch (NamingException e) {
            sqlResult = "<p>Error looking up DataSource: <br>" + e.getMessage() + "</p>";
        } catch (SQLException e) {
            sqlResult = "<p>Error executing the SQL statement: <br>" + e.getMessage() + "</p>";
        }

        HttpSession session = request.getSession();
        session.setAttribute("sqlResult", sqlResult);
        session.setAttribute("sqlStatement", sqlStatement);

        String url = "/index.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}