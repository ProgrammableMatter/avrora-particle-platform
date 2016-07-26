#!/bin/bash

SCRIPT=$(readlink -f $0)
SCRIPTPATH=`dirname $SCRIPT`
OLDPWD=`pwd`

cd $SCRIPTPATH
mvn clean compile assembly:assembly -DskipTests
cd $OLDPWD
