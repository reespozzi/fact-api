package uk.gov.hmcts.dts.fact.controllers.admin;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dts.fact.config.security.Role;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.services.admin.AdminCourtAreasOfLawService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_ADMIN;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_SUPER_ADMIN;

@RestController
@RequestMapping(
    path = "/admin/courts",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class AdminCourtAreasOfLawController {
    private final AdminCourtAreasOfLawService adminCourtAreasOfLawService;

    @Autowired
    public AdminCourtAreasOfLawController(AdminCourtAreasOfLawService adminService) {
        this.adminCourtAreasOfLawService = adminService;
    }

    @GetMapping(path = "/{slug}/courtAreasOfLaw")
    @ApiOperation("Find the areas of law for a court")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Court not Found")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> getCourtAreasOfLaw(@PathVariable String slug) {
        return ok(adminCourtAreasOfLawService.getCourtAreasOfLawBySlug(slug));
    }

    @PutMapping(path = "/{slug}/courtAreasOfLaw")
    @ApiOperation("Update the areas of law for a court")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Court not Found")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> updateCourtAreasOfLaw(@PathVariable String slug,
                                                                 @RequestBody List<AreaOfLaw> areasOfLaw) {
        return ok(adminCourtAreasOfLawService.updateAreasOfLawForCourt(slug, areasOfLaw));
    }
}
