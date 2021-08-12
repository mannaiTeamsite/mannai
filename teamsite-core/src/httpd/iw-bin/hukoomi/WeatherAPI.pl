#!/usr/opentext/TeamSite/iw-perl/bin/iwperl -w
# 
# Download and Deploy the Weather API Response 
# 
# Version 1.1
#
# Created By: Jatin Saraiya
#
# Syntax: {PERL} WeatherAPI.pl
#
# Example:  /usr/opentext/TeamSite/iw-perl/bin/iwperl WeatherAPI.pl
#########################################################################################################################################################################

use Cwd;
use Log::Log4perl qw(get_logger :levels);
use Time::HiRes qw(gettimeofday);
use TeamSite::CGI_lite;
my $iwgethome = TeamSite::Config::iwgethome();
Log::Log4perl->init( $iwgethome . "/httpd/iw-bin/hukoomi/config/HukoomiPerlLog.conf");
my $LOGGER= get_logger("WeatherAPI");
my $timestamp = int (gettimeofday * 1000);
$LOGGER->info("Weather API Fetch Triggerd on: " . $timestamp);

my $logdir = $iwgethome . '/local/logs/scripts/';
my $branch = "/default/main/Hukoomi";
my $json_output_path = $branch . "/WORKAREA/default/assets/json";
my $submit_cmd = $iwgethome . "/bin/iwsubmit";
my $deploy_cmd = "/usr/opentext/OpenDeployNG/bin/iwodcmd start custom-deployment -k \"area=/default/main/Hukoomi/WORKAREA/default\" -k \"fileList=/default/main/Hukoomi/WORKAREA/default/iw/config/properties/weather-filelist.txt\" -k \"definition=WebServerRuntimeDeployment\"";
$LOGGER->info("Deleting Existing Weather API content to avoid conflict of the files");
unlink $json_output_path . "/forecast.json";
unlink $json_output_path . "/current_weather.xml";
$cmd = "su iwui -c \"curl -A 'Hukoomi Server' -k https://qmet.com.qa/metservices/api/QMD/GetMeteoFactoryForecast --output $json_output_path/forecast.json\"";
$result = `$cmd`;
$LOGGER->info("Weather Forecast Downloaded " . $result ."\n");
$cmd = "su iwui -c \"curl -A 'Hukoomi Server' -k https://qweather.gov.qa/xml/EWSAll.xml --output $json_output_path/current_weather.xml\"";
$result = `$cmd`;
$LOGGER->info("Current Weather Forecast Downloaded " . $result ."\n");

$result = `$deploy_cmd`;
$LOGGER->info("Weather API Response Deployment to LiveSite Done. " . $result ."\n");

my $submit_comment = "Submitting Weather API Response " . $timestamp;

$cmd = $submit_cmd . " -x -u -r " . $json_output_path . " \"" . $submit_comment . "\"";
$result = `$cmd`;

$LOGGER->info("Sitemap Submit Post Generation Done. " . $result ."\n");
