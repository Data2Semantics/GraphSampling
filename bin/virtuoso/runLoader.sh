#!/bin/bash

source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;
isqlFile="${HOME}/.isqlCmdFile.sql"


echo "running loader"
echo "rdf_loader_run();&" > $isqlFile;
echo "EXIT;" >> $isqlFile;
echo "" >> $isqlFile;
cat $isqlFile | isql;

