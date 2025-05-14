package com.mitocode.repo.correspondence;

import com.mitocode.model.correspondence.CorrespondenceSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICorrespondenceSubtypeRepo extends JpaRepository<CorrespondenceSubtype, Long> {
    List<CorrespondenceSubtype> findByTypeId(Long typeId);

    Optional<CorrespondenceSubtype> findByNameAndTypeId(String name, Long typeId);

    Optional<CorrespondenceSubtype> findByName(String name);
}
