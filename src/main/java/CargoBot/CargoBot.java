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
    static String resultList = new String();
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> keyboard = new ArrayList<>();
    Map<String, Integer> userlist = DB.userGet();

    @Override
    public void onUpdateReceived(Update update, Optional<Session> optional) {
        Message message = update.getMessage();
        Session session = optional.get();
        session.setTimeout(600000);
        System.out.println(session.getAttributeKeys());
        if (message != null && message.hasText() && session.getAttribute("statement") != null) {
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            switch (session.getAttribute("statement").toString()) {
                case "Откуда":
                    //whereFrom = message.getText();
                    session.setAttribute("whereFrom", message.getText());
                    System.out.println(session.getAttribute("whereFrom"));
                    session.removeAttribute("statement");
                    break;
                case "Куда":
                    session.setAttribute("whereTo", message.getText());
                    System.out.println(session.getAttribute("whereTo"));
                    session.removeAttribute("statement");
                    break;
                case "Вес":
                    session.setAttribute("weight", message.getText());
                    System.out.println(session.getAttribute("weight"));
                    session.removeAttribute("statement");
                    break;
                case "Контактный телефон":
                    session.setAttribute("phone", message.getText());
                    System.out.println(session.getAttribute("phone"));
                    session.removeAttribute("statement");
                    break;
                case "Цена":
                    session.setAttribute("price", message.getText());
                    System.out.println(session.getAttribute("price"));
                    session.removeAttribute("statement");
                    break;
                case "Объём":
                    session.setAttribute("size", message.getText());
                    System.out.println(session.getAttribute("size"));
                    session.removeAttribute("statement");
                    break;
                case "Комментарий":
                    session.setAttribute("commentary", message.getText());
                    System.out.println(session.getAttribute("commentary"));
                    session.removeAttribute("statement");
                    break;
                case "Добавить пользователя":
//                    session.setAttribute("userToAdd", message.getText());
                    if (!(message.getText().contains("@"))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "Имя пользователя введено неправильно",keyboard);
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
                    String sql = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE whereFrom = '%s'", session.getAttribute("searchFrom").toString());
                    //String sql = "SELECT * FROM cargolist WHERE whereFrom =" + whereFrom
                    DB.get(sql);
                    System.out.println(resultList + " : " + resultList.length());
                    keyboard = createKeyboard("Добавить груз;Искать груз;Управление доступом");
                    if (resultList.length() > 2) {
                        System.out.println("иф");
                        sendMsg(message, resultList, keyboard);
                        session.removeAttribute("searchFrom");
                        session.removeAttribute("statement");
                    } else {
                        System.out.println("элс");
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("searchFrom");
                        session.removeAttribute("statement");
                    }

                    break;
                case "ПоискКуда":
                    session.setAttribute("searchTo", message.getText());
                    String sql1 = String.format(Locale.ROOT, "SELECT * FROM cargolist WHERE whereTo = '%s'", session.getAttribute("searchTo").toString());
                    System.out.println(sql1);
                    DB.get(sql1);
                    keyboard = createKeyboard("Добавить груз;Искать груз;Управление доступом");
                    if (resultList.length() > 2) {
                        sendMsg(message, resultList,keyboard);
                        session.removeAttribute("searchTo");
                        session.removeAttribute("statement");
                    } else {
                        sendMsg(message, "По Вашему запросу не найдено ни одного груза", keyboard);
                        session.removeAttribute("searchTo");
                        session.removeAttribute("statement");
                    }
                    break;
                default:
            }
        }

        if (message != null && message.hasText()) {
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            switch (((Message) message).getText()) {
                case "/start":
                    keyboard = createKeyboard("Добавить груз;Искать груз;Управление доступом");
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot.", keyboard);
                    break;
                case "Добавить груз":
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 3))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "У вас недостаточно прав, чтобы добавить груз. Чтобы получить одобрение, обратитесь к администратору: @samara_121 или по телефону +79375845056",keyboard);
                        break;
                    }
                    keyboard = createKeyboard("Откуда,Куда;Вес,Объём;Цена,Контактный телефон;Комментарий,Добавить", true);
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
                case "Контактный телефон":
                    session.setAttribute("statement", "Контактный телефон");
                    sendMsg(message, "Введите свой контактный телефон");
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
                    System.out.println(session.getHost());
                    System.out.println(userlist.keySet());
                    if (!(userlist.containsKey(session.getHost()) && (userlist.get(session.getHost()) < 2))) {
                        keyboard = createKeyboard("На главную");
                        sendMsg(message, "У вас недостаточно прав.",keyboard);
                        break;
                    }
                    keyboard = createKeyboard("Добавить пользователя,Удалить пользователя;Список пользователей,На главную");
                    sendMsg(message, "Выберите действие.",keyboard);
                    break;
                case "На главную":
                    session.removeAttribute("statement");
                   keyboard = createKeyboard("Добавить груз;Искать груз;Управление доступом");
                    sendMsg(message, "Добрый день! Вас приветствует CargoBot. ",keyboard);
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
                    System.out.println(out);
                    sendMsg(message, out);
                    break;
                case "Добавить":
                    Date date = new Date();
                    SimpleDateFormat pattern = new SimpleDateFormat("dd.MM.yyyy");
                    session.setAttribute("date", pattern.format(date));
                    System.out.println(session.getAttribute("date"));
                    String sql = String.format(Locale.ROOT, "INSERT INTO cargolist (whereFrom, whereTo, price, weight, username, phone, size, commentary, addDate) VALUES ('%s', '%s', %.0f, %4.1f, '%s', '%s', %4.1f, '%s', '%s')",
                            session.getAttribute("whereFrom"), session.getAttribute("whereTo"), Double.parseDouble(session.getAttribute("price").toString()),
                            Double.parseDouble(session.getAttribute("weight").toString()), session.getHost(), session.getAttribute("phone"), Double.parseDouble(session.getAttribute("size").toString()),
                            session.getAttribute("commentary"), session.getAttribute("date"));
                    System.out.println(sql);
                    DB.push(sql);
                    keyboard = createKeyboard("На главную");
                    sendMsg(message, "Груз добавлен: \nОткуда: " + session.getAttribute("whereFrom") + "\nКуда: " + session.getAttribute("whereTo") +
                            "\nЦена: " + session.getAttribute("price") + "\nВес: " + session.getAttribute("weight") +
                            "\nОбъём: " + session.getAttribute("size") + "\nИмя пользователя: " + session.getHost() + "\nКонтактный телефон: " + session.getAttribute("phone") +
                            "\nДата добавления груза: " + session.getAttribute("date") + "\nКомментарий: " + session.getAttribute("commentary")+"Телефон администрации: +79375845056",keyboard);
                    session.stop();
                    break;
                case "Искать груз":
                   keyboard = createKeyboard("Искать по городу отправления,Искать по городу назначения");

                    sendMsg(message, "По какому критерию искать груз?",keyboard);
                    break;
                case "Искать по городу отправления":
                    session.setAttribute("statement", "ПоискОткуда");
                    sendMsg(message, "Введите название города, в котором находится груз");
                    break;
                case "Искать по городу назначения":
                    session.setAttribute("statement", "ПоискКуда");
                    sendMsg(message, "Введите название города, в который надо доставить груз");
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
        for (String line:rowButtons) {
            String[] buttons = line.split(",");
            for (String button:buttons) {
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        System.out.println(keyboard);
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
        for (String line:rowButtons) {
            String[] buttons = line.split(",");
            for (String button:buttons) {
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        System.out.println(keyboard);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return "Sikor_CargoBot";
    }

    @Override
    public String getBotToken() {
        return "5307814884:AAESlq09cj50Nzh4cuyTRr6nzeTIu8SDTAc";
    }


    public static void getSearchResult(List<String> list) {
        resultList = list.toString();
    }

    public void sendMsg(Message message, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        System.out.println(text);
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
}

