package Marcos.desafio.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootController {
    
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @GetMapping
    public String welcomeMessage() {
        logger.info("Acesso à raiz do backend");
        return "Bem-vindo ao backend da aplicação!";
    }
}