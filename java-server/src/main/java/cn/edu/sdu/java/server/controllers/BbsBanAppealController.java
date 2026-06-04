package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsBanAppealService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/appeal")
public class BbsBanAppealController {

    private final BbsBanAppealService bbsBanAppealService;

    public BbsBanAppealController(BbsBanAppealService bbsBanAppealService) {
        this.bbsBanAppealService = bbsBanAppealService;
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public DataResponse submitAppeal(@RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        return bbsBanAppealService.submitAppeal(reason);
    }

    @GetMapping("/my-list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getMyAppeals(@RequestParam Map<String, String> params) {
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
        return bbsBanAppealService.getMyAppeals(dataRequest);
    }

    @GetMapping("/all-list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getAllAppeals(@RequestParam Map<String, String> params) {
        DataRequest dataRequest = new DataRequest();
        params.forEach((key, value) -> {
            try {
                if ("pageNum".equals(key) || "pageSize".equals(key) || "status".equals(key)) {
                    dataRequest.add(key, Integer.parseInt(value));
                } else {
                    dataRequest.add(key, value);
                }
            } catch (Exception e) {
                dataRequest.add(key, value);
            }
        });
        return bbsBanAppealService.getAllAppeals(dataRequest);
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getAppealDetail(@PathVariable Long id) {
        return bbsBanAppealService.getAppealDetail(id);
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse handleAppeal(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer decision = request.get("decision") != null ? 
            (request.get("decision") instanceof Integer ? (Integer) request.get("decision") : Integer.parseInt(request.get("decision").toString())) : null;
        String handleResult = request.get("handleResult") != null ? request.get("handleResult").toString() : "";
        return bbsBanAppealService.handleAppeal(id, decision, handleResult);
    }
}
