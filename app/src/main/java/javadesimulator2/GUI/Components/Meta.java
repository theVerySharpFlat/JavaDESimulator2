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
public class Meta extends Node {

  public Meta(Schematic schematic) {
    super(
        schematic.getCurrentNextID(),
        "META-" + schematic.getNextID(),
        new ArrayList<NodeAttribute>());
  }

  ImString name = new ImString(10);

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
  public void update() {
    // N/A
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
    for (Node node : schematic.getNodes().values()) {
      if (node.getClass().equals(getClass())) return false;
    }
    return schematic.getType() == Schematic.Type.COMPONENT;
  }
}
