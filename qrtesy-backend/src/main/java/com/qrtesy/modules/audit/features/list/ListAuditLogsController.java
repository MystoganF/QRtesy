package com.qrtesy.modules.audit.features.list;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/audit-logs")
public class ListAuditLogsController {

    private final ListAuditLogsHandler handler;

    public ListAuditLogsController(ListAuditLogsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public List<AuditLogItemResponse> listRecent(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(defaultValue = "25") int limit
    ) {
        return handler.handle(eventId, limit);
    }
}