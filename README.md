# Repostería Rosato

Aplicación de escritorio JavaFX (FXML) para la gestión de la repostería.
Este scaffolding cubre:

- **Módulo 1 (Autenticación)** — login, registro, bloqueo por intentos.
- **Módulo 2 (Clientes y Pedidos)** — listado de clientes, nuevo pedido con
  cálculo de precio/tiempo y validaciones, confirmación (transacción de
  adelanto 50 %), listado de pedidos.
- **Módulo 4 (Inventario y Compras)** — ingredientes con alerta de stock
  bajo, proveedores y registro de compras con actualización de stock y
  costo promedio ponderado en transacción.

## Stack

- Java 17 + JavaFX 21 + FXML
- Maven
- **SQL Server** + JDBC (`mssql-jdbc`)
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
| 2.2 Nuevo pedido    | `NuevoPedido.fxml`      | `NuevoPedidoController`           |
| 2.3 Confirmación    | `ConfirmacionPedido.fxml` | `ConfirmacionPedidoController`  |
| 2.4 Listado pedidos | `ListadoPedidos.fxml`   | `ListadoPedidosController`        |
| 4.1 Inventario      | `ListadoIngredientes.fxml` | `ListadoIngredientesController` |
| 4.2 Editar ingrediente | `EditarIngrediente.fxml` | `EditarIngredienteController` |
| 4.3 Proveedores     | `ListadoProveedores.fxml`  | `ListadoProveedoresController`  |
| 4.4 Editar proveedor| `EditarProveedor.fxml`     | `EditarProveedorController`     |
| 4.5 Compras         | `ListadoCompras.fxml`      | `ListadoComprasController`      |
| 4.6 Nueva compra    | `NuevaCompra.fxml`         | `NuevaCompraController`         |
| Dashboard cliente   | `DashboardCliente.fxml` | `DashboardClienteController`      |
| Dashboard empleado  | `DashboardEmpleado.fxml`| `DashboardEmpleadoController`     |

El resto de pantallas de la especificación (Módulos 3, 5–10) se construirán
sobre esta base.

## Puesta en marcha

### 1. Base de datos

Con **SQL Server** (local o contenedor). Ejemplo con `sqlcmd`:

```bash
sqlcmd -S localhost -U sa -P "TuClave!" -i sql/schema_modulos_1_2.sql
sqlcmd -S localhost -U sa -P "TuClave!" -i sql/schema_modulo_4.sql
```

O copiar el contenido de `sql/schema_modulos_1_2.sql` y ejecutarlo
desde SQL Server Management Studio / Azure Data Studio.

### 2. Configurar la conexión

Edita `src/main/resources/com/rosato/config.properties` o exporta
variables de entorno:

```bash
export ROSATO_DB_URL="jdbc:sqlserver://localhost:1433;databaseName=reposteria_rosato;encrypt=false;trustServerCertificate=true"
export ROSATO_DB_USER="sa"
export ROSATO_DB_PASSWORD="TuClave!"
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

El proyecto usa **SQL Server** por defecto. Para cambiar:

- **MySQL:** sustituir driver por `com.mysql:mysql-connector-j`,
  URL `jdbc:mysql://HOST:3306/reposteria_rosato?useSSL=false&serverTimezone=UTC`
  y adaptar el esquema (`IDENTITY` → `AUTO_INCREMENT`, `BIT` → `BOOLEAN`,
  `SYSDATETIME()` → `CURRENT_TIMESTAMP`, `NVARCHAR(MAX)` → `TEXT`).
- **PostgreSQL:** driver `org.postgresql:postgresql`,
  URL `jdbc:postgresql://HOST:5432/reposteria_rosato`,
  y adaptar el esquema a tipos PostgreSQL (`SERIAL`, `BOOLEAN`,
  `TIMESTAMP`, etc.).
