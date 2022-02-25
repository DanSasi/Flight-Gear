package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import viewmodel.Controller;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setMainStage(primaryStage);
        primaryStage.setScene(new Scene(root, 980, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
