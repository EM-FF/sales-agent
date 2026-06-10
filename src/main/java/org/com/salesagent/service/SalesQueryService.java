package org.com.salesagent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.com.salesagent.dto.MonthlyTrendDTO;
import org.com.salesagent.dto.ProductSalesDTO;
import org.com.salesagent.dto.RegionSalesDTO;
import org.com.salesagent.dto.RepSalesDTO;
import org.com.salesagent.entity.Product;
import org.com.salesagent.entity.SalesOrder;
import org.com.salesagent.entity.SalesRegion;
import org.com.salesagent.entity.SalesRep;
import org.com.salesagent.repository.ProductRepository;
import org.com.salesagent.repository.SalesOrderRepository;
import org.com.salesagent.repository.SalesRegionRepository;
import org.com.salesagent.repository.SalesRepRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesQueryService {
    private final SalesOrderRepository orderRepository;
    private final SalesRepRepository repRepository;
    private final SalesRegionRepository regionRepository;
    private final ProductRepository productRepository;

    // ============================================================
    // 基础查询
    // ============================================================

    /**
     * 查询指定时段的订单列表
     * 权限过滤由调用方（工具层）传入 regionId / repId 参数决定
     */
    public List<SalesOrder> queryOrders(Long repId, Long regionId,
                                        LocalDate start, LocalDate end) {
        if (repId != null){
            return orderRepository.findByRepIdAndOrderDateBetween(repId, start, end);
        }
        if (regionId != null){
            return orderRepository.findByRegionIdAndOrderDateBetween(regionId, start, end);
        }
        // 全量查询（只有 SALES_DIRECTOR 角色会走到这里）
        return orderRepository.findAll().stream()
                .filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end))
                .collect(Collectors.toList());
    }
    /**
     * 查询总销售额
     */
    public BigDecimal queryTotalAmount(Long regionId, LocalDate start, LocalDate end) {
        // 查询地区的销售总额
        if (regionId != null){
            return orderRepository.sumAmountByRegion(regionId, start, end);
        }
        // 所有地区销售总额
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus().equals("COMPLETED"))
                .filter(o -> o.getOrderDate().isBefore(start) && o.getOrderDate().isAfter(end))
                .map(SalesOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // ============================================================
    // 排名查询
    // ============================================================

    /**
     * 销售员业绩排名（带姓名、大区信息）
     */
    public List<RepSalesDTO> queryRepRank(LocalDate start, LocalDate end, int topN) {
        // 批量查询所有销售的数据，避免每次查一个用户，查询n次
        List<Object[]> raw = orderRepository.findRepRanking(start, end);

        // 批量所有查询销售员的信息
        Map<Long, SalesRep> repMap = repRepository.findAll().stream()
                .collect(Collectors.toMap(SalesRep::getId, r->r));
        Map<Long ,String> regionNameMap = regionRepository.findAll().stream()
                .collect(Collectors.toMap(r->r.getId(), r->r.getName()));

        List<RepSalesDTO> result = new ArrayList<>();
        for(Object[] row: raw) {
            Long repId = (Long) row[0];
            BigDecimal total = (BigDecimal) row[1];
            SalesRep rep = repMap.get(repId);
            if (rep == null) {continue;}

            String regionName = regionNameMap.getOrDefault(rep.getId(), "未知");

            // orderCount需要单独排查，简化处理 0
            result.add(new RepSalesDTO(repId, rep.getName(), rep.getRegionId(),
                    regionName, total, 0));

            if (result.size() >= topN) {break;}
        }
        return result;
    }

    /**
     * 大区业绩排名
     */
    public List<RegionSalesDTO> queryRegionRank(LocalDate start, LocalDate end) {
        // 返回regionId和 地区销售总额total
        List<Object[]> raw = orderRepository.findRegionRanking(start, end);

        // 查询所有地区信息 name id
        Map<Long, String> regionNameMap = regionRepository.findAll().stream().
                collect(Collectors.toMap(r->r.getId(), r->r.getName()));

        return raw.stream().map(row -> {
            Long regionId = (Long) row[0];
            BigDecimal total = (BigDecimal) row[1];
            String regionName = regionNameMap.getOrDefault(regionId, "未知");
            return new RegionSalesDTO(regionId, regionName, total, 0, BigDecimal.ZERO);
        }).collect(Collectors.toList());
    }

    /**
     * 产品销售排名
     */
    public List<ProductSalesDTO> queryProductRank(LocalDate start, LocalDate end, int topN) {
        // 查询结果[productId, total, quantity] 产品id，产品总销售额 库存
        List<Object[]> productRanking = orderRepository.findProductRanking(start, end);

        // 查询所有商品的信息 productId product
        Map<Long, Product> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getId(), p->p));

        List<ProductSalesDTO> result = new ArrayList<>();
        for(Object[] row: productRanking) {
            Long productId = ((Number) row[0]).longValue();
            BigDecimal total = new BigDecimal(row[1].toString());
            Integer quantity = ((Number) row[2]).intValue();

            Product product = productMap.get(productId);
            if (product == null) {continue;}
            result.add(new ProductSalesDTO(productId, product.getSkuCode(), product.getName(),
                    product.getCategory(), total, quantity));

            if(result.size() >= topN){break;}
        }
        return result;
    }

    // ============================================================
    // 趋势分析
    // ============================================================

    /**
     * 月度趋势数据（近 N 个月）
     */
    public List<MonthlyTrendDTO> queryMonthlyTrend(Long regionId, int months){
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months).withDayOfMonth(1);

        List<Object[]> raw = orderRepository.findMonthlyTrend(regionId, start, end);

        return raw.stream().map(row -> new MonthlyTrendDTO(
                row[0].toString(),
                (BigDecimal) row[1],
                (Integer) row[2]
        )).collect(Collectors.toList());
    }

    /**
     * 计算环比增长率（当期 vs 上期）
     */
    public BigDecimal calcGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null; // 上期为零，无法计算
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ============================================================
    // 异常检测辅助
    // ============================================================

    /**
     * 查询产品最后一次出单日期
     */
    public LocalDate queryLastOrderDate(Long productId) {
        return orderRepository.findLastOrderDateByProduct(productId);
    }

    /**
     * 查询大区在指定时段内的订单数
     */
    public Long queryOrderCount(Long regionId, LocalDate start, LocalDate end) {
        return orderRepository.countCompletedByRegion(regionId, start, end);
    }

    /**
     * 查询所有销售员退单率
     */
    public List<Object[]> queryRefundRates(LocalDate start, LocalDate end) {
        return orderRepository.findRefundRateByRep(start, end);
    }

    // ============================================================
    // 辅助查询（名称解析）
    // ============================================================

    /**
     * 根据员工id查询员工名字
     * @param repId 员工id
     * @return 员工名字
     */
    public String getRepName(Long repId) {
        return repRepository.findById(repId)
                .map(SalesRep::getName)
                .orElse("未知员工");
    }

    /**
     * 根据区域id查询区域名称
     * @param regionId 区域id
     * @return 区域名称
     */
    public String getRegionName(Long regionId) {
        return regionRepository.findById(regionId)
                .map(SalesRegion::getName)
                .orElse("未知区域");
    }

    /**
     * 根据区域名称获取区域id
     * @param regionName 区域名称
     * @return 区域id
     */
    public Long getRegionIdByName(String regionName) {
        return regionRepository.findByName(regionName)
                .map(SalesRegion::getId)
                .orElse(null);
    }

    /**
     * 根据员工名字获取员工id
     * @param repName 员工名字
     * @return 员工id
     */
    public Long getRepIdByName(String repName) {
        return repRepository.findByName(repName)
                .map(SalesRep::getId)
                .orElse(null);
    }

    public Map<Long, String> getRepNamesByIds(List<Long> repIds) {
        return repRepository.findAllById(repIds)
                .stream()
                .collect(Collectors.toMap(SalesRep::getId, r->r.getName()));
    }


}
