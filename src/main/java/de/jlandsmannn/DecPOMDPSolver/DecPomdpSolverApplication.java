package de.jlandsmannn.DecPOMDPSolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class DecPomdpSolverApplication {
    private final static Logger LOG = LoggerFactory.getLogger(DecPomdpSolverApplication.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(DecPomdpSolverApplication.class, args);
        LOG.info("STOPPING THE APPLICATION");
    }

}
