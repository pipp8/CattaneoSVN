#! /usr/bin/perl

$file='/tmp/backuplist.txt';
open(INFO, $file) or die("Could not open  file.");

$count = 0; 
$cmd = "tmutil";

foreach $line (<INFO>)  { 
	chomp($line);
	$line = '"' . $line . '"';  
    $count++;
    print $count, " -> ", $line, "\n";

	$result = `$cmd delete $line 2>&1`;
#    system( $cmd, "delete", $line);
#	if ( $? == -1 )
#	{
#	  print "command $cmd failed: $!\n";
#	}
#	else
#	{
	printf "command returned: %s", $result;
#	}     
}
close(INFO);
