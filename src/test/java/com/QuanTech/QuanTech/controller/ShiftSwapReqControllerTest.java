package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;
import com.QuanTech.QuanTech.controller.ShiftSwapRequestController;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.CreateShiftSwapRequestDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;
import com.QuanTech.QuanTech.services.ShiftSwapRequestServiceImpl;
import com.QuanTech.QuanTech.services.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShiftSwapRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ShiftSwapReqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShiftSwapRequestServiceImpl shiftSwapRequestService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-user");
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }
    }

    @Test
    public void createSwapRequest_success() throws Exception {
        UUID offeringShift = UUID.randomUUID();
        UUID requestingShift = UUID.randomUUID();
        CreateShiftSwapRequestDTO createDto = new CreateShiftSwapRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                offeringShift,
                requestingShift,
                "Need to swap"
        );

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                offeringShift, "DAY", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                requestingShift, "NIGHT", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocB"
        );

        ShiftSwapResponseDTO responseDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-123",
                "Alice",
                "Bob",
                ShiftSwapRequestStatus.PENDING,
                offeringInfo,
                requestingInfo,
                "Need to swap",
                null,
                null
        );

        when(shiftSwapRequestService.createSwapRequest(any(CreateShiftSwapRequestDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-swap-requests/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftSwapId").value("SSR-123"))
                .andExpect(jsonPath("$.fromEmployeeName").value("Alice"))
                .andExpect(jsonPath("$.toEmployeeName").value("Bob"))
                .andExpect(jsonPath("$.offeringShift.id").value(offeringShift.toString()))
                .andExpect(jsonPath("$.requestingShift.id").value(requestingShift.toString()));

        verify(shiftSwapRequestService, times(1)).createSwapRequest(any(CreateShiftSwapRequestDTO.class));
    }

    @Test
    public void getSwapRequestsForEmployee_success() throws Exception {
        UUID employeeId = UUID.randomUUID();

        ShiftSwapQueryResponseDTO dto = new ShiftSwapQueryResponseDTO(
                UUID.randomUUID(),
                "SSR-001",
                "Alice",
                "Bob",
                ShiftSwapRequestStatus.PENDING,
                "DAY",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocA",
                "NIGHT",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocB",
                "Reason",
                null,
                null
        );

        when(shiftSwapRequestService.getSwapRequestsForEmployee(employeeId.toString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/shift-swap-requests/employee/{employeeId}", employeeId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].shiftSwapId").value("SSR-001"));

        verify(shiftSwapRequestService, times(1)).getSwapRequestsForEmployee(employeeId.toString());
    }

    @Test
    public void getTeamSwapRequests_success() throws Exception {
        UUID managerId = UUID.randomUUID();

        ShiftSwapQueryResponseDTO dto = new ShiftSwapQueryResponseDTO(
                UUID.randomUUID(),
                "SSR-002",
                "Carol",
                "Dave",
                ShiftSwapRequestStatus.PENDING,
                "DAY",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocX",
                "NIGHT",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocY",
                "Reason 2",
                null,
                null
        );

        when(shiftSwapRequestService.getTeamSwapRequests(managerId.toString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/shift-swap-requests/manager/{managerId}/requests", managerId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].shiftSwapId").value("SSR-002"));

        verify(shiftSwapRequestService, times(1)).getTeamSwapRequests(managerId.toString());
    }

    @Test
    public void approveSwapRequest_success() throws Exception {
        UUID managerId = UUID.randomUUID();
        UUID swapReqId = UUID.randomUUID();

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                UUID.randomUUID(), "DAY", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                UUID.randomUUID(), "NIGHT", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocB"
        );

        ShiftSwapResponseDTO responseDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-APPR",
                "Eve",
                "Frank",
                ShiftSwapRequestStatus.APPROVED,
                offeringInfo,
                requestingInfo,
                "Approved reason",
                "Manager One",
                OffsetDateTime.now()
        );

        when(shiftSwapRequestService.approveSwapRequest(managerId.toString(), swapReqId.toString())).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-swap-requests/manager/{managerId}/requests/{swapRequestId}/approve", managerId.toString(), swapReqId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftSwapId").value("SSR-APPR"))
                .andExpect(jsonPath("$.approvedByName").value("Manager One"));

        verify(shiftSwapRequestService, times(1)).approveSwapRequest(managerId.toString(), swapReqId.toString());
    }

    @Test
    public void rejectSwapRequest_success() throws Exception {
        UUID managerId = UUID.randomUUID();
        UUID swapReqId = UUID.randomUUID();

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                UUID.randomUUID(), "DAY", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                UUID.randomUUID(), "NIGHT", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(), "LocB"
        );

        ShiftSwapResponseDTO responseDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-REJ",
                "Gina",
                "Hank",
                ShiftSwapRequestStatus.REJECTED,
                offeringInfo,
                requestingInfo,
                "Rejected reason",
                "Manager Two",
                OffsetDateTime.now()
        );

        when(shiftSwapRequestService.rejectSwapRequest(managerId.toString(), swapReqId.toString())).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-swap-requests/manager/{managerId}/requests/{swapRequestId}/reject", managerId.toString(), swapReqId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftSwapId").value("SSR-REJ"))
                .andExpect(jsonPath("$.approvedByName").value("Manager Two"));

        verify(shiftSwapRequestService, times(1)).rejectSwapRequest(managerId.toString(), swapReqId.toString());
    }
}