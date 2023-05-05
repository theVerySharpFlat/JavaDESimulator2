package javadesimulator2.GUI.Components;

import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class Or extends Node {
  public Or() {
    super(
        NodeEditor.getCurrentNextID(),
        "OR-" + NodeEditor.getNextID(),
        new ArrayList<NodeAttribute>());

    int idA = NodeEditor.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "A", idA, getID()));

    int idB = NodeEditor.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "B", idB, getID()));

    int idY = NodeEditor.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY, getID()));

    a = super.getAttributes().get(0);
    b = super.getAttributes().get(1);
    y = super.getAttributes().get(2);
  }

  @Override
  public void update() {
    y.setState(a.getState() || b.getState());
  }

  NodeAttribute a, b, y;
}
