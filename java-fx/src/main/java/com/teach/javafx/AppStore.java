package com.teach.javafx;


import com.teach.javafx.controller.base.*;
import com.teach.javafx.models.User;
import com.teach.javafx.request.JwtResponse;

/**
 * 前端应用全程数据类
 * JwtResponse jwt 客户登录信息
 */
public class AppStore {
    private static JwtResponse jwt;
    private static MainFrameController mainFrameController;
    private static Long selectedPostId;
    private static Integer selectedUserId;
    private static Long selectedConversationId;
    private static User currentUser;
    private AppStore(){
    }

    public static JwtResponse getJwt() {
        return jwt;
    }

    public static void setJwt(JwtResponse jwt) {
        AppStore.jwt = jwt;
    }

    public static MainFrameController getMainFrameController() {
        return mainFrameController;
    }

    public static void setMainFrameController(MainFrameController mainFrameController) {
        AppStore.mainFrameController = mainFrameController;
    }

    public static Long getSelectedPostId() {
        return selectedPostId;
    }

    public static void setSelectedPostId(Long selectedPostId) {
        AppStore.selectedPostId = selectedPostId;
    }

    public static Integer getSelectedUserId() {
        return selectedUserId;
    }

    public static void setSelectedUserId(Integer selectedUserId) {
        AppStore.selectedUserId = selectedUserId;
    }

    public static Long getSelectedConversationId() {
        return selectedConversationId;
    }

    public static void setSelectedConversationId(Long selectedConversationId) {
        AppStore.selectedConversationId = selectedConversationId;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        AppStore.currentUser = currentUser;
    }
}
