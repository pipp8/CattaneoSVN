#!/usr/bin/perl

use Getopt::Long;

use POSIX;


# use File;

# chdir("/Users/pipp8/Documents/DocumentiWindows/Casa/Mediolanum");

my $delta = "";

my $changeBaseName = "";


  GetOptions ("renumber=i" => \$delta,   # numeric
              "baseName=s" => \$changeBaseName)   # string
  or die("Error in command line arguments\n");
  
if (!$delta && !$changeBaseName) {
	die "Errore negli argomenti, usage: RenumberFileName --renumber # | --basename newBaseName";
	die "errore";
}


$ext = ".jpg";

opendir(DIR, ".");
@files = readdir(DIR);
closedir(DIR);

if ($delta) {
	@revFiles = reverse @files;
	foreach $file  (@revFiles) {
		$_ = $file;
	
		if (/(.*)-([0-9]{4})$ext/) {
	
			$nn = $2 + $delta;
			$newName = sprintf("%s-%04d%s", $1, $nn, $ext);
			printf( "%s -> %s\n", $file, $newName);
	
			rename $file, $newName or die "Rename failed";
		}
	}
}
elsif ($changeBaseName) {
	foreach $file (@files) {
		$_ = $file;
	
		if (/(.*)-([0-9]{4})$ext/) {
	
			$newName = sprintf("%s-%04d%s", $changeBaseName, $2, $ext);
			printf( "%s -> %s\n", $file, $newName);
	
			rename $file, $newName or die "Rename failed";			
		}
	}
}
else {
	printf("No Change\n");
}
