package antifraud.repository;

import antifraud.model.SuspiciousIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIPRepository extends JpaRepository<SuspiciousIP, Long> {

    // Used in the service to quickly check if a transaction should be prohibited
    boolean existsByIp(String ip);

    // Used for the DELETE endpoint to find the IP before deleting
    Optional<SuspiciousIP> findByIp(String ip);

    // Used for the GET endpoint to return the list sorted by ID
    List<SuspiciousIP> findAllByOrderByIdAsc();
}