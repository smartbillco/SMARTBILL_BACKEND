package com.mitocode.repo.correspondence;

import com.mitocode.model.correspondence.CorrespondenceDepartment;
import com.mitocode.model.correspondence.CorrespondenceSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDepartmentRepo extends JpaRepository<CorrespondenceDepartment, Long> {
    Optional<CorrespondenceDepartment> findByName(String name);
}
