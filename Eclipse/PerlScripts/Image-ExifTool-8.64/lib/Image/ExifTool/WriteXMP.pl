#------------------------------------------------------------------------------
# File:         WriteXMP.pl
#
# Description:  Write XMP meta information
#
# Revisions:    12/19/2004 - P. Harvey Created
#------------------------------------------------------------------------------
package Image::ExifTool::XMP;

use strict;
use vars qw(%specialStruct %dateTimeInfo $xlatNamespace);

use Image::ExifTool qw(:DataAccess :Utils);

sub CheckXMP($$$);
sub CaptureXMP($$$;$);
sub SetPropertyPath($$;$$$$);

my $debug = 0;
my $numPadLines = 24;       # number of blank padding lines

# when writing extended XMP, resources bigger than this get placed in their own
# rdf:Description so they can be moved to the extended segments if necessary
my $newDescThresh = 10240;  # 10 kB

# individual resources and namespaces to place last in separate rdf:Description's
# so they can be moved to extended XMP segments if required (see Oct. 2008 XMP spec)
my %extendedRes = (
    'photoshop:History' => 1,
    'xap:Thumbnails' => 1,
    'xmp:Thumbnails' => 1,
    'crs' => 1,
    'crss' => 1,
);

my $rdfDesc = 'rdf:Description';
#
# packet/xmp/rdf headers and trailers
#
my $pktOpen = "<?xpacket begin='\xef\xbb\xbf' id='W5M0MpCehiHzreSzNTczkc9d'?>\n";
my $xmlOpen = "<?xml version='1.0' encoding='UTF-8'?>\n";
my $xmpOpenPrefix = "<x:xmpmeta xmlns:x='$nsURI{x}'";
my $rdfOpen = "<rdf:RDF xmlns:rdf='$nsURI{rdf}'>\n";
my $rdfClose = "</rdf:RDF>\n";
my $xmpClose = "</x:xmpmeta>\n";
my $pktCloseW =  "<?xpacket end='w'?>"; # writable by default
my $pktCloseR =  "<?xpacket end='r'?>";

#------------------------------------------------------------------------------
# Get XMP opening tag (and set x:xmptk appropriately)
# Inputs: 0) ExifTool object ref
# Returns: x:xmpmeta opening tag
sub XMPOpen($)
{
    my $exifTool = shift;
    my $nv = $exifTool->{NEW_VALUE}->{$Image::ExifTool::XMP::x{xmptk}};
    my $tk;
    if (defined $nv) {
        $tk = $exifTool->GetNewValues($nv);
        $exifTool->VerboseValue(($tk ? '+' : '-') . ' XMP-x:XMPToolkit', $tk);
        ++$exifTool->{CHANGED};
    } else {
        $tk = "Image::ExifTool $Image::ExifTool::VERSION";
    }
    my $str = $tk ? (" x:xmptk='" . EscapeXML($tk) . "'") : '';
    return "$xmpOpenPrefix$str>\n";
}

