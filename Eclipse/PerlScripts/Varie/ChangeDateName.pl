#!/usr/bin/perl -w

use POSIX;
use Switch;

# use File;


opendir(DIR, ".");
@files = readdir(DIR);
closedir(DIR);

foreach $file (@files) {
	@fnInfo = split(/_/, $file);
# formato input: "CEDOL" _ "mese" _ "anno" . "pdf"

	if ($fnInfo[0] eq "CEDOL") {
		switch ($fnInfo[1]) {
			case /gennaio/i		{ $month = "01" }
			case /febbraio/i	{ $month = "02" }
			case /marzo/i		{ $month = "03" }
			case /aprile/i		{ $month = "04" }
			case /maggio/i		{ $month = "05" }
			case /giugno/i		{ $month = "06" }
			case /luglio/i		{ $month = "07" }
			case /agosto/i		{ $month = "08" }
			case /settembre/i	{ $month = "09" }
			case /ottobre/i		{ $month = "10" }
			case /novembre/i	{ $month = "11" }
			case /dicembre/i	{ $month = "12" }
			else 			{ $month = "xxx"}
		}
		($anno, $ext) = split(/\./, $fnInfo[2]);
		if ($ext ne "pdf") {
			next;
		} 
		else {
			$ext = "." . $ext;
		}
		$newName = $fnInfo[0] . "-" . $anno . "-" . $month . $ ext;
		print "$file -> $newName\n";
		rename( $file, $newName);
	}
}
