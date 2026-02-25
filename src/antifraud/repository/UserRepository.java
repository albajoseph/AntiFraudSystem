package antifraud.repository;

import antifraud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    // This is mandatory for the Hyperskill test ordering
    List<User> findAllByOrderByIdAsc();

    long count();
}