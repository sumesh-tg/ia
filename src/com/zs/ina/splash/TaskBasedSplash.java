package com.zs.ina.splash;

import com.zs.ina.login.INALoginForm;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import javax.swing.JOptionPane;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
public class TaskBasedSplash extends Application {

    /**
     *
     */
    public static final String APPLICATION_ICON
            = "http://cdn1.iconfinder.com/data/icons/Copenhagen/PNG/32/people.png";

    /**
     *
     */
    public static final String SPLASH_IMAGE = "images/splash.png";

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image("/com/zs/ina/splash/images/splash.png"));
        splash.setFitWidth(SPLASH_WIDTH);
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Loading data    . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; "
                + "-fx-background-color: cornsilk; "
                + "-fx-border-width:2; "
                + "-fx-border-color:#42426f; "
        );
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) throws Exception {
        final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundFriends
                        = FXCollections.<String>observableArrayList();
                ObservableList<String> availableFriends
                        = FXCollections.observableArrayList(
                                "Enquiry Module", "Search Module", "Admin module", "home", "conselor",
                                "Preparimg Launch", "Wait ..."
                        );

                updateMessage("Loading  Modules . . .");
                for (int i = 0; i < availableFriends.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, availableFriends.size());
                    String nextFriend = availableFriends.get(i);
                    foundFriends.add(nextFriend);
                    updateMessage("Loading Modules . . . " + nextFriend);
                }
                Thread.sleep(400);
                updateMessage("All friends found.");

                return foundFriends;
            }
        };

        showSplash(
                initStage,
                friendTask,
                () -> showMainStage(friendTask.valueProperty())
        );
        new Thread(friendTask).start();
    }

    private void showMainStage(ReadOnlyObjectProperty<ObservableList<String>> friends) {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setTitle("My Friends");
        //      mainStage.getIcons().add(new Image( APPLICATION_ICON));
//        final ListView<String> peopleView = new ListView<>();
//        peopleView.itemsProperty().bind(friends);
//

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/com/zs/ina/login/FXMLLogin.fxml"));
            root.getStylesheets().add(getClass().getResource("/com/zs/ina/login/style.css").toExternalForm());

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage());
        }

        // stage.setTitle("Home page");
        synchronized (root) {
//            stage.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(root);
            // scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            mainStage.setScene(scene);

            mainStage.getIcons().add(new Image(INALoginForm.class.getResourceAsStream("images/ia_logo.png")));
            mainStage.setTitle("International Academy");
            mainStage.setScene(scene);
        }
       // mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.show();
    }

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.getIcons().add(new Image(INALoginForm.class.getResourceAsStream("images/ina_window_logo.png")));
        initStage.show();
    }

    /**
     *
     */
    public interface InitCompletionHandler {

        /**
         *
         */
        public void complete();
    }
}
