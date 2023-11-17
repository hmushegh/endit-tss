**Endit-TSS software**

Endit-TSS is an intermediate software that works in conjunction with the dCache ENDIT-Provider plugin and the TSS-client (IBM SP based tape client) which must be pre-installed and properly configured. It was used to efficiently write/flush and stage/retrieve files to and from the IBM SP tape storage system.

Works fine with OpenJDK version 13 and above. 

To compile the plugin, run:
> mvn package

To install the software, simple install it as an RPM package available in the /endit-tss/rpms directory. It will be installed in /usr/share/endit-tss/.
> yum install endit-tss-*.*-*x86_64.rpm

To run/start the software as service, use systemctl commands as follows: 
To start the service:
> systemctl start endit-tss@read.service

> systemctl start endit-tss@write.service

To stop the service:
> systemctl stop endit-tss@read.service

> systemctl stop endit-tss@write.service

To check the status of the service:
> systemctl status endit-tss@read.service

> systemctl status endit-tss@write.service

Configuration
The default configuration file (/endit-tss/rpms/endit-tss.properties) needs to be adapted to your use case.

The file contains the following parameters:
- _write.out.dir.path=/export/GridKatest/f01-129-131-e_wT_tst/data/out_  //Path to the directory where the dCache Endit provider plugin stores hardlinks to files to be flushed to tape(s)

- _write.request.dir.path=/export/GridKatest/f01-129-131-e_wT_tst/data/request_  //Path to the directory where the dCache Endit provider plugin stores metadata files about files to be flushed to tape(s)

- _write.bash.script=/usr/share/endit-tss/script.sh_  //Path to the shell script. It should be located in the directory where Endit-TSS is installed, i.e. /usr/share/endit-tss/

- _read.in.dir.path=/export/GridKatest/f01-129-131-e_sT_tst/data/in_  //Path to the directory where staged files are temporarily stored.

- _read.request.dir.path=/export/GridKatest/f01-129-131-e_sT_tst/data/request_  //Path to the directory where the dCache Endit provider plugin stores metadata files about files to be staged from tapes(s). 

- _nr.active.requests=30000_  //The maximum number of active requests that are allowed to be sent/submitted to the TSS-client for both operations, stage and flush.

For more information about the software's features, please refer to the following article [1] or URL [2].

[1] Musheghyan, Haykuhi, et al. "The GridKa tape storage: latest improvements and current production setup." EPJ Web of Conferences. Vol. 251. EDP Sciences, 2021.
[2] https://www.epj-conferences.org/articles/epjconf/pdf/2021/05/epjconf_chep2021_02014.pdf
