package javadesimulator2.GUI.Components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.NodeAttribute.IO;
import javadesimulator2.GUI.Schematic;

public class CustomNode extends Node {
  public CustomNode(Schematic schematic, File path, File root) {
    super(schematic.getNextID(), "UNTITLED", new ArrayList<>());
    this.path = path;

    //    System.out.printf("path=%s, root=%s\n", path.getPath(), root.getPath());
    loadSchematic(schematic, new File(root.getPath(), path.getPath()));
    loadIO(schematic);
  }

  public void loadSchematic(Schematic parent, File path) {
    this.parent = parent;
    componentSchematic = new Schematic(Schematic.Type.COMPONENT);

    ObjectMapper mapper = new ObjectMapper();

    try {
      JsonNode rootNode = mapper.readTree(path);
      componentSchematic.load(rootNode.path("schematic"), new File(path.getParent()));
      /*
       * componentSchematic.optimizeIDs(parent.getCurrentNextID(), true);
       * parent.setNextID(componentSchematic.getCurrentNextID());
       */

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadIO(Schematic parent) {
    for (Node node : componentSchematic.getNodes().values()) {
      if (node.getClass().equals(DigiInput.class)) {
        NodeAttribute attribute =
            new NodeAttribute(
                IO.I, node.getCustomData().getOrDefault("name", "?"), parent.getNextID(), getID());
        parentToComponentInputAttributeMap.put(attribute, node.getAttributes().get(0));

        attributes.add(attribute);

      } else if (node.getClass().equals(DigiOutput.class)) {
        NodeAttribute attribute =
            new NodeAttribute(
                IO.O, node.getCustomData().getOrDefault("name", "?"), parent.getNextID(), getID());
        parentToComponentOutputAttributeMap.put(attribute, node.getAttributes().get(0));

        attributes.add(attribute);
      } else if (node.getClass().equals(Meta.class)) {
        Meta meta = (Meta) node;
        name = meta.getCustomData().getOrDefault("name", "UNTITLED") + "_" + getID();
        this.meta = meta;
      }
    }
  }

  @Override
  public void updateName() {
    name = meta.getCustomData().getOrDefault("name", "UNTITLED") + "_" + getID();
  }

  @Override
  public void update() {
    for (NodeAttribute key : parentToComponentInputAttributeMap.keySet()) {
      parentToComponentInputAttributeMap.get(key).setState(key.getState());
    }

    for (NodeAttribute key : parentToComponentOutputAttributeMap.keySet()) {
      key.setState(parentToComponentOutputAttributeMap.get(key).getState());
    }

    componentSchematic.simulate();
  }

  @Override
  public void matchDonor(Node donor) {
    super.matchDonor(donor);

    HashMap<NodeAttribute, NodeAttribute> replaceMap = new HashMap<>();

    for (NodeAttribute attribute : donor.getAttributes()) {
      for (NodeAttribute key : parentToComponentInputAttributeMap.keySet()) {
        if (key.getTitle().equals(attribute.getTitle())) {
          replaceMap.put(key, attribute);
          break;
        }
      }
    }

    for (NodeAttribute key : replaceMap.keySet()) {
      parentToComponentInputAttributeMap.put(
          replaceMap.get(key), parentToComponentInputAttributeMap.remove(key));
    }

    replaceMap.clear();

    for (NodeAttribute attribute : donor.getAttributes()) {
      for (NodeAttribute key : parentToComponentOutputAttributeMap.keySet()) {
        if (key.getTitle().equals(attribute.getTitle())) {
          replaceMap.put(key, attribute);
          break;
        }
      }
    }

    for (NodeAttribute key : replaceMap.keySet()) {
      parentToComponentOutputAttributeMap.put(
          replaceMap.get(key), parentToComponentOutputAttributeMap.remove(key));
    }

    runNewAttributeDamageControl();
  }

  public File getPath() {
    return path;
  }

  private void runNewAttributeDamageControl() {
    // Attributes that are new and don't appear in the attributes ArrayList
    for (NodeAttribute attribute : parentToComponentInputAttributeMap.keySet()) {
      boolean found = false;
      for (NodeAttribute attribute1 : attributes) {
        if (attribute.getTitle().equals(attribute1.getTitle())) {
          found = true;
          break;
        }
      }

      if (!found) {
        attributes.add(attribute);
        parent.getNodeAttributes().put(attribute.getID(), attribute);
        parent.getGraph().addNode(attribute.getID());
      }
    }

    for (NodeAttribute attribute : parentToComponentOutputAttributeMap.keySet()) {
      boolean found = false;
      for (NodeAttribute attribute1 : attributes) {
        if (attribute == attribute1) {
          found = true;
          break;
        }
      }

      if (!found) {
        attributes.add(attribute);
        parent.getNodeAttributes().put(attribute.getID(), attribute);
        parent.getGraph().addNode(attribute.getID());
      }
    }

    // Attributes that appear in the save file for user-schematic but don't appear in the defining
    // schematic
    ArrayList<NodeAttribute> toDelete = new ArrayList<>();
    for (NodeAttribute attribute : attributes) {
      boolean found = false;
      for (NodeAttribute attribute1 : parentToComponentInputAttributeMap.keySet()) {
        if (attribute1.getTitle().equals(attribute.getTitle())) {
          found = true;
          break;
        }
      }

      if (!found) {
        for (NodeAttribute attribute1 : parentToComponentOutputAttributeMap.keySet()) {
          if (attribute1.getTitle().equals(attribute.getTitle())) {
            found = true;
            break;
          }
        }

        if (!found) {
          toDelete.add(attribute);
        }
      }
    }

    for (NodeAttribute a : toDelete) {
      attributes.remove(a);
      parent.getGraph().removeNode(a.getID());
      parent.getNodeAttributes().remove(a.getID());
    }
  }

  final HashMap<NodeAttribute, NodeAttribute> parentToComponentInputAttributeMap = new HashMap<>();
  final HashMap<NodeAttribute, NodeAttribute> parentToComponentOutputAttributeMap = new HashMap<>();

  Schematic componentSchematic;
  final File path;
  Meta meta;

  Schematic parent;
}
