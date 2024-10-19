package com.uninote.backend.service;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.DepartmentRequest;
import com.uninote.backend.repository.DepartmentRequestRepository;

import java.util.List;

@Service
public class DepartmentRequestService {

    @Autowired
    private DepartmentRequestRepository departmentRequestRepository;

    public List<DepartmentRequest> getAllRequests() {
        return departmentRequestRepository.findAll();
    }

    public DepartmentRequest createRequest(DepartmentRequest departmentRequest) {
        return departmentRequestRepository.save(departmentRequest);
    }

    public DepartmentRequest getRequestById(Long id) {
        return departmentRequestRepository.findById(id).orElse(null);
    }

    public void deleteRequest(Long id) {
        departmentRequestRepository.deleteById(id);
    }
}
