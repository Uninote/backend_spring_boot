package com.uninote.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.QrView;
import com.uninote.backend.repository.QrViewRepository;

@Service
public class QrViewService {

    @Autowired
    private QrViewRepository qrViewRepository;

    public void recordView() {
        QrView qr = new QrView();
        qr.setViewTime(LocalDateTime.now());
        qrViewRepository.save(qr);
    }

    public Long countViews() {
        return qrViewRepository.countAll();
    }
}
