package com.uninote.backend.service;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Map<String, String>> getDepartmentsByUniversityIdAndLanguage(Long universityId, String languageCode) {
        List<Department> departments = departmentRepository.findByUniversityId(universityId);

        return departments.stream()
            .flatMap(department -> department.getDepartmentNames().stream()
                .filter(name -> name.getLanguage().getCode().equals(languageCode))
                .map(name -> {
                    Map<String, String> deptMap = new HashMap<>();
                    deptMap.put("id", String.valueOf(department.getId()));
                    deptMap.put("fullName", name.getFullName());
                    deptMap.put("name", name.getName());
                    deptMap.put("languageCode", name.getLanguage().getCode());
                    return deptMap;
                }))
            .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        return EntityToDTOConverter.convertDepartmentToDTO(department);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(EntityToDTOConverter::convertDepartmentToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setId(departmentDTO.getId());
        department.setUniversity(universityRepository.findById(departmentDTO.getUniversityId())
                .orElseThrow(() -> new IllegalArgumentException("University not found")));
        department.setCode(departmentDTO.getCode());
        department.setNumSemesters(departmentDTO.getSemesters());
        List<Course> coursesList = courseRepository.findAllById(departmentDTO.getCourseIds());
        Set<Course> coursesSet = coursesList.stream().collect(Collectors.toSet());
        department.setCourses(coursesSet);

        Department savedDepartment = departmentRepository.save(department);
        return EntityToDTOConverter.convertDepartmentToDTO(savedDepartment);
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    
}
