package javadesimulator2.GUI.Components;

import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

public class Not extends Node {
  public Not(Schematic schematic) {
    super(schematic.getCurrentNextID(), "NOT-" + schematic.getNextID(), new ArrayList<>());

    int idA = schematic.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "A", idA, getID()));

    int idY = schematic.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY, getID()));
  }

  @Override
  public void update() {
    y().setState(!a().getState());
  }

  private NodeAttribute a() {
    return super.getAttributes().get(0);
  }

  private NodeAttribute y() {
    return super.getAttributes().get(1);
  }
}
