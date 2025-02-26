#------------------------------------------------------------------------------
# File:         Ogg.pm
#
# Description:  Read Ogg meta information
#
# Revisions:    2011/07/13 - P. Harvey Created (split from Vorbis.pm)
#
# References:   1) http://www.xiph.org/vorbis/doc/
#               2) http://flac.sourceforge.net/ogg_mapping.html
#               3) http://www.theora.org/doc/Theora.pdf
#------------------------------------------------------------------------------

package Image::ExifTool::Ogg;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.00';

my $MAX_PACKETS = 2;    # maximum packets to scan from each stream at start of file

# Information types recognizedi in Ogg files
%Image::ExifTool::Ogg::Main = (
    NOTES => q{
        ExifTool extracts the following types of information from Ogg files.  See
        L<http://www.xiph.org/vorbis/doc/> for the Ogg specification.
    },
    # (these are for documentation purposes only, and aren't used by the code below)
    vorbis => { SubDirectory => { TagTable => 'Image::ExifTool::Vorbis::Main' } },
    theora => { SubDirectory => { TagTable => 'Image::ExifTool::Theora::Main' } },
    FLAC   => { SubDirectory => { TagTable => 'Image::ExifTool::FLAC::Main' } },
    ID3    => { SubDirectory => { TagTable => 'Image::ExifTool::ID3::Main' } },
);