#------------------------------------------------------------------------------
# Validate XMP packet and set read or read/write mode
# Inputs: 0) XMP data reference, 1) 'r' = read only, 'w' or undef = read/write
# Returns: true if XMP is good (and adds packet header/trailer if necessary)
sub ValidateXMP($;$)
{
    my ($xmpPt, $mode) = @_;
    unless ($$xmpPt =~ /^\0*<\0*\?\0*x\0*p\0*a\0*c\0*k\0*e\0*t/) {
        return '' unless $$xmpPt =~ /^<x(mp)?:x[ma]pmeta/;
        # add required xpacket header/trailer
        $$xmpPt = $pktOpen . $$xmpPt . $pktCloseW;
    }
    $mode = 'w' unless $mode;
    my $end = substr($$xmpPt, -32, 32);
    # check for proper xpacket trailer and set r/w mode if necessary
    return '' unless $end =~ s/(e\0*n\0*d\0*=\0*['"]\0*)([rw])(\0*['"]\0*\?\0*>)/$1$mode$3/;
    substr($$xmpPt, -32, 32) = $end if $2 ne $mode;
    return 1;
}

#------------------------------------------------------------------------------
# Check XMP date values for validity and format accordingly
# Inputs: 1) date string
# Returns: XMP date/time string (or undef on error)
sub FormatXMPDate($)
{
    my $val = shift;
    my ($y, $m, $d, $t, $tz);
    if ($val =~ /(\d{4}):(\d{2}):(\d{2}) (\d{2}:\d{2}(?::\d{2}(?:\.\d*)?)?)(.*)/) {
        ($y, $m, $d, $t, $tz) = ($1, $2, $3, $4, $5);
        $val = "$y-$m-${d}T$t";
    } elsif ($val =~ /^\s*\d{4}(:\d{2}){0,2}\s*$/) {
        # this is just a date (YYYY, YYYY-mm or YYYY-mm-dd)
        $val =~ tr/:/-/;
    } elsif ($val =~ /^\s*(\d{2}:\d{2}(?::\d{2}(?:\.\d*)?)?)(.*)\s*$/) {
        # this is just a time
        ($t, $tz) = ($1, $2);
        $val = $t;
    } else {
        return undef;
    }
    if ($tz) {
        $tz =~ /^(Z|[+-]\d{2}:\d{2})$/ or return undef;
        $val .= $tz;
    }
    return $val;
}

#------------------------------------------------------------------------------
# Check XMP values for validity and format accordingly
# Inputs: 0) ExifTool object ref, 1) tagInfo hash ref, 2) raw value ref
# Returns: error string or undef (and may change value) on success
# Note: copies structured information to avoid conflicts with calling code
sub CheckXMP($$$)
{
    my ($exifTool, $tagInfo, $valPtr) = @_;

    if ($$tagInfo{Struct}) {
        require 'Image/ExifTool/XMPStruct.pl';
        my ($item, $err, $w, $warn);
        unless (ref $$valPtr) {
            ($$valPtr, $warn) = InflateStruct($valPtr);
            # expect a structure HASH ref or ARRAY of structures
            ref $$valPtr or return 'Improperly formed structure';
        }
        if (ref $$valPtr eq 'ARRAY') {
            return 'Not a list tag' unless $$tagInfo{List};
            my @copy = ( @{$$valPtr} ); # copy the list for ExifTool to use
            $$valPtr = \@copy;          # return the copy
            foreach $item (@copy) {
                unless (ref $item eq 'HASH') {
                    ($item, $w) = InflateStruct(\$item); # deserialize structure
                    $w and $warn = $w;
                    next if ref $item eq 'HASH';
                    $err = 'Improperly formed structure';
                    last;
                }
                ($item, $err) = CheckStruct($exifTool, $item, $$tagInfo{Struct});
                last if $err;
            }
        } else {
            ($$valPtr, $err) = CheckStruct($exifTool, $$valPtr, $$tagInfo{Struct});
        }
        $warn and $$exifTool{CHECK_WARN} = $warn;
        return $err;
    }
    my $format = $tagInfo->{Writable};
    # (if no format specified, value is a simple string)
    if (not $format or $format eq 'string' or $format eq 'lang-alt') {
        # convert value to UTF8 if necessary
        if ($exifTool->{OPTIONS}->{Charset} ne 'UTF8') {
            if ($$valPtr =~ /[\x80-\xff]/) {
                # convert from Charset to UTF-8
                $$valPtr = $exifTool->Encode($$valPtr,'UTF8');
            }
        } else {
            # translate invalid XML characters to "."
            $$valPtr =~ tr/\0-\x08\x0b\x0c\x0e-\x1f/./;
            # fix any malformed UTF-8 characters
            if (FixUTF8($valPtr) and not $$exifTool{WarnBadUTF8}) {
                $exifTool->Warn('Malformed UTF-8 character(s)');
                $$exifTool{WarnBadUTF8} = 1;
            }
        }
        return undef;   # success
    }
    if ($format eq 'rational' or $format eq 'real') {
        # make sure the value is a valid floating point number
        unless (Image::ExifTool::IsFloat($$valPtr) or
            # allow 'inf' and 'undef' rational values
            ($format eq 'rational' and ($$valPtr eq 'inf' or
             $$valPtr eq 'undef' or Image::ExifTool::IsRational($$valPtr))))
        {
            return 'Not a floating point number' 
        }
        if ($format eq 'rational') {
            $$valPtr = join('/', Image::ExifTool::Rationalize($$valPtr));
        }
    } elsif ($format eq 'integer') {
        # make sure the value is integer
        if (Image::ExifTool::IsInt($$valPtr)) {
            # no conversion required (converting to 'int' would remove leading '+')
        } elsif (Image::ExifTool::IsHex($$valPtr)) {
            $$valPtr = hex($$valPtr);
        } else {
            return 'Not an integer';
        }
    } elsif ($format eq 'date') {
        my $newDate = FormatXMPDate($$valPtr);
        return "Invalid date/time (use YYYY:mm:dd HH:MM:SS[.ss][+/-HH:MM|Z])" unless $newDate;
        $$valPtr = $newDate;
    } elsif ($format eq 'boolean') {
        if (not $$valPtr or $$valPtr =~ /false/i or $$valPtr =~ /^no$/i) {
            $$valPtr = 'False';
        } else {
            $$valPtr = 'True';
        }
    } elsif ($format eq '1') {
        # this is the entire XMP data block
        return 'Invalid XMP data' unless ValidateXMP($valPtr);
    } else {
        return "Unknown XMP format: $format";
    }
    return undef;   # success!
}

#------------------------------------------------------------------------------
# Get PropertyPath for specified tagInfo
# Inputs: 0) tagInfo reference
# Returns: PropertyPath string
sub GetPropertyPath($)
{
    my $tagInfo = shift;
    SetPropertyPath($$tagInfo{Table}, $$tagInfo{TagID}) unless $$tagInfo{PropertyPath};
    return $$tagInfo{PropertyPath};
}

#------------------------------------------------------------------------------
# Set PropertyPath for specified tag (also for associated flattened tags and structure elements)
# Inputs: 0) tagTable reference, 1) tagID, 2) tagID of parent structure,
#         3) structure definition ref (or undef), 4) property list up to this point (or undef),
#         5) flag set if any containing structure has a TYPE
# Notes: also generates flattened tags if they don't already exist
sub SetPropertyPath($$;$$$$)
{
    my ($tagTablePtr, $tagID, $parentID, $structPtr, $propList, $isType) = @_;
    my $table = $structPtr || $tagTablePtr;
    my $tagInfo = $$table{$tagID};

    return if ref($tagInfo) ne 'HASH' or $$tagInfo{PropertyPath};

    # don't override existing main table entry if already set by a Struct
    if ($structPtr) {
        $isType = 1 if $$structPtr{TYPE};
    } else {
        # use property path from original tagInfo if this is an alternate-language tag
        my $srcInfo = $$tagInfo{SrcTagInfo};
        $$tagInfo{PropertyPath} = GetPropertyPath($srcInfo) if $srcInfo;
        return if $$tagInfo{PropertyPath};
        # set property path for all flattened tags in structure if necessary
        if ($$tagInfo{RootTagInfo}) {
            SetPropertyPath($tagTablePtr, $$tagInfo{RootTagInfo}{TagID});
            return if $$tagInfo{PropertyPath};
            warn "Internal Error: Didn't set path from root for $tagID\n";
        }
    }
    my $ns = $$tagInfo{Namespace} || $$table{NAMESPACE};
    $ns or warn("No namespace for $tagID\n"), return;
    my (@propList, $listType);
    $propList and @propList = @$propList;
    push @propList, "$ns:$tagID";
    # lang-alt lists are handled specially, signified by Writable='lang-alt'
    if ($$tagInfo{Writable} and $$tagInfo{Writable} eq 'lang-alt') {
        $listType = 'Alt';
        # remove language code from property path if it exists
        $propList[-1] =~ s/-$$tagInfo{LangCode}$// if $$tagInfo{LangCode};
        # handle lists of lang-alt lists (ie. XMP-plus:Custom tags)
        if ($$tagInfo{List} and $$tagInfo{List} ne '1') {
            push @propList, "rdf:$$tagInfo{List}", 'rdf:li 10';
        }
    } else {
        $listType = $$tagInfo{List};
    }
    # add required properties if this is a list
    push @propList, "rdf:$listType", 'rdf:li 10' if $listType and $listType ne '1';
    # set PropertyPath for all flattened tags of this structure if necessary
    # (note: don't do this for variable-namespace structures (undef NAMESPACE))
    my $strTable = $$tagInfo{Struct};
    if ($strTable and $$strTable{NAMESPACE}) {
        # make sure the structure namespace has been registered
        # (user-defined namespaces may not have been)
        RegisterNamespace($strTable) if ref $$strTable{NAMESPACE};
        my $tag;
        foreach $tag (keys %$strTable) {
            # ignore special fields and any lang-alt fields we may have added
            next if $specialStruct{$tag} or $$strTable{$tag}{LangCode};
            my $fullID = $parentID ? $parentID . ucfirst($tagID) : $tagID;
            SetPropertyPath($tagTablePtr, $tag, $fullID, $strTable, \@propList, $isType);
        }
    }
    # if this was a structure field and not a normal tag,
    # we set PropertyPath in the corresponding flattened tag
    if ($structPtr) {
        my $flatID = $parentID . ucfirst($tagID);
        $tagInfo = $$tagTablePtr{$flatID};
        # create flattened tag now if necessary
        # (could happen if we were just writing a structure)
        unless ($tagInfo) {
            $tagInfo = { Name => ucfirst($flatID), Flat => 1 };
            Image::ExifTool::AddTagToTable($tagTablePtr, $flatID, $tagInfo);
        }
        # set StructType flag if any containing structure has a TYPE
        $$tagInfo{StructType} = 1 if $isType;
    }
    # set property path for tagInfo in main table
    $$tagInfo{PropertyPath} = join '/', @propList;
}

#------------------------------------------------------------------------------
# Save XMP property name/value for rewriting
# Inputs: 0) ExifTool object reference
#         1) reference to array of XMP property path (last is current property)
#         2) property value, 3) optional reference to hash of property attributes
sub CaptureXMP($$$;$)
{
    my ($exifTool, $propList, $val, $attrs) = @_;
    return unless defined $val and @$propList > 2;
    if ($$propList[0] =~ /^x:x[ma]pmeta$/ and
        $$propList[1] eq 'rdf:RDF' and
        $$propList[2] =~ /$rdfDesc( |$)/)
    {
        # no properties to save yet if this is just the description
        return unless @$propList > 3;
        # ignore empty list properties
        if ($$propList[-1] =~ /^rdf:(Bag|Seq|Alt)$/) {
            $exifTool->Warn("Ignored empty $$propList[-1] list for $$propList[-2]", 1);
            return;
        }
        # save information about this property
        my $capture = $exifTool->{XMP_CAPTURE};
        my $path = join('/', @$propList[3..$#$propList]);
        if (defined $$capture{$path}) {
            $exifTool->{XMP_ERROR} = "Duplicate XMP property: $path";
        } else {
            $$capture{$path} = [$val, $attrs || { }];
        }
    } elsif ($$propList[0] eq 'rdf:RDF' and
             $$propList[1] =~ /$rdfDesc( |$)/)
    {
        # set flag so we don't write x:xmpmeta element
        $exifTool->{XMP_NO_XMPMETA} = 1;
        # add missing x:xmpmeta element and try again
        unshift @$propList, 'x:xmpmeta';
        CaptureXMP($exifTool, $propList, $val, $attrs);
    } else {
        $exifTool->{XMP_ERROR} = 'Improperly enclosed XMP property: ' . join('/',@$propList);
    }
}

#------------------------------------------------------------------------------
# Save information about resource containing blank node with nodeID
# Inputs: 0) reference to blank node information hash
#         1) reference to property list
#         2) property value
#         3) [optional] reference to attribute hash
# Notes: This routine and ProcessBlankInfo() are also used for reading information, but
#        are uncommon so are put in this file to reduce compile time for the common case
sub SaveBlankInfo($$$;$)
{
    my ($blankInfo, $propListPt, $val, $attrs) = @_;

    my $propPath = join '/', @$propListPt;
    my @ids = ($propPath =~ m{ #([^ /]*)}g);
    my $id;
    # split the property path at each nodeID
    foreach $id (@ids) {
        my ($pre, $prop, $post) = ($propPath =~ m{^(.*?)/([^/]*) #$id((/.*)?)$});
        defined $pre or warn("internal error parsing nodeID's"), next;
        # the element with the nodeID should be in the path prefix for subject
        # nodes and the path suffix for object nodes
        unless ($prop eq $rdfDesc) {
            if ($post) {
                $post = "/$prop$post";
            } else {
                $pre = "$pre/$prop";
            }
        }
        $blankInfo->{Prop}->{$id}->{Pre}->{$pre} = 1;
        if ((defined $post and length $post) or (defined $val and length $val)) {
            # save the property value and attributes for each unique path suffix
            $blankInfo->{Prop}->{$id}->{Post}->{$post} = [ $val, $attrs, $propPath ];
        }
    }
}

#------------------------------------------------------------------------------
# Process blank-node information
# Inputs: 0) ExifTool object ref, 1) tag table ref,
#         2) blank node information hash ref, 3) flag set for writing
sub ProcessBlankInfo($$$;$)
{
    my ($exifTool, $tagTablePtr, $blankInfo, $isWriting) = @_;
    $exifTool->VPrint(1, "  [Elements with nodeID set:]\n") unless $isWriting;
    my ($id, $pre, $post);
    # handle each nodeID separately
    foreach $id (sort keys %{$$blankInfo{Prop}}) {
        my $path = $blankInfo->{Prop}->{$id};
        # flag all resource names so we can warn later if some are unused
        my %unused;
        foreach $post (keys %{$path->{Post}}) {
            $unused{$post} = 1;
        }
        # combine property paths for all possible paths through this node
        foreach $pre (sort keys %{$path->{Pre}}) {
            # there will be no description for the object of a blank node
            next unless $pre =~ m{/$rdfDesc/};
            foreach $post (sort keys %{$path->{Post}}) {
                my @propList = split m{/}, "$pre$post";
                my ($val, $attrs) = @{$path->{Post}->{$post}};
                if ($isWriting) {
                    CaptureXMP($exifTool, \@propList, $val, $attrs);
                } else {
                    FoundXMP($exifTool, $tagTablePtr, \@propList, $val);
                }
                delete $unused{$post};
            }
        }
        # save information from unused properties (if RDF is malformed like f-spot output)
        if (%unused) {
            $exifTool->Options('Verbose') and $exifTool->Warn('An XMP resource is about nothing');
            foreach $post (sort keys %unused) {
                my ($val, $attrs, $propPath) = @{$path->{Post}->{$post}};
                my @propList = split m{/}, $propPath;
                if ($isWriting) {
                    CaptureXMP($exifTool, \@propList, $val, $attrs);
                } else {
                    FoundXMP($exifTool, $tagTablePtr, \@propList, $val);
                }
            }
        }
    }
}

#------------------------------------------------------------------------------
# Convert path to namespace used in file (this is a pain, but the XMP
# spec only suggests 'preferred' namespace prefixes...)
# Inputs: 0) ExifTool object reference, 1) property path
# Returns: conforming property path
sub ConformPathToNamespace($$)
{
    my ($exifTool, $path) = @_;
    my @propList = split('/',$path);
    my $nsUsed = $exifTool->{XMP_NS};
    my $prop;
    foreach $prop (@propList) {
        my ($ns, $tag) = $prop =~ /(.+?):(.*)/;
        next if $$nsUsed{$ns};
        my $uri = $nsURI{$ns};
        unless ($uri) {
            warn "No URI for namepace prefix $ns!\n";
            next;
        }
        my $ns2;
        foreach $ns2 (keys %$nsUsed) {
            next unless $$nsUsed{$ns2} eq $uri;
            # use the existing namespace prefix instead of ours
            $prop = "$ns2:$tag";
            last;
        }
    }
    return join('/',@propList);
}

#------------------------------------------------------------------------------
# Add necessary rdf:type element when writing structure
# Inputs: 0) ExifTool ref, 1) tag table ref, 2) capture hash ref, 3) path string
#         4) optional base path (already conformed to namespace) for elements in
#            variable-namespace structures
sub AddStructType($$$$;$)
{
    my ($exifTool, $tagTablePtr, $capture, $path, $basePath) = @_;
    my @props = split '/', $path;
    my %doneID;
    for (;;) {
        pop @props;
        last unless @props;
        my $tagID = GetXMPTagID(\@props);
        next if $doneID{$tagID};
        $doneID{$tagID} = 1;
        my $tagInfo = $$tagTablePtr{$tagID};
        last unless ref $tagInfo eq 'HASH';
        if ($$tagInfo{Struct}) {
            my $type = $$tagInfo{Struct}{TYPE};
            if ($type) {
                my $pat = $$tagInfo{PropertyPath};
                $pat or warn("Missing PropertyPath in AddStructType\n"), last;
                $pat = ConformPathToNamespace($exifTool, $pat);
                $pat =~  s/ \d+/ \\d\+/g;
                $path =~ /^($pat)/ or warn("Wrong path in AddStructType\n"), last;
                my $p = $1 . '/rdf:type';
                $p = "$basePath/$p" if $basePath;
                $$capture{$p} = [ '', { 'rdf:resource' => $type } ] unless $$capture{$p};
            }
        }
        last unless $$tagInfo{StructType};
    }
}

#------------------------------------------------------------------------------
# Utility routine to encode data in base64
# Inputs: 0) binary data string
# Returns:   base64-encoded string
sub EncodeBase64($)
{
    # encode the data in 45-byte chunks
    my $chunkSize = 45;
    my $len = length $_[0];
    my $str = '';
    my $i;
    for ($i=0; $i<$len; $i+=$chunkSize) {
        my $n = $len - $i;
        $n = $chunkSize if $n > $chunkSize;
        # add uuencoded data to output (minus size byte, but including trailing newline)
        $str .= substr(pack('u', substr($_[0], $i, $n)), 1);
    }
    # convert to base64 (remember that "\0" may be encoded as ' ' or '`')
    $str =~ tr/` -_/AA-Za-z0-9+\//;
    # convert pad characters at the end (remember to account for trailing newline)
    my $pad = 3 - ($len % 3);
    substr($str, -$pad-1, $pad) = ('=' x $pad) if $pad < 3;
    return $str;
}

#------------------------------------------------------------------------------
# sort tagInfo hash references by tag name
sub ByTagName
{
    return $$a{Name} cmp $$b{Name};
}

#------------------------------------------------------------------------------
# sort alphabetically, but with rdf:type first in the structure
sub TypeFirst
{
    if ($a =~ /rdf:type$/) {
        return substr($a, 0, -8) cmp $b unless $b =~ /rdf:type$/;
    } elsif ($b =~ /rdf:type$/) {
        return $a cmp substr($b, 0, -8);
    }
    return $a cmp $b;
}

#------------------------------------------------------------------------------
# Limit size of XMP
# Inputs: 0) ExifTool object ref, 1) XMP data ref (written up to start of $rdfClose),
#         2) max XMP len, 3) rdf:about string, 4) list ref for description start offsets
#         5) start offset of first description recommended for extended XMP
# Returns: 0) extended XMP ref, 1) GUID and updates $$dataPt (or undef if no extended XMP)
sub LimitXMPSize($$$$$$)
{
    my ($exifTool, $dataPt, $maxLen, $about, $startPt, $extStart) = @_;

    # return straight away if it isn't too big
    return undef if length($$dataPt) < $maxLen;

    push @$startPt, length($$dataPt);  # add end offset to list
    my $newData = substr($$dataPt, 0, $$startPt[0]);
    my $guid = '0' x 32;
    # write the required xmpNote:HasExtendedXMP property
    $newData .= "\n <$rdfDesc rdf:about='$about'\n  xmlns:xmpNote='$nsURI{xmpNote}'>\n" .
                  "  <xmpNote:HasExtendedXMP>$guid</xmpNote:HasExtendedXMP>\n" .
                  " </$rdfDesc>\n";

    my ($i, %descSize, $start);
    # calculate all description block sizes
    for ($i=1; $i<@$startPt; ++$i) {
        $descSize{$$startPt[$i-1]} = $$startPt[$i] - $$startPt[$i-1];
    }
    pop @$startPt;    # remove end offset
    # write the descriptions from smallest to largest, as many in main XMP as possible
    my @descStart = sort { $descSize{$a} <=> $descSize{$b} } @$startPt;
    my $extData = XMPOpen($exifTool) . $rdfOpen;
    for ($i=0; $i<2; ++$i) {
      foreach $start (@descStart) {
        # write main XMP first (in order of size), then extended XMP afterwards (in order)
        next if $i xor $start >= $extStart;
        my $pt = (length($newData) + $descSize{$start} > $maxLen) ? \$extData : \$newData;
        $$pt .= substr($$dataPt, $start, $descSize{$start});
      }
    }
    $extData .= $rdfClose . $xmpClose;  # close rdf:RDF and x:xmpmeta
    # calculate GUID from MD5 of extended XMP data
    if (eval 'require Digest::MD5') {
        $guid = uc unpack('H*', Digest::MD5::md5($extData));
        $newData =~ s/0{32}/$guid/;     # update GUID in main XMP segment
    }
    $exifTool->VerboseValue('+ XMP-xmpNote:HasExtendedXMP', $guid);
    $$dataPt = $newData;        # return main XMP block
    return (\$extData, $guid);  # return extended XMP and its GUID
}

#------------------------------------------------------------------------------
# Write XMP information
# Inputs: 0) ExifTool object reference, 1) source dirInfo reference,
#         2) [optional] tag table reference
# Returns: with tag table: new XMP data (may be empty if no XMP data) or undef on error
#          without tag table: 1 on success, 0 if not valid XMP file, -1 on write error
# Notes: May set dirInfo InPlace flag to rewrite with specified DirLen
#        May set dirInfo ReadOnly flag to write as read-only XMP ('r' mode and no padding)
#        May set dirInfo Compact flag to force compact (drops 2kB of padding)
#        May set dirInfo MaxDataLen to limit output data length -- this causes ExtendedXMP
#          and ExtendedGUID to be returned in dirInfo if extended XMP was required
sub WriteXMP($$;$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    $exifTool or return 1;    # allow dummy access to autoload this package
    my $dataPt = $$dirInfo{DataPt};
    my (%capture, %nsUsed, $xmpErr, $tagInfo, $about);
    my $changed = 0;
    my $xmpFile = (not $tagTablePtr);   # this is an XMP data file if no $tagTablePtr
    # prefer XMP over other metadata formats in some types of files
    my $preferred = $xmpFile || ($$exifTool{PreferredGroup} and $$exifTool{PreferredGroup} eq 'XMP');
    my $verbose = $exifTool->Options('Verbose');
    my $dirLen = $$dirInfo{DirLen};
    $dirLen = length($$dataPt) if not defined $dirLen and $dataPt;
#
# extract existing XMP information into %capture hash
#
    # define hash in ExifTool object to capture XMP information (also causes
    # CaptureXMP() instead of FoundXMP() to be called from ParseXMPElement())
    #
    # The %capture hash is keyed on the complete property path beginning after
    # rdf:RDF/rdf:Description/.  The values are array references with the
    # following entries: 0) value, 1) attribute hash reference.
    $exifTool->{XMP_CAPTURE} = \%capture;
    $exifTool->{XMP_NS} = \%nsUsed;
    delete $exifTool->{XMP_NO_XMPMETA};
    delete $exifTool->{XMP_NO_XPACKET};
    delete $exifTool->{XMP_IS_XML};
    delete $exifTool->{XMP_IS_SVG};

    if ($xmpFile or $dirLen) {
        delete $exifTool->{XMP_ERROR};
        delete $exifTool->{XMP_ABOUT};
        # extract all existing XMP information (to the XMP_CAPTURE hash)
        my $success = ProcessXMP($exifTool, $dirInfo, $tagTablePtr);
        # don't continue if there is nothing to parse or if we had a parsing error
        unless ($success and not $exifTool->{XMP_ERROR}) {
            my $err = $exifTool->{XMP_ERROR} || 'Error parsing XMP';
            # may ignore this error only if we were successful
            if ($xmpFile) {
                my $raf = $$dirInfo{RAF};
                # allow empty XMP data so we can create something from nothing
                if ($success or not $raf->Seek(0,2) or $raf->Tell()) {
                    # no error message if not an XMP file
                    return 0 unless $exifTool->{XMP_ERROR};
                    if ($exifTool->Error($err, $success)) {
                        delete $exifTool->{XMP_CAPTURE};
                        return 0;
                    }
                }
            } else {
                if ($exifTool->Warn($err, $success)) {
                    delete $exifTool->{XMP_CAPTURE};
                    return undef;
                }
            }
        }
        $tagInfo = $Image::ExifTool::XMP::rdf{about};
        if (defined $exifTool->{NEW_VALUE}->{$tagInfo}) {
            $about = $exifTool->GetNewValues($exifTool->{NEW_VALUE}->{$tagInfo}) || '';
            if ($verbose > 1) {
                my $wasAbout = $exifTool->{XMP_ABOUT};
                $exifTool->VerboseValue('- XMP-rdf:About', UnescapeXML($wasAbout)) if defined $wasAbout;
                $exifTool->VerboseValue('+ XMP-rdf:About', $about);
            }
            $about = EscapeXML($about); # must escape for XML
            ++$changed;
        } else {
            $about = $exifTool->{XMP_ABOUT} || '';
        }
        delete $exifTool->{XMP_ERROR};
        delete $exifTool->{XMP_ABOUT};
    } else {
        $about = '';
    }
#
# handle writing XMP as a block to XMP file
#
    if ($xmpFile) {
        $tagInfo = $Image::ExifTool::Extra{XMP};
        if ($tagInfo and $exifTool->{NEW_VALUE}->{$tagInfo}) {
            my $rtnVal = 1;
            my $newVal = $exifTool->GetNewValues($exifTool->{NEW_VALUE}->{$tagInfo});
            if (defined $newVal and length $newVal) {
                $exifTool->VPrint(0, "  Writing XMP as a block\n");
                ++$exifTool->{CHANGED};
                Write($$dirInfo{OutFile}, $newVal) or $rtnVal = -1;
            }
            delete $exifTool->{XMP_CAPTURE};
            return $rtnVal;
        }
    }
#
# delete groups in family 1 if requested
#
    if (%{$exifTool->{DEL_GROUP}} and (grep /^XMP-.+$/, keys %{$exifTool->{DEL_GROUP}} or
        # (logic is a bit more complex for group names in exiftool XML files)
        grep m{^http://ns.exiftool.ca/}, values %nsUsed))
    {
        my $del = $exifTool->{DEL_GROUP};
        my $path;
        foreach $path (keys %capture) {
            my @propList = split('/',$path); # get property list
            my ($tag, $ns) = GetXMPTagID(\@propList);
            # translate namespace if necessary
            $ns = $$xlatNamespace{$ns} if $$xlatNamespace{$ns};
            my ($grp, @g);
            # no "XMP-" added to most groups in exiftool RDF/XML output file
            if ($nsUsed{$ns} and (@g = ($nsUsed{$ns} =~ m{^http://ns.exiftool.ca/(.*?)/(.*?)/}))) {
                if ($g[1] =~ /^\d/) {
                    $grp = "XML-$g[0]";
                    #(all XML-* groups stored as uppercase DEL_GROUP key)
                    my $ucg = uc $grp;
                    next unless $$del{$ucg} or ($$del{'XML-*'} and not $$del{"-$ucg"});
                } else {
                    $grp = $g[1];
                    next unless $$del{$grp} or ($$del{$g[0]} and not $$del{"-$grp"});
                }
            } else {
                $grp = "XMP-$ns";
                my $ucg = uc $grp;
                next unless $$del{$ucg} or ($$del{'XMP-*'} and not $$del{"-$ucg"});
            }
            $exifTool->VerboseValue("- $grp:$tag", $capture{$path}->[0]);
            delete $capture{$path};
            ++$changed;
        }
    }
    # delete HasExtendedXMP tag (we create it as needed)
    my $hasExtTag = 'xmpNote:HasExtendedXMP';
    if ($capture{$hasExtTag}) {
        $exifTool->VerboseValue("- XMP-$hasExtTag", $capture{$hasExtTag}->[0]);
        delete $capture{$hasExtTag};
    }
    # set $xmpOpen now to to handle xmptk tag first
    my $xmpOpen = $exifTool->{XMP_NO_XMPMETA} ? '' : XMPOpen($exifTool);
#
# add, delete or change information as specified
#
    # get hash of all information we want to change
    # (sorted by tag name so alternate languages come last)
    my @tagInfoList = sort ByTagName $exifTool->GetNewTagInfoList();
    foreach $tagInfo (@tagInfoList) {
        next unless $exifTool->GetGroup($tagInfo, 0) eq 'XMP';
        my $tag = $$tagInfo{TagID};
        my $path = GetPropertyPath($tagInfo);
        unless ($path) {
            $exifTool->Warn("Can't write XMP:$tag (namespace unknown)");
            next;
        }
        # skip tags that were handled specially
        if ($path eq 'rdf:about' or $path eq 'x:xmptk') {
            ++$changed;
            next;
        }
        my $isStruct = $$tagInfo{Struct};
        # change our property path namespace prefixes to conform
        # to the ones used in this file
        $path = ConformPathToNamespace($exifTool, $path);
        # find existing property
        my $cap = $capture{$path}; 
        # MicrosoftPhoto screws up the case of some tags, and some other software,
        # including Adobe software, has been known to write the wrong list type or
        # not properly enclose properties in a list, so we check for this
        # (NOTE: we don't currently do these tests when writing structures!
        #  --> add this to DeleteStruct() below if it turns out to be a problem)
        unless ($cap or $isStruct) {
            my $regex = quotemeta $path;
            # also allow for missing structure fields in lists of structures
            $regex =~  s/ \d+/ \\d\+/g;
            my $ok = $regex; # regular expression to match standard property names
            # also check for incorrect list types which can cause problems
            if ($regex =~ s{\\/rdf\\:(Bag|Seq|Alt)\\/}{/rdf:(Bag|Seq|Alt)/}g) {
                # also look for missing bottom-level list
                $regex =~ s{/rdf:\(Bag\|Seq\|Alt\)\/rdf\\:li\\ \\d\+$}{(/.*)?};
            }
            my @matches = sort grep m{^$regex$}i, keys %capture;
            if (@matches) {
                if ($matches[0] =~ /^$ok$/) {
                    $path = $matches[0];    # use existing property path
                    $cap = $capture{$path};
                } else {
                    # property list was wrong, so issue a warning and fix it
                    my ($match, @fixed, %fixed, $err);
                    foreach $match (@matches) {
                        my $fixed = $path;
                        # set list indices
                        while ($match =~ / (\d+)/g) {
                            my $idx = $1;
                            # insert leading "X" so we don't replace this one again
                            $fixed =~ s/ \d+/ X$idx/;
                        }
                        $fixed =~ s/ X/ /g if $fixed ne $path;  # remove "X"s
                        $err = 1 if $capture{$fixed} or $fixed{$fixed};
                        push @fixed, $fixed;
                        $fixed{$fixed} = 1;
                    }
                    my $tg = $exifTool->GetGroup($tagInfo, 1) . ':' . $$tagInfo{Name};
                    my $wrn = lc($path) eq lc($matches[0]) ? 'tag ID case' : 'list type';
                    if ($err) {
                        $exifTool->Warn("Incorrect $wrn for $tg conflicts with existing tag");
                    } else {
                        # fix the incorrect property paths for all values of this tag
                        foreach $match (@matches) {
                            my $fixed = shift @fixed;
                            $capture{$fixed} = $capture{$match};
                            delete $capture{$match};
                        }
                        $cap = $capture{$path} || $capture{$fixed[0]};
                        $exifTool->Warn("Fixed incorrect $wrn for $tg", 1);
                    }
                }
            }
        }
        my $nvHash = $exifTool->GetNewValueHash($tagInfo);
        my $overwrite = $exifTool->IsOverwriting($nvHash);
        my $writable = $$tagInfo{Writable} || '';
        my (%attrs, $deleted, $added);
        # delete existing entry if necessary
        if ($isStruct) {
            require 'Image/ExifTool/XMPStruct.pl';
            ($deleted, $added) = DeleteStruct($exifTool, \%capture, \$path, $nvHash, \$changed);
        } elsif ($cap) {
            # take attributes from old values if they exist
            %attrs = %{$$cap[1]};
            if ($overwrite) {
                my ($delPath, $oldLang, $delLang, $addLang, @matchingPaths);
                # check to see if this is an indexed list item
                if ($path =~ / /) {
                    my $pp;
                    ($pp = $path) =~ s/ \d+/ \\d\+/g;
                    @matchingPaths = sort grep(/^$pp$/, keys %capture);
                } else {
                    push @matchingPaths, $path;
                }
                foreach $path (@matchingPaths) {
                    my ($val, $attrs) = @{$capture{$path}};
                    if ($writable eq 'lang-alt') {
                        unless (defined $addLang) {
                            # add to lang-alt list by default if creating this tag from scratch
                            $addLang = Image::ExifTool::IsCreating($nvHash) ? 1 : 0;
                        }
                        # get original language code (lc for comparisons)
                        $oldLang = lc($$attrs{'xml:lang'} || 'x-default');
                        if ($overwrite < 0) {
                            my $newLang = lc($$tagInfo{LangCode} || 'x-default');
                            next unless $oldLang eq $newLang;
                            # only add new tag if we are overwriting this one
                            # (note: this won't match if original XML contains CDATA!)
                            $addLang = $exifTool->IsOverwriting($nvHash, UnescapeXML($val));
                            next unless $addLang;
                        }
                        # delete all if deleting "x-default" and writing with no LangCode
                        # (XMP spec requires x-default language exist and be first in list)
                        if ($oldLang eq 'x-default' and not $$tagInfo{LangCode}) {
                            $delLang = 1;   # delete all languages
                            $overwrite = 1; # force overwrite
                        } elsif ($$tagInfo{LangCode} and not $delLang) {
                            # only overwrite specified language
                            next unless lc($$tagInfo{LangCode}) eq $oldLang;
                        }
                    } elsif ($overwrite < 0) {
                        # only overwrite specific values
                        if ($$nvHash{Shift}) {
                            # values to be shifted are checked (hence re-formatted) late,
                            # so we must un-format the to-be-shifted value for IsOverwriting()
                            my $fmt = $$tagInfo{Writable} || '';
                            if ($fmt eq 'rational') {
                                ConvertRational($val);
                            } elsif ($fmt eq 'date') {
                                $val = ConvertXMPDate($val);
                            }
                        }
                        # (note: this won't match if original XML contains CDATA!)
                        next unless $exifTool->IsOverwriting($nvHash, UnescapeXML($val));
                    }
                    if ($verbose > 1) {
                        my $grp = $exifTool->GetGroup($tagInfo, 1);
                        my $tagName = $$tagInfo{Name};
                        $tagName =~ s/-$$tagInfo{LangCode}$// if $$tagInfo{LangCode};
                        $tagName .= '-' . $$attrs{'xml:lang'} if $$attrs{'xml:lang'};
                        $exifTool->VerboseValue("- $grp:$tagName", $val);
                    }
                    # save attributes and path from first deleted property
                    # so we can replace it exactly
                    unless ($delPath) {
                        %attrs = %$attrs;
                        $delPath = $path;
                    }
                    # delete this tag
                    delete $capture{$path};
                    ++$changed;
                    # delete rdf:type tag if it is the only thing left in this structure
                    if ($path =~ /^(.*)\// and $capture{"$1/rdf:type"}) {
                        my $pp = $1;
                        my @a = grep /^\Q$pp\E\/[^\/]+/, keys %capture;
                        delete $capture{"$pp/rdf:type"} if @a == 1;
                    }
                }
                next unless $delPath or $$tagInfo{List} or $addLang;
                if ($delPath) {
                    $path = $delPath;
                    $deleted = 1;
                } else {
                    # don't change tag if we couldn't delete old copy
                    # unless this is a list or an lang-alt tag
                    next unless $$tagInfo{List} or $oldLang;
                    # (match last index to put in same lang-alt list for Bag of lang-alt items)
                    $path =~ m/.* (\d+)/g or warn "Internal error: no list index!\n", next;
                    $added = $1;
                }
            } else {
                # are never overwriting, so we must be adding to a list
                # match the last index unless this is a list of lang-alt lists
                my $pat = $writable eq 'lang-alt' ? '.* (\d+)(.*? \d+)' : '.* (\d+)';
                if ($path =~ m/$pat/g) {
                    $added = $1;
                    # set position to end of matching index number
                    pos($path) = pos($path) - length($2) if $2;
                }
            }
            if (defined $added) {
                my $len = length $added;
                my $pos = pos($path) - $len;
                my $nxt = substr($added, 1) + 1;
                # always insert x-default lang-alt entry first (as per XMP spec)
                # (need to test $overwrite because this will be a new lang-alt entry otherwise)
                if ($overwrite and $writable eq 'lang-alt' and (not $$tagInfo{LangCode} or
                    $$tagInfo{LangCode} eq 'x-default'))
                {
                    my $saveCap = $capture{$path};
                    for (;;) {
                        my $p = $path;
                        substr($p, $pos, $len) = length($nxt) . $nxt;
                        # increment index in the path of the existing item
                        my $nextCap = $capture{$p};
                        $capture{$p} = $saveCap;
                        last unless $nextCap;
                        $saveCap = $nextCap;
                        ++$nxt;
                    }
                } else {
                    # add to end of list
                    for (;;) {
                        my $try = length($nxt) . $nxt;
                        substr($path, $pos, $len) = $try;
                        last unless $capture{$path};
                        $len = length $try;
                        ++$nxt;
                    }
                }
            }
        }
        # check to see if we want to create this tag
        # (create non-avoided tags in XMP data files by default)
        my $isCreating = (Image::ExifTool::IsCreating($nvHash) or $isStruct or
                          ($preferred and not $$tagInfo{Avoid} and
                            not defined $$nvHash{Shift}));

        # don't add new values unless...
            # ...tag existed before and was deleted, or we added it to a list
        next unless $deleted or defined $added or
            # ...tag didn't exist before and we are creating it
            (not $cap and $isCreating);

        # get list of new values (all done if no new values specified)
        my @newValues = $exifTool->GetNewValues($nvHash) or next;

        # set language attribute for lang-alt lists
        $attrs{'xml:lang'} = $$tagInfo{LangCode} || 'x-default' if $writable eq 'lang-alt';

        # add new value(s) to %capture hash
        my $subIdx;
        for (;;) {
            my $newValue = shift @newValues;
            if ($isStruct) {
                ++$changed if AddNewStruct($exifTool, $tagInfo, \%capture,
                                           $path, $newValue, $$tagInfo{Struct});
            } else {
                $newValue = EscapeXML($newValue);
                if ($$tagInfo{Resource}) {
                    $capture{$path} = [ '', { %attrs, 'rdf:resource' => $newValue } ];
                } else {
                    $capture{$path} = [ $newValue, \%attrs ];
                }
                if ($verbose > 1) {
                    my $grp = $exifTool->GetGroup($tagInfo, 1);
                    $exifTool->VerboseValue("+ $grp:$$tagInfo{Name}", $newValue);
                }
                ++$changed;
                # add rdf:type if necessary
                if ($$tagInfo{StructType}) {
                    AddStructType($exifTool, $$tagInfo{Table}, \%capture, $path);
                }
            }
            last unless @newValues;
            # match last index except for lang-alt items where we want to put each
            # item in a different lang-alt list (so match the 2nd-last for these)
            my $pat = $writable eq 'lang-alt' ? '.* (\d+)(.*? \d+)' : '.* (\d+)';
            $path =~ m/$pat/g or warn("Internal error: no list index for $tag!\n"), next;
            my $idx = $1;
            my $len = length $1;
            my $pos = pos($path) - $len - ($2 ? length $2 : 0);
            # generate unique list sub-indices to store additional values in sequence
            if ($subIdx) {
                $idx = substr($idx, 0, -length($subIdx));   # remove old sub-index
                $subIdx = substr($subIdx, 1) + 1;
                $subIdx = length($subIdx) . $subIdx;
            } else {
                $subIdx = '10';
            }
            substr($path, $pos, $len) = $idx . $subIdx;
        }
    }
    # remove the ExifTool members we created
    delete $exifTool->{XMP_CAPTURE};
    delete $exifTool->{XMP_NS};

    my $maxDataLen = $$dirInfo{MaxDataLen};
    # get DataPt again because it may have been set by ProcessXMP
    $dataPt = $$dirInfo{DataPt};
    # return now if we didn't change anything
    unless ($changed or ($maxDataLen and $dataPt and defined $$dataPt and
        length($$dataPt) > $maxDataLen))
    {
        return undef unless $xmpFile;   # just rewrite original XMP
        Write($$dirInfo{OutFile}, $$dataPt) or return -1 if $dataPt and defined $$dataPt;
        return 1;
    }
#
# write out the new XMP information (serialize it)
#
    # start writing the XMP data
    my $newData = '';
    if ($$exifTool{XMP_NO_XPACKET}) {
        # write BOM if flag is set
        $newData .= "\xef\xbb\xbf" if $$exifTool{XMP_NO_XPACKET} == 2;
    } else {
        $newData .= $pktOpen;
    }
    $newData .= $xmlOpen if $$exifTool{XMP_IS_XML};
    $newData .= $xmpOpen . $rdfOpen;

    # initialize current property path list
    my (@curPropList, @writeLast, @descStart, $extStart);
    my (%nsCur, $prop, $n, $path);
    my @pathList = sort TypeFirst keys %capture;
    # order properties to write large values last if we have a MaxDataLen limit
    if ($maxDataLen and @pathList) {
        my @pathTmp;
        my ($lastProp, $lastNS, $propSize) = ('', '', 0);
        my @pathLoop = (@pathList, ''); # add empty path to end of list for loop
        undef @pathList;
        foreach $path (@pathLoop) {
            $path =~ /^((\w*)[^\/]*)/;  # get path element ($1) and ns ($2)
            if ($1 eq $lastProp) {
                push @pathTmp, $path;   # accumulate all paths with same root
            } else {
                # put in list to write last if recommended or values are too large
                if ($extendedRes{$lastProp} or $extendedRes{$lastNS} or
                    $propSize > $newDescThresh)
                {
                    push @writeLast, @pathTmp;
                } else {
                    push @pathList, @pathTmp;
                }
                last unless $path;      # all done if we hit empty path
                @pathTmp = ( $path );
                ($lastProp, $lastNS, $propSize) = ($1, $2, 0);
            }
            $propSize += length $capture{$path}->[0];
        }
    }

    # write out all properties
    for (;;) {
        my (%nsNew, $newDesc);
        unless (@pathList) {
            last unless @writeLast;
            @pathList = @writeLast;
            undef @writeLast;
            $extStart = length $newData;
            $newDesc = 1;   # start with a new description
        }
        $path = shift @pathList;
        my @propList = split('/',$path); # get property list
        # must open/close rdf:Description too
        unshift @propList, $rdfDesc;
        # make sure we have defined all necessary namespaces
        foreach $prop (@propList) {
            $prop =~ /(.*):/ or next;
            $1 eq 'rdf' and next;       # rdf namespace already defined
            my $uri = $nsUsed{$1};
            unless ($uri) {
                $uri = $nsURI{$1};      # we must have added a namespace
                $uri or $xmpErr = "Undefined XMP namespace: $1", next;
            }
            $nsNew{$1} = $uri;
            # need a new description if any new namespaces
            $newDesc = 1 unless $nsCur{$1};
        }
        my $closeTo = 0;
        if ($newDesc) {
            # look forward to see if we will want to also open other namespaces
            # at this level (this is necessary to keep lists and structures from
            # being broken if a property introduces a new namespace; plus it
            # improves formatting)
            my ($path2, $ns2);
            foreach $path2 (@pathList) {
                my @ns2s = ($path2 =~ m{(?:^|/)([^/]+?):}g);
                my $opening = 0;
                foreach $ns2 (@ns2s) {
                    next if $ns2 eq 'rdf';
                    $nsNew{$ns2} and ++$opening, next;
                    last unless $opening;
                    # get URI for this existing or new namespace
                    my $uri = $nsUsed{$ns2} || $nsURI{$ns2} or last;
                    $nsNew{$ns2} = $uri; # also open this namespace
                }
                last unless $opening;
            }
        } else {
            # find first property where the current path differs from the new path
            for ($closeTo=0; $closeTo<@curPropList; ++$closeTo) {
                last unless $closeTo < @propList;
                last unless $propList[$closeTo] eq $curPropList[$closeTo];
            }
        }
        # close out properties down to the common base path
        while (@curPropList > $closeTo) {
            ($prop = pop @curPropList) =~ s/ .*//;
            $newData .= (' ' x scalar(@curPropList)) . " </$prop>\n";
        }
        if ($newDesc) {
            # save rdf:Description start positions so we can reorder them if necessary
            push @descStart, length($newData) if $maxDataLen;
            # open the new description
            $prop = $rdfDesc;
            %nsCur = %nsNew;            # save current namespaces
            $newData .= "\n <$prop rdf:about='$about'";
            my @ns = sort keys %nsCur;
            # generate et:toolkit attribute if this is an exiftool RDF/XML output file
            if (@ns and $nsCur{$ns[0]} =~ m{^http://ns.exiftool.ca/}) {
                $newData .= "\n  xmlns:et='http://ns.exiftool.ca/1.0/'" .
                            " et:toolkit='Image::ExifTool $Image::ExifTool::VERSION'";
            }
            foreach (@ns) {
                $newData .= "\n  xmlns:$_='$nsCur{$_}'";
            }
            $newData .= ">\n";
            push @curPropList, $prop;
        }
        # loop over all values for this new property
        my ($val, $attrs) = @{$capture{$path}};
        $debug and print "$path = $val\n";
        # open new properties
        my $attr;
        for ($n=@curPropList; $n<$#propList; ++$n) {
            $prop = $propList[$n];
            push @curPropList, $prop;
            # remove list index if it exists
            $prop =~ s/ .*//;
            $attr = '';
            if ($prop ne $rdfDesc and ($propList[$n+1] !~ /^rdf:/ or
                ($propList[$n+1] eq 'rdf:type' and $n+1 == $#propList)))
            {
                # need parseType='Resource' to avoid new 'rdf:Description'
                $attr = " rdf:parseType='Resource'";
            }
            $newData .= (' ' x scalar(@curPropList)) . "<$prop$attr>\n";
        }
        my $prop2 = pop @propList;  # get new property name
        $prop2 =~ s/ .*//;          # remove list index if it exists
        $newData .= (' ' x scalar(@curPropList)) . " <$prop2";
        # write out attributes
        foreach $attr (sort keys %$attrs) {
            my $attrVal = $$attrs{$attr};
            my $quot = ($attrVal =~ /'/) ? '"' : "'";
            $newData .= " $attr=$quot$attrVal$quot";
        }
        $newData .= length $val ? ">$val</$prop2>\n" : "/>\n";
    }
    # close off any open elements
    while ($prop = pop @curPropList) {
        $prop =~ s/ .*//;   # remove list index if it exists
        $newData .= (' ' x scalar(@curPropList)) . " </$prop>\n";
    }
    # limit XMP length and re-arrange if necessary to fit inside specified size
    my $compact = $$dirInfo{Compact} || $exifTool->Options('Compact');
    if ($maxDataLen) {
        # adjust maxDataLen to allow room for closing elements
        $maxDataLen -= length($rdfClose) + length($xmpClose) + length($pktCloseW);
        $extStart or $extStart = length $newData;
        my @rtn = LimitXMPSize($exifTool, \$newData, $maxDataLen, $about, \@descStart, $extStart);
        # return extended XMP information in $dirInfo
        $$dirInfo{ExtendedXMP} = $rtn[0];
        $$dirInfo{ExtendedGUID} = $rtn[1];
        # compact if necessary to fit
        $compact = 1 if length($newData) + 101 * $numPadLines > $maxDataLen;
    }
#
# close out the XMP, clean up, and return our data
#
    $newData .= $rdfClose;
    $newData .= $xmpClose unless $exifTool->{XMP_NO_XMPMETA};

    # remove the ExifTool members we created
    delete $exifTool->{XMP_CAPTURE};
    delete $exifTool->{XMP_NS};
    delete $exifTool->{XMP_NO_XMPMETA};

    # (the XMP standard recommends writing 2k-4k of white space before the
    # packet trailer, with a newline every 100 characters)
    unless ($$exifTool{XMP_NO_XPACKET}) {
        my $pad = (' ' x 100) . "\n";
        if ($$dirInfo{InPlace}) {
            # pad to specified DirLen
            my $len = length($newData) + length($pktCloseW);
            if ($len > $dirLen) {
                $exifTool->Warn('Not enough room to edit XMP in place');
                return undef;
            }
            my $num = int(($dirLen - $len) / length($pad));
            if ($num) {
                $newData .= $pad x $num;
                $len += length($pad) * $num;
            }
            $len < $dirLen and $newData .= (' ' x ($dirLen - $len - 1)) . "\n";
        } elsif (not $compact and not $xmpFile and not $$dirInfo{ReadOnly}) {
            $newData .= $pad x $numPadLines;
        }
        $newData .= ($$dirInfo{ReadOnly} ? $pktCloseR : $pktCloseW);
    }
    # return empty data if no properties exist and this is allowed
    unless (%capture or $xmpFile or $$dirInfo{InPlace} or $$dirInfo{NoDelete}) {
        $newData = '';
    }
    if ($xmpErr) {
        if ($xmpFile) {
            $exifTool->Error($xmpErr);
            return -1;
        }
        $exifTool->Warn($xmpErr);
        return undef;
    }
    $exifTool->{CHANGED} += $changed;
    $debug > 1 and $newData and print $newData,"\n";
    return $newData unless $xmpFile;
    Write($$dirInfo{OutFile}, $newData) or return -1;
    return 1;
}


1; # end

__END__

=head1 NAME

Image::ExifTool::WriteXMP.pl - Write XMP meta information

=head1 SYNOPSIS

These routines are autoloaded by Image::ExifTool::XMP.

=head1 DESCRIPTION

This file contains routines to write XMP metadata.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 SEE ALSO

L<Image::ExifTool::XMP(3pm)|Image::ExifTool::XMP>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut
