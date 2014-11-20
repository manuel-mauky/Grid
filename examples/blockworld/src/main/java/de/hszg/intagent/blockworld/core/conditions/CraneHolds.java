package de.hszg.intagent.blockworld.core.conditions;

import de.hszg.intagent.blockworld.core.BlockStatus;

public class CraneHolds implements Condition {


    private final BlockStatus block;


    public CraneHolds(BlockStatus block ){
        verifyBlock(block);
        this.block = block;
    }

    public BlockStatus getBlock(){
        return block;
    }

    @Override
    public String toString() {
        return "CraneHolds(" + block.name() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CraneHolds that = (CraneHolds) o;

        if (block != that.block) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return block != null ? block.hashCode() : 0;
    }
}
