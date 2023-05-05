package javadesimulator2.GUI;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import java.util.ArrayList;
import java.util.HashMap;

public class Node {
  public Node(int id, String name, ArrayList<NodeAttribute> attributes) {
    this.attributes = attributes;
    this.id = id;
    this.name = name;
  }

  public ArrayList<NodeAttribute> getAttributes() {
    return attributes;
  }

  public int getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  protected void renderAttributeContents(NodeAttribute a) { // can be overridden for styling
    if (a.getIOType() == NodeAttribute.IO.O) {
      ImGui.setCursorPosX(
          ImNodes.getNodeScreenSpacePosX(getID())
              - ImGui.getWindowPos().x
              + ImNodes.getNodeDimensionsX(getID())
              - ImGui.calcTextSize(a.getTitle()).x
              - 10.0f);
    }

    ImGui.text(a.getTitle());
  }

  protected void renderNodeBottomContents() {}

  public void show() {
    ImNodes.beginNode(getID());
    ImNodes.getStyle().setNodeCornerRounding(0.0f);

    ImNodes.beginNodeTitleBar();
    ImGui.text(getName());
    ImNodes.endNodeTitleBar();

    for (NodeAttribute a : getAttributes()) {
      if (a.getIOType() == NodeAttribute.IO.I) {
        ImNodes.beginInputAttribute(a.getID(), ImNodesPinShape.CircleFilled);

        renderAttributeContents(a);

        ImNodes.endInputAttribute();

      } else {
        ImNodes.beginOutputAttribute(a.getID());

        renderAttributeContents(a);

        ImNodes.endOutputAttribute();
      }
    }

    renderNodeBottomContents();

    ImNodes.endNode();
  }

  protected void matchDonor(Node donor) {
    this.id = donor.id;
    this.name = donor.name;
    this.attributes = donor.attributes;
  }

  public void update() {}
  ; // Not neccesarry, but you should override me!

  public boolean canBeUsedInSchematic(Schematic schematic) {
    return true;
  }

  public HashMap<String, String> getCustomData() {
    return new HashMap<>();
  }

  public void loadCustomData(HashMap<String, String> data) {}

  private ArrayList<NodeAttribute> attributes;
  private int id;
  private String name;
}
