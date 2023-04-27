package javadesimulator2.GUI.Components;

import java.util.ArrayList;

import imgui.ImGui;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class LED extends Node {
    public LED() {
        super(NodeEditor.getCurrentNextID(), "LED-" + NodeEditor.getNextID(), new ArrayList<NodeAttribute>());

        int idY = NodeEditor.getNextID();
        super.getAttributes()
                .add(new NodeAttribute(NodeAttribute.IO.I, "I", idY, () -> styleBefore(), () -> styleAfter()));
    }

    public void styleBefore() {
        
    }

    public void styleAfter() {
        ImGui.sameLine();
        ImGui.text(super.getAttributes().get(0).getState() ? "ON" : "OFF");
    }

    @Override
    public void update() {
    }

}
