package ai.joy.demoBot.service;

import ai.joy.demoBot.config.BotConfig;
import ai.joy.demoBot.model.User;
import ai.joy.demoBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    private final BotConfig botConfig;
    private static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";

    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "Вывести приветственное сообщение"));
        listOfCommand.add(new BotCommand("/mydata", "Показать информацию"));
        listOfCommand.add(new BotCommand("/deletedata", "Удалить Данные"));
        listOfCommand.add(new BotCommand("/help","Вывести все команды"));
        listOfCommand.add(new BotCommand("/setting","Настройки"));
        try {
            this.execute(new SetMyCommands(listOfCommand,new BotCommandScopeDefault(),null));
        } catch (TelegramApiException exe) {
            log.error("Error occurred:" + exe.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (message){
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId,HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId,"Мне нечего сказать");
            }
        }

    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()){
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }

    protected void startCommandReceived(long chatId, String name){
        String answer = "Привет, " + name + " и Добро пожаловать!";
        log.info("replied to the user: " + name);
        sendMessage(chatId,answer);
    }
    protected void sendMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException exe) {
            log.error("Error occurred:" + exe.getMessage());
        }
    }
}
