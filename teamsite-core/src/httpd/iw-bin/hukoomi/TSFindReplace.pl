#!/usr/opentext/TeamSite/iw-perl/bin/iwperl -w
# 
# Find And Replace Utility - Designed for iwmnt (Virtual) Directory
# 
# Find and Replace with normal command clears content stored in the files of iwmnt Directory due to Virtual FileSystem
# To avoid this, run this utility to replace specific content stored in files. This File accepts string to find and replaces with given string.
# If no replace string is provided, only find command will be executed.
#
# Version 1.1
#
# Created By: Praveen Tarikere Indraiah
#
# Syntax: {PERL} TSFindReplace.pl "{PATH}" {FIND} {REPLACE}
#
# Example:  /usr/opentext/TeamSite/iw-perl/bin/iwperl TSFindReplace.pl "/iwmnt/default/main/iw/config/componentXSL/Page Header/" FindValue ReplaceValue
#########################################################################################################################################################

use Cwd;
use Log::Log4perl qw(get_logger :levels);
use Time::HiRes qw(gettimeofday);
use TeamSite::CGI_lite;
my $iwmount = TeamSite::Config::iwgetmount();
Log::Log4perl->init("/usr/opentext/TeamSite/httpd/iw-bin/hukoomi/config/HukoomiPerlLog.conf");
my $LOGGER= get_logger("safereplace");

$LOGGER->info("Start");

my $vpath = '';
my $find = '';
my $replace = '';
my $confirmation = '';
my $filetypes = '';
my $inclusion = '';
my $include = '';
my $logdir = '/usr/opentext/TeamSite/local/logs/scripts/';
my $timestamp = int (gettimeofday * 1000);
my $existingResult = '';
$LOGGER->debug("[DEBUG] TimeStamp: $timestamp");

$find = $ARGV[1];
$replace = $ARGV[2];
$vpath = $ARGV[0];

my $filepre = $find;
#$filepre =~ s/\//_/g;

if(defined $find) {
$LOGGER->debug("[DEBUG] String to Find: $find");
if(defined $replace){
$LOGGER->debug("[DEBUG] String to Find: $find");
if ( defined $vpath ){
$LOGGER->debug("[DEBUG] Path for operation: $vpath");
if($vpath eq '.'){
$LOGGER->info("[INFO] Changing Path of operation to current directory");
$vpath = getcwd();
$LOGGER->debug("[DEBUG] Operation Directory changed to: $vpath");
}
} else {
$LOGGER->error("[ERROR] NO PATH FOUND TO EXECUTE FIND/REPLACE: ABORTING\n");
exit(0);
}
} else {
if ( defined $vpath ){
$LOGGER->debug("[DEBUG] Path for operation: $vpath");
if($vpath eq '.'){
$LOGGER->info("[INFO] Changing Path of operation to current directory");
$vpath = getcwd();
$LOGGER->debug("[DEBUG] Operation Directory changed to: $vpath");
}
} else {
$LOGGER->error("[ERROR] NO PATH FOUND TO EXECUTE FIND/REPLACE: ABORTING\n");
exit(0);
}
}
} else {
$LOGGER->error("[ERROR] NO STRING FOUND TO EXECUTE FIND/REPLACE: ABORTING\n");
exit(0);
}

