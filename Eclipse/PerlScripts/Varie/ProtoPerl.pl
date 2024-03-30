#! d:\perl\bin\perl

if ($#ARGV < 1) {
    printf "Errore nei parametri\nuso: %s nomefile pat1 ... patn\n", $0;
    exit(-1);
}
$pat = "";
$fname = shift(@ARGV);
$OutFName = $fname . ".new";
$Bakfname = $fname . ".bak";

# system( "erase $Bakfname");


# tabella con le sostituzioni di nomi delle funzioni.
%replace = ( "GetNumberOfProperty" => "GetNumberOfProperty",
	     "NumMesiArch" => "GetNumMesiArch",
	     "GetRoot" => "GetRoot",
	     "GetName" => "GetName",
	     "GetPeriodicity" => "GetPeriodicity",
	     "GetAbsPeriodicity" => "GetAbsPeriodicity",
	     "GetDefault" => "GetDefault",
	     "GetAbsDefault" => "GetAbsDefault",
	     "GetTipoAgg" => "GetTipoAgg",
	     "GetType" => "GetType",
	     "GetNameEntFisic" => "GetEntFisName",
	     "GetPropertyAt" => "GetPropertyAt"
	     );


sub EmitFunction
{
    my( $p1, $p2, $p3, $p4, $p5) = @_;
    if ($p3 =~ /[A-Za-z][A-Za-z0-9]*/)
    {
	# la funzione conteneva un parametro 
	print ofh $p1, $p5, " ", $replace{ $pat}, "( $p2, $p3)", $p4;
    }
    else
    {
	# la funzione NON conteneva un parametri
	print ofh $p1, $p5, " ", $replace{ $pat}, "( $p2)", $p4;
    }
    $lo = 1;
}



open( fh, "<$fname") || die "non posso aprire il file input: $!";
open( ofh, ">$OutFName") || die "non posso aprire il file output: $!";

while (<fh>)
{
    $lo = 0;
    $gp = 0;
    
    foreach $pat (@ARGV)
    {
	# p1 = p2->patter( p3) p4)  ==> p1 pattern( p2, p3) p4
	if (/(.*)[\t ]*=[\t ]*([A-Za-z]+[a-zA-Z0-9_]*)[ ]*->[ ]*$pat\((.*)\)(.*)/)
	{
	    if (!defined($replace{ $pat}))
	    {
		print "errore replace pattern non definito: ", $pat, "\n";
	    }
	    else
	    {
		EmitFunction( $1, $2, $3, $4, "=");
	    }
	}
	# funzione a due parametri a inizio linea
	# ^p1->patter( p2) p3)  ==> pattern( p1, p2) p3
	elsif (/^([\t ]*)([A-Za-z]+[a-zA-Z0-9_]*)[ ]*->[ ]*$pat\((.*)\)(.*)/)
	{
	    if (!defined($replace{ $pat}))
	    {
		print "errore replace pattern non definito: ", $pat, "\n";
	    }
	    else
	    {
		EmitFunction( $1, $2, $3, $4, "");
	    }
	}
	# funzione a due parametri a inizio linea
	#  p1 p2->patter( p3) p4)  ==> p1 pattern( p2, p3) p4
	elsif (/^(.*[,\t ]+)([A-Za-z]+[a-zA-Z0-9_]*)[ ]*->[ ]*$pat\((.*)\)(.*)/)
	{
#	    print "replacing pattern: $pat (<$1>,<$2>,<$3>,<$4>)\n";
	    if (!defined($replace{ $pat}))
	    {
		print "errore replace pattern non definito: ", $pat, "\n";
	    }
	    else
	    {
		EmitFunction( $1, $2, $3, $4, "");
	    }
	}
    }
    if ($lo == 0)
    {
	print ofh $_;
	# print ofh "\n",$_,";
    }
    if ($lo == 1)
    {
   	print ofh "\n";
    }
}
close fh;
close ofh;

# system( "move $fname $Bakfname");
# system( "move $OutFName $fname");

