package br.com.ucsal.util.command;

import br.com.ucsal.controller.Command;
import br.com.ucsal.util.ioc.DependencyInjector;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CommandBus {
    private static CommandBus instance;
    private final Map<String, Command> commands;
    private final DependencyInjector dependencyInjector;

    public static CommandBus getInstance() {
        if (instance == null) {
            instance = new CommandBus();
        }

        return instance;
    }

    public CommandBus() {
        this.commands = new HashMap<>();
        this.dependencyInjector = DependencyInjector.getInstance();
    }

    // Escaneia todos os comandos anotados com @Rota
    public void scanCommandHandlers(String commandsDirector) throws ServletException {
        try {
            Set<Class<?>> classes = new Reflections(commandsDirector).getTypesAnnotatedWith(Rota.class);
            for (Class<?> clazz : classes) {
                if (Command.class.isAssignableFrom(clazz)) {
                    Rota rota = clazz.getAnnotation(Rota.class);
                    Command commandInstance = createCommandInstance(clazz);  // Usa o DependencyInjector para criar a instância
                    commands.put(rota.value(), commandInstance);
                    System.out.println("Rota registrada: " + rota.value() + " -> " + clazz.getSimpleName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Erro ao carregar rotas dinamicamente", e);
        }
    }

    // Cria a instância do comando utilizando o DependencyInjector (IOC)
    private Command createCommandInstance(Class<?> clazz) throws Exception {
        Command commandInstance = (Command) dependencyInjector.getClassInContainer(clazz.getName());

        if (commandInstance == null) {
            throw new IllegalStateException("Não foi possível criar a instância do comando: " + clazz.getSimpleName());
        }
        return commandInstance;
    }


    public void run(String path, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Command command = commands.get(path);
        if (command != null) {
            command.execute(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Página não encontrada");
        }
    }
}