print "\n[DEBUG] Find Value: $find\n";
print "\nContinue Further ? (Y/N) ";
chomp($confirmation = <STDIN>);
if ($confirmation ne 'Y' && $confirmation ne 'y') {
$LOGGER->debug("[DEBUG] String to Find Declined: $find");
$LOGGER->info("[INFO] Aborted by USER");
exit(0);
}
$LOGGER->info("[INFO] String to Find Confirmed: $find");
if(defined $replace){
print "\n[DEBUG] Replace Value: $replace\n";
print "\nContinue Further (Y/N) ? ";
chomp($confirmation = <STDIN>);
if ($confirmation ne 'Y' && $confirmation ne 'y') {
$LOGGER->debug("[DEBUG] String to Replace Declined: $find");
$LOGGER->info("[INFO] Aborted by USER");
exit(0);
}
$LOGGER->info("[INFO] String to Replace Confirmed: $replace");

#if(index($vpath, $iwmount) == -1){
# $LOGGER->info("[INFO] File System is not IW Mount Directory");
# $LOGGER->info("[INFO] Applying SED command for Find Replace");
# my $cmd = "find \"".$vpath."\" -type f | sed 's/\"/\\\\\"/g;s/.*/\"&\"/' | xargs perl -pi -e 's!".$find."!".$replace."!g'";
# $LOGGER->info("[INFO] Command to run: $cmd");
# my $result = `$cmd`;
# print "$result";
# $LOGGER->debug("[DEBUG] Results of Replace : $result");
# print "Result : $result";
# $LOGGER->info("Successful End of Script");
# exit(0);
#}
print "\n[DEBUG] Replace from Existing Result File (Y/N) ? ";
chomp($confirmation = <STDIN>);
if ($confirmation eq 'Y' || $confirmation eq 'y') {
$LOGGER->info("[INFO] Replacement to be done from existing result file");
print "\n[DEBUG] Enter Name of the Resultfile (File Name only) ? ";
chomp($existingResult = <STDIN>);
my $cmdReplace; 
$existingResult="'/usr/opentext/TeamSite/local/logs/".$existingResult."'";
$LOGGER->info("[INFO] Result File: $existingResult.txt");
open(DATA, "< $existingResult.txt") or die "Couldn't open file error.txt, $!";

my $deletebak;

foreach $line (<DATA>)  {   
print "\nProcessing....".$line;   
$LOGGER->debug("[DEBUG] Processing File: ".$line);
chomp ($line); 
$cmdReplace = "sed -i.bak 's!$find!$replace!g' "."\"$line\"";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdReplace."\n");
`$cmdReplace`;
$deletebak="rm "."\"$line".".bak\"";
$LOGGER->debug("[DEBUG] BAK File to Delete:".$deletebak."\n");
`$deletebak`;
}
$LOGGER->info("[INFO] Replacement is done from existing result file: $existingResult.txt");
close(DATA);
$LOGGER->info("Successful End of Script");
exit(0);
}
}

print "\nDo you want to include file type in your search ? (Y/N) ";
chomp($include = <STDIN>);

if($include eq 'Y' || $include eq 'y') {
$LOGGER->info("[INFO] Want to add File Types");
print "\nEnter Extensions (without '.', Comma Separated: )";
chomp($filetypes = <STDIN>);
}
if($filetypes ne '') {
$LOGGER->debug("[DEBUG] File Types Added: $filetypes");
my @filetype = split(',',$filetypes);
foreach my $ft (@filetype) {
if ($inclusion ne '') {
$inclusion .= " -o ";
}
$inclusion .= " -name \"*.".$ft."\"";
}
}
my $cmd = "find \"".$vpath."\" -not -name \"*.log\" -not -name \"*.pdf\" -not -name \"*.png\" -not -name \"*.jpg\" -not -name \"*.gif\" -not -name \"*.txt\" -not -name \"*.jar\" -type f ".$inclusion." -print0 | "."xargs -0 grep -l '".$find."'";
my $filename=$logdir.$timestamp.".txt";
$LOGGER->debug("[DEBUG] Command to Run Find: : $cmd");

my $result = `$cmd`;
print "$result";
my $toFile = `$cmd > $filename`;
$LOGGER->debug("[DEBUG] Results stored in : $filename");
print "Result : $result";

if(not(defined $replace)) {
$LOGGER->debug("[DEBUG] Find Query Executed Successfully.");
$LOGGER->debug("[DEBUG] Search String : $find");
$LOGGER->debug("[DEBUG] Location : $vpath");
$LOGGER->debug("[DEBUG] Results stored in : $filename");
$LOGGER->info("[INFO] Results file may be use for future Replace");
$LOGGER->info("Successful End of Script");
print "\nList of found files can be found in $filename\n";
print "\nResults file may be use for future Replace\n";
exit(0);
}

my $cmdReplace; 

open(DATA, "< $filename") or die "Couldn't open file '$filename', $!";

my $deletebak;

foreach $line (<DATA>)  {   
    print "\nProcessing....".$line;   
chomp ($line); 
$cmdReplace = "sed -i.bak 's!$find!$replace!g' "."\"$line\"";
$LOGGER->debug("[DEBUG] CMD to RUN:".$cmdReplace."\n");
`$cmdReplace`;
$deletebak="rm "."\"$line".".bak\"";
$LOGGER->debug("[DEBUG] BAK File to Delete:".$deletebak."\n");
`$deletebak`;
my $cmdRemoveBlankLine = "sed -i '/^$/d' "."\"$line\"";
`$cmdRemoveBlankLine`;
print "\nRemoving Blank Lines: ".$cmdRemoveBlankLine;
}
print "\nList of modified files can be found in '$filename'\n";
close(DATA);
$LOGGER->info("Successful End of Script");
