#------------------------------------------------------------------------------
# File:         RIFF.pm
#
# Description:  Read RIFF/WAV/AVI meta information
#
# Revisions:    09/14/2005 - P. Harvey Created
#
# References:   1) http://www.exif.org/Exif2-2.PDF
#               2) http://www.vlsi.fi/datasheets/vs1011.pdf
#               3) http://www.music-center.com.br/spec_rif.htm
#               4) http://www.codeproject.com/audio/wavefiles.asp
#               5) http://msdn.microsoft.com/archive/en-us/directx9_c/directx/htm/avirifffilereference.asp
#               6) http://research.microsoft.com/invisible/tests/riff.h.htm
#               7) http://www.onicos.com/staff/iz/formats/wav.html
#               8) http://graphics.cs.uni-sb.de/NMM/dist-0.9.1/Docs/Doxygen/html/mmreg_8h-source.html
#               9) http://developers.videolan.org/vlc/vlc/doc/doxygen/html/codecs_8h-source.html
#              10) http://wiki.multimedia.cx/index.php?title=TwoCC
#              11) Andreas Winter (SCLive) private communication
#              12) http://abcavi.kibi.ru/infotags.htm
#              13) http://tech.ebu.ch/docs/tech/tech3285.pdf
#------------------------------------------------------------------------------

package Image::ExifTool::RIFF;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.29';

sub ConvertTimecode($);

# type of current stream
$Image::ExifTool::RIFF::streamType = '';

