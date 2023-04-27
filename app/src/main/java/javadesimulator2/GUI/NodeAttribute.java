package javadesimulator2.GUI;


public class NodeAttribute {
    public enum IO {
        I,
        O
    }

    public NodeAttribute(IO ioType, String title, int id) {
        this.ioType = ioType;
        this.title = title;
        this.id = id;
    }

    public NodeAttribute(IO ioType, String title, int id, Runnable before, Runnable after) {
        this(ioType, title, id);
        this.before = before;
        this.after = after;
    }

    public Runnable getBeforeRenderFN() { return before; }
    public Runnable getAfterRenderFN() { return after; }
    

    public IO getIOType() {
        return ioType;
    }

    public String getTitle() {
        return title;
    }

    public int getID() {
        return id;
    }

    public void setParent(Node parent) {
        if (this.parent == null) {
            this.parent = parent;
        } else {
            System.out.println("error: node already has a parent!");
        }
    }

    public boolean getState() { return state; }
    public void setState(boolean val) { state = val; }


    private IO ioType;
    private String title;
    private int id;

    private Node parent = null;

    Runnable before = null;
    Runnable after = null;

    boolean state = false;
}
