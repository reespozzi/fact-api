package uk.gov.hmcts.dts.fact.services.admin;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.dts.fact.entity.Court;
import uk.gov.hmcts.dts.fact.exception.NotFoundException;
import uk.gov.hmcts.dts.fact.mapit.MapitData;
import uk.gov.hmcts.dts.fact.model.admin.CourtAddress;
import uk.gov.hmcts.dts.fact.repositories.CourtAddressRepository;
import uk.gov.hmcts.dts.fact.repositories.CourtRepository;
import uk.gov.hmcts.dts.fact.services.MapitService;
import uk.gov.hmcts.dts.fact.services.admin.list.AdminAddressTypeService;
import uk.gov.hmcts.dts.fact.services.validation.ValidationService;
import uk.gov.hmcts.dts.fact.util.AddressType;
import uk.gov.hmcts.dts.fact.util.AuditType;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class AdminCourtAddressService {
    private final CourtRepository courtRepository;
    private final CourtAddressRepository courtAddressRepository;
    private final AdminAddressTypeService addressTypeService;
    private final AdminService adminService;
    private final MapitService mapitService;
    private final ValidationService validationService;
    private final AdminAuditService adminAuditService;

    @Autowired
    public AdminCourtAddressService(final CourtRepository courtRepository,
                                    final CourtAddressRepository courtAddressRepository,
                                    final AdminAddressTypeService addressTypeService,
                                    final AdminService adminService,
                                    final MapitService mapitService,
                                    final ValidationService validationService,
                                    final AdminAuditService adminAuditService) {
        this.courtRepository = courtRepository;
        this.courtAddressRepository = courtAddressRepository;
        this.addressTypeService = addressTypeService;
        this.adminService = adminService;
        this.mapitService = mapitService;
        this.validationService = validationService;
        this.adminAuditService = adminAuditService;
    }

    public List<CourtAddress> getCourtAddressesBySlug(final String slug) {
        return courtRepository.findBySlug(slug)
            .map(c -> c.getAddresses()
                .stream()
                .sorted(Comparator.comparingInt(a -> AddressType.isCourtAddress(a.getAddressType().getName()) ? 0 : 1))
                .map(CourtAddress::new)
                .collect(toList()))
            .orElseThrow(() -> new NotFoundException(slug));
    }

    @Transactional()
    public List<CourtAddress> updateCourtAddressesAndCoordinates(final String slug, final List<CourtAddress> courtAddresses) {
        // Update the in-person court with its primary postcode coordinates
        final List<String> postcodes = getAllPostcodesSortedByAddressType(courtAddresses);
        if (isInPersonCourt(slug) && !CollectionUtils.isEmpty(postcodes)) {
            final String primaryPostcode = postcodes.get(0);
            if (StringUtils.isNotBlank(primaryPostcode)) {
                updateCourtLatLonUsingPrimaryPostcode(slug, primaryPostcode);
            }
        }

        final Court courtEntity = courtRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(slug));

        final List<uk.gov.hmcts.dts.fact.entity.CourtAddress> newCourtAddressesEntity = constructCourtAddressesEntity(courtEntity, courtAddresses);
        courtAddressRepository.deleteAll(courtEntity.getAddresses());

        List<CourtAddress> updatedAddresses = courtAddressRepository.saveAll(newCourtAddressesEntity)
            .stream()
            .sorted(Comparator.comparingInt(a -> AddressType.isCourtAddress(a.getAddressType().getName()) ? 0 : 1))
            .map(CourtAddress::new)
            .collect(toList());
        adminAuditService.saveAudit(
            AuditType.findByName("Update court addresses and coordinates"),
            courtEntity.getAddresses()
                .stream()
                .map(CourtAddress::new)
                .collect(toList()),
            updatedAddresses, slug);
        return updatedAddresses;
    }

    public List<String> validateCourtAddressPostcodes(final String slug, final List<CourtAddress> courtAddresses) {
        if (!CollectionUtils.isEmpty(courtAddresses)) {
            final List<String> allPostcodes = getAllPostcodesSortedByAddressType(courtAddresses);

            if (!CollectionUtils.isEmpty(allPostcodes)) {
                return validationService.validateFullPostcodes(allPostcodes);
            }
        }
        return emptyList();
    }

    private List<String> getAllPostcodesSortedByAddressType(final List<CourtAddress> courtAddresses) {
        final Map<Integer, uk.gov.hmcts.dts.fact.entity.AddressType> addressTypeMap = addressTypeService.getAddressTypeMap();
        return courtAddresses.stream()
            .sorted(Comparator.comparingInt(a -> AddressType.isCourtAddress(getAddressTypeFromId(addressTypeMap, a.getAddressTypeId())) ? 0 : 1))
            .map(CourtAddress::getPostcode)
            .filter(StringUtils::isNotBlank)
            .collect(toList());
    }

    private String getAddressTypeFromId(final Map<Integer, uk.gov.hmcts.dts.fact.entity.AddressType> map, final Integer addressTypeId) {
        if (!map.containsKey(addressTypeId)) {
            throw new IllegalArgumentException("Unknown address type ID: " + addressTypeId);
        }
        return map.get(addressTypeId).getName();
    }

    private boolean isInPersonCourt(final String slug) {
        final Court courtEntity = courtRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException(slug));
        return courtEntity.isInPerson();
    }

    private void updateCourtLatLonUsingPrimaryPostcode(final String slug, final String postcode) {
        final Optional<MapitData> mapitData = mapitService.getMapitData(postcode);
        if (mapitData.isPresent()) {
            adminService.updateCourtLatLon(slug, mapitData.get().getLat(), mapitData.get().getLon());
        }
    }

    private List<uk.gov.hmcts.dts.fact.entity.CourtAddress> constructCourtAddressesEntity(final Court court, final List<CourtAddress> courtAddresses) {
        final Map<Integer, uk.gov.hmcts.dts.fact.entity.AddressType> addressTypeMap = addressTypeService.getAddressTypeMap();
        return courtAddresses.stream()
            .map(a -> new uk.gov.hmcts.dts.fact.entity.CourtAddress(court,
                                                                    addressTypeMap.get(a.getAddressTypeId()),
                                                                    a.getAddressLines(),
                                                                    a.getAddressLinesCy(),
                                                                    a.getTownName(),
                                                                    a.getTownNameCy(),
                                                                    a.getPostcode()))
            .sorted(Comparator.comparingInt(a -> AddressType.isCourtAddress(a.getAddressType().getName()) ? 0 : 1))
            .collect(toList());
    }
}
