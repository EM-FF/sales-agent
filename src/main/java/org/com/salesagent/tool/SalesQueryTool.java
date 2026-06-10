package org.com.salesagent.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.com.salesagent.entity.SalesOrder;
import org.com.salesagent.service.SalesQueryService;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SalesQueryTool {
    private SalesQueryService queryService;

    public SalesQueryTool(SalesQueryService queryService) {
        this.queryService = queryService;
    }

    @Tool("查询原始销售订单数据。适用于：查具体订单、看某时段订单列表、统计某时段订单总数。" +
            "【不适合】排名、增长率、图表生成、异常检测等场景，那些请使用对应的专用工具。")
    public String queryOrders(
        @P("查询开始日期，格式 yyyy-MM-dd，如 2024-12-01") String startDate,
        @P("查询结束日期，格式 yyyy-MM-dd，如 2024-12-31") String endDate,
        @P("大区名称，如：华东区、华南区、西南区，传 null 或空字符串表示查询公司所有地区") String regionName,
        @P("销售员工名字，如需按指定销售人员筛选则传入如：张磊。否则传入null或空字符串") String repName,
        @P("最多返回条数，默认20，最大50，避免返回数据过多") int limit) {

        log.info("工具调用-queryOrders: start={}, end={}, region={}, repName={}, limit={}",
                startDate, endDate, regionName, repName, limit);

        try {
            // 参数转化
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 获取区域id
            Long regionId = null;
            if (regionName != null && !regionName.isBlank()) {
                regionId = queryService.getRegionIdByName(regionName);
                if (regionId == null) {
                    return "未找到大区：" + regionName + "，请确认大区名称是否正确（华东区/华南区/华北区/西南区）";
                }
            }

            // 获取销售员工id
            Long repId = null;
            if (repName != null && !repName.isBlank()) {
                repId = queryService.getRepIdByName(repName);
                if (repId == null) {
                    return "未找到销售员：" + repName + "，请确认姓名是否正确";
                }
            }

            List<SalesOrder> orders = queryService.queryOrders(repId, regionId, start, end);

            if (orders.isEmpty()) {
                return String.format("在 %s 至 %s 期间，%s暂无订单数据",
                        startDate, endDate,
                        regionName != null ? regionName + " " : "");
            }

            // 按limit截断
            if (limit <= 0) limit = 20;
            int actualLimit = Math.min(limit, 50);
            List<SalesOrder> limitedOrders = orders.size() > actualLimit
                    ? orders.subList(0, actualLimit) : orders;

            return formatOrder(limitedOrders, orders.size(), startDate, endDate, regionName);
        }catch (DateTimeException e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式，如：2024-11-01";
        }catch (Exception e) {
            log.error("查询订单失败", e);
            return "查询订单数据时出现问题，请稍后重试";
        }
    }

    private String formatOrder(List<SalesOrder> limitedOrders, int orderSize,
                               String startDate, String endDate, String regionName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("订单查询结果（%s 至 %s%s）：\n",
                startDate, endDate,
                regionName != null ? "，" + regionName : ""));
        sb.append(String.format("共找到 %d 条订单", limitedOrders.size()));
        if (limitedOrders.size() < orderSize) {
            sb.append(String.format("，以下显示 %d 条", limitedOrders.size()));
        }
        sb.append("\n\n");

        // 获取所有员工id
        List<Long> repIds = limitedOrders.stream()
                .map(SalesOrder::getRepId)
                .distinct()
                .collect(Collectors.toList());
        // 批量获取员工名字, 避免n+1查询员工姓名
        Map<Long, String> repNames = queryService.getRepNamesByIds(repIds);

        for (int i = 0; i < limitedOrders.size(); i++){
            SalesOrder order = limitedOrders.get(i);

            // 根据当前订单的repId去字典中查询 员工姓名
            String repName = repNames.getOrDefault(order.getRepId(), "未知员工");

            sb.append(String.format("- 订单号：%s | 日期：%s | 销售员：%s | 客户：%s | 金额：¥%,.0f | 状态：%s\n",
                    order.getOrderNo(),
                    order.getOrderDate(),
                    repName,
                    order.getCustomerName(),
                    order.getAmount(),
                    translateStatus(order.getStatus())));
        }

        // 统计订单成交总金额
        double completedTotal = limitedOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToDouble(o -> o.getAmount().doubleValue())
                .sum();
        // 统计订单数量
        long completedCount = limitedOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();

        sb.append(String.format("\n小计：完成订单 %d 笔，金额合计 ¥%,.0f", completedCount, completedTotal));
        return sb.toString();
    }

    private String translateStatus(String status) {
        return switch (status){
            case "COMPLETED" -> "已完成";
            case "REFUNDED"  -> "已退款";
            case "CANCELLED" -> "已取消";
            default          -> status;
        };
    }
}
