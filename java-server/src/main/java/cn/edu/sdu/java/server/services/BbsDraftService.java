package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsDraft;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsDraftRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BbsDraftService {

    private final BbsDraftRepository bbsDraftRepository;

    public DataResponse getDraftList() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return CommonMethod.getReturnMessageError("请先登录");
        }
        List<BbsDraft> drafts = bbsDraftRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        return CommonMethod.getReturnData(drafts);
    }

    public DataResponse getDraft(Long draftId) {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return CommonMethod.getReturnMessageError("请先登录");
        }
        Optional<BbsDraft> draft = bbsDraftRepository.findByIdAndUserId(draftId, userId);
        if (draft.isEmpty()) {
            return CommonMethod.getReturnMessageError("草稿不存在");
        }
        return CommonMethod.getReturnData(draft.get());
    }

    @Transactional
    public DataResponse saveDraft(DataRequest dataRequest) {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return CommonMethod.getReturnMessageError("请先登录");
        }

        Long draftId = dataRequest.getLong("id");
        String title = dataRequest.getString("title");
        Long boardId = dataRequest.getLong("boardId");
        String boardName = dataRequest.getString("boardName");
        String content = dataRequest.getString("content");
        String imageUrls = dataRequest.getString("imageUrls");
        String attachmentInfos = dataRequest.getString("attachmentInfos");

        BbsDraft draft;
        if (draftId != null && draftId > 0) {
            Optional<BbsDraft> existing = bbsDraftRepository.findByIdAndUserId(draftId, userId);
            if (existing.isEmpty()) {
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

        bbsDraftRepository.save(draft);
        return CommonMethod.getReturnData(Map.of("id", draft.getId()), "草稿已保存");
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
