package antifraud.service;

import antifraud.dto.TransactionRequest;
import antifraud.dto.TransactionResponse;
import antifraud.model.LimitEntity;
import antifraud.model.Transaction;
import antifraud.repository.LimitRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIPRepository;
import antifraud.repository.TransactionRepository;
import antifraud.util.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AntiFraudService {

    private final StolenCardRepository stolenCardRepository;
    private final SuspiciousIPRepository suspiciousIPRepository;
    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;

    public AntiFraudService(StolenCardRepository stolenCardRepository,
                            SuspiciousIPRepository suspiciousIPRepository,
                            TransactionRepository transactionRepository,
                            LimitRepository limitRepository) {
        this.stolenCardRepository = stolenCardRepository;
        this.suspiciousIPRepository = suspiciousIPRepository;
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
    }

    /**
     * POST /api/antifraud/transaction
     * Processes a transaction and applies fraud detection rules.
     */
    public TransactionResponse processTransaction(TransactionRequest request) {
        // 1. Validate Formats
        if (!Validator.isValidIP(request.getIp()) ||
                !Validator.isValidLuhn(request.getNumber()) ||
                !Validator.isValidRegion(request.getRegion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // 2. Load Dynamic Limits (Defaults to 200/1500 if not found)
        LimitEntity limits = limitRepository.findById(1L).orElse(new LimitEntity());
        long maxAllowed = limits.getMaxAllowed();
        long maxManual = limits.getMaxManual();

        List<String> info = new ArrayList<>();
        LocalDateTime endTime = request.getDate();
        LocalDateTime startTime = endTime.minusHours(1);

        // 3. Correlation Counts (Last hour history)
        long regionCount = transactionRepository.countDistinctRegions(
                request.getNumber(), request.getRegion(), startTime, endTime);
        long ipCount = transactionRepository.countDistinctIps(
                request.getNumber(), request.getIp(), startTime, endTime);

        // 4. Blacklist Checks
        boolean isCardBlacklisted = stolenCardRepository.existsByNumber(request.getNumber());
        boolean isIpBlacklisted = suspiciousIPRepository.existsByIp(request.getIp());

        // 5. Decision Logic
        String result = "ALLOWED";
        boolean prohibited = false;

        // PROHIBITED Rules
        if (isCardBlacklisted) { info.add("card-number"); prohibited = true; }
        if (isIpBlacklisted) { info.add("ip"); prohibited = true; }
        if (request.getAmount() > maxManual) { info.add("amount"); prohibited = true; }
        if (ipCount > 2) { info.add("ip-correlation"); prohibited = true; }
        if (regionCount > 2) { info.add("region-correlation"); prohibited = true; }

        if (prohibited) {
            result = "PROHIBITED";
        } else {
            // MANUAL_PROCESSING Rules
            boolean manual = false;
            if (request.getAmount() > maxAllowed) { info.add("amount"); manual = true; }
            if (ipCount == 2) { info.add("ip-correlation"); manual = true; }
            if (regionCount == 2) { info.add("region-correlation"); manual = true; }

            if (manual) {
                result = "MANUAL_PROCESSING";
            }
        }

        // 6. Save Transaction to History (Crucial for future correlations and feedback)
        Transaction transaction = new Transaction(
                request.getAmount(), request.getIp(), request.getNumber(),
                request.getRegion(), request.getDate(), result
        );
        transactionRepository.save(transaction);

        // 7. Sort and Format
        Collections.sort(info);
        String infoField = info.isEmpty() ? "none" : String.join(", ", info);
        return new TransactionResponse(result, infoField);
    }

    /**
     * PUT /api/antifraud/transaction
     * Adds feedback and updates dynamic limits.
     */
    public Transaction addFeedback(Long transactionId, String feedback) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!List.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED").contains(feedback)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!tx.getFeedback().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (tx.getResult().equals(feedback)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        updateDynamicLimits(tx.getResult(), feedback, tx.getAmount());
        tx.setFeedback(feedback);
        return transactionRepository.save(tx);
    }

    private void updateDynamicLimits(String validity, String feedback, long amount) {
        LimitEntity limits = limitRepository.findById(1L).get();

        if (validity.equals("ALLOWED") && feedback.equals("MANUAL_PROCESSING")) {
            limits.setMaxAllowed(decrease(limits.getMaxAllowed(), amount));
        } else if (validity.equals("ALLOWED") && feedback.equals("PROHIBITED")) {
            limits.setMaxAllowed(decrease(limits.getMaxAllowed(), amount));
            limits.setMaxManual(decrease(limits.getMaxManual(), amount));
        } else if (validity.equals("MANUAL_PROCESSING") && feedback.equals("ALLOWED")) {
            limits.setMaxAllowed(increase(limits.getMaxAllowed(), amount));
        } else if (validity.equals("MANUAL_PROCESSING") && feedback.equals("PROHIBITED")) {
            limits.setMaxManual(decrease(limits.getMaxManual(), amount));
        } else if (validity.equals("PROHIBITED") && feedback.equals("ALLOWED")) {
            limits.setMaxAllowed(increase(limits.getMaxAllowed(), amount));
            limits.setMaxManual(increase(limits.getMaxManual(), amount));
        } else if (validity.equals("PROHIBITED") && feedback.equals("MANUAL_PROCESSING")) {
            limits.setMaxManual(increase(limits.getMaxManual(), amount));
        }
        limitRepository.save(limits);
    }

    private long increase(long current, long value) {
        return (long) Math.ceil(0.8 * current + 0.2 * value);
    }

    private long decrease(long current, long value) {
        return (long) Math.ceil(0.8 * current - 0.2 * value);
    }

    // --- History Retrieval ---

    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAllByOrderByIdAsc();
    }

    public List<Transaction> getTransactionHistoryByNumber(String number) {
        List<Transaction> history = transactionRepository.findAllByNumberOrderByIdAsc(number);
        if (history.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return history;
    }
}