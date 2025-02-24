# Step 1: Use an official OpenJDK image as the base
FROM eclipse-temurin:21

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the application JAR file to the container
COPY target/g05-loggerserver.jar /app/g05-loggerserver.jar

ENTRYPOINT ["java", "-jar", "/app/g05-loggerserver.jar"]
