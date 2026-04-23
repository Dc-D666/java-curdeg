
package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModerationResult {
    private String auditResult;
    private String violationLevel;
    private String violationType;
    private List<String> violationFragments;
    private String suggestion;
    private Integer confidence;
    private String remark;
    private boolean error;
    private String errorMessage;

    public static ModerationResult pass() {
        ModerationResult result = new ModerationResult();
        result.setAuditResult("pass");
        result.setViolationLevel("none");
        result.setConfidence(100);
        return result;
    }

    public static ModerationResult manual() {
        ModerationResult result = new ModerationResult();
        result.setAuditResult("manual");
        result.setConfidence(50);
        return result;
    }

    public static ModerationResult error(String message) {
        ModerationResult result = new ModerationResult();
        result.setAuditResult("manual");
        result.setError(true);
        result.setErrorMessage(message);
        return result;
    }
}
