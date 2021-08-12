#!/usr/opentext/TeamSite/iw-perl/bin/iwperl -w
# 
# Set Extended Attributes to DCR XML files.
#
# Version 1.0-Beta
#
# Created By: Jatin Saraiya | OpenText Corp. Singapore
#
# Syntax: {PERL} ApplyExtAttr.pl "{PATH}"
#
# Example:  /usr/opentext/TeamSite/iw-perl/bin/iwperl ApplyExtAttr.pl "/default/main/sc-espresso/retail/bn/WORKAREA/bn/templatedata/scb_templates/48b_the_good_life_pintiles/data/credit-cards/the-good-life-privileges-new"
#############################################################################################################################################

use Cwd;
use Log::Log4perl qw(get_logger :levels);
use Time::HiRes qw(gettimeofday);
use TeamSite::CGI_lite;
use Sys::Hostname;

my $iwmount = TeamSite::Config::iwgetmount();
Log::Log4perl->init("/usr/opentext/TeamSite/httpd/iw-bin/hukoomi/config/HukoomiPerlLog.conf");
my $LOGGER= get_logger("setextattr");

$LOGGER->info("Start");

my $vpath = '';
my $logdir = '/usr/opentext/TeamSite/local/logs/scripts/';
my $timestamp = int (gettimeofday * 1000);
my $datestring = localtime();
my $existingResult = '';
my $teamsite_bin = "/usr/opentext/TeamSite/bin";
my $host = hostname;
$LOGGER->debug("[DEBUG] TimeStamp: $timestamp");

# $find = $ARGV[1];
# $replace = $ARGV[2];
$vpath = $ARGV[0];

my $filepre = "dcr_list_";

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

# /apps/data/iwmnt/default/main/sc-espresso/retail/hk/WORKAREA/hk/templatedata/scb_templates/48b_the_good_life_pintiles/data/credit-cards/the-good-life-privileges-new/111712hk

my @dcrtypeHierarchyTemplateData = split('/templatedata/',$vpath);
my @dcrtypeHierarchyData = split('/data',$dcrtypeHierarchyTemplateData[1]);
my $dcrType = $dcrtypeHierarchyData[0];

$LOGGER->debug("[DEBUG] DCR Type to be set: ".$dcrType);

my $ownername = "iwui";
my $groupname = "iwEveryone";
my $unixPerm = "0666";

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
    my $arDCR = $line =~ s/\/en\//\/ar\//gr;
    my $enDCR = $line =~ s/\/ar\//\/en\//gr; 

    print "\nCorresponding Arabic language DCR: ".$arDCR; 
    print "\nCorresponding English language DCR: ".$enDCR; 
chomp ($line); 
# Set Attribute for DCR Type
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Templating/DCR/Type=$dcrType\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set Attribute for valid DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"iw_form_valid=true\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Check if the DCR was created earlier
$cmdSetAttribute = "$teamsite_bin/iwextattr -g \"TeamSite/Metadata/createdOn\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
my $getCreatedDateValue = `$cmdSetAttribute`;

if($getCreatedDateValue eq "EA TeamSite/Metadata/createdOn not found*") {
	$LOGGER->debug("[DEBUG] DCR is newly created");
	print "DCR is newly created";
	# Set Attribute for valid DCR
	$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/createdOn=$datestring\" \"$line\";";
	$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
	`$cmdSetAttribute`;
}

# Set Locale Flag for valid DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/isLocalised=true\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set Master Flag for valid DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/isMaster=true\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set Locale DCR Path for valid DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/localDCR=//$host$arDCR\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set Master DCR path for valid DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/masterDCR=//$host$enDCR\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set Modified Date for DCR
$cmdSetAttribute = "$teamsite_bin/iwextattr -s \"TeamSite/Metadata/modifiedOn=$datestring\" \"$line\";";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set valid group to DCR
$cmdSetAttribute = "$teamsite_bin/iwaccess set-group \"$line\" $groupname;";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set valid owner to DCR
$cmdSetAttribute = "$teamsite_bin/iwaccess set-owner \"$line\" $ownername;";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;

# Set valid unix permissions to DCR
$cmdSetAttribute = "$teamsite_bin/iwaccess set-unix-permission \"$line\" $unixPerm;";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdSetAttribute."\n");
`$cmdSetAttribute`;
}
print "\nList of modified DCR files can be found in '$filename'\n";
close(DATA);
$LOGGER->info("Successful End of Script");
