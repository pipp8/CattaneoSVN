#! d:\perl\bin\perl

if ($#ARGV < 1) {
    printf "Errore nei parametri\nuso: %s nomefile pat1 ... patn\n", $0;
    exit(-1);
}
$pat = "";
$fname = shift(@ARGV);
$OutFName = $fname . ".new";
$Bakfname = $fname . ".bak";


open( fh, "<$fname") || die "non posso aprire il file input: $!";
open( ofh, ">$OutFName") || die "non posso aprire il file output: $!";

while (<fh>)
{
    $lo = 0;

    foreach $pat (@ARGV)
    {
	if (/(.*)[\t =]([A-Za-z]+[a-zA-Z0-9_]*)[ ]*->[ ]*$pat\([ ]*\)(.*)/)
	{
	    print ofh $1, " GetNumberOfProperty( $2)", $3;
	    $lo = 1;
	}
	elsif (/^([A-Za-z]+[a-zA-Z0-9_]*)[ ]*->[ ]*$pat\([ ]*\)(.*)/)
	{
	    print ofh $1, " GetNumberOfProperty( $2)", $3;
	    $lo = 1;
	}
    }
    if ($lo == 0)
    {
	print ofh $_,"\n";
    }
}
close fh;
close ofh;

system( "move $fname $Bakfname");
system( "move $OutFName $fname");
