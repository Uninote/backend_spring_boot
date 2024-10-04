package com.uninote.backend.controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.DepartmentRequest;
import com.uninote.backend.service.DepartmentRequestService;

import java.util.List;

@RestController
@RequestMapping("/department-requests")
public class DepartmentRequestController {

    @Autowired
    private DepartmentRequestService departmentRequestService;

    @GetMapping
    public List<DepartmentRequest> getAllRequests() {
        return departmentRequestService.getAllRequests();
    }

    @PostMapping
    public DepartmentRequest createRequest(@RequestBody DepartmentRequest departmentRequest) {
        return departmentRequestService.createRequest(departmentRequest);
    }

    @GetMapping("/{id}")
    public DepartmentRequest getRequestById(@PathVariable Long id) {
        return departmentRequestService.getRequestById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Long id) {
        departmentRequestService.deleteRequest(id);
    }
}
