package br.com.ucsal.controller;

import br.com.ucsal.persistencia.PersistenceType;
import br.com.ucsal.persistencia.RegisterPersistence;
import br.com.ucsal.util.command.CommandBus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;


@RegisterPersistence(PersistenceType.HSQL)
@WebServlet("/view/*") // Mapeia todas as requisições com "/view/*"
public class ProdutoController extends HttpServlet {

    private final CommandBus commandBus = CommandBus.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();


        if(path == null || path.equals("/")) {
            path = "/listarProdutos";
        }

        System.out.println("Path: " + path);

        this.commandBus.run(path, request, response);
    }
}