package eu.lestard.grid.tictactoe;

import eu.lestard.grid.GridModel;
import eu.lestard.grid.GridView;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = new AnchorPane();

        GridModel<States> gridModel = new GridModel<>();

        gridModel.setDefaultState(States.EMPTY);
        gridModel.setNumberOfColumns(3);
        gridModel.setNumberOfRows(3);

        GridView<States> gridView = new GridView<>();
        gridView.setGridModel(gridModel);

        gridView.addNodeMapping(States.O, (cell)-> new Label("O"));
        gridView.addNodeMapping(States.X, (cell)-> new Label("X"));

        GameLogic gameLogic = new GameLogic(gridModel);
        gameLogic.start();

        StackPane stackPane = new StackPane();

        Label winLabel = new Label("test");
        winLabel.setId("winLabel");


        winLabel.visibleProperty().bind(gameLogic.winnerProperty().isNotNull());
        winLabel.textProperty().bind(
            Bindings.when(
                gameLogic.winnerProperty().isEqualTo(States.EMPTY).or(gameLogic.winnerProperty().isNull()))
                .then("Tie")
                .otherwise(
                    Bindings.concat("Winner: ", gameLogic.winnerProperty())));


        stackPane.getChildren().add(gridView);
        stackPane.getChildren().add(winLabel);

        root.getChildren().add(stackPane);
        AnchorPane.setBottomAnchor(stackPane,0.0);
        AnchorPane.setTopAnchor(stackPane,0.0);
        AnchorPane.setLeftAnchor(stackPane,0.0);
        AnchorPane.setRightAnchor(stackPane,0.0);

        final Scene scene = new Scene(root, 500, 500);

        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
