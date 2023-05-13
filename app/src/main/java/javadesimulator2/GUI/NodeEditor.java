package javadesimulator2.GUI;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.EndpointPair;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiFocusedFlags;
import imgui.type.ImInt;
import javadesimulator2.GUI.Components.CustomNode;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class NodeEditor {
  final ImNodesContext context = new ImNodesContext();

  Schematic schematic = new Schematic(Schematic.Type.ROOT);

  public NodeEditor() {
    ImNodes.createContext();
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

        ImNodes.editorResetPanning(
            delta.x + currentEditorPosition.x, delta.y + currentEditorPosition.y);
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
        constructors.add(c.getConstructor(Schematic.class));
      } catch (NoSuchMethodException e) {
        System.out.println(e.getMessage());
      }
    }

    return constructors;
  }

  static final Set<Constructor<? extends Node>> nodeCtors = loadComponentConstructors();

  private boolean simulating = false;

  public void showSidebar(boolean shouldShow) {
    if (!shouldShow)
      return;

    ImGui.begin("Digital Electronics");

    float width = ImGui.getWindowWidth();

    for (Constructor<? extends Node> ctor : nodeCtors) {
      if (schematic.getType() != Schematic.Type.COMPONENT
          && ctor.getDeclaringClass().isAnnotationPresent(ComponentMeta.class))
        continue;

      String name = ctor.getDeclaringClass().getSimpleName().toUpperCase();
      ImGui.pushID(name + "-BTN");
      ImGui.button(name, width - 10.0f, 25.0f);
      if (ImGui.beginDragDropSource()) {
        ImGui.setDragDropPayload("NEW-COMPONENT", ctor);
        ImGui.button(name, width, 50.0f);
        ImGui.endDragDropSource();
      }
      ImGui.popID();
    }

    if (lastSavePath != null && lastSavePath.length() > 0) {
      File[] componentFiles = new File(new File(lastSavePath).getParent())
          .listFiles((dir, name) -> name.endsWith("jde2c"));

      for (int i = 0; i < componentFiles.length; i++) {
        // remove abs path
        componentFiles[i] = new File(componentFiles[i].getPath().replace((new File(lastSavePath).getParent()), ""));
      }

      for (File file : componentFiles) {
        ImGui.pushID(file.getName() + "-BTN");
        ImGui.button(file.getName(), width - 10.0f, 50.0f);
        if (ImGui.beginDragDropSource()) {
          ImGui.setDragDropPayload("NEW-CUSTOM-COMPONENT", file.getPath());
          System.out.println(file.getPath());
          ImGui.button(file.getName(), width, 50.0f);
          ImGui.endDragDropSource();
        }
        ImGui.popID();
      }
    }

    ImGui.end();
  }

  private void simulate() {
    for (EndpointPair<Integer> edge : schematic.getGraph().edges()) {
      int src = edge.nodeU();
      int dst = edge.nodeV();

      NodeAttribute srcAttribute = schematic.getNodeAttributes().get(src);
      NodeAttribute dstAttribute = schematic.getNodeAttributes().get(dst);

      srcAttribute.setState(dstAttribute.getState());
    }

    for (Node node : schematic.getNodes().values()) {
      node.update();
    }
  }

  public void newSchematic(Schematic.Type type) {
    schematic = new Schematic(type);
    ImNodes.editorResetPanning(0, 0);
  }

  public void load(String path) {
    newSchematic(Schematic.Type.ROOT); // Type overwritten by load

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(new File(path));

      schematic.load(rootNode.get("schematic"), new File(new File(path).getParent()));

      JsonNode editorStateNode = rootNode.path("editorState");
      if (!editorStateNode.isMissingNode()) {
        {
          JsonNode nextIDNode = editorStateNode.path("nextID");
          if (!nextIDNode.isMissingNode()) {
            schematic.setNextID(nextIDNode.asInt(-1));
          }
        }

        JsonNode nodePositionsNode = editorStateNode.path("nodePositions");
        if (!nodePositionsNode.isMissingNode()) {
          Iterator<JsonNode> it = nodePositionsNode.elements();
          while (it.hasNext()) {
            JsonNode node = it.next();

            int id;
            float x, y;

            id = node.get("ID").asInt(-1);
            x = (float) node.get("x").asDouble(Float.NEGATIVE_INFINITY);
            y = (float) node.get("y").asDouble(Float.NEGATIVE_INFINITY);

            if (id >= 0 && x != Float.NEGATIVE_INFINITY && y != Float.NEGATIVE_INFINITY) {
              ImNodes.setNodeGridSpacePos(id, x, y);
            } else {
              System.out.println("Warning: invalid node grid position!");
            }
          }
        }
        lastSavePath = path;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void serialize(String path) {
    System.out.println("here");

    try {
      JsonFactory factory = new JsonFactory();
      JsonGenerator generator = factory.createGenerator(new File(path), JsonEncoding.UTF8);

      generator.writeStartObject();

      generator.writeFieldName("schematic");
      generator.writeStartObject();
      schematic.serialize(generator);
      generator.writeEndObject();

      generator.writeFieldName("editorState");
      generator.writeStartObject();
      generator.writeNumberField("nextID", schematic.getNextID());

      generator.writeFieldName("nodePositions");
      generator.writeStartArray();
      for (Node node : schematic.getNodes().values()) {
        generator.writeStartObject();
        generator.writeNumberField("ID", node.getID());
        generator.writeNumberField("x", ImNodes.getNodeGridSpacePosX(node.getID()));
        generator.writeNumberField("y", ImNodes.getNodeGridSpacePosY(node.getID()));
        generator.writeEndObject();
      }
      generator.writeEndArray();

      generator.flush();
      generator.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    lastSavePath = path;
  }

  public void show(boolean shouldShow) {
    ImGui.begin("Nodes");
    ImGui.text("Next ID " + schematic.getCurrentNextID());
    ImGui.text("Save Path: " + lastSavePath);
    for (Node node : schematic.getNodes().values()) {
      ImGui.text("Node " + node.getID());
    }

    ImGui.end();

    ImGui.begin("attributes");
    for (NodeAttribute a : schematic.getNodeAttributes().values()) {
      ImGui.text("Node " + a.getID());
    }
    ImGui.end();

    ImGui.begin("edges");
    for (EndpointPair<Integer> edge : schematic.getGraph().edges()) {
      ImGui.text("Edge value: " + schematic.getGraph().edgeValue(edge.nodeU(), edge.nodeV()).get());
    }
    ImGui.end();

    if (simulating && schematic.getType() == Schematic.Type.ROOT) {
      simulate();
    }

    if (!shouldShow) {
      return;
    }
    //ImGui.showDemoWindow();
    ImGui.begin("Schematic View");
    if (schematic.getType() == Schematic.Type.ROOT) {

      ImGui.setCursorPosX(ImGui.getWindowSizeX() / 2.0f - ImGui.calcTextSize("PLAY").x);
      if (ImGui.button(simulating ? "STOP" : "PLAY")) {
        simulating = !simulating;
      }
    }
    ImNodes.editorContextSet(context);
    ImNodes.beginNodeEditor();

    ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.TopRight);
    handlePanning();

    for (EndpointPair<Integer> edge : schematic.getGraph().edges()) {
      java.util.Optional<Integer> edgeValue = schematic.getGraph().edgeValue(edge);
      if (edgeValue.isPresent()) {
        ImNodes.link(edgeValue.get(), edge.nodeU(), edge.nodeV());
      }
    }

    for (Node node : schematic.getNodes().values()) {
      node.show();
    }

    ImNodes.endNodeEditor();

    if (ImGui.beginDragDropTarget()) {
      Object raw = ImGui.acceptDragDropPayload("NEW-COMPONENT");
      if (raw != null) {
        Constructor<? extends Node> ctor = (Constructor<? extends Node>) (raw); // Trust me bro
        Node node = null;
        int preCreationNextID = schematic.getNextID(); // In case we need to reset

        try {
          node = ctor.newInstance(schematic);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }

        if (node == null) {
          System.out.println("Failed to create node!");
          return;
        }

        if (node.canBeUsedInSchematic(schematic)) {

          ImNodes.setNodeScreenSpacePos(node.getID(), ImGui.getMousePosX(), ImGui.getMousePosY());

          schematic.getNodes().put(node.getID(), node);

          for (NodeAttribute a : node.getAttributes()) {
            schematic.getGraph().addNode(a.getID());
            schematic.getNodeAttributes().put(a.getID(), a);
          }
        } else {
          schematic.setNextID(preCreationNextID);
        }
      }

      raw = ImGui.acceptDragDropPayload("NEW-CUSTOM-COMPONENT");
      if (raw != null && lastSavePath != null) {
        String path = (String) raw;
        path = path.replace((new File(lastSavePath)).getParent(), "");
        if (path.charAt(0) == '/' || path.charAt(0) == '\\') {
          path = path.substring(1);
        }

        System.out.println("path: " + path);
        CustomNode customNode = addCustomNode(new File(path));
        ImNodes.setNodeScreenSpacePos(customNode.getID(), ImGui.getMousePosX(), ImGui.getMousePosY());
      }

      ImGui.endDragDropTarget();
    }

    {
      ImInt start = new ImInt();
      ImInt end = new ImInt();

      if (ImNodes.isLinkCreated(start, end)) {
        NodeAttribute a = schematic.getNodeAttributes().get(start.get());
        NodeAttribute b = schematic.getNodeAttributes().get(end.get());

        if (a != null && b != null) {
          int inputNode = -1, outputNode = -1;

          if (a.getIOType() != b.getIOType()) {
            inputNode = a.getIOType() == NodeAttribute.IO.I ? a.getID() : b.getID();
            outputNode = inputNode == a.getID() ? b.getID() : a.getID();
          }

          if (schematic.getGraph().incidentEdges(inputNode).size() == 0) { // can't have multiple outputs
            // connected to a
            // single input
            schematic.getGraph().putEdgeValue(inputNode, outputNode, schematic.getNextID());
          }
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
            for (EndpointPair<Integer> edge : schematic.getGraph().edges()) {
              java.util.Optional<Integer> val = schematic.getGraph().edgeValue(edge);
              if (val.isPresent() && val.get().equals(link)) {
                schematic.getGraph().removeEdge(edge);
                break;
              }
            }
          }
        }

        if (ImNodes.numSelectedNodes() > 0) {
          int[] nodeIds = new int[ImNodes.numSelectedNodes()];
          ImNodes.getSelectedNodes(nodeIds);

          for (Integer nodeID : nodeIds) {

            Node node = schematic.getNodes().get(nodeID);

            if (node != null) {

              for (NodeAttribute a : node.getAttributes()) {
                schematic.getNodeAttributes().remove(a.getID());
                schematic.getGraph().removeNode(a.getID());
              }

              schematic.getNodes().remove(node.getID());
            }
          }
        }
      }
    }

    ImGui.end();
  }

  public CustomNode addCustomNode(File path) {
//    System.out.printf("addCustomNode path=%s, parent=%s\n", path.getPath(), new File(lastSavePath).getParent());
    CustomNode node = new CustomNode(schematic, path, new File(new File(lastSavePath).getParent()));
    ImNodes.setNodeScreenSpacePos(node.getID(), 0.0f, 0.0f);

    schematic.getNodes().put(node.getID(), node);

    for (NodeAttribute a : node.getAttributes()) {
      schematic.getGraph().addNode(a.getID());
      schematic.getNodeAttributes().put(a.getID(), a);
    }

    return node;
  }

  public String getLastSavePath() {
    return lastSavePath;
  }

  public void setLastSavePath(String savePath) {
    lastSavePath = savePath;
  }

  public void optimizeIDs() {
    schematic.optimizeIDs();
  }

  public Schematic.Type getSchematicType() {
    return schematic.getType();
  }

  private String lastSavePath = null;
}
