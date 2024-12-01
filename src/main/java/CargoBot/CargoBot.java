package CargoBot;

import org.apache.shiro.session.Session;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import java.util.*;

public class CargoBot extends TelegramLongPollingSessionBot {
    //    static String[] resultList = DB.get();
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> keyboard = new ArrayList<>();
    private final String TOKEN = "5307814884:AAESlq09cj50Nzh4cuyTRr6nzeTIu8SDTAc";
    private final String BOTUSERNAME = "@Sikor_CargoBot";

    private final ReplyKeyboardMarkup mainKeyboard = createKeyboard("Добавить груз,Искать груз;Управление доступом,Удалить груз");
    private final ReplyKeyboardMarkup returnKeyboard = createKeyboard("На главную");
    private final ReplyKeyboardMarkup addingKeyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную");
    private final ReplyKeyboardMarkup accessManageKeyboard = createKeyboard("Добавить пользователя,Удалить пользователя;Список пользователей,На главную");
    private final ReplyKeyboardMarkup searchKeyboard = createKeyboard("Искать по городу отправления,Искать по городу назначения;Искать по имени пользователя,Искать по дате; На главную");

    @Override
    public void onUpdateReceived(Update update, Optional<Session> optional) {
        Map<String, Integer> userlist = DB.userGet();
        Message message = update.getMessage();
        Session session = optional.get();
        session.setTimeout(600000);
        if (message != null && message.hasText() && session.getAttribute("statement") != null) {
            ReplyKeyboardMarkup keyboard;
            switch (session.getAttribute("statement").toString()) {
                case "Откуда":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("whereFrom", message.getText());
                    sendMsg(message, "Город отправления записан", keyboard);
                    session.removeAttribute("statement");
                    break;
                case "Куда":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("whereTo", message.getText());
                    sendMsg(message, "Город назначения записан", keyboard);
                    session.removeAttribute("statement");
                    break;
                case "Вес":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("weight", message.getText());
                    sendMsg(message, "Вес записан", keyboard);
                    session.removeAttribute("statement");
                    break;
                case "Цена":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("price", message.getText());
                    session.removeAttribute("statement");
                    sendMsg(message, "Цена записана", keyboard);
                    break;
                case "Объём":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("size", message.getText());
                    session.removeAttribute("statement");
                    sendMsg(message, "Объём записан", keyboard);
                    break;
                case "Комментарий":
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    session.setAttribute("commentary", message.getText());
                    session.removeAttribute("statement");
                    sendMsg(message, "Комментарий записан", keyboard);
                    break;
                case "Добавить пользователя":
                    if (!(message.getText().contains("@"))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "Имя пользователя введено неправильно", keyboard);
                        session.removeAttribute("statement");
                        break;
                    }
                    session.setAttribute("userToAdd", message.getText().substring(1));
                    String sqlPush = String.format(Locale.ROOT, "INSERT INTO userlist (username, `group`) VALUES('%s', 2)", session.getAttribute("userToAdd"));
                    DB.push(sqlPush);
                    userlist = DB.userGet();
                    sendMsg(message, "Пользователь добавлен");
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
                    DB.push(sqlDelete);
                    userlist = DB.userGet();
                    sendMsg(message, "Пользователь удалён");
                    session.removeAttribute("statement");
                    break;
                case "ПоискОткуда":
                    session.setAttribute("searchFrom", message.getText());
                    String sql4 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE whereFrom = '%s'", session.getAttribute("searchFrom").toString());
                    System.out.println("Поиск откуда");
                    List<String> resultList = getSearchResult(sql4);
                    if (resultList.size() > 0 && !(resultList.get(0).isBlank())) {
                        for (String result : resultList) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("searchFrom");
                    session.removeAttribute("statement");
                    break;
                case "ПоискКуда":
                    session.setAttribute("searchTo", message.getText());
                    System.out.println("Поиск куда");
                    List<String> resultList1 = getSearchResult(String.format(Locale.ROOT,
                            "SELECT * FROM cargolist WHERE whereTo = '%s'",
                            session.getAttribute("searchTo").toString()));
                    if (!resultList1.isEmpty()) {
                        for (String result : resultList1) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("searchTo");
                    session.removeAttribute("statement");
                    break;
                case "ПоискЮзернейм":
                    session.setAttribute("username", message.getText().substring(1));
                    String sql2 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE username = '%s'", session.getAttribute("username").toString());
                    DB.get(sql2);
                    System.out.println("Поиск юзернейм");
                    List<String> resultList2 = getSearchResult(sql2);
                    if (!resultList2.isEmpty()) {
                        for (String result : resultList2) {
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
                    String sql3 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE dateadded BETWEEN '%s' AND '%s'", session.getAttribute("date1"), session.getAttribute("date2"));
                    DB.get(sql3);
                    List<String> resultList3 = getSearchResult(sql3);
                    if (!resultList3.isEmpty()) {
                        for (String result : resultList3) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", returnKeyboard);
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", returnKeyboard);
                    }
                    session.removeAttribute("date1");
                    session.removeAttribute("date2");
                    session.removeAttribute("statement");
                    break;
                case "Удалить груз":
                    session.setAttribute("id", message.getText());
                    String sql5 = "DELETE FROM cargolist WHERE id =" + Double.parseDouble(session.getAttribute("id").toString());
                    DB.push(sql5);
                    keyboard = createKeyboard("На главную");
                    sendMsg(message, "Груз удалён", keyboard);
                    session.removeAttribute("id");
                    session.removeAttribute("statement");
                    break;
                default:
            }
        }

        if (message != null && message.hasText()) {
            switch (message.getText()) {
                case "/start":
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot.", mainKeyboard);
                    break;
                case "Добавить груз":
                    System.out.println("Попытка добавить груз");
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 3))) {
                        sendMsg(message, "У вас недостаточно прав, чтобы добавить груз. Чтобы получить одобрение, обратитесь к администратору: @samara_121 или по телефону +79375845056", returnKeyboard);
                        break;
                    }
                    sendMsg(message, "Нажмите нужную кнопку и введите информацию. После того, как Вы введете все данные, нажмите кнопку 'Добавить'.", addingKeyboard);
                    break;
                case "Откуда":
                    session.setAttribute("statement", "Откуда");
                    sendMsg(message, "Введите название населенного пункта, из которого надо забрать груз");
                    break;
                case "Куда":
                    session.setAttribute("statement", "Куда");
                    sendMsg(message, "Введите название населенного пункта, в который надо доставить груз");
                    break;
                case "Вес":
                    session.setAttribute("statement", "Вес");
                    sendMsg(message, "Введите вес груза в тоннах(только число)");
                    break;
                case "Цена":
                    session.setAttribute("statement", "Цена");
                    sendMsg(message, "Введите сумму, которую получит грузоперевозчик после перевозки груза(в рублях)");
                    break;
                case "Объём":
                    session.setAttribute("statement", "Объём");
                    sendMsg(message, "Введите объём груза в м³(только число)");
                    break;
                case "Комментарий":
                    session.setAttribute("statement", "Комментарий");
                    sendMsg(message, "Введите свой комментарий к грузу, например - точный адрес места, где находится груз и куда его надо доставить");
                    break;
                case "Управление доступом":
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 2))) {
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
                    for (var entry : userlist.entrySet()) {
                        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    String out = sb.toString();
                    out = out.replace("1", "администратор");
                    out = out.replace("2", "может добавлять грузы");
                    sendMsg(message, out);
                    break;
                case "Добавить":
                    Cargo cargo = new Cargo(session);
                    sendMsg(message, "Груз добавлен: \n" + cargo, returnKeyboard);
                    System.out.println(cargo.toPushSQL());
                    DB.push(cargo.toPushSQL());
                    System.out.println("Груз добавлен: \n" + cargo);
                    break;
                case "Искать груз":
                    sendMsg(message, "По какому критерию искать груз?", searchKeyboard);
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
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 2))) {
                        sendMsg(message, "У вас недостаточно прав, чтобы удалить груз.", returnKeyboard);
                        break;
                    }
                    session.setAttribute("statement", "Удалить груз");
                    sendMsg(message, "Введите номер груза");
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


    public static List<String> getSearchResult(String sql) {
        List<String> resultList = new ArrayList<>();
        List<Cargo> cargoList = DB.get(sql);
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


