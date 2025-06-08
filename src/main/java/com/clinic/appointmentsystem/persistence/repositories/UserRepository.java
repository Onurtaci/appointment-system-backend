package com.clinic.appointmentsystem.persistence.repositories;

import com.clinic.appointmentsystem.domain.entities.User;
import com.clinic.appointmentsystem.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);
}
