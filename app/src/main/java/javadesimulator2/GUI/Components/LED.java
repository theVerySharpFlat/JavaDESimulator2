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
                .add(new NodeAttribute(NodeAttribute.IO.I, "I", idY, getID()));
    }

    public void styleBefore() {

    }

    public void styleAfter() {
        ImGui.sameLine();
        ImGui.text(super.getAttributes().get(0).getState() ? "ON" : "OFF");
    }

    @Override
    protected void renderAttributeContents(NodeAttribute a) {
        styleBefore();
        super.renderAttributeContents(a);
        styleAfter();
    }

    @Override
    public void update() {
    }

    @Override
    protected void matchDonor(Node donor) {
        super.matchDonor(donor);
    }

}
