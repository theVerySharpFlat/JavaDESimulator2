package javadesimulator2.GUI;

import java.util.ArrayList;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;

public abstract class Node {
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

    public void show() {
        ImNodes.beginNode(getID());
        ImNodes.getStyle().setNodeCornerRounding(0.0f);

        ImNodes.beginNodeTitleBar();
        ImGui.text(getName());
        ImNodes.endNodeTitleBar();

        for (NodeAttribute a : getAttributes()) {
            if (a.getIOType() == NodeAttribute.IO.I) {
                ImNodes.beginInputAttribute(a.getID(), ImNodesPinShape.CircleFilled);
                ImGui.text(a.getTitle());
                if (a.getBeforeRenderFN() != null) {
                    a.getBeforeRenderFN().run();
                }

                ImNodes.endInputAttribute();

                if (a.getAfterRenderFN() != null) {
                    a.getAfterRenderFN().run();
                }

            } else {
                if (a.getBeforeRenderFN() != null) {
                    a.getBeforeRenderFN().run();
                }

                ImNodes.beginOutputAttribute(a.getID());

                if (a.getAfterRenderFN() != null) {
                    a.getAfterRenderFN().run();
                }
                ImGui.setCursorPosX(ImNodes.getNodeScreenSpacePosX(getID()) - ImGui.getWindowPos().x
                        + ImNodes.getNodeDimensionsX(getID())
                        - ImGui.calcTextSize(a.getTitle()).x - 10.0f);
                ImGui.text(a.getTitle());
                ImNodes.endOutputAttribute();
            }
        }

        ImNodes.endNode();
    }

    public abstract void update();

    private ArrayList<NodeAttribute> attributes;
    private int id;
    private String name;
}
