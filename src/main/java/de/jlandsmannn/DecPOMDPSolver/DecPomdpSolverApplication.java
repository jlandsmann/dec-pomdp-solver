package de.jlandsmannn.DecPOMDPSolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@ConfigurationPropertiesScan
@SpringBootApplication
public class DecPomdpSolverApplication {
  private final static Logger LOG = LoggerFactory.getLogger(DecPomdpSolverApplication.class);

  public static void main(String[] args) {
    LOG.info("STARTING THE APPLICATION");
    var application = new SpringApplication(DecPomdpSolverApplication.class);
    application.setBannerMode(Banner.Mode.OFF);
    application.setLazyInitialization(true);
    application.run(args);
    LOG.info("STOPPING THE APPLICATION");
  }

}
