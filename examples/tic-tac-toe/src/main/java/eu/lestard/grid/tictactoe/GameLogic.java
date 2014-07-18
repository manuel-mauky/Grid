package eu.lestard.grid.tictactoe;

import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

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

        List<Cell<States>> diagonalOne = new ArrayList<>();
        diagonalOne.add(gridModel.getCell(0,0));
        diagonalOne.add(gridModel.getCell(1,1));
        diagonalOne.add(gridModel.getCell(2,2));

        if(checkCellList(diagonalOne)){
            return;
        }



        List<Cell<States>> diagonalTwo = new ArrayList<>();
        diagonalTwo.add(gridModel.getCell(2,0));
        diagonalTwo.add(gridModel.getCell(1,1));
        diagonalTwo.add(gridModel.getCell(0,2));

        if(checkCellList(diagonalTwo)){
            return;
        }

        for(int number = 0; number<3 ; number++){
            final List<Cell<States>> row = gridModel.getCellsOfRow(number);

            if(checkCellList(row)){
                return;
            }


            final List<Cell<States>> column = gridModel.getCellsOfColumn(number);

            if(checkCellList(column)){
                return;
            }
        }


        if(gridModel.getCellsWithState(States.EMPTY).isEmpty()){
            winner.set(States.EMPTY);
        }
    }

    private boolean checkCellList(List<Cell<States>> cells){
        if(threeOfTheSameKind(cells, States.O)){
            winner.set(States.O);
            return true;
        }

        if(threeOfTheSameKind(cells, States.X)){
            winner.set(States.X);
            return true;
        }

        return false;
    }


    private boolean threeOfTheSameKind(List<Cell<States>> cells, States state){
        return cells.stream().filter(cell->cell.getState().equals(state)).count() == 3;
    }


    public ReadOnlyObjectProperty<States> winnerProperty(){
        return winner;
    }
}
