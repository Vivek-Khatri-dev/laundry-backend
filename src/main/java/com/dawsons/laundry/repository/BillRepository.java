package com.dawsons.laundry.repository;

import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Integer> {

    // ------------------------------------------------------------------
    // Bill.items is FetchType.EAGER, and Bill.customer/createdBy are
    // ManyToOne (also eager by default). Without an explicit JOIN FETCH,
    // Hibernate loads each of those associations with a SEPARATE query
    // per bill returned — the classic N+1 problem. For a list of 200
    // bills that's 200+ extra round trips to the database, and it only
    // gets slower as more bills pile up. Every query below that actually
    // returns a list/lookup of bills now fetches items/customer/createdBy
    // in the SAME query instead, cutting each page load down to 1 query.
    // ------------------------------------------------------------------

    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.items " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.receiptNo = :receiptNo")
    Optional<Bill> findByReceiptNo(@Param("receiptNo") Integer receiptNo);

    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.items " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.status <> :excludedStatus " +
           "ORDER BY b.receiptNo DESC")
    List<Bill> findByStatusNotOrderByReceiptNoDesc(@Param("excludedStatus") BillStatus excludedStatus);

    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.items " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.createDate = :date " +
           "ORDER BY b.receiptNo DESC")
    List<Bill> findByCreateDateOrderByReceiptNoDesc(@Param("date") LocalDate date);

    // Used by the range/monthly report — same eager-fetch treatment as the
    // single-day query above, just bounded by an inclusive [from, to] range.
    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.items " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.createDate BETWEEN :from AND :to " +
           "ORDER BY b.receiptNo DESC")
    List<Bill> findByCreateDateBetweenOrderByReceiptNoDesc(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ADD THIS METHOD - it's the one ReportService needs
    List<Bill> findByCreateDate(LocalDate date);

    @Query("SELECT COALESCE(MAX(b.receiptNo), 0) FROM Bill b")
    Integer findMaxReceiptNo();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b " +
           "WHERE b.createDate = :date AND b.status <> com.dawsons.laundry.entity.BillStatus.VOIDED")
    java.math.BigDecimal getDailyTotal(@Param("date") LocalDate date);

    // Kanban board calls this once per status — it's the most-repeated
    // (and previously most expensive) query in the app, so it gets the
    // same JOIN FETCH treatment.
    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.items " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE b.status = :status")
    List<Bill> findByStatus(@Param("status") BillStatus status);

    List<Bill> findByStatusNot(BillStatus status);

    List<Bill> findByStatusOrderByCreatedAtDesc(BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Bill> findByStatusIn(@Param("statuses") List<BillStatus> statuses);

    // Add this method to BillRepository.java
    @Query("SELECT DISTINCT b FROM Bill b " +
        "LEFT JOIN FETCH b.items " +
        "LEFT JOIN FETCH b.customer " +
        "LEFT JOIN FETCH b.createdBy " +
        "WHERE " +
        "LOWER(b.customerName) LIKE LOWER(:searchTerm) OR " +
        "LOWER(b.customerPhone) LIKE LOWER(:searchTerm) OR " +
        "CAST(b.receiptNo AS string) LIKE :searchTerm")
    List<Bill> searchBills(@Param("searchTerm") String searchTerm);
}