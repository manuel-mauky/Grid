package eu.lestard.grid;

import de.saxsys.javafx.test.JfxRunner;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

@RunWith(JfxRunner.class)
public class GridViewTest {

    public static enum States {
        EMPTY,
        FILLED
    }

    private GridView<States> gridView;

    private GridModel<States> gridModel;

    @Before
    public void setup(){
        gridView = new GridView<>();
        gridModel = new GridModel<>();
        gridView.setGridModel(gridModel);
    }

    @Test
    public void testWhenCellsAreAddedToModelThereAreRectanglesAddedInView(){

        final ObservableList<Node> rectangles = gridView.getRootPane().getChildren();
        assertThat(rectangles).isEmpty();

        gridModel.cells().add(new Cell<>(0, 0));

        assertThat(rectangles).hasSize(1);

        final Pane rect1 = (Pane)rectangles.get(0);
        assertThat(rect1).isNotNull();


        gridModel.cells().add(new Cell<>(0, 1));
        assertThat(rectangles).hasSize(2);

        assertThat(rectangles).contains(rect1);
    }

    @Test
    public void testWhenCellsAreRemovedFromModelThereAreRectanglesRemovedInView(){
        final ObservableList<Node> rectangles = gridView.getRootPane().getChildren();

        final Cell<States> cell_0_0 = new Cell<>(0, 0);
        gridModel.cells().add(cell_0_0);

        final Cell<States> cell_0_1 = new Cell<>(0, 0);
        gridModel.cells().add(cell_0_1);

        assertThat(rectangles).hasSize(2);


        gridModel.cells().remove(cell_0_0);


        assertThat(rectangles).hasSize(1);
    }

    @Test
    public void testWhenAddingColorMappingAllCellsAreUpdated(){
        gridModel.setNumberOfColumns(1);
        gridModel.setNumberOfRows(2);

        gridModel.getCell(0, 0).changeState(States.EMPTY);
        gridModel.getCell(0, 1).changeState(States.FILLED);


        gridView.addColorMapping(States.EMPTY, Color.BLACK);
        gridView.addColorMapping(States.FILLED, Color.WHITE);

        assertThat(gridView.getCellPane(gridModel.getCell(0, 0)).getBackground().getFills().get(0).getFill()).isEqualTo(Color.BLACK);
        assertThat(gridView.getCellPane(gridModel.getCell(0, 1)).getBackground().getFills().get(0).getFill()).isEqualTo(Color.WHITE);

    }

    @Test
    public void testWhenAddingNodeMappingAllCellsAreUpdated(){
        gridModel.setNumberOfColumns(1);
        gridModel.setNumberOfRows(2);

        gridModel.getCell(0, 0).changeState(States.EMPTY);
        gridModel.getCell(0, 1).changeState(States.FILLED);

        Label emptyLabel = new Label();

        Button filledButton = new Button();

        gridView.addNodeMapping(States.EMPTY, (cell) -> emptyLabel);
        gridView.addNodeMapping(States.FILLED, (cell) -> filledButton);

        assertThat(gridView.getCellPane(gridModel.getCell(0, 0)).getChildren()).contains(emptyLabel);
        assertThat(gridView.getCellPane(gridModel.getCell(0,1)).getChildren()).contains(filledButton);
    }

    @Test
    public void testClickListener()throws Exception{
        gridModel.setNumberOfColumns(3);
        gridModel.setNumberOfRows(3);

        CompletableFuture<String> testFuture = new CompletableFuture<>();

        gridModel.getCell(1,1).setOnClick(event -> {
            testFuture.complete("clicked");
        });

        final Pane cellPane = gridView.getCellPane(gridModel.getCell(1, 1));

        final MouseEvent event = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);

        Event.fireEvent(cellPane, event);
        assertThat(testFuture.get()).isEqualTo("clicked");
    }
}

