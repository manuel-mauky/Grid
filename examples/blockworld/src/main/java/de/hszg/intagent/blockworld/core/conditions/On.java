package de.hszg.intagent.blockworld.core.conditions;


import de.hszg.intagent.blockworld.core.BlockStatus;

public class On implements Condition {

    private final BlockStatus bottom;
    private final BlockStatus top;

    public On(BlockStatus top, BlockStatus bottom){
        verifyBlock(top);
        verifyBlock(bottom);

        this.top = top;
        this.bottom = bottom;
    }

    public BlockStatus getBottom(){
        return bottom;
    }

    public BlockStatus getTop(){
        return top;
    }

    @Override
    public String toString() {
        return "On(" + top.name() +"," + bottom.name() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        On on = (On) o;

        if (bottom != on.bottom) {
            return false;
        }
        if (top != on.top) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = bottom != null ? bottom.hashCode() : 0;
        result = 31 * result + (top != null ? top.hashCode() : 0);
        return result;
    }
}
