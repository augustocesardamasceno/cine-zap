# Etapa 1: Build da aplicação usando Maven
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copie os arquivos de configuração e o código-fonte para dentro do container
COPY pom.xml .
# Baixa as dependências (cache de dependências) – opcional, mas recomendado para acelerar builds
RUN mvn dependency:go-offline

# Copie o restante do código-fonte
COPY src ./src

# Compile a aplicação e gere o .jar (pulando os testes para agilizar o build, se desejar)
RUN mvn clean package -DskipTests

# Etapa 2: Criar a imagem final com JRE
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copie o .jar gerado na etapa anterior para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Exponha a porta padrão (caso a aplicação rode na 8080)
EXPOSE 8080

# Defina o comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
