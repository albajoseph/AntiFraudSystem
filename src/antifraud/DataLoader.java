package antifraud;

import antifraud.model.LimitEntity;
import antifraud.repository.LimitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final LimitRepository limitRepository;

    public DataLoader(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }

    @Override
    public void run(String... args) {
        // Initialize the dynamic limits if they don't exist
        if (limitRepository.findById(1L).isEmpty()) {
            LimitEntity initialLimits = new LimitEntity();
            initialLimits.setMaxAllowed(200);
            initialLimits.setMaxManual(1500);
            limitRepository.save(initialLimits);
        }
    }
}