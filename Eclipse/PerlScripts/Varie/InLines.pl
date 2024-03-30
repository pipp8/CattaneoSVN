#! /usr/bin/perl

$file='/tmp/backuplist.txt';
open(INFO, $file) or die("Could not open  file.");

$count = 0; 
$cmd = "tmutil delete ";
$cmdLine = $cmd;
foreach $line (<INFO>)  { 
	chomp($line);
	$line = '"' . $line . '"';  
	$cmdLine = $cmdLine . " " . $line;
    	$count++;
	if ($count >= 10) {
    		print $count, " -> ", $cmdLine, "\n";
		system $cmdLine;
		$cmdLine = $cmd;
		$count = 0;
	}
}
close(INFO);
