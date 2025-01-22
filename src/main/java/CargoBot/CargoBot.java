package CargoBot;

import org.apache.shiro.session.Session;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CargoBot extends TelegramLongPollingSessionBot {

    private final DataBase dataBase;
    private final String TOKEN = System.getenv("botToken");
    private final String BOTUSERNAME = System.getenv("botUsername");

    private final ReplyKeyboardMarkup mainKeyboard = createKeyboard("Добавить груз,Искать груз;Управление доступом,Удалить груз;Мой рейтинг");
    private final ReplyKeyboardMarkup returnKeyboard = createKeyboard("На главную");
    private final ReplyKeyboardMarkup accessManageKeyboard = createKeyboard("Добавить пользователя,Удалить пользователя;Список пользователей,Обновить рейтинг;На главную");
    private final ReplyKeyboardMarkup searchKeyboard = createKeyboard("Показать все заявки;Искать по городу отправления,Искать по городу назначения;Искать по имени пользователя,Искать по дате; На главную");

    public CargoBot(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public void onUpdateReceived(Update update, Optional<Session> optional) {
        Map<String, Integer> userList = dataBase.userGet();
        Message message = update.getMessage();
        Session session = optional.get();
        session.setTimeout(600000);
        if (message != null && message.hasText() && session.getAttribute("statement") != null) {
            ReplyKeyboardMarkup keyboard;
            switch (session.getAttribute("statement").toString()) {
                case "Добавление":
                    session.setAttribute("cargoData", message.getText());
                    System.out.println(message.getText());
                    sendMsg(message,"Информация о грузе записана. Нажмите кнопку \"Добавить\"", createKeyboard("Добавить"));
                    session.removeAttribute("statement");
                    break;
                case "Добавить пользователя":
                    if (!(message.getText().contains("@"))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "Имя пользователя введено неправильно", keyboard);
                        session.removeAttribute("statement");
                        break;
                    }
                    session.setAttribute("userToAdd", message.getText().substring(1));
                    String addUser = String.format(Locale.ROOT, "INSERT INTO userlist (username, accesslevel) VALUES('%s', 2)", session.getAttribute("userToAdd"));
                    dataBase.push(addUser);
                    sendMsg(message, "Пользователь добавлен", returnKeyboard);
                    session.removeAttribute("statement");
                    break;
                case "Удалить пользователя":
                    if (!(message.getText().contains("@"))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "Имя пользователя введено неправильно", keyboard);
                        session.removeAttribute("statement");
                        break;
                    }
                    session.setAttribute("userToDelete", message.getText().substring(1));
                    String sqlDelete = String.format(Locale.ROOT, "DELETE FROM userlist WHERE username = '%s'", session.getAttribute("userToDelete"));
                    dataBase.push(sqlDelete);
                    sendMsg(message, "Пользователь удалён", returnKeyboard);
                    session.removeAttribute("statement");
                    break;
                case "ПоискОткуда":
                    session.setAttribute("searchFrom", message.getText());
                    String whereFromSearch = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE wherefrom ILIKE '%s'", session.getAttribute("searchFrom").toString());
                    System.out.println("Поиск откуда");
                    System.out.println(whereFromSearch);
                    List<String> whereFromSearchResult = getSearchResult(whereFromSearch);
                    if (!whereFromSearchResult.isEmpty() && !(whereFromSearchResult.get(0).isBlank())) {
                        for (String result : whereFromSearchResult) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен. Найдено заявок: " + whereFromSearchResult.size(), returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("searchFrom");
                    session.removeAttribute("statement");
                    break;
                case "ПоискКуда":
                    session.setAttribute("searchTo", message.getText());
                    System.out.println("Поиск куда");
                    List<String> whereToSearchResult = getSearchResult(String.format(Locale.ROOT,
                            "SELECT * FROM cargolist WHERE whereTo ILIKE '%s'",
                            session.getAttribute("searchTo").toString()));
                    if (!whereToSearchResult.isEmpty()) {
                        for (String result : whereToSearchResult) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен, Найдено заявок: " + whereToSearchResult.size(), returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("searchTo");
                    session.removeAttribute("statement");
                    break;
                case "ПоискЮзернейм":
                    session.setAttribute("username", message.getText().substring(1));
                    String searchByUsername = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE username ILIKE '%s'", session.getAttribute("username").toString());
                    dataBase.getCargo(searchByUsername);
                    System.out.println("Поиск юзернейм");
                    List<String> userSearchResult = getSearchResult(searchByUsername);
                    if (!userSearchResult.isEmpty()) {
                        for (String result : userSearchResult) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("username");
                    session.removeAttribute("statement");
                    break;
                case "ПоискДата":
                    String[] dateRange = message.getText().split(":");
                    session.setAttribute("date1", dateRange[0].replace(".", "-"));
                    session.setAttribute("date2", dateRange[1].replace(".", "-"));
                    System.out.println("Поиск даты");
                    String dateSearch = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE dateadded BETWEEN '%s' AND '%s'", session.getAttribute("date1"), session.getAttribute("date2"));
                    dataBase.getCargo(dateSearch);
                    List<String> dateSearchResult = getSearchResult(dateSearch);
                    if (!dateSearchResult.isEmpty()) {
                        for (String result : dateSearchResult) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен. Найдено заявок: " + dateSearchResult.size(), returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("date1");
                    session.removeAttribute("date2");
                    session.removeAttribute("statement");
                    break;
                case "Удаление":
                    session.setAttribute("id", message.getText());
                    String deleteCargo = "DELETE FROM cargolist WHERE id ='" + Integer.parseInt(session.getAttribute("id").toString());
                    deleteCargo = deleteCargo +  "' RETURNING *";
                    System.out.println(deleteCargo);
                    List<Cargo> deletedCargo = dataBase.getCargo(deleteCargo);
                    if (!deletedCargo.isEmpty()) {
                        sendMsg(message, "Груз удалён:\n" + deletedCargo.get(0).toString(), returnKeyboard);
                    } else {
                        sendMsg(message, "Груза с таким номером не существует.", returnKeyboard);
                    }
                    session.removeAttribute("id");
                    session.removeAttribute("statement");
                    break;
                case "Обновить рейтинг":
                    session.setAttribute("updateRating", message.getText());
                    String[] updateRating = session.getAttribute("updateRating").toString().split(",");
                    System.out.println(Arrays.toString(updateRating));
                    if (updateRating[0].isBlank() || updateRating[1].isBlank()) {
                        sendMsg(message, "Информация введена неверно", returnKeyboard);
                        session.removeAttribute("updateRating");
                        session.removeAttribute("statement");
                        break;
                    }
                    dataBase.push(String.format(Locale.ROOT,"UPDATE userrating SET rating = %d WHERE username = '%s'",
                            Integer.parseInt(updateRating[1].strip()), updateRating[0].substring(1)));
                    sendMsg(message, "Рейтинг обновлён:\n" + dataBase.getUserRating(updateRating[0].substring(1)));
                    session.removeAttribute("updateRating");
                    session.removeAttribute("statement");
                    break;
            }
        }

        if (message != null && message.hasText()) {
            switch (message.getText()) {
                case "/start":
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot.", mainKeyboard);
                    break;
                case "Добавить груз":
                    System.out.println("Попытка добавить груз" + getUsername(message));
                    if (!(userList.containsKey(getUsername(message)) && (userList.get(getUsername(message)) >= 2))) {
                        sendMsg(message, "У вас недостаточно прав, чтобы добавить груз. Чтобы получить одобрение, обратитесь к администратору: @samara_121 или по телефону +79375845056", returnKeyboard);
                        break;
                    }
                    session.setAttribute("statement", "Добавление");
                    sendMsg(message, "Скопируйте шаблон из следующего сообщения, " +
                            "введите информацию о грузе и отправьте сообщение \n" +
                            "\nПосле отправки сообщения нажмите кнопку 'Добавить'.");
                    sendMsg(message, "```" +
                            "\nАдрес отправления:" +
                            "\nАдрес доставки:" +
                            "\nВес:" +
                            "\nОбъем:" +
                            "\nЦена:" +
                            "\nКомментарий:```", createKeyboard("Добавить", false), true);
                    break;
                case "Управление доступом":
                    if (!(userList.containsKey(getUsername(message)) && (userList.get(getUsername(message)) > 2))) {
                        sendMsg(message, "У вас недостаточно прав.", returnKeyboard);
                        break;
                    }
                    sendMsg(message, "Выберите действие.", accessManageKeyboard);
                    break;
                case "На главную":
                    session.removeAttribute("statement");
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot. ", mainKeyboard);
                    break;
                case "Добавить пользователя":
                    session.setAttribute("statement", "Добавить пользователя");
                    sendMsg(message, "Введите имя пользователя в формате @username");
                    break;
                case "Удалить пользователя":
                    session.setAttribute("statement", "Удалить пользователя");
                    sendMsg(message, "Введите имя пользователя в формате @username");
                    break;
                case "Список пользователей":
                    StringBuilder sb = new StringBuilder();
                    for (var entry : userList.entrySet()) {
                        sb.append("@").append(entry.getKey()).append(": ");
                        if (entry.getValue() == 2) {
                            sb.append("может добавлять грузы\n");
                        } else if (entry.getValue() == 3) {
                            sb.append("администратор\n");
                        }
                    }
                    sendMsg(message, sb.toString());
                    break;
                case "Добавить":
                    Cargo cargo = new Cargo(session, getUsername(message));
                    dataBase.push(cargo.toPushSQL());
                    sendMsg(message, "Груз добавлен: \n" + dataBase.getCargo(cargo.getExactCargo()).get(0).toString(), returnKeyboard);
                    break;
                case "Искать груз":
                    sendMsg(message, "По какому критерию искать груз?", searchKeyboard);
                    break;
                case "Показать все заявки":
                    List<String> showAll = getSearchResult("select * from cargolist");
                    if (!showAll.isEmpty()) {
                        for (String result : showAll) {
                            sendMsg(message, result);
                        }
                        sendMsg(message,"Поиск завершен. Найдено заявок: " + showAll.size(), returnKeyboard);
                    } else {
                        sendMsg(message, "На данный момент доступных заявок нет.", returnKeyboard);
                    }
                    break;
                case "Искать по городу отправления":
                    session.setAttribute("statement", "ПоискОткуда");
                    sendMsg(message, "Введите название города, в котором находится груз");
                    break;
                case "Искать по городу назначения":
                    session.setAttribute("statement", "ПоискКуда");
                    sendMsg(message, "Введите название города, в который надо доставить груз");
                    break;
                case "Искать по имени пользователя":
                    session.setAttribute("statement", "ПоискЮзернейм");
                    sendMsg(message, "Введите имя пользователя в формате @username");
                    break;
                case "Искать по дате":
                    session.setAttribute("statement", "ПоискДата");
                    sendMsg(message, "Введите временной диапазон в формате ГГГГ.ММ.ДД:ГГГГ.ММ.ДД");
                    break;
                case "Удалить груз":
                    if (!(userList.get(getUsername(message)) >= 3)) {
                        sendMsg(message, "У вас недостаточно прав, чтобы удалить груз.", returnKeyboard);
                        session.removeAttribute("statement");
                        break;
                    }
                    session.setAttribute("statement", "Удаление");
                    sendMsg(message, "Введите номер груза");
                    break;
                case "Мой рейтинг":
                    if (getUsername(message) == null) {
                        sendMsg(message, "Имя пользователя Telegram не установлено." +
                                " Чтобы продолжить, установите имя пользователя Telegram в разделе \"Мой профиль\"", returnKeyboard);
                        break;
                    }
                    String userRating = dataBase.getUserRating(getUsername(message));
                    if (userRating.isBlank()) {
                        dataBase.push(String.format(Locale.ROOT, "INSERT INTO userrating (username, rating) VALUES ('%s',0) ON CONFLICT DO NOTHING", getUsername(message)));
                        userRating = dataBase.getUserRating(getUsername(message));
                    }
                    sendMsg(message, userRating, returnKeyboard);
                    break;
                case "Обновить рейтинг":
                    session.setAttribute("statement", "Обновить рейтинг");
                    sendMsg(message, "Введите имя пользователя, рейтинг которого вы хотите обновить, " +
                            "и новое число рейтинга этого пользователя в формате \"@пользователь, рейтинг\"");
                    break;
            }
        }
    }

    public ReplyKeyboardMarkup createKeyboard(String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        String[] rowButtons = text.split(";");
        for (String line : rowButtons) {
            String[] buttons = line.split(",");
            for (String button : buttons) {
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createKeyboard(String text, Boolean oneTime) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(oneTime);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        String[] rowButtons = text.split(";");
        for (String line : rowButtons) {
            String[] buttons = line.split(",");
            for (String button : buttons) {
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return BOTUSERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    public static String getUsername (Message message) {
        return message.getChat().getUserName();
    }


    public List<String> getSearchResult(String sql) {
        List<String> resultList = new ArrayList<>();
        List<Cargo> cargoList = dataBase.getCargo(sql);
        for (Cargo cargo : cargoList) {
            resultList.add(cargo.toString());
        }
        return resultList;
    }

    public void sendMsg(Message message, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text, ReplyKeyboardMarkup replyKeyboardMarkup, boolean enableMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(enableMarkup);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String text, int chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


