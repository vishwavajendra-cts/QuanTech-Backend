package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.employee.EmployeeNameResponseDTO;
import com.QuanTech.QuanTech.services.EmployeeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "Employee CRUD Rest API",
        description = "CRUD Rest APIs - Create Employee, Update Employee, Get All Employee, Get Employee By Id, Update Employee, Delete Employee, Patch Employee"
)
@Slf4j
@RestController
@RequestMapping("/api/employees")
@CrossOrigin("*")
public class EmployeeController {
    private final EmployeeServiceImpl employeeService;

    @Autowired
    public EmployeeController(EmployeeServiceImpl employeeService) {
        this.employeeService = employeeService;
    }


    @Operation(
            summary = "Create Employee REST API",
            description = "Create Employee REST API endpoint is used to save an employee into the database"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Http Status 201 Created",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - validation failed or malformed request"
            )
    })
    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("Invoked the POST: createEmployee controller method, employeeDTO:{}", employeeDTO);
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(createdEmployee,HttpStatus.CREATED);
    }


    @Operation(
            summary = "Get Employee By ID REST API",
            description = "Get Employee By ID REST API endpoint is used to fetch a single employee from the database"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Http Status 200 Success",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - invalid id supplied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - employee with given id does not exist"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        log.info("Invoked the GET: getEmployeeId controller method, employeeId:{}", id);
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }


    @Operation(
            summary = "Get Employee Name REST API",
            description = "Get Employee Name REST API endpoint is used to fetch the employee First Name and Last Name"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Http Status 200 Success",
                    content = @Content(schema = @Schema(implementation = EmployeeNameResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - invalid id supplied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - employee with given id does not exist"
            )
    })
    @GetMapping("/{id}/name")
    public ResponseEntity<?> getEmployeeName(@PathVariable String id) {
        log.info("Invoked GET: getEmployeeName controller method, employeeId:{}", id);
        EmployeeNameResponseDTO getName = employeeService.getEmployeeName(id);
        return new ResponseEntity<>(getName, HttpStatus.OK);
    }


    @Operation(
            summary = "Get All Employees REST API",
            description = "Get All Employees REST API endpoint is used to fetch all employees from the database"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Http Status 200 Success",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO[].class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        log.info("Invoked the GET: getAllEmployees controller method");
        List<EmployeeDTO> getEmployees = employeeService.getAllEmployees();
        return ResponseEntity.ok(getEmployees);
    }


    @Operation(
            summary = "Update Employee REST API",
            description = "Update Employee REST API endpoint is used to update an existing employee's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Http Status 200 Success",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("Invoked the PUT: updateEmployee controller method, employeeId:{}, employeeDTO:{}", id, employeeDTO);
        EmployeeDTO updated = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "Patch Employee REST API",
            description = "Patch Employee REST API endpoint is used to perform a partial update on an employee's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Http Status 200 Success",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchEmployee(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        log.info("Invoked the PATCH: patchEmployee controller method, employeeId:{}, updates:{}", id, updates);
        EmployeeDTO patchEmployee = employeeService.patchEmployee(id, updates);
        return ResponseEntity.ok(patchEmployee);
    }


    @Operation(
            summary = "Delete Employee REST API",
            description = "Delete Employee REST API endpoint is used to remove an employee from the database"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Http Status 204 No Content"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable String id) {
        log.info("Invoked the DELETE: deleteEmployee controller method, employeeId:{}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
