#!/bin/bash
################################################################################
#                                    INSTRUCTIONS
################################################################################
# 1. Execute command to allow script execution: chmod 0700 script.sh (If needed)

# 2. Arguments for script are (1,2,3 or 4) (ex: ./script.sh 3):
#	1 - Clean the project
#	2 - Run the webserver with instrumented code
#	3 - Run the webserver without the instrumented code
#   4 - Run the loadbalancer
################################################################################
# ========================= Functions ============================

function cleanTempDirectory () {
	echo "[SCRIPT]CLEANING PROJECT..."
	cd $temp > /dev/null 2>&1
	if [ $? -eq 0 ]
	then
		echo "[SCRIPT]REMOVING ALL GENERATED FILES..."
		rm -r *
		cd ..
	else	#Directory doesn't exist
		echo "[SCRIPT]NOTHING TO BE CLEANED"
	fi
}

# ============================ Script =============================

temp="bin"

echo "[SCRIPT]STARTING AWESOME SCRIPT..."

task=$1	            #Get the first argument passed to the script

if [ $task = 1 ]	#Clean temp directory
then
	cleanTempDirectory
elif [ $task = 2 ] || [ $task = 3 ] || [ $task = 4 ]	#Clean and run
then
	cleanTempDirectory
	export CLASSPATH="$CLASSPATH:./src/main/lib/aws-java-sdk-1.11.123/*:./src/main/lib/BIT:./src/main/java/metrics:./src/main/java/raytracer:./src/main/java/raytracer/pigments:./src/main/java/raytracer/shapes:./src/main/java/webserver:./src/main/java:./bin/BIT:./bin/metrics:./bin/raytracer/:./bin/raytracer/pigments/:./bin/raytracer/shapes:./bin/webserver:./bin"
	export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
	mkdir $temp > /dev/null 2>&1	#Make temp directory where put binaries
	echo "[SCRIPT]COMPILING CODE..."
	javac -d $temp $(find . -name "*.java")
	if [ $? -eq 0 ]
	then
		echo "[SCRIPT]COMPILE SUCCESS!"
	else
		echo "[SCRIPT]PROBLEMS COMPILING!"
		exit 1
	fi
	cp -a ./src/main/resources/. ./bin
	if [ $? -eq 0 ]
	then
		echo "[SCRIPT]COPYING RESOURCES SUCCESS!"
	else
		echo "[SCRIPT]PROBLEMS COPYING RESOURCES!"
		exit 1
	fi
	cd $temp
	if [ $task = 2 ]	#Instrument code if asked
	then
		echo "[SCRIPT]INSTRUMENTING CODE..."
		java InstrumentRaytracer ./raytracer ./raytracer/pigments ./raytracer/shapes
		if [ $? -eq 0 ]
		then
			echo "[SCRIPT]INSTRUMENT CODE SUCCESS!"
		else
			echo "[SCRIPT]PROBLEMS IN INSTRUMENTATION APPLICATION"
			exit 1	#Exit the script
		fi
	fi
	if [ $task = 2 ] || [ $task = 3 ]
	then
        java webserver.MultiThreadedWebServerMain
    else
        java loadbalancer.LoadBalancerMain
    fi
else
	echo "[SCRIPT]WRONG SCRIPT ARGUMENT"
fi
