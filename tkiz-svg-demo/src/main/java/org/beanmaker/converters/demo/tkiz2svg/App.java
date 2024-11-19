package org.beanmaker.converters.demo.tkiz2svg;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/convert")
public class App extends Application {

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public App() {
        singletons.add( new Hello());
        singletons.add(new TkizToSvg(
                 "/home/chris/Prov/tkiz2svg/work",
                "/home/chris/Prov/tkiz2svg/result",
                 "/home/chris/Prov/tkiz2svg/error"));
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
