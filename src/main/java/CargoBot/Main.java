package CargoBot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            ConfigParser parser = new ConfigParser("src/main/resources/config.json");
            Map<String, String> config = parser.getConfig();
            Connection conn = DriverManager.getConnection(config.get("dbUrl"), config.get("dbUser"), config.get("dbPassword"));
            Class.forName("org.postgresql.Driver");
            DataBase db = new DataBase(conn);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CargoBot(db, config));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
