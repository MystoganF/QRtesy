package com.qrtesy.modules.events.features.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/dashboard")
public class StaffDashboardController {

    private final StaffDashboardHandler handler;

    public StaffDashboardController(StaffDashboardHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public StaffDashboardResponse getDashboard() {
        return handler.handle();
    }
}