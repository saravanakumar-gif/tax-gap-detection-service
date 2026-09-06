package com.taxcompliance.repository;

import com.taxcompliance.entity.ComplianceRule;
import com.taxcompliance.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, UUID> {

    List<ComplianceRule> findByEnabledTrueOrderByRuleCodeAsc();

    Optional<ComplianceRule> findByRuleType(RuleType ruleType);
}
