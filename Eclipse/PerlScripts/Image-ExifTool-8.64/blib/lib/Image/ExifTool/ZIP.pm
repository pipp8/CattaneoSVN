#------------------------------------------------------------------------------
# File:         ZIP.pm
#
# Description:  Read ZIP archive meta information
#
# Revisions:    10/28/2007 - P. Harvey Created
#
# References:   1) http://www.pkware.com/documents/casestudies/APPNOTE.TXT
#               2) http://www.cpanforum.com/threads/9046
#               3) http://www.gzip.org/zlib/rfc-gzip.html
#               4) http://DataCompression.info/ArchiveFormats/RAR202.txt
#               5) https://jira.atlassian.com/browse/CONF-21706
#------------------------------------------------------------------------------

package Image::ExifTool::ZIP;

use strict;
use vars qw($VERSION $warnString);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.08';

sub WarnProc($) { $warnString = $_[0]; }

# file types for recognized Open Document "mimetype" values
my %openDocType = (
    'application/vnd.oasis.opendocument.database'     => 'ODB', #5
    'application/vnd.oasis.opendocument.chart'        => 'ODC', #5
    'application/vnd.oasis.opendocument.formula'      => 'ODF', #5
    'application/vnd.oasis.opendocument.graphics'     => 'ODG', #5
    'application/vnd.oasis.opendocument.image'        => 'ODI', #5
    'application/vnd.oasis.opendocument.presentation' => 'ODP',
    'application/vnd.oasis.opendocument.spreadsheet'  => 'ODS',
    'application/vnd.oasis.opendocument.text'         => 'ODT',
);

