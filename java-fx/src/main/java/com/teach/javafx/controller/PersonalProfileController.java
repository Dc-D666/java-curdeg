package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class PersonalProfileController extends ToolController {
    @FXML
    private ImageView avatarImageView;
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
    private TextField personBirthdayTextField;
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

    @FXML
    public void initialize() {
        ObservableList<String> genderOptions = FXCollections.observableArrayList("", "男", "女");
        personGenderComboBox.setItems(genderOptions);
        
        ObservableList<String> privacyOptions = FXCollections.observableArrayList("PUBLIC", "FOLLOWING", "PRIVATE");
        namePrivacyComboBox.setItems(privacyOptions);
        deptPrivacyComboBox.setItems(privacyOptions);
        genderPrivacyComboBox.setItems(privacyOptions);
        birthdayPrivacyComboBox.setItems(privacyOptions);
        emailPrivacyComboBox.setItems(privacyOptions);
        phonePrivacyComboBox.setItems(privacyOptions);
        addressPrivacyComboBox.setItems(privacyOptions);
        introducePrivacyComboBox.setItems(privacyOptions);
        
        loadUserData();
        toggleEditMode(false);
        
        editButton.setOnAction(event -> onEdit());
        saveButton.setOnAction(event -> onSave());
        cancelButton.setOnAction(event -> onCancel());
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

    private void updateUI(User user) {
        studentIdLabel.setText(user.getStudentId() != null ? user.getStudentId() : "");
        nicknameTextField.setText(user.getNickname() != null ? user.getNickname() : "");
        signatureTextArea.setText(user.getSignature() != null ? user.getSignature() : "");
        avatarUrlTextField.setText(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        personNameTextField.setText(user.getPersonName() != null ? user.getPersonName() : "");
        personDeptTextField.setText(user.getPersonDept() != null ? user.getPersonDept() : "");
        personGenderComboBox.setValue(user.getPersonGender() != null ? user.getPersonGender() : "");
        personBirthdayTextField.setText(user.getPersonBirthday() != null ? user.getPersonBirthday() : "");
        personEmailTextField.setText(user.getPersonEmail() != null ? user.getPersonEmail() : "");
        personPhoneTextField.setText(user.getPersonPhone() != null ? user.getPersonPhone() : "");
        personAddressTextField.setText(user.getPersonAddress() != null ? user.getPersonAddress() : "");
        personIntroduceTextArea.setText(user.getPersonIntroduce() != null ? user.getPersonIntroduce() : "");
        postCountLabel.setText(user.getPostCount() != null ? String.valueOf(user.getPostCount()) : "0");
        followingCountLabel.setText(user.getFollowingCount() != null ? String.valueOf(user.getFollowingCount()) : "0");
        followerCountLabel.setText(user.getFollowerCount() != null ? String.valueOf(user.getFollowerCount()) : "0");
        
        namePrivacyComboBox.setValue(user.getNamePrivacy() != null ? user.getNamePrivacy() : "PUBLIC");
        deptPrivacyComboBox.setValue(user.getDeptPrivacy() != null ? user.getDeptPrivacy() : "PUBLIC");
        genderPrivacyComboBox.setValue(user.getGenderPrivacy() != null ? user.getGenderPrivacy() : "PUBLIC");
        birthdayPrivacyComboBox.setValue(user.getBirthdayPrivacy() != null ? user.getBirthdayPrivacy() : "PUBLIC");
        emailPrivacyComboBox.setValue(user.getEmailPrivacy() != null ? user.getEmailPrivacy() : "PUBLIC");
        phonePrivacyComboBox.setValue(user.getPhonePrivacy() != null ? user.getPhonePrivacy() : "PUBLIC");
        addressPrivacyComboBox.setValue(user.getAddressPrivacy() != null ? user.getAddressPrivacy() : "PUBLIC");
        introducePrivacyComboBox.setValue(user.getIntroducePrivacy() != null ? user.getIntroducePrivacy() : "PUBLIC");
        
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

    private void toggleEditMode(boolean isEditing) {
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
        personBirthdayTextField.setEditable(isEditing);
        personBirthdayTextField.setDisable(!isEditing);
        personEmailTextField.setEditable(isEditing);
        personEmailTextField.setDisable(!isEditing);
        personPhoneTextField.setEditable(isEditing);
        personPhoneTextField.setDisable(!isEditing);
        personAddressTextField.setEditable(isEditing);
        personAddressTextField.setDisable(!isEditing);
        personIntroduceTextArea.setEditable(isEditing);
        personIntroduceTextArea.setDisable(!isEditing);
        namePrivacyComboBox.setDisable(!isEditing);
        deptPrivacyComboBox.setDisable(!isEditing);
        genderPrivacyComboBox.setDisable(!isEditing);
        birthdayPrivacyComboBox.setDisable(!isEditing);
        emailPrivacyComboBox.setDisable(!isEditing);
        phonePrivacyComboBox.setDisable(!isEditing);
        addressPrivacyComboBox.setDisable(!isEditing);
        introducePrivacyComboBox.setDisable(!isEditing);
        
        editButton.setVisible(!isEditing);
        editButton.setManaged(!isEditing);
        saveButton.setVisible(isEditing);
        saveButton.setManaged(isEditing);
        cancelButton.setVisible(isEditing);
        cancelButton.setManaged(isEditing);
    }

    private void onEdit() {
        toggleEditMode(true);
    }

    private void onSave() {
        String nickname = nicknameTextField.getText();
        String signature = signatureTextArea.getText();
        String avatarUrl = avatarUrlTextField.getText();
        String personName = personNameTextField.getText();
        String personDept = personDeptTextField.getText();
        String personGender = personGenderComboBox.getValue();
        String personBirthday = personBirthdayTextField.getText();
        String personEmail = personEmailTextField.getText();
        String personPhone = personPhoneTextField.getText();
        String personAddress = personAddressTextField.getText();
        String personIntroduce = personIntroduceTextArea.getText();
        String namePrivacy = namePrivacyComboBox.getValue();
        String deptPrivacy = deptPrivacyComboBox.getValue();
        String genderPrivacy = genderPrivacyComboBox.getValue();
        String birthdayPrivacy = birthdayPrivacyComboBox.getValue();
        String emailPrivacy = emailPrivacyComboBox.getValue();
        String phonePrivacy = phonePrivacyComboBox.getValue();
        String addressPrivacy = addressPrivacyComboBox.getValue();
        String introducePrivacy = introducePrivacyComboBox.getValue();

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

    private void onCancel() {
        nicknameTextField.setText(originalNickname != null ? originalNickname : "");
        signatureTextArea.setText(originalSignature != null ? originalSignature : "");
        avatarUrlTextField.setText(originalAvatarUrl != null ? originalAvatarUrl : "");
        personNameTextField.setText(originalPersonName != null ? originalPersonName : "");
        personDeptTextField.setText(originalPersonDept != null ? originalPersonDept : "");
        personGenderComboBox.setValue(originalPersonGender != null ? originalPersonGender : "");
        personBirthdayTextField.setText(originalPersonBirthday != null ? originalPersonBirthday : "");
        personEmailTextField.setText(originalPersonEmail != null ? originalPersonEmail : "");
        personPhoneTextField.setText(originalPersonPhone != null ? originalPersonPhone : "");
        personAddressTextField.setText(originalPersonAddress != null ? originalPersonAddress : "");
        personIntroduceTextArea.setText(originalPersonIntroduce != null ? originalPersonIntroduce : "");
        namePrivacyComboBox.setValue(originalNamePrivacy != null ? originalNamePrivacy : "PUBLIC");
        deptPrivacyComboBox.setValue(originalDeptPrivacy != null ? originalDeptPrivacy : "PUBLIC");
        genderPrivacyComboBox.setValue(originalGenderPrivacy != null ? originalGenderPrivacy : "PUBLIC");
        birthdayPrivacyComboBox.setValue(originalBirthdayPrivacy != null ? originalBirthdayPrivacy : "PUBLIC");
        emailPrivacyComboBox.setValue(originalEmailPrivacy != null ? originalEmailPrivacy : "PUBLIC");
        phonePrivacyComboBox.setValue(originalPhonePrivacy != null ? originalPhonePrivacy : "PUBLIC");
        addressPrivacyComboBox.setValue(originalAddressPrivacy != null ? originalAddressPrivacy : "PUBLIC");
        introducePrivacyComboBox.setValue(originalIntroducePrivacy != null ? originalIntroducePrivacy : "PUBLIC");
        toggleEditMode(false);
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
