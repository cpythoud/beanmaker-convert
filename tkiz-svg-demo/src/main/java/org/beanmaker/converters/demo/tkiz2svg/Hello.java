package org.beanmaker.converters.demo.tkiz2svg;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "/hello")
public class Hello {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path( "/sayHello")
    public String sayHello() {
        return "Hello, World!";
    }

}
