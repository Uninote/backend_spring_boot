package com.uninote.backend.service;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentNameId;
import com.uninote.backend.entity.Language;
import com.uninote.backend.repository.DepartmentNameRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.LanguageRepository;

@Service
public class DepartmentNameService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentNameRepository departmentNameRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Transactional
    public DepartmentNameDTO addDepartmentName(DepartmentNameDTO departmentNameDTO) {
        
        Department department = departmentRepository.findById(departmentNameDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        
        Language language = languageRepository.findById(Long.parseLong(departmentNameDTO.getLanguage()))
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));

        
        DepartmentName departmentName = new DepartmentName();
        departmentName.setDepartment(department);
        departmentName.setLanguage(language);
        departmentName.setName(departmentNameDTO.getName());
        departmentName.setFullName(departmentNameDTO.getFullName());

        
        DepartmentNameId departmentNameId = new DepartmentNameId(department.getId(), language.getId());
        departmentName.setId(departmentNameId);

        DepartmentName savedDepartmentName = departmentNameRepository.save(departmentName);

        
        DepartmentNameDTO resultDTO = new DepartmentNameDTO();
        resultDTO.setId(savedDepartmentName.getDepartment().getId());
        resultDTO.setLanguage(language.getId().toString());
        resultDTO.setName(savedDepartmentName.getName());
        resultDTO.setFullName(savedDepartmentName.getFullName());

        return resultDTO;
    }
}
