package me.arzi.eventsystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class Listener {

    private final Object instance;
    private final Method method;
    private final int priority;

    private final Consumer<Object> consumer;

    /**
     * The invoke function needs two parameters: the instance of the method's class and the parameter we want to pass.
     * We get the instance in the constructer, whereas the parameter is passed obtained from the Listener's invoke
     * method using a Consumer.
     * @param instance the method's class's instance
     */
    public Listener(Object instance, Method method, int priority) {
        this.instance = instance;
        this.method = method;
        this.priority = priority;

        consumer = event -> {
          try {
              method.invoke(instance, event);
          } catch (InvocationTargetException | IllegalAccessException e) {
              throw new RuntimeException(e);
          }
        };
    }

    public <T extends Event> void invoke(T event) {
        consumer.accept(event);
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public int getPriority() {
        return priority;
    }
}