#------------------------------------------------------------------------------
# Process Ogg packet
# Inputs: 0) ExifTool object ref, 1) data ref
# Returns: 1 on success
sub ProcessPacket($$)
{
    my ($exifTool, $dataPt) = @_;
    my $rtnVal = 0;
    if ($$dataPt =~ /^(.)(vorbis|theora)/s) {
        my ($tag, $type) = (ord($1), ucfirst($2));
        # this is an OGV file if it contains Theora video
        $exifTool->OverrideFileType('OGV') if $type eq 'Theora' and $$exifTool{FILE_TYPE} eq 'OGG';
        my $tagTablePtr = GetTagTable("Image::ExifTool::${type}::Main");
        my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tag);
        return 0 unless $tagInfo and $$tagInfo{SubDirectory};
        my $subdir = $$tagInfo{SubDirectory};
        my %dirInfo = (
            DataPt => $dataPt,
            DirName => $$tagInfo{Name},
            DirStart => 7,
        );
        my $table = GetTagTable($$subdir{TagTable});
        # set group1 so Theoris comments can be distinguised from Vorbis comments
        $$exifTool{SET_GROUP1} = $type if $type eq 'Theora';
        SetByteOrder($$subdir{ByteOrder}) if $$subdir{ByteOrder};
        $rtnVal = $exifTool->ProcessDirectory(\%dirInfo, $table);
        SetByteOrder('II');
        delete $$exifTool{SET_GROUP1};
    }
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Extract information from an Ogg file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid Ogg file
sub ProcessOGG($$)
{
    my ($exifTool, $dirInfo) = @_;

    # must first check for leading/trailing ID3 information
    unless ($exifTool->{DoneID3}) {
        require Image::ExifTool::ID3;
        Image::ExifTool::ID3::ProcessID3($exifTool, $dirInfo) and return 1;
    }
    my $raf = $$dirInfo{RAF};
    my $verbose = $exifTool->Options('Verbose');
    my $out = $exifTool->Options('TextOut');
    my ($success, $page, $packets, $streams, $stream) = (0,0,0,0,'');
    my ($buff, $flag, %val, $numFlac, %streamPage);

    for (;;) {
        # must read ahead to next page to see if it is a continuation
        # (this code would be a lot simpler if the continuation flag
        #  was on the leading instead of the trailing page!)
        if ($raf and $raf->Read($buff, 28) == 28) {
            # validate magic number
            unless ($buff =~ /^OggS/) {
                $success and $exifTool->Warn('Lost synchronization');
                last;
            }
            unless ($success) {
                # set file type and initialize on first page
                $success = 1;
                $exifTool->SetFileType();
                SetByteOrder('II');
            }
            $flag = Get8u(\$buff, 5);       # page flag
            $stream = Get32u(\$buff, 14);   # stream serial number
            if ($flag & 0x02) {
                ++$streams;                 # count start-of-stream pages
                $streamPage{$stream} = $page = 0;
            } else {
                $page = $streamPage{$stream};
            }
            ++$packets unless $flag & 0x01; # keep track of packet count
        } else {
            # all done unless we have to process our last packet
            last unless %val;
            ($stream) = sort keys %val;     # take a stream
            $flag = 0;                      # no continuation
            undef $raf;                     # flag for done reading
        }

        if (defined $numFlac) {
            # stop to process FLAC headers if we hit the end of file
            last unless $raf;
            --$numFlac; # one less header packet to read
        } else {
            # can finally process previous packet from this stream
            # unless this is a continuation page
            if (defined $val{$stream} and not $flag & 0x01) {
                ProcessPacket($exifTool, \$val{$stream});
                delete $val{$stream};
                # only read the first $MAX_PACKETS packets from each stream
                if ($packets > $MAX_PACKETS * $streams or not defined $raf) {
                    last unless %val;   # all done (success!)
                }
            }
            # stop processing Ogg if we have scanned enough packets
            last if $packets > $MAX_PACKETS * $streams and not %val;
        }

        # continue processing the current page
        my $pageNum = Get32u(\$buff, 18);   # page sequence number
        my $nseg = Get8u(\$buff, 26);       # number of segments
        # calculate total data length
        my $dataLen = Get8u(\$buff, 27);
        if ($nseg) {
            $raf->Read($buff, $nseg-1) == $nseg-1 or last;
            my @segs = unpack('C*', $buff);
            # could check that all these (but the last) are 255...
            foreach (@segs) { $dataLen += $_ }
        }
        if (defined $page) {
            if ($page == $pageNum) {
                $streamPage{$stream} = ++$page;
            } else {
                $exifTool->Warn('Missing page(s) in Ogg file');
                undef $page;
                delete $streamPage{$stream};
            }
        }
        # read page data
        $raf->Read($buff, $dataLen) == $dataLen or last;
        if ($verbose > 1) {
            printf $out "Page %d, stream 0x%x, flag 0x%x (%d bytes)\n",
                   $pageNum, $stream, $flag, $dataLen;
            $exifTool->VerboseDump(\$buff, DataPos => $raf->Tell() - $dataLen);
        }
        if (defined $val{$stream}) {
            $val{$stream} .= $buff;     # add this continuation page
        } elsif (not $flag & 0x01) {    # ignore remaining pages of a continued packet
            # ignore the first page of any packet we aren't parsing
            if ($buff =~ /^(.)(vorbis|theora)/s) {
                $val{$stream} = $buff;      # save this page
            } elsif ($buff =~ /^\x7fFLAC..(..)/s) {
                $numFlac = unpack('n',$1);
                $val{$stream} = substr($buff, 9);
            }
        }
        if (defined $numFlac) {
            # stop to process FLAC headers if we have them all
            last if $numFlac <= 0;
        } elsif (defined $val{$stream} and $flag & 0x04) {
            # process Ogg packet now if end-of-stream bit is set
            ProcessPacket($exifTool, \$val{$stream});
            delete $val{$stream};
        }
    }
    if (defined $numFlac and defined $val{$stream}) {
        # process FLAC headers as if it was a complete FLAC file
        require Image::ExifTool::FLAC;
        my %dirInfo = ( RAF => new File::RandomAccess(\$val{$stream}) );
        Image::ExifTool::FLAC::ProcessFLAC($exifTool, \%dirInfo);
    }
    return $success;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::Ogg - Read Ogg meta information

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to extract meta
information from Ogg bitstream container files.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.xiph.org/vorbis/doc/>

=item L<http://flac.sourceforge.net/ogg_mapping.html>

=item L<http://www.theora.org/doc/Theora.pdf>

=back

=head1 SEE ALSO

L<Image::ExifTool::TagNames/Ogg Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

