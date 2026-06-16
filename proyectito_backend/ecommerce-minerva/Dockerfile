# Etapa 1: Construcción (Instala Maven y compila tu código)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Prepara una computadora ligera con Java para correr tu app)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copia el archivo .jar que se generó en la Etapa 1
COPY --from=build /app/target/*.jar app.jar
# Expone el puerto por defecto de Spring Boot
EXPOSE 8080
# Comando para encender la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
