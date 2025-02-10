#!/usr/bin/env bash
set -e

VERSION=1.0
SSM_USER=ssm
BASE_HREF=/ssm/

STARTING_DIR=$PWD

sudo su -c "cd /home/ssm && java -jar -Dspring.profiles.active=prod -Dserver.servlet.context-path=$BASE_HREF /home/$SSM_USER/lib/ssm-$VERSION.jar > /dev/null 2>&1 &" $SSM_USER

cd $STARTING_DIR