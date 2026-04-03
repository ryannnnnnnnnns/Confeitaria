# Estágio 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Faz o build do projeto ignorando os testes para ser mais rápido (opcional)
RUN mvn clean package -DskipTests

# Estágio 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia o jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar
# Expõe a porta que a aplicação usa
EXPOSE 8080
# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]