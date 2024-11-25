package br.com.ucsal.util.ioc;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



public class DependencyInjector {
    private Map<String, Object> singletonInstances = new HashMap<>();
    private Map<String, Class<?>> registeredClasses = new HashMap<>();

    public void scanAndRegister(String ...packageName) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

        Set<Class<?>> injectableClasses = reflections.getTypesAnnotatedWith(Injectable.class);

        for (Class<?> clazz : injectableClasses) {
            System.out.println("Classes anotadas encontras: " + clazz.getName());
        }

        for (Class<?> clazz : injectableClasses) {
            mapClass(clazz);
        }

        for (String className : registeredClasses.keySet()) {
            resolveDependencies(className);
        }
    }

    private void mapClass(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Não é possível anotar interfaces diretamente com @Injectable: " + clazz.getName());
        }

        String className = clazz.getName();
        Class<?>[] interfaces = clazz.getInterfaces();

        boolean isCommandInterface = Arrays.stream(interfaces).anyMatch(i -> i.getName().equals("br.com.ucsal.controller.Command"));

        System.out.println("Mapeando classe: " + className);
        System.out.println("Interfaces: " + Arrays.toString(interfaces));
        System.out.println("É interface de comando? " + isCommandInterface);

        System.out.println("Precisa resolve a interface " + (interfaces.length > 0 && !isCommandInterface));

        if (interfaces.length > 0 && !isCommandInterface) {
            for (Class<?> iface : interfaces) {
                registeredClasses.put(iface.getName(), clazz); // Mapeia interface -> implementação
            }
        } else {

            registeredClasses.put(className, clazz); // Mapeia classe concreta
        }

    }

    public Object resolveDependencies(String className) throws Exception {
        if (singletonInstances.containsKey(className)) {
            return singletonInstances.get(className); // Retorna a instância existente
        }

        Class<?> implementation = registeredClasses.get(className);
        if (implementation == null) {
            throw new IllegalStateException("Nenhuma implementação registrada para " + className);
        }

        Constructor<?> injectConstructor = getInjectableConstructor(implementation);
        Object instance = createInstance(injectConstructor);

        singletonInstances.put(className, instance);
        return instance;
    }

    private Constructor<?> getInjectableConstructor(Class<?> clazz) throws Exception {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }
        return clazz.getDeclaredConstructor(); // Retorna o construtor padrão
    }

    private Object createInstance(Constructor<?> constructor) throws Exception {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            String dependencyName = parameterTypes[i].getName(); // Nome da dependência
            parameters[i] = resolveDependencies(dependencyName); // Resolve dependência pelo nome
        }

        return constructor.newInstance(parameters);
    }

    public Object getClassInContainer(String className) {

        for (String classname : registeredClasses.keySet()) {
            System.out.println("Classe registrada: " + classname);
        }

        System.out.println("Retornando instância de " + className);
        return singletonInstances.get(className);
    }

    private static DependencyInjector dependencyInjector;

    public static DependencyInjector getInstance() {


        if (dependencyInjector == null) {
            dependencyInjector = new DependencyInjector();
        }
        return dependencyInjector;
    }
}
