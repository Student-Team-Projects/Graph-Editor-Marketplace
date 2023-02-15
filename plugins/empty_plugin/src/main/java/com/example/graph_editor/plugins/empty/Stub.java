package com.example.graph_editor.plugins.empty;

import graph_editor.extensions.EditingPlugin;
import graph_editor.extensions.OnGraphOptionSelection;
import java.util.Collections;

public class Stub implements EditingPlugin {
    @Override
    public void activate(Proxy<OnGraphOptionSelection> proxy) {
        System.out.println("stub activates");
    }
    @Override
    public void deactivate(Proxy<OnGraphOptionSelection> proxy) {
        System.out.println("stub deactivates");
    }
    @Override
    public Iterable<String> usedPropertiesNames() {
        System.out.println("stub usedProperties");
        return Collections.emptyList();
    }
}
