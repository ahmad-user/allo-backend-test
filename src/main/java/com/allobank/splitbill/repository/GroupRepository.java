package com.allobank.splitbill.repository;

import com.allobank.splitbill.domain.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT DISTINCT g FROM GroupEntity g LEFT JOIN FETCH g.participants WHERE g.id = :id")
    Optional<GroupEntity> findByIdWithParticipants(@Param("id") Long id);
}
