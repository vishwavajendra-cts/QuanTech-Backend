package com.QuanTech.QuanTech.controller;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.employee.EmployeeNameResponseDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.exception.custom.EmployeeNotFoundException;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.services.EmployeeServiceImpl;
import com.QuanTech.QuanTech.util.mappers.EmployeeMapper;
import com.QuanTech.QuanTech.constants.enums.Gender;
import com.QuanTech.QuanTech.constants.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ShiftControllerTest {

    private EmployeeRepository employeeRepository;
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setup() {
        employeeRepository = mock(EmployeeRepository.class);
        employeeService = new EmployeeServiceImpl(employeeRepository);
    }

    @Test
    public void createEmployee_success() {
        EmployeeDTO inputDto = new EmployeeDTO(
                null,
                "EMP-001",
                "John",
                "Doe",
                "john@example.com",
                Gender.MALE,
                "1234567890",
                "Developer",
                true,
                "Engineering",
                Role.EMPLOYEE,
                "team-1"
        );

        Employee mappedEntity = mock(Employee.class);
        Employee savedEntity = mock(Employee.class);
        EmployeeDTO returnedDto = new EmployeeDTO(
                UUID.randomUUID(),
                "EMP-001",
                "John",
                "Doe",
                "john@example.com",
                Gender.MALE,
                "1234567890",
                "Developer",
                true,
                "Engineering",
                Role.EMPLOYEE,
                "team-1"
        );

        try (MockedStatic<EmployeeMapper> mapper = Mockito.mockStatic(EmployeeMapper.class)) {
            mapper.when(() -> EmployeeMapper.employeeDtoToEntity(inputDto)).thenReturn(mappedEntity);
            when(employeeRepository.save(mappedEntity)).thenReturn(savedEntity);
            mapper.when(() -> EmployeeMapper.employeeEntityToDto(savedEntity)).thenReturn(returnedDto);

            EmployeeDTO result = employeeService.createEmployee(inputDto);

            assertEquals(returnedDto, result);
            verify(employeeRepository, times(1)).save(mappedEntity);
        }
    }

    @Test
    public void getEmployeeById_success() {
        UUID id = UUID.randomUUID();
        EmployeeDTO dto = new EmployeeDTO(id, "EMP-002", "Jane", "Smith", "jane@example.com",
                Gender.FEMALE, "0987654321", "Manager", true, "Sales", Role.MANAGER, "team-2");

        when(employeeRepository.findEmployeeByID(id)).thenReturn(Optional.of(dto));

        EmployeeDTO result = employeeService.getEmployeeById(id.toString());

        assertEquals(dto, result);
        verify(employeeRepository, times(1)).findEmployeeByID(id);
    }

    @Test
    public void getEmployeeById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findEmployeeByID(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(id.toString()));
        verify(employeeRepository, times(1)).findEmployeeByID(id);
    }

    @Test
    public void getAllEmployees_success() {
        Employee entity = mock(Employee.class);
        EmployeeDTO dto = new EmployeeDTO(UUID.randomUUID(), "EMP-003", "A", "B", "a@b.com",
                Gender.MALE, "111", "Role", true, "Dept", Role.EMPLOYEE, "team-x");

        when(employeeRepository.findAll()).thenReturn(List.of(entity));

        try (MockedStatic<EmployeeMapper> mapper = Mockito.mockStatic(EmployeeMapper.class)) {
            mapper.when(() -> EmployeeMapper.employeeEntityToDto(entity)).thenReturn(dto);

            List<EmployeeDTO> result = employeeService.getAllEmployees();

            assertEquals(1, result.size());
            assertEquals(dto, result.get(0));
            verify(employeeRepository, times(1)).findAll();
        }
    }

    @Test
    public void updateEmployee_success() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        EmployeeDTO updateDto = new EmployeeDTO(id, "EMP-004", "New", "Name", "new@example.com",
                Gender.MALE, "222", "Dev", true, "Eng", Role.EMPLOYEE, "team-4");

        Employee existingEntity = mock(Employee.class);
        Employee savedEntity = mock(Employee.class);
        EmployeeDTO returnedDto = new EmployeeDTO(id, "EMP-004", "New", "Name", "new@example.com",
                Gender.MALE, "222", "Dev", true, "Eng", Role.EMPLOYEE, "team-4");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(employeeRepository.save(existingEntity)).thenReturn(savedEntity);

        try (MockedStatic<EmployeeMapper> mapper = Mockito.mockStatic(EmployeeMapper.class)) {
            mapper.when(() -> EmployeeMapper.employeeEntityToDto(savedEntity)).thenReturn(returnedDto);

            EmployeeDTO result = employeeService.updateEmployee(idStr, updateDto);

            assertEquals(returnedDto, result);
            verify(employeeRepository, times(1)).findById(id);
            verify(employeeRepository, times(1)).save(existingEntity);
        }
    }

    @Test
    public void updateEmployee_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(id.toString(), mock(EmployeeDTO.class)));
        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    public void patchEmployee_success_delegatesToUpdate() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        EmployeeDTO existing = new EmployeeDTO(id, "EMP-005", "First", "Last", "f@l.com",
                Gender.FEMALE, "333", "Title", true, "Dept", Role.EMPLOYEE, "team-5");

        EmployeeDTO expectedAfterPatch = new EmployeeDTO(id, "EMP-005", "Patched", "Last", "f@l.com",
                Gender.FEMALE, "333", "Title", true, "Dept", Role.EMPLOYEE, "team-5");

        when(employeeRepository.findEmployeeByID(id)).thenReturn(Optional.of(existing));

        // spy service so we can stub updateEmployee call and not depend on BeanUtils behavior here
        EmployeeServiceImpl spyService = Mockito.spy(new EmployeeServiceImpl(employeeRepository));
        doReturn(expectedAfterPatch).when(spyService).updateEmployee(eq(idStr), any(EmployeeDTO.class));

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Patched");

        EmployeeDTO result = spyService.patchEmployee(idStr, updates);

        assertEquals(expectedAfterPatch, result);
        verify(employeeRepository, times(1)).findEmployeeByID(id);
        verify(spyService, times(1)).updateEmployee(eq(idStr), any(EmployeeDTO.class));
    }

    @Test
    public void patchEmployee_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findEmployeeByID(id)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.patchEmployee(id.toString(), Map.of("firstName", "X")));
        verify(employeeRepository, times(1)).findEmployeeByID(id);
    }

    @Test
    public void deleteEmployee_success() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(true);

        employeeService.deleteEmployee(id.toString());

        verify(employeeRepository, times(1)).existsById(id);
        verify(employeeRepository, times(1)).deleteById(id);
    }

    @Test
    public void deleteEmployee_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(id.toString()));
        verify(employeeRepository, times(1)).existsById(id);
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    public void getEmployeeName_success() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        Employee existingEntity = mock(Employee.class);
        EmployeeNameResponseDTO responseDTO = new EmployeeNameResponseDTO("Alpha", "Beta");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(employeeRepository.findEmployeeName(id)).thenReturn(responseDTO);

        EmployeeNameResponseDTO result = employeeService.getEmployeeName(idStr);

        assertEquals(responseDTO, result);
        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, times(1)).findEmployeeName(id);
    }

    @Test
    public void getEmployeeName_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeName(id.toString()));
        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, never()).findEmployeeName(any());
    }
}