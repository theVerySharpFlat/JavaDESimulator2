package javadesimulator2.GUI;

public class NodeAttribute {
  public enum IO {
    I,
    O
  }

  public NodeAttribute(IO ioType, String title, int id, int parentID) {
    this.ioType = ioType;
    this.title = title;
    this.id = id;
    this.parent = parentID;
  }

  public IO getIOType() {
    return ioType;
  }

  public void setIOType(IO io) {
    this.ioType = io;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String name) {
    title = name;
  }

  public int getID() {
    return id;
  }

  void setID(int id) {
    this.id = id;
  }

  public int getParentID() {
    return parent;
  }

  public void setParentID(int id) {
    parent = id;
  }

  public boolean getState() {
    return state;
  }

  public void setState(boolean val) {
    state = val;
  }

  private IO ioType;
  private String title;
  private int id;

  private int parent;

  boolean state = false;
}
