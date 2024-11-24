package br.com.ucsal.util.ioc;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DependencyInjector {
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> registeredClasses = new HashMap<>();

    public void scanAndRegister(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> injectableClasses = reflections.getTypesAnnotatedWith(Injectable.class);

        for (Class<?> clazz : injectableClasses) {
            register(clazz);
        }
    }

    public void register(Class<?> clazz) throws Exception {
        if (!clazz.isAnnotationPresent(Injectable.class)) {
            throw new IllegalArgumentException(clazz.getName() + " não está anotada com @Injectable");
        }

        registeredClasses.put(clazz, clazz);

        if (!singletonInstances.containsKey(clazz)) {
            singletonInstances.put(clazz, createInstance(clazz));
        }
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> injectConstructor = null;

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                injectConstructor = constructor;
                break;
            }
        }

        if (injectConstructor == null) {
            injectConstructor = clazz.getDeclaredConstructor();
        }

        Class<?>[] parameterTypes = injectConstructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> dependencyType = parameterTypes[i];
            if (!singletonInstances.containsKey(dependencyType)) {
                if (registeredClasses.containsKey(dependencyType)) {
                    singletonInstances.put(dependencyType, createInstance(dependencyType));
                } else {
                    throw new IllegalStateException("Nenhuma instância registrada para " + dependencyType.getName());
                }
            }
            parameters[i] = singletonInstances.get(dependencyType);
        }

        return injectConstructor.newInstance(parameters);
    }

    public <T> T getInstance(Class<T> clazz) {
        return clazz.cast(singletonInstances.get(clazz));
    }
}
