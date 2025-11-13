package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.constants.enums.LeaveStatus;
import com.QuanTech.QuanTech.constants.enums.LeaveType;
import com.QuanTech.QuanTech.controller.LeaveController;
import com.QuanTech.QuanTech.dto.leaveRequests.EmployeeLeaveRequestDashboardResponseDTO;
import com.QuanTech.QuanTech.dto.leaveRequests.LeaveRequestCreateRequestDTO;
import com.QuanTech.QuanTech.dto.leaveRequests.LeaveRequestResponseDTO;
import com.QuanTech.QuanTech.services.LeaveRequestServiceImpl;
import com.QuanTech.QuanTech.services.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matchers;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LeaveController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveRequestServiceImpl leaveRequestService;

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
    void createLeaveRequest_returns200AndResponse() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        LeaveRequestCreateRequestDTO requestDTO = new LeaveRequestCreateRequestDTO(
                LeaveType.VACATION,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 12),
                "Vacation for family event"
        );

        OffsetDateTime now = OffsetDateTime.parse("2025-01-01T09:00:00Z");
        LeaveRequestResponseDTO responseDTO = new LeaveRequestResponseDTO(
                "LR-ABC123",
                LeaveType.VACATION,
                requestDTO.startDate(),
                requestDTO.endDate(),
                3L,
                LeaveStatus.PENDING,
                now,
                requestDTO.reason()
        );

        when(leaveRequestService.createLeaveRequest(eq(employeeId), eq(requestDTO))).thenReturn(responseDTO);

        String json = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/leave-requests/employees/{employeeId}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.leaveRequestId").value("LR-ABC123"))
                .andExpect(jsonPath("$.leaveType").value("VACATION"))
                .andExpect(jsonPath("$.days").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("Vacation for family event"));
    }

    @Test
    void getLeaveRequestByEmployee_returns200List() throws Exception {
        String employeeId = UUID.randomUUID().toString();

        OffsetDateTime now = OffsetDateTime.parse("2025-01-01T09:00:00Z");
        LeaveRequestResponseDTO dto1 = new LeaveRequestResponseDTO(
                "LR-1",
                LeaveType.SICK,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 1),
                1L,
                LeaveStatus.APPROVED,
                now,
                "Flu"
        );
        LeaveRequestResponseDTO dto2 = new LeaveRequestResponseDTO(
                "LR-2",
                LeaveType.VACATION,
                LocalDate.of(2024, 12, 24),
                LocalDate.of(2024, 12, 26),
                3L,
                LeaveStatus.REJECTED,
                now.minusDays(5),
                "Year end break"
        );

        List<LeaveRequestResponseDTO> list = List.of(dto1, dto2);

        when(leaveRequestService.getEmployeeLeaveRequests(eq(employeeId))).thenReturn(list);

        mockMvc.perform(get("/api/leave-requests/employees/{employeeId}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].leaveRequestId").value("LR-1"))
                .andExpect(jsonPath("$[1].leaveRequestId").value("LR-2"));
    }

    @Test
    void getLeaveRequestEmployeeDashboard_returns200List() throws Exception {
        String employeeId = UUID.randomUUID().toString();

        EmployeeLeaveRequestDashboardResponseDTO d1 = new EmployeeLeaveRequestDashboardResponseDTO(
                "LR-DB-1",
                LeaveType.VACATION,
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 3),
                LeaveStatus.PENDING
        );

        EmployeeLeaveRequestDashboardResponseDTO d2 = new EmployeeLeaveRequestDashboardResponseDTO(
                "LR-DB-2",
                LeaveType.SICK,
                LocalDate.of(2024, 12, 5),
                LocalDate.of(2024, 12, 5),
                LeaveStatus.APPROVED
        );

        List<EmployeeLeaveRequestDashboardResponseDTO> dashboard = List.of(d1, d2);

        when(leaveRequestService.getLeaveRequestEmployeeDashboard(eq(employeeId))).thenReturn(dashboard);

        mockMvc.perform(get("/api/leave-requests/employees/{employeeId}/dashboard", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].leaveRequestId").value("LR-DB-1"))
                .andExpect(jsonPath("$[1].leaveRequestId").value("LR-DB-2"));
    }
}