#!/usr/opentext/TeamSite/iw-perl/bin/iwperl -w
# 
# Set Group Name to DCR XML files.
#
# Version 1.0-Beta
#
# Created By: Prince Sharma | Hayagreeva Consulting
#
# Syntax: {PERL} setDcrGroup.pl "{PATH}"
#
# Example:  /usr/opentext/TeamSite/iw-perl/bin/iwperl setDcrGroup.pl "/default/main/sc-espresso/retail/bn/WORKAREA/bn/templatedata/scb_templates/48b_the_good_life_pintiles/data/credit-cards/the-good-life-privileges-new"
#############################################################################################################################################

use Cwd;
use Log::Log4perl qw(get_logger :levels);
use Time::HiRes qw(gettimeofday);
use TeamSite::CGI_lite;
use Sys::Hostname;

my $iwmount = TeamSite::Config::iwgetmount();
Log::Log4perl->init("/usr/opentext/TeamSite/httpd/iw-bin/hukoomi/config/HukoomiPerlLog.conf");
##my $LOGGER= get_logger("setDcrGroupName");
my $LOGGER= get_logger("setextattr");

$LOGGER->info("Start");

my $vpath = '';
my $logdir = '/usr/opentext/TeamSite/local/logs/scripts/';
my $timestamp = int (gettimeofday * 1000);
my $datestring = localtime();
my $existingResult = '';
my $teamsite_bin = "/usr/opentext/TeamSite/bin";
$LOGGER->debug("[DEBUG] TimeStamp: $timestamp");

$vpath = $ARGV[0];

my $filepre = "dcr_list_group_";

if ( defined $vpath ){
$LOGGER->debug("[DEBUG] Path for operation: $vpath");
if($vpath eq '.'){
$LOGGER->info("[INFO] Changing Path of operation to current directory");
$vpath = getcwd();
$LOGGER->debug("[DEBUG] Operation Directory changed to: $vpath");
}
} else {
$LOGGER->error("[ERROR] NO PATH FOUND TO EXECUTE SetExtAttr: ABORTING\n");
exit(0);
}

my $groupname = "";

my $cmd = "find \"".$vpath."\" -type f";
my $filename=$logdir.$filepre.$timestamp.".txt";
$LOGGER->debug("[DEBUG] Command to Run Find for DCRs: : $cmd");

my $result = `$cmd`;
print "$result";
my $toFile = `$cmd > $filename`;
$LOGGER->debug("[DEBUG] Results stored in : $filename");
print "Result : $result";

open(DATA, "< $filename") or die "Couldn't open file '$filename', $!";

foreach $line (<DATA>)  {   
    print "\nProcessing....".$line;  
    ##my $arDCR = $line =~ s/\/en\//\/ar\//gr;
    ##my $enDCR = $line =~ s/\/ar\//\/en\//gr; 

    ##print "\nCorresponding Arabic language DCR: ".$arDCR; 
    ##print "\nCorresponding English language DCR: ".$enDCR; 

	chomp ($line); 
	open (DCR, "< $line") or die "Couldn't open file '$line', $!";

	foreach $dcrLine (<DCR>) {
		chomp($dcrLine);
		if (index($dcrLine, "<service-entities>") != -1) {
		$dcrLine  =~ m/(.*)<service-entities><service-entities>(.*)<\/service-entities><value>(.*)<\/value><label-en>(.*)<\/label-en><label-ar>(.*)<\/label-ar><\/service-entities>(.*)/;
			if($4 ne ""){
				print "Four: ".$3."\n";
				$groupname = "hukoomi-".$3;
				$LOGGER->debug("[DEBUG] groupname: ".$groupname."\n");

				# Set valid group to DCR
				$cmdSetAttribute = "$teamsite_bin/iwaccess set-group \"$line\" \"$groupname\"";
				$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
				`$cmdSetAttribute`;
			}
		}
	}
	close(DCR);
}
print "\nList of modified DCR files can be found in '$filename'\n";
close(DATA);
$LOGGER->info("Successful End of Script");
