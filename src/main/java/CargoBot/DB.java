package CargoBot;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {
    public static Map<String, Integer> userGet() {
        Map<String, Integer> temporary = new HashMap<>();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/cargodb", "postgres", "sikora2001");
            PreparedStatement ps = conn.prepareStatement("SELECT username, \"group\" FROM userlist");
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                Integer group = resultSet.getInt("group");
                temporary.put(username, group);
            }
            conn.close();

        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return temporary;
    }

    public static void push(String statement) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/cargodb", "postgres", "sikora2001");
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.execute();
            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }

    public static List<Cargo> get(String statement) {
        List<Cargo> searchResult = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/cargodb", "postgres", "sikora2001");
            PreparedStatement ps = conn.prepareStatement(statement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cargo cargo = new Cargo();
                cargo.setId(rs.getInt("id"));
                cargo.setWhereFrom(rs.getString("wherefrom"));
                cargo.setWhereTo(rs.getString("whereto"));
                cargo.setPrice(String.valueOf(rs.getDouble("price")));
                cargo.setWeight(String.valueOf(rs.getDouble("weight")));
                cargo.setSize(String.valueOf(rs.getDouble("size")));
                cargo.setCommentary(rs.getString("commentary"));
                cargo.setDateAdded(rs.getDate("dateadded").toString());
                searchResult.add(cargo);
                System.out.println(cargo.toString());
            }
            rs.close();
            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return searchResult;
    }
}
