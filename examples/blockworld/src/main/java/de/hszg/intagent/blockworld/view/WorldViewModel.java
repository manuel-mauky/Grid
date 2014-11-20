package de.hszg.intagent.blockworld.view;

import de.hszg.intagent.blockworld.core.World;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WorldViewModel implements ViewModel {

    private StringProperty title = new SimpleStringProperty();

    private ObjectProperty<World> world = new SimpleObjectProperty<>();


    public ObjectProperty<World> world() {
        return world;
    }

    public World getWorld() {
        return world.get();
    }

    public void setWorld(World world) {
        this.world.set(world);
    }

    public StringProperty titleProperty() {
        return title;
    }
}
