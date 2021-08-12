#!\usr\opentext\TeamSite\iw-perl\bin\iwperl -w
use strict;
use warnings;
use CGI;
use XML::XPath;
use XML::XPath::XMLParser;
use TeamSite::Config;
use TeamSite::CGI_lite;

my $time=localtime();
my $dcr = "";
my $in = new CGI;
$dcr = $in->param('dcrName');  
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




