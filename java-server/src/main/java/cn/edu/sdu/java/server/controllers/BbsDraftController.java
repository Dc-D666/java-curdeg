package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bbs/draft")
@RequiredArgsConstructor
public class BbsDraftController {

    private final BbsDraftService bbsDraftService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getDraftList() {
        return bbsDraftService.getDraftList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public DataResponse getDraft(@PathVariable Long id) {
        return bbsDraftService.getDraft(id);
    }

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public DataResponse saveDraft(@Valid @RequestBody DataRequest dataRequest) {
        return bbsDraftService.saveDraft(dataRequest);
    }

    @PostMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public DataResponse deleteDraft(@Valid @RequestBody DataRequest dataRequest) {
        return bbsDraftService.deleteDraft(dataRequest);
    }
}
