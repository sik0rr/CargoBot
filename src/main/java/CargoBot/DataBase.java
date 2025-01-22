package CargoBot;

import java.sql.*;
import java.util.*;

public class DataBase {

    private final Connection conn;

    public DataBase(Connection conn) {
        this.conn = conn;
    }

    public Map<String, Integer> userGet() {
        Map<String, Integer> temporary = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT username, accesslevel FROM userlist");) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                Integer group = resultSet.getInt("accesslevel");
                temporary.put(username, group);
            }
        } catch (SQLException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return temporary;
    }

    public void push(String statement) {
        try (PreparedStatement ps = conn.prepareStatement(statement);) {
            ps.execute();
        } catch (SQLException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }

    public String getUserRating(String username) {
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(String.format(Locale.ROOT,
                "SELECT username, rating FROM userrating WHERE username='%s'", username))) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sb.append("Пользователь: ").append(rs.getString("username")).append("\n")
                        .append("Рейтинг: ").append(rs.getInt("rating")).append("\n");
                return sb.toString();
            }
        } catch (SQLException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return sb.toString();
    }

    public List<Cargo> getCargo(String statement) {
        List<Cargo> searchResult = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(statement);) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cargo cargo = new Cargo();
                cargo.setId(rs.getInt("id"));
                cargo.setWhereFrom(rs.getString("wherefrom"));
                cargo.setWhereTo(rs.getString("whereto"));
                cargo.setPrice(rs.getString("price"));
                cargo.setWeight(rs.getString("weight"));
                cargo.setSize(rs.getString("size"));
                cargo.setCommentary(rs.getString("commentary"));
                cargo.setDateAdded(rs.getDate("dateadded").toString());
                cargo.setUsername(rs.getString("username"));
                searchResult.add(cargo);
                System.out.println(cargo);
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return searchResult;
    }
}
