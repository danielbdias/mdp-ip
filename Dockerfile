FROM openjdk

# needed to run ampl and minos
RUN yum install -y glibc.i686

COPY libs/linux/ampl /usr/sbin
COPY libs/linux/lrs /usr/sbin
COPY libs/linux/minos /usr/sbin