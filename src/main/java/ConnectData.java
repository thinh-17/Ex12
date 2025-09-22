import java.sql.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ConnectData {

    private static ConnectData pool = null;
    private static DataSource dataSource = null;

    private ConnectData() {
        try {
            InitialContext ic = new InitialContext();
            dataSource = (DataSource)
                    ic.lookup("java:/comp/env/jdbc/Project");
        } catch (NamingException e) {
            System.out.println(e);
        }
    }
    public static synchronized ConnectData getInstance() {
        if (pool == null) {
            pool = new ConnectData();
        }
        return pool;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }

    public void freeConnection(Connection c) {
        try {
            c.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
