package CargoBot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.session.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private String username;
    private String commentary;
    private String dateAdded;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Номер груза: ").append(this.id).append("\n")
                .append("Вес груза: ").append(this.weight).append(" т.").append("\n")
                .append("Размер груза: ").append(this.size).append("м³").append("\n")
                .append("Цена груза: ").append(this.price).append(" руб").append("\n")
                .append("Куда: ").append(this.whereTo).append("\n")
                .append("Откуда: ").append(this.whereFrom).append("\n")
                .append("Добавил: ").append(this.username).append("\n")
                .append("Дата добавления: ").append(this.dateAdded).append("\n")
                .append("Комментарий: ").append(this.commentary).append("\n")
                .append("Контакты администрации: +79375845056, @samara_121");
        return stringBuilder.toString();
    }

    public Cargo(Session session) {
        this.price = session.getAttribute("price").toString();
        this.weight = session.getAttribute("weight").toString();
        this.size = session.getAttribute("size").toString();
        this.whereFrom = (String) session.getAttribute("whereFrom");
        this.whereTo = (String) session.getAttribute("whereTo");
        this.username = session.getHost();
        this.dateAdded = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        this.commentary = (String) session.getAttribute("commentary");
    }

    public String toPushSQL() {
        return String.format(Locale.ROOT,
                "INSERT INTO cargolist (" +
                        "wherefrom, whereto, price, weight, size, username, commentary, dateadded) " +
                        "VALUES ('%s', '%s', %.0f, %4.1f, %4.1f, '%s', '%s', '%s')",
                this.whereFrom,
                this.whereTo,
                Double.parseDouble(this.price),
                Double.parseDouble(this.weight),
                Double.parseDouble(this.size),
                this.username,
                this.commentary,
                this.dateAdded);
    }
}
