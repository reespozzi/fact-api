package uk.gov.hmcts.dts.fact.admin.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.util.AdminFunctionalTestBase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.dts.fact.util.TestUtil.BEARER;
import static uk.gov.hmcts.dts.fact.util.TestUtil.objectMapper;

@SuppressWarnings("PMD.TooManyMethods")
public class AdminAreasOfLawEndpointTest extends AdminFunctionalTestBase {

    private static final String ADMIN_AREAS_OF_LAW_ENDPOINT = "/admin/areasOfLaw/";

    private static final int AREA_OF_LAW_ID_HOUSING_POSSESSION = 34_253;
    private static final int AREA_OF_LAW_ID_NOT_FOUND = 342_053;
    private static final String HOUSING_POSSESSION_NAME = "Housing possession";
    private static final String TEST_ALT_NAME = "Housing1234";
    private static final String TEST_ALT_NAME_CY = "Tai";
    private static final String TEST_DISPLAY_NAME = "Housing234";
    private static final String TEST_DISPLAY_NAME_CY = "Tai1234";
    private static final Boolean TEST_SINGLE_POINT_OF_ENTRY = false;
    private static final String TEST_EXTERNAL_LINK = "https%3A//www.gov.uk/evicting-tenants";
    private static final String TEST_EXTERNAL_LINK_DESC = "Information about evicting tenants";
    private static final String TEST_EXTERNAL_LINK_DESC_CY = "Gwybodaeth ynglŷn â troi tenantiaid allan";
    private static final String TEST_DISPLAY_EXTERNAL_LINK = null;
    private static final int AREA_OF_LAW_NEW_TEST_ID = 34_258;
    private static final String AREA_OF_LAW_NEW_TEST_NAME = "TEST";

    /************************************************************* Get Request Tests. ***************************************************************/

    @Test
    public void shouldReturnAllAreasOfLaw() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        assertThat(response.statusCode()).isEqualTo(OK.value());

