package antifraud.dto;

public class TransactionResponse {

    private String result;
    private String info; // New field required for Stage 4

    // Default constructor needed for Jackson JSON serialization
    public TransactionResponse() {
    }

    // Updated constructor to easily set both fields
    public TransactionResponse(String result, String info) {
        this.result = result;
        this.info = info;
    }

    // Getters and Setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}