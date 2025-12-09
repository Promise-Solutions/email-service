# Use a imagem oficial do OpenJDK como base
FROM eclipse-temurin:21-jdk

# Diretório de trabalho dentro do container
WORKDIR /app

# Copie o JAR gerado pelo Maven para o container
COPY target/service.email-0.0.1-SNAPSHOT.jar app.jar

# Exponha a porta usada pela aplicação
EXPOSE 5005

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
