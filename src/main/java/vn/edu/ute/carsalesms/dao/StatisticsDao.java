package vn.edu.ute.carsalesms.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsDao {

    BigDecimal sumRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    long countOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    long countPaidOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    List<Object[]> findDailyRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    List<Object[]> findDailyOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    List<Object[]> findOrderStatusBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    List<Object[]> findPaymentMethodBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    List<Object[]> findTopCars(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId, int limit);

    List<Object[]> findBranchRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, int limit);
}

