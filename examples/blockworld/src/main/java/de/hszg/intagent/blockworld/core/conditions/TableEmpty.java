package de.hszg.intagent.blockworld.core.conditions;

public class TableEmpty implements Condition {

    private final int placeNr;

    public TableEmpty (int placeNr){
        verifyPlaceNr(placeNr);
        this.placeNr = placeNr;
    }

    public int getPlaceNr(){
        return placeNr;
    }


    @Override
    public String toString() {
        return "TableEmpty(" + placeNr + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableEmpty that = (TableEmpty) o;

        if (placeNr != that.placeNr) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return placeNr;
    }
}
