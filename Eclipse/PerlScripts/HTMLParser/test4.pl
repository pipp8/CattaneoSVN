

@dirs = split(";", $ENV{PATH});

foreach $d (@dirs) {
	print $d, "\n";
} 

