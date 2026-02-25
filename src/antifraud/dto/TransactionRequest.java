package antifraud.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransactionRequest {

    @NotNull
    @Min(1)
    private Long amount;

    @NotEmpty
    private String ip;

    @NotEmpty
    private String number;

    @NotEmpty
    private String region; // Added for Stage 5/6

    @NotNull
    private LocalDateTime date; // Added for Stage 5/6

    public TransactionRequest() {
    }

    // Standard Getters and Setters
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    // These methods resolve the red errors in AntiFraudService
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}