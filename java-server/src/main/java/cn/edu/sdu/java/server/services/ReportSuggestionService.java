package cn.edu.sdu.java.server.services;

import java.util.Optional;

public interface ReportSuggestionService {
    Optional<ReportSuggestion> suggestHandling(Integer targetType, Long targetId, String reportReason, String targetSnapshot);

    record ReportSuggestion(Integer suggestedHandleType, String reason, Integer confidence) {
    }
}
