package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsReportService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/report")
public class BbsReportController {

    private final BbsReportService bbsReportService;

    public BbsReportController(BbsReportService bbsReportService) {
        this.bbsReportService = bbsReportService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public DataResponse createReport(@Valid @RequestBody DataRequest dataRequest) {
        return bbsReportService.createReport(dataRequest);
    }

    @GetMapping("/my-list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyReportList(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("pageNum".equals(key) || "pageSize".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsReportService.getMyReportList(dataRequest);
    }

    @GetMapping("/admin-list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getAdminReportList(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("pageNum".equals(key) || "pageSize".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else if ("status".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsReportService.getAdminReportList(dataRequest);
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse handleReport(@PathVariable Long id, @Valid @RequestBody DataRequest dataRequest) {
        return bbsReportService.handleReport(id, dataRequest);
    }
}
