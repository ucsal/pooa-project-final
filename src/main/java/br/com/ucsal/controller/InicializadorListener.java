package br.com.ucsal.controller;

import br.com.ucsal.util.DatabaseUtil;
import br.com.ucsal.util.command.CommandBus;
import br.com.ucsal.util.ioc.DependencyInjector;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class InicializadorListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("Inicializando recursos na inicialização da aplicação");
            CommandBus commandBus = CommandBus.getInstance();
            DependencyInjector dependencyInjector = DependencyInjector.getInstance();

            dependencyInjector.scanAndRegister("br.com.ucsal");
            commandBus.scanCommandHandlers("br.com.ucsal");

            System.out.println("Iniciando o banco de dados HSQLDB...");
            DatabaseUtil.iniciarBanco();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}