package com.example.DienCamTamThe.controller;

import com.example.DienCamTamThe.entity.DivinationRequest;
import com.example.DienCamTamThe.entity.DivinationResponse;
import com.example.DienCamTamThe.service.impl.DivinationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/divination")
public class DivinationController {

    @Autowired
    private DivinationServiceImpl divinationServiceImpl;

    @PostMapping("/process")
    public ResponseEntity<DivinationResponse> processDivinationParams(@RequestBody DivinationRequest request) {
        DivinationResponse response = new DivinationResponse();
        response.setFullname(request.getFullname());

        String dob = request.getBirthDay() + "/" + request.getBirthMonth() + "/" + request.getBirthYear();
        response.setDob(dob);
        response.setCategory(request.getLookupCategory());

        // Chuyển dữ liệu sang Service xử lý
        String content = divinationServiceImpl.processDivination(request);
        response.setContent(content);

        return ResponseEntity.ok(response);
    }
}
