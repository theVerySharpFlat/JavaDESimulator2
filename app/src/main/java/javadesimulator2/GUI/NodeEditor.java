package javadesimulator2.GUI;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiFocusedFlags;
import imgui.type.ImInt;

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

    public String currentDraggedElement = "";

    private static Set<Constructor<? extends Node>> loadComponentConstructors() {
        final String packageName = "javadesimulator2.GUI.Components";

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage(packageName)
                        .filterInputsBy(new FilterBuilder().includePackage(packageName)));

        Set<Class<? extends Node>> classes = reflections.getSubTypesOf(Node.class);

        HashSet<Constructor<? extends Node>> constructors = new HashSet<>(classes.size());

        for (Class<? extends Node> c : classes) {
            try {
                constructors.add(c.getConstructor());
            } catch (NoSuchMethodException e) {
                System.out.println(e.getMessage());
            }
        }

        return constructors;
    }

    private static final Set<Constructor<? extends Node>> nodeCtors = loadComponentConstructors();

    private boolean simulating = false;

    public void showSidebar(boolean shouldShow) {
        if (!shouldShow)
            return;

        ImGui.begin("Sidebar");

        float width = ImGui.getWindowWidth();

        for (Constructor<? extends Node> ctor : nodeCtors) {
            String name = ctor.getDeclaringClass().getSimpleName().toUpperCase();
            ImGui.pushID(name + "-BTN");
            ImGui.button(name, width - 10.0f, 50.0f);
            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload("NEW-COMPONENT", ctor);
                ImGui.button(name, width, 50.0f);
                ImGui.endDragDropSource();

            }
            ImGui.popID();
        }

        ImGui.end();
    }

    private void simulate() {
        for (EndpointPair<Integer> edge : graph.edges()) {
            int src = edge.nodeU();
            int dst = edge.nodeV();

            NodeAttribute srcAttribute = nodeAttributes.get(src);
            NodeAttribute dstAttribute = nodeAttributes.get(dst);

            srcAttribute.setState(dstAttribute.getState());
        }

        for (Node node : nodes.values()) {
            node.update();
        }
    }

    public void show(boolean shouldShow) {
        ImGui.begin("Nodes");
        for (Node node : nodes.values()) {
            ImGui.text("Node " + node.getID());
        }

        ImGui.end();

        ImGui.begin("attributes");
        for (NodeAttribute a : nodeAttributes.values()) {
            ImGui.text("Node " + a.getID());
        }
        ImGui.end();

        ImGui.begin("edges");
        for (EndpointPair<Integer> edge : graph.edges()) {
            ImGui.text("Edge value: " + graph.edgeValue(edge.nodeU(), edge.nodeV()).get());
        }
        ImGui.end();

        if (simulating) {
            simulate();
        }

        ImGui.showDemoWindow();
        if (!shouldShow) {
            return;
        }

        ImGui.begin("Node Editor");
        ImGui.setCursorPosX(ImGui.getWindowSizeX() / 2.0f - ImGui.calcTextSize("play").x);
        if (ImGui.button(simulating ? "stop" : "play")) {
            simulating = !simulating;
        }
        ImNodes.editorContextSet(context);
        ImNodes.beginNodeEditor();

        ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.TopRight);
        handlePanning();

        for (EndpointPair<Integer> edge : graph.edges()) {
            java.util.Optional<Integer> edgeValue = graph.edgeValue(edge);
            if (edgeValue.isPresent()) {
                ImNodes.link(edgeValue.get(), edge.nodeU(), edge.nodeV());
            }
        }

        for (Node node : nodes.values()) {
            node.show();
        }

        ImNodes.endNodeEditor();

        if (ImGui.beginDragDropTarget()) {
            Object raw = ImGui.acceptDragDropPayload("NEW-COMPONENT");
            if (raw != null) {
                Constructor<? extends Node> ctor = (Constructor<? extends Node>) (raw); // Trust me bro
                Node node = null;
                try {
                    node = ctor.newInstance();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                if (node == null) {
                    System.out.println("Failed to create node!");
                    return;
                }

                ImNodes.setNodeScreenSpacePos(node.getID(), ImGui.getMousePosX(), ImGui.getMousePosY());

                nodes.put(node.getID(), node);

                for (NodeAttribute a : node.getAttributes()) {
                    graph.addNode(a.getID());
                    a.setParent(node);

                    nodeAttributes.put(a.getID(), a);
                }
            }
            ImGui.endDragDropTarget();
        }

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
                if (ImNodes.numSelectedLinks() > 0) {
                    int[] linkIds = new int[ImNodes.numSelectedLinks()];
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
                }

                if (ImNodes.numSelectedNodes() > 0) {
                    int[] nodeIds = new int[ImNodes.numSelectedNodes()];
                    ImNodes.getSelectedNodes(nodeIds);

                    for (Integer nodeID : nodeIds) {

                        Node node = nodes.get(nodeID);

                        if (node != null) {

                            for (NodeAttribute a : node.getAttributes()) {
                                nodeAttributes.remove(a.getID());
                                graph.removeNode(a.getID());
                            }

                            nodes.remove(node.getID());
                        }
                    }
                }
            }

        }

        ImGui.end();
    }
}
