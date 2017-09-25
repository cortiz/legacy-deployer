#!/bin/sh
nohup java -server -Djava.ext.dirs=. -classpath .:conf org.craftercms.cstudio.publishing.PublishingReceiverMain >> deployment.out 2>&1&
