use File::Find;
use Digest::MD5 qw(md5 md5_hex md5_base64);
use Getopt::Long;
use Data::Dump qw(dump);
use strict;



sub bySize
{
	($a->{'size'} * $a->{'len'}) <=> ($b->{'size'} * $b->{'len'}); 
}






my $minSize = 4000;			# no file less than 4 Kb
my $maxSize = 5000000000;	# no file greater than 5Gb
my $verbose = '1';
my $StartDir = ".";	


GetOptions('minSize=i' => \$minSize,
			'verbose' => \$verbose);

print @ARGV, "\n";

if ($#ARGV >= 0) {
	$StartDir = $ARGV[0];
}
else {
	$StartDir = ".";	
} 

printf( "Start Directory: %s,  minSize: %d, maxSize: %d, verbose = %s\n",
				$StartDir, $minSize, $maxSize, $verbose) if ($verbose);

my $filename = 'report.txt';
open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";
print $fh "Cerca duplicati:\n";    
printf( $fh "Start Directory: %s,  minSize: %d, maxSize: %d, verbose = %s\n",
				$StartDir, $minSize, $maxSize, $verbose) if ($verbose);
    
my %hashList = ();
my $totDups = 0;
my $files = 0;

$| = 1;

find({wanted => \&ProcessName,
      no_chdir => 1}, $StartDir);
      
printf("Found %d duplicati su %d files\n", $totDups, $files);
printf($fh "Found %d duplicati su %d files\n", $totDups, $files);

my ($tot, $fDups) = (0 , 0);
my $k = '';

my @sorted = sort bySize values( %hashList);

foreach $k ( @sorted) {
		
	my @p = @{$k->{'paths'}};
	
	if ($k->{'len'} > 1) {
		$fDups++;
		printf($fh "size: %d, len: %d ->\n\t", $k->{'size'}, $k->{'len'});
		$tot += $k->{'len'};

		print $fh join("\n\t", @p), "\n";
	}
}
printf( $fh "Totale duplicati = %d (%d), in = %d files, su = %d files totali\n", $tot, $totDups, $fDups, $files);
close $fh;
print "done\n";

# my @sorted = sort bySize values %dupFiles;

exit;



sub ProcessName
{
	my $name = $_;
		
	if (-f $name ) {
		$files++;
		if ($verbose) {
			print "." if ($files % 1000 == 0);
		}

		my $size = -s $name;

		if ($minSize > 0 && $size < $minSize ||
		    $maxSize > 0 && $size > $maxSize) { 
#		    	printf("Skipping file: %s -> size = %d\n", $name, $size);
		}
		else {
			open (my $fh, '<', $name) or die "Can't open '$name': $!";
			binmode ($fh);
			my $key = Digest::MD5->new->addfile($fh)->hexdigest;
			close( $fh);
#			print "Processing file: $name\n";
		
			if (exists $hashList{ $key}) {
				
				my $rec = $hashList{$key};		# riferimento al record 
				my @names = @{$rec->{'paths'}}; # dereferenzia il riferimento all'array paths
#				printf( "Found duplicate n. %d : %s\n", $rec->{'len'} + 1, $name);
				
				push( @names, $name);			# aggiunge la nuova path				
#				print join("\n\t", @names), "\n";
						
				$rec->{'paths'} = \@names;		# riaggiorna il riferimento all'array
				$rec->{'len'} = $rec->{'len'} + 1;
				$hashList{$key} = $rec;			# riaggiorna il riferimento al record
				
				$totDups++;
			}
			else {
				my @names = ();
				push( @names, $name);					
				my %rec = ( 'size' => $size, 'len' => 1, 'paths' => \@names); # salva il riferimento all'array con un solo elemento				
 				$hashList{$key} = \%rec;					}
		}
	}
#		 
#    print "$name is dir\n" if -d ;
}