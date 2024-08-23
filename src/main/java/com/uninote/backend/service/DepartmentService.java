package com.uninote.backend.service;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentNameId;
import com.uninote.backend.entity.Language;
import com.uninote.backend.interfaceProjection.DepartmentProjection;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.DepartmentNameRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.LanguageRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentNameRepository departmentNameRepository;

    @Autowired
    private LanguageRepository languageRepository;

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
        List<DepartmentProjection> departmentProjections = departmentRepository
            .findDepartmentProjectionsByUniversityIdAndLanguageCode(universityId, languageCode);

        return departmentProjections.stream()
            .map(projection -> {
                Map<String, String> deptMap = new HashMap<>();
                deptMap.put("id", String.valueOf(projection.getId()));
                deptMap.put("fullName", projection.getFullName());
                deptMap.put("name", projection.getName());
                deptMap.put("languageCode", projection.getLanguageCode());
                return deptMap;
            })
            .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        return EntityToDTOConverter.convertDepartmentToDTO(department);
    }

    public List<DepartmentNameDTO> getDepartmentNames(Long departmentId) {
        return departmentRepository.findNamesById(departmentId);
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

        Department savedDepartment = departmentRepository.saveAndFlush(department);
        return EntityToDTOConverter.convertDepartmentToDTO(savedDepartment);
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }


    @Transactional
    public Long getDepartmentIdByName(String name) {
        Optional<DepartmentName> department = departmentNameRepository.findByName(name);
        if (department.isPresent()) {
            return department.get().getDepartment().getId();
        } else {
            throw new IllegalArgumentException("Department not found");
        }
    }

    
}
