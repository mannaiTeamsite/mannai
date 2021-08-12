#!usr\opentext\TeamSite/iw-perl/bin/iwperl -w
use strict;
use warnings;

my $time=localtime();
my $scr_name= "listing.log";
open LOGOUT, ">>$scr_name" or die("cant open file for logging !!");
print LOGOUT "********************************solr ingest*********************************************\n";
print LOGOUT "time : $time\n";
print LOGOUT "Core : portal-en";
print LOGOUT `curl -kvs http://localhost:8984/solr/portal-en/dataimport?command=full-import&clean=true&commit=true`;
print LOGOUT "Core : portal-ar";
print LOGOUT `curl -kvs http://localhost:8984/solr/portal-ar/dataimport?command=full-import&clean=true&commit=true`;
print LOGOUT "Core : media";
print LOGOUT `curl -kvs http://localhost:8984/solr/media/dataimport?command=full-import&clean=true&commit=true`;
print LOGOUT "Core : media-ar";
print LOGOUT `curl -kvs http://localhost:8984/solr/media-ar/dataimport?command=full-import&clean=true&commit=true`;

close LOGOUT;
exit;




