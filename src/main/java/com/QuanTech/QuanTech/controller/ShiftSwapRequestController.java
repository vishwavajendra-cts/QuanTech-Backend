package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.dto.shiftSwapRequest.CreateShiftSwapRequestDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;
import com.QuanTech.QuanTech.exception.ErrorResponse;
import com.QuanTech.QuanTech.services.ShiftSwapRequestServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Shift Swap Request CRUD Rest API",
        description = "REST APIs - Create Shift Swap Request, Get Employee Swap Requests, Get Team Swap Requests, Approve/Reject Swap Requests"
)
@Slf4j
@RestController
@RequestMapping("/api/shift-swap-requests")
@CrossOrigin("*")
public class ShiftSwapRequestController {
    private final ShiftSwapRequestServiceImpl shiftSwapRequestService;

    @Autowired
    public ShiftSwapRequestController(ShiftSwapRequestServiceImpl shiftSwapRequestService) {
        this.shiftSwapRequestService = shiftSwapRequestService;
    }


    @Operation(
            summary = "Create Shift Swap Request REST API",
            description = "Create a new shift swap request between employees"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created shift swap request",
                    content = @Content(schema = @Schema(implementation = ShiftSwapResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/create")
    public ResponseEntity<ShiftSwapResponseDTO> createSwapRequest(@Valid @RequestBody CreateShiftSwapRequestDTO createShiftSwapRequestDTO) {
        log.info("Invoked the POST: createSwapRequest controller method, createShiftSwapRequestDTO:{}", createShiftSwapRequestDTO);
        ShiftSwapResponseDTO createSwap = shiftSwapRequestService.createSwapRequest(createShiftSwapRequestDTO);
        return ResponseEntity.ok(createSwap);
    }


    @Operation(
            summary = "Get Shift Swap Requests For Employee REST API",
            description = "Retrieve all shift swap requests related to a specific employee"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved shift swap requests",
                    content = @Content(schema = @Schema(implementation = ShiftSwapQueryResponseDTO[].class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid employee ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftSwapQueryResponseDTO>> getSwapRequestsForEmployee(@PathVariable String employeeId) {
        log.info("Invoked the GET: getSwapRequestsForEmployee controller method: employeeId:{}", employeeId);
        List<ShiftSwapQueryResponseDTO> getSwapRequestsByEmployee = shiftSwapRequestService.getSwapRequestsForEmployee(employeeId);
        return ResponseEntity.ok(getSwapRequestsByEmployee);
    }


    @Operation(
            summary = "Get Team Shift Swap Requests REST API",
            description = "Retrieve all shift swap requests for manager's team members"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved team shift swap requests",
                    content = @Content(schema = @Schema(implementation = ShiftSwapQueryResponseDTO[].class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid manager ID format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/manager/{managerId}/requests")
    public ResponseEntity<List<ShiftSwapQueryResponseDTO>> getTeamSwapRequests(@PathVariable String managerId) {
        log.info("Invoked the GET: getTeamSwapRequests controller method, managerId:{}", managerId);
        List<ShiftSwapQueryResponseDTO> getTeamsShift = shiftSwapRequestService.getTeamSwapRequests(managerId);
        return ResponseEntity.ok(getTeamsShift);
    }


    @Operation(
            summary = "Approve Shift Swap Request REST API",
            description = "Approve a pending shift swap request"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully approved shift swap request",
                    content = @Content(schema = @Schema(implementation = ShiftSwapResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid manager ID or swap request ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/manager/{managerId}/requests/{swapRequestId}/approve")
    public ResponseEntity<ShiftSwapResponseDTO> approveSwapRequest(@PathVariable String managerId, @PathVariable String swapRequestId) {
        log.info("Invoked the POST: approveSwapRequest controller method: managerId:{}, swapRequestId:{}", managerId, swapRequestId);
        ShiftSwapResponseDTO approveSwapRequest = shiftSwapRequestService.approveSwapRequest(managerId, swapRequestId);
        return ResponseEntity.ok(approveSwapRequest);
    }


    @Operation(
            summary = "Reject Shift Swap Request REST API",
            description = "Reject a pending shift swap request"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully rejected shift swap request",
                    content = @Content(schema = @Schema(implementation = ShiftSwapResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid manager ID or swap request ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/manager/{managerId}/requests/{swapRequestId}/reject")
    public ResponseEntity<ShiftSwapResponseDTO> rejectSwapRequest(@PathVariable String managerId, @PathVariable String swapRequestId) {
        log.info("Invoked the POST: rejectSwapRequest controller method, managerId:{}, swapRequestId:{}", managerId, swapRequestId);
        ShiftSwapResponseDTO rejectSwapRequest = shiftSwapRequestService.rejectSwapRequest(managerId, swapRequestId);
        return ResponseEntity.ok(rejectSwapRequest);
    }
}
