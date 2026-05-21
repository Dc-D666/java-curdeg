package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PointRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, Long> {

    Optional<PointRule> findByRuleCode(String ruleCode);

    List<PointRule> findByEnabledOrderByIdAsc(Boolean enabled);

    Boolean existsByRuleCode(String ruleCode);
}