%Image::ExifTool::RIFF::audioEncoding = ( #2
    Notes => 'These "TwoCC" audio encoding codes are used in RIFF and ASF files.',
    0x01 => 'Microsoft PCM',
    0x02 => 'Microsoft ADPCM',
    0x03 => 'Microsoft IEEE float',
    0x04 => 'Compaq VSELP', #4
    0x05 => 'IBM CVSD', #4
    0x06 => 'Microsoft a-Law',
    0x07 => 'Microsoft u-Law',
    0x08 => 'Microsoft DTS', #4
    0x09 => 'DRM', #4
    0x0a => 'WMA 9 Speech', #9
    0x0b => 'Microsoft Windows Media RT Voice', #10
    0x10 => 'OKI-ADPCM',
    0x11 => 'Intel IMA/DVI-ADPCM',
    0x12 => 'Videologic Mediaspace ADPCM', #4
    0x13 => 'Sierra ADPCM', #4
    0x14 => 'Antex G.723 ADPCM', #4
    0x15 => 'DSP Solutions DIGISTD',
    0x16 => 'DSP Solutions DIGIFIX',
    0x17 => 'Dialoic OKI ADPCM', #6
    0x18 => 'Media Vision ADPCM', #6
    0x19 => 'HP CU', #7
    0x1a => 'HP Dynamic Voice', #10
    0x20 => 'Yamaha ADPCM', #6
    0x21 => 'SONARC Speech Compression', #6
    0x22 => 'DSP Group True Speech', #6
    0x23 => 'Echo Speech Corp.', #6
    0x24 => 'Virtual Music Audiofile AF36', #6
    0x25 => 'Audio Processing Tech.', #6
    0x26 => 'Virtual Music Audiofile AF10', #6
    0x27 => 'Aculab Prosody 1612', #7
    0x28 => 'Merging Tech. LRC', #7
    0x30 => 'Dolby AC2',
    0x31 => 'Microsoft GSM610',
    0x32 => 'MSN Audio', #6
    0x33 => 'Antex ADPCME', #6
    0x34 => 'Control Resources VQLPC', #6
    0x35 => 'DSP Solutions DIGIREAL', #6
    0x36 => 'DSP Solutions DIGIADPCM', #6
    0x37 => 'Control Resources CR10', #6
    0x38 => 'Natural MicroSystems VBX ADPCM', #6
    0x39 => 'Crystal Semiconductor IMA ADPCM', #6
    0x3a => 'Echo Speech ECHOSC3', #6
    0x3b => 'Rockwell ADPCM',
    0x3c => 'Rockwell DIGITALK',
    0x3d => 'Xebec Multimedia', #6
    0x40 => 'Antex G.721 ADPCM',
    0x41 => 'Antex G.728 CELP',
    0x42 => 'Microsoft MSG723', #7
    0x43 => 'IBM AVC ADPCM', #10
    0x45 => 'ITU-T G.726', #9
    0x50 => 'Microsoft MPEG',
    0x51 => 'RT23 or PAC', #7
    0x52 => 'InSoft RT24', #4
    0x53 => 'InSoft PAC', #4
    0x55 => 'MP3',
    0x59 => 'Cirrus', #7
    0x60 => 'Cirrus Logic', #6
    0x61 => 'ESS Tech. PCM', #6
    0x62 => 'Voxware Inc.', #6
    0x63 => 'Canopus ATRAC', #6
    0x64 => 'APICOM G.726 ADPCM',
    0x65 => 'APICOM G.722 ADPCM',
    0x66 => 'Microsoft DSAT', #6
    0x67 => 'Micorsoft DSAT DISPLAY', #6
    0x69 => 'Voxware Byte Aligned', #7
    0x70 => 'Voxware AC8', #7
    0x71 => 'Voxware AC10', #7
    0x72 => 'Voxware AC16', #7
    0x73 => 'Voxware AC20', #7
    0x74 => 'Voxware MetaVoice', #7
    0x75 => 'Voxware MetaSound', #7
    0x76 => 'Voxware RT29HW', #7
    0x77 => 'Voxware VR12', #7
    0x78 => 'Voxware VR18', #7
    0x79 => 'Voxware TQ40', #7
    0x7a => 'Voxware SC3', #10
    0x7b => 'Voxware SC3', #10
    0x80 => 'Soundsoft', #6
    0x81 => 'Voxware TQ60', #7
    0x82 => 'Microsoft MSRT24', #7
    0x83 => 'AT&T G.729A', #7
    0x84 => 'Motion Pixels MVI MV12', #7
    0x85 => 'DataFusion G.726', #7
    0x86 => 'DataFusion GSM610', #7
    0x88 => 'Iterated Systems Audio', #7
    0x89 => 'Onlive', #7
    0x8a => 'Multitude, Inc. FT SX20', #10
    0x8b => 'Infocom ITS A/S G.721 ADPCM', #10
    0x8c => 'Convedia G729', #10
    0x8d => 'Not specified congruency, Inc.', #10
    0x91 => 'Siemens SBC24', #7
    0x92 => 'Sonic Foundry Dolby AC3 APDIF', #7
    0x93 => 'MediaSonic G.723', #8
    0x94 => 'Aculab Prosody 8kbps', #8
    0x97 => 'ZyXEL ADPCM', #7,
    0x98 => 'Philips LPCBB', #7
    0x99 => 'Studer Professional Audio Packed', #7
    0xa0 => 'Malden PhonyTalk', #8
    0xa1 => 'Racal Recorder GSM', #10
    0xa2 => 'Racal Recorder G720.a', #10
    0xa3 => 'Racal G723.1', #10
    0xa4 => 'Racal Tetra ACELP', #10
    0xb0 => 'NEC AAC NEC Corporation', #10
    0xff => 'AAC', #10
    0x100 => 'Rhetorex ADPCM', #6
    0x101 => 'IBM u-Law', #3
    0x102 => 'IBM a-Law', #3
    0x103 => 'IBM ADPCM', #3
    0x111 => 'Vivo G.723', #7
    0x112 => 'Vivo Siren', #7
    0x120 => 'Philips Speech Processing CELP', #10
    0x121 => 'Philips Speech Processing GRUNDIG', #10
    0x123 => 'Digital G.723', #7
    0x125 => 'Sanyo LD ADPCM', #8
    0x130 => 'Sipro Lab ACEPLNET', #8
    0x131 => 'Sipro Lab ACELP4800', #8
    0x132 => 'Sipro Lab ACELP8V3', #8
    0x133 => 'Sipro Lab G.729', #8
    0x134 => 'Sipro Lab G.729A', #8
    0x135 => 'Sipro Lab Kelvin', #8
    0x136 => 'VoiceAge AMR', #10
    0x140 => 'Dictaphone G.726 ADPCM', #8
    0x150 => 'Qualcomm PureVoice', #8
    0x151 => 'Qualcomm HalfRate', #8
    0x155 => 'Ring Zero Systems TUBGSM', #8
    0x160 => 'Microsoft Audio1', #8
    0x161 => 'Windows Media Audio V2 V7 V8 V9 / DivX audio (WMA) / Alex AC3 Audio', #10
    0x162 => 'Windows Media Audio Professional V9', #10
    0x163 => 'Windows Media Audio Lossless V9', #10
    0x164 => 'WMA Pro over S/PDIF', #10
    0x170 => 'UNISYS NAP ADPCM', #10
    0x171 => 'UNISYS NAP ULAW', #10
    0x172 => 'UNISYS NAP ALAW', #10
    0x173 => 'UNISYS NAP 16K', #10
    0x174 => 'MM SYCOM ACM SYC008 SyCom Technologies', #10
    0x175 => 'MM SYCOM ACM SYC701 G726L SyCom Technologies', #10
    0x176 => 'MM SYCOM ACM SYC701 CELP54 SyCom Technologies', #10
    0x177 => 'MM SYCOM ACM SYC701 CELP68 SyCom Technologies', #10
    0x178 => 'Knowledge Adventure ADPCM', #10
    0x180 => 'Fraunhofer IIS MPEG2AAC', #10
    0x190 => 'Digital Theater Systems DTS DS', #10
    0x200 => 'Creative Labs ADPCM', #6
    0x202 => 'Creative Labs FASTSPEECH8', #6
    0x203 => 'Creative Labs FASTSPEECH10', #6
    0x210 => 'UHER ADPCM', #8
    0x215 => 'Ulead DV ACM', #10
    0x216 => 'Ulead DV ACM', #10
    0x220 => 'Quarterdeck Corp.', #6
    0x230 => 'I-Link VC', #8
    0x240 => 'Aureal Semiconductor Raw Sport', #8
    0x241 => 'ESST AC3', #10
    0x250 => 'Interactive Products HSX', #8
    0x251 => 'Interactive Products RPELP', #8
    0x260 => 'Consistent CS2', #8
    0x270 => 'Sony SCX', #8
    0x271 => 'Sony SCY', #10
    0x272 => 'Sony ATRAC3', #10
    0x273 => 'Sony SPC', #10
    0x280 => 'TELUM Telum Inc.', #10
    0x281 => 'TELUMIA Telum Inc.', #10
    0x285 => 'Norcom Voice Systems ADPCM', #10
    0x300 => 'Fujitsu FM TOWNS SND', #6
    0x301 => 'Fujitsu (not specified)', #10
    0x302 => 'Fujitsu (not specified)', #10
    0x303 => 'Fujitsu (not specified)', #10
    0x304 => 'Fujitsu (not specified)', #10
    0x305 => 'Fujitsu (not specified)', #10
    0x306 => 'Fujitsu (not specified)', #10
    0x307 => 'Fujitsu (not specified)', #10
    0x308 => 'Fujitsu (not specified)', #10
    0x350 => 'Micronas Semiconductors, Inc. Development', #10
    0x351 => 'Micronas Semiconductors, Inc. CELP833', #10
    0x400 => 'Brooktree Digital', #6
    0x401 => 'Intel Music Coder (IMC)', #10
    0x402 => 'Ligos Indeo Audio', #10
    0x450 => 'QDesign Music', #8
    0x500 => 'On2 VP7 On2 Technologies', #10
    0x501 => 'On2 VP6 On2 Technologies', #10
    0x680 => 'AT&T VME VMPCM', #7
    0x681 => 'AT&T TCP', #8
    0x700 => 'YMPEG Alpha (dummy for MPEG-2 compressor)', #10
    0x8ae => 'ClearJump LiteWave (lossless)', #10
    0x1000 => 'Olivetti GSM', #6
    0x1001 => 'Olivetti ADPCM', #6
    0x1002 => 'Olivetti CELP', #6
    0x1003 => 'Olivetti SBC', #6
    0x1004 => 'Olivetti OPR', #6
    0x1100 => 'Lernout & Hauspie', #6
    0x1101 => 'Lernout & Hauspie CELP codec', #10
    0x1102 => 'Lernout & Hauspie SBC codec', #10
    0x1103 => 'Lernout & Hauspie SBC codec', #10
    0x1104 => 'Lernout & Hauspie SBC codec', #10
    0x1400 => 'Norris Comm. Inc.', #6
    0x1401 => 'ISIAudio', #7
    0x1500 => 'AT&T Soundspace Music Compression', #7
    0x181c => 'VoxWare RT24 speech codec', #10
    0x181e => 'Lucent elemedia AX24000P Music codec', #10
    0x1971 => 'Sonic Foundry LOSSLESS', #10
    0x1979 => 'Innings Telecom Inc. ADPCM', #10
    0x1c07 => 'Lucent SX8300P speech codec', #10
    0x1c0c => 'Lucent SX5363S G.723 compliant codec', #10
    0x1f03 => 'CUseeMe DigiTalk (ex-Rocwell)', #10
    0x1fc4 => 'NCT Soft ALF2CD ACM', #10
    0x2000 => 'FAST Multimedia DVM', #7
    0x2001 => 'Dolby DTS (Digital Theater System)', #10
    0x2002 => 'RealAudio 1 / 2 14.4', #10
    0x2003 => 'RealAudio 1 / 2 28.8', #10
    0x2004 => 'RealAudio G2 / 8 Cook (low bitrate)', #10
    0x2005 => 'RealAudio 3 / 4 / 5 Music (DNET)', #10
    0x2006 => 'RealAudio 10 AAC (RAAC)', #10
    0x2007 => 'RealAudio 10 AAC+ (RACP)', #10
    0x2500 => 'Reserved range to 0x2600 Microsoft', #10
    0x3313 => 'makeAVIS (ffvfw fake AVI sound from AviSynth scripts)', #10
    0x4143 => 'Divio MPEG-4 AAC audio', #10
    0x4201 => 'Nokia adaptive multirate', #10
    0x4243 => 'Divio G726 Divio, Inc.', #10
    0x434c => 'LEAD Speech', #10
    0x564c => 'LEAD Vorbis', #10
    0x5756 => 'WavPack Audio', #10
    0x674f => 'Ogg Vorbis (mode 1)', #10
    0x6750 => 'Ogg Vorbis (mode 2)', #10
    0x6751 => 'Ogg Vorbis (mode 3)', #10
    0x676f => 'Ogg Vorbis (mode 1+)', #10
    0x6770 => 'Ogg Vorbis (mode 2+)', #10
    0x6771 => 'Ogg Vorbis (mode 3+)', #10
    0x7000 => '3COM NBX 3Com Corporation', #10
    0x706d => 'FAAD AAC', #10
    0x7a21 => 'GSM-AMR (CBR, no SID)', #10
    0x7a22 => 'GSM-AMR (VBR, including SID)', #10
    0xa100 => 'Comverse Infosys Ltd. G723 1', #10
    0xa101 => 'Comverse Infosys Ltd. AVQSBC', #10
    0xa102 => 'Comverse Infosys Ltd. OLDSBC', #10
    0xa103 => 'Symbol Technologies G729A', #10
    0xa104 => 'VoiceAge AMR WB VoiceAge Corporation', #10
    0xa105 => 'Ingenient Technologies Inc. G726', #10
    0xa106 => 'ISO/MPEG-4 advanced audio Coding', #10
    0xa107 => 'Encore Software Ltd G726', #10
    0xa109 => 'Speex ACM Codec xiph.org', #10
    0xdfac => 'DebugMode SonicFoundry Vegas FrameServer ACM Codec', #10
    0xe708 => 'Unknown -', #10
    0xf1ac => 'Free Lossless Audio Codec FLAC', #10
    0xfffe => 'Extensible', #7
    0xffff => 'Development', #4
);

