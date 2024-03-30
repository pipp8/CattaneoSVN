#! /usr/bin/perl

use Digest::MD4;
use File::Find;


$ctx = Digest::MD4->new;

$dupcount = 0;
if ($#ARGV == 0) {
	$TopDir = $ARGV[0];
}
else {
	$TopDir = ".";
}

print ("TopDir = ", $TopDir, "\n");
$processed = 0;

find(\&wanted, $TopDir);

if ($dupcount <= 0) {
  print("Non ci sono file duplicati\n");
}
else {
  printf("%d / %d file duplicati\n", $dupcount, $processed);
}
print("fine\n");

sub doitbis {
  $fname = $File::Find::name;
  if (length( $fname) == 1) {
	print $fname, "\n";
  }
}


sub doit {
  $fname = $File::Find::name;
#  print $fname, "\n";
  if (-f $fname) {
	  $processed++;
#	  if ($processed % 100 == 0) {
#	  	print ".";
#	  }
	  open(FILE, $fname) or die "Can't open '$fname': $!";
	  binmode(FILE);
	  $ctx->reset();
	  $hash = $ctx->addfile(*FILE)->hexdigest;
	  if (exists $ass{$hash}) {
	    @stat = stat $fname;
	    # $fname =~ s/$TopDir//g;
	    @names = $ass{$hash};
	    printf("%s\t(%d) -> ", $fname, $stat[7]);
		print join "\n", @names;
		print "\n";
		
	    push( @names, $fname);
	    $ass{$hash} = @names;
	    $dupcount++;
	  } 
	  else {
	    # $fname =~ s/$TopDir//g;
	    @names = ();
	    push( @names, $fname);
	    $ass{$hash} = $fname;
	  }
  }
  return 1;
}

sub wanted {
#  /\.JPG$/is && doit 
  /.+/is && doit 
}
