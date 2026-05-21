package com.noise.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.repository.AlarmLogRepository;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmLogRepository repository;

    @GetMapping("/all")
    public List<AlarmLog> getAll() {
        return repository.findAll();
    }

    @GetMapping("/count/{classroomId}")
    public long countAlarm(@PathVariable Long classroomId) {
        return repository.countByClassroomId(classroomId);
    }
}