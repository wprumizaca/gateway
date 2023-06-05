
FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=target/*.jar

#Renombramos nuestro *.jar
COPY ${JAR_FILE} app.jar

# LLevarlos al servidor y ejecutarlo. Esto se ejecuta dentro del contenedor
ENTRYPOINT ["java", "-jar", "/app.jar"]

