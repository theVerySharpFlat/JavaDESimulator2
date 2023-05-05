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

  public String getTitle() {
    return title;
  }

  public int getID() {
    return id;
  }

  public int getParentID() {
    return parent;
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

  private int parent = Integer.MIN_VALUE;

  boolean state = false;
}
