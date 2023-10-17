#!/bin/bash


cmd=$1
file_name=$2

log="/var/log/endit-tss/writeLog.log"
exec &>> $log

file="/etc/endit-tss.properties"

request_dir=""
out_dir=""

if [ -f "$file" ]
then
    	request_dir=`sed '/^\#/d' $file | grep 'write.request.dir.path'  | tail -n 1 | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`
	out_dir=`sed '/^\#/d' $file | grep 'write.out.dir.path'  | tail -n 1 | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`

else
    	echo "$file not found."
fi


#echo "Request_dir: " $request_dir
#echo "Out_dir: " $out_dir
#echo "File_Name: " $file_name


request_file=$request_dir/$file_name
out_file=$out_dir/$file_name


#echo $request_file
#echo $out_file


eval $cmd &

pid=$!

if [ $pid ]; then

	echo $(date '+%Y-%m-%d %H:%M:%S') "INFO bash:- TSS process opened, file: " $file_name 

	#add 'tss-pid' to metadata file
	
	jq_tss_pid='."tss-pid"='$pid
	jq $jq_tss_pid $request_file >$request_file.tmp
	mv -u $request_file.tmp $request_file

	wait $pid

	exitValue=$? 

	if [ $exitValue -eq 0 ]; then
  
		# add 'tss-pid-exitValue' to metadata file

  		jq_tss_pid_exitValue='."tss-pid-exitValue"='$exitValue
  		jq $jq_tss_pid_exitValue $request_file >$request_file.tmp
  		mv -u $request_file.tmp $request_file
		
		#remove a file from "out" directory

  		if [ -f "$out_file" ]; then
      			rm $out_file
			echo $(date '+%Y-%m-%d %H:%M:%S') "INFO bash:- File: " $file_name " removed from 'out' directory by bash script."
 
  		fi

	else
 	echo $(date '+%Y-%m-%d %H:%M:%S') "INFO bash:- TSS process interrupted: FileName:" $file_name 
  	jq_tss_pid_exitValue='."tss-pid-exitValue"='$exitValue
  	jq $jq_tss_pid_exitValue $request_file >$request_file.tmp
  	mv -u $request_file.tmp $request_file
     
	fi
  	  
else

  echo $(date '+%Y-%m-%d %H:%M:%S') "INFO bash:- TSS process opening failed: FileName: " $file_name 

fi

