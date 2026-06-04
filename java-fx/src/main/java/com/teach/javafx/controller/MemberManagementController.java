package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.User;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class MemberManagementController extends ToolController {
    @FXML
    private TableView<User> userTable;
    
    @FXML
    private TableColumn<User, Integer> idColumn;
    
    @FXML
    private TableColumn<User, String> usernameColumn;
    
    @FXML
    private TableColumn<User, String> nicknameColumn;
    
    @FXML
    private TableColumn<User, String> roleColumn;
    
    @FXML
    private TableColumn<User, Boolean> bannedColumn;
    
    @FXML
    private TableColumn<User, Void> actionColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label pageInfoLabel;
    
    @FXML
    private Button prevPageButton;
    
    @FXML
    private Button nextPageButton;
    
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private String currentKeyword = null;
    
    @FXML
    private Integer currentUserId;
    private String currentUserRole;
    
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("personId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        nicknameColumn.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        roleColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String role = "USER";
            String authority = user.getAuthority();
            if (authority != null) {
                if ("ROLE_SUPER".equals(authority)) {
                    role = "SUPER";
                } else if ("ROLE_ADMIN".equals(authority)) {
                    role = "ADMIN";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(role);
        });
        
        roleColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label roleLabel = new Label();
                    roleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 4;");
                    
                    if ("SUPER".equals(item)) {
                        roleLabel.setText("★ 超级管理员");
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-text-fill: white; -fx-background-color: #FF5722;");
                    } else if ("ADMIN".equals(item)) {
                        roleLabel.setText("◆ 管理员");
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-text-fill: white; -fx-background-color: #2196F3;");
                    } else {
                        roleLabel.setText("● 普通用户");
                        roleLabel.setStyle(roleLabel.getStyle() + "-fx-text-fill: white; -fx-background-color: #9E9E9E;");
                    }
                    
                    setGraphic(roleLabel);
                    setText(null);
                }
            }
        });
        
        bannedColumn.setCellValueFactory(new PropertyValueFactory<>("isBanned"));
        bannedColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "是" : "否");
                    setStyle(item ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                }
            }
        });
        
        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button setAdminButton = new Button("设为管理员");
            private final Button revokeAdminButton = new Button("撤销管理员");
            private final Button banButton = new Button("禁言");
            private final Button unbanButton = new Button("解除禁言");
            
            {
                setAdminButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                revokeAdminButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                banButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                unbanButton.setStyle("-fx-background-color: #00BCD4; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                
                setAdminButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    setAdmin(user);
                });
                
                revokeAdminButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    revokeAdmin(user);
                });
                
                banButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    banUser(user);
                });
                
                unbanButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    unbanUser(user);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    boolean isSuperAdmin = "ROLE_SUPER".equals(currentUserRole);
                    boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole);
                    
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                    
                    String userRole = user.getAuthority();
                    boolean isUserSuperAdmin = "ROLE_SUPER".equals(userRole);
                    boolean isUserAdmin = "ROLE_ADMIN".equals(userRole);
                    
                    if (isSuperAdmin && !isUserSuperAdmin) {
                        if (!isUserAdmin) {
                            hbox.getChildren().add(setAdminButton);
                        } else {
                            hbox.getChildren().add(revokeAdminButton);
                        }
                    }
                    
                    if (!isUserSuperAdmin) { // 不能操作超级管理员
                        boolean canManageBannedState = false;
                        if (isSuperAdmin) {
                            // 超级管理员可以管理任何非超级管理员用户的禁言状态
                            canManageBannedState = true;
                        } else if (isAdmin) {
                            // 管理员只能管理普通用户的禁言状态，不能管理其他管理员
                            canManageBannedState = !isUserAdmin;
                        }
                        
                        if (canManageBannedState) {
                            if (Boolean.TRUE.equals(user.getIsBanned())) {
                                hbox.getChildren().add(unbanButton);
                            } else {
                                hbox.getChildren().add(banButton);
                            }
                        }
                    }
                    
                    setGraphic(hbox);
                }
            }
        });
        
        userTable.setItems(userList);
        
        loadCurrentUser();
        loadUsers();
        
        searchButton.setOnAction(event -> searchUsers());
        refreshButton.setOnAction(event -> refreshUsers());
        prevPageButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                loadUsers();
            }
        });
        nextPageButton.setOnAction(event -> {
            currentPage++;
            loadUsers();
        });
    }
    
    private void loadCurrentUser() {
        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.getCurrentUser();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                User user = task.getValue();
                if (user != null) {
                    currentUserId = user.getPersonId();
                    currentUserRole = user.getAuthority();
                    userTable.refresh();
                }
            });
        });
        
        new Thread(task).start();
    }
    
    private void loadUsers() {
        Task<PageResult<User>> task = new Task<PageResult<User>>() {
            @Override
            protected PageResult<User> call() {
                return HttpRequestUtil.getUserList(currentPage, pageSize, currentKeyword);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                PageResult<User> result = task.getValue();
                if (result != null && result.getList() != null) {
                    userList.setAll(result.getList());
                    long total = result.getTotal() != null ? result.getTotal() : 0;
                    int pageSize = result.getPageSize() != null ? result.getPageSize() : 10;
                    int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 1;
                    updatePageInfo(total, totalPages);
                }
            });
        });
        
        new Thread(task).start();
    }
    
    private void searchUsers() {
        String keyword = searchField.getText().trim();
        currentKeyword = keyword.isEmpty() ? null : keyword;
        currentPage = 1;
        loadUsers();
    }
    
    private void refreshUsers() {
        searchField.setText("");
        currentKeyword = null;
        currentPage = 1;
        loadUsers();
    }
    
    private void updatePageInfo(long total, int totalPages) {
        int displayTotalPages = Math.max(totalPages, 1);
        pageInfoLabel.setText("共 " + total + " 条，第 " + currentPage + " / " + displayTotalPages + " 页");
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= displayTotalPages);
    }
    
    private void setAdmin(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认");
        confirmAlert.setHeaderText("确定要将 " + user.getNickname() + " 设为管理员吗？");
        confirmAlert.setContentText("此操作将赋予该用户管理员权限。");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return HttpRequestUtil.setAdmin(user.getPersonId());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(task.getValue())) {
                        showInfo("设置成功！");
                        loadUsers();
                    } else {
                        showError("设置失败，请稍后重试。");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("设置失败，请稍后重试。"));
            });
            
            new Thread(task).start();
        }
    }
    
    private void revokeAdmin(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认");
        confirmAlert.setHeaderText("确定要撤销 " + user.getNickname() + " 的管理员权限吗？");
        confirmAlert.setContentText("此操作将撤销该用户的管理员权限，该用户将成为普通用户。");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return HttpRequestUtil.setUser(user.getPersonId());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(task.getValue())) {
                        showInfo("撤销成功！该用户已成为普通用户。");
                        loadUsers();
                    } else {
                        showError("撤销失败，请稍后重试。");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("撤销失败，请稍后重试。"));
            });
            
            new Thread(task).start();
        }
    }
    
    private void banUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认");
        confirmAlert.setHeaderText("确定要禁言 " + user.getNickname() + " 吗？");
        confirmAlert.setContentText("此用户将无法在社区中发帖、评论和发送私信。");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return HttpRequestUtil.banUser(user.getPersonId());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(task.getValue())) {
                        showInfo("禁言成功！");
                        loadUsers();
                    } else {
                        showError("禁言失败，请稍后重试。");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("禁言失败，请稍后重试。"));
            });
            
            new Thread(task).start();
        }
    }
    
    private void unbanUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认");
        confirmAlert.setHeaderText("确定要解除 " + user.getNickname() + " 的禁言吗？");
        confirmAlert.setContentText("此用户将恢复在社区中的正常使用权限。");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return HttpRequestUtil.unbanUser(user.getPersonId());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(task.getValue())) {
                        showInfo("解除禁言成功！");
                        loadUsers();
                    } else {
                        showError("解除禁言失败，请稍后重试。");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("解除禁言失败，请稍后重试。"));
            });
            
            new Thread(task).start();
        }
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