# RIFF info
%Image::ExifTool::RIFF::Main = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    NOTES => q{
        The RIFF container format is used various types of fines including WAV, AVI
        and WEBP.  According to the EXIF specification, Meta information is embedded
        in two types of RIFF C<LIST> chunks: C<INFO> and C<exif>, and information
        about the audio content is stored in the C<fmt > chunk.  As well as this
        information, some video information and proprietary manufacturer-specific
        information is also extracted.
    },
   'fmt ' => {
        Name => 'AudioFormat',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::AudioFormat' },
    },
   'bext' => {
        Name => 'BroadcastExtension',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::BroadcastExt' },
    },
    LIST_INFO => {
        Name => 'Info',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::Info' },
    },
    LIST_exif => {
        Name => 'Exif',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::Exif' },
    },
    LIST_hdrl => { # AVI header LIST chunk
        Name => 'Hdrl',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::Hdrl' },
    },
    LIST_Tdat => { #PH (Adobe CS3 Bridge)
        Name => 'Tdat',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::Tdat' },
    },
    LIST_ncdt => { #PH (Nikon metadata)
        Name => 'NikonData',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Nikon::AVI',
            # define ProcessProc here so we don't need to load RIFF.pm from Nikon.pm
            ProcessProc => \&Image::ExifTool::RIFF::ProcessChunks,
        },
    },
    LIST_hydt => { #PH (Pentax metadata)
        Name => 'PentaxData',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Pentax::AVI',
            ProcessProc => \&Image::ExifTool::RIFF::ProcessChunks,
        },
    },
    JUNK => [
        {
            Name => 'OlympusJunk',
            Condition => '$$valPt =~ /^OLYMDigital Camera/',
            SubDirectory => { TagTable => 'Image::ExifTool::Olympus::AVI' },
        },
        {
            Name => 'CasioJunk',
            Condition => '$$valPt =~ /^QVMI/',
            # Casio stores standard EXIF-format information in AVI videos (EX-S600)
            SubDirectory => {
                TagTable => 'Image::ExifTool::Exif::Main',
                DirName => 'IFD0',
                Multi => 0, # (IFD1 is not written)
                Start => 10,
                ByteOrder => 'BigEndian',
            },
        },
        {
            Name => 'RicohJunk',
            # the Ricoh Caplio GX stores sub-chunks in here
            Condition => '$$valPt =~ /^ucmt/',
            SubDirectory => {
                TagTable => 'Image::ExifTool::Ricoh::AVI',
                ProcessProc => \&Image::ExifTool::RIFF::ProcessChunks,
            },
        },
        {
            Name => 'PentaxJunk', # (Optio RS1000)
            Condition => '$$valPt =~ /^IIII\x01\0/',
            SubDirectory => { TagTable => 'Image::ExifTool::Pentax::Junk' },
        },
        {
            Name => 'TextJunk',
            # try to interpret unknown junk as an ASCII string
            RawConv => '$val =~ /^([^\0-\x1f\x7f-\xff]+)\0*$/ ? $1 : undef',
        }
    ],
    _PMX => { #PH (Adobe CS3 Bridge)
        Name => 'XMP',
        SubDirectory => { TagTable => 'Image::ExifTool::XMP::Main' },
    },
    JUNQ => { #PH (Adobe CS3 Bridge)
        # old XMP is preserved when metadata is replaced in Bridge
        Name => 'OldXMP',
        Binary => 1,
    },
    olym => {
        Name => 'Olym',
        SubDirectory => { TagTable => 'Image::ExifTool::Olympus::WAV' },
    },
);

