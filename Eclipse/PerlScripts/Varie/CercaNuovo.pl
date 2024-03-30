#! perl
use POSIX;
# use File;

$TopDir = "/tmp/";
$LocalRoot = "/cygdrive/C/Attivita/";
$RemoteRoot = "/cygdrive/U/Personal/";

$DiffOut = $TopDir . "personal.diff";
$SoloSuUdsab = $TopDir . "solo_su_nettuno.txt";
$MergeFile = $TopDir . "diversi.txt";
$AllSuMob = $TopDir . "allFilesMob.txt";

$OutFile = $TopDir . "report.txt";

system ("diff -r --brief  " . $RemoteRoot . "Universita/  " . $LocalRoot . "Universita  > " . $DiffOut);

system ("fgrep Only " . $DiffOut . " | fgrep /U/ > " . $SoloSuUdsab);
system ("fgrep differ " . $DiffOut . " > " . $MergeFile);
system ("cd " . $LocalRoot . " ; find . -print > " . $AllSuMob);


open( InFile, "<$MergeFile") || die ("Non trovo il file input: $InFile");
open( OutFile, ">$OutFile") || die ("Non posso aprire risultati: $OutFile") ;

$moved=0;
printf( OutFile "stage1: Moving newer file from %s@udsab\n", $MergeFile); 
while( <InFile>)
  {	
    @files = split();
    @udsab = stat( $files[1]);
    @mob = stat( $files[3]);
    if ($udsab[9] > $mob[9])
      {
	$dt1 = POSIX::ctime($udsab[9]);
	$dt2 = POSIX::ctime($mob[9]);
	chomp( $dt1);
	chomp( $dt2);
	printf( OutFile "file %s on udsab newer: %s vs. %s moving ...\n", 
		$files[1],$dt1, $dt2 );
	$cmd = "mv " . $files[3] . "  " . $files[3] . ".bak";
	system( $cmd); # save previous file
	$cmd = "mv " . $files[1] . "  " . $files[3];
	system( $cmd);
	printf(OutFile "%s\n", $cmd);
	$moved++;	
      }
    else 
      {
	printf(OutFile "%s is older ... leaving in place on udsab\n", $file[1])
      }
  }
close( InFile);
printf(OutFile "%d file moved\n", $moved );

open( InFile, "<$AllSuMob") || die ("Non trovo il file input: $AllSuMob");

printf(OutFile "\n\nstage2: Loading file data base ... ");
$cnt = 0;
while( <InFile>)
  {
    chomp();
    # $file = File::Basename($_);
    $path = $_;
    $pos = rindex( $path, "/");
    if ($pos >= 0)
      {
	$file = substr($path, $pos + 1);
	$old{$file} = substr( $path, 2); # salta i trailing ./ (cf. find)
	# print( $file, " added\n");
	$cnt++;
      }
  }
close( InFile);
printf(OutFile "%d files loaded\n", $cnt);


open( InFile, "<$SoloSuUdsab") || die ("Non trovo il file input: $SoloSuUdsab");
printf( OutFile "\n\nstage3: Moving newer file from %s@udsab\n", $SoloSuUdsab); 
$moved = 0;
while( <InFile>)
  {
    @files = split();
    chop($files[2]);
    $OldDir = $files[2];
    $NewDir = $OldDir;
    $FileName = $files[3];
    if ( ! exists( $old{ $FileName}))
      {
	$NewDir =~ s($RemoteRoot)[$LocalRoot];
	$cmd = "mv " . $OldDir . "/" . $FileName . "  " . $NewDir . "/" . $FileName;
	printf(OutFile "Moving (solo su udsab) Cmd = %s\n", $cmd);
        system ($cmd);
	$moved++;
      }
    else
      {
	$f1 = $LocalRoot . $old{$Filename};
	$f2 = $OldDir . $FileName;
	@mob = stat( $f1);
	@udsab = stat( $f2);
	if ($udsab[9] > $mob[9])
	  {
	    printf( OutFile "Moving File: %s already existing but newer than local file\n", $FileName);
	    printf(OutFile "udsab:  %s - %s", $f2, POSIX::ctime($udsab[9]));
	    printf(OutFile "mob007: %s - %s", $f1, POSIX::ctime($mob[9]));
	    $cmd = "mv " . $f2 . "  " . $f1;
	    printf( OutFile "Moving (exiting in other dir, but older ) Cmd: %s\n", $cmd);
	    system( $cmd);
	    $moved++;
	   }
	else
	  {
	    printf(OutFile "File: %s already existing (other dir)  and newer(leaving)\n", $FileName);
	  }
      }
  }
close( InFile);
printf(OutFile "%d file moved\n", $moved );
close( OutFile);

	    
 
