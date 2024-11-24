package br.com.ucsal.controller;

import br.com.ucsal.ui.Rota;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebServlet("/view/*") // Mapeia todas as requisições com "/view/*"
public class ProdutoController extends HttpServlet {

    private Map<String, Command> commands = new HashMap<>();

    @Override
    public void init() throws ServletException {
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

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo(); // Obtém a parte da URL após "/view/"
        Command command = commands.get(path); // Encontra o comando correspondente

        if (command != null) {
            command.execute(request, response); // Executa o comando
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Página não encontrada");
        }
    }
}