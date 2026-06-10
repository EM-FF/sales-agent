package org.com.salesagent;

import org.com.salesagent.entity.SalesRegion;
import org.com.salesagent.repository.SalesRegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;

// 核心：在测试环境强行把延迟初始化关掉，让数据在测试前 100% 灌入完毕
@SpringBootTest(properties = "spring.jpa.defer-datasource-initialization=false")
public class RepositoryTest {

    @Autowired
    private SalesRegionRepository salesRegionRepository;

    @Test
    public void testFindByName() {
        System.out.println("=========================================");
        System.out.println("【时序同步测试】开始直接测试 SalesRegionRepository...");

        String targetName = "华东区";
        Optional<SalesRegion> result = salesRegionRepository.findByName(targetName);

        if (result.isPresent()) {
            SalesRegion region = result.get();
            System.out.println("【🎉🎉🎉 惊天逆转，成功命中！】");
            System.out.println("大区 ID: " + region.getId() + " | 名称: " + region.getName());
        } else {
            System.out.println("【❌ 依然失败】说明 data.sql 的插入语句中，‘华东区’的拼写可能和这里不一致。");
        }
        System.out.println("=========================================");
    }
}