package com.biblioteca.bibliotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación BiblioTech.
 * Sistema de Gestión de Biblioteca con control de préstamos,
 * autenticación simple y reportes.
 * 
 * @author BiblioTech Team
 * @version 1.0.0
 */
@SpringBootApplication
public class BibliotechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotechApplication.class, args);
        System.out.println("========================================");
        System.out.println("  BIBLIOTECH - Sistema de Biblioteca");
        System.out.println("  Aplicación iniciada correctamente");
        System.out.println("  URL: http://localhost:8080/bibliotech");
        System.out.println("========================================");
    }
}
