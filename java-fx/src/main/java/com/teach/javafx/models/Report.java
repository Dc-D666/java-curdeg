package com.teach.javafx.models;

public class Report {
    private Long id;
    private Long reporterId;
    private Integer targetType;
    private Long targetId;
    private String targetSnapshot;
    private String reason;
    private Integer status;
    private Long handlerId;
    private Integer handleType;
    private String handleRemark;
    private String handleTime;
    private String createTime;
    private String updateTime;
    
    private String reporterNickname;
    private String handlerNickname;

    public Report() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTargetSnapshot() {
        return targetSnapshot;
    }

    public void setTargetSnapshot(String targetSnapshot) {
        this.targetSnapshot = targetSnapshot;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public Integer getHandleType() {
        return handleType;
    }

    public void setHandleType(Integer handleType) {
        this.handleType = handleType;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getReporterNickname() {
        return reporterNickname;
    }

    public void setReporterNickname(String reporterNickname) {
        this.reporterNickname = reporterNickname;
    }

    public String getHandlerNickname() {
        return handlerNickname;
    }

    public void setHandlerNickname(String handlerNickname) {
        this.handlerNickname = handlerNickname;
    }

    public ReportFlowView getReportFlowView() {
        ReportFlowStep createStep = new ReportFlowStep("发起举报", "已提交", ReportFlowVisualState.COMPLETED);
        ReportFlowStep manualStep;
        ReportFlowStep resultStep;
        String summary;
        java.util.List<Boolean> connectors;

        if (status != null && status == 1) {
            manualStep = new ReportFlowStep("人工审核", "已完成", ReportFlowVisualState.COMPLETED);
            resultStep = new ReportFlowStep("处理结果", getHandleTypeText(), resolveResultState());
            connectors = java.util.Arrays.asList(true, true);
            summary = "举报已完成处理，当前结果：" + getHandleTypeText();
        } else {
            manualStep = new ReportFlowStep("人工审核", "进行中", ReportFlowVisualState.WARNING);
            resultStep = new ReportFlowStep("处理结果", "待定", ReportFlowVisualState.INACTIVE);
            connectors = java.util.Arrays.asList(true, false);
            summary = "举报已提交，等待管理员人工审核";
        }

        return new ReportFlowView(summary, java.util.Arrays.asList(createStep, manualStep, resultStep), connectors);
    }

    private ReportFlowVisualState resolveResultState() {
        if (handleType == null) {
            return ReportFlowVisualState.INACTIVE;
        }
        switch (handleType) {
            case 1:
            case 3:
                return ReportFlowVisualState.DANGER;
            case 2:
                return ReportFlowVisualState.COMPLETED;
            default:
                return ReportFlowVisualState.INACTIVE;
        }
    }

    public String getHandleTypeText() {
        if (handleType == null) {
            return "待处理";
        }
        switch (handleType) {
            case 1:
                return "删除内容";
            case 2:
                return "驳回举报";
            case 3:
                return "清空违规资料";
            default:
                return "未知";
        }
    }

    public enum ReportFlowVisualState {
        COMPLETED,
        ACTIVE,
        WARNING,
        DANGER,
        INACTIVE
    }

    public static class ReportFlowStep {
        private final String title;
        private final String stateText;
        private final ReportFlowVisualState visualState;

        public ReportFlowStep(String title, String stateText, ReportFlowVisualState visualState) {
            this.title = title;
            this.stateText = stateText;
            this.visualState = visualState;
        }

        public String getTitle() {
            return title;
        }

        public String getStateText() {
            return stateText;
        }

        public ReportFlowVisualState getVisualState() {
            return visualState;
        }
    }

    public static class ReportFlowView {
        private final String summary;
        private final java.util.List<ReportFlowStep> steps;
        private final java.util.List<Boolean> connectors;

        public ReportFlowView(String summary, java.util.List<ReportFlowStep> steps, java.util.List<Boolean> connectors) {
            this.summary = summary;
            this.steps = steps;
            this.connectors = connectors;
        }

        public String getSummary() {
            return summary;
        }

        public java.util.List<ReportFlowStep> getSteps() {
            return steps;
        }

        public boolean isConnectorReached(int index) {
            return index >= 0 && index < connectors.size() && Boolean.TRUE.equals(connectors.get(index));
        }
    }
}
