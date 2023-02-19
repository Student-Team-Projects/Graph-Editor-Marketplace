#!/bin/bash
dalvik-exchange --dex --min-sdk-version=26 --output classes.dex graph-editor-plugin-server.plugins.refined_draw.main.jar
aapt add graph-editor-plugin-server.plugins.refined_draw.main.jar classes.dex