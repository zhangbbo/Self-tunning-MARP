#!/bin/bash

val=$(cat ~/hadoop/etc/hadoop/capacity-scheduler.xml | grep -oPm1 "(?<=<name>yarn.scheduler.capacity.maximum-am-resource-percent</name><value>)[^<]+")
oldval=$val

# "up", "down", or "set"
policy=$1
interval=$2

date +%s | awk '{print "Time(s) " $1}' | tee -a valueCapacitySchedulerLog
echo $val | awk '{print "Value " $1}' | tee -a valueCapacitySchedulerLog

if [ "$policy" = "up" ]
then
	val=$(echo "$val + $interval" | bc)
else
	if [ "$policy" = "down" ]
	then
		val=$(echo "$val - $interval" | bc)
	else
		val=$interval
	fi
fi


if [ $(echo "$val >= 1" | bc) -eq 1 ]
then
	val=1
fi

if [ $(echo "$val <= 0.1" | bc) -eq 1 ]
then
	val=.1
fi

date +%s | awk '{print "Time(s) " $1}' | tee -a valueCapacitySchedulerLog
echo $val | awk '{print "Value " $1}' | tee -a valueCapacitySchedulerLog
echo " " | tee -a valueCapacitySchedulerLog

if [ "$oldval" != "$val" ]
then
	sed -i "s|\(<name>yarn.scheduler.capacity.maximum-am-resource-percent</name><value>\)[^<>]*\(</value>\)|\1${val}\2|g" ~/hadoop/etc/hadoop/capacity-scheduler.xml
	/root/hadoop/bin/yarn rmadmin -refreshQueues
fi
