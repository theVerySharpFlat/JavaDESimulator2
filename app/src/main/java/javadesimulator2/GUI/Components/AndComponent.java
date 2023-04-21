package javadesimulator2.GUI.Components;

import java.util.ArrayList;

import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class AndComponent extends Node {
    public AndComponent() {
        super(NodeEditor.getCurrentNextID(), "AND-" + NodeEditor.getNextID(), new ArrayList<NodeAttribute>());

        int idA = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "A", idA));

        int idB = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "B", idB));

        int idY = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY));
    }
}
