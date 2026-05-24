package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.PersonalCenterController;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

public class PersonalProfileController extends ToolController {
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Button uploadAvatarButton;
    @FXML
    private Label studentIdLabel;
    @FXML
    private TextField nicknameTextField;
    @FXML
    private TextArea signatureTextArea;
    @FXML
    private TextField avatarUrlTextField;
    @FXML
    private TextField personNameTextField;
    @FXML
    private TextField personDeptTextField;
    @FXML
    private ComboBox<String> personGenderComboBox;
    @FXML
    private DatePicker personBirthdayPicker;
    @FXML
    private TextField personEmailTextField;
    @FXML
    private TextField personPhoneTextField;
    @FXML
    private TextField personAddressTextField;
    @FXML
    private TextArea personIntroduceTextArea;
    @FXML
    private Label postCountLabel;
    @FXML
    private Label followingCountLabel;
    @FXML
    private Label followerCountLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox<String> namePrivacyComboBox;
    @FXML
    private ComboBox<String> deptPrivacyComboBox;
    @FXML
    private ComboBox<String> genderPrivacyComboBox;
    @FXML
    private ComboBox<String> birthdayPrivacyComboBox;
    @FXML
    private ComboBox<String> emailPrivacyComboBox;
    @FXML
    private ComboBox<String> phonePrivacyComboBox;
    @FXML
    private ComboBox<String> addressPrivacyComboBox;
    @FXML
    private ComboBox<String> introducePrivacyComboBox;
    @FXML
    private GridPane statsGrid;
    @FXML
    private VBox postCountBox;
    @FXML
    private VBox followingCountBox;
    @FXML
    private VBox followerCountBox;
    @FXML
    private VBox pointsBox;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private ImageView levelIconImageView;

    private User currentUser;
    private String originalNickname;
    private String originalSignature;
    private String originalAvatarUrl;
    private String originalPersonName;
    private String originalPersonDept;
    private String originalPersonGender;
    private String originalPersonBirthday;
    private String originalPersonEmail;
    private String originalPersonPhone;
    private String originalPersonAddress;
    private String originalPersonIntroduce;
    private String originalNamePrivacy;
    private String originalDeptPrivacy;
    private String originalGenderPrivacy;
    private String originalBirthdayPrivacy;
    private String originalEmailPrivacy;
    private String originalPhonePrivacy;
    private String originalAddressPrivacy;
    private String originalIntroducePrivacy;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // 正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://.*$|^/.*$");

    // 隐私选项的简化显示
    private static final ObservableList<String> PRIVACY_OPTIONS = FXCollections.observableArrayList("公开", "仅互关可见", "私密");
    private static final ObservableList<String> PRIVACY_VALUES = FXCollections.observableArrayList("PUBLIC", "FOLLOWING", "PRIVATE");

    private void setupCircleAvatar() {
        // 创建圆形裁剪
        double radius = 60.0; // 头像半径为 fitWidth/2
        Circle clip = new Circle(radius, radius, radius);
        avatarImageView.setClip(clip);
        
        // 监听尺寸变化，保持圆形裁剪
        avatarImageView.fitWidthProperty().addListener((obs, oldVal, newVal) -> {
            double r = newVal.doubleValue() / 2;
            Circle newClip = new Circle(r, r, r);
            avatarImageView.setClip(newClip);
        });
        avatarImageView.fitHeightProperty().addListener((obs, oldVal, newVal) -> {
            double r = newVal.doubleValue() / 2;
            Circle newClip = new Circle(r, r, r);
            avatarImageView.setClip(newClip);
        });
    }

