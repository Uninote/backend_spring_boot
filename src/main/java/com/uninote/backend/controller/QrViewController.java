package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.QrViewService;

@RestController
@RequestMapping("/qr")
public class QrViewController {
    
    @Autowired
    private QrViewService qrViewService;

    @PostMapping
    public void recordView() {
        qrViewService.recordView();
    }
}
