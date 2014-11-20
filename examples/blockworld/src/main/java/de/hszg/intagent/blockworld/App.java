package de.hszg.intagent.blockworld;

import de.hszg.intagent.blockworld.view.MainView;
import de.saxsys.mvvmfx.FluentViewLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String...args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Blockworld");

        final Parent view = FluentViewLoader.fxmlView(MainView.class).load().getView();
        stage.setScene(new Scene(view));

        stage.show();
    }
}
