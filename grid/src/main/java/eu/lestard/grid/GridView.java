package eu.lestard.grid;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GridView<State extends Enum> extends StackPane {


    private Pane rootPane = new Pane();

    private Map<State, Color> colorMapping = new HashMap<>();

    private Map<State, Supplier<Node>> nodeMapping = new HashMap<>();

    private ObjectProperty<GridModel<State>> gridModelProperty = new SimpleObjectProperty<>();

    private Map<Cell<State>, Pane> rectangleMap = new HashMap<>();

    private ObjectProperty<Paint> strokeProperty = new SimpleObjectProperty<>(Color.LIGHTGREY);

    private DoubleProperty strokeWidthProperty = new SimpleDoubleProperty(1);

    public GridView() {
        this.getChildren().add(rootPane);

        gridModelProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                initGridModel();
            }
        });
    }

    private void initGridModel() {
        NumberBinding widthPerCell = this.widthProperty().divide(getGridModel().numberOfColumns());
        NumberBinding heightPerCell = this.heightProperty().divide(getGridModel().numberOfRows());

        NumberBinding pxPerCell = Bindings.min(widthPerCell, heightPerCell);

        NumberBinding rootWidth = pxPerCell.multiply(getGridModel().numberOfColumns());
        NumberBinding rootHeight = pxPerCell.multiply(getGridModel().numberOfRows());

        rootPane.maxWidthProperty().bind(rootWidth);
        rootPane.maxHeightProperty().bind(rootHeight);

        gridModelProperty.get().getCells().forEach(cell->{
            addedCell(pxPerCell, cell);
        });

        gridModelProperty.get().cells().addListener((ListChangeListener<Cell<State>>) change -> {
            while (change.next()) {

                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(cell -> {
                        addedCell(pxPerCell, cell);
                    });
                }

                if (change.wasRemoved()) {
                    change.getRemoved().forEach(cell -> {
                        Pane pane = rectangleMap.get(cell);
                        rootPane.getChildren().remove(pane);

                        rectangleMap.remove(cell);
                    });
                }
            }
        });

    }

    private void addedCell(NumberBinding pxPerCell, Cell<State> cell) {
        NumberBinding xStart = pxPerCell.multiply(cell.getColumn());
        NumberBinding yStart = pxPerCell.multiply(cell.getRow());

        Pane pane = new StackPane();
        pane.layoutXProperty().bind(xStart);
        pane.layoutYProperty().bind(yStart);

        pane.minWidthProperty().bind(pxPerCell);
        pane.maxWidthProperty().bind(pxPerCell);

        pane.minHeightProperty().bind(pxPerCell);
        pane.maxHeightProperty().bind(pxPerCell);

        updateCell(pane, cell.stateProperty().get());

        updateStroke(pane);
        strokeProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updateStroke(pane);
            }
        });

        strokeWidthProperty().addListener((obs, oldV, newV)->{
            updateStroke(pane);
        });

        cell.stateProperty().addListener((obs, oldValue, newValue) -> {
            updateCell(pane, newValue);
        });

        rectangleMap.put(cell,pane);

        rootPane.getChildren().add(pane);
    }

    private void updateCell(Pane pane, State newState){
        pane.setBackground(new Background(new BackgroundFill(colorMapping.get(newState), CornerRadii.EMPTY, Insets.EMPTY )));
        pane.getChildren().clear();
        final Supplier<Node> nodeSupplier = nodeMapping.get(newState);
        if(nodeSupplier!=null){
            pane.getChildren().add(nodeSupplier.get());
        }
    }

    private void updateStroke(Pane pane){
        BorderWidths widths = new BorderWidths(strokeWidthProperty().get());
        BorderStroke stroke = new BorderStroke(strokeProperty().get(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, widths);
        pane.setBorder(new Border(stroke));
    }

    public void setGridModel(GridModel<State> gridModel) {
        gridModelProperty.set(gridModel);
    }

    public GridModel<State> getGridModel() {
        return gridModelProperty.get();
    }

    public void addColorMapping(State state, Color color) {
        this.colorMapping.put(state, color);
    }

    public void addNodeMapping(State state, Supplier<Node> nodeSupplier){
        this.nodeMapping.put(state,nodeSupplier);
    }

    public ReadOnlyDoubleProperty rootWidthProperty(){
        return rootPane.widthProperty();
    }

    public ReadOnlyDoubleProperty rootHeightProperty(){
        return rootPane.heightProperty();
    }

    public ReadOnlyDoubleProperty rootLayoutXProperty(){
        return rootPane.layoutXProperty();
    }

    public ReadOnlyDoubleProperty rootLayoutYProperty(){
        return rootPane.layoutYProperty();
    }

    public DoubleProperty strokeWidthProperty(){
        return strokeWidthProperty;
    }

    public ObjectProperty<Paint> strokeProperty(){
        return strokeProperty;
    }

    Pane getRootPane() {
        return rootPane;
    }


}
