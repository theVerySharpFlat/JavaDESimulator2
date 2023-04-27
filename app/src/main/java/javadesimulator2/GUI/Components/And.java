package javadesimulator2.GUI.Components;

import java.util.ArrayList;

import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class And extends Node {
    public And() {
        super(NodeEditor.getCurrentNextID(), "AND-" + NodeEditor.getNextID(), new ArrayList<NodeAttribute>());

        int idA = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "A", idA));

        int idB = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "B", idB));

        int idY = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY));

        a = super.getAttributes().get(0);
        b = super.getAttributes().get(1);
        y = super.getAttributes().get(2);
    }

    @Override
    public void update() {
        y.setState(a.getState() & b.getState());
    }

    NodeAttribute a, b, y;
}
