package javadesimulator2.GUI.Components;

import imgui.ImGui;
import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

public class LED extends Node {
  public LED(Schematic schematic) {
    super(
        schematic.getCurrentNextID(),
        "LED-" + schematic.getNextID(),
        new ArrayList<NodeAttribute>());

    int idY = schematic.getNextID();
    super.getAttributes().add(new NodeAttribute(NodeAttribute.IO.I, "I", idY, getID()));
  }

  public void styleBefore() {}

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
  public void update() {}

  @Override
  protected void matchDonor(Node donor) {
    super.matchDonor(donor);
  }
}
