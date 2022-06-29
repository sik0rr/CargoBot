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
    private Map<String, Integer> userlist = DB.userGet();
    private final String TOKEN = System.getenv("TOKEN");
    private final String BOTUSERNAME = System.getenv("BOTUSERNAME");
    @Override
    public void onUpdateReceived(Update update, Optional<Session> optional) {
        Message message = update.getMessage();
        Session session = optional.get();
        session.setTimeout(600000);
        if (message != null && message.hasText() && session.getAttribute("statement") != null) {
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
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
//                case "Контактный телефон":
//                    session.setAttribute("phone", message.getText());
//                    session.removeAttribute("statement");
//                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Контактный телефон;Комментарий,Добавить;На главную", false);
//                    sendMsg(message, "Контактный телефон записан", keyboard);
//                    break;
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
                    //String sql = "SELECT * FROM cargolist WHERE whereFrom =" + whereFrom
                    keyboard = createKeyboard("На главную");
                    System.out.println("Поиск откуда");
                    String[] resultList = getSearchResult(sql4);
                    if (resultList.length >0 && !(resultList[0].isBlank())) {
                        for (String result:resultList) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", keyboard);
                        session.removeAttribute("searchFrom");
                        session.removeAttribute("statement");
                    }
                    else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("searchFrom");
                        session.removeAttribute("statement");
                        break;
                    }
                    break;
                case "ПоискКуда":
                    session.setAttribute("searchTo", message.getText());
                    String sql1 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE whereTo = '%s'", session.getAttribute("searchTo").toString());
                    DB.get(sql1);
                    keyboard = createKeyboard("На главную");
                    System.out.println("Поиск куда");
                    String[] resultList1 = getSearchResult(sql1);
                    if (resultList1.length >0 && !(resultList1[0].isBlank())) {
                        for (String result : resultList1) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", keyboard);
                        session.removeAttribute("searchTo");
                        session.removeAttribute("statement");
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("searchTo");
                        session.removeAttribute("statement");
                    }
                    break;
                case "ПоискЮзернейм":
                    session.setAttribute("username", message.getText().substring(1));
                    String sql2 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE username = '%s'", session.getAttribute("username").toString());
                    DB.get(sql2);
                    System.out.println("Поиск юзернейм");
                    keyboard = createKeyboard("На главную");
                    String[] resultList2 = getSearchResult(sql2);
                    if (resultList2.length >0 && !(resultList2[0].isBlank())) {
                        for (String result : resultList2) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", keyboard);
                        session.removeAttribute("username");
                        session.removeAttribute("statement");
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("username");
                        session.removeAttribute("statement");
                    }
                    break;
                case "ПоискДата":
                    String[] dateRange = message.getText().split(":");
                    session.setAttribute("date1", dateRange[0].replace(".", "-"));
                    session.setAttribute("date2", dateRange[1].replace(".", "-"));
                    System.out.println("Поиск даты");
                    String sql3 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE addDate BETWEEN '%s' AND '%s'", session.getAttribute("date1"), session.getAttribute("date2"));
                    DB.get(sql3);
                    keyboard = createKeyboard("На главную");
                    String[] resultList3 = getSearchResult(sql3);
                    if (resultList3.length >0 && !(resultList3[0].isBlank())) {
                        for (String result:resultList3) {
                            sendMsg(message, result);
                        }
                        sendMsg(message, "Поиск завершен", keyboard);
                        session.removeAttribute("date1");
                        session.removeAttribute("date2");
                        session.removeAttribute("statement");
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("date1");
                        session.removeAttribute("date2");
                        session.removeAttribute("statement");
                    }
                    break;
                case "Удалить груз":
                    session.setAttribute("id", message.getText());
                    String sql5 = "DELETE FROM cargolist WHERE id =" +  Double.parseDouble(session.getAttribute("id").toString());
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
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            switch (((Message) message).getText()) {
                case "/start":
                    keyboard = createKeyboard("Добавить груз;Искать груз;Управление доступом,Удалить груз");
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot.", keyboard);
                    break;
                case "Добавить груз":
                    System.out.println("Попытка добавить груз");
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 3))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "У вас недостаточно прав, чтобы добавить груз. Чтобы получить одобрение, обратитесь к администратору: @samara_121 или по телефону +79375845056", keyboard);
                        break;
                    }
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Комментарий;Добавить,На главную", true);
                    sendMsg(message, "Нажмите нужную кнопку и введите информацию. После того, как Вы введете все данные, нажмите кнопку 'Добавить'.", keyboard);
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
//                case "Контактный телефон":
//                    session.setAttribute("statement", "Контактный телефон");
//                    sendMsg(message, "Введите свой контактный телефон");
//                    break;
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
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "У вас недостаточно прав.", keyboard);
                        break;
                    }
                    keyboard = createKeyboard("Добавить пользователя,Удалить пользователя;Список пользователей,На главную");
                    sendMsg(message, "Выберите действие.", keyboard);
                    break;
                case "На главную":
                    session.removeAttribute("statement");
                    keyboard = createKeyboard("Добавить груз,Искать груз;Управление доступом,Удалить груз");
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot. ", keyboard);
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
                    String out = userlist.toString();
                    out = out.replace(", ", "\n");
                    out = out.replace("{", "");
                    out = out.replace("}", "");
                    out = out.replace("=2", ": может добавлять грузы");
                    out = out.replace("=1", ": администратор");
                    sendMsg(message, out);
                    break;
                case "Добавить":
