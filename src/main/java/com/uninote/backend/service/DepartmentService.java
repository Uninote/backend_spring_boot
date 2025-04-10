package com.uninote.backend.service;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentNameId;
import com.uninote.backend.entity.DepartmentSimilarity;
import com.uninote.backend.entity.Language;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.interfaceProjection.DepartmentProjection;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.DepartmentNameRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.DepartmentSimilarityRepository;
import com.uninote.backend.repository.LanguageRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;


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
    private SimilarityService<DepartmentName> similarityService;


    @Autowired
    private DepartmentSimilarityRepository departmentSimilarityRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentProjection> getDepartmentsByUniversityIdAndLanguage(Long universityId, String languageCode) {
        List<DepartmentProjection> departmentProjections = departmentRepository
            .findDepartmentProjectionsByUniversityIdAndLanguageCode(universityId, languageCode);

        return departmentProjections;
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

    public List<DepartmentProjection> getDepartmentsWithQuestionsByUniversityAndLanguage(Long universityId, String language) {
        return departmentRepository.findDepartmentsWithQuestionsByUniversityAndLanguage(universityId, language);
    }

    public List<String> getSemestersWithQuestionsByDepartment(Long departmentId) {
        String semesters = departmentRepository.findSemestersWithQuestionsByDepartment(departmentId);
        return semesters != null ? Arrays.asList(semesters.split(",")) : new ArrayList<>();
    }

    public List<Map<String, Object>> getSimilarDepartments(Long id, Long languageId) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found"));
    
        List<DepartmentSimilarity> similarities =
            departmentSimilarityRepository.findByDepartmentA_IdOrderBySimilarityScoreDesc(id);
    
        return similarities.stream()
            .map(sim -> {
                Department deptB = sim.getDepartmentB();
    
                // Efficiently check if any notes exist for deptB
                boolean hasNotes = noteRepository.existsByCourse_DepartmentAndIsPublicTrueAndDeletedFalse(deptB);
                if (!hasNotes) {
                    return null; // Skip departments with no public, undeleted notes
                }
    
                String name = departmentNameRepository
                    .findByDepartmentIdAndLanguageId(deptB.getId(), languageId)
                    .map(DepartmentName::getName)
                    .orElse("Unnamed");
                University university =  deptB.getUniversity();

                String fullName = departmentNameRepository
                    .findByDepartmentIdAndLanguageId(deptB.getId(), languageId)
                    .map(DepartmentName::getFullName)
                    .orElse("");

                String uniFullName = university.getUniversityNames()
                    .stream()
                    .filter(un -> un.getLanguage().getId().equals(languageId))
                    .map(UniversityName::getFullName)
                    .findFirst()
                    .orElse("");    
                String uniName = university.getUniversityNames()
                    .stream()
                    .filter(un -> un.getLanguage().getId().equals(languageId))
                    .map(UniversityName::getName)
                    .findFirst()
                    .orElse("");    
                Map<String, Object> map = new HashMap<>();
                map.put("id", deptB.getId());
                map.put("name", name);
                map.put("fullName", fullName);
                map.put("score", sim.getSimilarityScore());
                map.put("universityName", uniName);
                map.put("universityFullName", uniFullName);
                map.put("universityId", university.getId());
    
                return map;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    


    
}
