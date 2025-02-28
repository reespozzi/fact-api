package uk.gov.hmcts.dts.fact.services.admin;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.entity.Audit;
import uk.gov.hmcts.dts.fact.entity.AuditType;
import uk.gov.hmcts.dts.fact.repositories.AuditRepository;
import uk.gov.hmcts.dts.fact.repositories.AuditTypeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = AdminAuditService.class)
public class AdminAuditServiceTest {

    @Autowired
    private AdminAuditService adminAuditService;

    @MockBean
    private AuditRepository auditRepository;

    @MockBean
    private AuditTypeRepository auditTypeRepository;

    private static final List<Audit> AUDIT_DATA = new ArrayList<>();
    private static final String TEST_LOCATION = "mosh court";
    private static final String TEST_EMAIL = "kupo email";

    @BeforeAll
    static void beforeAll() {
        AUDIT_DATA.add(new Audit("Test String", new AuditType(1, "test"),
                               "data before", "data after",
                               "some court", LocalDateTime.of(1000, 10, 10, 10, 10)));
        AUDIT_DATA.add(new Audit("Test String 2", new AuditType(1, "test 2"),
                               "data before 2", "data after 2",
                               "some court", LocalDateTime.of(1000, 10, 10, 10, 10)));
        AUDIT_DATA.get(0).setId(0);
        AUDIT_DATA.get(1).setId(1);
    }

    @Test
    void shouldGetAllAuditDataNoDateRange() {
        when(auditRepository.findAllByLocationContainingAndUserEmailContaining(
            TEST_LOCATION, TEST_EMAIL, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(AUDIT_DATA));

        final List<uk.gov.hmcts.dts.fact.model.admin.Audit> results =
            adminAuditService.getAllAuditData(0, 10,
                                              Optional.of(TEST_LOCATION), Optional.of(TEST_EMAIL),
                                              Optional.empty(), Optional.empty());

        verify(auditRepository, atLeastOnce()).findAllByLocationContainingAndUserEmailContaining(
            TEST_LOCATION, TEST_EMAIL, PageRequest.of(0, 10));
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.Audit(AUDIT_DATA.get(0)));
        assertThat(results.get(1)).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.Audit(AUDIT_DATA.get(1)));
    }

    @Test
    void shouldGetAllAuditDataWithDateRange() {
        final LocalDateTime dateFrom = LocalDateTime.of(1000, 10, 10, 10, 10);
        final LocalDateTime dateTo = LocalDateTime.of(1000, 12, 10, 10, 10);
        when(auditRepository.findAllByLocationContainingAndUserEmailContainingAndCreationTimeBetween(
            TEST_LOCATION, TEST_EMAIL, dateFrom, dateTo,
            PageRequest.of(0, 10))).thenReturn(new PageImpl<>(AUDIT_DATA));

        final List<uk.gov.hmcts.dts.fact.model.admin.Audit> results =
            adminAuditService.getAllAuditData(0, 10,
                                              Optional.of(TEST_LOCATION), Optional.of(TEST_EMAIL),
                                              Optional.of(dateFrom), Optional.of(dateTo));

        verify(auditRepository, atLeastOnce()).findAllByLocationContainingAndUserEmailContainingAndCreationTimeBetween(
            TEST_LOCATION, TEST_EMAIL, dateFrom, dateTo, PageRequest.of(0, 10));
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.Audit(AUDIT_DATA.get(0)));
        assertThat(results.get(1)).isEqualTo(new uk.gov.hmcts.dts.fact.model.admin.Audit(AUDIT_DATA.get(1)));
    }

    @Test
    void shouldSaveAudit() {
        when(auditTypeRepository.findByName(anyString())).thenReturn(new AuditType(1, "test type"));
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        adminAuditService.saveAudit("test type", AUDIT_DATA, emptyList(), "some court");

        verify(auditRepository, atLeastOnce()).save(any(Audit.class));
    }
}
