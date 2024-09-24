FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine as exec
VOLUME /tmp

RUN addgroup -S solver && adduser -S solver -G solver
USER solver

ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
COPY scripts /scripts

ENV SPRING_PROFILES_ACTIVE=production
ENV SPRING_SHELL_INTERACTIVE_ENABLED=false
ENV PROBLEM=GridSmall
ENV SCRIPT=/scripts/solve-${PROBLEM}

ENTRYPOINT java -cp app:app/lib/* de.jlandsmannn.DecPOMDPSolver.DecPomdpSolverApplication @${SCRIPT}