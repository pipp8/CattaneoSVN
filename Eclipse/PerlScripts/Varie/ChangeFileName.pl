#!/usr/bin/perl

use POSIX;


# use File;

chdir("/Users/pipp8/Documents/DocumentiWindows/Casa/Mediolanum");

opendir(DIR, ".");
@files = readdir(DIR);
closedir(DIR);

foreach $file (@files) {
	@fnInfo = split(/_/, $file);
	$_ = $file;
#	if (/BM-\sEC(.*)\.pdf/) {
#		$old = $file;
#		$new = "BM-EC" . $1 . ".pdf";
#		@args = ("mv", $old, $new );
#		print( "$file: old = $old -> new = $new\n");
#		# rename "$file", "BM-EC" . $1 . ".pdf";
#		system (@args) == 0
#			or die "system @args failed: $?";
#	}
	if (/(.*)([0-9]{2})-([0-9]{2})-([0-9]{4})\.pdf/) {
		$old = $file;
#		print( "$file -> pre:$1 day: $2 month: $3 year: $4\n");
		$new = $1 . $4 . "-" . $3 . "-" . $2 . ".pdf";
		print( "$file: old = $old -> new = $new\n");
		rename $old, $new or die "Rename failed";
		
#		@args = ("mv", $old, $new );
#		system (@args) == 0
#			or die "system @args failed: $?";	
	}
}
