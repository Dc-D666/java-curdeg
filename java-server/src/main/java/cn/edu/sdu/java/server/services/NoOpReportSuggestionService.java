package cn.edu.sdu.java.server.services;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoOpReportSuggestionService implements ReportSuggestionService {
    @Override
    public Optional<ReportSuggestion> suggestHandling(Integer targetType, Long targetId, String reportReason, String targetSnapshot) {
        return Optional.empty();
    }
}
