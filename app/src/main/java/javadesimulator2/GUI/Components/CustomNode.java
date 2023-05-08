package javadesimulator2.GUI.Components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javadesimulator2.GUI.Node;
import javadesimulator2.GUI.NodeAttribute;
import javadesimulator2.GUI.Schematic;

public class CustomNode extends Node {
  public CustomNode(Schematic schematic, File path) {
    super(
        schematic.getCurrentNextID(),
        path.getPath() + "_" + schematic.getNextID(),
        new ArrayList<NodeAttribute>());
    this.path = path;

    loadSchematic(path);
    loadIO();
  }

  public void loadSchematic(File path) {
    schematic = new Schematic(Schematic.Type.COMPONENT);

    ObjectMapper mapper = new ObjectMapper();

    try {
      JsonNode rootNode = mapper.readTree(path);
      schematic.load(rootNode.path("schematic"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadIO() {
    for(Node node : schematic.getNodes().values()) {
      if(node.getClass().equals(DigiInput.class)){
        
      }
    }
  }

  @Override
  public void update() {
  }



  Schematic schematic;
  File path;
}