//                    if (session.getAttribute("whereFrom").toString().isEmpty()
//                            && session.getAttribute("whereTo").toString().length() <0
//                            && session.getAttribute("price").toString().length() <0
//                            && session.getAttribute("weight").toString().length() <0
//                            && session.getAttribute("size").toString().length() >0
//                            && session.getAttribute("commentary").toString().length() >0) {
//                        keyboard = createKeyboard("Добавить груз");
//                        System.out.println("zashlo v if");
//                        sendMsg(message, "Информация введена не полностью. Пожалуйста, добавьте груз заново", keyboard);
//                        session.stop();
//                        break;
//                    }
                    sendMsg(message, "Если кнопка не срабатывает, значит Вы не указали какую-то информацию. В этом случае нужно нажать на кнопку 'На главную' и повторить попытку.");
                    Date date = new Date();
                    SimpleDateFormat pattern = new SimpleDateFormat("yyyy.MM.dd");
                    session.setAttribute("date", pattern.format(date));
                    sendMsg("Груз добавлен пользователем: " + session.getHost(), 383458909);
                    String sql = String.format(Locale.ROOT, "INSERT INTO cargolist (whereFrom, whereTo, price, weight, username, size, commentary, addDate) VALUES ('%s', '%s', %.0f, %4.1f, '%s', %4.1f, '%s', '%s')",
                            session.getAttribute("whereFrom"),
                            session.getAttribute("whereTo"),
                            Double.parseDouble(session.getAttribute("price").toString()),
                            Double.parseDouble(session.getAttribute("weight").toString()),
                            session.getHost(),
                            Double.parseDouble(session.getAttribute("size").toString()),
                            session.getAttribute("commentary"),
                            session.getAttribute("date"));
                    DB.push(sql);
                    System.out.println("Попытка добавить груз: \nОткуда: " + session.getAttribute("whereFrom") + "\nКуда: " + session.getAttribute("whereTo") +
                            "\nЦена: " + session.getAttribute("price") + "\nВес: " + session.getAttribute("weight") +
                            "\nОбъём: " + session.getAttribute("size") + "\nИмя пользователя: " + session.getHost() +
                            "\nДата добавления груза: " + session.getAttribute("date") + "\nКомментарий: " + session.getAttribute("commentary")+"\nТелефон администрации: +79375845056");
                    sendMsg(message, "Груз добавлен: \nОткуда: " + session.getAttribute("whereFrom") + "\nКуда: " + session.getAttribute("whereTo") +
                            "\nЦена: " + session.getAttribute("price") + "\nВес: " + session.getAttribute("weight") +
                            "\nОбъём: " + session.getAttribute("size") + "\nИмя пользователя: " + session.getHost() +
                            "\nДата добавления груза: " + session.getAttribute("date") + "\nКомментарий: " + session.getAttribute("commentary") + "\nТелефон администрации: +79375845056", keyboard);
                    session.stop();
                    break;
                case "Искать груз":
                    keyboard = createKeyboard("Искать по городу отправления,Искать по городу назначения;Искать по имени пользователя,Искать по дате");
                    sendMsg(message, "По какому критерию искать груз?", keyboard);
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
                    if(!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) <2))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message,"У вас недостаточно прав, чтобы удалить груз.");
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
    public static String addCargo (Session session) {
        String whereFrom = session.getAttribute("whereFrom").toString();
        String whereTo = session.getAttribute("whereTo").toString();
        Double price = Double.parseDouble(session.getAttribute("price").toString());
        Double weight = Double.parseDouble(session.getAttribute("weight").toString());
        String username = session.getHost();
        Double size = Double.parseDouble(session.getAttribute("size").toString());
        String comment = session.getAttribute("commentary").toString();
        String date = session.getAttribute("date").toString();
        System.out.println(whereFrom);
        System.out.println(weight);
        if (whereFrom.isBlank() || whereTo.isBlank() || price <=0 || weight <= 0 || username.isBlank() || size <=0 || comment.isBlank() || date.isBlank()) {
            return "";
        }
        return String.format(Locale.ROOT, "INSERT INTO cargolist (whereFrom, whereTo, price, weight, username, size, commentary, addDate) VALUES ('%s', '%s', %.0f, %4.1f, '%s', %4.1f, '%s', '%s')",
                whereFrom, whereTo, price,weight,username,size,comment, date);
    }
    @Override
    public String getBotUsername() {
        return BOTUSERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }


    public static String[] getSearchResult(String sql) {
        String rawResultList = DB.get(sql).toString();
        rawResultList = rawResultList.replace("]", "");
        rawResultList = rawResultList.replace("[", "");
        String[] resultList = rawResultList.split(",");
        System.out.println(Arrays.toString(resultList));
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


