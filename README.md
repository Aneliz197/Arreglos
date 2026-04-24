# Repostería Rosato

Aplicación de escritorio JavaFX (FXML) para la gestión de la repostería.
Este scaffolding cubre el **Módulo 1 (Autenticación)** y la **base del
Módulo 2 (Clientes y Pedidos)**.

## Stack

- Java 17 + JavaFX 21 + FXML
- Maven
- MySQL 8 + JDBC (`mysql-connector-j`)
- BCrypt (`jbcrypt`) para hash de contraseñas
- JUnit 5

## Estructura

```
.
├── pom.xml
├── sql/
│   └── schema_modulos_1_2.sql        # Esquema MySQL (Módulos 1 y 2)
└── src/
    ├── main/
    │   ├── java/com/rosato/
    │   │   ├── App.java              # Punto de entrada JavaFX
    │   │   ├── controlador/          # Controllers FXML
    │   │   ├── dao/                  # Acceso a datos JDBC
    │   │   ├── modelo/               # POJOs (Cliente, Empleado, Pedido, ...)
    │   │   └── util/                 # Navegador, Sesión, PasswordUtil, SeedAdmin
    │   └── resources/com/rosato/
    │       ├── config.properties     # Configuración de conexión
    │       ├── estilos/styles.css
    │       └── vista/                # Archivos FXML
    └── test/java/com/rosato/...      # Tests JUnit 5
```

## Pantallas incluidas

| Pantalla de la spec | FXML                    | Controlador                       |
|---------------------|-------------------------|-----------------------------------|
| 1.1 Login           | `Login.fxml`            | `LoginController`                 |
| 1.2 Registro        | `Registro.fxml`         | `RegistroController`              |
| 2.1 Listado clientes| `ListadoClientes.fxml`  | `ListadoClientesController`       |
| 2.2 Nuevo pedido    | `NuevoPedido.fxml`      | `NuevoPedidoController` (stub)    |
| 2.4 Listado pedidos | `ListadoPedidos.fxml`   | `ListadoPedidosController`        |
| Dashboard cliente   | `DashboardCliente.fxml` | `DashboardClienteController`      |
| Dashboard empleado  | `DashboardEmpleado.fxml`| `DashboardEmpleadoController`     |

El resto de pantallas de la especificación (Módulos 3 a 10) se construirán
sobre esta base.

## Puesta en marcha

### 1. Base de datos

```bash
mysql -u root -p < sql/schema_modulos_1_2.sql
```

### 2. Configurar la conexión

Edita `src/main/resources/com/rosato/config.properties` o exporta
variables de entorno:

```bash
export ROSATO_DB_URL="jdbc:mysql://localhost:3306/reposteria_rosato?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export ROSATO_DB_USER="root"
export ROSATO_DB_PASSWORD="tu_clave"
```

### 3. Crear el administrador

```bash
mvn -q compile exec:java -Dexec.mainClass="com.rosato.util.SeedAdmin" \
    -Dexec.args="admin Admin123"
```

### 4. Ejecutar la aplicación

```bash
mvn -q javafx:run
```

### 5. Tests

```bash
mvn -q test
```

## Reglas de negocio ya implementadas (Módulo 1)

- Validación de credenciales contra `Cliente` y `Empleado`.
- Hash BCrypt para contraseñas.
- Bloqueo temporal tras 3 intentos fallidos
  (`auth.max.intentos` / `auth.bloqueo.minutos` en `config.properties`).
- Registro de cliente: usuario único, email único, contraseña fuerte
  (8+ chars, 1 mayúscula, 1 número), aceptación de términos,
  auto-login tras registro.

## Cambiar de motor de base de datos

`config.properties` apunta a MySQL por defecto. Para usar SQL Server o
PostgreSQL sustituye el driver en `pom.xml` y el JDBC URL:

- **SQL Server:** `com.microsoft.sqlserver:mssql-jdbc`,
  URL `jdbc:sqlserver://HOST;databaseName=reposteria_rosato;encrypt=false`.
- **PostgreSQL:** `org.postgresql:postgresql`,
  URL `jdbc:postgresql://HOST:5432/reposteria_rosato`.

El esquema SQL usa sintaxis MySQL (`AUTO_INCREMENT`, `BOOLEAN`,
`CURRENT_TIMESTAMP`, `CONCAT`). Para otros motores habrá que adaptarlo.
