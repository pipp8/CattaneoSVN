#------------------------------------------------------------------------------
# File:         Import.pm
#
# Description:  Import CSV and JSON database files
#
# Revisions:    2011-03-05 - P. Harvey Created
#------------------------------------------------------------------------------
package Image::ExifTool::Import;

use strict;
require Exporter;

use vars qw($VERSION @ISA @EXPORT_OK);

$VERSION = '1.01';
@ISA = qw(Exporter);
@EXPORT_OK = qw(ReadCSV ReadJSON);

sub ReadJSONObject($;$);

my %unescapeJSON = ( 't'=>"\t", 'n'=>"\n", 'r'=>"\r" );
my $charset;

#------------------------------------------------------------------------------
# Read CSV file
# Inputs: 0) CSV file name, 1) database hash ref, 2) flag to delete "-" tags
# Returns: undef on success, or error string
# Notes: There are various flavours of CSV, but here we assume that only
#        double quotes are escaped, and they are escaped by doubling them
sub ReadCSV($$;$)
{
    local ($_, $/);
    my ($file, $database, $delDash) = @_;
    my ($buff, @tags, $found, $err);

    open CSVFILE, $file or return "Error opening CSV file '$file'";
    binmode CSVFILE;
    my $raf = new File::RandomAccess(\*CSVFILE);
    # set input record separator by first newline found in the file
    # (safe because first line should contain only tag names)
    while ($raf->Read($buff, 65536)) {
        $buff =~ /(\x0d\x0a|\x0d|\x0a)/ and $/ = $1, last;
    }
    $raf->Seek(0,0);
    while ($raf->ReadLine($buff)) {
        my (@vals, $v, $i, %fileInfo);
        my @toks = split ',', $buff;
        while (@toks) {
            ($v = shift @toks) =~ s/^ +//;  # remove leading spaces
            if ($v =~ s/^"//) {
                # quoted value must end in an odd number of quotes
                while ($v !~ /("+)\s*$/ or not length($1) & 1) {
                    if (@toks) {
                        $v .= ',' . shift @toks;
                    } else {
                        # read another line from the file
                        $raf->ReadLine($buff) or last;
                        @toks = split ',', $buff;
                        last unless @toks;
                        $v .= shift @toks;
                    }
                }
                $v =~ s/"\s*$//;    # remove trailing quote and whitespace
                $v =~ s/""/"/g;     # un-escape quotes
            } else {
                $v =~ s/[ \n\r]+$//;# remove trailing spaces/newlines
            }
            push @vals, $v;
        }
        if (@tags) {
            # save values for each tag
            for ($i=0; $i<@vals and $i<@tags; ++$i) {
                next unless length $vals[$i];   # ignore empty entries
                # delete tag if value (set value to undef) is '-' and -f option is used
                $fileInfo{$tags[$i]} = ($vals[$i] eq '-' and $delDash) ? undef : $vals[$i];
            }
            # figure out the file name to use
            if ($fileInfo{SourceFile}) {
                $$database{$fileInfo{SourceFile}} = \%fileInfo;
                $found = 1;
            }
        } else {
            # the first row should be the tag names
            foreach (@vals) {
                # terminate at first blank tag name (ie. extra comma at end of line)
                last unless length $_;
                /^[-\w]+(:[-\w+]+)?#?$/ or $err = "Invalid tag name '$_'", last;
                push(@tags, $_);
            }
            last if $err;
            @tags or $err = 'No tags found', last;
        }
    }
    close CSVFILE;
    undef $raf;
    $err = 'No SourceFile column' unless $found or $err;
    return $err ? "$err in $file" : undef;
}

#------------------------------------------------------------------------------
# Convert unicode code point to UTF-8
# Inputs: 0) integer Unicode character
# Returns: UTF-8 bytes
sub ToUTF8($)
{
    require Image::ExifTool::Charset;
    return Image::ExifTool::Charset::Recompose(undef, [$_[0]], $charset);
}

#------------------------------------------------------------------------------
# Read JSON object from file
# Inputs: 0) JSON file handle, 1) optional file buffer reference
# Returns: JSON object (scalar, hash ref, or array ref), or undef on EOF or
#          empty object or array (and sets $$buffPt to empty string on EOF)
# Notes: position in buffer is significant
sub ReadJSONObject($;$)
{
    my ($fp, $buffPt) = @_;
    # initialize buffer if necessary
    my ($pos, $readMore, $rtnVal, $tok, $key);
    if ($buffPt) {
        $pos = pos $$buffPt;
    } else {
        my $buff = '';
        $buffPt = \$buff;
        $pos = 0;
    }
Tok: for (;;) {
        if ($pos >= length $$buffPt or $readMore) {
            # read another 64kB and add to unparsed data
            my $offset = length($$buffPt) - $pos;
            $$buffPt = substr($$buffPt, $pos) if $offset;
            read $fp, $$buffPt, 65536, $offset or $$buffPt = '', last;
            $pos = pos($$buffPt) = 0;
            $readMore = 0;
        }
        unless ($tok) {
            # skip white space and find next character
            $$buffPt =~ /(\S)/g or $pos = length($$buffPt), next;
            $tok = $1;
            $pos = pos $$buffPt;
        }
        # see what type of object this is
        if ($tok eq '{') {      # object (hash)
            $rtnVal = { } unless defined $rtnVal;
            for (;;) {
                # read "KEY":"VALUE" pairs
                unless (defined $key) {
                    $key = ReadJSONObject($fp, $buffPt);
                    $pos = pos $$buffPt;
                }
                # ($key may be undef for empty JSON object)
                if (defined $key) {
                    # scan to delimiting ':'
                    $$buffPt =~ /(\S)/g or $readMore = 1, next Tok;
                    $1 eq ':' or return undef;  # error if not a colon
                    my $val = ReadJSONObject($fp, $buffPt);
                    $pos = pos $$buffPt;
                    return undef unless defined $val;
                    $$rtnVal{$key} = $val;
                    undef $key;
                }
                # scan to delimiting ',' or bounding '}'
                $$buffPt =~ /(\S)/g or $readMore = 1, next Tok;
                last if $1 eq '}';          # check for end of object
                $1 eq ',' or return undef;  # error if not a comma
            }
        } elsif ($tok eq '[') { # array
            $rtnVal = [ ] unless defined $rtnVal;
            for (;;) {
                my $item = ReadJSONObject($fp, $buffPt);
                $pos = pos $$buffPt;
                # ($item may be undef for empty array)
                push @$rtnVal, $item if defined $item;
                # scan to delimiting ',' or bounding ']'
                $$buffPt =~ /(\S)/g or $readMore = 1, next Tok;
                last if $1 eq ']';          # check for end of array
                $1 eq ',' or return undef;  # error if not a comma
            }
        } elsif ($tok eq '"') { # quoted string
            for (;;) {
                $$buffPt =~ /(\\*)"/g or $readMore = 1, next Tok;
                last unless length($1) & 1; # check for escaped quote
            }
            $rtnVal = substr($$buffPt, $pos, pos($$buffPt)-$pos-1);
            # unescape characters
            $rtnVal =~ s/\\u([0-9a-f]{4})/ToUTF8(hex $1)/ige;
            $rtnVal =~ s/\\(.)/$unescapeJSON{$1}||$1/sge;
        } elsif ($tok eq ']' or $tok eq '}' or $tok eq ',') {
            # return undef for empty object, array, or list item
            # (empty list item actually not valid JSON)
            pos($$buffPt) = pos($$buffPt) - 1;
        } else {                # number, 'true', 'false', 'null'
            $$buffPt =~ /([\s:,\}\]])/g or $readMore = 1, next;
            pos($$buffPt) = pos($$buffPt) - 1;
            $rtnVal = $tok . substr($$buffPt, $pos, pos($$buffPt)-$pos);
        }
        last;
    }
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Read JSON file
# Inputs: 0) JSON file name, 1) database hash ref, 2) flag to delete "-" tags
#         2) character set
# Returns: undef on success, or error string
sub ReadJSON($$;$$)
{
    local $_;
    my ($file, $database, $delDash, $chset) = @_;

    # initialize character set for converting "\uHHHH" chars
    $charset = $chset || 'UTF8';
    open JSONFILE, $file or return "Error opening JSON file '$file'";
    binmode JSONFILE;
    my $obj = ReadJSONObject(\*JSONFILE);
    close JSONFILE;
    unless (ref $obj eq 'ARRAY') {
        ref $obj eq 'HASH' or return "Format error in JSON file '$file'";
        $obj = [ $obj ];
    }
    my ($info, $found);
    foreach $info (@$obj) {
        next unless ref $info eq 'HASH' and $$info{SourceFile};
        if ($delDash) {
            $$info{$_} eq '-' and $$info{$_} = undef foreach keys %$info;
        }
        $$database{$$info{SourceFile}} = $info;
        $found = 1;
    }
    return $found ? undef : "No SourceFile entries in '$file'";
}


