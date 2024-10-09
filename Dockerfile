FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre AS exec
VOLUME /tmp

ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY scripts /scripts
COPY problems /problems

ENV SPRING_PROFILES_ACTIVE=production
ENV SPRING_SHELL_INTERACTIVE_ENABLED=false
ENV PROBLEM=GridSmall
ENV SCRIPT=/scripts/solve-${PROBLEM}

ENTRYPOINT java -cp app:app/lib/* de.jlandsmannn.DecPOMDPSolver.DecPomdpSolverApplication @${SCRIPT}