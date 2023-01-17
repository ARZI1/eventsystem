package me.arzi.eventsystem;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class EventBus {

    /**
     * We use a hashmap for fast event lookup. Listeners are put into a list based on their event class.
     */
    private final HashMap<Class<?>, LinkedList<Listener>> listeners;

    public EventBus() {
        this.listeners = new HashMap<>();
    }

    public <T extends Event> void postEvent(T event) {
        if (!listeners.containsKey(event.getClass()))
            return;

        for (Listener l : listeners.get(event.getClass()))
            l.invoke(event);
    }

    public void subscribe(Object instance) {
        addListeners(getListeningMethods(instance.getClass()), instance);
    }

    public void subscribe(Class<?> clazz) {
        addListeners(getListeningMethods(clazz), null);
    }

    public void unsubscribe(Object instance) {
        removeListeners(getListeningMethods(instance.getClass()), instance);
    }

    public void unsubscribe(Class<?> clazz) {
        removeListeners(getListeningMethods(clazz), null);
    }

    /**
     * Turns methods we know are listeners into listener objects.
     * @param methods the methods we want to turn into listeners
     * @param instance the method's class' instance (null if methods are static)
     */
    private void addListeners(List<Method> methods, Object instance) {
        for (Method method : methods) {
            Class<?> eventType = getEventParameterType(method);
            listeners.putIfAbsent(eventType, new LinkedList<>());
            LinkedList<Listener> list = listeners.get(eventType);

            int priority = getListeningPriority(method);
            int index = list.size();
            Iterator<Listener> iterator = list.descendingIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getPriority() > priority)
                    break;
                else
                    index--;
            }

            list.add(index, new Listener(instance, method, priority));
        }
    }

    /**
     * Removes Listeners by looping over their respective lists
     * @param methods the methods we want to remove
     * @param instance method's class' instance (null if methods are static)
     */
    private void removeListeners(List<Method> methods, Object instance) {
        for (Method method : methods) {
            Class<?> eventType = getEventParameterType(method);
            LinkedList<Listener> list = listeners.get(eventType);
            if (list == null)
                continue;

            list.removeIf(l -> l.getMethod().equals(method) && l.getInstance() == instance);
        }
    }

    private static List<Method> getListeningMethods(Class<?> clazz) {
        ArrayList<Method> listening = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (isMethodListening(method))
                listening.add(method);
        }

        return listening;
    }

    private static boolean isMethodListening(Method method) {
        boolean annotated = false;

        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof EventListener) {
                annotated = true;
                break;
            }
        }

        boolean hasEventParam = method.getParameterCount() == 1 &&
                Event.class.isAssignableFrom(method.getParameters()[0].getType());

        return annotated && hasEventParam;
    }

    private static Class<?> getEventParameterType(Method method) {
        if (method.getParameterCount() != 1)
            return null;

        return method.getParameters()[0].getType();
    }

    private static int getListeningPriority(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof EventListener) {
                EventListener e = (EventListener) annotation;
                return e.priority().getVal();
            }
        }

        return -1; // TODO: throw exception instead
    }
}
