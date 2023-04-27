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

    public NodeAttribute(IO ioType, String title, int id, Runnable extraShow) {
        this(ioType, title, id);
        this.extraRender = extraShow;
    }

    public Runnable getExtraRenderFN() { return extraRender; }

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


    private IO ioType;
    private String title;
    private int id;

    private Node parent = null;

    Runnable extraRender = null;
}
