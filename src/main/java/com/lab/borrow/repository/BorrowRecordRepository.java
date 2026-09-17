package com.lab.borrow.repository;

import com.lab.borrow.entity.BorrowRecord;
import com.lab.borrow.entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    @Query("""
            SELECT b
            FROM BorrowRecord b
            WHERE (:userId IS NULL OR b.userId = :userId)
              AND (:status IS NULL OR b.status = :status)
            ORDER BY b.id DESC
            """)
    List<BorrowRecord> search(
            @Param("userId") Long userId,
            @Param("status") BorrowStatus status
    );

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
