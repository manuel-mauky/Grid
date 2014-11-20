package de.hszg.intagent.blockworld;

import de.hszg.intagent.blockworld.view.MainView;
import de.hszg.intagent.blockworld.view.MainViewModel;
import de.saxsys.jfx.mvvm.viewloader.ViewLoader;
import de.saxsys.jfx.mvvm.viewloader.ViewTuple;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String...args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Blockworld");

        ViewLoader viewLoader = new ViewLoader();

        final ViewTuple<MainView, MainViewModel> viewTuple = viewLoader.loadViewTuple(MainView.class);

        stage.setScene(new Scene(viewTuple.getView()));

        stage.show();
    }
}
