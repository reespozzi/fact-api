package uk.gov.hmcts.dts.fact.services.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dts.fact.entity.AreaOfLaw;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.entity.CourtLocalAuthorityAreaOfLaw;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.admin.LocalAuthority;
import uk.gov.hmcts.dts.fact.repositories.CourtLocalAuthorityAreaOfLawRepository;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;
import uk.gov.hmcts.dts.fact.repositories.LocalAuthorityRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AdminCourtLocalAuthoritiesService {

    private final LocalAuthorityRepository localAuthorityRepository;
    private final CourtRepository courtRepository;
    private final CourtLocalAuthorityAreaOfLawRepository courtLocalAuthorityAreaOfLawRepository;


    @Autowired
    public AdminCourtLocalAuthoritiesService(final LocalAuthorityRepository localAuthorityRepository, final CourtRepository courtRepository, final CourtLocalAuthorityAreaOfLawRepository courtLocalAuthorityAreaOfLawRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
        this.courtRepository = courtRepository;
        this.courtLocalAuthorityAreaOfLawRepository = courtLocalAuthorityAreaOfLawRepository;
    }

    public List<LocalAuthority> getAllLocalAuthorities() {
        return localAuthorityRepository.findAll()
            .stream()
            .map(LocalAuthority::new)
            .collect(toList());
    }

    public List<LocalAuthority> getCourtLocalAuthoritiesBySlugAndAreaOfLaw(final String slug, final String areaOfLaw) {

        final Court courtEntity = courtRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(slug));

        return courtLocalAuthorityAreaOfLawRepository.findByCourtId(courtEntity.getId())
                .stream()
                .filter(c -> c.getAreaOfLaw().getName().equals(areaOfLaw))
                .map(la -> new LocalAuthority(la.getLocalAuthority().getId(), la.getLocalAuthority().getName()))
                .collect(toList());
    }

    @Transactional(rollbackFor = {RuntimeException.class})
    public List<LocalAuthority> updateCourtLocalAuthority(final String slug, final String areaOfLaw, final List<LocalAuthority> localAuthorities) {

        final Court courtEntity = courtRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(slug));

        final AreaOfLaw areaOfLawEntity = courtEntity.getAreasOfLaw()
            .stream()
            .filter(al -> al.getName().equals(areaOfLaw)).findFirst()
            .orElseThrow(() -> new NotFoundException(areaOfLaw));

        return saveNewCourtLocalAuthorities(courtEntity, areaOfLawEntity, localAuthorities);
    }

    protected List<LocalAuthority> saveNewCourtLocalAuthorities(final Court courtEntity, final AreaOfLaw areaOflaw, final List<LocalAuthority> localAuthorities) {

        final List<CourtLocalAuthorityAreaOfLaw> courtLocalAuthorityAreaOfLawEntities = getNewCourtLocalAuthorities(courtEntity, areaOflaw, localAuthorities);
        final List<CourtLocalAuthorityAreaOfLaw> existingCourtLocalAuthorities = getExistingCourtLocalAuthorities(courtEntity, areaOflaw);

        //delete existing Court Local Authorities for Area of Law
        courtLocalAuthorityAreaOfLawRepository.deleteAll(existingCourtLocalAuthorities);

        return courtLocalAuthorityAreaOfLawRepository
            .saveAll(courtLocalAuthorityAreaOfLawEntities)
            .stream()
            .map(la -> new LocalAuthority(la.getLocalAuthority().getId(), la.getLocalAuthority().getName()))
            .collect(toList());

    }

    private List<CourtLocalAuthorityAreaOfLaw> getNewCourtLocalAuthorities(final Court courtEntity, final AreaOfLaw areaOflaw, final List<LocalAuthority> localAuthorities) {
        return localAuthorities.stream()
            .map(o -> new CourtLocalAuthorityAreaOfLaw(areaOflaw, courtEntity, new uk.gov.hmcts.dts.fact.entity.LocalAuthority(o.getId(), o.getName())))
            .collect(toList());
    }

    private List<CourtLocalAuthorityAreaOfLaw> getExistingCourtLocalAuthorities(final Court courtEntity, final AreaOfLaw areaOflaw) {
        return courtLocalAuthorityAreaOfLawRepository.findByCourtId(courtEntity.getId())
            .stream()
            .filter(c -> c.getAreaOfLaw().equals(areaOflaw))
            .collect(toList());
    }

}
