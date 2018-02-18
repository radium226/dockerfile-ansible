FROM ubuntu

RUN apt-get -y install curl \
    echo 'coucou'

COPY ["bim", "bamboum"]

COPY ["./bim.txt", "/bam/boum/bim/txt"]

RUN curl http://www.google.fr -O toto \
    mkdir -p bim \
    mkdir -p bamboum \
    apt-get -y install toto \
    apt-get -y install ton_cul \
    apt-get -y install toto

COPY ["shell-1ea8eb2.sh", "/tmp/shell-1ea8eb2.sh"]

RUN chmod +x /tmp/shell-1ea8eb2.sh \
    /tmp/shell-1ea8eb2.sh

CMD ["java", "-jar", "coucou"]