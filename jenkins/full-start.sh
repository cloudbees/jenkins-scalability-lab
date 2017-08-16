#!/bin/bash
# Quick bolt-on to launch telegraf as well as Jenkins, with the telegraf process launching silently in the background
telegraf 0<&- &>/dev/null &
/usr/local/bin/jenkins.sh
