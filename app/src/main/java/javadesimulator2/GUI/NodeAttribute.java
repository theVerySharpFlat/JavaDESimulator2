package javadesimulator2.GUI;

/** A NodeAttribute the equivalent of a pin on a IC chip */
public class NodeAttribute {
  /** NodeAttributes can either be Input or Ouptu */
  public enum IO {
    I,
    O
  }

  /**
   * @param ioType Input or Output
   * @param title Name of attribute
   * @param id ID of attribute
   * @param parentID ID of parent node
   */
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
