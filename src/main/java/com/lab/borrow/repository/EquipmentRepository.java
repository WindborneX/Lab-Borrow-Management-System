package com.lab.borrow.repository;

import com.lab.borrow.entity.Equipment;
import com.lab.borrow.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("""
            SELECT e
            FROM Equipment e
            WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:category IS NULL OR LOWER(e.category) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (:status IS NULL OR e.status = :status)
            ORDER BY e.id ASC
            """)
    List<Equipment> search(
            @Param("name") String name,
            @Param("category") String category,
            @Param("status") EquipmentStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Equipment e
            SET e.status = :targetStatus
            WHERE e.id = :id
              AND e.status = :expectedStatus
            """)
    int updateStatusIfCurrent(
            @Param("id") Long id,
            @Param("expectedStatus") EquipmentStatus expectedStatus,
            @Param("targetStatus") EquipmentStatus targetStatus
    );
}
