package CargoBot;


import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {
    public static Map<String, Integer> userGet() {
        Map<String, Integer> temporary = new HashMap<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/cargodb", "root", "sikora2001");
            PreparedStatement ps = conn.prepareStatement("SELECT username, `group` FROM userlist");
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                Integer group = resultSet.getInt("group");
                temporary.put(username, group);
            }
            conn.close();

        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
        return temporary;
    }
    public static void push(String statement){
        System.out.println(statement);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/cargodb", "root", "sikora2001");
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.execute();
            conn.close();
        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
   // static List<String> searchResult = new ArrayList<>();
    public static void get(String statement){
        try {
            List<String> searchResult = new ArrayList<>();
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/cargodb", "root", "sikora2001");
            PreparedStatement ps = conn.prepareStatement(statement);
            ResultSet rs = ps.executeQuery();
            System.out.println(rs);
            while (rs.next()){
                int nomer = rs.getInt("id");
                String otkuda = rs.getString("whereFrom");
                String kuda = rs.getString("whereTo");
                double tsena = rs.getDouble("price");
                double ves = rs.getDouble("weight");
                double objem = rs.getDouble("size");
                String telefon = rs.getString("phone");
                String comment = rs.getString("commentary");
                String data = rs.getDate("addDate").toString();
                String result = "\nНомер груза:" +nomer+"\nОткуда: " + otkuda +"\nКуда: " +kuda+"\nЦена: "+tsena+"руб\nВес: "+ves+" т\nОбъём: "+objem+"м³\nДата добавления: "+ data +
                        "\nКомментарий: " + comment + "\nТелефон:" +telefon+"\n"+"Телефон администрации: +79375845056";
                searchResult.add(result);
            }
            CargoBot.getSearchResult(searchResult);
            searchResult.clear();
            rs.close();
            conn.close();
        } catch (SQLException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
}
