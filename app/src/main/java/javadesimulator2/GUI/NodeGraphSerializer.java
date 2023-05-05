package javadesimulator2.GUI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import java.io.IOException;

public class NodeGraphSerializer extends StdSerializer<ValueGraph<Integer, Integer>> {

  public NodeGraphSerializer() {
    this(null);
  }

  public NodeGraphSerializer(Class<ValueGraph<Integer, Integer>> t) {
    super(t);
  }

  @Override
  public void serialize(
      ValueGraph<Integer, Integer> value, JsonGenerator gen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

    gen.writeStartObject();

    gen.writeFieldName("nodes");
    gen.writeStartArray();
    for (Integer i : value.nodes()) {
      gen.writeNumber(i);
    }
    gen.writeEndArray();

    gen.writeFieldName("edges");
    gen.writeStartArray();
    for (EndpointPair<Integer> edge : value.edges()) {
      gen.writeStartObject();
      gen.writeNumberField("U", edge.nodeU());
      gen.writeNumberField("V", edge.nodeV());
      gen.writeEndObject();
    }
    gen.writeEndArray();

    gen.writeEndObject();
  }
}
