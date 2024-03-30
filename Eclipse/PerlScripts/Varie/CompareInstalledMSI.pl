#! /usr/bin/perl

use File::Basename;

$referencedFiles='versions.txt';
$installerFiles='installer.dir';

my %ref;

open(INFO, $referencedFiles) or die("Could not open  file.");
$cnt = 0;
foreach $line (<INFO>)  { 
	$cnt++;
	chomp( $line);
	@a = split( ',', $line);
	
	$a[2] =~ tr/\\/\//;	# cambia gli slash
	$a[2] =~ s/[Cc]://;	# toglie C:
	$a[2] =~ s/\r//;	# toglie il cr
	
	$b = basename($a[2]);
	$ref{$b} = 1;
#    printf("line: %d Location: %s (%d)\n", $cnt, $b, $ref{$b});
}
close(INFO);
printf("%d keys loaded\n", $cnt);


open( DIRS, $installerFiles) or die("Could not open  file.");
$tbr = 0;
foreach $b (<DIRS>)  { 
	$cnt++;
	chomp( $b);
	$b =~ s/\r//;	# toglie il cr
	
	if (!exists ($ref{$b})) {
		$tbr++;
	    printf("[%d] package %s can be removed\n", $tbr, $b);	
	}

}
close(DIRS);
