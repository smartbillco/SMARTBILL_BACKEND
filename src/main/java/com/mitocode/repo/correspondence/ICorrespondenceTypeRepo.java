package com.mitocode.repo.correspondence;

import com.mitocode.model.correspondence.CorrespondenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICorrespondenceTypeRepo extends JpaRepository<CorrespondenceType, Long> {
    Optional<CorrespondenceType> findByName(String name);
}
