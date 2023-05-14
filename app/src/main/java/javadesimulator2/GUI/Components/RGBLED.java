package javadesimulator2.GUI.Components;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImDrawFlags;
import java.util.ArrayList;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

public class RGBLED extends Node {
  public RGBLED(Schematic schematic) {
    super(schematic.getCurrentNextID(), "RGBLED-" + schematic.getNextID(), new ArrayList<>());

    super.getAttributes()
        .add(new NodeAttribute(NodeAttribute.IO.I, "R", schematic.getNextID(), getID()));
    super.getAttributes()
        .add(new NodeAttribute(NodeAttribute.IO.I, "G", schematic.getNextID(), getID()));
    super.getAttributes()
        .add(new NodeAttribute(NodeAttribute.IO.I, "B", schematic.getNextID(), getID()));
  }

  @Override
  protected void renderNodeBottomContents() {
    super.renderNodeBottomContents();
    int r = 0, g = 0, b = 0;
    boolean on = false;
    if (attributes.get(0).getState()) {
      r = 255;
      on = true;
    }
    if (attributes.get(1).getState()) {
      g = 255;
      on = true;
    }
    if (attributes.get(2).getState()) {
      b = 255;
      on = true;
    }

    if (!on) {
      r = 57;
      g = 57;
      b = 57;
    }

    ImVec2 cursorPos = ImGui.getCursorScreenPos();
    ImVec2 nodePadding = new ImVec2();
    float nodeWidth = ImNodes.getNodeDimensionsX(id);

    ImNodes.getStyle().getNodePadding(nodePadding);

    float bulbWidth = nodeWidth * 0.65f;
    ImVec2 bulbStartPos =
        new ImVec2(cursorPos.x + (nodeWidth - bulbWidth) / 2.0f - nodePadding.x, cursorPos.y);

    ImGui.dummy(bulbWidth, bulbWidth);

    ImGui.getWindowDrawList()
        .addRectFilled(
            bulbStartPos.x,
            bulbStartPos.y,
            bulbStartPos.x + bulbWidth,
            bulbStartPos.y + bulbWidth,
            ImColor.rgba(r, g, b, 255),
            0.0f,
            ImDrawFlags.None);
  }

  @Override
  protected void renderAttributeContents(NodeAttribute a) {
    super.renderAttributeContents(a);
  }

  @Override
  protected void matchDonor(Node donor) {
    super.matchDonor(donor);
  }
}
