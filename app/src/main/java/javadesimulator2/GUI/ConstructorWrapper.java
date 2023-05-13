package javadesimulator2.GUI;

import java.util.function.Supplier;

public class ConstructorWrapper<T> {
  public ConstructorWrapper(Supplier<T> ctor) {
    this.ctor = ctor;
  }

  public T get() {
    return ctor.get();
  }

  final Supplier<T> ctor;
}
