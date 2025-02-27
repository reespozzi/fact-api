package uk.gov.hmcts.dts.fact.controllers.admin.list;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dts.fact.config.security.Role;
import uk.gov.hmcts.dts.fact.model.admin.AreaOfLaw;
import uk.gov.hmcts.dts.fact.services.admin.list.AdminAreasOfLawService;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_ADMIN;
import static uk.gov.hmcts.dts.fact.services.admin.AdminRole.FACT_SUPER_ADMIN;

@RestController
@RequestMapping(
    path = "/admin/areasOfLaw",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AdminAreasOfLawController {
    private final AdminAreasOfLawService adminAreasOfLawService;

    @Autowired
    public AdminAreasOfLawController(AdminAreasOfLawService adminAreasOfLawService) {
        this.adminAreasOfLawService = adminAreasOfLawService;
    }

    @GetMapping()
    @ApiOperation("Return all areas of law")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<List<AreaOfLaw>> getAllAreasOfLaw() {
        return ok(adminAreasOfLawService.getAllAreasOfLaw());
    }

    @GetMapping(path = "/{id}")
    @ApiOperation("Get area of law")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Area of Law not found")
    })
    @Role({FACT_ADMIN, FACT_SUPER_ADMIN})
    public ResponseEntity<AreaOfLaw> getAreaOfLaw(@PathVariable Integer id) {
        return ok(adminAreasOfLawService.getAreaOfLaw(id));
    }

    @PostMapping()
    @ApiOperation("Create area of law")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = AreaOfLaw.class),
        @ApiResponse(code = 400, message = "Invalid Area of Law", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 409, message = "Area of Law already exists")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<AreaOfLaw> createAreaOfLaw(@RequestBody AreaOfLaw areaOfLaw) {
        return created(URI.create(StringUtils.EMPTY)).body(adminAreasOfLawService.createAreaOfLaw(areaOfLaw));
    }

    @PutMapping()
    @ApiOperation("Update area of law")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class),
        @ApiResponse(code = 400, message = "Invalid Area of Law", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Area of Law not found")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity<AreaOfLaw> updateAreaOfLaw(@RequestBody AreaOfLaw areaOfLaw) {
        return ok(adminAreasOfLawService.updateAreaOfLaw(areaOfLaw));
    }

    @DeleteMapping("/{areaOfLawId}")
    @ApiOperation("Delete area of law")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful", response = AreaOfLaw.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Area of Law not found"),
        @ApiResponse(code = 409, message = "Area of Law in use")
    })
    @Role({FACT_SUPER_ADMIN})
    public ResponseEntity deleteAreaOfLaw(@PathVariable Integer areaOfLawId) {
        adminAreasOfLawService.deleteAreaOfLaw(areaOfLawId);
        return ok().body(areaOfLawId);
    }
}
