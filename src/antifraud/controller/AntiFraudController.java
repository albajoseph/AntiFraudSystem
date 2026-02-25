package antifraud.controller;

import antifraud.dto.TransactionRequest;
import antifraud.dto.TransactionResponse;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIP;
import antifraud.model.Transaction;
import antifraud.service.AntiFraudService;
import antifraud.service.BlacklistService;
import antifraud.util.Validator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
public class AntiFraudController {

    private final AntiFraudService antiFraudService;
    private final BlacklistService blacklistService;

    public AntiFraudController(AntiFraudService antiFraudService, BlacklistService blacklistService) {
        this.antiFraudService = antiFraudService;
        this.blacklistService = blacklistService;
    }

    // --- Transaction Processing & Feedback (Stage 6) ---

    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(antiFraudService.processTransaction(request));
    }

    @PutMapping("/transaction")
    public Transaction addFeedback(@RequestBody Map<String, Object> request) {
        Long transactionId = Long.valueOf(request.get("transactionId").toString());
        String feedback = (String) request.get("feedback");
        return antiFraudService.addFeedback(transactionId, feedback);
    }

    // --- Transaction History (Stage 6) ---

    @GetMapping("/history")
    public List<Transaction> getHistory() {
        return antiFraudService.getTransactionHistory();
    }

    @GetMapping("/history/{number}")
    public List<Transaction> getHistoryByCard(@PathVariable String number) {
        if (!Validator.isValidLuhn(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return antiFraudService.getTransactionHistoryByNumber(number);
    }

    // --- Suspicious IP Endpoints ---

    @PostMapping("/suspicious-ip")
    public SuspiciousIP addSuspiciousIP(@RequestBody Map<String, String> request) {
        return blacklistService.addIP(request.get("ip"));
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, String> deleteSuspiciousIP(@PathVariable String ip) {
        blacklistService.deleteIP(ip);
        return Map.of("status", "IP " + ip + " successfully removed!");
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIP> listSuspiciousIPs() {
        return blacklistService.listIPs();
    }

    // --- Stolen Card Endpoints ---

    @PostMapping("/stolencard")
    public StolenCard addStolenCard(@RequestBody Map<String, String> request) {
        return blacklistService.addCard(request.get("number"));
    }

    @DeleteMapping("/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable String number) {
        blacklistService.deleteCard(number);
        return Map.of("status", "Card " + number + " successfully removed!");
    }

    @GetMapping("/stolencard")
    public List<StolenCard> listStolenCards() {
        return blacklistService.listCards();
    }
}