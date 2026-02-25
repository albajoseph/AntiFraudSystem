package antifraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("transactionId") // Maps 'id' to 'transactionId' in JSON
    private Long id;

    private Long amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;

    private String result;    // The system's initial decision
    private String feedback = ""; // The human feedback, defaults to empty string

    public Transaction() {
    }

    // Updated constructor to include the system's result
    public Transaction(Long amount, String ip, String number, String region, LocalDateTime date, String result) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}