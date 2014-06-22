package eu.lestard.grid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application{

    public static enum States {
        A,
        B
    }


    public static void main(String...args){
        Application.launch(args);
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Grid");

        AnchorPane root = new AnchorPane();


        GridModel<States> gridModel = new GridModel<>();
        gridModel.setDefaultState(States.A);
        GridView<States> gridView = new GridView<>();
        gridView.setGridModel(gridModel);

        gridView.addColorMapping(States.A, Color.YELLOWGREEN);
        gridView.addColorMapping(States.B, Color.ORANGE);


        gridView.addNodeMapping(States.A, (cell) -> new Label("A"));


        gridView.strokeProperty().set(Color.BLACK);
        gridView.strokeWidthProperty().set(1);


        AnchorPane.setLeftAnchor(gridView, 0.0);
        AnchorPane.setRightAnchor(gridView, 0.0);
        AnchorPane.setBottomAnchor(gridView, 0.0);
        AnchorPane.setTopAnchor(gridView, 0.0);

        root.getChildren().add(gridView);


        gridModel.setNumberOfRows(9);
        gridModel.setNumberOfColumns(5);

        gridModel.getCell(2,4).changeState(States.B);


        gridModel.getCells().forEach(cell->{
            cell.setOnClick((event) -> {
                final States stateBefore = cell.getState();
                if (stateBefore == States.A) {
                    cell.changeState(States.B);
                } else {
                    cell.changeState(States.A);
                }
            });
        });

        stage.setScene(new Scene(root, 800,600));

        stage.show();
    }
}
