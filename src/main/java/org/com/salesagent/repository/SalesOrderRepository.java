package org.com.salesagent.repository;


import org.com.salesagent.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    // 按销售员查 以及时间区间
    List<SalesOrder> findByRepIdAndOrderDateBetween(Long repId, LocalDate start, LocalDate end);

    // 按大区查 以及时间区间
    List<SalesOrder> findByRegionIdAndOrderDateBetween(Long regionId, LocalDate start, LocalDate end);

    // 按产品查 以及时间区间
    List<SalesOrder> findByProductIdAndOrderDateBetween(Long productId, LocalDate start, LocalDate end);

    // 某大区某时段的完成订单总金额
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM SalesOrder o " +
            "WHERE o.regionId = :regionId AND o.status = 'COMPLETED' " +
            "AND o.orderDate BETWEEN :start AND :end")
    BigDecimal sumAmountByRegion(@Param("regionId") Long regionId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);

    // 某销售员某时段的完成订单总金额
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM SalesOrder o " +
            "WHERE o.repId = :repId AND o.status = 'COMPLETED' " +
            "AND o.orderDate BETWEEN :start AND :end")
    BigDecimal sumAmountByRep(@Param("repId") Long repId,
                              @Param("start") LocalDate start,
                              @Param("end") LocalDate end);

    /**
     * 各销售员业绩排名
     * @param start 起始日期
     * @param end   结束日期
     * @return list<[repId,total]> 销售id和对应个人销售总额
     */
    @Query("SELECT o.repId, SUM(o.amount) AS total FROM SalesOrder o " +
            "WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN :start AND :end " +
            "GROUP BY o.repId ORDER BY total DESC")
    List<Object[]> findRepRanking(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end);

    /**
     * 各大区业绩排名
     * @param start 起始日期
     * @param end   结束日期
     * @return list<[regionId,total]> 地区id，地区销售总额
     */
    @Query("SELECT o.regionId, SUM(o.amount) AS total FROM SalesOrder o " +
            "WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN :start AND :end " +
            "GROUP BY o.regionId ORDER BY total DESC")
    List<Object[]> findRegionRanking(@Param("start") LocalDate start,
                                     @Param("end") LocalDate end);

    /**
     * 各产品销售排名
     * @param start 起始日期
     * @param end   结束日期
     * @return list<[productId,total]> 产品id，产品销售总额 产品库存
     */
    @Query("SELECT o.productId, SUM(o.amount) AS total, SUM(o.quantity) AS qty " +
            "FROM SalesOrder o WHERE o.status = 'COMPLETED' " +
            "AND o.orderDate BETWEEN :start AND :end " +
            "GROUP BY o.productId ORDER BY total DESC")
    List<Object[]> findProductRanking(@Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    //

    /**
     * 地区月度汇总（用于趋势分析）
     * @param regionId 区域id
     * @param start 起始月份
     * @param end 结束月份
     * @return [month, total, order_count] 订单日期Y-m 销售总额 订单数
     */
    @Query(value = "SELECT DATE_FORMAT(order_date, '%Y-%m') AS month, " +
            "SUM(amount) AS total, COUNT(*) AS order_count " +
            "FROM sa_sales_order WHERE status = 'COMPLETED' " +
            "AND (:regionId IS NULL OR region_id = :regionId) " +
            "AND order_date BETWEEN :start AND :end " +
            "GROUP BY month ORDER BY month",
            nativeQuery = true)
    List<Object[]> findMonthlyTrend(@Param("regionId") Long regionId,
                                    @Param("start") LocalDate start,
                                    @Param("end") LocalDate end);


    /**
     * 产品最近一次出单日期（用于预警）
     * @param productId 产品id
     * @return 最近一次产品订单生成时间 orderDate
     */
    @Query("SELECT MAX(o.orderDate) FROM SalesOrder o " +
            "WHERE o.productId = :productId AND o.status = 'COMPLETED'")
    LocalDate findLastOrderDateByProduct(@Param("productId") Long productId);

    /**
     * 某销售员的退单率统计
     * @param start 起始日期
     * @param end 结束日期
     * @return list[repId, refunded_count, order_count] 销售员工id 退单数 销售出的订单数
     */
    @Query("SELECT o.repId, " +
            "SUM(CASE WHEN o.status = 'REFUNDED' THEN 1 ELSE 0 END) AS refunded, " +
            "COUNT(*) AS total " +
            "FROM SalesOrder o WHERE o.orderDate BETWEEN :start AND :end " +
            "GROUP BY o.repId")
    List<Object[]> findRefundRateByRep(@Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    /**
     * 某大区某时段的订单数（用于异常检测）
     * @param regionId 区域id
     * @param start  起始日期
     * @param end 结束日期
     * @return 某地区时间区间内的订单数
     */
    @Query("SELECT COUNT(o) FROM SalesOrder o " +
            "WHERE o.regionId = :regionId AND o.status = 'COMPLETED' " +
            "AND o.orderDate BETWEEN :start AND :end")
    Long countCompletedByRegion(@Param("regionId") Long regionId,
                                @Param("start") LocalDate start,
                                @Param("end") LocalDate end);
}
