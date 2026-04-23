package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.BbsBoardService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/board")
public class BbsBoardController {

    private final BbsBoardService bbsBoardService;

    public BbsBoardController(BbsBoardService bbsBoardService) {
        this.bbsBoardService = bbsBoardService;
    }

    @GetMapping("/list")
    public DataResponse getBoardList() {
        return bbsBoardService.getBoardList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER')")
    public DataResponse createBoard(@Valid @RequestBody DataRequest dataRequest) {
        return bbsBoardService.createBoard(dataRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER')")
    public DataResponse updateBoard(@PathVariable Long id, @Valid @RequestBody DataRequest dataRequest) {
        return bbsBoardService.updateBoard(id, dataRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER')")
    public DataResponse deleteBoard(@PathVariable Long id) {
        return bbsBoardService.deleteBoard(id);
    }
}
