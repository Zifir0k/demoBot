package ai.joy.demoBot.service;

import ai.joy.demoBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
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
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId,"Мне нечего сказать");
            }
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
