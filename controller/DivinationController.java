package com.example.diencamtamthe.controller;

import com.example.diencamtamthe.model.DivinationRequest;
import com.example.diencamtamthe.model.DivinationResponse;
import com.example.diencamtamthe.service.DivinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/divination")
public class DivinationController {

    @Autowired
    private DivinationService divinationService;

    @PostMapping("/process")
    public ResponseEntity<DivinationResponse> processDivinationParams(@RequestBody DivinationRequest request) {
        DivinationResponse response = new DivinationResponse();
        response.setFullname(request.getFullname());
        
        String dob = request.getBirthDay() + "/" + request.getBirthMonth() + "/" + request.getBirthYear();
        response.setDob(dob);
        response.setCategory(request.getLookupCategory());
        
        // Chuyển dữ liệu sang Service xử lý
        String content = divinationService.processDivination(request);
        response.setContent(content);

        return ResponseEntity.ok(response);
    }
}
