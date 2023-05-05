package javadesimulator2.GUI.Components;

import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class Passthrough extends Node {
  public Passthrough() {
    super(
        NodeEditor.getCurrentNextID(),
        "PASSTHROUGH-" + NodeEditor.getNextID(),
        new ArrayList<NodeAttribute>());

    int idA = NodeEditor.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "A", idA, getID()));

    int idY = NodeEditor.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY, getID()));

    a = super.getAttributes().get(0);
    y = super.getAttributes().get(1);
  }

  @Override
  public void update() {
    y.setState(a.getState());
  }

  NodeAttribute a, y;
}
