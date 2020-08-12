#!E:\Interwoven\TeamSite/iw-perl/bin/iwperl -w
################################################################################################################
#	Description: Callserver script for Product Categories selection for Details Display DCT
#	Creator: OpenText Pro Services, 2017
#	Company: Maybank
#
################################################################################################################
use CGI::Carp qw(fatalsToBrowser);
use CGI;
use strict;
use File::Find;
use TeamSite::Config;
use File::Path qw(mkpath);


(my $iwmount=TeamSite::Config::iwgetmount()) =~ tr|\\|/|;
my $dcrPath="";
my $time="";
$time=localtime();
my $dcrName="";
my $branch="";
my $result="";
my $final="";
my $in = new CGI;
$dcrPath = $in->param('folderpath');  
$dcrPath=~ s#//[^/]*##g;
$dcrPath=~ s#datacapture\.cfg#data#g;
$dcrPath= $iwmount.$dcrPath."/";
my $scr_name= "listing.log";
open LOGOUT, ">>$scr_name" or die("cant open file for logging !!");
print LOGOUT "********************************deleteDCR.ipl*********************************************\n";
print LOGOUT "Time : $time \n";
print LOGOUT "dcrPath : $dcrPath \n";
print LOGOUT "iwmount : $iwmount \n";

if(!(-d $dcrPath)){
	print LOGOUT "$dcrPath directory path does not exists !!\n";
	print LOGOUT "mkdir $dcrPath !!\n";
	mkpath($dcrPath) or die "error : $!";
	print LOGOUT "cmd : mkdir $dcrPath :\n";
	print LOGOUT "$dcrPath directory created !!\n";
}
#return $dcrPath;

exit();
