#!/bin/bash
dalvik-exchange --dex --min-sdk-version=26 --output classes.dex graph-editor-plugin-server.plugins.turbo_matching.main.jar
aapt add graph-editor-plugin-server.plugins.turbo_matching.main.jar classes.dex