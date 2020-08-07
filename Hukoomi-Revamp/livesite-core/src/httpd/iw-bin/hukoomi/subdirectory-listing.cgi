#!usr\opentext\TeamSite/iw-perl/bin/iwperl -w

use strict;
use warnings;
use CGI;
use XML::XPath;
use XML::XPath::XMLParser;
use TeamSite::Config;
use TeamSite::CGI_lite;
use XML::Simple;
use Data::Dumper;
use File::Find;
use File::Basename;

$| = 1;
# my $cgi = TeamSite::CGI_lite->new();
# $cgi->parse_data();
print "Content-Type: text/html\n\n";

my $list="";
my $time="";
my $nodeXpath="";
my $listNode="";
$time=localtime();
my $iwmount = TeamSite::Config::iwgetmount();
my $in = new CGI;
$nodeXpath = $in->param('nodeXpath');  
$listNode = $in->param('listNode');     
my $filepath = "//iwmnt//default//main//Hukoomi//WORKAREA//default//templatedata//Taxonomy//Persona//data//en//persona.xml";

print "Content-Type: text/html\n\n";
print <<END;
	<html><head><script>
	var obj =parent.getScriptFrame();
	obj.alert("test");
	//obj.setList();
	</script><body></body></head></html>

END

my $scr_name= "listing.log";
open LOGOUT, ">>$scr_name" or die("cant open file for logging !!");
print LOGOUT "********************************subdirectory-listing.cgi*********************************************\n";
print LOGOUT "time : $time\n";

my $pt1 = XML::XPath->new(filename => "$filepath");
my $nodeset = $pt1->find($nodeXpath);

foreach my $node ($nodeset->get_nodelist) 
{
  
   my $Label = $node->findvalue('./Label');
   my $Value = $node->findvalue('./Value');
   
   $list=$list.",".$Value."||".$Label;
   print LOGOUT "list : $list\n";
   
}

print "Content-Type: text/html\n\n";
print <<END;
	<html><head><script>
	var obj =parent.getScriptFrame();
	obj.IWDatacapture.getItem("$listNode").setValue("$list");
	//obj.setList();
	</script><body></body></head></html>

END

close LOGOUT;
exit;




