package javadesimulator2.GUI;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import java.util.ArrayList;
import java.util.HashMap;

/** A node is the software equivalent of a IC chip */
public class Node {
  /**
   * @param id ID of Node
   * @param name Name of Node
   * @param attributes Attributes for the Node. You can add attributes later as well using
   *     getAttributes() Attributes for the Node. You can add attributes later as well using
   *     getAttributes().add
   */
  public Node(int id, String name, ArrayList<NodeAttribute> attributes) {
    this.attributes = attributes;
    this.id = id;
    this.name = name;
  }

  public ArrayList<NodeAttribute> getAttributes() {
    return attributes;
  }

  public void updateName() {
    name = getClass().getSimpleName().toUpperCase() + "-" + id;
  }

  public int getID() {
    return id;
  }

  void setID(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  /**
   * Render a node attribute
   *
   * @param a Node attribute to render
   */
  protected void renderAttributeContents(NodeAttribute a) { // can be overridden for styling
    if (a.getIOType() == NodeAttribute.IO.O) {
      ImGui.setCursorPosX(
          ImNodes.getNodeScreenSpacePosX(getID())
              - ImGui.getWindowPos().x
              + ImNodes.getNodeDimensionsX(getID())
              - ImGui.calcTextSize(a.getTitle() + a.getID() + "_").x
              - 10.0f);
    }

    ImGui.text(a.getTitle() + "_" + a.getID());
  }

  /** Render the bottom contents of a node. This should be overridden */
  protected void renderNodeBottomContents() {}

  /** Render the node */
  public void show() {
    ImNodes.beginNode(getID());
    ImNodes.pushColorStyle(
        ImNodesColorStyle.TitleBar, ImGui.colorConvertFloat4ToU32(0.38f, 0.38f, 0.38f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.TitleBarHovered, ImGui.colorConvertFloat4ToU32(0.45f, 0.45f, 0.45f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.TitleBarSelected, ImGui.colorConvertFloat4ToU32(0.45f, 0.45f, 0.45f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.Pin, ImGui.colorConvertFloat4ToU32(0.45f, 0.45f, 0.45f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.PinHovered, ImGui.colorConvertFloat4ToU32(0.55f, 0.55f, 0.55f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.Link, ImGui.colorConvertFloat4ToU32(0.38f, 0.38f, 0.38f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.LinkSelected, ImGui.colorConvertFloat4ToU32(0.55f, 0.55f, 0.55f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.LinkHovered, ImGui.colorConvertFloat4ToU32(0.55f, 0.55f, 0.55f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.MiniMapLink, ImGui.colorConvertFloat4ToU32(0.38f, 0.38f, 0.38f, 1));
    ImNodes.pushColorStyle(
        ImNodesColorStyle.MiniMapLinkSelected,
        ImGui.colorConvertFloat4ToU32(0.55f, 0.55f, 0.55f, 1));
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

  /**
   * Take in a generic Node and steal (rip, salvage, devour) its attributes to match
   *
   * @param donor The donor Node
   */
  protected void matchDonor(Node donor) {
    this.id = donor.id;
    this.name = donor.name;
    this.attributes = donor.attributes;
  }

  /** Update the node's logic state. Should be overridden */
  public void update() {}

  public boolean canBeUsedInSchematic(Schematic schematic) {
    return true;
  }

  public HashMap<String, String> getCustomData() {
    return new HashMap<>();
  }

  public void loadCustomData(HashMap<String, String> data) {}

  protected ArrayList<NodeAttribute> attributes;
  protected int id;
  protected String name;
}
