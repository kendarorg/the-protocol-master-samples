package org.kendar.quotes.api;

import org.kendar.quotes.data.Quotation;
import org.kendar.quotes.data.QuotationsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/quotation")
public class QuotationController {
    private final QuotationsRepository repository;

    public QuotationController(QuotationsRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping(value = "/symbols", produces = "application/json")
    String clearQuotationsData() {
        repository.deleteAll();
        return "OK";
    }


    @GetMapping(value = "/symbols", produces = "application/json")
    List<QuotationSymbol> findAllSymbols() {

        return repository.findAllSymbols().stream()
                .map(s -> {
                    var res = new QuotationSymbol();
                    res.setSymbol(s);
                    return res;
                }).collect(Collectors.toList());
    }

    @GetMapping(value = "/quotes/{identifier}", produces = "application/json")
    List<Quotation> findQuotesForSymbol(@PathVariable String identifier) {
        return repository.findQuotesForSymbol(identifier);
    }

    @GetMapping(value = "/quote/{identifier}", produces = "application/json")
    Quotation findQuoteForSymbol(@PathVariable String identifier) {
        var res = repository.findLastQuoteForSymbol(identifier);
        if (res.size() == 0) return null;
        return res.get(0);
    }
}