1; # end

__END__

=head1 NAME

Image::ExifTool::Import - Import CSV and JSON database files

=head1 SYNOPSIS

    use Image::ExifTool::Import qw(ReadCSV ReadJSON);

    $err = ReadCSV($csvFile, \%database);

    $err = ReadJSON($jsonfile, \%database);

=head1 DESCRIPTION

This module contains routines for importing tag information from CSV (Comma
Separated Value) and JSON (JavaScript Object Notation) database files.

=head1 EXPORTS

Exports nothing by default, but ReadCSV and ReadJSON may be exported.

=head1 METHODS

=head2 ReadCSV / ReadJSON

Read CSV or JSON file into a database hash.

=over 4

=item Inputs:

0) CSV file name.

1) Hash reference for database object.

2) Optional flag to set '-' values to undef in the database.  (Used for
deleting tags.)

3) [ReadJSON only] Optional character set for converting Unicode escape
sequences in strings.  Defaults to "UTF8".  See the ExifTool Charset option
for a list of valid settings.

=item Return Value:

These functions return an error string, or undef on success and populate the
database hash with entries from the CSV or JSON file.  Entries are keyed
based on the SourceFile column of the CSV or JSON information, and are
stored as hash lookups of tag name/value for each SourceFile.

=back

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 SEE ALSO

L<Image::ExifTool(3pm)|Image::ExifTool>

=cut
