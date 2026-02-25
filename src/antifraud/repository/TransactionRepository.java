package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Correlation logic: Count unique regions (excluding current) in the last hour
    @Query("SELECT COUNT(DISTINCT t.region) FROM Transaction t " +
            "WHERE t.number = ?1 AND t.region <> ?2 AND t.date BETWEEN ?3 AND ?4")
    long countDistinctRegions(String number, String region, LocalDateTime start, LocalDateTime end);

    // Correlation logic: Count unique IPs (excluding current) in the last hour
    @Query("SELECT COUNT(DISTINCT t.ip) FROM Transaction t " +
            "WHERE t.number = ?1 AND t.ip <> ?2 AND t.date BETWEEN ?3 AND ?4")
    long countDistinctIps(String number, String ip, LocalDateTime start, LocalDateTime end);

    // Stage 6: Get all history sorted by ID
    List<Transaction> findAllByOrderByIdAsc();

    // Stage 6: Get history for a specific card number sorted by ID
    List<Transaction> findAllByNumberOrderByIdAsc(String number);
}