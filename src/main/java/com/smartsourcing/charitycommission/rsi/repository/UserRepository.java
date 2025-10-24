package com.smartsourcing.charitycommission.rsi.repository;

import com.smartsourcing.charitycommission.rsi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
