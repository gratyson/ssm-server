#!/usr/bin/env bash
set -e

STARTING_DIR=$PWD
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SSM_SERVER_DIR=$SCRIPT_DIR/..
SSM_CLIENT_DIR=$SCRIPT_DIR/../../ssm-client
SSM_CLIENT_BUILD_DIR=$SSM_CLIENT_DIR/dist/ssm-client/browser
SSM_USER=ssm
BASE_HREF=/ssm/

$SCRIPT_DIR/stop.sh

cd $SSM_CLIENT_DIR
ng build --base-href $BASE_HREF
if [ -n "$(ls -A $SSM_SERVER_DIR/src/main/resources/static/ 2>/dev/null)" ]
then
  rm -rf $SSM_SERVER_DIR/src/main/resources/static/*
fi
cp -r $SSM_CLIENT_BUILD_DIR/* $SSM_SERVER_DIR/src/main/resources/static/
rm -rf $SSM_CLIENT_DIR/dist/

cd $SSM_SERVER_DIR
./gradlew build

sudo mkdir -p /home/$SSM_USER/scripts
sudo cp $SSM_SERVER_DIR/scripts/start.sh /home/$SSM_USER/scripts/start.sh
sudo cp $SSM_SERVER_DIR/scripts/stop.sh /home/$SSM_USER/scripts/stop.sh
sudo cp $SSM_SERVER_DIR/scripts/backup.sh /home/$SSM_USER/scripts/backup.sh
sudo chown -R $SSM_USER:$SSM_USER /home/$SSM_USER/scripts

sudo mkdir -p /home/$SSM_USER/lib/
sudo cp $SSM_SERVER_DIR/build/libs/* /home/$SSM_USER/lib
sudo chown -R $SSM_USER:$SSM_USER /home/$SSM_USER/lib

$SCRIPT_DIR/start.sh

cd $STARTING_DIR
