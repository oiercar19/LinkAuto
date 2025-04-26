
![example workflow](https://github.com/oiercar19/LinkAuto/actions/workflows/maven.yml/badge.svg)

# LinkAuto

LinkAuto es una red social para entusiastas de automóviles que permite a los usuarios conectarse, compartir contenido relacionado con vehículos y construir una comunidad en torno a su pasión por los coches.

## Requisitos previos

- Java 11 o superior
- Maven 3.9 o superior
- MySQL 8.0 o superior (o base de datos alternativa compatible)
- Navegador web moderno (Chrome, Firefox, Safari, Edge)

## Estructura del proyecto

El proyecto está dividido en dos módulos principales:
- `rest-api`: Backend basado en Spring Boot que proporciona los servicios REST
- `client`: Frontend que consume los servicios del backend

## Instalación y configuración

### 1. Configuración de la base de datos

Cree una base de datos MySQL utilizando el script SQL proporcionado:

```
rest-api/resources/dbsetup.sql
```

Este script creará:
- La base de datos de LinkAuto
- Un usuario `la` con contraseña `la` con los permisos necesarios

> **Nota**: Si desea utilizar credenciales diferentes, deberá modificar el archivo `rest-api/resources/application.properties` con los nuevos valores.

### 2. Configuración alternativa de base de datos (opcional)

Si prefiere usar otra base de datos diferente a MySQL, modifique el archivo `rest-api/resources/application.properties` con la configuración adecuada para su sistema de gestión de base de datos, incluyendo los drivers correspondientes.

## Ejecución del proyecto

Desde la carpeta raíz del proyecto, siga estos pasos:

### 1. Limpiar compilaciones anteriores

```
mvn clean
```

### 2. Iniciar el servidor backend (rest-api)

```
mvn -pl rest-api spring-boot:run
```

El servidor se iniciará por defecto en el puerto 8080.

### 3. Iniciar el cliente frontend

```
mvn -pl client spring-boot:run
```

El cliente se iniciará por defecto en el puerto 8081.

## Acceso a la aplicación

Una vez que el servidor y el cliente estén en funcionamiento, puede acceder a LinkAuto a través de:

```
http://localhost:8081
```

## Ejecución de tests

Puede ejecutar las distintas pruebas del proyecto utilizando los siguientes comandos:

- **Tests unitarios**:

  ```
  mvn test
  ```

- **Tests de integración**:

  ```
  mvn -Pintegration integration-test
  ```

- **Tests de rendimiento**:

  ```
  mvn -Pperformance integration-test
  ```

## Configuración personalizada

Puede modificar la configuración del proyecto editando los siguientes archivos:

- `rest-api/resources/application.properties`: Para cambiar la configuración del backend (puerto, conexión a la base de datos, etc.)
- `client/resources/application.properties`: Para cambiar la configuración del frontend (puerto, URL del backend, etc.)

## Solución de problemas comunes

- **Error de conexión a la base de datos**: Verifique las credenciales y la disponibilidad del servidor de base de datos.
- **Error de puertos en uso**: Asegúrese de que los puertos 8080 y 8081 no estén siendo utilizados por otras aplicaciones. Si es necesario, puede cambiar los puertos en los archivos de configuración.
- **Problemas de compilación**: Asegúrese de tener instalada la versión correcta de Java y Maven.

## Contacto y soporte

Para obtener ayuda o reportar problemas, puede crear un nuevo issue en el repositorio del proyecto o contactar al equipo de desarrollo.

## Licencia

Este proyecto está licenciado bajo la licencia GPL-3.0. Consulte el archivo LICENSE para más detalles.
