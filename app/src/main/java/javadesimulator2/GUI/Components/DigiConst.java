package javadesimulator2.GUI.Components;

import java.util.ArrayList;
import java.util.HashMap;

import imgui.ImGui;
import imgui.type.ImBoolean;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeEditor;

public class DigiConst extends Node {

    public DigiConst() {
        super(NodeEditor.getCurrentNextID(), "DIGICONST-" + NodeEditor.getNextID(), new ArrayList<NodeAttribute>());

        int idY = NodeEditor.getNextID();
        super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.O, "Y", idY, getID()));
    }

    ImBoolean boxState = new ImBoolean(false);

    public void outputShowMod() {
        ImGui.checkbox("##Checkbox", boxState);

        ImGui.sameLine();
        ImGui.text(boxState.get() ? "ON" : "OFF");

        ImGui.sameLine();
    }

    @Override
    protected void renderAttributeContents(NodeAttribute a) {
        outputShowMod();
        super.renderAttributeContents(a);
    }

    @Override
    public void update() {
        super.getAttributes().get(0).setState(boxState.get());
    }

    @Override
    protected void matchDonor(Node donor) {
        super.matchDonor(donor);
    }

    @Override
    public HashMap<String, String> getCustomData() {
        HashMap<String, String> map = new HashMap<>();

        map.put("state", Boolean.toString(boxState.get()));

        return map;
    }

    @Override
    public void loadCustomData(HashMap<String, String> data) {
        String strState = data.getOrDefault("state", "false");
        boxState.set(Boolean.valueOf(strState));
    }
}
