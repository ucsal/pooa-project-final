package br.com.ucsal.util.command;

import br.com.ucsal.controller.Command;
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

    public  static CommandBus getInstance() {
        if(instance == null) {
            instance = new CommandBus();
        }

        return instance;
    }

    public  CommandBus() {
        this.commands = new HashMap<>();
    }

    public void scanCommandHandlers()  throws ServletException {
        try {
            Set<Class<?>> classes = new Reflections("br.com.ucsal.controller").getTypesAnnotatedWith(Rota.class);
            for (Class<?> clazz : classes) {
                if (Command.class.isAssignableFrom(clazz)) {
                    Rota rota = clazz.getAnnotation(Rota.class);
                    Command commandInstance = (Command) clazz.getDeclaredConstructor().newInstance();
                    commands.put(rota.value(), commandInstance);
                    System.out.println("Rota registrada: " + rota.value() + " -> " + clazz.getSimpleName());
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erro ao carregar rotas dinamicamente", e);
        }
    }

    public void run(String path, HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        Command command = commands.get(path);
        if (command != null) {
            command.execute(request, response);
        }else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Página não encontrada");
        }
    }
}
