package org.beanmaker.converters.demo.tkiz2svg;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/convert")
public class App extends Application {

    private static final String BASE_DIR = "/home/chris/Prov/tkiz2svg/";

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public App() {
        singletons.add( new Hello());
        singletons.add(new TkizToSvg(
                BASE_DIR + "work",
                BASE_DIR + "result",
                BASE_DIR + "error")
        );
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
