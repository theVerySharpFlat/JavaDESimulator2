package javadesimulator2.GUI;

import java.util.ArrayList;

public class Node {
    public Node(int id, String name, ArrayList<NodeAttribute> attributes) {
        this.attributes = attributes;
        this.id = id;
        this.name = name;
    }

    public ArrayList<NodeAttribute> getAttributes() {
        return attributes;
    }

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

    private ArrayList<NodeAttribute> attributes;
    private int id;
    private String name;
}
