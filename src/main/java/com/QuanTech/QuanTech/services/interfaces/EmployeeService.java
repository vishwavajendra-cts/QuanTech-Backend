package com.QuanTech.QuanTech.services.interfaces;

import com.QuanTech.QuanTech.dto.EmployeeDTO;
import com.QuanTech.QuanTech.dto.employee.EmployeeNameResponseDTO;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    EmployeeDTO getEmployeeById(String id);

    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO updateEmployee(String id, EmployeeDTO employeeDetails);

    EmployeeDTO patchEmployee(String employeeId, Map<String, Object> updates);

    void deleteEmployee(String id);

    EmployeeNameResponseDTO getEmployeeName(String id);
}
