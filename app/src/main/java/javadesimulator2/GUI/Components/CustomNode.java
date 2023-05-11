package javadesimulator2.GUI.Components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javadesimulator2.GUI.ComponentMeta;
import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;
import javadesimulator2.GUI.NodeAttribute.IO;

public class CustomNode extends Node {
  public CustomNode(Schematic schematic, File path, File root) {
    super(
        schematic.getNextID(),
        "UNTITLED",
        new ArrayList<NodeAttribute>());
    this.path = path;

    System.out.printf("path=%s, root=%s\n", path.getPath(), root.getPath());
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
        NodeAttribute attribute = new NodeAttribute(IO.I, node.getCustomData().getOrDefault("name", "?"),
            parent.getNextID(), getID());
        parentToComponentInputAttributeMap.put(attribute, node.getAttributes().get(0));

        attributes.add(attribute);

      } else if (node.getClass().equals(DigiOutput.class)) {
        NodeAttribute attribute = new NodeAttribute(IO.O, node.getCustomData().getOrDefault("name", "?"),
            parent.getNextID(), getID());
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
      parentToComponentInputAttributeMap.put(replaceMap.get(key), parentToComponentInputAttributeMap.remove(key));
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
      parentToComponentOutputAttributeMap.put(replaceMap.get(key), parentToComponentOutputAttributeMap.remove(key));
    }
  }

  public File getPath() {
    return path;
  }

  HashMap<NodeAttribute, NodeAttribute> parentToComponentInputAttributeMap = new HashMap<>();
  HashMap<NodeAttribute, NodeAttribute> parentToComponentOutputAttributeMap = new HashMap<>();

  Schematic componentSchematic;
  File path;
  Meta meta;

  Schematic parent;
}
