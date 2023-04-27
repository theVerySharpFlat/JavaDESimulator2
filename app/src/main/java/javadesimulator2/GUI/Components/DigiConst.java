package javadesimulator2.GUI.Components;

import java.util.ArrayList;

import imgui.ImGui;
import imgui.type.ImBoolean;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class DigiConst extends Node {

    public DigiConst() {
        super(NodeEditor.getCurrentNextID(), "DIGICONST-" + NodeEditor.getNextID(), new ArrayList<NodeAttribute>());

        int idY = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY, () -> outputShowMod()));
    }


    ImBoolean state = new ImBoolean(false);
    public void outputShowMod() {
        ImGui.checkbox("##Checkbox", state);

        ImGui.sameLine();
        ImGui.text(state.get() ? "ON" : "OFF");

        ImGui.sameLine();
    }
}
