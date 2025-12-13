package com.Catalogo.Inventario.config;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ProductRepository productRepository;

    @Value("${app.data.initializer.enabled:true}")
    private boolean dataInitializerEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!dataInitializerEnabled) {
            logger.info("=== CARGA INICIAL DE DATOS DESHABILITADA ===");
            return;
        }

        // Solo cargar datos si la base de datos está vacía
        long productCount = productRepository.count();
        if (productCount == 0) {
            logger.info("=== INICIANDO CARGA DE PRODUCTOS PRECARGADOS ===");
            List<Product> products = createInitialProducts();
            productRepository.saveAll(products);
            logger.info("=== {} PRODUCTOS PRECARGADOS INSERTADOS EXITOSAMENTE ===", products.size());
            logger.info("=== CATEGORÍAS: GPU (5), CPU (5), RAM (5), SSD (5), Motherboard (5), PSU (5), Case (5), Cooler (5), Peripheral (5) ===");
        } else {
            logger.info("=== LA BASE DE DATOS YA CONTIENE {} PRODUCTOS. OMITIENDO CARGA INICIAL ===", productCount);
        }
    }

    private List<Product> createInitialProducts() {
        return Arrays.asList(
            // ============================================
            // GPUs (Tarjetas Gráficas)
            // ============================================
            createProduct("GeForce RTX 4070", "MSI", "Ventus 3X OC", "GPU", 899990.00, 8,
                "Tarjeta gráfica NVIDIA RTX 4070 con 12GB GDDR6X. Rendimiento excepcional para gaming en 1440p y 4K."),
            createProduct("GeForce RTX 4060 Ti", "ASUS", "DUAL OC", "GPU", 649990.00, 5,
                "Tarjeta gráfica NVIDIA RTX 4060 Ti con 16GB GDDR6. Ideal para gaming en 1080p y 1440p."),
            createProduct("Radeon RX 7800 XT", "AMD", "Reference", "GPU", 799990.00, 10,
                "Tarjeta gráfica AMD Radeon RX 7800 XT con 16GB GDDR6. Excelente relación precio-rendimiento."),
            createProduct("GeForce RTX 4090", "NVIDIA", "Founders Edition", "GPU", 1999990.00, 2,
                "Tarjeta gráfica flagship NVIDIA RTX 4090 con 24GB GDDR6X. Máximo rendimiento para gaming y creación de contenido."),
            createProduct("Radeon RX 7600", "Sapphire", "Pulse OC", "GPU", 449990.00, 7,
                "Tarjeta gráfica AMD Radeon RX 7600 con 8GB GDDR6. Perfecta para gaming en 1080p."),

            // ============================================
            // CPUs (Procesadores)
            // ============================================
            createProduct("Intel Core i9-14900K", "Intel", "Core i9-14900K", "CPU", 699990.00, 6,
                "Procesador Intel Core i9-14900K de 14ª generación. 24 núcleos (8P+16E), hasta 6.0GHz. Socket LGA1700."),
            createProduct("AMD Ryzen 7 7800X3D", "AMD", "Ryzen 7 7800X3D", "CPU", 599990.00, 9,
                "Procesador AMD Ryzen 7 7800X3D con tecnología 3D V-Cache. 8 núcleos, 16 hilos. Socket AM5."),
            createProduct("Intel Core i5-13600K", "Intel", "Core i5-13600K", "CPU", 399990.00, 10,
                "Procesador Intel Core i5-13600K de 13ª generación. 14 núcleos (6P+8E), hasta 5.1GHz. Excelente relación precio-rendimiento."),
            createProduct("AMD Ryzen 5 7600X", "AMD", "Ryzen 5 7600X", "CPU", 299990.00, 8,
                "Procesador AMD Ryzen 5 7600X de 6 núcleos y 12 hilos. Socket AM5. Ideal para gaming y productividad."),
            createProduct("Intel Core i7-14700K", "Intel", "Core i7-14700K", "CPU", 549990.00, 4,
                "Procesador Intel Core i7-14700K de 14ª generación. 20 núcleos (8P+12E), hasta 5.6GHz."),

            // ============================================
            // RAM (Memoria)
            // ============================================
            createProduct("Corsair Vengeance DDR5 32GB", "Corsair", "CMK32GX5M2B5600C36", "RAM", 199990.00, 10,
                "Kit de memoria DDR5 32GB (2x16GB) a 5600MHz CL36. RGB opcional."),
            createProduct("G.Skill Trident Z5 DDR5 32GB", "G.Skill", "F5-6000J3636F16GX2-TZ5RK", "RAM", 219990.00, 7,
                "Kit de memoria DDR5 32GB (2x16GB) a 6000MHz CL36. Diseño RGB premium."),
            createProduct("Kingston Fury Beast DDR5 16GB", "Kingston", "KF556C40BB-16", "RAM", 99990.00, 9,
                "Kit de memoria DDR5 16GB (2x8GB) a 5600MHz CL40. Sin RGB, excelente precio."),
            createProduct("Corsair Dominator Platinum DDR5 64GB", "Corsair", "CMT64GX5M2B6400C32", "RAM", 499990.00, 3,
                "Kit de memoria DDR5 64GB (2x32GB) a 6400MHz CL32. Máximo rendimiento."),
            createProduct("TeamGroup T-Force Delta DDR5 32GB", "TeamGroup", "TF5D532G6000HC38ADC01", "RAM", 189990.00, 6,
                "Kit de memoria DDR5 32GB (2x16GB) a 6000MHz CL38. RGB personalizable."),

            // ============================================
            // SSD (Almacenamiento)
            // ============================================
            createProduct("Samsung 990 PRO 1TB", "Samsung", "MZ-V9P1T0BW", "SSD", 199990.00, 8,
                "SSD NVMe PCIe 4.0 de 1TB. Velocidades de lectura hasta 7450 MB/s y escritura hasta 6900 MB/s."),
            createProduct("WD Black SN850X 2TB", "Western Digital", "WDS200T2X0E", "SSD", 349990.00, 5,
                "SSD NVMe PCIe 4.0 de 2TB. Velocidades de lectura hasta 7300 MB/s. Ideal para gaming."),
            createProduct("Crucial P5 Plus 1TB", "Crucial", "CT1000P5PSSD8", "SSD", 149990.00, 10,
                "SSD NVMe PCIe 4.0 de 1TB. Excelente relación precio-rendimiento. Velocidades hasta 6600 MB/s."),
            createProduct("Kingston NV2 2TB", "Kingston", "SNV2S/2000G", "SSD", 149990.00, 7,
                "SSD NVMe PCIe 4.0 de 2TB. Económico y confiable. Velocidades hasta 3500 MB/s."),
            createProduct("Seagate FireCuda 530 1TB", "Seagate", "ZP1000GM30013", "SSD", 219990.00, 4,
                "SSD NVMe PCIe 4.0 de 1TB. Velocidades de lectura hasta 7300 MB/s. Incluye disipador térmico."),

            // ============================================
            // Motherboards (Placas Base)
            // ============================================
            createProduct("ASUS ROG Strix X670E-E Gaming", "ASUS", "ROG STRIX X670E-E GAMING WIFI", "Motherboard", 699990.00, 6,
                "Placa base AMD AM5 para Ryzen 7000. Chipset X670E, WiFi 6E, PCIe 5.0, DDR5."),
            createProduct("MSI MAG B650 Tomahawk", "MSI", "MAG B650 TOMAHAWK WIFI", "Motherboard", 349990.00, 9,
                "Placa base AMD AM5. Chipset B650, WiFi 6, PCIe 4.0, DDR5. Excelente para gaming."),
            createProduct("Gigabyte Z790 Aorus Elite AX", "Gigabyte", "Z790 AORUS ELITE AX", "Motherboard", 449990.00, 5,
                "Placa base Intel LGA1700. Chipset Z790, WiFi 6E, PCIe 5.0, DDR5."),
            createProduct("ASRock B650M Pro4", "ASRock", "B650M Pro4", "Motherboard", 199990.00, 8,
                "Placa base AMD AM5 micro-ATX. Chipset B650, PCIe 4.0, DDR5. Económica y confiable."),
            createProduct("MSI MPG Z790 Carbon WiFi", "MSI", "MPG Z790 CARBON WIFI", "Motherboard", 599990.00, 3,
                "Placa base Intel LGA1700 premium. Chipset Z790, WiFi 6E, PCIe 5.0, RGB."),

            // ============================================
            // PSU (Fuentes de Poder)
            // ============================================
            createProduct("Corsair RM850x 850W", "Corsair", "CP-9020180-NA", "PSU", 199990.00, 10,
                "Fuente de poder modular 850W 80 Plus Gold. Silenciosa y eficiente."),
            createProduct("Seasonic Focus GX-750 750W", "Seasonic", "SSR-750FX", "PSU", 179990.00, 7,
                "Fuente de poder modular 750W 80 Plus Gold. Confiable y con garantía extendida."),
            createProduct("EVGA SuperNOVA 1000 G5", "EVGA", "220-G5-1000-X1", "PSU", 299990.00, 4,
                "Fuente de poder modular 1000W 80 Plus Gold. Ideal para sistemas high-end."),
            createProduct("be quiet! Straight Power 11 750W", "be quiet!", "BN295", "PSU", 219990.00, 6,
                "Fuente de poder modular 750W 80 Plus Platinum. Ultra silenciosa."),
            createProduct("Cooler Master MWE Gold 650W", "Cooler Master", "MPE-6501-ACAAG-US", "PSU", 99990.00, 9,
                "Fuente de poder modular 650W 80 Plus Gold. Excelente relación precio-calidad."),

            // ============================================
            // Cases (Gabinete)
            // ============================================
            createProduct("Corsair 4000D Airflow", "Corsair", "CC-9011200-WW", "Case", 149990.00, 8,
                "Gabinete mid-tower con excelente flujo de aire. Compatible con E-ATX, ATX, mATX, mITX."),
            createProduct("Fractal Design Define 7", "Fractal Design", "FD-C-DEF7C-01", "Case", 199990.00, 5,
                "Gabinete full-tower silencioso. Espacioso y con excelente gestión de cables."),
            createProduct("NZXT H5 Flow", "NZXT", "CA-H52FB-01", "Case", 129990.00, 10,
                "Gabinete mid-tower con diseño minimalista. Excelente ventilación y construcción."),
            createProduct("Lian Li O11 Dynamic EVO", "Lian Li", "O11DEX", "Case", 249990.00, 3,
                "Gabinete mid-tower premium con diseño único. Ideal para builds con water cooling."),
            createProduct("Phanteks Eclipse P400A", "Phanteks", "PH-EC400ATG_BK01", "Case", 119990.00, 7,
                "Gabinete mid-tower con mesh front panel. Excelente flujo de aire y precio."),

            // ============================================
            // Coolers (Refrigeración)
            // ============================================
            createProduct("Noctua NH-D15", "Noctua", "NH-D15", "Cooler", 149990.00, 9,
                "Cooler de aire de doble torre. Máximo rendimiento y silencio. Compatible con todos los sockets modernos."),
            createProduct("Corsair iCUE H150i Elite", "Corsair", "CW-9060071-WW", "Cooler", 299990.00, 6,
                "AIO water cooler 360mm con RGB. Pantalla LCD integrada. Excelente para overclocking."),
            createProduct("be quiet! Dark Rock Pro 4", "be quiet!", "BK022", "Cooler", 129990.00, 8,
                "Cooler de aire de doble torre ultra silencioso. Diseño elegante y rendimiento excelente."),
            createProduct("Arctic Liquid Freezer II 280", "Arctic", "ACFRE00068A", "Cooler", 179990.00, 5,
                "AIO water cooler 280mm. Excelente relación precio-rendimiento. Sin RGB."),
            createProduct("Thermalright Peerless Assassin 120", "Thermalright", "PA120", "Cooler", 69990.00, 10,
                "Cooler de aire de doble torre económico. Excelente rendimiento por su precio."),

            // ============================================
            // Peripherals (Periféricos)
            // ============================================
            createProduct("Logitech G Pro X Superlight", "Logitech", "910-005631", "Peripheral", 149990.00, 10,
                "Mouse inalámbrico gaming ultra ligero. Sensor HERO 25K. Batería de larga duración."),
            createProduct("Razer DeathAdder V3 Pro", "Razer", "RZ01-04630200-R3M1", "Peripheral", 159990.00, 7,
                "Mouse inalámbrico gaming ergonómico. Sensor Focus Pro 30K. Diseño para diestros."),
            createProduct("Corsair K70 RGB TKL", "Corsair", "CH-9119014-NA", "Peripheral", 199990.00, 5,
                "Teclado mecánico gaming tenkeyless. Switches Cherry MX. RGB per-key."),
            createProduct("SteelSeries Arctis 7P+", "SteelSeries", "61510", "Peripheral", 249990.00, 4,
                "Auriculares inalámbricos gaming multiplataforma. Sonido surround 7.1. Batería de 30 horas."),
            createProduct("HyperX Cloud Alpha", "HyperX", "HX-HSCA-RD/EM", "Peripheral", 99990.00, 8,
                "Auriculares gaming con cable. Sonido estéreo de alta calidad. Micrófono desmontable.")
        );
    }

    private Product createProduct(String name, String brand, String model, String category, 
                                  Double price, Integer stock, String description) {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setModel(model);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        product.setIsOnSale(false);
        product.setDiscount(0);
        return product;
    }
}
