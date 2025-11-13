package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.controller.EmployeeController;
import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.employee.EmployeeNameResponseDTO;
import com.QuanTech.QuanTech.services.EmployeeServiceImpl;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeServiceImpl employeeService;

    // mocks that may be required by security / other beans in the slice
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
    void createEmployee_returns201AndDto() throws Exception {
        EmployeeDTO request = new EmployeeDTO(
                null,
                "EMP-001",
                "John",
                "Doe",
                "john.doe@example.com",
                null,
                "1234567890",
                "Developer",
                true,
                "Engineering",
                null,
                null
        );

        EmployeeDTO created = new EmployeeDTO(
                UUID.randomUUID(),
                request.displayEmployeeId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.gender(),
                request.phoneNumber(),
                request.jobTitle(),
                request.isActive(),
                request.departmentName(),
                request.role(),
                request.teamId()
        );

        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(created);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayEmployeeId").value("EMP-001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getEmployeeById_returns200AndDto() throws Exception {
        String id = UUID.randomUUID().toString();
        EmployeeDTO dto = new EmployeeDTO(
                UUID.fromString(id),
                "EMP-002",
                "Alice",
                "Smith",
                "alice@example.com",
                null,
                "0987654321",
                "Manager",
                true,
                "Sales",
                null,
                null
        );

        when(employeeService.getEmployeeById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayEmployeeId").value("EMP-002"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void getEmployeeName_returns200() throws Exception {
        String id = UUID.randomUUID().toString();
        EmployeeNameResponseDTO nameDto = new EmployeeNameResponseDTO("Bob", "Jones");

        when(employeeService.getEmployeeName(id)).thenReturn(nameDto);

        mockMvc.perform(get("/api/employees/{id}/name", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Jones"));
    }

    @Test
    void getAllEmployees_returns200List() throws Exception {
        EmployeeDTO a = new EmployeeDTO(UUID.randomUUID(), "EMP-A", "A", "One", "a@example.com", null, null, null, true, "Dept", null, null);
        EmployeeDTO b = new EmployeeDTO(UUID.randomUUID(), "EMP-B", "B", "Two", "b@example.com", null, null, null, true, "Dept", null, null);

        when(employeeService.getAllEmployees()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].displayEmployeeId").value("EMP-A"))
                .andExpect(jsonPath("$[1].displayEmployeeId").value("EMP-B"));
    }

    @Test
    void updateEmployee_returns200() throws Exception {
        String id = UUID.randomUUID().toString();
        EmployeeDTO req = new EmployeeDTO(UUID.fromString(id), "EMP-UPD", "Updated", "Name", "upd@example.com", null, null, "Lead", true, "Eng", null, null);
        when(employeeService.updateEmployee(eq(id), any(EmployeeDTO.class))).thenReturn(req);

        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayEmployeeId").value("EMP-UPD"))
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void patchEmployee_returns200() throws Exception {
        String id = UUID.randomUUID().toString();
        Map<String, Object> updates = Map.of("firstName", "Patched", "jobTitle", "Senior Dev");

        EmployeeDTO patched = new EmployeeDTO(UUID.fromString(id), "EMP-P", "Patched", "Last", "p@example.com", null, null, "Senior Dev", true, "Eng", null, null);
        when(employeeService.patchEmployee(eq(id), any(Map.class))).thenReturn(patched);

        String json = objectMapper.writeValueAsString(updates);

        mockMvc.perform(patch("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Patched"))
                .andExpect(jsonPath("$.jobTitle").value("Senior Dev"));
    }

    @Test
    void deleteEmployee_returns204() throws Exception {
        String id = UUID.randomUUID().toString();
        doNothing().when(employeeService).deleteEmployee(id);

        mockMvc.perform(delete("/api/employees/{id}", id))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(id);
    }
}