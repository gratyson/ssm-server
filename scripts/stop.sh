#!/usr/bin/env bash
set -e

SSM_USER=ssm

if [[ $(sudo cat /home/$SSM_USER/application.pid) ]]; then
  sudo kill -15 $(sudo cat /home/$SSM_USER/application.pid)
fi