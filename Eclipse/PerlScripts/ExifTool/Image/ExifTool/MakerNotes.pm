#------------------------------------------------------------------------------
# File:         MakerNotes.pm
#
# Description:  Read and write EXIF maker notes
#
# Revisions:    11/11/2004 - P. Harvey Created
#------------------------------------------------------------------------------

package Image::ExifTool::MakerNotes;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess);
use Image::ExifTool::Exif;

sub ProcessUnknown($$$);
sub ProcessUnknownOrPreview($$$);
sub ProcessCanon($$$);
sub ProcessGE2($$$);
sub WriteUnknownOrPreview($$$);
sub FixLeicaBase($$;$);

$VERSION = '1.69';

my $debug;          # set to 1 to enable debugging code

# conditional list of maker notes
# Notes:
# - This is NOT a normal tag table!
# - All byte orders are now specified because we can now
#   write maker notes into a file with different byte ordering!
# - Put these in alphabetical order to make TagNames documentation nicer.
@Image::ExifTool::MakerNotes::Main = (
    # decide which MakerNotes to use (based on camera make/model)
    {
        Name => 'MakerNoteCanon',
        # (starts with an IFD)
        Condition => '$$self{Make} =~ /^Canon/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Canon::Main',
            ProcessProc => \&ProcessCanon,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteCasio',
        # do negative lookahead assertion just to get tags
        # in a nice order for documentation
        # (starts with an IFD)
        Condition => '$$self{Make}=~/^CASIO/ and $$valPt!~/^(QVC|DCI)\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Casio::Main',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteCasio2',
        # (starts with "QVC\0" [Casio] or "DCI\0" [Concord])
        # (also found in AVI and MOV videos)
        Condition => '$$valPt =~ /^(QVC|DCI)\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Casio::Type2',
            Start => '$valuePtr + 6',
            ByteOrder => 'Unknown',
            FixBase => 1, # necessary for AVI and MOV videos
        },
    },
    {
        # The Fuji maker notes use a structure similar to a self-contained
        # TIFF file, but with "FUJIFILM" instead of the standard TIFF header
        Name => 'MakerNoteFujiFilm',
        # (starts with "FUJIFILM" -- also used by some Leica, Minolta and Sharp models)
        # (GE FujiFilm models start with "GENERALE")
        Condition => '$$valPt =~ /^(FUJIFILM|GENERALE)/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::FujiFilm::Main',
            # there is an 8-byte maker tag (FUJIFILM) we must skip over
            OffsetPt => '$valuePtr+8',
            # the pointers are relative to the subdirectory start
            # (before adding the offsetPt) - PH
            Base => '$start',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteGE',
        Condition => '$$valPt =~ /^GE(\0\0|NIC\0)/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::GE::Main',
            Start => '$valuePtr + 18',
            FixBase => 1,
            AutoFix => 1,
            ByteOrder => 'Unknown',
       },
    },
    {
        Name => 'MakerNoteGE2',
        Condition => '$$valPt =~ /^GE\x0c\0\0\0\x16\0\0\0/',
        # Note: we will get a "Maker notes could not be parsed" warning when writing
        #       these maker notes because they aren't currently supported for writing
        SubDirectory => {
            TagTable => 'Image::ExifTool::FujiFilm::Main',
            ProcessProc => \&ProcessGE2,
            Start => '$valuePtr + 12',
            Base => '$start - 6',
            ByteOrder => 'LittleEndian',
            # hard patch for crazy offsets
            FixOffsets => '$valuePtr -= 210 if $tagID >= 0x1303',
       },
    },
    # (the GE X5 has really messed up EXIF-like maker notes starting with
    #  "GENIC\x0c\0" --> currently not decoded)
    {
        Name => 'MakerNoteHP',  # PhotoSmart 720 (also Vivitar 3705, 3705B and 3715)
        Condition => '$$valPt =~ /^(Hewlett-Packard|Vivitar)/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::HP::Main',
            ProcessProc => \&ProcessUnknown,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteHP2', # PhotoSmart E427
        # (this type of maker note also used by BenQ, Mustek, Sanyo, Traveler and Vivitar)
        Condition => '$$valPt =~ /^610[\0-\4]/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::HP::Type2',
            Start => '$valuePtr',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteHP4', # PhotoSmart M627
        Condition => '$$valPt =~ /^IIII\x04\0/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::HP::Type4',
            Start => '$valuePtr',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteHP6', # PhotoSmart M425, M525 and M527
        Condition => '$$valPt =~ /^IIII\x06\0/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::HP::Type6',
            Start => '$valuePtr',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteISL', # (used in Samsung GX20 samples)
        Condition => '$$valPt =~ /^ISLMAKERNOTE000\0/',
        # this maker notes starts with a TIFF-like header at offset 0x10
        SubDirectory => {
            TagTable => 'Image::ExifTool::Unknown::Main',
            Start => '$valuePtr + 24',
            Base => '$start - 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteJVC',
        Condition => '$$valPt=~/^JVC /',
        SubDirectory => {
            TagTable => 'Image::ExifTool::JVC::Main',
            Start => '$valuePtr + 4',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteJVCText',
        Condition => '$$self{Make}=~/^(JVC|Victor)/ and $$valPt=~/^VER:/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::JVC::Text',
        },
    },
    {
        Name => 'MakerNoteKodak1a',
        Condition => '$$self{Make}=~/^EASTMAN KODAK/ and $$valPt=~/^KDK INFO/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKodak1b',
        Condition => '$$self{Make}=~/^EASTMAN KODAK/ and $$valPt=~/^KDK/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        # used by various Kodak, HP, Pentax and Minolta models
        Name => 'MakerNoteKodak2',
        Condition => q{
            $$valPt =~ /^.{8}Eastman Kodak/s or
            $$valPt =~ /^\x01\0[\0\x01]\0\0\0\x04\0[a-zA-Z]{4}/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type2',
            ByteOrder => 'BigEndian',
        },
    },
    {
        # not much to key on here, but we know the
        # upper byte of the year should be 0x07:
        Name => 'MakerNoteKodak3',
        Condition => q{
            $$self{Make} =~ /^EASTMAN KODAK/ and
            $$valPt =~ /^(?!MM|II).{12}\x07/s and
            $$valPt !~ /^(MM|II|AOC)/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type3',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKodak4',
        Condition => q{
            $$self{Make} =~ /^Eastman Kodak/ and
            $$valPt =~ /^.{41}JPG/s and
            $$valPt !~ /^(MM|II|AOC)/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type4',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKodak5',
        Condition => q{
            $$self{Make}=~/^EASTMAN KODAK/ and
            ($$self{Model}=~/CX(4200|4230|4300|4310|6200|6230)/ or
            # try to pick up similar models we haven't tested yet
            $$valPt=~/^\0(\x1a\x18|\x3a\x08|\x59\xf8|\x14\x80)\0/)
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type5',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKodak6a',
        Condition => q{
            $$self{Make}=~/^EASTMAN KODAK/ and
            $$self{Model}=~/DX3215/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type6',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKodak6b',
        Condition => q{
            $$self{Make}=~/^EASTMAN KODAK/ and
            $$self{Model}=~/DX3700/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type6',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteKodak7',
        # look for something that looks like a serial number
        # (confirmed serial numbers have the format KXXXX########, but we also
        #  accept other strings from sample images that may be serial numbers)
        Condition => q{
            $$self{Make}=~/Kodak/i and
            $$valPt =~ /^[CK][A-Z\d]{3} ?[A-Z\d]{1,2}\d{2}[A-Z\d]\d{4}[ \0]/
        },
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type7',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteKodak8a',
        # IFD-format maker notes: look for reasonable number of
        # entries and check format and count of first IFD entry
        Condition => q{
            $$self{Make}=~/Kodak/i and
            ($$valPt =~ /^\0[\x02-\x7f]..\0[\x01-\x0c]\0\0/s or
             $$valPt =~ /^[\x02-\x7f]\0..[\x01-\x0c]\0..\0\0/s)
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type8',
            ProcessProc => \&ProcessUnknown,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteKodak8b',
        # TIFF-format maker notes
        Condition => q{
            $$self{Make}=~/Kodak/i and
            $$valPt =~ /^(MM\0\x2a\0\0\0\x08|II\x2a\0\x08\0\0\0)/
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type8',
            ProcessProc => \&ProcessUnknown,
            ByteOrder => 'Unknown',
            Start => '$valuePtr + 8',
            Base => '$start - 8',
        },
    },
    {
        Name => 'MakerNoteKodak9',
        # test header and Kodak:DateTimeOriginal
        Condition => '$$valPt =~ m{^IIII[\x02\x03]\0.{14}\d{4}/\d{2}/\d{2} }s',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type9',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNoteKodak10',
        # yet another type of Kodak IFD-format maker notes:
        # this type begins with a byte order indicator,
        # followed immediately by the IFD
        Condition => q{
            $$self{Make}=~/Kodak/i and
            $$valPt =~ /^(MM\0[\x02-\x7f]|II[\x02-\x7f]\0)/
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Type10',
            ProcessProc => \&ProcessUnknown,
            ByteOrder => 'Unknown',
            Start => '$valuePtr + 2',
        },
    },
    {
        Name => 'MakerNoteKodakUnknown',
        Condition => '$$self{Make}=~/Kodak/i and $$valPt!~/^AOC\0/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::Unknown',
            ByteOrder => 'BigEndian',
        },
    },
    {
        Name => 'MakerNoteKyocera',
        # (starts with "KYOCERA")
        Condition => '$$valPt =~ /^KYOCERA/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Unknown::Main',
            Start => '$valuePtr + 22',
            Base => '$start + 2',
            EntryBased => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteMinolta',
        Condition => q{
            $$self{Make}=~/^(Konica Minolta|Minolta)/i and
            $$valPt !~ /^(MINOL|CAMER|MLY0|KC|\+M\+M|\xd7)/
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Minolta::Main',
            ByteOrder => 'Unknown',
        },
    },
    {
        # the DiMAGE E323 (MINOL) and E500 (CAMER), and some models
        # of Mustek, Pentax, Ricoh and Vivitar (CAMER).
        Name => 'MakerNoteMinolta2',
        Condition => '$$valPt =~ /^(MINOL|CAMER)\0/ and $$self{OlympusCAMER} = 1',
        SubDirectory => {
            # these models use Olympus tags in the range 0x200-0x221 plus 0xf00
            TagTable => 'Image::ExifTool::Olympus::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        # /^MLY0/ - DiMAGE G400, G500, G530, G600
        # /^KC/   - Revio KD-420Z, DiMAGE E203
        # /^+M+M/ - DiMAGE E201
        # /^\xd7/ - DiMAGE RD3000
        Name => 'MakerNoteMinolta3',
        Condition => '$$self{Make} =~ /^(Konica Minolta|Minolta)/i',
        Binary => 1,
        Notes => 'not EXIF-based',
    },
    {
        # this maker notes starts with a standard TIFF header at offset 0x0a
        Name => 'MakerNoteNikon',
        Condition => '$$self{Make}=~/^NIKON/i and $$valPt=~/^Nikon\x00\x02/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Nikon::Main',
            Start => '$valuePtr + 18',
            Base => '$start - 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        # older Nikon maker notes
        Name => 'MakerNoteNikon2',
        Condition => '$$self{Make}=~/^NIKON/ and $$valPt=~/^Nikon\x00\x01/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Nikon::Type2',
            Start => '$valuePtr + 8',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        # headerless Nikon maker notes
        Name => 'MakerNoteNikon3',
        Condition => '$$self{Make}=~/^NIKON/i',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Nikon::Main',
            ByteOrder => 'Unknown', # most are little-endian, but D1 is big
        },
    },
    {
        Name => 'MakerNoteOlympus',
        # (if Make is 'SEIKO EPSON CORP.', starts with "EPSON\0")
        # (if Make is 'OLYMPUS OPTICAL CO.,LTD' or 'OLYMPUS CORPORATION',
        #  starts with "OLYMP\0")
        Condition => '$$valPt =~ /^(OLYMP|EPSON)\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Olympus::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteOlympus2',
        # new Olympus maker notes start with "OLYMPUS\0"
        Condition => '$$valPt =~ /^OLYMPUS\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Olympus::Main',
            Start => '$valuePtr + 12',
            Base => '$start - 12',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica',
        # (starts with "LEICA\0\0\0")
        Condition => '$$self{Make} eq "LEICA"',
        SubDirectory => {
            # many Leica models use the same format as Panasonic
            TagTable => 'Image::ExifTool::Panasonic::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica2', # used by the M8
        # (starts with "LEICA\0\0\0")
        Condition => '$$self{Make} =~ /^Leica Camera AG/ and $$valPt =~ /^LEICA\0\0\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Leica2',
            # (the offset base is different in JPEG and DNG images, but we
            # can copy makernotes from one to the other, so we need special
            # logic to decide which base to apply)
            ProcessProc => \&FixLeicaBase,
            Start => '$valuePtr + 8',
            Base => '$start', # (- 8 for DNG images!)
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica3', # used by the R8 and R9
        # (starts with IFD)
        Condition => '$$self{Make} =~ /^Leica Camera AG/ and $$valPt !~ /^LEICA/ and $$self{Model} ne "S2"',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Leica3',
            Start => '$valuePtr',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica4', # used by the M9
        # (M9 starts with "LEICA0\x03\0")
        Condition => '$$self{Make} =~ /^Leica Camera AG/ and $$valPt =~ /^LEICA0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Leica4',
            Start => '$valuePtr + 8',
            Base => '$start - 8', # (yay! Leica fixed the M8 problem)
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica5', # used by the X1
        # (X1 starts with "LEICA\0\x01\0", Make is "LEICA CAMERA AG")
        Condition => '$$valPt =~ /^LEICA\0\x01\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Leica5',
            Start => '$valuePtr + 8',
            Base => '$start - 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteLeica6', # used by the S2 (CAUTION: this tag name is special cased in the code)
        # (S2 starts with "LEICA\0\x02\xff", Make is "LEICA CAMERA AG",
        #  but maker notes aren't loaded at the time this is tested)
        Condition => '$$self{Model} eq "S2"',
        DataTag => 'LeicaTrailer',  # (generates fixup name for this tag)
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Leica6',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
            # NOTE: Leica uses absolute file offsets when this maker note is stored
            # as a JPEG trailer -- this case is handled by ProcessLeicaTrailer in
            # Panasonic.pm, and any "Base" defined here is ignored for this case.
            # ExifTool may also create S2 maker notes inside the APP1 segment when
            # copying from other files, and for this the normal EXIF offsets are used,
            # Base should not be defined!
        },
    },
    {
        Name => 'MakerNotePanasonic',
        # (starts with "Panasonic\0")
        Condition => '$$valPt=~/^Panasonic/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Main',
            Start => '$valuePtr + 12',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePanasonic2',
        # (starts with "Panasonic\0")
        Condition => '$$self{Make}=~/^Panasonic/ and $$valPt=~/^MKE/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Panasonic::Type2',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNotePentax',
        # (starts with "AOC\0", but so does MakerNotePentax3)
        # (also used by some Samsung models)
        Condition => q{
            $$valPt=~/^AOC\0/ and
            $$self{Model} !~ /^PENTAX Optio ?[34]30RS\s*$/
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::Main',
            # process as Unknown maker notes because the start offset and
            # byte ordering are so variable
            ProcessProc => \&ProcessUnknown,
            # offsets can be totally whacky for Pentax maker notes,
            # so attempt to fix the offset base if possible
            FixBase => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePentax2',
        # (starts with an IFD)
        # Casio-like maker notes used only by the Optio 330 and 430
        Condition => '$$self{Make}=~/^Asahi/ and $$valPt!~/^AOC\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::Type2',
            ProcessProc => \&ProcessUnknown,
            FixBase => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePentax3',
        # (starts with "AOC\0", like the more common Pentax maker notes)
        # Casio maker notes used only by the Optio 330RS and 430RS
        Condition => '$$self{Make}=~/^Asahi/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Casio::Type2',
            ProcessProc => \&ProcessUnknown,
            FixBase => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePentax4',
        # (starts with 3 or 4 digits)
        # HP2-like text-based maker notes used by Optio E20
        Condition => '$$self{Make}=~/^PENTAX/ and $$valPt=~/^\d{3}/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::Type4',
            Start => '$valuePtr',
            ByteOrder => 'LittleEndian',
        },
    },
    {
        Name => 'MakerNotePentax5',
        # (starts with "PENTAX \0")
        Condition => '$$valPt=~/^PENTAX \0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::Main',
            Start => '$valuePtr + 10',
            Base => '$start - 10',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePentax6',
        # (starts with "S1\0\0\0\0\0\0\x0c\0\0\0")
        Condition => '$$valPt=~/^S1\0{6}\x0c\0{3}/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::S1',
            Start => '$valuePtr + 12',
            Base => '$start - 12',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNotePhaseOne',
        # Starts with: 'IIIITwaR' or 'IIIICwaR' (have seen both written by P25)
        # (have also seen code which expects 'MMMMRawT')
        Condition => q{
            return undef unless $$valPt =~ /^(IIII.waR|MMMMRaw.)/s;
            $self->OverrideFileType($$self{TIFF_TYPE} = 'IIQ') if $count > 1000000;
            return 1;
        },
        NotIFD => 1,
        Binary => 1,
        PutFirst => 1, # place immediately after TIFF header
        Notes => 'the raw image data in PhaseOne IIQ images',
    },
    {
        Name => 'MakerNoteReconyx',
        Condition => '$$valPt =~ /^\x01\xf1[\x02\x03]\x00/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Reconyx::Main',
            ByteOrder => 'Little-endian',
        },
    },
    {
        Name => 'MakerNoteRicoh',
        # (my test R50 image starts with "      \x02\x01" - PH)
        Condition => '$$self{Make}=~/^RICOH/ and $$valPt=~/^(Ricoh|      )/i',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Ricoh::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteRicohText',
        Condition => '$$self{Make}=~/^RICOH/',
        NotIFD => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Ricoh::Text',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSamsung1a',
        # Samsung STMN maker notes WITHOUT PreviewImage
        Condition => '$$valPt =~ /^STMN\d{3}.\0{4}/s',
        Binary => 1,
        Notes => 'Samsung "STMN" maker notes without PreviewImage',
    },
    {
        Name => 'MakerNoteSamsung1b',
        # Samsung STMN maker notes WITH PreviewImage
        Condition => '$$valPt =~ /^STMN\d{3}/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Samsung::Type1',
        },
    },
    {
        Name => 'MakerNoteSamsung2',
        # Samsung EXIF-format maker notes
        Condition => q{
            $$self{Make} eq 'SAMSUNG' and ($$self{TIFF_TYPE} eq 'SRW' or
            $$valPt=~/^(\0.\0\x01\0\x07\0{3}\x04|.\0\x01\0\x07\0\x04\0{3})0100/s)
        },
        SubDirectory => {
            TagTable => 'Image::ExifTool::Samsung::Type2',
            # Samsung is very inconsistent here, and uses absolute offsets for some
            # models and relative offsets for others, so process as Unknown
            ProcessProc => \&ProcessUnknown,
            FixBase => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSanyo',
        # (starts with "SANYO\0")
        Condition => '$$self{Make}=~/^SANYO/ and $$self{Model}!~/^(C4|J\d|S\d)\b/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sanyo::Main',
            Validate => '$val =~ /^SANYO/',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSanyoC4',
        # The C4 offsets are wrong by 12, so they must be fixed
        Condition => '$$self{Make}=~/^SANYO/ and $$self{Model}=~/^C4\b/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sanyo::Main',
            Validate => '$val =~ /^SANYO/',
            Start => '$valuePtr + 8',
            FixBase => 1,
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSanyoPatch',
        # The J1, J2, J4, S1, S3 and S4 offsets are completely screwy
        Condition => '$$self{Make}=~/^SANYO/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sanyo::Main',
            Validate => '$val =~ /^SANYO/',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
            FixOffsets => 'Image::ExifTool::Sanyo::FixOffsets($valuePtr, $valEnd, $size, $tagID, $wFlag)',
        },
    },
    {
        Name => 'MakerNoteSigma',
        # (starts with "SIGMA\0")
        Condition => '$$self{Make}=~/^(SIGMA|FOVEON)/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sigma::Main',
            Validate => '$val =~ /^(SIGMA|FOVEON)/',
            Start => '$valuePtr + 10',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSony',
        # (starts with "SONY DSC \0" or "SONY CAM \0")
        Condition => '$$self{Make}=~/^SONY/ and $$valPt=~/^SONY (DSC|CAM)/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sony::Main',
            Start => '$valuePtr + 12',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSony2',
        # (starts with "SONY PI\0" -- DSC-S650/S700/S750)
        Condition => '$$self{Make}=~/^SONY/ and $$valPt=~/^SONY PI\0/ and $$self{OlympusCAMER}=1',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Olympus::Main',
            Start => '$valuePtr + 12',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSony3',
        # (starts with "PREMI\0" -- DSC-S45/S500)
        Condition => '$$self{Make}=~/^SONY/ and $$valPt=~/^(PREMI)\0/ and $$self{OlympusCAMER}=1',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Olympus::Main',
            Start => '$valuePtr + 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSony4', # used in SR2 and ARW images
        Condition => '$$self{Make}=~/^SONY/ and $$valPt!~/^\x01\x00/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sony::Main',
            Start => '$valuePtr',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSonyEricsson',
        Condition => '$$valPt =~ /^SEMC MS\0/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sony::Ericsson',
            Start => '$valuePtr + 20',
            Base => '$start - 8',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteSonySRF',
        Condition => '$$self{Make}=~/^SONY/',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Sony::SRF',
            Start => '$valuePtr',
            ByteOrder => 'Unknown',
        },
    },
    {
        Name => 'MakerNoteUnknownText',
        Condition => '$$valPt =~ /^[\x09\x0d\x0a\x20-\x7e]+\0*$/',
        Notes => 'unknown text-based maker notes',
        # show as binary if it is too long
        ValueConv => 'length($val) > 64 ? \$val : $val',
        ValueConvInv => '$val',
    },
    {
        Name => 'MakerNoteUnknown',
        PossiblePreview => 1,
        SubDirectory => {
            TagTable => 'Image::ExifTool::Unknown::Main',
            ProcessProc => \&ProcessUnknownOrPreview,
            WriteProc => \&WriteUnknownOrPreview,
            ByteOrder => 'Unknown',
            FixBase => 2,
       },
    },
);

# insert writable properties so we can write our maker notes
my $tagInfo;
foreach $tagInfo (@Image::ExifTool::MakerNotes::Main) {
    $$tagInfo{Writable} = 'undef';
    $$tagInfo{Format} = 'undef', # (make sure we don't convert this when reading)
    $$tagInfo{WriteGroup} = 'ExifIFD';
    $$tagInfo{Groups} = { 1 => 'MakerNotes' };
    next unless $$tagInfo{SubDirectory};
    # make all SubDirectory tags block-writable
    $$tagInfo{Binary} = 1,
    $$tagInfo{MakerNotes} = 1;
}

#------------------------------------------------------------------------------
# Get normal offset of value data from end of maker note IFD
# Inputs: 0) ExifTool object reference
# Returns: Array: 0) relative flag (undef for no change)
#                 1) normal offset from end of IFD to first value (empty if unknown)
#                 2-N) other possible offsets used by some models
# Notes: Directory size should be validated before calling this routine
sub GetMakerNoteOffset($)
{
    my $exifTool = shift;
    # figure out where we expect the value data based on camera type
    my $make = $exifTool->{Make};
    my $model = $exifTool->{Model};
    my ($relative, @offsets);

    # normally value data starts 4 bytes after end of directory, so this is the default.
    # offsets of 0 and 4 are always allowed even if not specified,
    # but the first offset specified is the one used when writing
    if ($make =~ /^Canon/) {
        push @offsets, ($model =~ /\b(20D|350D|REBEL XT|Kiss Digital N)\b/) ? 6 : 4;
        # some Canon models (FV-M30, Optura50, Optura60) leave 24 unused bytes
        # at the end of the IFD (2 spare IFD entries?)
        push @offsets, 28 if $model =~ /\b(FV\b|OPTURA)/;
        # some Canon PowerShot models leave 12 unused bytes
        push @offsets, 16 if $model =~ /(PowerShot|IXUS|IXY)/;
    } elsif ($make =~ /^CASIO/) {
        # Casio AVI and MOV images use no padding, and their JPEG's use 4,
        # except some models like the EX-S770,Z65,Z70,Z75 and Z700 which use 16,
        # and the EX-Z35 which uses 2 (grrrr...)
        push @offsets, $$exifTool{FILE_TYPE} =~ /^(RIFF|MOV)$/ ? 0 : (4, 16, 2);
    } elsif ($make =~ /^(General Imaging Co.|GEDSC IMAGING CORP.)/i) {
        push @offsets, 0;
    } elsif ($make =~ /^KYOCERA/) {
        push @offsets, 12;
    } elsif ($make =~ /^Leica Camera AG/) {
        if ($model eq 'S2') {
            # lots of empty space before first value in S2 images
            push @offsets, 4, ($$exifTool{FILE_TYPE} eq 'JPEG' ? 286 : 274);
        } elsif ($model =~ /^(R8|R9|M8)\b/) {
            push @offsets, 6;
        } else {
            push @offsets, 4;
        }
    } elsif ($make =~ /^OLYMPUS/ and $model =~ /^E-(1|300|330)\b/) {
        push @offsets, 16;
    } elsif ($make =~ /^OLYMPUS/ and
        # these Olympus models are just weird
        $model =~ /^(C2500L|C-1Z?|C-5000Z|X-2|C720UZ|C725UZ|C150|C2Z|E-10|E-20|FerrariMODEL2003|u20D|u10D)\b/)
    {
        # no expected offset --> determine offset empirically via FixBase()
    } elsif ($make =~ /^(Panasonic|JVC)\b/) {
        push @offsets, 0;
    } elsif ($make =~ /^SONY/) {
        # DSLR and "PREMI" models use an offset of 4
        if ($model =~ /DSLR/ or $$exifTool{OlympusCAMER}) {
            push @offsets, 4;
        } else {
            push @offsets, 0;
        }
    } elsif ($make eq 'FUJIFILM') {
        # some models have offset of 6, so allow that too (A345,A350,A360,A370)
        push @offsets, 4, 6;
    } elsif ($make =~ /^TOSHIBA/) {
        # similar to Canon, can also have 24 bytes of padding
        push @offsets, 0, 24;
    } elsif ($make =~ /^PENTAX/) {
        push @offsets, 4;
        # the Pentax addressing mode is determined automatically, but
        # sometimes the algorithm gets it wrong, but Pentax always uses
        # absolute addressing, so force it to be absolute
        $relative = 0;
    } elsif ($make =~ /^Konica Minolta/i) {
        # patch for DiMAGE X50, Xg, Z2 and Z10
        push @offsets, 4, -16;
    } elsif ($make =~ /^Minolta/) {
        # patch for DiMAGE 7, X20 and Z1
        push @offsets, 4, -8, -12;
    } else {
        push @offsets, 4;   # the normal offset
    }
    return ($relative, @offsets);
}

#------------------------------------------------------------------------------
# Get hash of value offsets / block sizes
# Inputs: 0) Data pointer, 1) offset to start of directory,
#         2) hash ref to return value pointers based on tag ID
# Returns: 0) hash reference: keys are offsets, values are block sizes
#          1) same thing, but with keys adjusted for value-based offsets
# Notes: Directory size should be validated before calling this routine
# - calculates MIN and MAX offsets in entry-based hash
sub GetValueBlocks($$;$)
{
    my ($dataPt, $dirStart, $tagPtr) = @_;
    my $numEntries = Get16u($dataPt, $dirStart);
    my ($index, $valPtr, %valBlock, %valBlkAdj, $end);
    for ($index=0; $index<$numEntries; ++$index) {
        my $entry = $dirStart + 2 + 12 * $index;
        my $format = Get16u($dataPt, $entry+2);
        last if $format < 1 or $format > 13;
        my $count = Get32u($dataPt, $entry+4);
        my $size = $count * $Image::ExifTool::Exif::formatSize[$format];
        next if $size <= 4;
        $valPtr = Get32u($dataPt, $entry+8);
        $tagPtr and $$tagPtr{Get16u($dataPt, $entry)} = $valPtr;
        # save location and size of longest block at this offset
        unless (defined $valBlock{$valPtr} and $valBlock{$valPtr} > $size) {
            $valBlock{$valPtr} = $size;
        }
        # adjust for case of value-based offsets
        $valPtr += 12 * $index;
        unless (defined $valBlkAdj{$valPtr} and $valBlkAdj{$valPtr} > $size) {
            $valBlkAdj{$valPtr} = $size;
            my $end = $valPtr + $size;
            if (defined $valBlkAdj{MIN}) {
                # save minimum only if it has a value of 12 or greater
                $valBlkAdj{MIN} = $valPtr if $valBlkAdj{MIN} < 12 or $valBlkAdj{MIN} > $valPtr;
                $valBlkAdj{MAX} = $end if $valBlkAdj{MAX} > $end;
            } else {
                $valBlkAdj{MIN} = $valPtr;
                $valBlkAdj{MAX} = $end;
            }
        }
    }
    return(\%valBlock, \%valBlkAdj);
}

#------------------------------------------------------------------------------
# Fix base for value offsets in maker notes IFD (if necessary)
# Inputs: 0) ExifTool object ref, 1) DirInfo hash ref
# Return: amount of base shift (and $dirInfo Base and DataPos are updated,
#         FixedBy is set if offsets fixed, and Relative or EntryBased may be set)
sub FixBase($$)
{
    local $_;
    my ($exifTool, $dirInfo) = @_;
    # don't fix base if fixing offsets individually or if we don't want to fix them
    return 0 if $$dirInfo{FixOffsets} or $$dirInfo{NoFixBase};

    my $dataPt = $$dirInfo{DataPt};
    my $dataPos = $$dirInfo{DataPos};
    my $dirStart = $$dirInfo{DirStart} || 0;
    my $entryBased = $$dirInfo{EntryBased};
    my $dirName = $$dirInfo{DirName};
    my $fixBase = $exifTool->Options('FixBase');
    my $setBase = (defined $fixBase and $fixBase ne '') ? 1 : 0;
    my ($fix, $fixedBy, %tagPtr);

    # get hash of value block positions
    my ($valBlock, $valBlkAdj) = GetValueBlocks($dataPt, $dirStart, \%tagPtr);
    return 0 unless %$valBlock;
    # get sorted list of value offsets
    my @valPtrs = sort { $a <=> $b } keys %$valBlock;
#
# handle special case of Canon maker notes with TIFF footer containing original offset
#
    if ($$exifTool{Make} =~ /^Canon/ and $$dirInfo{DirLen} > 8) {
        my $footerPos = $dirStart + $$dirInfo{DirLen} - 8;
        my $footer = substr($$dataPt, $footerPos, 8);
        if ($footer =~ /^(II\x2a\0|MM\0\x2a)/ and  # check for TIFF footer
            substr($footer,0,2) eq GetByteOrder()) # validate byte ordering
        {
            my $oldOffset = Get32u(\$footer, 4);
            my $newOffset = $dirStart + $dataPos;
            if ($setBase) {
                $fix = $fixBase;
            } else {
                $fix = $newOffset - $oldOffset;
                return 0 unless $fix;
                # Picasa and ACDSee have a bug where they update other offsets without
                # updating the TIFF footer (PH - 2009/02/25), so test for this case:
                # validate Canon maker note footer fix by checking offset of last value
                my $maxPt = $valPtrs[-1] + $$valBlock{$valPtrs[-1]};
                # compare to end of maker notes, taking 8-byte footer into account
                my $endDiff = $dirStart + $$dirInfo{DirLen} - ($maxPt - $dataPos) - 8;
                # ignore footer offset only if end difference is exactly correct
                # (allow for possible padding byte, although I have never seen this)
                if (not $endDiff or $endDiff == 1) {
                    $exifTool->Warn('Canon maker note footer may be invalid (ignored)',1);
                    return 0;
                }
            }
            $exifTool->Warn("Adjusted $dirName base by $fix",1);
            $$dirInfo{FixedBy} = $fix;
            $$dirInfo{Base} += $fix;
            $$dirInfo{DataPos} -= $fix;
            return $fix;
        }
    }
#
# analyze value offsets to see if they need fixing.  The first task is to determine
# the minimum valid offset used (this is tricky, because we have to weed out bad
# offsets written by some cameras)
#
    my $minPt = $$dirInfo{MinOffset} = $valPtrs[0]; # if life were simple, this would be it
    my $ifdLen = 2 + 12 * Get16u($$dirInfo{DataPt}, $dirStart);
    my $ifdEnd = $dirStart + $ifdLen;
    my ($relative, @offsets) = GetMakerNoteOffset($exifTool);
    my $makeDiff = $offsets[0];
    my $verbose = $exifTool->Options('Verbose');
    my ($diff, $shift);

    # calculate expected minimum value offset
    my $expected = $dataPos + $ifdEnd + (defined $makeDiff ? $makeDiff : 4);
    $debug and print "$expected expected\n";

    # zero our counters
    my ($countNeg12, $countZero, $countOverlap) = (0, 0, 0);
    my ($valPtr, $last);
    foreach $valPtr (@valPtrs) {
        printf("%d - %d (%d)\n", $valPtr, $valPtr + $$valBlock{$valPtr},
               $valPtr - ($last || 0)) if $debug;
        if (defined $last) {
            my $gap = $valPtr - $last;
            if ($gap == 0 or $gap == 1) {
                ++$countZero;
            } elsif ($gap == -12 and not $entryBased) {
                # you get this when value offsets are relative to the IFD entry
                ++$countNeg12;
            } elsif ($gap < 0) {
                # any other negative difference indicates overlapping values
                ++$countOverlap if $valPtr; # (but ignore zero value pointers)
            } elsif ($gap >= $ifdLen) {
                # ignore previous minimum if we took a jump to near the expected value
                # (some values can be stored before the IFD)
                $minPt = $valPtr if abs($valPtr - $expected) <= 4;
            }
            # an offset less than 12 is surely garbage, so ignore it
            $minPt = $valPtr if $minPt < 12;
        }
        $last = $valPtr + $$valBlock{$valPtr};
    }
    # could this IFD be using entry-based offsets?
    if ((($countNeg12 > $countZero and $$valBlkAdj{MIN} >= $ifdLen - 2) or
         ($$valBlkAdj{MIN} == $ifdLen - 2 or $$valBlkAdj{MIN} == $ifdLen + 2)
        ) and $$valBlkAdj{MAX} <= $$dirInfo{DirLen}-2)
    {
        # looks like these offsets are entry-based, so use the offsets
        # which have been correcting for individual entry position
        $entryBased = 1;
        $verbose and $exifTool->Warn("$dirName offsets are entry-based",1);
    } else {
        # calculate offset difference from end of IFD to first value
        $diff = ($minPt - $dataPos) - $ifdEnd;
        $shift = 0;
        $countOverlap and $exifTool->Warn("Overlapping $dirName values",1);
        if ($entryBased) {
            $exifTool->Warn("$dirName offsets do NOT look entry-based",1);
            undef $entryBased;
            undef $relative;
        }
        # use PrintIM tag to do special check for correct absolute offsets
        if ($tagPtr{0xe00}) {
            my $ptr = $tagPtr{0xe00} - $dataPos;
            return 0 if $ptr > 0 and $ptr <= length($$dataPt) - 8 and
                        substr($$dataPt, $ptr, 8) eq "PrintIM\0";
        }
        # allow a range of reasonable differences for Unknown maker notes
        if ($$dirInfo{FixBase} and $$dirInfo{FixBase} == 2) {
            return 0 if $diff >=0 and $diff <= 24;
        }
        # (used for testing to extract differences)
        # $exifTool->FoundTag('Diff', $diff);
    }
#
# handle entry-based offsets
#
    if ($entryBased) {
        $debug and print "--> entry-based!\n";
        # most of my entry-based samples have first value immediately
        # after last IFD entry (ie. no padding or next IFD pointer)
        $makeDiff = 0;
        push @offsets, 4;   # but some do have a next IFD pointer
        # corrected entry-based offsets are relative to start of first entry
        my $expected = 12 * Get16u($$dirInfo{DataPt}, $dirStart);
        $diff = $$valBlkAdj{MIN} - $expected;
        # set up directory to read values with entry-based offsets
        # (ignore everything and set base to start of first entry)
        $shift = $dataPos + $dirStart + 2;
        $$dirInfo{Base} += $shift;
        $$dirInfo{DataPos} -= $shift;
        $$dirInfo{EntryBased} = 1;
        $$dirInfo{Relative} = 1;    # entry-based offsets are relative
        delete $$dirInfo{FixBase};  # no automatic base fix
        undef $fixBase unless $setBase;
    }
#
# return without doing shift if offsets look OK
#
    unless ($setBase) {
        # don't try to fix offsets for whacky cameras
        return $shift unless defined $makeDiff;
        # normal value data starts 4 bytes after IFD, but allow 0 or 4...
        return $shift if $diff == 0 or $diff == 4;
        # also check for allowed make-specific differences
        foreach (@offsets) {
            return $shift if $diff == $_;
        }
    }
#
# apply the fix, or issue a warning
#
    # use default padding of 4 bytes unless already specified
    $makeDiff = 4 unless defined $makeDiff;
    $fix = $makeDiff - $diff;   # assume standard diff for this make

    if ($$dirInfo{FixBase}) {
        # set flag if offsets are relative (base is at or above directory start)
        if ($dataPos - $fix + $dirStart <= 0) {
            $$dirInfo{Relative} = (defined $relative) ? $relative : 1;
        }
        if ($setBase) {
            $fixedBy = $fixBase;
            $fix += $fixBase;
        }
    } elsif (defined $fixBase) {
        $fix = $fixBase if $fixBase ne '';
        $fixedBy = $fix;
    } else {
        # print warning unless difference looks reasonable
        if ($diff < 0 or $diff > 16 or ($diff & 0x01)) {
            $exifTool->Warn("Possibly incorrect maker notes offsets (fix by $fix?)",1);
        }
        # don't do the fix (but we already adjusted base if entry-based)
        return $shift;
    }
    if (defined $fixedBy) {
        $exifTool->Warn("Adjusted $dirName base by $fixedBy",1);
        $$dirInfo{FixedBy} = $fixedBy;
    }
    $$dirInfo{Base} += $fix;
    $$dirInfo{DataPos} -= $fix;
    return $fix + $shift;
}

#------------------------------------------------------------------------------
# Find start of IFD in unknown maker notes
# Inputs: 0) reference to directory information
# Returns: offset to IFD on success, undefined otherwise
# - dirInfo may contain TagInfo reference for tag associated with directory
# - on success, updates DirStart, DirLen, Base and DataPos in dirInfo
# - also sets Relative flag in dirInfo if offsets are relative to IFD
# Note: Changes byte ordering!
sub LocateIFD($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $dirStart = $$dirInfo{DirStart} || 0;
    # (ignore MakerNotes DirLen since sometimes this is incorrect)
    my $size = $$dirInfo{DataLen} - $dirStart;
    my $dirLen = $$dirInfo{DirLen} || $size;
    my $tagInfo = $$dirInfo{TagInfo};
    my $ifdOffsetPos;
    # the IFD should be within the first 32 bytes
    # (Kyocera sets the current record at 22 bytes)
    my ($firstTry, $lastTry) = (0, 32);

    # make sure Base and DataPos are defined
    $$dirInfo{Base} or $$dirInfo{Base} = 0;
    $$dirInfo{DataPos} or $$dirInfo{DataPos} = 0;
#
# use tag information (if provided) to determine directory location
#
    if ($tagInfo and $$tagInfo{SubDirectory}) {
        my $subdir = $$tagInfo{SubDirectory};
        unless ($$subdir{ProcessProc} and 
               ($$subdir{ProcessProc} eq \&ProcessUnknown or
                $$subdir{ProcessProc} eq \&ProcessUnknownOrPreview))
        {
            # look for the IFD at the "Start" specified in our SubDirectory information
            my $valuePtr = $dirStart;
            my $newStart = $dirStart;
            if (defined $$subdir{Start}) {
                #### eval Start ($valuePtr)
                $newStart = eval($$subdir{Start});
            }
            if ($$subdir{Base}) {
                # calculate subdirectory start relative to $base for eval
                my $start = $newStart + $$dirInfo{DataPos};
                my $base = $$dirInfo{Base} || 0;
                #### eval Base ($start,$base)
                my $baseShift = eval($$subdir{Base});
                # shift directory base (note: we may do this again below
                # if an OffsetPt is defined, but that doesn't matter since
                # the base shift is relative to DataPos, which we set too)
                $$dirInfo{Base} += $baseShift;
                $$dirInfo{DataPos} -= $baseShift;
                # this is a relative directory if Base depends on $start
                if ($$subdir{Base} =~ /\$start\b/) {
                    $$dirInfo{Relative} = 1;
                    # hack to fix Leica quirk
                    if ($$subdir{ProcessProc} and $$subdir{ProcessProc} eq \&FixLeicaBase) {
                        my $oldStart = $$dirInfo{DirStart};
                        $$dirInfo{DirStart} = $newStart;
                        FixLeicaBase($exifTool, $dirInfo);
                        $$dirInfo{DirStart} = $oldStart;
                    }
                }
            }
            # add offset to the start of the directory if necessary
            if ($$subdir{OffsetPt}) {
                if ($$subdir{ByteOrder} =~ /^Little/i) {
                    SetByteOrder('II');
                } elsif ($$subdir{ByteOrder} =~ /^Big/i) {
                    SetByteOrder('MM');
                } else {
                    warn "Can't have variable byte ordering for SubDirectories using OffsetPt\n";
                    return undef;
                }
                #### eval OffsetPt ($valuePtr)
                $ifdOffsetPos = eval($$subdir{OffsetPt}) - $dirStart;
            }
            # pinpoint position to look for this IFD
            $firstTry = $lastTry = $newStart - $dirStart;
        }
    }
#
# scan for something that looks like an IFD
#
    if ($dirLen >= 14 + $firstTry) {  # minimum size for an IFD
        my $offset;
IFD_TRY: for ($offset=$firstTry; $offset<=$lastTry; $offset+=2) {
            last if $offset + 14 > $dirLen;    # 14 bytes is minimum size for an IFD
            my $pos = $dirStart + $offset;
#
# look for a standard TIFF header (Nikon uses it, others may as well),
#
            if (SetByteOrder(substr($$dataPt, $pos, 2)) and
                Get16u($dataPt, $pos + 2) == 0x2a)
            {
                $ifdOffsetPos = 4;
            }
            if (defined $ifdOffsetPos) {
                # get pointer to IFD
                my $ptr = Get32u($dataPt, $pos + $ifdOffsetPos);
                if ($ptr >= $ifdOffsetPos + 4 and $ptr + $offset + 14 <= $dirLen) {
                    # shift directory start and shorten dirLen accordingly
                    $$dirInfo{DirStart} += $ptr + $offset;
                    $$dirInfo{DirLen} -= $ptr + $offset;
                    # shift pointer base to the start of the TIFF header
                    my $shift = $$dirInfo{DataPos} + $dirStart + $offset;
                    $$dirInfo{Base} += $shift;
                    $$dirInfo{DataPos} -= $shift;
                    $$dirInfo{Relative} = 1;   # set "relative offsets" flag
                    return $ptr + $offset;
                }
                undef $ifdOffsetPos;
            }
#
# look for a standard IFD (starts with 2-byte entry count)
#
            my $num = Get16u($dataPt, $pos);
            next unless $num;
            # number of entries in an IFD should be between 1 and 255
            if (!($num & 0xff)) {
                # lower byte is zero -- byte order could be wrong
                ToggleByteOrder();
                $num >>= 8;
            } elsif ($num & 0xff00) {
                # upper byte isn't zero -- not an IFD
                next;
            }
            my $bytesFromEnd = $size - ($offset + 2 + 12 * $num);
            if ($bytesFromEnd < 4) {
                next unless $bytesFromEnd == 2 or $bytesFromEnd == 0;
            }
            # do a quick validation of all format types
            my $index;
            for ($index=0; $index<$num; ++$index) {
                my $entry = $pos + 2 + 12 * $index;
                my $format = Get16u($dataPt, $entry+2);
                my $count = Get32u($dataPt, $entry+4);
                # allow everything to be zero if not first entry
                # because some manufacturers pad with null entries
                next unless $format or $count or $index == 0;
                # patch for Canon EOS 40D firmware 1.0.4 bug: allow zero format for last entry
                next if $format==0 and $index==$num-1 and $$exifTool{Model}=~/EOS 40D/;
                # (would like to verify tag ID, but some manufactures don't
                #  sort entries in order of tag ID so we don't have much of
                #  a handle to verify this field)
                # verify format
                next IFD_TRY if $format < 1 or $format > 13;
                # count must be reasonable (can't test for zero count because
                # cameras like the 1DmkIII use this value)
                next IFD_TRY if $count & 0xff000000;
                # extra tests to avoid mis-identifying Samsung makernotes (GT-I9000, etc)
                next unless $num == 1;
                my $valueSize = $count * $Image::ExifTool::Exif::formatSize[$format];
                if ($valueSize > 4) {
                    next IFD_TRY if $valueSize > $size;
                    my $valuePtr = Get32u($dataPt, $entry+8);
                    next IFD_TRY if $valuePtr > 0x10000;
                }
            }
            $$dirInfo{DirStart} += $offset;    # update directory start
            $$dirInfo{DirLen} -= $offset;
            return $offset;   # success!!
        }
    }
    return undef;
}

#------------------------------------------------------------------------------
# Fix base offset for Leica maker notes
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: 1 on success, and updates $dirInfo if necessary for new directory
sub FixLeicaBase($$;$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $dirStart = $$dirInfo{DirStart} || 0;
    # get hash of value block positions
    my ($valBlock, $valBlkAdj) = GetValueBlocks($dataPt, $dirStart);
    if (%$valBlock) {
        # get sorted list of value offsets
        my @valPtrs = sort { $a <=> $b } keys %$valBlock;
        my $numEntries = Get16u($dataPt, $dirStart);
        my $diff = $valPtrs[0] - ($numEntries * 12 + 4);
        if ($diff > 8) {
            $$dirInfo{Base} -= 8;
            $$dirInfo{DataPos} += 8;
        }
    }
    my $success = 1;
    if ($tagTablePtr) {
        $success = Image::ExifTool::Exif::ProcessExif($exifTool, $dirInfo, $tagTablePtr);
    }
    return $success;
}

#------------------------------------------------------------------------------
# Process Canon maker notes
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: 1 on success
sub ProcessCanon($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    # identify Canon MakerNote footer in HtmlDump
    # (this code moved from FixBase so it also works for Adobe MakN in DNG images)
    if ($$exifTool{HTML_DUMP} and $$dirInfo{DirLen} > 8) {
        my $dataPos = $$dirInfo{DataPos};
        my $dirStart = $$dirInfo{DirStart} || 0;
        my $footerPos = $dirStart + $$dirInfo{DirLen} - 8;
        my $footer = substr(${$$dirInfo{DataPt}}, $footerPos, 8);
        if ($footer =~ /^(II\x2a\0|MM\0\x2a)/ and substr($footer,0,2) eq GetByteOrder()) {
            my $oldOffset = Get32u(\$footer, 4);
            my $newOffset = $dirStart + $dataPos;
            my $str = sprintf('Original maker note offset: 0x%.4x', $oldOffset);
            if ($oldOffset != $newOffset) {
                $str .= sprintf("\nCurrent maker note offset: 0x%.4x", $newOffset);
            }
            my $filePos = ($$dirInfo{Base} || 0) + $dataPos + $footerPos;
            $exifTool->HDump($filePos, 8, '[Canon MakerNotes footer]', $str);
        }
    }
    # process as normal
    return Image::ExifTool::Exif::ProcessExif($exifTool, $dirInfo, $tagTablePtr);
}

#------------------------------------------------------------------------------
# Process GE type 2 maker notes
# Inputs: 0) ExifTool object ref, 1) DirInfo ref, 2) tag table ref
# Returns: 1 on success
sub ProcessGE2($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt} or return 0;
    my $dirStart = $$dirInfo{DirStart} || 0;

    # these maker notes are missing the IFD entry count, but they
    # always have 25 entries, so write the entry count manually
    Set16u(25, $dataPt, $dirStart);
    return Image::ExifTool::Exif::ProcessExif($exifTool, $dirInfo, $tagTablePtr);
}

#------------------------------------------------------------------------------
# Process unknown maker notes or PreviewImage
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: 1 on success, and updates $dirInfo if necessary for new directory
sub ProcessUnknownOrPreview($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $dirStart = $$dirInfo{DirStart};
    my $dirLen = $$dirInfo{DirLen};
    # check to see if this is a preview image
    if ($dirLen > 6 and substr($$dataPt, $dirStart, 3) eq "\xff\xd8\xff") {
        $exifTool->VerboseDir('PreviewImage');
        if ($$exifTool{HTML_DUMP}) {
            my $pos = $$dirInfo{DataPos} + $$dirInfo{Base} + $dirStart;
            $exifTool->HDump($pos, $dirLen, '(MakerNotes:PreviewImage data)', "Size: $dirLen bytes")
        }
        $exifTool->FoundTag('PreviewImage', substr($$dataPt, $dirStart, $dirLen));
        return 1;
    }
    return ProcessUnknown($exifTool, $dirInfo, $tagTablePtr);
}

#------------------------------------------------------------------------------
# Write unknown maker notes or PreviewImage
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: directory data, '' to delete, or undef on error
sub WriteUnknownOrPreview($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $dirStart = $$dirInfo{DirStart};
    my $dirLen = $$dirInfo{DirLen};
    my $newVal;
    # check to see if this is a preview image
    if ($dirLen > 6 and substr($$dataPt, $dirStart, 3) eq "\xff\xd8\xff") {
        if ($$exifTool{NEW_VALUE}{$Image::ExifTool::Extra{PreviewImage}}) {
            # write or delete new preview (if deleted, it can't currently be added back again)
            $newVal = $exifTool->GetNewValues('PreviewImage') || '';
            if ($exifTool->Options('Verbose') > 1) {
                $exifTool->VerboseValue("- MakerNotes:PreviewImage", substr($$dataPt, $dirStart, $dirLen));
                $exifTool->VerboseValue("+ MakerNotes:PreviewImage", $newVal) if $newVal;
            }
            ++$$exifTool{CHANGED};
        } else {
            $newVal = substr($$dataPt, $dirStart, $dirLen);
        }
    } else {
        # rewrite MakerNote IFD
        $newVal = Image::ExifTool::Exif::WriteExif($exifTool, $dirInfo, $tagTablePtr);
    }
    return $newVal;
}

#------------------------------------------------------------------------------
# Process unknown maker notes assuming it is in EXIF IFD format
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: 1 on success, and updates $dirInfo if necessary for new directory
sub ProcessUnknown($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $success = 0;

    my $loc = LocateIFD($exifTool, $dirInfo);
    if (defined $loc) {
        $exifTool->{UnknownByteOrder} = GetByteOrder();
        if ($exifTool->Options('Verbose') > 1) {
            my $out = $exifTool->Options('TextOut');
            my $indent = $exifTool->{INDENT};
            $indent =~ s/\| $/  /;
            printf $out "${indent}Found IFD at offset 0x%.4x in maker notes:\n",
                    $$dirInfo{DirStart} + $$dirInfo{DataPos} + $$dirInfo{Base};
        }
        $success = Image::ExifTool::Exif::ProcessExif($exifTool, $dirInfo, $tagTablePtr);
    } else {
        $exifTool->{UnknownByteOrder} = ''; # indicates we tried but didn't set byte order
        $exifTool->Warn("Unrecognized $$dirInfo{DirName}", 1);
    }
    return $success;
}


1;  # end

__END__

=head1 NAME

Image::ExifTool::MakerNotes - Read and write EXIF maker notes

=head1 SYNOPSIS

This module is required by Image::ExifTool.

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to interpret
maker notes in EXIF information.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 SEE ALSO

L<Image::ExifTool::TagNames(3pm)|Image::ExifTool::TagNames>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut
