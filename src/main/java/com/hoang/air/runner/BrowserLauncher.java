package com.hoang.air.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BrowserLauncher {

    private static final Logger log = LogManager.getLogger(BrowserLauncher.class);

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() throws URISyntaxException, IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("http://localhost:9000"));
        } else {
            log.info("Open: {}", new URI("http://localhost:9000"));
        }
    }
}
