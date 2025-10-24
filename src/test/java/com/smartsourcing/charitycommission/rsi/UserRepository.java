package uk.gov.ccew.rsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ccew.rsi.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
