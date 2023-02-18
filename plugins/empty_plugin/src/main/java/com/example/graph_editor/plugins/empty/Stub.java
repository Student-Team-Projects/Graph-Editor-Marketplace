package com.example.graph_editor.plugins.empty;

import graph_editor.extensions.Plugin;

public class Stub extends Plugin {
    @Override
    public void activate(Proxy proxy) {
        System.out.println("stub activates");
    }

    @Override
    public void deactivate(Proxy proxy) {
        System.out.println("stub deactivates");
    }

    @Override
    public boolean supportsDirectedGraphs() {
        return true;
    }

    @Override
    public boolean supportsUndirectedGraphs() {
        return true;
    }
}