# the maker notes used by some digital cameras
%Image::ExifTool::RIFF::Junk = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Audio' },
);

# Format and Audio Stream Format chunk data
%Image::ExifTool::RIFF::AudioFormat = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Audio' },
    FORMAT => 'int16u',
    0 => {
        Name => 'Encoding',
        PrintHex => 1,
        PrintConv => \%Image::ExifTool::RIFF::audioEncoding,
        SeparateTable => 'AudioEncoding',
    },
    1 => 'NumChannels',
    2 => {
        Name => 'SampleRate',
        Format => 'int32u',
    },
    4 => {
        Name => 'AvgBytesPerSec',
        Format => 'int32u',
    },
   # uninteresting
   # 6 => 'BlockAlignment',
    7 => 'BitsPerSample',
);

# Broadcast Audio Extension 'bext' information (ref 13)
%Image::ExifTool::RIFF::BroadcastExt = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Audio' },
    NOTES => q{
        Information found in the Broadcast Audio Extension chunk (see
        L<http://tech.ebu.ch/docs/tech/tech3285.pdf>).
    },
    0 => {
        Name => 'Description',
        Format => 'string[256]',
    },
    256 => {
        Name => 'Originator',
        Format => 'string[32]',
    },
    288 => {
        Name => 'OriginatorReference',
        Format => 'string[32]',
    },
    320 => {
        Name => 'DateTimeOriginal',
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        Format => 'string[18]',
        ValueConv => '$_=$val; tr/-/:/; s/^(\d{4}:\d{2}:\d{2})/$1 /; $_',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    338 => {
        Name => 'TimeReference',
        Notes => 'first sample count since midnight',
        Format => 'int32u[2]',
        ValueConv => 'my @v=split(" ",$val); $v[0] + $v[1] * 4294967296',
    },
    346 => {
        Name => 'BWFVersion',
        Format => 'int16u',
    },
    # 348 - int8u[64] - SMPTE 330M UMID (Unique Material Identifier)
    # 412 - int8u[190] - reserved
    602 => {
        Name => 'CodingHistory',
        Format => 'string[$size-602]',
    },
);

# Sub chunks of INFO LIST chunk
%Image::ExifTool::RIFF::Info = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Audio' },
    NOTES => q{
        RIFF INFO tags found in WAV audio and AVI video files.  Tags which are part
        of the EXIF 2.3 specification have an underlined Tag Name in the HTML
        version of this documentation.  Other tags are found in AVI files generated
        by some software.
    },
    IARL => 'ArchivalLocation',
    IART => { Name => 'Artist',    Groups => { 2 => 'Author' } },
    ICMS => 'Commissioned',
    ICMT => 'Comment',
    ICOP => { Name => 'Copyright', Groups => { 2 => 'Author' } },
    ICRD => {
        Name => 'DateCreated',
        Groups => { 2 => 'Time' },
        ValueConv => '$_=$val; s/-/:/g; $_',
    },
    ICRP => 'Cropped',
    IDIM => 'Dimensions',
    IDPI => 'DotsPerInch',
    IENG => 'Engineer',
    IGNR => 'Genre',
    IKEY => 'Keywords',
    ILGT => 'Lightness',
    IMED => 'Medium',
    INAM => 'Title',
    IPLT => 'NumColors',
    IPRD => 'Product',
    ISBJ => 'Subject',
    ISFT => {
        Name => 'Software',
        # remove trailing nulls/spaces and split at first null
        # (Casio writes "CASIO" in unicode after the first null)
        ValueConv => '$_=$val; s/(\s*\0)+$//; s/(\s*\0)/, /; s/\0+//g; $_',
    },
    ISHP => 'Sharpness',
    ISRC => 'Source',
    ISRF => 'SourceForm',
    ITCH => 'Technician',
#
# 3rd party tags
#
    # internet movie database (ref 12)
    ISGN => 'SecondaryGenre',
    IWRI => 'WrittenBy',
    IPRO => 'ProducedBy',
    ICNM => 'Cinematographer',
    IPDS => 'ProductionDesigner',
    IEDT => 'EditedBy',
    ICDS => 'CostumeDesigner',
    IMUS => 'MusicBy',
    ISTD => 'ProductionStudio',
    IDST => 'DistributedBy',
    ICNT => 'Country',
    ILNG => 'Language',
    IRTD => 'Rating',
    ISTR => 'Starring',
    # MovieID (ref12)
    TITL => 'Title',
    DIRC => 'Directory',
    YEAR => 'Year',
    GENR => 'Genre',
    COMM => 'Comments',
    LANG => 'Language',
    AGES => 'Rated',
    STAR => 'Starring',
    CODE => 'EncodedBy',
    PRT1 => 'Part',
    PRT2 => 'NumberOfParts',
    # Morgan Multimedia INFO tags (ref 12)
    IAS1 => 'FirstLanguage',
    IAS2 => 'SecondLanguage',
    IAS3 => 'ThirdLanguage',
    IAS4 => 'FourthLanguage',
    IAS5 => 'FifthLanguage',
    IAS6 => 'SixthLanguage',
    IAS7 => 'SeventhLanguage',
    IAS8 => 'EighthLanguage',
    IAS9 => 'NinthLanguage',
    ICAS => 'DefaultAudioStream',
    IBSU => 'BaseURL',
    ILGU => 'LogoURL',
    ILIU => 'LogoIconURL',
    IWMU => 'WatermarkURL',
    IMIU => 'MoreInfoURL',
    IMBI => 'MoreInfoBannerImage',
    IMBU => 'MoreInfoBannerURL',
    IMIT => 'MoreInfoText',
    # GSpot INFO tags (ref 12)
    IENC => 'EncodedBy',
    IRIP => 'RippedBy',
    # Sound Forge Pro tags
    DISP => 'SoundSchemeTitle',
    TLEN => { Name => 'Length', ValueConv => '$val/1000', PrintConv => '"$val s"' },
    TRCK => 'TrackNumber',
    TURL => 'URL',
    TVER => 'Version',
    LOCA => 'Location',
    TORG => 'Organization',
    # Sony Vegas AVI tags, also used by SCLive and Adobe Premier (ref 11)
    TAPE => {
        Name => 'TapeName',
        Groups => { 2 => 'Video' },
    },
    TCOD => {
        Name => 'StartTimecode',
        # this is the tape time code for the start of the video
        Groups => { 2 => 'Video' },
        ValueConv => '$val * 1e-7',
        PrintConv => \&ConvertTimecode,
    },
    TCDO => {
        Name => 'EndTimecode',
        Groups => { 2 => 'Video' },
        ValueConv => '$val * 1e-7',
        PrintConv => \&ConvertTimecode,
    },
    VMAJ => {
        Name => 'VegasVersionMajor',
        Groups => { 2 => 'Video' },
    },
    VMIN => {
        Name => 'VegasVersionMinor',
        Groups => { 2 => 'Video' },
    },
    CMNT => {
        Name => 'Comment',
        Groups => { 2 => 'Video' },
    },
    RATE => {
        Name => 'Rate', #? (video? units?)
        Groups => { 2 => 'Video' },
    },
    STAT => {
        Name => 'Statistics',
        Groups => { 2 => 'Video' },
        # ("7318 0 3.430307 1", "0 0 3500.000000 1", "7 0 3.433228 1")
        PrintConv => [
            '"$val frames captured"',
            '"$val dropped"',
            '"Data rate $val"',
            { 0 => 'Bad', 1 => 'OK' }, # capture OK?
        ],
    },
    DTIM => {
        Name => 'DateTimeOriginal',
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        ValueConv => q{
            my @v = split ' ', $val;
            return undef unless @v == 2;
            # the Kodak EASYSHARE Sport stores this incorrectly as a string:
            return $val if $val =~ /^\d{4}:\d{2}:\d{2} \d{2}:\d{2}:\d{2}$/;
            # get time in seconds
            $val = 1e-7 * ($v[0] * 4294967296 + $v[1]);
            # shift from Jan 1, 1601 to Jan 1, 1970
            $val -= 134774 * 24 * 3600 if $val != 0;
            return Image::ExifTool::ConvertUnixTime($val);
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
);

# Sub chunks of EXIF LIST chunk
%Image::ExifTool::RIFF::Exif = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Audio' },
    NOTES => 'These tags are part of the EXIF 2.3 specification for WAV audio files.',
    ever => 'ExifVersion',
    erel => 'RelatedImageFile',
    etim => { Name => 'TimeCreated', Groups => { 2 => 'Time' } },
    ecor => { Name => 'Make',        Groups => { 2 => 'Camera' } },
    emdl => { Name => 'Model',       Groups => { 2 => 'Camera' } },
    emnt => { Name => 'MakerNotes',  Binary => 1 },
    eucm => {
        Name => 'UserComment',
        PrintConv => 'Image::ExifTool::Exif::ConvertExifText($self,$val)',
    },
);

# Sub chunks of hdrl LIST chunk
%Image::ExifTool::RIFF::Hdrl = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Image' },
    avih => {
        Name => 'AVIHeader',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::AVIHeader' },
    },
    IDIT => {
        Name => 'DateTimeOriginal',
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        ValueConv => 'Image::ExifTool::RIFF::ConvertRIFFDate($val)',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    ISMP => 'Timecode',
    LIST_strl => {
        Name => 'Stream',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::Stream' },
    },
    LIST_odml => {
        Name => 'OpenDML',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::OpenDML' },
    },
);

# Sub chunks of Tdat LIST chunk (ref PH)
%Image::ExifTool::RIFF::Tdat = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Video' },
    # (have seen tc_O, tc_A, rn_O and rn_A)
);

