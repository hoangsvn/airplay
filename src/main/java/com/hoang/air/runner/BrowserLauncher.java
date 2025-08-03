package com.hoang.air.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
public class BrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() throws URISyntaxException, IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("http://localhost:9000"));
        } else {
            log.info("Open: {}", new URI("http://localhost:9000"));
        }
    }
}
