package com.lab.borrow.repository;

import com.lab.borrow.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE BorrowRecord b
            SET b.status = com.lab.borrow.entity.BorrowStatus.RETURNED,
                b.actualReturnTime = :actualReturnTime
            WHERE b.id = :id
              AND b.status = com.lab.borrow.entity.BorrowStatus.BORROWING
            """)
    int markReturnedIfBorrowing(
            @Param("id") Long id,
            @Param("actualReturnTime") LocalDateTime actualReturnTime
    );
}
