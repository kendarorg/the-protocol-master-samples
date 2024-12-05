package org.kendar.quotes.api;

import org.kendar.quotes.data.Quotation;
import org.kendar.quotes.data.QuotationsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/status")
public class StatusController {


    @GetMapping(value ="",produces = "text/plain")
    String findAllSymbols() {
        return "OK";
    }
}
