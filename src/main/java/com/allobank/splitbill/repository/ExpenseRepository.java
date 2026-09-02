package com.allobank.splitbill.repository;

import com.allobank.splitbill.domain.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    @Query("SELECT DISTINCT e FROM ExpenseEntity e " +
           "JOIN FETCH e.paidBy " +
           "LEFT JOIN FETCH e.participants " +
           "WHERE e.group.id = :groupId")
    List<ExpenseEntity> findByGroupIdWithDetails(@Param("groupId") Long groupId);
}
