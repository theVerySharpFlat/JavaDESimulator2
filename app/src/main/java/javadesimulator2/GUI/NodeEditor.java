package javadesimulator2.GUI;

import java.util.ArrayList;
import java.util.HashMap;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiFocusedFlags;
import imgui.type.ImInt;
import javadesimulator2.GUI.Components.AndComponent;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

public class NodeEditor {
    final ImNodesContext context = new ImNodesContext();

    private static int nextID = 0;

    private HashMap<Integer, Node> nodes = new HashMap<>();
    private HashMap<Integer, NodeAttribute> nodeAttributes = new HashMap<>();
    MutableValueGraph<Integer, Integer> graph = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .build();

    public NodeEditor() {
        ImNodes.createContext();
    }

    public static int getNextID() {
        return nextID++;
    }

    public static int getCurrentNextID() {
        return nextID;
    }

    ImVec2 lastHeldMousePosition;

    private void handlePanning() {
        if (ImNodes.isEditorHovered()) {
            if (ImGui.isMouseClicked(0)) {
                lastHeldMousePosition = ImGui.getMousePos();
            } else if (ImGui.isMouseDown(0) && ImGui.getIO().getKeyShift()) {
                ImVec2 currentEditorPosition = new ImVec2();
                ImNodes.editorContextGetPanning(currentEditorPosition);

                ImVec2 delta = ImGui.getMousePos();
                delta.x -= lastHeldMousePosition.x;
                delta.y -= lastHeldMousePosition.y;

                ImNodes.editorResetPanning(delta.x + currentEditorPosition.x, delta.y + currentEditorPosition.y);
                lastHeldMousePosition = ImGui.getMousePos();
            }
        }
    }

    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_DELETE = 261;

    public void show(boolean shouldShow) {
        ImGui.showDemoWindow();
        if (!shouldShow) {
            return;
        }

        ImGui.begin("Node Editor");
        ImNodes.editorContextSet(context);
        ImNodes.beginNodeEditor();

        ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.TopRight);
        handlePanning();

        if (ImGui.getIO().getKeysDown((int) 'A')) {
            /*
             * ArrayList<NodeAttribute> attributes = new ArrayList<>();
             * attributes.add(new NodeAttribute(NodeAttribute.IO.I, "Input", nextID++));
             * attributes.add(new NodeAttribute(NodeAttribute.IO.O, "Output", nextID++));
             */

            // Node node = new Node(nextID++, "A node", attributes);

            Node node = new AndComponent();
            nodes.put(node.getID(), node);

            for (NodeAttribute a : node.getAttributes()) {
                graph.addNode(a.getID());
                a.setParent(node);

                nodeAttributes.put(a.getID(), a);
            }
        }

        for (EndpointPair<Integer> edge : graph.edges()) {
            java.util.Optional<Integer> edgeValue = graph.edgeValue(edge);
            if (edgeValue.isPresent()) {
                ImNodes.link(edgeValue.get(), edge.nodeU(), edge.nodeV());
            }
        }

        for (Node node : nodes.values()) {
            ImNodes.beginNode(node.getID());
            ImNodes.getStyle().setNodeCornerRounding(0.0f);

            ImNodes.beginNodeTitleBar();
            ImGui.text(node.getName());
            ImNodes.endNodeTitleBar();

            for (NodeAttribute a : node.getAttributes()) {
                if (a.getIOType() == NodeAttribute.IO.I) {
                    ImNodes.beginInputAttribute(a.getID(), ImNodesPinShape.CircleFilled);
                    ImGui.text(a.getTitle());
                    ImNodes.endInputAttribute();
                } else {
                    ImNodes.beginOutputAttribute(a.getID());
                    ImGui.setCursorPosX(ImNodes.getNodeScreenSpacePosX(node.getID()) - ImGui.getWindowPos().x
                            + ImNodes.getNodeDimensionsX(node.getID())
                            - ImGui.calcTextSize(a.getTitle()).x - 10.0f);
                    ImGui.text(a.getTitle());
                    ImNodes.endOutputAttribute();
                }
            }

            ImNodes.endNode();
        }

        ImNodes.endNodeEditor();

        {
            ImInt start = new ImInt();
            ImInt end = new ImInt();

            if (ImNodes.isLinkCreated(start, end)) {
                NodeAttribute a = nodeAttributes.get(start.get());
                NodeAttribute b = nodeAttributes.get(end.get());

                if (a != null && b != null) {
                    int inputNode = -1, outputNode = -1;

                    if (a.getIOType() != b.getIOType()) {
                        inputNode = a.getIOType() == NodeAttribute.IO.I ? a.getID() : b.getID();
                        outputNode = inputNode == a.getID() ? b.getID() : a.getID();
                    }

                    graph.putEdgeValue(inputNode, outputNode, nextID++);
                } else {
                    System.out.println("Could not find attributes in HashMap");
                }

            }

        }

        {
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
                    && (ImGui.getIO().getKeysDown(KEY_DELETE) || ImGui.getIO().getKeysDown(KEY_BACKSPACE))) {
                int[] linkIds = new int[1024];
                ImNodes.getSelectedLinks(linkIds);

                for (Integer link : linkIds) {
                    for (EndpointPair<Integer> edge : graph.edges()) {
                        java.util.Optional<Integer> val = graph.edgeValue(edge);
                        if (val.isPresent() && val.get() == link) {
                            graph.removeEdge(edge);
                            break;
                        }
                    }
                }

                int[] nodeIds = new int[1024];
                ImNodes.getSelectedNodes(nodeIds);

                for (Integer nodeID : nodeIds) {
                    graph.removeNode(nodeID);

                    Node node = nodes.get(nodeID);

                    if (node != null) {
                        for (NodeAttribute a : node.getAttributes()) {
                            nodeAttributes.remove(a.getID());
                        }

                        nodes.remove(node.getID());
                    }
                }
            }

        }

        ImGui.end();
    }
}
