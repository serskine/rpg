package game.rolltable.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for RollTable entities.
 */
@Repository
public interface RollTableJpaRepository extends JpaRepository<RollTableEntity, Integer> {
    RollTableEntity findByTitle(final String title);
}
