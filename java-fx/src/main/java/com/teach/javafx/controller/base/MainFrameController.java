package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.MyTreeNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.LoginRequest;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * MainFrameController 登录交互控制类 对应 base/main-frame.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class MainFrameController {
    class ChangePanelHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            changeContent(actionEvent);
        }
    }
    private Map<String,Tab> tabMap = new HashMap<String,Tab>();
    private Map<String,Scene> sceneMap = new HashMap<String,Scene>();
    private Map<String,ToolController> controlMap =new HashMap<String,ToolController>();
    
    private com.teach.javafx.controller.PostListController postListController;
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuHome;
    @FXML
    private MenuItem menuPostList;
    @FXML
    private Menu menuTool;
    @FXML
    private Menu menuMy;
    @FXML
    private Menu menuSwitchAccount;
    @FXML
    private TreeView<MyTreeNode> menuTree;
    @FXML
    protected TabPane contentTabPane;
    @FXML
    private Label systemPrompt;
    @FXML
    private Label unreadNotificationLabel;

    private ChangePanelHandler handler= null;

    void addMenuItems(Menu parent, List<Map> mList) {
        String name, title;
        List sList;
        Map ms;
        Menu menu;
        MenuItem item;
        for ( Map m :mList) {
            sList = (List<Map>)m.get("sList");
            name = (String)m.get("name");
            title = (String)m.get("title");
            if(sList == null || sList.size()== 0) {
                item = new MenuItem();
                item.setId(name);
                item.setText(title);
                item.setOnAction(this::changeContent);
                parent.getItems().add(item);
            }else {
                menu = new Menu();
                menu.setText(title);
                addMenuItems(menu,sList);
                parent.getItems().add(menu);
            }
        }
    }

    /**
     * 页面加载对象创建完成初始话方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     * 系统初始时为没个角色增加了框架已经实现好了基础管理的功能，采用代码显示添加的方法加入，加入完缺省的功能菜单后，通过
     * HttpRequestUtil.request("/api/base/getMenuList",new DataRequest())加载用菜单管理功能，维护的菜单
     * 项目开发过程中，同学可以扩该方法，增肌自己设计的功能菜单，也可以通过菜单管理程序添加菜单，框架自动加载菜单管理维护的菜单，
     * 是新功能扩展
     */
    public void addMenuItem(Menu menu, String name, String title){
        MenuItem item;
        item = new MenuItem();
        item.setText(title);
        item.setId(name);
        item.setOnAction(this::changeContent);
        menu.getItems().add(item);
    }
    public void initMenuBar(List<Map> mList){
        Menu menu;
        Map m;
        int i;
        List<Map> sList;
        for(i = 0; i < mList.size();i++) {
            m = mList.get(i);
            sList = (List<Map>)m.get("sList");
            menu = new Menu();
            menu.setText((String)m.get("title"));
            if(sList != null && sList.size()> 0) {
                addMenuItems(menu,sList);
            }
            menuBar.getMenus().add(menu);
        }
    }
    void addMenuItems( TreeItem<MyTreeNode> parent, List<Map> mList) {
        List sList;
        TreeItem<MyTreeNode> menu;
        for ( Map m :mList) {
            sList = (List<Map>)m.get("sList");
            menu = new TreeItem<>(new MyTreeNode(null,(String)m.get("name") ,(String)m.get("title"),0));
            parent.getChildren().add(menu);
            // 展开所有子节点
            menu.setExpanded(true);
            if(sList !=  null && sList.size()> 0) {
                addMenuItems(menu, sList);
            }
        }
    }

    public void initMenuTree(List<Map> mList) {
        String role = AppStore.getJwt().getRole();
        MyTreeNode node = new MyTreeNode(null, null,"菜单",0);
        TreeItem<MyTreeNode> root = new TreeItem<>(node);
        TreeItem<MyTreeNode>  menu;
        int i,j;
        Map m;
        List<Map> sList;
        for(i = 0; i < mList.size();i++) {
            m = mList.get(i);
            sList = (List<Map>)m.get("sList");
            menu = new TreeItem<>(new MyTreeNode(null, (String)m.get("name"), (String)m.get("title"), (Integer)m.get("isLeft")));
            // 展开一级菜单
            menu.setExpanded(true);
            if(sList != null && sList.size()> 0) {
                addMenuItems(menu,sList);
            }
            root.getChildren().add(menu);
        }
        menuTree.setRoot(root);
        menuTree.setShowRoot(false);
        menuTree.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                Node node = event.getPickResult().getIntersectedNode();
                TreeItem<MyTreeNode> treeItem = menuTree.getSelectionModel().getSelectedItem();
                if(treeItem == null)
                    return;
                MyTreeNode menu = treeItem.getValue();
                if(menu == null)
                    return;
                String name = menu.getValue();
                if(name == null || name.length() == 0)
                    return ;
                if("logout".equals(name)) {
                    logout();
                }else if(name.endsWith("Command")){
                    try {
                        Method m = this.getClass().getMethod(name);
                        m.invoke(this);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    changeContent(name,menu.getLabel());
                }
            }
        });
    }
    @FXML
    public void initialize() {
        handler =new ChangePanelHandler();
        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        contentTabPane.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");
        
        unreadNotificationLabel.setOnMouseClicked(event -> {
            changeContent("my-notification", "我的通知");
        });
        
        Task<List<Map>> menuTask = new Task<List<Map>>() {
            @Override
            protected List<Map> call() {
                DataRequest request = new DataRequest();
                DataResponse response = HttpRequestUtil.request("/api/base/getMenuList", request);
                if (response.getCode() == 0 && response.getData() != null) {
                    return (List<Map>) response.getData();
                }
                return null;
            }
        };
        
        menuTask.setOnSucceeded(event -> {
            List<Map> menuList = menuTask.getValue();
            if (menuList != null && !menuList.isEmpty()) {
                Platform.runLater(() -> {
                    initMenuTree(menuList);
                });
            }
        });
        
        new Thread(menuTask).start();
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                String role = AppStore.getJwt().getRole();
                User currentUser = HttpRequestUtil.getCurrentUser();
                
                Platform.runLater(() -> {
                    String displayRole = role;
                    
                    if (currentUser != null && currentUser.getAuthority() != null) {
                        displayRole = currentUser.getAuthority();
                    }
                    
                    systemPrompt.setText("服务器：http://localhost:22223 数据库：java_2_48 团队编号：48 成员：202400210090 戴睿羲 202333180042 南迪 202420210070 张心甜 202420210042 吴煜菲 202400210027 刘鑫");
                });
                return null;
            }
        };
        new Thread(task).start();
        
        loadUnreadNotificationCount();

        String currentUsername = AppStore.getJwt().getUsername();
        
        MenuItem superItem = new MenuItem("super");
        MenuItem admin1Item = new MenuItem("admin1");
        MenuItem admin2Item = new MenuItem("admin2");
        MenuItem user1Item = new MenuItem("user1");
        MenuItem user2Item = new MenuItem("user2");
        
        menuSwitchAccount.getItems().addAll(superItem, admin1Item, admin2Item, user1Item, user2Item);
        
        if ("super".equals(currentUsername)) {
            superItem.setDisable(true);
        } else if ("admin1".equals(currentUsername)) {
            admin1Item.setDisable(true);
        } else if ("admin2".equals(currentUsername)) {
            admin2Item.setDisable(true);
        } else if ("user1".equals(currentUsername)) {
            user1Item.setDisable(true);
        } else if ("user2".equals(currentUsername)) {
            user2Item.setDisable(true);
        }
        
        superItem.setOnAction(e -> switchToAccount("super"));
        admin1Item.setOnAction(e -> switchToAccount("admin1"));
        admin2Item.setOnAction(e -> switchToAccount("admin2"));
        user1Item.setOnAction(e -> switchToAccount("user1"));
        user2Item.setOnAction(e -> switchToAccount("user2"));
    }


    /**
     * 点击菜单栏中的“退出”菜单，执行onLogoutMenuClick方法 加载登录页面，切换回登录界面
     * @param event
     */
    @FXML
    protected void onLogoutMenuClick(ActionEvent event){
        logout();
    }

    protected void logout(){
        AppStore.setJwt(null);
        AppStore.setMainFrameController(null);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            MainApplication.loginStage("Login", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  void changeContent(ActionEvent ae) {
        Object obj = ae.getSource();
        String name= null, title= null;
        if(obj instanceof MenuItem) {
            MenuItem item = (MenuItem)obj;
            name = item.getId();
            title = item.getText();
        }
        if(name == null)
            return;
        changeContent(name,title);
    }

    /**
     * 点击菜单栏中的菜单 执行changeContent 在主框架工作区增加和显示一个工作面板
     * @param name  菜单名 name.fxml 对应面板的配置文件
     * @param title 菜单标题 工作区中的TablePane的标题
     */

    public  void changeContent(String name, String title) {
        System.out.println("=== changeContent called ===");
        System.out.println("Original name: " + name);
        System.out.println("title: " + title);
        
        // 驼峰转 kebab-case
        String actualName = camelToKebab(name);
        System.out.println("Looking for: " + actualName + ".fxml");
        
        if(actualName == null || actualName.length() == 0)
            return;
        Tab tab = tabMap.get(actualName);
        Scene scene;
        Object c;
        if(tab == null) {
            scene = sceneMap.get(actualName);
            if(scene == null) {
                java.net.URL resource = MainApplication.class.getResource(actualName + ".fxml");
                System.out.println("Resource found: " + resource);
                if (resource == null) {
                    System.out.println("ERROR: FXML file not found for: " + actualName);
                    return;
                }
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                try {
                    scene = new Scene(fxmlLoader.load(), 1024, 768);
                    sceneMap.put(actualName, scene);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                c = fxmlLoader.getController();
                if(c instanceof ToolController) {
                    controlMap.put(actualName,(ToolController)c);
                }
                if ("post-list".equals(actualName) && c instanceof com.teach.javafx.controller.PostListController) {
                    postListController = (com.teach.javafx.controller.PostListController) c;
                }
                if ("post-publish".equals(actualName) && c instanceof com.teach.javafx.controller.PostPublishController) {
                    ((com.teach.javafx.controller.PostPublishController) c).setMainFrameController(this);
                }
            }
            tab = new Tab(title);
            tab.setId(actualName);
            tab.setOnSelectionChanged(this::tabSelectedChanged);
            tab.setOnClosed(this::tabOnClosed);
            tab.setContent(scene.getRoot());
            contentTabPane.getTabs().add(tab);
            tabMap.put(actualName, tab);
        }
        contentTabPane.getSelectionModel().select(tab);
    }
    
    // 驼峰转 kebab-case
    private String camelToKebab(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public void changeContentWithScene(String name, String title, Scene scene, Object controller) {
        if(name == null || name.length() == 0)
            return;
        Tab tab = tabMap.get(name);
        if(tab == null) {
            sceneMap.put(name, scene);
            if(controller instanceof ToolController) {
                controlMap.put(name,(ToolController)controller);
            }
            tab = new Tab(title);
            tab.setId(name);
            tab.setOnSelectionChanged(this::tabSelectedChanged);
            tab.setOnClosed(this::tabOnClosed);
            tab.setContent(scene.getRoot());
            contentTabPane.getTabs().add(tab);
            tabMap.put(name, tab);
        }
        contentTabPane.getSelectionModel().select(tab);
    }

    public void tabSelectedChanged(Event e) {
        Tab tab = (Tab)e.getSource();
        String name = tab.getId();
        ToolController c = controlMap.get(name);
        if(c != null)
            c.doRefresh();
//        Node node =tab.getContent();
//        Scene scene = node.getScene();

    }

    /**
     * 点击TablePane 标签页 的关闭图标 执行tabOnClosed方法
     * @param e
     */

    public void tabOnClosed(Event e) {
        Tab tab = (Tab)e.getSource();
        String name = tab.getId();
        contentTabPane.getTabs().remove(tab);
        tabMap.remove(name);
        sceneMap.remove(name);
        controlMap.remove(name);
        if ("post-list".equals(name)) {
            postListController = null;
        }
    }
    
    public com.teach.javafx.controller.PostListController getPostListController() {
        return postListController;
    }
    /**
     * ToolController getCurrentToolController() 获取当前显示的面板的控制对象， 如果面板响应编辑菜单中的编辑命名，交互控制需要继承 ToolController， 重写里面的方法
     * @return
     */
    public ToolController getCurrentToolController(){
        Iterator<String> iterator = controlMap.keySet().iterator();
        String name;
        Tab tab;
        while(iterator.hasNext()) {
            name = iterator.next();
            tab = tabMap.get(name);
            if(tab.isSelected()) {
                return controlMap.get(name);
            }
        }
        return null;
    }
    /**
     * 点击编辑菜单中的“新建”菜单，执行doNewCommand方法， 执行当前显示的面板对应的控制类中的doNew()方法
     */
    protected  void doNewCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doNew();
    }
    /**
     * 点击编辑菜单中的“保存”菜单，执行doSaveCommand方法， 执行当前显示的面板对应的控制类中的doSave()方法
     */
    protected  void doSaveCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doSave();
    }
    /**
     * 点击编辑菜单中的“删除”菜单，执行doDeleteCommand方法， 执行当前显示的面板对应的控制类中的doDelete()方法
     */
    protected  void doDeleteCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doDelete();
    }
    /**
     * 点击编辑菜单中的“打印”菜单，执行doPrintCommand方法， 执行当前显示的面板对应的控制类中的doPrint()方法
     */
    protected  void doPrintCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doPrint();
    }
    /**
     * 点击编辑菜单中的“导出”菜单，执行doExportCommand方法， 执行当前显示的面板对应的控制类中的doExport方法
     */
    protected  void doExportCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doExport();
    }
    /**
     * 点击编辑菜单中的“导入”菜单，执行doImportCommand方法， 执行当前显示的面板对应的控制类中的doImport()方法
     */
    protected  void doImportCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doImport();
    }
    /**
     * 点击编辑菜单中的“测试”菜单，执行doTestCommand方法， 执行当前显示的面板对应的控制类中的doImport()方法
     */
    protected  void doTestCommand(){
        ToolController c = getCurrentToolController();
        if(c == null) {
            c= new ToolController(){
            };
        }
        c.doTest();
    }
    public ToolController getToolController(String name){
        return  controlMap.get(name);
    }

    public void closeCurrentTab() {
        Tab currentTab = contentTabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            String name = currentTab.getId();
            contentTabPane.getTabs().remove(currentTab);
            tabMap.remove(name);
            sceneMap.remove(name);
            controlMap.remove(name);
            if ("post-list".equals(name)) {
                postListController = null;
            }
        }
    }

    public void openUserHome(Integer userId, String nickname) {
        String tabId = "user-home-" + userId;
        String tabTitle = (nickname != null ? nickname : "用户") + "的主页";
        
        Tab existingTab = tabMap.get(tabId);
        if (existingTab != null) {
            contentTabPane.getSelectionModel().select(existingTab);
            return;
        }
        
        try {
            java.net.URL resource = MainApplication.class.getResource("user-home.fxml");
            if (resource == null) {
                System.out.println("ERROR: user-home.fxml not found");
                return;
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
            
            Object controller = fxmlLoader.getController();
            if (controller instanceof com.teach.javafx.controller.UserHomeController) {
                ((com.teach.javafx.controller.UserHomeController) controller).setUserId(userId);
            }
            
            Tab tab = new Tab(tabTitle);
            tab.setId(tabId);
            tab.setOnSelectionChanged(this::tabSelectedChanged);
            tab.setOnClosed(this::tabOnClosed);
            tab.setContent(scene.getRoot());
            
            contentTabPane.getTabs().add(tab);
            tabMap.put(tabId, tab);
            sceneMap.put(tabId, scene);
            if (controller instanceof ToolController) {
                controlMap.put(tabId, (ToolController) controller);
            }
            
            contentTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onPostListMenuClick(ActionEvent event) {
        changeContent("post-list", "帖子列表");
    }
    
    @FXML
    protected void onNewMenuClick(ActionEvent event) {
        doNewCommand();
    }
    
    @FXML
    protected void onSaveMenuClick(ActionEvent event) {
        doSaveCommand();
    }
    
    @FXML
    protected void onDeleteMenuClick(ActionEvent event) {
        doDeleteCommand();
    }
    
    @FXML
    protected void onPrintMenuClick(ActionEvent event) {
        doPrintCommand();
    }
    
    @FXML
    protected void onImportMenuClick(ActionEvent event) {
        doImportCommand();
    }
    
    @FXML
    protected void onExportMenuClick(ActionEvent event) {
        doExportCommand();
    }
    
    public void loadUnreadNotificationCount() {
        Task<Long> task = new Task<Long>() {
            @Override
            protected Long call() {
                return HttpRequestUtil.getUnreadNotificationCount();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Long count = task.getValue();
                if (count != null && count > 0) {
                    unreadNotificationLabel.setText("(" + count + " 条未读)");
                } else {
                    unreadNotificationLabel.setText("");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                unreadNotificationLabel.setText("");
            });
        });
        
        new Thread(task).start();
    }
    
    private void switchToAccount(String username) {
        AppStore.setJwt(null);
        LoginRequest loginRequest = new LoginRequest(username, "123456");
        String errorMsg = HttpRequestUtil.login(loginRequest);
        if (errorMsg != null) {
            MessageDialog.showDialog(errorMsg);
            return;
        }
        sceneMap.clear();
        tabMap.clear();
        controlMap.clear();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/main-frame.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            AppStore.setMainFrameController((MainFrameController) fxmlLoader.getController());
            
            // 使用两个阶段来确保窗口正确显示
            Stage stage = MainApplication.getMainStage();
            // 第一阶段：先取消全屏，设置新场景
            Platform.runLater(() -> {
                stage.setMaximized(false);
                stage.setTitle("学生交流社区");
                stage.setScene(scene);
                stage.show();
            });
            // 第二阶段：延迟100ms后再设置全屏
            Platform.runLater(() -> {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                pause.setOnFinished(event -> {
                    stage.setMaximized(true);
                });
                pause.play();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}