package cz.cuni.mff.fruiton.component.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

@Component
public final class ServerAddressHelper {

    private static final String HTTP_SERVER_ADDRESS_FORMAT = "http://%s:%s%s/%s";

    @Value("${domain.address}")
    private String domainAddress;

    @Value("${server.port}")
    private int port;

    private final ServletContext servletContext;

    @Autowired
    public ServerAddressHelper(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getHttpAddress(final String path) {
        return String.format(HTTP_SERVER_ADDRESS_FORMAT, domainAddress, port, servletContext.getContextPath(), path);
    }

}
