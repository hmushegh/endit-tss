%global ver 2
%global dist 5

Summary:    ENDIT TSS Backend for Tape
Name:       endit-tss
Version:    %( echo %ver.1 | tr '-' '.' )
Release:    %{?dist}
URL:        https://git.scc.kit.edu/wl4565/endit-tss/-/tree/endit-tss-v2
License:    None
Group:      System Environment/Daemons

BuildRoot:  %{_tmppath}/%{name}-%{version}-%{release}-root

Source0:    endit-tss-endit-tss-v%{ver}.tar.gz
Source1:    endit-tss.properties
Source2:    endit-tss@.service
Source3:    endit-tss.service-environment

Requires:   java


%description
ENDIT TSS Backend for Tape, works for Java 13 and Java 14 versions.


%prep
%setup -q -n endit-tss-endit-tss-v%{ver}
pwd
ls -la


%build
echo "Using pre-packages jar file"
ls -la target/endit-tss-2.0.0.jar



%install
rm -rf %{buildroot}
install -m644 -D script.sh %{buildroot}/usr/share/endit-tss/script.sh
install -m644 -D target/endit-tss-2.0.0.jar %{buildroot}/usr/share/endit-tss/endit-tss.jar
install -d %{buildroot}/usr/share/endit-tss/dependency-jars
install -D -t %{buildroot}/usr/share/endit-tss/dependency-jars/ target/dependency-jars/*
install -d %{buildroot}/var/log/endit-tss
install -m644 -D %{SOURCE1} %{buildroot}/etc/endit-tss.properties
install -m644 -D %{SOURCE2} %{buildroot}/usr/lib/systemd/system/endit-tss@.service


%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%doc README.md
/usr/share/endit-tss
%attr(640,scc-dcache-0001,scc-dcache-0001) %config(noreplace) /etc/endit-tss.properties
/usr/lib/systemd/system/endit-tss@.service
%dir %attr(750,scc-dcache-0001,scc-dcache-0001) /var/log/endit-tss

