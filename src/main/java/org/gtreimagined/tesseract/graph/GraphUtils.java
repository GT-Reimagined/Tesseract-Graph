package org.gtreimagined.tesseract.graph;

import java.util.ServiceLoader;

public interface GraphUtils {
    GraphUtils INSTANCE =  ServiceLoader.load(GraphUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of GraphUtils found"));

    boolean isDevEnvironment();
}
