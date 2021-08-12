#!/usr/opentext/TeamSite/iw-perl/bin/iwperl -w
use strict;
use warnings;
use XML::XPath;
use XML::XPath::XMLParser;
use TeamSite::Config;
use TeamSite::CGI_lite;
use XML::Simple;
use Data::Dumper;
use File::Find;
use File::Basename;

my @array="";
my ($var ,$dctCategoryPath) = "";
$dctCategoryPath = $ARGV[0];
my $filepath = $dctCategoryPath;
@array = `ls $filepath`;
print "<substitution>\n";
foreach(sort @array){
	$var = $_;
	chomp $var;
	print "<option label=\"".$var."\" value=\"".$var."\"></option>\n";
}
print "</substitution>\n";
