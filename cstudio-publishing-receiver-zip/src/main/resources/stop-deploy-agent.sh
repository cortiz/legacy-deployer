#!/bin/sh
nohup java -server -Djava.ext.dirs=. org.craftercms.cstudio.publishing.StopServiceMain >> deployment.out 2>&1
