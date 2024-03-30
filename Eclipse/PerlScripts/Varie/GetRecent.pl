#! /usr/bin/perl


###########################################################
#
# script per:
#	download del listing completo dal sito $hostname
# 	ordinamento globale per data
#	confronto del risultato con un vecchio file archivio per mostrare solo $max righe nuove più recenti
#
# usage: GetRecent [-since 20 gen 2010] [-maxlines 100] [-ftype avi -ftype mpg -ftype xxxx] [-compare] [-makedb]
#
# $Header: /cvsroot/Cattaneo/Sources/PerlScripts/GetRecent.pl,v 1.8 2010/04/24 17:22:43 cattaneo Exp $
#
###########################################################

use Date::Parse;
use Date::Language;
use Time::Local;
use Getopt::Long;
use Cwd;

sub MySplice
{
	my ($index, @array) = @_;
	my $retVal = "";
	my $i;
	for($i = $index; $i <= $#array; $i++) {
		$retVal = $retVal . $array[$i];
	}
	return $retVal;
}

sub trim($)
{
	my $string = shift;
	$string =~ s/\s+//;
#	$string =~ s/\s+$//;
	return $string;
}

  

$programName = $ARGV[0];
$lang = Date::Language->new('Italian');

$max = 100;					# massimo numero di linee processate in output	
$strEpoc = "01 gen 2000";	# data di default di partenza

if (!GetOptions ('maxlines=s' => \$max,
			'since=s' => \$strEpoc,
			'ftype=s' => \@ftypes,
			'compare' => \$delta,
			'makedb' => \$makedb)) {
	printf("Errore nei parametri.\nUsage: %s [-since 20 gen 2010] [-maxlines 100] [-ftype avi -ftype mpg -ftype xxxx] [-compare] [-makedb]\n", $programName);
	die("restart");
}

$sinceDate = $lang->str2time($strEpoc, "CEST");
unless (defined $sinceDate)  { die "Can't parse '$strEpoc' as a date\n" }

if (@ftypes > 0) {
	$extPattern = $ftypes[0];
	foreach $p (1 .. $#ftypes) {
		$extPattern = "$extPattern|$ftypes[$p]";
	}
	$extPattern = "\.$extPattern\$";	
} 
else {
	undef $extPattern;
}


$dir = getcwd;

@curTime = gmtime time;

$ctSuffix = sprintf("%04d-%02d-%02d", 1900+$curTime[5], $curTime[4]+1, $curTime[3]);
$FName = "all-$ctSuffix.txt";
$PrevList = "PrevList-100.txt";
$outFName = sprintf("top-%03d-%s.txt", $max, $ctSuffix);

printf("Saving Data in: %s (%d linee)\n", $outFName, $max);

$cmd = "ftp ftp://ciccio:poseidone\@192.133.28.17 << %%%EOF%%% > /dev/null 2>&1
ls -lR $FName
y
close
quit
%%%EOF%%%";

system($cmd);

open(FILE, $FName) or die "Can't open \'$FName\': $!";

my @sorted=();
my @line=();
my $LDate;

# sample lines from dir command
# drw-rw-rw-   1 user     group           0 Oct 22  2008 Sasa
# -rw-rw-rw-   1 user     group     3985974 Mar  7  2008 Suoneria_bella_topolona.wav
# -rw-rw-rw-   1 user     group    726988800 Apr  7  2004 Colazione Da Tiffany [Cineteca - 1961 - A.Hepburn DvdRip by BLB].avi
# /e:/Ataru Moroboshi:
# total 0

while (<FILE>) {
	if (/^\-/) {
		@line = split;
		
		if (scalar( @line) < 9) {
			next;
		}
		
		if (defined($extPattern)) {
			if (! /$extPattern/) {
#				print "ext $line[$#line] rejected\n";
				next;
			}
		}
		
		$LDate = join(' ', @line[5 .. 7]);
		$curDate = str2time($LDate);
		$strDate = localtime( $curDate);
		
		if ($curDate >= $sinceDate) {
			$fileName = join( ' ', @line[8 .. $#line]); 
			$dir{$fileName} = $curDir;
			push( @sorted, "$curDate/$strDate/$fileName");
			#	print( "$LDate -> $curDate,  ($strDate), \'$fileName\': \"$dir{$fileName}\"\n");		
		}
	}
	else {
		if (/\/e\:\//) {
			$curDir = $_;
			chop( $curDir);	
		}
	}
}

$cnt = 0;

if ($makedb) {
	$outFName = $PrevList;	
}

if ($delta || $makedb) {
	open(OUTF, ">$outFName") or die "Can't open out file \'$outFName\': $!";
   	open(STDOUT, ">&OUTF");
}

foreach $a (reverse sort( @sorted)) {
	@fname = split( /\//, $a);
	print "$fname[1] - $fname[2] -> $dir{$fname[2]}\n";
	if ($cnt++ > $max) {
		last;
	}
}
close OUTF;
close FILE;
unlink $FName; 

if ($makedb) {
	# crea solo il file 
	exit(0);
}


if ($delta) {
	if (! -e $PrevList) {
		printf(STDERR "Non esiste il file history (%s)\n", $PrevList);
		printf(STDERR "Puo' essere creato per usi successivi con il comando:\n");
		printf(STDERR "%s -makedb\n", $0);
		exit(1);
	}
	
	printf(STDERR "Comparing %s vs %s:\n\n", $PrevList, $outFName);
	$status = system("diff -e $PrevList $outFName");
	
	if ($status == 0) {
		printf(STDERR "Non ci sono state modifiche alla base dati\n");
	}
}
exit(0);
