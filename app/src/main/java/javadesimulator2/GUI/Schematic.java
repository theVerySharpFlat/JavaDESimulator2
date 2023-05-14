package javadesimulator2.GUI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javadesimulator2.GUI.Components.CustomNode;

/** A software representation of a digital schematic */
public class Schematic implements Serializable {
  private HashMap<Integer, Node> nodes = new HashMap<>();
  private HashMap<Integer, NodeAttribute> nodeAttributes = new HashMap<>();

  private int nextID = 0;

  private MutableValueGraph<Integer, Integer> graph =
      ValueGraphBuilder.directed().allowsSelfLoops(false).build();

  /** The type of schematic */
  public enum Type {
    ROOT,

    /** Components allow for modularity and reusability through custom components */
    COMPONENT
  }

  private Type type;

  /**
   * Create a schematic
   *
   * @param type The type of schematic to create
   */
  public Schematic(Type type) {
    this.type = type;
  }

  /**
   * Get the schematic type
   *
   * @return The type of schematic
   */
  public Type getType() {
    return type;
  }

  /**
   * Get the nodes in the schematic
   *
   * @return A hashmap filled with all the nodes in the schematic. They Keys are the IDs of the
   *     Nodes and the values are the actual nodes
   */
  public HashMap<Integer, Node> getNodes() {
    return nodes;
  }

  /**
   * @return A hashmap filled with all the nodes attributes in the schematic. The keys are the IDs
   *     of the NodeAttributes and the values are the NodeAttributes themselves
   */
  public HashMap<Integer, NodeAttribute> getNodeAttributes() {
    return nodeAttributes;
  }

  /**
   * @return A graph filled with the connections between nodes in the schematic. Integer IDs are
   *     used instead of the actual Node objects
   */
  public MutableValueGraph<Integer, Integer> getGraph() {
    return graph;
  }

  /**
   * Retrieve the next ID for creation of Nodes, NodeAttributes, and edges and then increment the
   * nextID.
   *
   * @return The next object ID
   */
  public int getNextID() {
    return nextID++;
  }

  /**
   * Retrieve the current next ID, but do not increment for the next call
   *
   * @return The current next object ID
   */
  public int getCurrentNextID() {
    return nextID;
  }

  /**
   * Serialize the schematic into the given generator
   *
   * @param generator The generator used to serialize the schematic
   * @throws IOException
   */
  public void serialize(JsonGenerator generator) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    generator.writeStringField("type", type.toString());

    generator.writeFieldName("nodes");
    generator.writeStartArray();
    for (Node node : nodes.values()) {
      generator.writeStartObject();
      generator.writeNumberField("ID", node.getID());
      generator.writeStringField("name", node.getName());
      generator.writeStringField("type", node.getClass().getSimpleName());

      if (node.getClass().equals(CustomNode.class)) {
        generator.writeStringField("path", ((CustomNode) node).getPath().getPath());
      }

      generator.writeFieldName("customData");
      generator.writeString(mapper.writeValueAsString(node.getCustomData()));

      generator.writeEndObject();
    }
    generator.writeEndArray();

    generator.writeFieldName("nodeAttributes");
    generator.writeStartArray();
    for (NodeAttribute attribute : nodeAttributes.values()) {
      generator.writeStartObject();
      generator.writeNumberField("ID", attribute.getID());
      generator.writeStringField("ioType", attribute.getIOType().toString());
      generator.writeStringField("title", attribute.getTitle());
      generator.writeNumberField("parentID", attribute.getParentID());
      generator.writeEndObject();
    }
    generator.writeEndArray();

