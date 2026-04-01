package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

public record StatisticsDashboardData(
		StatisticsKpiItem kpi,
		List<StatisticsTrendPoint> trend,
		List<StatisticsBreakdownItem> orderStatusBreakdown,
		List<StatisticsBreakdownItem> paymentMethodBreakdown,
		List<TopCarStatisticsItem> topCars,
		List<BranchStatisticsItem> branchStatistics
) {
	public static StatisticsDashboardData empty() {
		return new StatisticsDashboardData(
				StatisticsKpiItem.empty(),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);
	}
}

