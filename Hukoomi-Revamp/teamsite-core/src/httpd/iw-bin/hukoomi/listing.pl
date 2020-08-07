#!usr\opentext\TeamSite/iw-perl/bin/iwperl -w
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
my ($dcrName ,$dctType, $Xpath) = "";
$dcrName = $ARGV[0];
$dctType = $ARGV[1];
$Xpath = $ARGV[2];
# my $filepath = "//iwmnt//default//main//Hukoomi//WORKAREA//default//templatedata//Taxonomy//Persona//data//en//persona.xml";
my $filepath = "//iwmnt//default//main//Hukoomi//WORKAREA//default//templatedata//".$dctType."//data//".$dcrName;


my $pt1 = XML::XPath->new(filename => "$filepath");
# my $nodeset = $pt1->find("/Root/Persona");
my $nodeset = $pt1->find($Xpath);
print "<substitution>\n";
foreach my $node ($nodeset->get_nodelist) 
{
 
    my $CatLabelAr = $node->findvalue('./LabelAr');    
    my $CatLabelEn = $node->findvalue('./LabelEn');    
    my $CatValue = $node->findvalue('./Value');
	
	if($Xpath =~ /master-data\/Category/){
	
		my $nodeset2 = $node->find('./SubCategory');
				
		foreach my $node2 ($nodeset2->get_nodelist){
				
			my $SubCatLabelAr = $node2->findvalue('./LabelAr');			
			my $SubCatLabelEn = $node2->findvalue('./LabelEn');			
			my $SubCatValue = $node2->findvalue('./Value');
			
			print "<option label=\"".$CatLabelEn."(".$SubCatLabelEn.") | ".$CatLabelAr."(".$SubCatLabelAr.") \" value=\"".$CatValue."-".$SubCatValue."\"></option>\n";

		}
	
	# }elsif($Xpath =~ /master-data\/Key-Label/){
	}else{
		# $list=$list.",".$Value."||".$Label;
		print "<option label=\"".$CatLabelEn." | ".$CatLabelAr."\" value=\"".$CatValue."\"></option>\n";
	}
	   
  }
print "</substitution>\n";
