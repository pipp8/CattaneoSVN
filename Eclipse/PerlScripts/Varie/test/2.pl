#!/usr/bin/perl

$TmpFile = "/tmp/aa.out";
$DirFile = "/ClassDir.txt";
$JarFile = $ARGV[0];

system( "jar -tf " . $JarFile . " >> " . $TmpFile);

open( ifh, "<$TmpFile") || die "non posso aprire il file input: $!";
open( ofh, ">>$DirFile") || die "non posso aprire il file output: $!";


printf(ofh "\n-------------------------------------------------------\n");
while (<ifh>) {
  chomp( $_);
  printf(ofh "%s\t%s\n", $_, $JarFile);
}

close(ifh);
close(ofh);
unlink( $TmpFile);