%Image::ExifTool::RIFF::AVIHeader = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Video' },
    FORMAT => 'int32u',
    FIRST_ENTRY => 0,
    0 => {
        Name => 'FrameRate',
        # (must use RawConv because raw value used in Composite tag)
        RawConv => '$val ? 1e6 / $val : undef',
        PrintConv => 'int($val * 1000 + 0.5) / 1000',
    },
    1 => {
        Name => 'MaxDataRate',
        PrintConv => 'sprintf("%.4g kB/s",$val / 1024)',
    },
  # 2 => 'PaddingGranularity',
  # 3 => 'Flags',
    4 => 'FrameCount',
  # 5 => 'InitialFrames',
    6 => 'StreamCount',
  # 7 => 'SuggestedBufferSize',
    8 => 'ImageWidth',
    9 => 'ImageHeight',
);

%Image::ExifTool::RIFF::Stream = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Image' },
    strh => {
        Name => 'StreamHeader',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::StreamHeader' },
    },
    strn => 'StreamName',
    strd => { #PH
        Name => 'StreamData',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::StreamData' },
    },
    strf => [
        {
            Name => 'AudioFormat',
            Condition => '$Image::ExifTool::RIFF::streamType eq "auds"',
            SubDirectory => { TagTable => 'Image::ExifTool::RIFF::AudioFormat' },
        },
        {
            Name => 'VideoFormat',
            Condition => '$Image::ExifTool::RIFF::streamType eq "vids"',
            SubDirectory => { TagTable => 'Image::ExifTool::BMP::Main' },
        },
    ],
);

