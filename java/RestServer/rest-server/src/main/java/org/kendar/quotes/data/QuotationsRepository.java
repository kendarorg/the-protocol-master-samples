package org.kendar.quotes.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationsRepository extends JpaRepository<Quotation, Long> {

    @Query("SELECT lr.symbol FROM Quotation lr GROUP BY lr.symbol ORDER BY lr.symbol ASC")
    List<String> findAllSymbols();

    @Query("SELECT lr FROM Quotation lr WHERE lr.symbol=:symbol ORDER BY lr.date ASC")
    List<Quotation> findQuotesForSymbol(@Param("symbol") String symbol);

    @Query("SELECT lr FROM Quotation lr WHERE lr.symbol=:symbol ORDER BY lr.date DESC")
    List<Quotation> findLastQuoteForSymbol(String symbol);
}