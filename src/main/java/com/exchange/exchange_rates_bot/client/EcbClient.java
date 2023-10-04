package com.exchange.exchange_rates_bot.client;

import com.exchange.exchange_rates_bot.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class EcbClient {

    private final OkHttpClient client;

    @Autowired
    public EcbClient(OkHttpClient client) {
        this.client = client;
    }

    @Value("${cbr.currency.rates.xml.url}")
    private String ecbCurrencyRatesXmlUrl;

    public Optional<String> getCurrencyRatesXML() throws ServiceException {
        var request = new Request.Builder()
                .url(ecbCurrencyRatesXmlUrl)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException e) {
            throw new ServiceException("Error in receiving exchange rates from Europe Central Bank", e);
        }
    }
}
