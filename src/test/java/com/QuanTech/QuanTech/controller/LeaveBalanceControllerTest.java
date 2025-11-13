package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.constants.enums.AttendanceStatus;
import com.QuanTech.QuanTech.controller.AttendanceController;
import com.QuanTech.QuanTech.dto.attendance.AttendanceResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceDisplayByDateResponseDTO;
import com.QuanTech.QuanTech.dto.attendance.ManagerAttendanceRowDTO;
import com.QuanTech.QuanTech.services.interfaces.AttendanceService;
import com.QuanTech.QuanTech.services.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaveBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;


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
    void getLatestAttendance_returns200AndDto() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.parse("2025-01-01T09:00:00Z");
        AttendanceResponseDTO dto = new AttendanceResponseDTO(
                "ATT-123",
                now,
                now,
                null,
                0.0,
                AttendanceStatus.ACTIVE,
                "Main Office"
        );

        when(attendanceService.getLatestAttendance(eq(employeeId))).thenReturn(dto);

        mockMvc.perform(get("/api/attendances/{employeeId}/latest", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.attendanceId").value("ATT-123"))
                .andExpect(jsonPath("$.location").value("Main Office"))
                .andExpect(jsonPath("$.attendanceStatus").value("ACTIVE"));
    }

    @Test
    void getAttendanceHistory_returns200List() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.parse("2025-01-01T08:00:00Z");
        AttendanceResponseDTO dto1 = new AttendanceResponseDTO("ATT-1", now, now, null, 0.0, AttendanceStatus.COMPLETE, "Loc1");
        AttendanceResponseDTO dto2 = new AttendanceResponseDTO("ATT-2", now.minusDays(1), now.minusDays(1), null, 0.0, AttendanceStatus.COMPLETE, "Loc2");
        List<AttendanceResponseDTO> list = Arrays.asList(dto1, dto2);

        when(attendanceService.getAttendanceHistory(eq(employeeId))).thenReturn(list);

        mockMvc.perform(get("/api/attendances/{employeeId}/history", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].attendanceId").value("ATT-1"))
                .andExpect(jsonPath("$[1].attendanceId").value("ATT-2"));
    }

    @Test
    void checkIn_returns201() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.parse("2025-01-01T09:30:00Z");
        AttendanceResponseDTO responseDto = new AttendanceResponseDTO("ATT-CKIN", now, now, null, 0.0, AttendanceStatus.ACTIVE, "Office");

        when(attendanceService.checkIn(eq(employeeId), Mockito.any())).thenReturn(responseDto);

        Map<String, String> requestBody = Map.of("location", "Office");
        String json = objectMapper.writeValueAsString(requestBody);

        mockMvc.perform(post("/api/attendances/{employeeId}/check-in", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attendanceId").value("ATT-CKIN"))
                .andExpect(jsonPath("$.location").value("Office"))
                .andExpect(jsonPath("$.attendanceStatus").value("ACTIVE"));
    }

    @Test
    void checkOut_returns201() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        OffsetDateTime checkIn = OffsetDateTime.parse("2025-01-01T08:00:00Z");
        OffsetDateTime checkOut = OffsetDateTime.parse("2025-01-01T17:00:00Z");
        AttendanceResponseDTO responseDto = new AttendanceResponseDTO("ATT-CKOUT", checkIn, checkIn, checkOut, 9.0, AttendanceStatus.COMPLETE, "Office");

        when(attendanceService.checkOut(eq(employeeId))).thenReturn(responseDto);

        mockMvc.perform(post("/api/attendances/{employeeId}/check-out", employeeId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attendanceId").value("ATT-CKOUT"))
                .andExpect(jsonPath("$.attendanceStatus").value("COMPLETE"))
                .andExpect(jsonPath("$.hoursWorked").value(Matchers.closeTo(9.0, 0.01)));
    }

    @Test
    void getTeamAttendanceByDate_returns200() throws Exception {
        String managerId = UUID.randomUUID().toString();
        String dateStr = "2025-01-01";
        LocalDate localDate = LocalDate.parse(dateStr);

        ManagerAttendanceRowDTO row = new ManagerAttendanceRowDTO("EMP-001", "John Doe", OffsetDateTime.parse("2025-01-01T09:00:00Z"), OffsetDateTime.parse("2025-01-01T17:00:00Z"), 8.0, AttendanceStatus.COMPLETE);
        List<ManagerAttendanceRowDTO> rows = List.of(row);

        ManagerAttendanceDisplayByDateResponseDTO responseDto = new ManagerAttendanceDisplayByDateResponseDTO(localDate, rows);

        when(attendanceService.getTeamsAttendanceByDate(eq(managerId), eq(dateStr))).thenReturn(responseDto);

        mockMvc.perform(get("/api/attendances/{managerId}/attendance", managerId).param("date", dateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(dateStr))
                .andExpect(jsonPath("$.attendanceRows", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.attendanceRows[0].displayEmployeeId").value("EMP-001"))
                .andExpect(jsonPath("$.attendanceRows[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.attendanceRows[0].attendanceStatus").value("COMPLETE"));
    }
}