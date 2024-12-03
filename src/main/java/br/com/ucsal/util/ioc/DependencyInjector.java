package br.com.ucsal.util.ioc;
import br.com.ucsal.persistencia.PersistenceType;
import br.com.ucsal.persistencia.RegisterPersistence;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import java.lang.reflect.Constructor;
import java.util.*;

public class DependencyInjector {
    private final Map<String, Object> singletonInstances = new HashMap<>();
    private final Map<String, Class<?>> registeredClasses = new HashMap<>();
    private final String persistencePackage = "br.com.ucsal.persistencia";

    private PersistenceType persistenceType;

    public void scanAndRegister(String ...packageName) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .addScanners(Scanners.SubTypes, Scanners.TypesAnnotated));

        Set<Class<?>> classesWithValidAnnotates= new HashSet<>();

        classesWithValidAnnotates.addAll(reflections.getTypesAnnotatedWith(Injectable.class));
        classesWithValidAnnotates.addAll(reflections.getTypesAnnotatedWith(RegisterPersistence.class));


        for (Class<?> clazz : classesWithValidAnnotates) {
              if(clazz.isAnnotationPresent(Injectable.class)){
                System.out.println("Classe com anotação de injeção de dependência: " + clazz.getName());
              }

           if(clazz.isAnnotationPresent(RegisterPersistence.class)){
               System.out.println("Classe com anotação de persistência config: " + clazz.getName());
               persistenceType = clazz.getAnnotation(RegisterPersistence.class).value();
           }

            registerClass(clazz);
        }

        for (String className : registeredClasses.keySet()) {
            resolveDependencies(className);
        }
    }

    private void registerClass(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Não é possível anotar interfaces diretamente com @Injectable: " + clazz.getName());
        }
        String className = clazz.getName();
        Class<?>[] interfaces = clazz.getInterfaces();

        registeredClasses.put(className, clazz);
    }

    public Object resolveDependencies(String className) throws Exception {
        if (singletonInstances.containsKey(className)) {
            return singletonInstances.get(className);
        }

        Class<?> implementation = registeredClasses.get(className);

        if (implementation == null) {
            throw new IllegalStateException("Nenhuma implementação registrada para " + className);
        }

        Constructor<?> injectConstructor = getInjectableConstructor(implementation);

        Object instance = createInstance(injectConstructor);

        if(implementation.isAnnotationPresent(Singleton.class)) {
            System.out.println("Detectado anotação @Singleton Registrando instância singleton de " + className);
            singletonInstances.put(className, instance);
        }

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
            boolean isPersistenceType = parameterTypes[i].getName().equals("br.com.ucsal.persistencia.ProdutoRepository");


            if(isPersistenceType){
                System.out.println("Resolvendo dependência de persistência: " + persistenceType.getClassName());
                parameters[i] = resolveDependencies(persistencePackage + "." + persistenceType.getClassName());
                continue;
            }
            String dependencyName = parameterTypes[i].getName();
            parameters[i] = resolveDependencies(dependencyName);
        }

        return constructor.newInstance(parameters);
    }

    public Object getClassInContainer(String className) throws Exception {
        System.out.println("Retornando instância de " + className);
        return singletonInstances.get(className) != null ? singletonInstances.get(className) :resolveDependencies(className);
    }

    private static DependencyInjector dependencyInjector;

    public static DependencyInjector getInstance() {
        if (dependencyInjector == null) {
            dependencyInjector = new DependencyInjector();
        }
        return dependencyInjector;
    }
}
