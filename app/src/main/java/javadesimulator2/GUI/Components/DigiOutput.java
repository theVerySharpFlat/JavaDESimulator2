package javadesimulator2.GUI.Components;

import imgui.ImGui;
import imgui.type.ImString;
import java.util.ArrayList;
import java.util.HashMap;
import javadesimulator2.GUI.ComponentMeta;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

@ComponentMeta
public class DigiOutput extends Node {

  public DigiOutput(Schematic schematic) {
    super(schematic.getCurrentNextID(), "DIGIOUT-" + schematic.getNextID(), new ArrayList<>());

    int idY = schematic.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "Y", idY, getID()));
  }

  final ImString name = new ImString(10);

  @Override
  protected void renderAttributeContents(NodeAttribute a) {
    super.renderAttributeContents(a);
  }

  @Override
  protected void renderNodeBottomContents() {
    ImGui.pushItemWidth(ImGui.calcTextSize("c".repeat(name.getBufferSize())).x);
    ImGui.inputText("name", name);
  }

  @Override
  protected void matchDonor(Node donor) {
    super.matchDonor(donor);
  }

  @Override
  public HashMap<String, String> getCustomData() {
    HashMap<String, String> map = new HashMap<>();

    map.put("name", name.get());

    return map;
  }

  @Override
  public void loadCustomData(HashMap<String, String> data) {
    name.set(data.getOrDefault("name", "UNTITLED"));
  }

  @Override
  public boolean canBeUsedInSchematic(Schematic schematic) {
    return schematic.getType() == Schematic.Type.COMPONENT;
  }
}
