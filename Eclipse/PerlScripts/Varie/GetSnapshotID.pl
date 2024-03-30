#! /usr/bin/perl

use File::Basename;
use Time::Piece;
use Getopt::Long;


my ($startDate, $endDate) =  ('', '');

GetOptions('start=s' =>  \$startDate, 'end=s' => \$endDate);
 
$startDate = Time::Piece->strptime($startDate, "%d/%m/%Y");
$endDate = Time::Piece->strptime($endDate, "%d/%m/%Y");
$minDate = Time::Piece->strptime("01/01/2000", "%d/%m/%Y");
$maxDate = Time::Piece->new();

if ($startDate < $minDate || $startDate > $maxDate ||
	$endDate < $minDate || $endDate > $maxDate ||
	$startDate > $endDate) {

		printf("Usage: %s --start DD/MM/YYYY --end DD/MM/YYYY");
		exit -1;
}
	else {
		printf("Selecting from: %s to: %s\n", $startDate, $endDate);
	}
	
	
my $referencedFiles='versionList.txt';
my $scriptFile = 'ddscript.txt';

open(INFO, $referencedFiles) or die("Could not open file: $referencedFiles.");

open(SCRIPT, ">$scriptFile") or die("Could not open file: $scriptFile.");

$cnt = 0;
$started=0;


foreach $line (<INFO>)  { 

	chomp( $line);
	$line =~ s/\r//;	# toglie il cr
	$d = $line;
	
	if ($started == 0) {
		# Backup time: 12/10/2014 02:30 
		if ($d =~ /Backup time\: (.*) $/) {
	
			$d2 = $1;
			my $t = Time::Piece->strptime($d2, "%d/%m/%Y %H:%M");
			
			if ($t > $startDate && $t <= $endDate) {
				
				printf("found: <%s> ->", $t);
				$started = 1;
			}	
		}	
	}
	else { 
		# ho già trovato una data valida
		# Snapshot ID: {26d7a379-dc69-4f74-9a97-8622ada5c34c}
		if ($d =~ /Snapshot ID: (\{.*\})$/) {
	
			$ss = $1;
				
			printf("<%s>\n", $ss);
			printf(SCRIPT "Delete shadows ID %s\r\n", $ss);
			$cnt++;
			$started = 0;	
		}
		
	}
}
printf("Trovati %d snapshots\n", $cnt);

close(INFO);
close(SCRIPT);
