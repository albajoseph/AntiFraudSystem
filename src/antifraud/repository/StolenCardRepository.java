package antifraud.repository;

import antifraud.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

    // Used in the service to quickly check if a transaction should be prohibited
    boolean existsByNumber(String number);

    // Used for the DELETE endpoint to find the card before deleting
    Optional<StolenCard> findByNumber(String number);

    // Used for the GET endpoint to return the list sorted by ID
    List<StolenCard> findAllByOrderByIdAsc();
}