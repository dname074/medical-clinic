package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