# Open DML tags (ref http://www.morgan-multimedia.com/download/odmlff2.pdf)
%Image::ExifTool::RIFF::OpenDML = (
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessChunks,
    GROUPS => { 2 => 'Video' },
    dmlh => {
        Name => 'ExtendedAVIHeader',
        SubDirectory => { TagTable => 'Image::ExifTool::RIFF::ExtAVIHdr' },
    },
);

# Extended AVI Header tags (ref http://www.morgan-multimedia.com/download/odmlff2.pdf)
%Image::ExifTool::RIFF::ExtAVIHdr = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Video' },
    FORMAT => 'int32u',
    0 => 'TotalFrameCount',
);

%Image::ExifTool::RIFF::StreamHeader = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Video' },
    FORMAT => 'int32u',
    FIRST_ENTRY => 0,
    PRIORITY => 0,  # so we get values from the first stream
    0 => {
        Name => 'StreamType',
        Format => 'string[4]',
        RawConv => '$Image::ExifTool::RIFF::streamType = $val',
        PrintConv => {
            auds => 'Audio',
            mids => 'MIDI',
            txts => 'Text',
            vids => 'Video',
        },
    },
    1 => [
        {
            Name => 'AudioCodec',
            Condition => '$Image::ExifTool::RIFF::streamType eq "auds"',
            Format => 'string[4]',
        },
        {
            Name => 'VideoCodec',
            Condition => '$Image::ExifTool::RIFF::streamType eq "vids"',
            Format => 'string[4]',
        },
        {
            Name => 'Codec',
            Format => 'string[4]',
        },
    ],
  # 2 => 'StreamFlags',
  # 3 => 'StreamPriority',
  # 3.5 => 'Language',
  # 4 => 'InitialFrames',
    5 => [
        {
            Name => 'AudioSampleRate',
            Condition => '$Image::ExifTool::RIFF::streamType eq "auds"',
            Format => 'rational64u',
            ValueConv => '$val ? 1/$val : 0',
            PrintConv => 'int($val * 100 + 0.5) / 100',
        },
        {
            Name => 'VideoFrameRate',
            Condition => '$Image::ExifTool::RIFF::streamType eq "vids"',
            Format => 'rational64u',
            # (must use RawConv because raw value used in Composite tag)
            RawConv => '$val ? 1/$val : undef',
            PrintConv => 'int($val * 1000 + 0.5) / 1000',
        },
        {
            Name => 'StreamSampleRate',
            Format => 'rational64u',
            ValueConv => '$val ? 1/$val : 0',
            PrintConv => 'int($val * 1000 + 0.5) / 1000',
        },
    ],
  # 7 => 'Start',
    8 => [
        {
            Name => 'AudioSampleCount',
            Condition => '$Image::ExifTool::RIFF::streamType eq "auds"',
        },
        {
            Name => 'VideoFrameCount',
            Condition => '$Image::ExifTool::RIFF::streamType eq "vids"',
        },
        {
            Name => 'StreamSampleCount',
        },
    ],
  # 9 => 'SuggestedBufferSize',
    10 => {
        Name => 'Quality',
        PrintConv => '$val eq 0xffffffff ? "Default" : $val',
    },
    11 => {
        Name => 'SampleSize',
        PrintConv => '$val ? "$val byte" . ($val==1 ? "" : "s") : "Variable"',
    },
  # 12 => { Name => 'Frame', Format => 'int16u[4]' },
);

%Image::ExifTool::RIFF::StreamData = ( #PH
    PROCESS_PROC => \&Image::ExifTool::RIFF::ProcessStreamData,
    GROUPS => { 2 => 'Video' },
    NOTES => q{
        This chunk is used to store proprietary information in AVI videos from some
        cameras.  The first 4 characters of the data are used as the Tag ID below.
    },
    AVIF => {
        Name => 'AVIF',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Exif::Main',
            DirName => 'IFD0',
            Start => 8,
            ByteOrder => 'LittleEndian',
        },
    },
    CASI => { # (used by Casio GV-10)
        Name => 'CasioData',
        SubDirectory => { TagTable => 'Image::ExifTool::Casio::AVI' },
    },
    Zora => 'VendorName',   # (Samsung PL90 AVI files)
    unknown => {
        Name => 'UnknownData',
        # try to interpret unknown stream data as a string
        RawConv => '$_=$val; /^[^\0-\x1f\x7f-\xff]+$/ ? $_ : undef',
    },
);

