FROM maven:3.6-openjdk-8

WORKDIR /app
COPY . .

RUN mvn clean package -Dmaven.test.skip=true

EXPOSE 9001

#CMD ["java","-jar","/app/target/prescription.data.exchange-0.0.1-SNAPSHOT.jar"]