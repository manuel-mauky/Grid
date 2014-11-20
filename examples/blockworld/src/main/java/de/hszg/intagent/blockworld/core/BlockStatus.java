package de.hszg.intagent.blockworld.core;

public enum BlockStatus {

    EMPTY(false),

    ADD_BUTTON(false),

    A(true),

    B(true),

    C(true),

    D(true),

    CRANE(false);


    private boolean isBlock;

    BlockStatus(boolean isBlock){
        this.isBlock = isBlock;
    }

    public boolean isBlock(){
        return isBlock;
    }
}
