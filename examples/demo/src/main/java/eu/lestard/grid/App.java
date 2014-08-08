package eu.lestard.grid;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application{


    /**
     * The states that a grid cell can have in our example
     */
    public static enum States {
        A,
        B
    }


    public static void main(String...args){
        Application.launch(args);
    }


    private IntegerProperty numberOfRows = new SimpleIntegerProperty(9);
    private IntegerProperty numberOfColumns = new SimpleIntegerProperty(8);

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Grid");
        BorderPane root = new BorderPane();


        GridModel<States> gridModel = new GridModel<>();

        // define the state that will be used as default.
        gridModel.setDefaultState(States.A);


        // create the grid view and set the grid model
        GridView<States> gridView = new GridView<>();
        gridView.setGridModel(gridModel);

        // define which color is shown when a cell has a specific state
        gridView.addColorMapping(States.A, Color.YELLOWGREEN);
        gridView.addColorMapping(States.B, Color.ORANGE);


        // additionally we add labels that are placed inside the cells
        gridView.addNodeMapping(States.A, (cell) -> new Label("A"));
        gridView.addNodeMapping(States.B, (cell) -> new Label("B"));


        // styling of the strokes between the cells
        gridView.strokeProperty().set(Color.BLACK);
        gridView.strokeWidthProperty().set(1);



        root.setCenter(gridView);

        // every time a new cell is added, we add an click listener to it.
        gridModel.setOnCellAddedHandler((cell)->{
            // the click handler switches the state of the cells
            cell.setOnClick(event -> switchStates(cell));

            // move over cells with pressed mouse button will switch states
            cell.setOnMouseOver(event -> {
                if(event.isPrimaryButtonDown()){
                    switchStates(cell);
                }
            });
        });


        // bind the number of rows/columns in the grid model
        gridModel.numberOfColumns().bind(numberOfColumns);
        gridModel.numberOfRows().bind(numberOfRows);

        root.setBottom(createControls());

        stage.setScene(new Scene(root, 800,600));
        stage.show();
    }


    private void switchStates(Cell<States> cell){
        final States stateBefore = cell.getState();
        if (stateBefore == States.A) {
            cell.changeState(States.B);
        } else {
            cell.changeState(States.A);
        }
    }

    /**
     * Create a controls panel so that we can control the grid properties.
     */
    private VBox createControls(){
        VBox controlsBox = new VBox();
        controlsBox.setSpacing(10);
        controlsBox.setPadding(new Insets(10));


        HBox numberOfRowsBox = new HBox();
        numberOfRowsBox.setSpacing(5);
        Label numberOfRowsLabel = new Label();
        Slider numberOfRowsSlider = new Slider();
        numberOfRowsBox.getChildren().addAll(numberOfRowsLabel, numberOfRowsSlider);


        numberOfRowsLabel.textProperty().bind(Bindings.concat("Rows: ", numberOfRows));
        numberOfRowsSlider.valueProperty().bindBidirectional(numberOfRows);

        numberOfRowsSlider.setMin(3);
        numberOfRowsSlider.setMax(30);




        HBox numberOfColumnsBox = new HBox();
        numberOfColumnsBox.setSpacing(5);
        Label numberOfColumnsLabel = new Label();
        Slider numberOfColumnsSlider = new Slider();
        numberOfColumnsBox.getChildren().addAll(numberOfColumnsLabel, numberOfColumnsSlider);

        numberOfColumnsLabel.textProperty().bind(Bindings.concat("Columns:", numberOfColumns));
        numberOfColumnsSlider.valueProperty().bindBidirectional(numberOfColumns);

        numberOfColumnsSlider.setMin(3);
        numberOfColumnsSlider.setMax(30);


        controlsBox.getChildren().addAll(numberOfRowsBox, numberOfColumnsBox);

        return controlsBox;
    }
}

