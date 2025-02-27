package uk.gov.hmcts.dts.fact.services.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dts.fact.config.security.RolesProvider;
import uk.gov.hmcts.dts.fact.entity.CourtOpeningTime;
import uk.gov.hmcts.dts.fact.entity.InPerson;
import uk.gov.hmcts.dts.fact.entity.OpeningTime;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.model.CourtForDownload;
import uk.gov.hmcts.dts.fact.model.CourtReference;
import uk.gov.hmcts.dts.fact.model.admin.Court;
import uk.gov.hmcts.dts.fact.model.admin.CourtInfoUpdate;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;
import uk.gov.hmcts.dts.fact.util.AuditType;
import uk.gov.hmcts.dts.fact.util.RepoUtils;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class AdminService {

    private final CourtRepository courtRepository;
    private final RolesProvider rolesProvider;
    private final AdminAuditService adminAuditService;

    @Autowired
    public AdminService(final CourtRepository courtRepository,
                        final RolesProvider rolesProvider,
                        final AdminAuditService adminAuditService) {
        this.courtRepository = courtRepository;
        this.rolesProvider = rolesProvider;
        this.adminAuditService = adminAuditService;
    }

    public List<CourtReference> getAllCourtReferences() {
        return courtRepository
            .findAll()
            .stream()
            .map(CourtReference::new)
            .collect(toList());
    }

    public List<CourtForDownload> getAllCourtsForDownload() {
        return courtRepository
            .findAll()
            .stream()
            .map(CourtForDownload::new)
            .sorted(Comparator.comparing(CourtForDownload::getName))
            .collect(toList());
    }

    public Court getCourtBySlug(String slug) {
        return courtRepository
            .findBySlug(slug)
            .map(Court::new)
            .orElseThrow(() -> new NotFoundException(slug));
    }

    public uk.gov.hmcts.dts.fact.entity.Court getCourtEntityBySlug(String slug) {
        return courtRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException(slug));
    }

    public Court save(String slug, Court court) {
        uk.gov.hmcts.dts.fact.entity.Court courtEntity = getCourtEntityBySlug(slug);
        courtEntity.setAlert(court.getAlert());
        courtEntity.setAlertCy(court.getAlertCy());

        if (rolesProvider.getRoles().contains("fact-super-admin")) {
            courtEntity.setDisplayed(court.getOpen());
            courtEntity.setInfo(court.getInfo());
            courtEntity.setInfoCy(court.getInfoCy());
            if (courtEntity.getInPerson() != null && courtEntity.getInPerson().getIsInPerson()) {
                courtEntity.getInPerson().setAccessScheme(court.getAccessScheme());
            }
        }

        final List<OpeningTime> openingTimes =
            ofNullable(court.getOpeningTimes()).stream()
                .flatMap(Collection::stream)
                .map(o -> new OpeningTime(o.getType(), o.getTypeCy(), o.getHours()))
                .collect(toList());

        List<CourtOpeningTime> courtOpeningTimes = new ArrayList<>();
        for (int i = 0; i < openingTimes.size(); i++) {
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
            CourtOpeningTime courtOpeningTime = new CourtOpeningTime();
            courtOpeningTime.setCourt(courtEntity);
            courtOpeningTime.setOpeningTime(openingTimes.get(i));
            courtOpeningTime.setSort(i);
            courtOpeningTimes.add(courtOpeningTime);
        }
        if (courtEntity.getCourtOpeningTimes() == null) {
            courtEntity.setCourtOpeningTimes(courtOpeningTimes);
        } else {
            courtEntity.getCourtOpeningTimes().clear();
            courtEntity.getCourtOpeningTimes().addAll(courtOpeningTimes);
        }

        uk.gov.hmcts.dts.fact.entity.Court updatedCourt = courtRepository.save(courtEntity);
        Court updatedCourtModel = new Court(updatedCourt);
        adminAuditService.saveAudit(
            AuditType.findByName("Update court details"),
            court,
            updatedCourtModel, slug);
        return updatedCourtModel;
    }

    @Transactional
    public void updateMultipleCourtsInfo(CourtInfoUpdate info) {
        courtRepository.updateInfoForSlugs(info.getCourts(), info.getInfo(), info.getInfoCy());
    }

    @Transactional
    public void updateCourtLatLon(final String slug, final Double lat, final Double lon) {
        courtRepository.updateLatLonBySlug(slug, lat, lon);
    }

    public String getCourtImage(final String slug) {
        final uk.gov.hmcts.dts.fact.entity.Court court =
            courtRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException(slug));
        return court.getImageFile();
    }

    @Transactional
    public String updateCourtImage(final String slug, final String imageFile) {
        final Optional<uk.gov.hmcts.dts.fact.entity.Court> court =
            courtRepository.findBySlug(slug);
        if (court.isEmpty()) {
            throw new NotFoundException(slug);
        }
        return courtRepository.updateCourtImageBySlug(slug, imageFile);
    }

    @Transactional
    public Court addNewCourt(String newCourtName, String newCourtSlug, Boolean serviceCentre,
                             double lon, double lat) {
        RepoUtils.checkIfCourtAlreadyExists(courtRepository, newCourtSlug);
        uk.gov.hmcts.dts.fact.entity.Court newCourt =
            new uk.gov.hmcts.dts.fact.entity.Court();
        newCourt.setName(newCourtName);
        newCourt.setSlug(newCourtSlug);
        newCourt.setDisplayed(true);
        newCourt.setHideAols(false);
        newCourt.setWelshEnabled(true);
        newCourt.setLon(lon);
        newCourt.setLat(lat);

        // By default the court will be in person unless the "service centre" flag is ticked
        // in which case we can skip this, as it will then default to false
        InPerson inPerson = new InPerson();
        inPerson.setIsInPerson(!serviceCentre);
        inPerson.setAccessScheme(false);

        // Save the court to generate the ID and then link the search_court and search_inperson tables
        // together through the court_id column relationship
        inPerson.setCourtId(courtRepository.save(newCourt));
        newCourt.setInPerson(inPerson);

        Court createdCourtModel = new Court(courtRepository.save(newCourt));

        adminAuditService.saveAudit(
            AuditType.findByName("Create new court"),
            null,
            createdCourtModel, newCourtSlug);

        return createdCourtModel;
    }

    @Transactional
    public void deleteCourt(String courtSlug) {
        uk.gov.hmcts.dts.fact.entity.Court court =
            courtRepository.findBySlug(courtSlug).orElseThrow(() -> new NotFoundException(courtSlug));
        adminAuditService.saveAudit(AuditType.findByName("Delete existing court"), new Court(court),
                                    null, courtSlug);
        courtRepository.deleteById(court.getId());
    }
}