    @FXML
    public void initialize() {
        setupPageScroll();
        // 设置头像圆形裁剪
        setupCircleAvatar();
        
        ObservableList<String> genderOptions = FXCollections.observableArrayList("", "男", "女");
        personGenderComboBox.setItems(genderOptions);
        
        // 为所有隐私下拉框设置自定义显示
        setupPrivacyComboBox(namePrivacyComboBox);
        setupPrivacyComboBox(deptPrivacyComboBox);
        setupPrivacyComboBox(genderPrivacyComboBox);
        setupPrivacyComboBox(birthdayPrivacyComboBox);
        setupPrivacyComboBox(emailPrivacyComboBox);
        setupPrivacyComboBox(phonePrivacyComboBox);
        setupPrivacyComboBox(addressPrivacyComboBox);
        setupPrivacyComboBox(introducePrivacyComboBox);
        
        loadUserData();
        toggleEditMode(false);
        
        editButton.setOnAction(event -> onEdit());
        saveButton.setOnAction(event -> onSave());
        cancelButton.setOnAction(event -> onCancel());
        uploadAvatarButton.setOnAction(event -> selectAndUploadAvatar());
        avatarImageView.setOnMouseClicked(event -> selectAndUploadAvatar());
        
        // 设置统计卡片的点击事件
        setupStatsCardEvents();
    }

    private void setupPageScroll() {
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
            double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight <= 0) {
                return;
            }

