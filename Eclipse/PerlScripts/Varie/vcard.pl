
$TopDir = "C:/tmp/";

$InFile = $TopDir . "Phonebook.vcf";
$OutFile = $TopDir;

@w;   # max 10 token
@l;   # max 10 linee
$vcardCnt = 0;

open( InFile, "<$InFile") || die ("Non trovo il file input: $InFile");

$cnt=0;
while( <InFile>) {
  $cnt = 0;
  $l[$cnt++] = $_;
  @w = split( ":", $_);
  if ($w[0] != "BEGIN") { 
    print "# errore begin mancante", "\n";
    exit;
  }
  else { 
    $vcardCnt++;
    $l[$cnt++] = <InFile>;   # Versione ...
    $cnt++;
    $l[$cnt] = <InFile>;   # N. nome;cognome
    @w = split( ":", $l[$cnt]);
    if ($w[0] != "N")  { 
      print "# errore N mancante", "\n";
    }
    else  { 
      @w1 = split( ";", $w[1]);
      chomp( $w1[1]);
#      if (index( $w1, "'") >= 0)  { 
#	$w[1] = "";
#      }
#     HANDLE per cognomi composti
      @w2 = split( " ", $w1[0]);
      if ($#w2 > 0)   { 
	$cognome = $w1[1] . " " . $w2[0];
	$nome = $w2[1];
      }
      else  { 
	$cognome = $w1[1];
	$nome = $w1[0];
      }
      if (length($nome) == 0) {  
           $OutFile = $TopDir . $cognome . ".vcf";
      }
      else {  
	$OutFile = $TopDir . $cognome . " " . $nome . ".vcf";
      }
      print( "Saving vcard in : " . $OutFile . "\n");
      $cnt++;
      $l[$cnt] = "FN:".$cognome . " " . $nome . "\n";
      do  { 
	$cnt++;
	$l[$cnt] = <InFile>;
	@w = split( ":", $l[$cnt]);
      } while ($w[0] ne "END");

      # letta una vcard ora la salva
      open( OutFile, ">$OutFile") || die ("Non posso aprire il file output: $OutFile") ;
      for($i=0; $i <= $cnt; $i++)   { 
	printf( OutFile "%s", $l[$i]);
      }
      close OutFile;
    }
  }
}
	    
