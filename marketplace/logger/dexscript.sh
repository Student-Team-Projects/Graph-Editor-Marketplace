#!/bin/bash
dalvik-exchange --dex --min-sdk-version=26 --output classes.dex graph-editor-plugin-server.plugins.empty_plugin.main.jar
aapt add graph-editor-plugin-server.plugins.empty_plugin.main.jar classes.dex