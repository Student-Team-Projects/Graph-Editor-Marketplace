package com.example.graph_editor.plugins.empty;

import graph_editor.extensions.Plugin;

import java.util.Collections;

public class Stub implements Plugin {
    @Override
    public void activate(Proxy proxy) {

    }

    @Override
    public void deactivate(Proxy proxy) {

    }

    @Override
    public Iterable<String> usedPropertiesNames() {
        return Collections.emptyList();
    }
}