# RIFF composite tags
%Image::ExifTool::RIFF::Composite = (
    Duration => {
        Require => {
            0 => 'RIFF:FrameRate',
            1 => 'RIFF:FrameCount',
        },
        Desire => {
            2 => 'VideoFrameRate',
            3 => 'VideoFrameCount',
        },
        # this is annoying.  Apparently (although I couldn't verify this), FrameCount
        # in the RIFF header includes multiple video tracks if they exist (ie. with the
        # FujiFilm REAL 3D AVI's), but the video stream information isn't reliable for
        # some cameras (ie. Olympus FE models), so use the video stream information
        # only if the RIFF header duration is 2 to 3 times longer
        RawConv => q {
            my ($dur1, $dur2);
            $dur1 = $val[1] / $val[0] if $val[0];
            return $dur1 unless $val[2] and $val[3];
            $dur2 = $val[3] / $val[2];
            my $rat = $dur1 / $dur2;
            return ($rat > 1.9 and $rat < 3.1) ? $dur2 : $dur1;
        },
        PrintConv => 'ConvertDuration($val)',
    },
    Duration2 => {
        Name => 'Duration',
        Require => {
            0 => 'RIFF:AvgBytesPerSec',
            1 => 'FileSize',
        },
        Desire => {
            # check FrameCount because this calculation only applies
            # to audio-only files (ie. WAV)
            2 => 'FrameCount',
            3 => 'VideoFrameCount',
        },
        RawConv => '($val[0] and not ($val[2] or $val[3])) ? $val[1] / $val[0] : undef',
        PrintConv => 'ConvertDuration($val)',
    },
);

# add our composite tags
Image::ExifTool::AddCompositeTags('Image::ExifTool::RIFF');


#------------------------------------------------------------------------------
# Convert RIFF date to EXIF format
my %monthNum = (
    Jan=>1, Feb=>2, Mar=>3, Apr=>4, May=>5, Jun=>6,
    Jul=>7, Aug=>8, Sep=>9, Oct=>10,Nov=>11,Dec=>12
);
sub ConvertRIFFDate($)
{
    my $val = shift;
    my @part = split ' ', $val;
    my $mon;
    if (@part >= 5 and $mon = $monthNum{ucfirst(lc($part[1]))}) {
        # the standard AVI date format (ie. "Mon Mar 10 15:04:43 2003")
        $val = sprintf("%.4d:%.2d:%.2d %s", $part[4],
                       $mon, $part[2], $part[3]);
    } elsif ($val =~ m{(\d{4})/\s*(\d+)/\s*(\d+)/?\s+(\d+):\s*(\d+)\s*(P?)}) {
        # but the Casio QV-3EX writes dates like "2001/ 1/27  1:42PM",
        # and the Casio EX-Z30 writes "2005/11/28/ 09:19"... doh!
        $val = sprintf("%.4d:%.2d:%.2d %.2d:%.2d:00",$1,$2,$3,$4+($6?12:0),$5);
    }
    return $val;
}

#------------------------------------------------------------------------------
# Print time
# Inputs: 0) time in seconds
# Returns: time string
sub ConvertTimecode($)
{
    my $val = shift;
    my $hr = int($val / 3600);
    $val -= $hr * 3600;
    my $min = int($val / 60);
    $val -= $min * 60;
    return sprintf("%d:%.2d:%05.2f", $hr, $min, $val);
}

#------------------------------------------------------------------------------
# Process stream data
# Inputs: 0) ExifTool object ref, 1) dirInfo reference, 2) tag table ref
# Returns: 1 on success
sub ProcessStreamData($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $start = $$dirInfo{DirStart};
    my $size = $$dirInfo{DirLen};
    return 0 if $size < 4;
    if ($exifTool->Options('Verbose')) {
        $exifTool->VerboseDir($$dirInfo{DirName}, 0, $size);
    }
    my $tag = substr($$dataPt, $start, 4);
    my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tag);
    unless ($tagInfo) {
        $tagInfo = $exifTool->GetTagInfo($tagTablePtr, 'unknown');
        return 1 unless $tagInfo;
    }
    my $subdir = $$tagInfo{SubDirectory};
    if ($$tagInfo{SubDirectory}) {
        my $offset = $$subdir{Start} || 0;
        my $baseShift = $$dirInfo{DataPos} + $$dirInfo{DirStart} + $offset;
        my %subdirInfo = (
            DataPt  => $dataPt,
            DataPos => $$dirInfo{DataPos} - $baseShift,
            Base    => ($$dirInfo{Base} || 0) + $baseShift,
            DataLen => $$dirInfo{DataLen},
            DirStart=> $$dirInfo{DirStart} + $offset,
            DirLen  => $$dirInfo{DirLen} - $offset,
            DirName => $$subdir{DirName},
            Parent  => $$dirInfo{DirName},
        );
        unless ($offset) {
            # allow processing of 2nd directory at the same address
            my $addr = $subdirInfo{DirStart} + $subdirInfo{DataPos} + $subdirInfo{Base};
            delete $exifTool->{PROCESSED}->{$addr}
        }
        # (we could set FIRST_EXIF_POS to $subdirInfo{Base} here to make
        #  htmlDump offsets relative to EXIF base if we wanted...)
        my $subTable = GetTagTable($$subdir{TagTable});
        $exifTool->ProcessDirectory(\%subdirInfo, $subTable);
    } else {
        $exifTool->HandleTag($tagTablePtr, $tag, undef,
            DataPt  => $dataPt,
            DataPos => $$dirInfo{DataPos},
            Start   => $start,
            Size    => $size,
            TagInfo => $tagInfo,
        );
    }
    return 1;
}