            double delta = event.getDeltaY() / scrollableHeight;
            mainScrollPane.setVvalue(clamp(mainScrollPane.getVvalue() - delta));
            event.consume();
        });
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }

    private void setupPrivacyComboBox(ComboBox<String> comboBox) {
        comboBox.setItems(PRIVACY_OPTIONS);
        comboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                if (object == null) return "";
                int index = PRIVACY_VALUES.indexOf(object);
                return index != -1 ? PRIVACY_OPTIONS.get(index) : object;
            }

            @Override
            public String fromString(String string) {
                if (string == null) return "";
                int index = PRIVACY_OPTIONS.indexOf(string);
                return index != -1 ? PRIVACY_VALUES.get(index) : string;
            }
        });
    }

    private void navigateToPage(String pageName, String title) {
        try {
            PersonalCenterController centerController = AppStore.getPersonalCenterController();
            if (centerController != null) {
                // 优先通过 PersonalCenterController 导航
                centerController.navigateByPage(pageName, title);
            } else {
                // 备用方案：直接通过 MainFrameController 导航
                MainFrameController mainFrameController = AppStore.getMainFrameController();
                if (mainFrameController != null) {
                    mainFrameController.changeContent(pageName, title);
                } else {
                    showError("无法导航：找不到主窗口控制器");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("导航失败：" + e.getMessage());
        }
    }

    private void setupStatsCardEvents() {
        // 为每个统计卡片及其内部组件设置点击事件
        setupCardClickHandler(postCountBox, "my-posts", "我的帖子");
        setupCardClickHandler(followingCountBox, "my-following", "我的关注");
        setupCardClickHandler(followerCountBox, "my-followers", "我的粉丝");
        setupCardClickHandler(pointsBox, "point-history", "山竹瓣明细");
    }
    
    private void setupCardClickHandler(javafx.scene.layout.VBox card, String pageName, String title) {
        // 创建统一的点击事件处理器
        javafx.event.EventHandler<MouseEvent> clickHandler = event -> {
            event.consume();
            navigateToPage(pageName, title);
        };
        
        // 为 VBox 本身添加点击事件
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
        
        // 为 VBox 内的所有子组件也添加点击事件，确保无论点击哪个位置都能响应
        card.getChildren().forEach(child -> {
            child.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
        });
        
        // 设置鼠标样式，让用户知道这些卡片是可点击的
        card.setStyle("-fx-cursor: hand;");
    }

    private void loadUserData() {
        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.getCurrentUser();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                currentUser = task.getValue();
                if (currentUser != null) {
                    updateUI(currentUser);
                    loadPointsData();
                } else {
                    showError("加载用户数据失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载用户数据失败"));
        });

        new Thread(task).start();
    }

    private void loadPointsData() {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getMyPoints();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    Object pointsObj = data.get("points");
                    Object levelObj = data.get("level");
                    Object levelNameObj = data.get("levelName");
                    Object iconPathObj = data.get("iconPath");
                    Object nicknameStyleObj = data.get("nicknameStyle");

                    int points = pointsObj instanceof Number ? ((Number) pointsObj).intValue() : 0;
                    int level = levelObj instanceof Number ? ((Number) levelObj).intValue() : 0;
                    String levelName = levelNameObj instanceof String ? (String) levelNameObj : "";
                    String iconPath = iconPathObj instanceof String ? (String) iconPathObj : null;
                    String nicknameStyle = nicknameStyleObj instanceof String ? (String) nicknameStyleObj : "normal";

                    pointsLabel.setText(String.valueOf(points));
                    levelLabel.setText(levelName != null && !levelName.isEmpty() ? levelName : "Lv." + level);

                    if (iconPath != null && !iconPath.isEmpty()) {
                        try {
                            String fullIconUrl = iconPath.startsWith("/") ? HttpRequestUtil.serverUrl + iconPath : iconPath;
                            Image iconImage = new Image(fullIconUrl, true);
                            levelIconImageView.setImage(iconImage);
                        } catch (Exception e) {
                        }
                    }

                    applyNicknameStyle(nicknameStyle);
                }
            });
        });

        new Thread(task).start();
    }

    private void applyNicknameStyle(String style) {
        if ("bold_red".equals(style)) {
            nicknameTextField.setStyle("-fx-font-weight: bold; -fx-text-fill: #e53935;");
        } else if ("bold".equals(style)) {
            nicknameTextField.setStyle("-fx-font-weight: bold;");
        } else {
            nicknameTextField.setStyle("");
        }
    }

    private void updateUI(User user) {
        studentIdLabel.setText(user.getStudentId() != null ? user.getStudentId() : "");
        nicknameTextField.setText(user.getNickname() != null ? user.getNickname() : "");
        signatureTextArea.setText(user.getSignature() != null ? user.getSignature() : "");
        avatarUrlTextField.setText(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        personNameTextField.setText(user.getPersonName() != null ? user.getPersonName() : "");
        personDeptTextField.setText(user.getPersonDept() != null ? user.getPersonDept() : "");
        personGenderComboBox.setValue(user.getPersonGender() != null ? user.getPersonGender() : "");
        
        // 设置生日
        if (user.getPersonBirthday() != null && !user.getPersonBirthday().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(user.getPersonBirthday(), DATE_FORMATTER);
                personBirthdayPicker.setValue(date);
            } catch (Exception e) {
                personBirthdayPicker.setValue(null);
            }
        } else {
            personBirthdayPicker.setValue(null);
        }
        
        personEmailTextField.setText(user.getPersonEmail() != null ? user.getPersonEmail() : "");
        personPhoneTextField.setText(user.getPersonPhone() != null ? user.getPersonPhone() : "");
        personAddressTextField.setText(user.getPersonAddress() != null ? user.getPersonAddress() : "");
        personIntroduceTextArea.setText(user.getPersonIntroduce() != null ? user.getPersonIntroduce() : "");
        postCountLabel.setText(user.getPostCount() != null ? String.valueOf(user.getPostCount()) : "0");
        followingCountLabel.setText(user.getFollowingCount() != null ? String.valueOf(user.getFollowingCount()) : "0");
        followerCountLabel.setText(user.getFollowerCount() != null ? String.valueOf(user.getFollowerCount()) : "0");
        
        // 设置隐私选项
        setPrivacyComboBoxValue(namePrivacyComboBox, user.getNamePrivacy());
        setPrivacyComboBoxValue(deptPrivacyComboBox, user.getDeptPrivacy());
        setPrivacyComboBoxValue(genderPrivacyComboBox, user.getGenderPrivacy());
        setPrivacyComboBoxValue(birthdayPrivacyComboBox, user.getBirthdayPrivacy());
        setPrivacyComboBoxValue(emailPrivacyComboBox, user.getEmailPrivacy());
        setPrivacyComboBoxValue(phonePrivacyComboBox, user.getPhonePrivacy());
        setPrivacyComboBoxValue(addressPrivacyComboBox, user.getAddressPrivacy());
        setPrivacyComboBoxValue(introducePrivacyComboBox, user.getIntroducePrivacy());
        
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                String fullAvatarUrl = user.getAvatarUrl().startsWith("/") ? 
                    HttpRequestUtil.serverUrl + user.getAvatarUrl() : user.getAvatarUrl();
                Image avatarImage = new Image(fullAvatarUrl, true);
                avatarImageView.setImage(avatarImage);
            } catch (Exception e) {
            }
        }
        
        originalNickname = user.getNickname();
        originalSignature = user.getSignature();
        originalAvatarUrl = user.getAvatarUrl();
        originalPersonName = user.getPersonName();
        originalPersonDept = user.getPersonDept();
        originalPersonGender = user.getPersonGender();
        originalPersonBirthday = user.getPersonBirthday();
        originalPersonEmail = user.getPersonEmail();
        originalPersonPhone = user.getPersonPhone();
        originalPersonAddress = user.getPersonAddress();
        originalPersonIntroduce = user.getPersonIntroduce();
        originalNamePrivacy = user.getNamePrivacy();
        originalDeptPrivacy = user.getDeptPrivacy();
        originalGenderPrivacy = user.getGenderPrivacy();
        originalBirthdayPrivacy = user.getBirthdayPrivacy();
        originalEmailPrivacy = user.getEmailPrivacy();
        originalPhonePrivacy = user.getPhonePrivacy();
        originalAddressPrivacy = user.getAddressPrivacy();
        originalIntroducePrivacy = user.getIntroducePrivacy();
    }

    private void setPrivacyComboBoxValue(ComboBox<String> comboBox, String value) {
        if (value == null || value.isEmpty()) {
            value = "PUBLIC";
        }
        int index = PRIVACY_VALUES.indexOf(value);
        if (index != -1) {
            comboBox.setValue(PRIVACY_OPTIONS.get(index));
        } else {
            comboBox.setValue(value);
        }
    }

    private void toggleEditMode(boolean isEditing) {
        // 添加淡入淡出效果
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300));
        fadeTransition.setNode(statsGrid.getParent());
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
        
        nicknameTextField.setEditable(isEditing);
        nicknameTextField.setDisable(!isEditing);
        signatureTextArea.setEditable(isEditing);
        signatureTextArea.setDisable(!isEditing);
        avatarUrlTextField.setEditable(isEditing);
        avatarUrlTextField.setDisable(!isEditing);
        personNameTextField.setEditable(isEditing);
        personNameTextField.setDisable(!isEditing);
        personDeptTextField.setEditable(isEditing);
        personDeptTextField.setDisable(!isEditing);
        personGenderComboBox.setDisable(!isEditing);
        personBirthdayPicker.setDisable(!isEditing);
        personEmailTextField.setEditable(isEditing);
        personEmailTextField.setDisable(!isEditing);
        personPhoneTextField.setEditable(isEditing);
        personPhoneTextField.setDisable(!isEditing);
        personAddressTextField.setEditable(isEditing);
        personAddressTextField.setDisable(!isEditing);
        personIntroduceTextArea.setEditable(isEditing);
        personIntroduceTextArea.setDisable(!isEditing);
        
        // 隐私设置下拉框：编辑模式显示完整，查看模式禁用但可通过Tooltip查看
        namePrivacyComboBox.setDisable(!isEditing);
        deptPrivacyComboBox.setDisable(!isEditing);
        genderPrivacyComboBox.setDisable(!isEditing);
        birthdayPrivacyComboBox.setDisable(!isEditing);
        emailPrivacyComboBox.setDisable(!isEditing);
        phonePrivacyComboBox.setDisable(!isEditing);
        addressPrivacyComboBox.setDisable(!isEditing);
        introducePrivacyComboBox.setDisable(!isEditing);
        
        // 为查看模式添加Tooltip
        if (!isEditing) {
            addTooltipToPrivacyComboBox(namePrivacyComboBox);
            addTooltipToPrivacyComboBox(deptPrivacyComboBox);
            addTooltipToPrivacyComboBox(genderPrivacyComboBox);
            addTooltipToPrivacyComboBox(birthdayPrivacyComboBox);
            addTooltipToPrivacyComboBox(emailPrivacyComboBox);
            addTooltipToPrivacyComboBox(phonePrivacyComboBox);
            addTooltipToPrivacyComboBox(addressPrivacyComboBox);
            addTooltipToPrivacyComboBox(introducePrivacyComboBox);
        } else {
            // 移除Tooltip
            namePrivacyComboBox.setTooltip(null);
            deptPrivacyComboBox.setTooltip(null);
            genderPrivacyComboBox.setTooltip(null);
            birthdayPrivacyComboBox.setTooltip(null);
            emailPrivacyComboBox.setTooltip(null);
            phonePrivacyComboBox.setTooltip(null);
            addressPrivacyComboBox.setTooltip(null);
            introducePrivacyComboBox.setTooltip(null);
        }
        
        editButton.setVisible(!isEditing);
        editButton.setManaged(!isEditing);
        saveButton.setVisible(isEditing);
        saveButton.setManaged(isEditing);
        cancelButton.setVisible(isEditing);
        cancelButton.setManaged(isEditing);
    }

    private void addTooltipToPrivacyComboBox(ComboBox<String> comboBox) {
        String value = comboBox.getValue();
        if (value != null) {
            int index = PRIVACY_OPTIONS.indexOf(value);
            String description;
            if (index == 0) {
                description = "所有人可见";
            } else if (index == 1) {
                description = "仅您关注的人可见";
            } else {
                description = "仅您自己可见";
            }
            Tooltip tooltip = new Tooltip(description);
            comboBox.setTooltip(tooltip);
        }
    }

    private void onEdit() {
        toggleEditMode(true);
    }

    private void onSave() {
        // 表单验证
        if (!validateForm()) {
            return;
        }
        
        final String nickname = nicknameTextField.getText();
        final String signature = signatureTextArea.getText();
        final String avatarUrl = avatarUrlTextField.getText();
        final String personName = personNameTextField.getText();
        final String personDept = personDeptTextField.getText();
        final String personGender = personGenderComboBox.getValue();
        final String personBirthday;
        if (personBirthdayPicker.getValue() != null) {
            personBirthday = personBirthdayPicker.getValue().format(DATE_FORMATTER);
        } else {
            personBirthday = null;
        }
        final String personEmail = personEmailTextField.getText();
        final String personPhone = personPhoneTextField.getText();
        final String personAddress = personAddressTextField.getText();
        final String personIntroduce = personIntroduceTextArea.getText();
        final String namePrivacy = getPrivacyValue(namePrivacyComboBox.getValue());
        final String deptPrivacy = getPrivacyValue(deptPrivacyComboBox.getValue());
        final String genderPrivacy = getPrivacyValue(genderPrivacyComboBox.getValue());
        final String birthdayPrivacy = getPrivacyValue(birthdayPrivacyComboBox.getValue());
        final String emailPrivacy = getPrivacyValue(emailPrivacyComboBox.getValue());
        final String phonePrivacy = getPrivacyValue(phonePrivacyComboBox.getValue());
        final String addressPrivacy = getPrivacyValue(addressPrivacyComboBox.getValue());
        final String introducePrivacy = getPrivacyValue(introducePrivacyComboBox.getValue());

        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.updateUserProfile(nickname, signature, avatarUrl, 
                                                         personName, personDept, personGender, 
                                                         personBirthday, personEmail, personPhone, 
                                                         personAddress, personIntroduce,
                                                         namePrivacy, deptPrivacy, genderPrivacy,
                                                         birthdayPrivacy, emailPrivacy, phonePrivacy,
                                                         addressPrivacy, introducePrivacy);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                User updatedUser = task.getValue();
                if (updatedUser != null) {
                    currentUser = updatedUser;
                    updateUI(updatedUser);
                    toggleEditMode(false);
                    showSuccess("个人信息更新成功");
                } else {
                    showError("个人信息更新失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("个人信息更新失败"));
        });

        new Thread(task).start();
    }

    private String getPrivacyValue(String displayValue) {
        if (displayValue == null || displayValue.isEmpty()) {
            return "PUBLIC";
        }
        int index = PRIVACY_OPTIONS.indexOf(displayValue);
        return index != -1 ? PRIVACY_VALUES.get(index) : displayValue;
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // 昵称验证：2-20字符
        String nickname = nicknameTextField.getText();
        if (nickname == null || nickname.trim().length() < 2 || nickname.trim().length() > 20) {
            setFieldError(nicknameTextField, "昵称长度应在2-20个字符之间");
            isValid = false;
        } else {
            clearFieldError(nicknameTextField);
        }
        
        // 邮箱验证
        String email = personEmailTextField.getText();
        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError(personEmailTextField, "请输入有效的邮箱地址");
            isValid = false;
        } else {
            clearFieldError(personEmailTextField);
        }
        
        // 电话验证
        String phone = personPhoneTextField.getText();
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            setFieldError(personPhoneTextField, "请输入有效的11位手机号码");
            isValid = false;
        } else {
            clearFieldError(personPhoneTextField);
        }
        
        // URL验证
        String url = avatarUrlTextField.getText();
        if (url != null && !url.isEmpty() && !URL_PATTERN.matcher(url).matches()) {
            setFieldError(avatarUrlTextField, "请输入有效的URL地址");
            isValid = false;
        } else {
            clearFieldError(avatarUrlTextField);
        }
        
        return isValid;
    }

    private void setFieldError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        Tooltip tooltip = new Tooltip(message);
        tooltip.setStyle("-fx-text-fill: red;");
        field.setTooltip(tooltip);
    }

    private void clearFieldError(TextField field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private void onCancel() {
        nicknameTextField.setText(originalNickname != null ? originalNickname : "");
        signatureTextArea.setText(originalSignature != null ? originalSignature : "");
        avatarUrlTextField.setText(originalAvatarUrl != null ? originalAvatarUrl : "");
        personNameTextField.setText(originalPersonName != null ? originalPersonName : "");
        personDeptTextField.setText(originalPersonDept != null ? originalPersonDept : "");
        personGenderComboBox.setValue(originalPersonGender != null ? originalPersonGender : "");
        
        // 恢复生日
        if (originalPersonBirthday != null && !originalPersonBirthday.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(originalPersonBirthday, DATE_FORMATTER);
                personBirthdayPicker.setValue(date);
            } catch (Exception e) {
                personBirthdayPicker.setValue(null);
            }
        } else {
            personBirthdayPicker.setValue(null);
        }
        
        personEmailTextField.setText(originalPersonEmail != null ? originalPersonEmail : "");
        personPhoneTextField.setText(originalPersonPhone != null ? originalPersonPhone : "");
        personAddressTextField.setText(originalPersonAddress != null ? originalPersonAddress : "");
        personIntroduceTextArea.setText(originalPersonIntroduce != null ? originalPersonIntroduce : "");
        
        // 恢复隐私设置
        setPrivacyComboBoxValue(namePrivacyComboBox, originalNamePrivacy);
        setPrivacyComboBoxValue(deptPrivacyComboBox, originalDeptPrivacy);
        setPrivacyComboBoxValue(genderPrivacyComboBox, originalGenderPrivacy);
        setPrivacyComboBoxValue(birthdayPrivacyComboBox, originalBirthdayPrivacy);
        setPrivacyComboBoxValue(emailPrivacyComboBox, originalEmailPrivacy);
        setPrivacyComboBoxValue(phonePrivacyComboBox, originalPhonePrivacy);
        setPrivacyComboBoxValue(addressPrivacyComboBox, originalAddressPrivacy);
        setPrivacyComboBoxValue(introducePrivacyComboBox, originalIntroducePrivacy);
        
        // 清除错误状态
        clearFieldError(nicknameTextField);
        clearFieldError(personEmailTextField);
        clearFieldError(personPhoneTextField);
        clearFieldError(avatarUrlTextField);
        
        if (originalAvatarUrl != null && !originalAvatarUrl.isEmpty()) {
            try {
                String fullOriginalUrl = originalAvatarUrl.startsWith("/") ? 
                    HttpRequestUtil.serverUrl + originalAvatarUrl : originalAvatarUrl;
                Image avatarImage = new Image(fullOriginalUrl, true);
                avatarImageView.setImage(avatarImage);
            } catch (Exception e) {
            }
        }
        
        toggleEditMode(false);
    }

    private void selectAndUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择头像");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return;
        }
        
        try {
            Image localImage = new Image(selectedFile.toURI().toString());
            avatarImageView.setImage(localImage);
        } catch (Exception e) {
            showError("图片预览失败");
            return;
        }
        
        String originalButtonText = uploadAvatarButton.getText();
        uploadAvatarButton.setDisable(true);
        uploadAvatarButton.setText("上传中...");
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return HttpRequestUtil.uploadImage(selectedFile.getAbsolutePath());
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                String avatarUrl = task.getValue();
                if (avatarUrl != null && !avatarUrl.isBlank()) {
                    avatarUrlTextField.setText(avatarUrl);
                    String fullAvatarUrl = HttpRequestUtil.serverUrl + avatarUrl;
                    try {
                        Image avatarImage = new Image(fullAvatarUrl, true);
                        avatarImageView.setImage(avatarImage);
                    } catch (Exception e) {
                    }
                    saveProfileAfterAvatarUpload(avatarUrl);
                } else {
                    showError("头像上传失败");
                    if (originalAvatarUrl != null && !originalAvatarUrl.isEmpty()) {
                        try {
                            String fullOriginalUrl = originalAvatarUrl.startsWith("/") ? 
                                HttpRequestUtil.serverUrl + originalAvatarUrl : originalAvatarUrl;
                            Image avatarImage = new Image(fullOriginalUrl, true);
                            avatarImageView.setImage(avatarImage);
                        } catch (Exception e) {
                        }
                    }
                }
                uploadAvatarButton.setDisable(false);
                uploadAvatarButton.setText(originalButtonText);
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("头像上传失败");
                if (originalAvatarUrl != null && !originalAvatarUrl.isEmpty()) {
                    try {
                        String fullOriginalUrl = originalAvatarUrl.startsWith("/") ? 
                            HttpRequestUtil.serverUrl + originalAvatarUrl : originalAvatarUrl;
                        Image avatarImage = new Image(fullOriginalUrl, true);
                        avatarImageView.setImage(avatarImage);
                    } catch (Exception e) {
                    }
                }
                uploadAvatarButton.setDisable(false);
                uploadAvatarButton.setText(originalButtonText);
            });
        });
        
        new Thread(task).start();
    }
    
    private void saveProfileAfterAvatarUpload(String avatarUrl) {
        final String nickname = nicknameTextField.getText() != null ? nicknameTextField.getText() : "";
        final String signature = signatureTextArea.getText() != null ? signatureTextArea.getText() : "";
        final String personName = personNameTextField.getText() != null ? personNameTextField.getText() : "";
        final String personDept = personDeptTextField.getText() != null ? personDeptTextField.getText() : "";
        final String personGender = personGenderComboBox.getValue();
        final String personBirthday;
        if (personBirthdayPicker.getValue() != null) {
            personBirthday = personBirthdayPicker.getValue().format(DATE_FORMATTER);
        } else {
            personBirthday = null;
        }
        final String personEmail = personEmailTextField.getText() != null ? personEmailTextField.getText() : "";
        final String personPhone = personPhoneTextField.getText() != null ? personPhoneTextField.getText() : "";
        final String personAddress = personAddressTextField.getText() != null ? personAddressTextField.getText() : "";
        final String personIntroduce = personIntroduceTextArea.getText() != null ? personIntroduceTextArea.getText() : "";
        final String namePrivacy = getPrivacyValue(namePrivacyComboBox.getValue());
        final String deptPrivacy = getPrivacyValue(deptPrivacyComboBox.getValue());
        final String genderPrivacy = getPrivacyValue(genderPrivacyComboBox.getValue());
        final String birthdayPrivacy = getPrivacyValue(birthdayPrivacyComboBox.getValue());
        final String emailPrivacy = getPrivacyValue(emailPrivacyComboBox.getValue());
        final String phonePrivacy = getPrivacyValue(phonePrivacyComboBox.getValue());
        final String addressPrivacy = getPrivacyValue(addressPrivacyComboBox.getValue());
        final String introducePrivacy = getPrivacyValue(introducePrivacyComboBox.getValue());

        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.updateUserProfile(nickname, signature, avatarUrl, 
                                                         personName, personDept, personGender, 
                                                         personBirthday, personEmail, personPhone, 
                                                         personAddress, personIntroduce,
                                                         namePrivacy, deptPrivacy, genderPrivacy,
                                                         birthdayPrivacy, emailPrivacy, phonePrivacy,
                                                         addressPrivacy, introducePrivacy);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                User updatedUser = task.getValue();
                if (updatedUser != null) {
                    currentUser = updatedUser;
                    originalAvatarUrl = avatarUrl;
                    updateUI(updatedUser);
                    showSuccess("头像上传并保存成功");
                } else {
                    showError("头像上传成功，但保存信息失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("头像上传成功，但保存信息失败"));
        });

        new Thread(task).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
