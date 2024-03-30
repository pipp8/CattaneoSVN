
$TopDir = "C:/prj/sissi/";

$InFile = $TopDir . "RagSoc.txt";
$OutFile = $TopDir . "MatchedRaws.txt";
$WordsFile = $TopDir . "words.txt";
$MinLen = 5;

#$SepList = "\\\"\\)\\-\\,\\&\\:\\;";
$SepList = "\")-,&:;";
$SepList = quotemeta( $SepList);

$tot = MakeWords( $MinLen);
printf( "%d parole estratte (len > %d)\nLista Separatori: <%s>\n",
	 $tot, $MinLen, $SepList);

open( InFile, "<$InFile") || die ("Non trovo il file input: $InFile");
open( WordsFile, "<$WordsFile") || die ("Non posso aprire il file delle parole: $WordsFile") ;
open( OutFile, ">$OutFile") || die ("Non posso aprire risultati: $OutFile") ;

$cnt=0;
while( <WordsFile>) {
    $cnt++;
    chop ; # una parola per linea
    $w = $_;
    $got = 0;
    # cerca tutte le parole a distanza 1
   $pattern = "(";
    for( $i = 0; $i < length( $w); $i++) {
      $pl = quotemeta(substr( $w, 0, $i)) . ".?" . quotemeta( substr($w, $i+1));
      if ($i > 0) {
	$pattern = $pattern . "|";
      }
      $pattern = $pattern . "[ ".$SepList."]+" . $pl . "[ ".$SepList."]+";
    }
    $pattern = $pattern . ")+";
    seek( InFile, 0, 0);
    $line = 0;
    while( <InFile>) {
      $line++;
      if ( /$pattern/i ) {
	if (!exists($found{ $line})) {
	  $found{ $line} = $_;
	  $got++;
	}
      }
    }
       
    if ($got > 1) {
	printf(OutFile "\nWord input = <%s> (pos. %6d/%6d) %d match(es)\n",
	       $w, $cnt, $tot, $got);
    }
    print $cnt, " ", $w, "-> ", $got, "\n";
    foreach $k ( sort( NumCompare keys( %found))) {
	if ($got > 1) {
	    printf(OutFile "\t%6d -> %s", $k, $found{$k});
	}
	delete( $found{ $k});
    }
}


sub MakeWords( $MinLen) {
    
    $BadWords{"S.P.A."} = 0;
    $BadWords{"S.R.L."} = 0;
    $BadWords{"S.N.C."} = 0;
    $BadWords{"&C.SNC"} = 0;
    $BadWords{"SOCIETà"} = 0;
    $BadWords{"AZIENDA"} = 0;
    $BadWords{"AZIENDE"} = 0;
    $BadWords{"INDUSTRIE"} = 0;
    $BadWords{"UNIONE"} = 0;
    $BadWords{"MACCHINE"} = 0;
    $BadWords{"NAZIONALE"} = 0;
    $BadWords{"FONDERIA"} = 0;
    $BadWords{"ITALIA"} = 0;
    $BadWords{"ASSOCIAZIONE"} = 0;
    $BadWords{"ASSOCIAZIONI"} = 0;
    $BadWords{"ALBERGO"} = 0;


    open( InFile, "<$InFile") || die ("Non trovo il file input: $InFile");
    open( WordsFile, ">$WordsFile") || die ("Non posso aprire il file delle parole> $WordsFile") ;

    while (<InFile>) {
	chop $_;
	s/[$SepList]+/ /g ;
	split ;

	foreach $w  (@_) {

	    $w =~ s/[\"\(\)]+//g ;
	    $w = uc( $w);
	    $w =~ s/(SNC|SPA|SRL)+//g ;

	    if (length( $w) > $MinLen) {
		if (!exists($BadWords{ $w})) {
		    $BadWords{$w} = 1;
		}
#		else {
#		    print $w, "Jumped\n";
#		}
	    }
	}
    }

    $cnt = 0;
    foreach $k ( sort(  keys( %BadWords))) {
	if ($BadWords{ $k} == 1) {
	    print WordsFile $k, "\n";
	    $cnt++;
	}
	delete( $BadWords{ $k});
    }
    
    close InFile;
    close WordsFile;
    return $cnt;
}


sub NumCompare {
    if ($a < $b) {
	return -1;
    }
    elsif ($a == $b) {
	return 0;
    }
    else {
	return 1;
    }
}
	    
