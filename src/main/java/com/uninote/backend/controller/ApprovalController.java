package com.uninote.backend.controller;

import com.uninote.backend.dto.ApprovalDTO;
import com.uninote.backend.entity.ApprovalId;
import com.uninote.backend.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    
    @PostMapping
    public ResponseEntity<ApprovalDTO> createOrUpdateApproval(@RequestBody ApprovalDTO approvalDTO) {
        ApprovalDTO savedApproval = approvalService.saveApproval(approvalDTO);
        return ResponseEntity.ok(savedApproval);
    }

    
    @GetMapping
    public ResponseEntity<List<ApprovalDTO>> getAllApprovals() {
        List<ApprovalDTO> approvals = approvalService.getAllApprovals();
        return ResponseEntity.ok(approvals);
    }

    
    @GetMapping("/{userId}/{approvedId}")
    public boolean checkApprovalExists(@PathVariable Long userId, @PathVariable Long approvedId) {
        return approvalService.checkApprovalExists(approvedId, userId);
    }


    @GetMapping("/count/{approvedId}")
    public long countUserApprovals(@PathVariable Long approvedId) {
        return approvalService.countUserApprovals(approvedId);
    }


    
    @DeleteMapping("/{userId}/{approvedId}")
    public ResponseEntity<Void> deleteApproval(@PathVariable Long userId, @PathVariable Long approvedId) {
        ApprovalId id = new ApprovalId(userId, approvedId);
        approvalService.deleteApprovalById(id);
        return ResponseEntity.noContent().build();
    }
}
