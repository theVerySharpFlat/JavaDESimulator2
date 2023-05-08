package javadesimulator2.GUI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class Schematic implements Serializable {
  private HashMap<Integer, Node> nodes = new HashMap<>();
  private HashMap<Integer, NodeAttribute> nodeAttributes = new HashMap<>();

  private int nextID = 0;

  @JsonSerialize(using = NodeGraphSerializer.class)
  private MutableValueGraph<Integer, Integer> graph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();

  public enum Type {
    ROOT,
    COMPONENT
  }

  private Type type;

  public Schematic(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public HashMap<Integer, Node> getNodes() {
    return nodes;
  }

  public HashMap<Integer, NodeAttribute> getNodeAttributes() {
    return nodeAttributes;
  }

  public MutableValueGraph<Integer, Integer> getGraph() {
    return graph;
  }

  public int getNextID() {
    return nextID++;
  }

  public int getCurrentNextID() {
    return nextID;
  }

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
      generator.writeNumberField("ID", graph.edgeValue(edge).get());
      generator.writeNumberField("U", edge.nodeU());
      generator.writeNumberField("V", edge.nodeV());
      generator.writeEndObject();
    }
    generator.writeEndArray();
  }

  public void load(JsonNode root) {
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

        String name, type;
        name = node.get("name").asText("");
        type = node.get("type").asText("");

        if (name.length() == 0 || type.length() == 0) {
          System.out.println("Failed to load name or type on node!");
          return;
        }

        Constructor<? extends Node> foundCtor = null;
        for (Constructor<? extends Node> ctor : NodeEditor.nodeCtors) {
          if (ctor.getDeclaringClass().getSimpleName().equals(type)) {
            foundCtor = ctor;
          }
        }

        if (foundCtor == null) {
          System.out.println("Failed to find suitable constructor!");
          return;
        }

        if (!nodeToAttributesMap.containsKey(id)) {
          System.out.println("Warning: failed to find attributes that match node!");
          // return;
        }

        HashMap<String, String> customDataMap = null;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode customDataNode = node.path("customData");
        if (!customDataNode.isMissingNode()) {
          try {
            customDataMap = mapper.readValue(
                customDataNode.asText(), new TypeReference<HashMap<String, String>>() {
                });
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        }

        Node donor = new Node(id, name, nodeToAttributesMap.getOrDefault(id, new ArrayList<>()));

        try {
          Node newNode = foundCtor.newInstance(this);
          newNode.matchDonor(donor);
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
      }
    } else {
      System.out.println("Failed to get nodes node!");
    }

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

  public void optimizeIDs() {
    ArrayList<Integer> ids = new ArrayList<>();

    for (Node node : nodes.values()) {
      ids.add(node.getID());

      for (NodeAttribute attribute : node.getAttributes()) {
        ids.add(attribute.getID());
      }
    }

    for (EndpointPair<Integer> edge : graph.edges()) {
      ids.add(graph.edgeValue(edge).get());
    }

    // ids.sort(Comparator.naturalOrder());

    HashMap<Integer, Integer> oldIDToNewIDMap = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      oldIDToNewIDMap.put(ids.get(i), i);
    }

    HashMap<Integer, Node> newNodeMap = new HashMap<>();
    HashMap<Integer, NodeAttribute> newNodeAttributeMap = new HashMap<>();

    HashMap<Integer, ImVec2> newNodeToPositionMap = new HashMap<>();

    for (Integer oldID : nodes.keySet()) {
      Node node = nodes.get(oldID);
      int newID = oldIDToNewIDMap.getOrDefault(oldID, -1);

      newNodeToPositionMap.put(newID,
          new ImVec2(ImNodes.getNodeGridSpacePosX(oldID), ImNodes.getNodeGridSpacePosY(oldID)));

      if (newID == -1) {
        System.out.println("error: newID == -1");
        continue;
      }

      node.setID(newID);
      node.updateName();

      /*
       * ImNodes.setNodeGridSpacePos(node.getID(),
       * ImNodes.getNodeGridSpacePosX(oldID),
       * ImNodes.getNodeGridSpacePosY(oldID));
       */

      for (Integer oldAttributeID : nodeAttributes.keySet()) {
        NodeAttribute attribute = nodeAttributes.get(oldAttributeID);

        int newAttributeID = oldIDToNewIDMap.getOrDefault(oldAttributeID, -1);

        if (newAttributeID == -1) {
          System.out.println("error: newAttributeID == -1");
          continue;
        }

        attribute.setParentID(newID);
        attribute.setID(newAttributeID);

        newNodeAttributeMap.put(newAttributeID, attribute);
      }

      newNodeMap.put(newID, node);
    }

    MutableValueGraph<Integer, Integer> newGraph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    for (EndpointPair<Integer> oldEdge : graph.edges()) {
      int oldU = oldEdge.nodeU();
      int oldV = oldEdge.nodeV();

      int newU = oldIDToNewIDMap.getOrDefault(oldU, -1);
      int newV = oldIDToNewIDMap.getOrDefault(oldV, -1);

      if (newU == -1 || newV == -1) {
        System.out.println("newU = -1 || newV == -1!");
        continue;
      }

      newGraph.putEdgeValue(newU, newV, oldIDToNewIDMap.getOrDefault(graph.edgeValue(oldEdge).get(), -1));
    }

    for (NodeAttribute attribute : newNodeAttributeMap.values()) {
      newGraph.addNode(attribute.getID());
    }

    graph = newGraph;
    nodes = newNodeMap;
    nodeAttributes = newNodeAttributeMap;

    for(Node node : nodes.values()) {
      ImVec2 pos = newNodeToPositionMap.getOrDefault(node.getID(), new ImVec2(0.0f, 0.0f));
      ImNodes.setNodeGridSpacePos(node.getID(), pos.x, pos.y);
    }

    nextID = ids.size();
  }

  public void setNextID(int id) {
    nextID = id;
  }
}
