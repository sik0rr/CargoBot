package CargoBot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.session.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Cargo {

    private int id;

    private String price;
    private String weight;
    private String size;

    private String whereFrom;
    private String whereTo;
    private String commentary;
    private String dateAdded;
    private String username;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Номер груза: ").append(this.id).append("\n")
                .append("Откуда: ").append(this.whereFrom).append("\n")
                .append("Куда: ").append(this.whereTo).append("\n")
                .append("Вес груза: ").append(this.weight).append(" т.").append("\n")
                .append("Объем груза: ").append(this.size).append("м³").append("\n")
                .append("Цена груза: ").append(this.price).append(" руб").append("\n")
                .append("Дата добавления: ").append(this.dateAdded).append("\n")
                .append("Добавил: ").append(this.username).append("\n")
                .append("Комментарий: ").append(this.commentary).append("\n")
                .append("Контакты администрации: +79375845056, @samara_121");
        return stringBuilder.toString();
    }

    public Cargo(Session session, String username) {
        Map<String, String> cargoData = parseMessage(session);
        this.username = username;
        this.price = cargoData.get("Цена");
        this.weight = cargoData.get("Вес");
        this.size = cargoData.get("Объем");
        this.whereFrom = cargoData.get("Адрес отправления");
        this.whereTo = cargoData.get("Адрес доставки");
        this.dateAdded = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        this.commentary = cargoData.get("Комментарий");
    }

    public String toPushSQL() {
        return String.format(Locale.ROOT,
                "INSERT INTO cargolist (" +
                        "wherefrom, whereto, price, weight, size, commentary, dateadded, username) " +
                        "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                this.whereFrom,
                this.whereTo,
                this.price,
                this.weight,
                this.size,
                this.commentary,
                this.dateAdded,
                this.username);
    }

    public String getExactCargo() {
        return String.format(Locale.ROOT,
                "SELECT * FROM cargolist WHERE (" +
                        "wherefrom, whereto, price, weight, size, commentary, dateadded, username) " +
                        "= ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                this.whereFrom,
                this.whereTo,
                this.price,
                this.weight,
                this.size,
                this.commentary,
                this.dateAdded,
                this.username);
    }

    private Map<String, String> parseMessage(Session session) {
        String rawCargoData = (String) session.getAttribute("cargoData");
        Map<String, String> cargoData = new HashMap<>();
        for (String line : rawCargoData.split("\n")) {
            String[] splitLine = line.split(":");
            cargoData.put(splitLine[0].strip(), splitLine[1].strip());
        }
        return cargoData;
    }
}
