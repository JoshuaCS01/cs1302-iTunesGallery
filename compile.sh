#!/bin/bash -ex

mvn -q -e clean
mvn -q -e compile
mvn exec:java -Dprism.order=sw -Dexec.mainClass="cs1302.gallery.GalleryDriver"