    generator.writeFieldName("links");
    generator.writeStartArray();
    for (EndpointPair<Integer> edge : graph.edges()) {
      generator.writeStartObject();
      if (graph.edgeValue(edge).isPresent()) {
        generator.writeNumberField("ID", graph.edgeValue(edge).get());
      }

      generator.writeNumberField("U", edge.nodeU());
      generator.writeNumberField("V", edge.nodeV());
      generator.writeEndObject();
    }
    generator.writeEndArray();
  }

  /**
   * Load the schematic from a JsonNode
   *
   * @param root JsonNode that points to a serialized schematic
   * @param parent The root directory of the schematic file used to load other components
   */
  public void load(JsonNode root, File parent) {
    HashMap<Integer, ArrayList<NodeAttribute>> nodeToAttributesMap = new HashMap<>();

    type = Type.valueOf(root.path("type").asText("ROOT"));

    JsonNode attributesNode = root.get("nodeAttributes");
    if (attributesNode != null) {
      Iterator<JsonNode> it = attributesNode.elements();

      while (it.hasNext()) {
        JsonNode node = it.next();

        int id = node.get("ID").asInt(-1);
        if (id < 0) {
          System.out.println("Invalid attribute ID!");
          return;
        }

        String rawIOType = node.get("ioType").asText("");
        NodeAttribute.IO ioType;
        try {
          ioType = NodeAttribute.IO.valueOf(rawIOType);
        } catch (IllegalArgumentException e) {
          System.out.println("Invalid attribute IO type!");
          return;
        }

        String title = node.get("title").asText("");

        int parentID = node.get("parentID").asInt(-1);
        if (id < 0) {
          System.out.println("Invalid attribute parentID!");
          return;
        }

        NodeAttribute attribute = new NodeAttribute(ioType, title, id, parentID);

        nodeAttributes.put(id, attribute);

        nodeToAttributesMap.putIfAbsent(parentID, new ArrayList<>());
        nodeToAttributesMap.get(parentID).add(attribute);
      }
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode nodesNode = root.get("nodes");
    if (nodesNode != null) {
      Iterator<JsonNode> it = nodesNode.elements();

      while (it.hasNext()) {
        JsonNode node = it.next();

        int id = node.get("ID").asInt(-1);
        if (id < 0) {
          System.out.println("Invalid attribute ID!");
          return;
        }

        String name, type, path;
        name = node.get("name").asText("");
        type = node.get("type").asText("");
        path = node.path("path").asText(""); // Only for custom nodes

        if (name.length() == 0 || type.length() == 0) {
          System.out.println("Failed to load name or type on node!");
          return;
        }

        // Find a constructor for the serialized node. Since the type is serialized, we can choose a
        // constructor
        // Based on the name of the class and the type field
        Constructor<? extends Node> foundCtor = null;
        for (Constructor<? extends Node> ctor : NodeEditor.nodeCtors) {
          if (ctor.getDeclaringClass().getSimpleName().equals(type)) {
            foundCtor = ctor;
          }
        }

        if (!type.equals("CustomNode") && foundCtor == null) {
          System.out.println("Failed to find suitable constructor!");
          return;
        }

        if (!nodeToAttributesMap.containsKey(id)) {
          System.out.println("Warning: failed to find attributes that match node!" + "id=" + id);
          // return;
        }

        // Deserialize custom data
        HashMap<String, String> customDataMap = null;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode customDataNode = node.path("customData");
        if (!customDataNode.isMissingNode()) {
          try {
            customDataMap = mapper.readValue(customDataNode.asText(), new TypeReference<>() {});
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        }

        // We fill out a generic node with all the attributes, id, and name
        // to be consumed by the new node
        Node donor = new Node(id, name, nodeToAttributesMap.getOrDefault(id, new ArrayList<>()));

        if (!type.equals("CustomNode")) {
          try {
            Node newNode = foundCtor.newInstance(this);
            newNode.matchDonor(donor); // Essentially my java version of the C++ move constructor
            if (customDataMap != null) {
              newNode.loadCustomData(customDataMap);
            }

            nodes.put(newNode.getID(), newNode);
          } catch (InstantiationException
              | IllegalAccessException
              | IllegalArgumentException
              | InvocationTargetException e) {
            e.printStackTrace();
          }
        } else {
          if (path.length() == 0) {
            System.out.println("Failed to retrieve path!");
            continue;
          }

          // Custom nodes require a special constructor with the relative path to the component file
          // and the project root
          CustomNode newNode = new CustomNode(this, new File(path), parent);
          newNode.matchDonor(donor);
          nodes.put(newNode.getID(), newNode);
        }
      }
    } else {
      System.out.println("Failed to get nodes node!");
    }

    // Add all attributes to the graph
    for (NodeAttribute nodeAttribute : nodeAttributes.values()) {
      graph.addNode(nodeAttribute.getID());
    }

    // Load all links
    JsonNode linksNode = root.get("links");
    if (linksNode != null) {
      Iterator<JsonNode> it = linksNode.elements();

      while (it.hasNext()) {
        JsonNode node = it.next();

        int id, u, v;

        id = node.get("ID").asInt(-1);
        u = node.get("U").asInt(-1);
        v = node.get("V").asInt(-1);

        if (id < 0 || u < 0 || v < 0) {
          System.out.println("Invaid link!");
          return;
        }

        graph.putEdgeValue(u, v, id);
      }
    }
  }

  /** Optimize IDs in the schematic such that no ID is skipped */
  public void optimizeIDs() {
    optimizeIDs(0, false);
  }

  /**
   * Optimize IDs in the schematic such that no ID is skipped
   *
   * @param baseID The starting ID, eg. 0, in the schematic
   * @param virtualSchematic This should be marked true if the schematic's nodes aren't being drawn
   *     on editor which indicates to the method that it should not call
   *     ImNodes.getNodeGridPosition()
   */
  public void optimizeIDs(int baseID, boolean virtualSchematic) {
    ArrayList<Integer> ids = new ArrayList<>();

    // Load all of the nodes and their attributes into the ArrayList
    for (Node node : nodes.values()) {
      ids.add(node.getID());

      for (NodeAttribute attribute : node.getAttributes()) {
        ids.add(attribute.getID());
      }
    }

    // Load the edges into the ArrayList
    for (EndpointPair<Integer> edge : graph.edges()) {
      if (graph.edgeValue(edge).isPresent()) {
        ids.add(graph.edgeValue(edge).get());
      }
    }

    // Map old IDs to the optimized ones. The new IDs are the indices in the ArrayList and the old
    // ones are the actual
    // values stored
    HashMap<Integer, Integer> oldIDToNewIDMap = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      oldIDToNewIDMap.put(ids.get(i), i + baseID);
    }

    HashMap<Integer, Node> newNodeMap = new HashMap<>();
    HashMap<Integer, NodeAttribute> newNodeAttributeMap = new HashMap<>();

    HashMap<Integer, ImVec2> newNodeToPositionMap = new HashMap<>();

    for (Integer oldID : nodes.keySet()) {
      Node node = nodes.get(oldID);
      int newID = oldIDToNewIDMap.getOrDefault(oldID, -1);

      if (oldID == -1) System.out.println("oldID: " + oldID);

      newNodeToPositionMap.put(
          newID,
          virtualSchematic
              ? new ImVec2(0.0f, 0.0f)
              : new ImVec2(
                  ImNodes.getNodeGridSpacePosX(oldID), ImNodes.getNodeGridSpacePosY(oldID)));

      if (newID == -1) {
        System.out.println("error: newID == -1");
        continue;
      }

      node.setID(newID);
      node.updateName();

      for (NodeAttribute attribute : node.attributes) {
        int oldAttributeID = attribute.getID();

        int newAttributeID = oldIDToNewIDMap.getOrDefault(oldAttributeID, -1) + baseID;

        if (newAttributeID == -1 + baseID) {
          System.out.println("error: newAttributeID == -1");
          continue;
        }

        attribute.setParentID(newID);
        attribute.setID(newAttributeID);

        newNodeAttributeMap.put(newAttributeID, attribute);
      }

      newNodeMap.put(newID, node);
    }

    MutableValueGraph<Integer, Integer> newGraph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    for (EndpointPair<Integer> oldEdge : graph.edges()) {
      int oldU = oldEdge.nodeU();
      int oldV = oldEdge.nodeV();

      int newU = oldIDToNewIDMap.getOrDefault(oldU, -1);
      int newV = oldIDToNewIDMap.getOrDefault(oldV, -1);

      if (newU == -1 || newV == -1) {
        System.out.println("newU = -1 || newV == -1!");
        continue;
      }

      if (graph.edgeValue(oldEdge).isPresent()) {
        newGraph.putEdgeValue(
            newU, newV, oldIDToNewIDMap.getOrDefault(graph.edgeValue(oldEdge).get(), -1));
      }
    }

    for (NodeAttribute attribute : newNodeAttributeMap.values()) {
      newGraph.addNode(attribute.getID());
    }

    graph = newGraph;
    nodes = newNodeMap;
    nodeAttributes = newNodeAttributeMap;

    for (Node node : nodes.values()) {
      ImVec2 pos = newNodeToPositionMap.getOrDefault(node.getID(), new ImVec2(0.0f, 0.0f));
      ImNodes.setNodeGridSpacePos(node.getID(), pos.x, pos.y);
    }

    nextID = ids.size() + baseID;
    System.out.println("end");
  }

  public void setNextID(int id) {
    nextID = id;
  }

  /** Simulate the schematic's logic */
  public void simulate() {
    // Loop through all the connections
    for (EndpointPair<Integer> edge : getGraph().edges()) {
      int dst = edge.nodeU();
      int src = edge.nodeV();

      NodeAttribute dstAttribute = getNodeAttributes().get(dst);
      NodeAttribute srcAttribute = getNodeAttributes().get(src);

      // Update the state of the input attribute
      dstAttribute.setState(srcAttribute.getState());

      // Update the nodes
      Node srcNode = nodes.get(dstAttribute.getParentID());
      Node dstNode = nodes.get(srcAttribute.getParentID());
      srcNode.update();
      dstNode.update();
    }
  }
}
