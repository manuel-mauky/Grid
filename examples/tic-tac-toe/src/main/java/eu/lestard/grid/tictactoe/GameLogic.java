package eu.lestard.grid.tictactoe;

import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.*;

public class GameLogic {

    private States currentTurn;

    private GridModel<States> gridModel;

    private ObjectProperty<States> winner = new SimpleObjectProperty<>();

    public GameLogic(GridModel<States> gridModel){
        this.gridModel = gridModel;
    }


    public void start() {
        currentTurn = States.X;

        gridModel.getCells().forEach(cell -> cell.setOnClick(event -> makeTurn(cell.getColumn(),cell.getRow())));


    }

    private void makeTurn(int column, int row){
        final Cell<States> cell = gridModel.getCell(column, row);

        if(cell.getState() == States.EMPTY){
            if(currentTurn == States.X){
                currentTurn = States.O;
            }else{
                currentTurn = States.X;
            }

            cell.changeState(currentTurn);
            checkWin();
        }
    }

    private void checkWin(){

        winner.set(States.O);

    }

    public ReadOnlyObjectProperty<States> winnerProperty(){
        return winner;
    }
}
