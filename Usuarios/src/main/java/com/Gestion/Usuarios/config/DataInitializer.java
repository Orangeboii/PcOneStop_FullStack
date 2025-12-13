package com.Gestion.Usuarios.config;

import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.UserRepository;
import com.Gestion.Usuarios.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${app.data.initializer.enabled:true}")
    private boolean dataInitializerEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!dataInitializerEnabled) {
            logger.info("=== CARGA INICIAL DE DATOS DESHABILITADA ===");
            return;
        }

        long userCount = userRepository.count();
        logger.info("=== VERIFICANDO USUARIOS PRECARGADOS ===");

        // Crear usuario ADMIN si no existe
        User adminUser = userRepository.findByEmail("admin@pconestop.com");
        if (adminUser == null) {
            logger.info("=== CREANDO USUARIO ADMIN PRECARGADO ===");
            User admin = new User();
            admin.setFirstName("Administrador");
            admin.setLastName("PcOneStop");
            admin.setEmail("admin@pconestop.com");
            admin.setPassword("admin123"); // Se encriptará automáticamente
            admin.setRole("ADMIN");
            userService.save(admin);
            logger.info("✅ Usuario ADMIN creado: admin@pconestop.com / admin123");
        } else {
            logger.info("ℹ️  Usuario ADMIN ya existe: admin@pconestop.com");
        }

        // Crear usuario CLIENTE si no existe
        User clienteUser = userRepository.findByEmail("cliente@pconestop.com");
        if (clienteUser == null) {
            logger.info("=== CREANDO USUARIO CLIENTE PRECARGADO ===");
            User cliente = new User();
            cliente.setFirstName("Juan");
            cliente.setLastName("Pérez");
            cliente.setEmail("cliente@pconestop.com");
            cliente.setPassword("cliente123"); // Se encriptará automáticamente
            cliente.setRole("CLIENTE");
            userService.save(cliente);
            logger.info("✅ Usuario CLIENTE creado: cliente@pconestop.com / cliente123");
        } else {
            logger.info("ℹ️  Usuario CLIENTE ya existe: cliente@pconestop.com");
        }

        long finalCount = userRepository.count();
        if (finalCount > userCount) {
            logger.info("=== {} USUARIOS PRECARGADOS CREADOS EXITOSAMENTE ===", finalCount - userCount);
        } else {
            logger.info("=== TODOS LOS USUARIOS PRECARGADOS YA EXISTÍAN ===");
        }
    }
}
