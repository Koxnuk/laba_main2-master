package com.example.currency.service;

import com.example.currency.client.NbrbApiClient;
import com.example.currency.models.CurrencyInfo;
import com.example.currency.models.CurrencyRate;
import com.example.currency.repository.CurrencyInfoRepository;
import com.example.currency.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    private final CurrencyInfoRepository currencyInfoRepository;
    private final CurrencyRateRepository currencyRateRepository;
    private final NbrbApiClient apiClient;

    @Autowired
    public CurrencyService(
            CurrencyInfoRepository currencyInfoRepository,
            CurrencyRateRepository currencyRateRepository,
            NbrbApiClient apiClient
    ) {
        this.currencyInfoRepository = currencyInfoRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.apiClient = apiClient;
    }

    public List<CurrencyInfo> getAllCurrencies() {
        List<CurrencyInfo> dbCurrencies = currencyInfoRepository.findAll();
        if (dbCurrencies.isEmpty()) {
            // Fetch from API and save to DB if not found
            List<CurrencyInfo> apiCurrencies = apiClient.getAllCurrencies();
            return currencyInfoRepository.saveAll(apiCurrencies);
        }
        return dbCurrencies;
    }

    public CurrencyRate getCurrencyRate(Integer curId) {
        // Try to find the latest rate in the database
        Optional<CurrencyInfo> currency = currencyInfoRepository.findById(curId);
        if (currency.isPresent()) {
            List<CurrencyRate> rates = currency.get().getRates();
            Optional<CurrencyRate> latestRate = rates.stream()
                    .filter(rate -> rate.getDate().equals(LocalDate.now()))
                    .findFirst();
            if (latestRate.isPresent()) {
                return latestRate.get();
            }
        }
        // Fetch from API if not found in DB
        CurrencyRate rate = apiClient.getCurrencyRate(curId);
        if (currency.isPresent()) {
            rate.setCurrency(currency.get());
            currencyRateRepository.save(rate);
        }
        return rate;
    }

    public CurrencyInfo createCurrency(CurrencyInfo currencyInfo) {
        return currencyInfoRepository.save(currencyInfo);
    }

    public List<CurrencyInfo> getAllCurrenciesFromDb() {
        return currencyInfoRepository.findAll();
    }

    public Optional<CurrencyInfo> getCurrencyById(Integer id) {
        return currencyInfoRepository.findById(id);
    }

    public CurrencyInfo updateCurrency(Integer id, CurrencyInfo updatedCurrency) {
        Optional<CurrencyInfo> existingCurrency = currencyInfoRepository.findById(id);
        if (existingCurrency.isPresent()) {
            CurrencyInfo currency = existingCurrency.get();
            currency.setCurCode(updatedCurrency.getCurCode());
            currency.setCurAbbreviation(updatedCurrency.getCurAbbreviation());
            currency.setCurName(updatedCurrency.getCurName());
            currency.setCurScale(updatedCurrency.getCurScale());
            return currencyInfoRepository.save(currency);
        }
        throw new RuntimeException("Currency not found with id: " + id);
    }

    public void deleteCurrency(Integer id) {
        currencyInfoRepository.deleteById(id);
    }
}