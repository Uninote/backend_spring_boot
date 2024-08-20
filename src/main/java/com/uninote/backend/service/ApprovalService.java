package com.uninote.backend.service;

import com.uninote.backend.dto.ApprovalDTO;
import com.uninote.backend.entity.Approval;
import com.uninote.backend.entity.ApprovalId;
import com.uninote.backend.repository.ApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepository;

    
    public ApprovalDTO saveApproval(ApprovalDTO approvalDTO) {
        Approval approval = convertToEntity(approvalDTO);
        Approval savedApproval = approvalRepository.save(approval);
        return convertToDTO(savedApproval);
    }

    
    public List<ApprovalDTO> getAllApprovals() {
        List<Approval> approvals = approvalRepository.findAll();
        return approvals.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
    }

    
    public Optional<ApprovalDTO> getApprovalById(ApprovalId id) {
        Optional<Approval> approval = approvalRepository.findById(id);
        return approval.map(this::convertToDTO);
    }

    
    public void deleteApprovalById(ApprovalId id) {
        approvalRepository.deleteById(id);
    }

    
    private Approval convertToEntity(ApprovalDTO approvalDTO) {
        ApprovalId approvalId = new ApprovalId(approvalDTO.getUserId(), approvalDTO.getApprovedId());
        Approval approval = new Approval();
        approval.setId(approvalId);
        approval.setUserId(approvalDTO.getUserId());
        approval.setApprovedId(approvalDTO.getApprovedId());
        return approval;
    }

    
    private ApprovalDTO convertToDTO(Approval approval) {
        return new ApprovalDTO(approval.getUserId(), approval.getApprovedId());
    }
}
