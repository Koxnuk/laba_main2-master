package com.example.currency.service;

import com.example.currency.models.CurrencyRate;
import com.example.currency.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyConversionService {

    private final CurrencyService currencyService;
    private final CurrencyRateRepository currencyRateRepository;

    @Autowired
    public CurrencyConversionService(CurrencyService currencyService, CurrencyRateRepository currencyRateRepository) {
        this.currencyService = currencyService;
        this.currencyRateRepository = currencyRateRepository;
    }

    public BigDecimal convertCurrency(Integer fromCurId, Integer toCurId, BigDecimal amount) {
        CurrencyRate fromRate = currencyService.getCurrencyRate(fromCurId);
        CurrencyRate toRate = currencyService.getCurrencyRate(toCurId);

        BigDecimal fromRatePerUnit = fromRate.getCurOfficialRate()
                .divide(BigDecimal.valueOf(fromRate.getCurScale()), 6, RoundingMode.HALF_UP);

        BigDecimal toRatePerUnit = toRate.getCurOfficialRate()
                .divide(BigDecimal.valueOf(toRate.getCurScale()), 6, RoundingMode.HALF_UP);

        return amount.multiply(fromRatePerUnit)
                .divide(toRatePerUnit, 2, RoundingMode.HALF_UP);
    }

    public CurrencyRate createRate(CurrencyRate rate) {
        return currencyRateRepository.save(rate);
    }

    public List<CurrencyRate> getAllRates() {
        return currencyRateRepository.findAll();
    }

    public Optional<CurrencyRate> getRateById(Long id) {
        return currencyRateRepository.findById(id);
    }

    public CurrencyRate updateRate(Long id, CurrencyRate updatedRate) {
        Optional<CurrencyRate> existingRate = currencyRateRepository.findById(id);
        if (existingRate.isPresent()) {
            CurrencyRate rate = existingRate.get();
            rate.setCurOfficialRate(updatedRate.getCurOfficialRate());
            rate.setCurScale(updatedRate.getCurScale());
            rate.setDate(updatedRate.getDate());
            rate.setCurrency(updatedRate.getCurrency());
            return currencyRateRepository.save(rate);
        }
        throw new RuntimeException("Rate not found with id: " + id);
    }

    public void deleteRate(Long id) {
        currencyRateRepository.deleteById(id);
    }
}