# ZIP metadata blocks
%Image::ExifTool::ZIP::Main = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    FORMAT => 'int16u',
    NOTES => q{
        The following tags are extracted from ZIP archives.  ExifTool also extracts
        additional meta information from compressed documents inside some ZIP-based
        files such Office Open XML (DOCX, PPTX and XLSX), Open Document (ODB, ODC,
        ODF, ODG, ODI, ODP, ODS and ODT), iWork (KEY, PAGES, NUMBERS), and Capture
        One Enhanced Image Package (EIP).  The ExifTool family 3 groups may be used
        to organize the output by embedded document number (ie. the exiftool C<-g3>
        option).
    },
    2 => 'ZipRequiredVersion',
    3 => {
        Name => 'ZipBitFlag',
        PrintConv => '$val ? sprintf("0x%.4x",$val) : $val',
    },
    4 => {
        Name => 'ZipCompression',
        PrintConv => {
            0 => 'None',
            1 => 'Shrunk',
            2 => 'Reduced with compression factor 1',
            3 => 'Reduced with compression factor 2',
            4 => 'Reduced with compression factor 3',
            5 => 'Reduced with compression factor 4',
            6 => 'Imploded',
            7 => 'Tokenized',
            8 => 'Deflated',
            9 => 'Enhanced Deflate using Deflate64(tm)',
           10 => 'Imploded (old IBM TERSE)',
           12 => 'BZIP2',
           14 => 'LZMA (EFS)',
           18 => 'IBM TERSE (new)',
           19 => 'IBM LZ77 z Architecture (PFS)',
           96 => 'JPEG recompressed', #2
           97 => 'WavPack compressed', #2
           98 => 'PPMd version I, Rev 1',
       },
    },
    5 => {
        Name => 'ZipModifyDate',
        Format => 'int32u',
        Groups => { 2 => 'Time' },
        ValueConv => sub {
            my $val = shift;
            return sprintf('%.4d:%.2d:%.2d %.2d:%.2d:%.2d',
                ($val >> 25) + 1980, # year
                ($val >> 21) & 0x0f, # month
                ($val >> 16) & 0x1f, # day
                ($val >> 11) & 0x1f, # hour
                ($val >> 5)  & 0x3f, # minute
                 $val        & 0x1f  # second
            );
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    7 => { Name => 'ZipCRC', Format => 'int32u', PrintConv => 'sprintf("0x%.8x",$val)' },
    9 => { Name => 'ZipCompressedSize',    Format => 'int32u' },
    11 => { Name => 'ZipUncompressedSize', Format => 'int32u' },
    13 => {
        Name => 'ZipFileNameLength',
        # don't store a tag -- just extract the value for use with ZipFileName
        Hidden => 1,
        RawConv => '$$self{ZipFileNameLength} = $val; undef',
    },
    # 14 => 'ZipExtraFieldLength',
    15 => {
        Name => 'ZipFileName',
        Format => 'string[$$self{ZipFileNameLength}]',
    },
);

# GNU ZIP tags (ref 3)
%Image::ExifTool::ZIP::GZIP = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    NOTES => q{
        These tags are extracted from GZIP (GNU ZIP) archives, but currently only
        for the first file in the archive.
    },
    2 => {
        Name => 'Compression',
        PrintConv => {
            8 => 'Deflated',
        },
    },
    3 => {
        Name => 'Flags',
        PrintConv => { BITMASK => {
            0 => 'Text',
            1 => 'CRC16',
            2 => 'ExtraFields',
            3 => 'FileName',
            4 => 'Comment',
        }},
    },
    4 => {
        Name => 'ModifyDate',
        Format => 'int32u',
        Groups => { 2 => 'Time' },
        ValueConv => 'ConvertUnixTime($val,1)',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    8 => {
        Name => 'ExtraFlags',
        PrintConv => {
            0 => '(none)',
            2 => 'Maximum Compression',
            4 => 'Fastest Algorithm',
        },
    },
    9 => {
        Name => 'OperatingSystem',
        PrintConv => {
            0 => 'FAT filesystem (MS-DOS, OS/2, NT/Win32)',
            1 => 'Amiga',
            2 => 'VMS (or OpenVMS)',
            3 => 'Unix',
            4 => 'VM/CMS',
            5 => 'Atari TOS',
            6 => 'HPFS filesystem (OS/2, NT)',
            7 => 'Macintosh',
            8 => 'Z-System',
            9 => 'CP/M',
            10 => 'TOPS-20',
            11 => 'NTFS filesystem (NT)',
            12 => 'QDOS',
            13 => 'Acorn RISCOS',
            255 => 'unknown',
        },
    },
    10 => 'ArchivedFileName',
    11 => 'Comment',
);

# RAR tags (ref 4)
%Image::ExifTool::ZIP::RAR = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    NOTES => 'These tags are extracted from RAR archive files.',
    0 => {
        Name => 'CompressedSize',
        Format => 'int32u',
    },
    4 => {
        Name => 'UncompressedSize',
        Format => 'int32u',
    },
    8 => {
        Name => 'OperatingSystem',
        PrintConv => {
            0 => 'MS-DOS',
            1 => 'OS/2',
            2 => 'Win32',
            3 => 'Unix',
        },
    },
    13 => {
        Name => 'ModifyDate',
        Format => 'int32u',
        Groups => { 2 => 'Time' },
        ValueConv => sub {
            my $val = shift;
            return sprintf('%.4d:%.2d:%.2d %.2d:%.2d:%.2d',
                ($val >> 25) + 1980, # year
                ($val >> 21) & 0x0f, # month
                ($val >> 16) & 0x1f, # day
                ($val >> 11) & 0x1f, # hour
                ($val >> 5)  & 0x3f, # minute
                 $val        & 0x1f  # second
            );
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    18 => {
        Name => 'PackingMethod',
        PrintHex => 1,
        PrintConv => {
            0x30 => 'Stored',
            0x31 => 'Fastest',
            0x32 => 'Fast',
            0x33 => 'Normal',
            0x34 => 'Good Compression',
            0x35 => 'Best Compression',
        },
    },
    19 => {
        Name => 'FileNameLength',
        Format => 'int16u',
        Hidden => 1,
        RawConv => '$$self{FileNameLength} = $val; undef',
    },
    25 => {
        Name => 'ArchivedFileName',
        Format => 'string[$$self{FileNameLength}]',
    },
);

#------------------------------------------------------------------------------
# Extract information from a RAR file (ref 4)
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid RAR file
sub ProcessRAR($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my ($flags, $buff);

    return 0 unless $raf->Read($buff, 7) and $buff eq "Rar!\x1a\x07\0";

    $exifTool->SetFileType();
    SetByteOrder('II');
    my $tagTablePtr = GetTagTable('Image::ExifTool::ZIP::RAR');
    my $docNum = 0;

    for (;;) {
        # read block header
        $raf->Read($buff, 7) == 7 or last;
        my ($type, $flags, $size) = unpack('xxCvv', $buff);
        $size -= 7;
        if ($flags & 0x8000) {
            $raf->Read($buff, 4) == 4 or last;
            $size += unpack('V',$buff) - 4;
        }
        last if $size < 0;
        next unless $size;  # ignore blocks with no data
        # don't try to read very large blocks unless LargeFileSupport is enabled
        if ($size > 0x80000000 and not $exifTool->Options('LargeFileSupport')) {
            $exifTool->Warn('Large block encountered. Aborting.');
            last;
        }
        # process the block
        if ($type == 0x74) { # file block
            # read maximum 4 KB from a file block
            my $n = $size > 4096 ? 4096 : $size;
            $raf->Read($buff, $n) == $n or last;
            # add compressed size to start of data so we can extract it with the other tags
            $buff = pack('V',$size) . $buff;
            $$exifTool{DOC_NUM} = ++$docNum;
            $exifTool->ProcessDirectory({ DataPt => \$buff }, $tagTablePtr);
            $size -= $n;
        } elsif ($type == 0x75 and $size > 6) { # comment block
            $raf->Read($buff, $size) == $size or last;
            # save comment, only if "Stored" (this is untested)
            if (Get8u(\$buff, 3) == 0x30) {
                $exifTool->FoundTag('Comment', substr($buff, 6));
            }
            next;
        }
        # seek to the start of the next block
        $raf->Seek($size, 1) or last if $size;
    }
    $$exifTool{DOC_NUM} = 0;

    return 1;
}

#------------------------------------------------------------------------------
# Extract information from a GNU ZIP file (ref 3)
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid GZIP file
sub ProcessGZIP($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my ($flags, $buff);

    return 0 unless $raf->Read($buff, 10) and $buff =~ /^\x1f\x8b\x08/;

    $exifTool->SetFileType();
    SetByteOrder('II');

    my $tagTablePtr = GetTagTable('Image::ExifTool::ZIP::GZIP');
    $exifTool->HandleTag($tagTablePtr, 2, Get8u(\$buff, 2));
    $exifTool->HandleTag($tagTablePtr, 3, $flags = Get8u(\$buff, 3));
    $exifTool->HandleTag($tagTablePtr, 4, Get32u(\$buff, 4));
    $exifTool->HandleTag($tagTablePtr, 8, Get8u(\$buff, 8));
    $exifTool->HandleTag($tagTablePtr, 9, Get8u(\$buff, 9));

    # extract file name and comment if they exist
    if ($flags & 0x18) {
        if ($flags & 0x04) {
            # skip extra field
            $raf->Read($buff, 2) == 2 or return 1;
            my $len = Get16u(\$buff, 0);
            $raf->Read($buff, $len) == $len or return 1;
        }
        $raf->Read($buff, 4096) or return 1;
        my $pos = 0;
        my $tagID;
        # loop for ArchivedFileName (10) and Comment (11) tags
        foreach $tagID (10, 11) {
            my $mask = $tagID == 10 ? 0x08 : 0x10;
            next unless $flags & $mask;
            my $end = $buff =~ /\0/g ? pos($buff) - 1 : length($buff);
            # (the doc specifies the string should be ISO 8859-1,
            # but in OS X it seems to be UTF-8, so don't translate
            # it because I could just as easily screw it up)
            my $str = substr($buff, $pos, $end - $pos);
            $exifTool->HandleTag($tagTablePtr, $tagID, $str);
            last if $end >= length $buff;
            $pos = $end + 1;
        }
    }
    return 1;
}

#------------------------------------------------------------------------------
# Call HandleTags for attributes of an Archive::Zip member
# Inputs: 0) ExifTool object ref, 1) member ref, 2) optional tag table ref
sub HandleMember($$;$)
{
    my ($exifTool, $member, $tagTablePtr) = @_;
    $tagTablePtr or  $tagTablePtr = GetTagTable('Image::ExifTool::ZIP::Main');
    $exifTool->HandleTag($tagTablePtr, 2, $member->versionNeededToExtract());
    $exifTool->HandleTag($tagTablePtr, 3, $member->bitFlag());
    $exifTool->HandleTag($tagTablePtr, 4, $member->compressionMethod());
    $exifTool->HandleTag($tagTablePtr, 5, $member->lastModFileDateTime());
    $exifTool->HandleTag($tagTablePtr, 7, $member->crc32());
    $exifTool->HandleTag($tagTablePtr, 9, $member->compressedSize());
    $exifTool->HandleTag($tagTablePtr, 11, $member->uncompressedSize());
    $exifTool->HandleTag($tagTablePtr, 15, $member->fileName());
}

#------------------------------------------------------------------------------
# Extract information from an ZIP file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid ZIP file
sub ProcessZIP($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my ($buff, $buf2, $zip, $docNum);

    return 0 unless $raf->Read($buff, 30) and $buff =~ /^PK\x03\x04/;

    my $tagTablePtr = GetTagTable('Image::ExifTool::ZIP::Main');

    # use Archive::Zip if avilable
    for (;;) {
        unless (eval 'require Archive::Zip' and eval 'require IO::File') {
            if ($$exifTool{FILE_EXT} and $$exifTool{FILE_EXT} ne 'ZIP') {
                $exifTool->Warn("Install Archive::Zip to decode compressed ZIP information");
            }
            last;
        }
        # Archive::Zip requires a seekable IO::File object
        my $fh = $raf->{FILE_PT};
        if ($fh and seek($fh, 0, 0)) {
            unless (eval 'require IO::File') {
                # (this shouldn't happen because IO::File is a prerequisite of Archive::Zip)
                $exifTool->Warn("Install IO::File to decode compressed ZIP information");
                last;
            }
            bless $fh, 'IO::File';  # Archive::Zip expects an IO::File object
        } elsif (eval 'require IO::String') {
            # read the whole file into memory (what else can I do?)
            $raf->Slurp();
            $fh = new IO::String ${$raf->{BUFF_PT}};
        } else {
            my $type = $fh ? 'pipe or socket' : 'scalar reference';
            $exifTool->Warn("Install IO::String to decode compressed ZIP information from a $type");
            last;
        }
        $exifTool->VPrint(1, "  --- using Archive::Zip ---\n");
        $zip = new Archive::Zip;
        # catch all warnings! (Archive::Zip is bad for this)
        local $SIG{'__WARN__'} = \&WarnProc;
        my $status = $zip->readFromFileHandle($fh);
        if ($status) {
            undef $zip;
            my %err = ( 1=>'Stream end error', 3=>'Format error', 4=>'IO error' );
            my $err = $err{$status} || "Error $status";
            $exifTool->Warn("$err reading ZIP file");
            last;
        }
        $$dirInfo{ZIP} = $zip;

        # check for an Office Open file (DOCX, etc)
        # --> read '[Content_Types].xml' to determine the file type
        my ($mime, @members);
        my $cType = $zip->memberNamed('[Content_Types].xml');
        if ($cType) {
            ($buff, $status) = $zip->contents($cType);
            if (not $status and $buff =~ /ContentType\s*=\s*(['"])([^"']+)\.main(\+xml)?\1/) {
                $mime = $2;
            }
        }
        # check for docProps if we couldn't find a MIME type
        $mime or @members = $zip->membersMatching('^docProps/.*\.(xml|XML)$');
        if ($mime or @members) {
            $$dirInfo{MIME} = $mime;
            require Image::ExifTool::OOXML;
            Image::ExifTool::OOXML::ProcessDOCX($exifTool, $dirInfo);
            delete $$dirInfo{MIME};
            last;
        }

        # check for an EIP file
        @members = $zip->membersMatching('^CaptureOne/.*\.(cos|COS)$');
        if (@members) {
            require Image::ExifTool::CaptureOne;
            Image::ExifTool::CaptureOne::ProcessEIP($exifTool, $dirInfo);
            last;
        }

        # check for an iWork file
        @members = $zip->membersMatching('^(index\.(xml|apxl)|QuickLook/Thumbnail\.jpg)$');
        if (@members) {
            require Image::ExifTool::iWork;
            Image::ExifTool::iWork::Process_iWork($exifTool, $dirInfo);
            last;
        }

        # check for an Open Document file
        my $mType = $zip->memberNamed('mimetype');
        if ($mType) {
            ($mime, $status) = $zip->contents($mType);
            unless ($status) {
                chomp $mime;
                if ($openDocType{$mime}) {
                    $exifTool->SetFileType($openDocType{$mime}, $mime);
                    # extract Open Document metadata from "meta.xml"
                    my $meta = $zip->memberNamed('meta.xml');
                    if ($meta) {
                        ($buff, $status) = $zip->contents($meta);
                        unless ($status) {
                            my %dirInfo = (
                                DataPt => \$buff,
                                DirLen => length $buff,
                                DataLen => length $buff,
                            );
                            my $xmpTable = GetTagTable('Image::ExifTool::XMP::Main');
                            $exifTool->ProcessDirectory(\%dirInfo, $xmpTable);
                        }
                    }
                    # extract preview image(s) from "Thumbnails" directory
                    my $type;
                    my %tag = ( jpg => 'PreviewImage', png => 'PreviewPNG' );
                    foreach $type ('jpg', 'png') {
                        my $thumb = $zip->memberNamed("Thumbnails/thumbnail.$type");
                        next unless $thumb;
                        ($buff, $status) = $zip->contents($thumb);
                        $exifTool->FoundTag($tag{$type}, $buff) unless $status;
                    }
                    last;
                }
            }
        }

        # otherwise just extract general ZIP information
        $exifTool->SetFileType();
        @members = $zip->members();
        $docNum = 0;
        my $member;
        foreach $member (@members) {
            $$exifTool{DOC_NUM} = ++$docNum;
            HandleMember($exifTool, $member, $tagTablePtr);
        }
        last;
    }
    # all done if we processed this using Archive::Zip
    if ($zip) {
        delete $$dirInfo{ZIP};
        delete $$exifTool{DOC_NUM};
        return 1;
    }
#
# process the ZIP file by hand (funny, but this seems easier than using Archive::Zip)
#
    $docNum = 0;
    $exifTool->VPrint(1, "  -- processing as binary data --\n");
    $raf->Seek(30, 0);
    $exifTool->SetFileType();
    SetByteOrder('II');

    #  A.  Local file header:
    #  local file header signature     0) 4 bytes  (0x04034b50)
    #  version needed to extract       4) 2 bytes
    #  general purpose bit flag        6) 2 bytes
    #  compression method              8) 2 bytes
    #  last mod file time             10) 2 bytes
    #  last mod file date             12) 2 bytes
    #  crc-32                         14) 4 bytes
    #  compressed size                18) 4 bytes
    #  uncompressed size              22) 4 bytes
    #  file name length               26) 2 bytes
    #  extra field length             28) 2 bytes
    for (;;) {
        my $len = Get16u(\$buff, 26) + Get16u(\$buff, 28);
        $raf->Read($buf2, $len) == $len or last;

        $$exifTool{DOC_NUM} = ++$docNum;
        $buff .= $buf2;
        my %dirInfo = (
            DataPt => \$buff,
            DataPos => $raf->Tell() - 30 - $len,
            DataLen => 30 + $len,
            DirStart => 0,
            DirLen => 30 + $len,
        );
        $exifTool->ProcessDirectory(\%dirInfo, $tagTablePtr);
        my $flags = Get16u(\$buff, 6);
        if ($flags & 0x08) {
            # we don't yet support skipping stream mode data
            # (when this happens, the CRC, compressed size and uncompressed
            #  sizes are set to 0 in the header.  Instead, they are stored
            #  after the compressed data with an optional header of 0x08074b50)
            $exifTool->Warn('Stream mode data encountered, file list may be incomplete');
            last;
        }
        $len = Get32u(\$buff, 18);      # file data length
        $raf->Seek($len, 1) or last;    # skip file data
        $raf->Read($buff, 30) == 30 and $buff =~ /^PK\x03\x04/ or last;
    }
    delete $$exifTool{DOC_NUM};
    return 1;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::ZIP - Read ZIP archive meta information

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to extract meta
information from ZIP, GZIP and RAR archives.  This includes ZIP-based file
types like DOCX, PPTX, XLSX, ODB, ODC, ODF, ODG, ODI, ODP, ODS, ODT and EIP.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.pkware.com/documents/casestudies/APPNOTE.TXT>

=item L<http://www.gzip.org/zlib/rfc-gzip.html>

=item L<http://DataCompression.info/ArchiveFormats/RAR202.txt>

=back

=head1 SEE ALSO

L<Image::ExifTool::TagNames/ZIP Tags>,
L<Image::ExifTool::TagNames/OOXML Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