#------------------------------------------------------------------------------
# Process RIFF chunks
# Inputs: 0) ExifTool object reference, 1) directory information reference
#         2) tag table reference
# Returns: 1 on success
sub ProcessChunks($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $start = $$dirInfo{DirStart};
    my $size = $$dirInfo{DirLen};
    my $end = $start + $size;
    my $base = $$dirInfo{Base};

    if ($exifTool->Options('Verbose')) {
        $exifTool->VerboseDir($$dirInfo{DirName}, 0, $size);
    }
    while ($start + 8 < $end) {
        my $tag = substr($$dataPt, $start, 4);
        my $len = Get32u($dataPt, $start + 4);
        $start += 8;
        if ($start + $len > $end) {
            $exifTool->Warn("Bad $tag chunk");
            return 0;
        }
        if ($tag eq 'LIST' and $len >= 4) {
            $tag .= '_' . substr($$dataPt, $start, 4);
            $len -= 4;
            $start += 4;
        }
        my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tag);
        my $baseShift = 0;
        my $val;
        if ($tagInfo) {
            if ($$tagInfo{SubDirectory}) {
                # adjust base if necessary (needed for Ricoh maker notes)
                my $newBase = $tagInfo->{SubDirectory}{Base};
                if (defined $newBase) {
                    # different than your average Base eval...
                    # here we use an absolute $start address
                    $start += $base;
                    #### eval Base ($start)
                    $newBase = eval $newBase;
                    $baseShift = $newBase - $base;
                    $start -= $base;
                }
            } elsif (not $$tagInfo{Binary}) {
                $val = substr($$dataPt, $start, $len);
                $val =~ s/\0+$//;   # remove trailing nulls from strings
            }
        }
        $exifTool->HandleTag($tagTablePtr, $tag, $val,
            DataPt  => $dataPt,
            DataPos => $$dirInfo{DataPos} - $baseShift,
            Start   => $start,
            Size    => $len,
            Base    => $base + $baseShift,
        );
        ++$len if $len & 0x01;  # must account for padding if odd number of bytes
        $start += $len;
    }
    return 1;
}

#------------------------------------------------------------------------------
# Extract information from a RIFF file
# Inputs: 0) ExifTool object reference, 1) DirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid RIFF file
sub ProcessRIFF($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my ($buff, $err);
    my %types = ( 'WAVE' => 'WAV', 'AVI ' => 'AVI', 'WEBP' => 'WEBP' );
    my $verbose = $exifTool->Options('Verbose');

    # verify this is a valid RIFF file
    return 0 unless $raf->Read($buff, 12) == 12;
    return 0 unless $buff =~ /^RIFF....(.{4})/s;
    $exifTool->SetFileType($types{$1}); # set type to 'WAV', 'AVI', 'WEBP' or 'RIFF'
    $Image::ExifTool::RIFF::streamType = '';    # initialize stream type
    SetByteOrder('II');
    my $tagTablePtr = GetTagTable('Image::ExifTool::RIFF::Main');
    my $pos = 12;
#
# Read chunks in RIFF image
#
    for (;;) {
        my $num = $raf->Read($buff, 8);
        if ($num < 8) {
            $err = 1 if $num;
            last;
        }
        $pos += 8;
        my ($tag, $len) = unpack('a4V', $buff);
        # special case: construct new tag name from specific LIST type
        if ($tag eq 'LIST') {
            $raf->Read($buff, 4) == 4 or $err=1, last;
            $pos += 4;
            $tag .= "_$buff";
            $len -= 4;  # already read 4 bytes (the LIST type)
        }
        $exifTool->VPrint(0, "RIFF '$tag' chunk ($len bytes of data):\n");
        # stop when we hit the audio data or AVI index or AVI movie data
        # --> no more because Adobe Bridge stores XMP after this!!
        # (so now we only do this on the FastScan option)
        if (($tag eq 'data' or $tag eq 'idx1' or $tag eq 'LIST_movi') and
            $exifTool->Options('FastScan'))
        {
            $exifTool->VPrint(0, "(end of parsing)\n");
            last;
        }
        # RIFF chunks are padded to an even number of bytes
        my $len2 = $len + ($len & 0x01);
        if ($$tagTablePtr{$tag} or ($verbose and $tag !~ /^(data|idx1|LIST_movi)$/)) {
            $raf->Read($buff, $len2) == $len2 or $err=1, last;
            $exifTool->HandleTag($tagTablePtr, $tag, $buff,
                DataPt  => \$buff,
                DataPos => 0,   # (relative to Base)
                Start   => 0,
                Size    => $len2,
                Base    => $pos,
            );
        } else {
            $raf->Seek($len2, 1) or $err=1, last;
        }
        $pos += $len2;
    }
    $err and $exifTool->Warn('Error reading RIFF file (corrupted?)');
    return 1;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::RIFF - Read RIFF/WAV/AVI meta information

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains routines required by Image::ExifTool to extract
information from RIFF-based (Resource Interchange File Format) files,
including Windows WAV audio and AVI video files.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.exif.org/Exif2-2.PDF>

=item L<http://www.vlsi.fi/datasheets/vs1011.pdf>

=item L<http://www.music-center.com.br/spec_rif.htm>

=item L<http://www.codeproject.com/audio/wavefiles.asp>

=item L<http://msdn.microsoft.com/archive/en-us/directx9_c/directx/htm/avirifffilereference.asp>

=item L<http://wiki.multimedia.cx/index.php?title=TwoCC>

=back

=head1 SEE ALSO

L<Image::ExifTool::TagNames/RIFF Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

