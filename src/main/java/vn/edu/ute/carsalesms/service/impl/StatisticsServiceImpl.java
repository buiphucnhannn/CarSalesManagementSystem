package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.StatisticsDao;
import vn.edu.ute.carsalesms.model.dto.BranchStatisticsItem;
import vn.edu.ute.carsalesms.model.dto.StatisticsBreakdownItem;
import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;
import vn.edu.ute.carsalesms.model.dto.StatisticsKpiItem;
import vn.edu.ute.carsalesms.model.dto.StatisticsTrendPoint;
import vn.edu.ute.carsalesms.model.dto.TopCarStatisticsItem;
import vn.edu.ute.carsalesms.service.StatisticsService;
import vn.edu.ute.carsalesms.session.UserSessionContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsDao statisticsDao;
    private final UserSessionContext sessionContext;

    public StatisticsServiceImpl(StatisticsDao statisticsDao, UserSessionContext sessionContext) {
        this.statisticsDao = Objects.requireNonNull(statisticsDao, "statisticsDao is required");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext is required");
    }

    @Override
    public StatisticsDashboardData getAdminStatistics(LocalDate fromDate, LocalDate toDate) {
        return loadStatistics(fromDate, toDate, null, true);
    }

    @Override
    public StatisticsDashboardData getStaffStatistics(LocalDate fromDate, LocalDate toDate) {
        Long branchId = sessionContext.currentBranchId();
        if (branchId == null) {
            throw new IllegalStateException("Không xác định được chi nhánh của nhân viên hiện tại.");
        }
        return loadStatistics(fromDate, toDate, branchId, false);
    }

    private StatisticsDashboardData loadStatistics(LocalDate fromDate,
                                                   LocalDate toDate,
                                                   Long branchId,
                                                   boolean includeBranch) {
        LocalDate start = fromDate == null ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate end = toDate == null ? LocalDate.now() : toDate;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
        }

        LocalDateTime fromInclusive = start.atStartOfDay();
        LocalDateTime toExclusive = end.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = defaultAmount(statisticsDao.sumRevenue(fromInclusive, toExclusive, branchId));
        long totalOrders = statisticsDao.countOrders(fromInclusive, toExclusive, branchId);
        long paidOrders = statisticsDao.countPaidOrders(fromInclusive, toExclusive, branchId);
        BigDecimal avgOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP);

        StatisticsKpiItem kpi = new StatisticsKpiItem(totalRevenue, totalOrders, paidOrders, avgOrderValue);

        List<StatisticsTrendPoint> trend = buildTrend(start, end,
                statisticsDao.findDailyRevenue(fromInclusive, toExclusive, branchId),
                statisticsDao.findDailyOrders(fromInclusive, toExclusive, branchId));

        List<StatisticsBreakdownItem> statusBreakdown = statisticsDao
                .findOrderStatusBreakdown(fromInclusive, toExclusive, branchId)
                .stream()
                .map(r -> new StatisticsBreakdownItem(
                        String.valueOf(r[0]),
                        toLong(r[1]),
                        defaultAmount(toBigDecimal(r[2]))
                ))
                .sorted(Comparator.comparingLong(StatisticsBreakdownItem::count).reversed())
                .toList();

        List<StatisticsBreakdownItem> paymentBreakdown = statisticsDao
                .findPaymentMethodBreakdown(fromInclusive, toExclusive, branchId)
                .stream()
                .map(r -> new StatisticsBreakdownItem(
                        String.valueOf(r[0]),
                        toLong(r[1]),
                        defaultAmount(toBigDecimal(r[2]))
                ))
                .sorted(Comparator.comparing(StatisticsBreakdownItem::amount).reversed())
                .toList();

        List<TopCarStatisticsItem> topCars = statisticsDao.findTopCars(fromInclusive, toExclusive, branchId, 8)
                .stream()
                .map(r -> new TopCarStatisticsItem(
                        String.valueOf(r[0]),
                        String.valueOf(r[1]),
                        toLong(r[2]),
                        defaultAmount(toBigDecimal(r[3]))
                ))
                .toList();

        List<BranchStatisticsItem> branches = includeBranch
                ? statisticsDao.findBranchRevenue(fromInclusive, toExclusive, 8)
                .stream()
                .map(r -> new BranchStatisticsItem(
                        String.valueOf(r[0]),
                        String.valueOf(r[1]),
                        toLong(r[2]),
                        defaultAmount(toBigDecimal(r[3]))
                ))
                .toList()
                : List.of();

        return new StatisticsDashboardData(kpi, trend, statusBreakdown, paymentBreakdown, topCars, branches);
    }

    private List<StatisticsTrendPoint> buildTrend(LocalDate from,
                                                  LocalDate to,
                                                  List<Object[]> revenueRows,
                                                  List<Object[]> orderRows) {
        Map<LocalDate, BigDecimal> revenueByDay = revenueRows.stream()
                .collect(Collectors.toMap(
                        r -> toLocalDate(r[0]),
                        r -> defaultAmount(toBigDecimal(r[1])),
                        BigDecimal::add
                ));

        Map<LocalDate, Long> ordersByDay = orderRows.stream()
                .collect(Collectors.toMap(
                        r -> toLocalDate(r[0]),
                        r -> toLong(r[1]),
                        Long::sum
                ));

        long days = to.toEpochDay() - from.toEpochDay() + 1;
        return Stream.iterate(from, d -> d.plusDays(1))
                .limit(days)
                .map(day -> new StatisticsTrendPoint(
                        day,
                        revenueByDay.getOrDefault(day, BigDecimal.ZERO),
                        ordersByDay.getOrDefault(day, 0L)
                ))
                .toList();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return new java.sql.Date(date.getTime()).toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

