#!/bin/bash

source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;
isqlFile="${HOME}/.isqlCmdFile.sql"

echo "UPDATE DB.DBA.load_list SET ll_state = 0 WHERE ll_state = 1;" > $isqlFile;
echo "EXIT;" >> $isqlFile;
echo "" >> $isqlFile;
cat $isqlFile | isql;

echo "running 5 loaders"
runLoader.sh
runLoader.sh
runLoader.sh
runLoader.sh
runLoader.sh
