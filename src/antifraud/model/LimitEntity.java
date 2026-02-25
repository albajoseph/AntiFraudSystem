package antifraud.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dynamic_limits")
public class LimitEntity {

    @Id
    private Long id = 1L;

    private long maxAllowed = 200;
    private long maxManual = 1500;

    public LimitEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getMaxAllowed() { return maxAllowed; }
    public void setMaxAllowed(long maxAllowed) { this.maxAllowed = maxAllowed; }

    public long getMaxManual() { return maxManual; }
    public void setMaxManual(long maxManual) { this.maxManual = maxManual; }
}