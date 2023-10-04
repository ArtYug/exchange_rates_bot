package com.exchange.exchange_rates_bot.bot;

import com.exchange.exchange_rates_bot.exception.ServiceException;
import com.exchange.exchange_rates_bot.service.ExchangeRatesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;

@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);
    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String HELP = "/help";


    private ExchangeRatesService exchangeRatesService;

    @Autowired
    public void setExchangeRatesService(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @Autowired
    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        switch (message) {
            case START -> {
                String userName = getBotUsername();
                // String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknownCommand(chatId);
        }
    }

    private void unknownCommand(Long chatId) {
        var text = "could not recognize the command";
        sendMessage(chatId, text);
    }

    private void startCommand(Long chatId, String userName) {
        System.out.println("userName = " + userName);
        var text = """
                Welcome to the bot, %s!
                                
                Here you can find out the official exchange rates for today, established by the Central Bank of the Europe Union.
                                
                To do this, use the commands:
                /usd - dollar exchange rate
                /eur - euro exchange rate
                                
                Additional commands:
                /help - get help
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void helpCommand(Long chatId) {
        var text = """
                Bot background information
                                
                To get current exchange rates, use the commands:
                /usd - dollar exchange rate
                /eur - euro rate
                """;
        sendMessage(chatId, text);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesService.getUSDExchangeRate();
            var text = "The dollar exchange rate at %s is %s ruble";
            formattedText = String.format(text, LocalDate.now(), usd);

        } catch (ServiceException e) {
            LOG.error("Error getting dollar rate", e);
            formattedText = "Could not get the current dollar rate. Please try later.";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesService.getEURExchangeRate();
            var text = "The euro exchange rate at %s is %s ruble";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException e) {
            LOG.error("Error getting euro rate", e);
            formattedText = "Could not get the current euro rate. Please try later.";
        }
        sendMessage(chatId, formattedText);
    }

    public void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("error sending message");
        }
    }

    @Override
    public String getBotUsername() {
        return "joljolero_bot";
    }
}
