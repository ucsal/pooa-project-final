package br.com.ucsal.controller;

import br.com.ucsal.util.command.CommandBus;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class InicializadorListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Carregue suas classes ou inicialize recursos aqui
            System.out.println("Inicializando recursos na inicialização da aplicação");
            CommandBus commandBus = CommandBus.getInstance();
            commandBus.scanCommandHandlers();
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

    }


}