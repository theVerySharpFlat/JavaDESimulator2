package javadesimulator2.GUI.Components;

import imgui.ImGui;
import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

public class LED extends Node {
  public LED(Schematic schematic) {
    super(schematic.getCurrentNextID(), "LED-" + schematic.getNextID(), new ArrayList<>());

    int idY = schematic.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "I", idY, getID()));
  }

  public void styleAfter() {
    ImGui.sameLine();
    ImGui.pushItemWidth(ImGui.calcTextSize("OFF").x);
    ImGui.text(super.getAttributes().get(0).getState() ? "ON " : "OFF");
  }

  @Override
  protected void renderAttributeContents(NodeAttribute a) {
    super.renderAttributeContents(a);
    styleAfter();
  }

  @Override
  protected void matchDonor(Node donor) {
    super.matchDonor(donor);
  }
}
