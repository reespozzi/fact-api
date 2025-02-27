package uk.gov.hmcts.dts.fact.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.dts.fact.entity.AreaOfLaw;

public interface AreasOfLawRepository extends JpaRepository<AreaOfLaw, Integer> {
}
