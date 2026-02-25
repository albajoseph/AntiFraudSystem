package antifraud.repository;

import antifraud.model.LimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitRepository extends JpaRepository<LimitEntity, Long> {
}