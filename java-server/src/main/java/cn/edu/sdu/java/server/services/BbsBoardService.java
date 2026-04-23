package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsBoard;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsBoardRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BbsBoardService {

    private final BbsBoardRepository bbsBoardRepository;

    public BbsBoardService(BbsBoardRepository bbsBoardRepository) {
        this.bbsBoardRepository = bbsBoardRepository;
    }

    public DataResponse getBoardList() {
        List<BbsBoard> boardList = bbsBoardRepository.findAll(Sort.by(Sort.Order.asc("sortOrder")));
        return CommonMethod.getReturnData(boardList);
    }

    @Transactional
    public DataResponse createBoard(DataRequest dataRequest) {
        String name = dataRequest.getString("name");
        String description = dataRequest.getString("description");
        Integer sortOrder = dataRequest.getInteger("sortOrder");

        if (name == null || name.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：板块名称不能为空");
        }
        if (name.length() < 2 || name.length() > 50) {
            return CommonMethod.getReturnMessageError("参数错误：板块名称长度2-50");
        }

        if (description != null && description.length() > 200) {
            return CommonMethod.getReturnMessageError("参数错误：板块描述长度不能超过200");
        }

        if (sortOrder != null && sortOrder < 0) {
            return CommonMethod.getReturnMessageError("参数错误：板块排序必须≥0");
        }

        if (bbsBoardRepository.existsByName(name)) {
            return CommonMethod.getReturnMessageError("板块名称已存在");
        }

        BbsBoard board = new BbsBoard();
        board.setName(name);
        board.setDescription(description);
        board.setSortOrder(sortOrder != null ? sortOrder : 0);

        bbsBoardRepository.saveAndFlush(board);

        return CommonMethod.getReturnData(board);
    }

    @Transactional
    public DataResponse updateBoard(Long id, DataRequest dataRequest) {
        Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(id);
        if (boardOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("板块不存在");
        }

        BbsBoard board = boardOptional.get();

        String name = dataRequest.getString("name");
        if (name != null && !name.isBlank()) {
            if (name.length() < 2 || name.length() > 50) {
                return CommonMethod.getReturnMessageError("参数错误：板块名称长度2-50");
            }
            if (!name.equals(board.getName()) && bbsBoardRepository.existsByName(name)) {
                return CommonMethod.getReturnMessageError("板块名称已存在");
            }
            board.setName(name);
        }

        String description = dataRequest.getString("description");
        if (description != null) {
            if (description.length() > 200) {
                return CommonMethod.getReturnMessageError("参数错误：板块描述长度不能超过200");
            }
            board.setDescription(description);
        }

        Integer sortOrder = dataRequest.getInteger("sortOrder");
        if (sortOrder != null) {
            if (sortOrder < 0) {
                return CommonMethod.getReturnMessageError("参数错误：板块排序必须≥0");
            }
            board.setSortOrder(sortOrder);
        }

        bbsBoardRepository.saveAndFlush(board);

        return CommonMethod.getReturnData(board);
    }

    @Transactional
    public DataResponse deleteBoard(Long id) {
        Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(id);
        if (boardOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("板块不存在");
        }

        bbsBoardRepository.delete(boardOptional.get());

        return CommonMethod.getReturnMessageOK("删除成功");
    }
}
