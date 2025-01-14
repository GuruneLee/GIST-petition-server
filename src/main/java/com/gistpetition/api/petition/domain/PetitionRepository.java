package com.gistpetition.api.petition.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetitionRepository extends JpaRepository<Petition, Long> {
    Page<Petition> findByUserId(Long userId,Pageable pageable);

    Page<Petition> findByCategory(Category category, Pageable pageable);

    Page<Petition> findAll(Pageable pageable);

    Page<Petition> findByTitleContains(String keyword, Pageable pageable);

    @Query("SELECT p FROM Petition AS p LEFT JOIN FETCH p.agreements WHERE p.id=:petitionId")
    Petition findPetitionByWithEagerMode(@Param("petitionId") Long petitionId);
}
