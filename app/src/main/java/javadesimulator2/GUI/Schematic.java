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
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Schematic implements Serializable {
  private HashMap<Integer, Node> nodes = new HashMap<>();
  private HashMap<Integer, NodeAttribute> nodeAttributes = new HashMap<>();

  @JsonSerialize(using = NodeGraphSerializer.class)
  private MutableValueGraph<Integer, Integer> graph =
      ValueGraphBuilder.directed().allowsSelfLoops(false).build();

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
          System.out.println("Failed to find attributes that match node!");
          return;
        }

        HashMap<String, String> customDataMap = null;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode customDataNode = node.path("customData");
        if (!customDataNode.isMissingNode()) {
          try {
            customDataMap =
                mapper.readValue(
                    customDataNode.asText(), new TypeReference<HashMap<String, String>>() {});
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        }

        Node donor = new Node(id, name, nodeToAttributesMap.get(id));

        try {
          Node newNode = foundCtor.newInstance();
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
}
