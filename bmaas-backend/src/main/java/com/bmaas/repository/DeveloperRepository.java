package com.bmaas.repository;

import com.bmaas.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

    Optional<Developer> findByEmail(String email);

    Optional<Developer> findByEmailIgnoreCase(String email);

    Optional<Developer> findByUserId(Long userId);

    Boolean existsByEmail(String email);

    Boolean existsByUserId(Long userId);
}
