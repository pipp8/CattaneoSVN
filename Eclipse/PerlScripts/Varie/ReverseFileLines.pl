#! /usr/bin/perl

$file='/tmp/backuplist.txt';
open(INFO, $file) or die("Could not open  $file.");

$revFile=$file.'.reversed';
open(REV, ">$revFile") or die("Could not open $revFile.");

@lines = reverse <INFO>;

foreach $line (@lines) {
    print REV $line;
}
close(INFO);
close(REV);
