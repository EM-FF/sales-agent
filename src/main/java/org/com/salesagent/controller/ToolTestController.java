package org.com.salesagent.controller;

import lombok.RequiredArgsConstructor;
import org.com.salesagent.tool.SalesQueryTool;
import org.com.salesagent.tool.SalesSummaryTool;
import org.com.salesagent.tool.SalesTrendTool;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/tool")
public class ToolTestController {
    private final SalesQueryTool salesQueryTool;
    private final SalesSummaryTool salesSummaryTool;
    private final SalesTrendTool salesTrendTool;

    public ToolTestController(SalesQueryTool salesQueryTool,
                              SalesSummaryTool salesSummaryTool,
                              SalesTrendTool salesTrendTool) {
        this.salesQueryTool = salesQueryTool;
        this.salesSummaryTool = salesSummaryTool;
        this.salesTrendTool = salesTrendTool;
    }

    record QueryRequest(String startDate, String endDate,
                        String regionName, String repName, int limit) {}

    // 工具1
    @PostMapping("/query-orders")
    public String queryOrders(@RequestBody QueryRequest req) {
        return salesQueryTool.queryOrders(req.startDate(), req.endDate(),
                req.regionName(), req.repName(), req.limit());
    }

    // 工具2
    record RepRankRequest(String startDate, String endDate, String regionName, int topN){}
    record RegionRankRequest(String startDate, String endDate){}
    record ProductRankRequest(String startDate, String endDate, int topN){}


    @PostMapping("top-reps")
    public String topReps(@RequestBody RepRankRequest req) {
        return salesSummaryTool.getTopReps(req.startDate(), req.endDate(), req.regionName(), req.topN());
    }

    @PostMapping("top-regions")
    public String topRegions(@RequestBody RegionRankRequest req) {
        return salesSummaryTool.getRegionRanking(req.startDate(), req.endDate());
    }

    @PostMapping("top-products")
    public String topProducts(@RequestBody ProductRankRequest req) {
        return salesSummaryTool.getTopProducts(req.startDate(), req.endDate(), req.topN());
    }


    // -------- 工具三 --------
    record MomRequest(String currentStart, String currentEnd,
                      String prevStart, String prevEnd, String regionName) {}
    record YoyRequest(String startDate, String endDate, String regionName) {}
    record TrendRequest(int months, String regionName) {}

    @PostMapping("/month-over-month")
    public String monthOverMonth(@RequestBody MomRequest req) {
        return salesTrendTool.calcMonthOverMonth(
                req.currentStart(), req.currentEnd(),
                req.prevStart(), req.prevEnd(), req.regionName());
    }

    @PostMapping("/year-over-year")
    public String yearOverYear(@RequestBody YoyRequest req) {
        return salesTrendTool.calcYearOverYear(
                req.startDate(), req.endDate(), req.regionName());
    }

    @PostMapping("/monthly-trend")
    public String monthlyTrend(@RequestBody TrendRequest req) {
        return salesTrendTool.getMonthlyTrend(req.months(), req.regionName());
    }



}
