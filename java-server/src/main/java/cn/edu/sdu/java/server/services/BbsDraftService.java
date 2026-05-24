package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsDraft;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsDraftRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BbsDraftService {

    private final BbsDraftRepository bbsDraftRepository;

    public DataResponse getDraftList() {
        Integer userId = CommonMethod.getPersonId();
        log.info("BbsDraftService.getDraftList: 用户ID=" + userId);
        if (userId == null) {
            log.warn("BbsDraftService.getDraftList: 用户未登录");
            return CommonMethod.getReturnMessageError("请先登录");
        }
        List<BbsDraft> drafts = bbsDraftRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        log.info("BbsDraftService.getDraftList: 找到草稿数量=" + drafts.size());
        return CommonMethod.getReturnData(drafts);
    }

    public DataResponse getDraft(Long draftId) {
        Integer userId = CommonMethod.getPersonId();
        log.info("BbsDraftService.getDraft: 用户ID=" + userId + ", 草稿ID=" + draftId);
        if (userId == null) {
            return CommonMethod.getReturnMessageError("请先登录");
        }
        Optional<BbsDraft> draft = bbsDraftRepository.findByIdAndUserId(draftId, userId);
        if (draft.isEmpty()) {
            log.warn("BbsDraftService.getDraft: 草稿不存在");
            return CommonMethod.getReturnMessageError("草稿不存在");
        }
        log.info("BbsDraftService.getDraft: 找到草稿, 标题=" + draft.get().getTitle());
        return CommonMethod.getReturnData(draft.get());
    }

    @Transactional
    public DataResponse saveDraft(DataRequest dataRequest) {
        Integer userId = CommonMethod.getPersonId();
        log.info("BbsDraftService.saveDraft: 用户ID=" + userId);
        if (userId == null) {
            log.warn("BbsDraftService.saveDraft: 用户未登录");
            return CommonMethod.getReturnMessageError("请先登录");
        }

        Long draftId = dataRequest.getLong("id");
        String title = dataRequest.getString("title");
        Long boardId = dataRequest.getLong("boardId");
        String boardName = dataRequest.getString("boardName");
        String content = dataRequest.getString("content");
        String imageUrls = dataRequest.getString("imageUrls");
        String attachmentInfos = dataRequest.getString("attachmentInfos");
        
        log.info("BbsDraftService.saveDraft: 草稿ID={}, 标题={}, 板块ID={}, 板块名={}, 内容长度={}", 
                 draftId, title, boardId, boardName, (content != null ? content.length() : 0));

        BbsDraft draft;
        if (draftId != null && draftId > 0) {
            Optional<BbsDraft> existing = bbsDraftRepository.findByIdAndUserId(draftId, userId);
            if (existing.isEmpty()) {
                log.warn("BbsDraftService.saveDraft: 草稿不存在");
                return CommonMethod.getReturnMessageError("草稿不存在");
            }
            draft = existing.get();
        } else {
            draft = new BbsDraft();
            draft.setUserId(userId);
        }

        draft.setTitle(title != null ? title : "");
        draft.setBoardId(boardId);
        draft.setBoardName(boardName);
        draft.setContent(content);
        draft.setImageUrls(imageUrls);
        draft.setAttachmentInfos(attachmentInfos);

        log.info("BbsDraftService.saveDraft: 准备保存草稿, 标题=" + draft.getTitle());
        BbsDraft saved = bbsDraftRepository.save(draft);
        log.info("BbsDraftService.saveDraft: 草稿已保存, ID=" + saved.getId());
        return CommonMethod.getReturnData(Map.of("id", saved.getId()), "草稿已保存");
    }

    @Transactional
    public DataResponse deleteDraft(DataRequest dataRequest) {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return CommonMethod.getReturnMessageError("请先登录");
        }
        Long draftId = dataRequest.getLong("id");
        if (draftId == null) {
            return CommonMethod.getReturnMessageError("参数错误");
        }
        bbsDraftRepository.deleteByIdAndUserId(draftId, userId);
        return CommonMethod.getReturnMessageOK("草稿已删除");
    }
}
