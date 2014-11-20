package de.hszg.intagent.blockworld.core.conditions;

import de.hszg.intagent.blockworld.core.BlockStatus;

public class OnTable implements Condition {

    private final BlockStatus block;
    private final int placeNr;

    public OnTable( int placeNr,BlockStatus block){
        verifyPlaceNr(placeNr);
        verifyBlock(block);

        this.placeNr = placeNr;
        this.block = block;
    }

    public BlockStatus getBlock(){
        return block;
    }

    public int getPlaceNr(){
        return placeNr;
    }

    @Override
    public String toString() {
        return "OnTable(" + placeNr + "," + block.name() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OnTable onTable = (OnTable) o;

        if (placeNr != onTable.placeNr) {
            return false;
        }
        if (block != onTable.block) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = block != null ? block.hashCode() : 0;
        result = 31 * result + placeNr;
        return result;
    }
}
