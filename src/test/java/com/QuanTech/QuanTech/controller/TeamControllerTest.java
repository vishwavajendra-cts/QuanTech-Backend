package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.TeamDTO;
import com.QuanTech.QuanTech.dto.shift.TeamEmployeesShiftFormResponseDTO;
import com.QuanTech.QuanTech.dto.shift.TeamMembersShiftDTO;
import com.QuanTech.QuanTech.services.TeamServiceImpl;
import com.QuanTech.QuanTech.services.auth.JwtService;
import com.QuanTech.QuanTech.testutil.TeamTestDataInitializer;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamServiceImpl teamService;

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
    public void createTeam_success() throws Exception {
        UUID managerId = UUID.randomUUID();
        List<UUID> employeeIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        TeamDTO teamDTO = TeamTestDataInitializer.createTeamDTO("TEAM-001", "Development Team", managerId, employeeIds);

        when(teamService.createTeam(any(TeamDTO.class))).thenReturn(teamDTO);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value("TEAM-001"))
                .andExpect(jsonPath("$.teamName").value("Development Team"))
                .andExpect(jsonPath("$.teamManagersId").value(managerId.toString()));

        verify(teamService, times(1)).createTeam(any(TeamDTO.class));
    }

    @Test
    public void getTeamSize_success() throws Exception {
        String managerId = UUID.randomUUID().toString();
        when(teamService.getTeamSize(managerId)).thenReturn(5);

        mockMvc.perform(get("/api/teams/manager/{managerId}/teamSize", managerId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(teamService, times(1)).getTeamSize(managerId);
    }


    @Test
    public void getTeamMembers_success() throws Exception {
        String managerId = UUID.randomUUID().toString();

        List<EmployeeDTO> teamMembers = TeamTestDataInitializer.createTeamMembers();

        when(teamService.getTeamMembers(managerId)).thenReturn(teamMembers);

        mockMvc.perform(get("/api/teams/manager/{managerId}/team-members", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].displayEmployeeId").value("EMP-001"))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].displayEmployeeId").value("EMP-002"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(teamService, times(1)).getTeamMembers(managerId);
    }

    @Test
    public void getTeamMembersWithUpcomingShifts_success() throws Exception {
        String employeeId = UUID.randomUUID().toString();

        List<TeamMembersShiftDTO> teamMembersWithShifts = TeamTestDataInitializer.createTeamMembersWithShifts();

        when(teamService.getTeamMembersWithUpcomingShifts(employeeId)).thenReturn(teamMembersWithShifts);

        mockMvc.perform(get("/api/teams/{employeeId}/members-with-upcoming-shifts", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].lastName").value("Johnson"))
                .andExpect(jsonPath("$[0].shifts", hasSize(1)));

        verify(teamService, times(1)).getTeamMembersWithUpcomingShifts(employeeId);
    }

    @Test
    public void getTeamEmployeesByManagerInCreateShiftForm_success() throws Exception {
        String managerId = UUID.randomUUID().toString();

        List<TeamEmployeesShiftFormResponseDTO> teamEmployees = TeamTestDataInitializer.createTeamEmployeesShiftForm();

        when(teamService.getTeamEmployeesByManagerInCreateShiftForm(managerId)).thenReturn(teamEmployees);

        mockMvc.perform(get("/api/teams/manager/{managerId}/team-employees", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Bob"))
                .andExpect(jsonPath("$[0].lastName").value("Wilson"))
                .andExpect(jsonPath("$[1].firstName").value("Carol"))
                .andExpect(jsonPath("$[1].lastName").value("Brown"));

        verify(teamService, times(1)).getTeamEmployeesByManagerInCreateShiftForm(managerId);
    }
}