        final List<AreaOfLaw> courtAreasOfLaw = response.body().jsonPath().getList(".", AreaOfLaw.class);
        assertThat(courtAreasOfLaw).hasSizeGreaterThan(1);
    }

    @Test
    public void shouldRequireATokenWhenGettingAllCourtAreasOfLaw() {
        final Response response = doGetRequest(ADMIN_AREAS_OF_LAW_ENDPOINT);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldBeForbiddenForGettingAllCourtAreasOfLaw() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken)
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldReturnAreaOfLaw() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        assertThat(response.statusCode()).isEqualTo(OK.value());
        final AreaOfLaw areaOfLaw = response.as(AreaOfLaw.class);
        assertThat(areaOfLaw).isNotNull();
        assertThat(areaOfLaw.getId()).isEqualTo(AREA_OF_LAW_ID_HOUSING_POSSESSION);
    }

    @Test
    public void shouldRequireATokenWhenGettingAreaOfLaw() {
        final Response response = doGetRequest(ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldBeForbiddenForGettingAreaOfLaw() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken)
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldReturnAreaOfLawNotFound() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + "12345",
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        assertThat(response.statusCode()).isEqualTo(NOT_FOUND.value());
    }

    /************************************************************* Update Request Tests. ***************************************************************/

    @Test
    public void shouldUpdateAreaOfLaw() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final AreaOfLaw expectedAreaOfLaw = getUpdatedHousingPossessionAreaOfLaw();
        final String updatedJson = objectMapper().writeValueAsString(expectedAreaOfLaw);
        final String originalJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            updatedJson
        );

        assertThat(response.statusCode()).isEqualTo(OK.value());

        final AreaOfLaw updatedAreaOfLaw = response.as(AreaOfLaw.class);
        assertThat(updatedAreaOfLaw).isEqualTo(expectedAreaOfLaw);

        //clean up by removing added record
        final Response cleanUpResponse = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            originalJson
        );
        assertThat(cleanUpResponse.statusCode()).isEqualTo(OK.value());
        final AreaOfLaw cleanAreaOfLaw = cleanUpResponse.as(AreaOfLaw.class);
        final String cleanUpJson = objectMapper().writeValueAsString(cleanAreaOfLaw);
        assertThat(cleanUpJson).isEqualTo(originalJson);
    }

    @Test
    public void shouldBeForbiddenForUpdatingAreaOfLaw() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken), testJson
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldRequireATokenWhenUpdatingAreaOfLaw() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT, testJson);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotFoundForUpdateWhenAreaOfLawDoesNotExist() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        currentAreaOfLaw.setId(1234);
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT, Map.of(AUTHORIZATION, BEARER + superAdminToken),
            testJson
        );
        assertThat(response.statusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    public void shouldReturnBadRequestForUpdateAreaOfLawWhenIdIsNull() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        currentAreaOfLaw.setId(null);
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);
        final Response response = doPutRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT, Map.of(AUTHORIZATION, BEARER + superAdminToken),
            testJson
        );
        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
    }

    /************************************************************* POST request tests section. ***************************************************************/

    @Test
    public void shouldCreateAreaOfLaw() throws JsonProcessingException {

        final List<AreaOfLaw> currentAreasOfLaw = getCurrentAreasOfLaw();
        final AreaOfLaw expectedAreaOfLaw = createAreaOfLaw();
        final String newAreaOfLawJson = objectMapper().writeValueAsString(expectedAreaOfLaw);

        final var response = doPostRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            newAreaOfLawJson
        );
        assertThat(response.statusCode()).isEqualTo(CREATED.value());
        final AreaOfLaw createdAreaOfLaw = response.as(AreaOfLaw.class);

        assertThat(createdAreaOfLaw.getName()).isEqualTo(expectedAreaOfLaw.getName());
        final String deleteJson = objectMapper().writeValueAsString(createdAreaOfLaw);
        //clean up by removing added record
        final var cleanUpResponse = doDeleteRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + createdAreaOfLaw.getId(),
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            deleteJson
        );
        assertThat(cleanUpResponse.statusCode()).isEqualTo(OK.value());

        final var responseAfterClean = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );

        final List<AreaOfLaw> cleanedAreaOfLaw = responseAfterClean.body().jsonPath().getList(
            ".",
            AreaOfLaw.class
        );
        assertThat(cleanedAreaOfLaw).containsExactlyElementsOf(currentAreasOfLaw);
    }

    @Test
    public void shouldBeForbiddenForCreatingAreaOfLaw() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPostRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + forbiddenToken), testJson
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldRequireATokenWhenCreatingAreaOfLaw() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPostRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT, testJson);
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotCreateAreaOfLawThatAlreadyExist() throws JsonProcessingException {
        final AreaOfLaw currentAreaOfLaw = getCurrentAreaOfLaw();
        final String testJson = objectMapper().writeValueAsString(currentAreaOfLaw);

        final Response response = doPostRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + superAdminToken), testJson
        );
        assertThat(response.statusCode()).isEqualTo(CONFLICT.value());
    }

    /************************************************************* Delete request tests section. ***************************************************************/

    @Test
    public void adminShouldBeForbiddenForDeletingAreasOfLaw() throws JsonProcessingException {

        final var response = doDeleteRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken), getTestAreasOfLawJson()
        );
        assertThat(response.statusCode()).isEqualTo(FORBIDDEN.value());
    }

    @Test
    public void shouldRequireATokenWhenDeletingAreaOfLaw() throws JsonProcessingException {
        final Response response = doDeleteRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            getTestAreasOfLawJson()
        );
        assertThat(response.statusCode()).isEqualTo(UNAUTHORIZED.value());
    }

    @Test
    public void shouldNotDeleteAreaOfLawNotFound() throws JsonProcessingException {

        final var response = doDeleteRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_NOT_FOUND,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            getTestAreasOfLawJson()
        );
        assertThat(response.statusCode()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    public void shouldNotDeleteAreaOfLawInUse() throws JsonProcessingException {

        final var response = doDeleteRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            Map.of(AUTHORIZATION, BEARER + superAdminToken),
            getTestAreasOfLawJson()
        );
        assertThat(response.statusCode()).isEqualTo(CONFLICT.value());
    }

    /************************************************************* Shared utility methods. ***************************************************************/

    private AreaOfLaw getCurrentAreaOfLaw() {
        final Response response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT + AREA_OF_LAW_ID_HOUSING_POSSESSION,
            Map.of(AUTHORIZATION, BEARER + authenticatedToken)
        );
        return response.as(AreaOfLaw.class);
    }

    private AreaOfLaw getUpdatedHousingPossessionAreaOfLaw() {

        return new AreaOfLaw(
            AREA_OF_LAW_ID_HOUSING_POSSESSION,
            HOUSING_POSSESSION_NAME,
            TEST_SINGLE_POINT_OF_ENTRY,
            TEST_EXTERNAL_LINK,
            TEST_EXTERNAL_LINK_DESC,
            TEST_EXTERNAL_LINK_DESC_CY,
            TEST_ALT_NAME,
            TEST_ALT_NAME_CY,
            TEST_DISPLAY_NAME,
            TEST_DISPLAY_NAME_CY,
            TEST_DISPLAY_EXTERNAL_LINK
        );
    }

    private String getTestAreasOfLawJson() throws JsonProcessingException {
        return objectMapper().writeValueAsString(getUpdatedHousingPossessionAreaOfLaw());
    }

    private List<AreaOfLaw> getCurrentAreasOfLaw() {
        final var response = doGetRequest(
            ADMIN_AREAS_OF_LAW_ENDPOINT,
            Map.of(AUTHORIZATION, BEARER + superAdminToken)
        );
        return response.body().jsonPath().getList(".", AreaOfLaw.class);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private AreaOfLaw createAreaOfLaw() {
        return new AreaOfLaw(
            AREA_OF_LAW_NEW_TEST_ID,
            AREA_OF_LAW_NEW_TEST_NAME,
            TEST_SINGLE_POINT_OF_ENTRY,
            TEST_EXTERNAL_LINK,
            TEST_EXTERNAL_LINK_DESC,
            TEST_EXTERNAL_LINK_DESC_CY,
            TEST_ALT_NAME,
            TEST_ALT_NAME_CY,
            TEST_DISPLAY_NAME,
            TEST_DISPLAY_NAME_CY,
            TEST_DISPLAY_EXTERNAL_LINK
        );
    }
}
