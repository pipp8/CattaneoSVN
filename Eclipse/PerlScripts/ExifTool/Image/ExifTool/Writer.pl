#------------------------------------------------------------------------------
# File:         Writer.pl
#
# Description:  ExifTool write routines
#
# Notes:        Also contains some less used ExifTool functions
#
# URL:          http://owl.phy.queensu.ca/~phil/exiftool/
#
# Revisions:    12/16/2004 - P. Harvey Created
#------------------------------------------------------------------------------

package Image::ExifTool;

use strict;

use Image::ExifTool::TagLookup qw(FindTagInfo TagExists);
use Image::ExifTool::Fixup;

sub AssembleRational($$@);
sub LastInList($);
sub CreateDirectory($);
sub RemoveNewValueHash($$$);
sub RemoveNewValuesForGroup($$);
sub GetWriteGroup1($$);
sub Sanitize($$);
sub ConvInv($$$$$;$$);

my $loadedAllTables;    # flag indicating we loaded all tables

# the following is a road map of where we write each directory
# in the different types of files.
my %tiffMap = (
    IFD0         => 'TIFF',
    IFD1         => 'IFD0',
    XMP          => 'IFD0',
    ICC_Profile  => 'IFD0',
    ExifIFD      => 'IFD0',
    GPS          => 'IFD0',
    SubIFD       => 'IFD0',
    GlobParamIFD => 'IFD0',
    PrintIM      => 'IFD0',
    IPTC         => 'IFD0',
    Photoshop    => 'IFD0',
    InteropIFD   => 'ExifIFD',
    MakerNotes   => 'ExifIFD',
    CanonVRD     => 'MakerNotes', # (so VRDOffset will get updated)
    NikonCapture => 'MakerNotes', # (to allow delete by group)
);
my %exifMap = (
    IFD1         => 'IFD0',
    EXIF         => 'IFD0', # to write EXIF as a block
    ExifIFD      => 'IFD0',
    GPS          => 'IFD0',
    SubIFD       => 'IFD0',
    GlobParamIFD => 'IFD0',
    PrintIM      => 'IFD0',
    InteropIFD   => 'ExifIFD',
    MakerNotes   => 'ExifIFD',
    NikonCapture => 'MakerNotes', # (to allow delete by group)
    # (no CanonVRD trailer allowed)
);
my %jpegMap = (
    %exifMap, # covers all JPEG EXIF mappings
    JFIF         => 'APP0',
    CIFF         => 'APP0',
    IFD0         => 'APP1',
    XMP          => 'APP1',
    ICC_Profile  => 'APP2',
    FlashPix     => 'APP2',
    Meta         => 'APP3',
    MetaIFD      => 'Meta',
    RMETA        => 'APP5',
    Ducky        => 'APP12',
    Photoshop    => 'APP13',
    IPTC         => 'Photoshop',
    MakerNotes   => ['ExifIFD', 'CIFF'], # (first parent is the default)
    CanonVRD     => 'MakerNotes', # (so VRDOffset will get updated)
    NikonCapture => 'MakerNotes', # (to allow delete by group)
    Comment      => 'COM',
);
my %dirMap = (
    JPEG => \%jpegMap,
    TIFF => \%tiffMap,
    ORF  => \%tiffMap,
    RAW  => \%tiffMap,
    EXIF => \%exifMap,
);

# groups we are allowed to delete
# Notes:
# 1) these names must either exist in %dirMap, or be translated in InitWriteDirs())
# 2) any dependencies must be added to %excludeGroups
my @delGroups = qw(
    AFCP CanonVRD CIFF Ducky EXIF ExifIFD File FlashPix FotoStation GlobParamIFD
    GPS ICC_Profile IFD0 IFD1 InteropIFD IPTC JFIF MakerNotes Meta MetaIFD MIE
    NikonCapture PDF PDF-update PhotoMechanic Photoshop PNG PrintIM RMETA RSRC
    SubIFD Trailer XML XML-* XMP XMP-*
);
# other group names of new tag values to remove when deleting an entire group
my %removeGroups = (
    IFD0    => [ 'EXIF', 'MakerNotes' ],
    EXIF    => [ 'MakerNotes' ],
    ExifIFD => [ 'MakerNotes', 'InteropIFD' ],
    Trailer => [ 'CanonVRD' ], #(because we can add back CanonVRD as a block)
);
# related family 0/1 groups in @delGroups (and not already in %jpegMap)
# that must be removed from delete list when excluding a group
my %excludeGroups = (
    EXIF         => [ qw(IFD0 IFD1 ExifIFD GPS MakerNotes GlobParamIFD InteropIFD PrintIM SubIFD) ],
    IFD0         => [ 'EXIF' ],
    IFD1         => [ 'EXIF' ],
    ExifIFD      => [ 'EXIF' ],
    GPS          => [ 'EXIF' ],
    MakerNotes   => [ 'EXIF' ],
    InteropIFD   => [ 'EXIF' ],
    GlobParamIFD => [ 'EXIF' ],
    PrintIM      => [ 'EXIF' ],
    CIFF         => [ 'MakerNotes' ],
    # technically correct, but very uncommon and not a good reason to avoid deleting trailer
  # IPTC         => [ qw(AFCP FotoStation Trailer) ],
    AFCP         => [ 'Trailer' ],
    FotoStation  => [ 'Trailer' ],
    CanonVRD     => [ 'Trailer' ],
    PhotoMechanic=> [ 'Trailer' ],
    MIE          => [ 'Trailer' ],
);
# group names to translate for writing
my %translateWriteGroup = (
    EXIF => 'ExifIFD',
    Meta => 'MetaIFD',
    File => 'Comment',
    MIE  => 'MIE',
);
# names of valid EXIF and Meta directories:
my %exifDirs = (
    gps          => 'GPS',
    exififd      => 'ExifIFD',
    subifd       => 'SubIFD',
    globparamifd => 'GlobParamIFD',
    interopifd   => 'InteropIFD',
    makernotes   => 'MakerNotes',
    previewifd   => 'PreviewIFD', # (in MakerNotes)
    metaifd      => 'MetaIFD', # Kodak APP3 Meta
);
# min/max values for integer formats
my %intRange = (
    'int8u'  => [0, 0xff],
    'int8s'  => [-0x80, 0x7f],
    'int16u' => [0, 0xffff],
    'int16uRev' => [0, 0xffff],
    'int16s' => [-0x8000, 0x7fff],
    'int32u' => [0, 0xffffffff],
    'int32s' => [-0x80000000, 0x7fffffff],
);
# lookup for file types with block-writable EXIF
my %blockExifTypes = ( JPEG=>1, PNG=>1, JP2=>1, MIE=>1, EXIF=>1 );

my $maxSegmentLen = 0xfffd;     # maximum length of data in a JPEG segment
my $maxXMPLen = $maxSegmentLen; # maximum length of XMP data in JPEG

# value separators when conversion list is used (in SetNewValue)
my %listSep = ( PrintConv => '; ?', ValueConv => ' ' );

# printConv hash keys to ignore when doing reverse lookup
my %ignorePrintConv = ( OTHER => 1, BITMASK => 1, Notes => 1 );

#------------------------------------------------------------------------------
# Set tag value
# Inputs: 0) ExifTool object reference
#         1) tag key, tag name, or '*' (optionally prefixed by group name),
#            or undef to reset all previous SetNewValue() calls
#         2) new value (scalar, scalar ref or list ref), or undef to delete tag
#         3-N) Options:
#           Type => PrintConv, ValueConv or Raw - specifies value type
#           AddValue => true to add to list of existing values instead of overwriting
#           DelValue => true to delete this existing value value from a list
#           Group => family 0 or 1 group name (case insensitive)
#           Replace => 0, 1 or 2 - overwrite previous new values (2=reset)
#           Protected => bitmask to write tags with specified protections
#           EditOnly => true to only edit existing tags (don't create new tag)
#           EditGroup => true to only edit existing groups (don't create new group)
#           Shift => undef, 0, +1 or -1 - shift value if possible
#           NoShortcut => true to prevent looking up shortcut tags
#           CreateGroups => [internal use] createGroups hash ref from related tags
#           ListOnly => [internal use] set only list or non-list tags
#           SetTags => [internal use] hash ref to return tagInfo refs of set tags
# Returns: number of tags set (plus error string in list context)
# Notes: For tag lists (like Keywords), call repeatedly with the same tag name for
#        each value in the list.  Internally, the new information is stored in
#        the following members of the $self->{NEW_VALUE}{$tagInfo} hash:
#           TagInfo - tag info ref
#           DelValue - list ref for values to delete
#           Value - list ref for values to add
#           IsCreating - must be set for the tag to be added, otherwise just
#                        changed if it already exists.  Set to 2 to not create group
#           CreateGroups - hash of all family 0 group names where tag may be created
#           WriteGroup - group name where information is being written (correct case)
#           WantGroup - group name as specified in call to function (case insensitive)
#           Next - pointer to next new value hash (if more than one)
#           IsNVH - Flag indicating this is a new value hash
#           Shift - shift value
#           MAKER_NOTE_FIXUP - pointer to fixup if necessary for a maker note value
sub SetNewValue($;$$%)
{
    local $_;
    my ($self, $tag, $value, %options) = @_;
    my ($err, $tagInfo);
    my $verbose = $self->{OPTIONS}{Verbose};
    my $out = $self->{OPTIONS}{TextOut};
    my $protected = $options{Protected} || 0;
    my $listOnly = $options{ListOnly};
    my $setTags = $options{SetTags};
    my $numSet = 0;

    unless (defined $tag) {
        delete $self->{NEW_VALUE};
        $self->{DEL_GROUP} = { };
        return 1;
    }
    # allow value to be scalar or list reference
    if (ref $value) {
        if (ref $value eq 'ARRAY') {
            # (since value is an ARRAY, it will have more than one entry)
            # set all list-type tags first
            my $replace = $options{Replace};
            foreach (@$value) {
                my ($n, $e) = SetNewValue($self, $tag, $_, %options, ListOnly => 1);
                $err = $e if $e;
                $numSet += $n;
                delete $options{Replace}; # don't replace earlier values in list
            }
            # and now set only non-list tags
            $value = join $self->{OPTIONS}{ListSep}, @$value;
            $options{Replace} = $replace;
            $listOnly = $options{ListOnly} = 0;
        } elsif (ref $value eq 'SCALAR') {
            $value = $$value;
        }
    }
    # un-escape as necessary and make sure the Perl UTF-8 flag is OFF for the value
    # if perl is 5.6 or greater (otherwise our byte manipulations get corrupted!!)
    $self->Sanitize(\$value) if defined $value and not ref $value;

    # set group name in options if specified
    if ($tag =~ /(.*):(.+)/) {
        $options{Group} = $1 if $1 ne '*' and lc($1) ne 'all';
        $tag = $2;
    }
    # allow trailing '#' for ValueConv value
    $options{Type} = 'ValueConv' if $tag =~ s/#$//;
    # ignore leading family number if 0 or 1 specified
    if ($options{Group} and $options{Group} =~ /^(\d+)(.*)/ and $1 < 2) {
        $options{Group} = $2;
    }
#
# get list of tags we want to set
#
    my $wantGroup = $options{Group};
    $tag =~ s/ .*//;    # convert from tag key to tag name if necessary
    my @matchingTags = FindTagInfo($tag);
    until (@matchingTags) {
        if ($tag eq '*' or lc($tag) eq 'all') {
            # set groups to delete
            if (defined $value) {
                $err = "Can't set value for all tags";
            } else {
                my (@del, $grp);
                my $remove = ($options{Replace} and $options{Replace} > 1);
                if ($wantGroup) {
                    @del = grep /^$wantGroup$/i, @delGroups unless $wantGroup =~ /^XM[LP]-\*$/i;
                    # remove associated groups when excluding from mass delete
                    if (@del and $remove) {
                        # remove associated groups in other family
                        push @del, @{$excludeGroups{$del[0]}} if $excludeGroups{$del[0]};
                        # remove upstream groups according to JPEG map
                        my $dirName = $del[0];
                        my @dirNames;
                        for (;;) {
                            my $parent = $jpegMap{$dirName};
                            if (ref $parent) {
                                push @dirNames, @$parent;
                                $parent = pop @dirNames;
                            }
                            $dirName = $parent || shift @dirNames or last;
                            push @del, $dirName;    # exclude this too
                        }
                    }
                    # allow MIE groups to be deleted by number,
                    # and allow any XMP family 1 group to be deleted
                    push @del, uc($wantGroup) if $wantGroup =~ /^(MIE\d+|XM[LP]-[-\w]+)$/i;
                } else {
                    # push all groups plus '*', except IFD1 and a few others
                    push @del, (grep !/^(IFD1|SubIFD|InteropIFD|GlobParamIFD|PDF-update)$/, @delGroups), '*';
                }
                if (@del) {
                    ++$numSet;
                    my @donegrps;
                    my $delGroup = $self->{DEL_GROUP};
                    foreach $grp (@del) {
                        if ($remove) {
                            my $didExcl;
                            if ($grp =~ /^(XM[LP])(-.*)?$/) {
                                my $x = $1;
                                if ($grp eq $x) {
                                    # exclude all related family 1 groups too
                                    foreach (keys %$delGroup) {
                                        next unless /^-?$x-/;
                                        push @donegrps, $_ if /^$x/;
                                        delete $$delGroup{$_};
                                    }
                                } elsif ($$delGroup{"$x-*"} and not $$delGroup{"-$grp"}) {
                                    # must also exclude XMP or XML to prevent bulk delete
                                    if ($$delGroup{$x}) {
                                        push @donegrps, $x;
                                        delete $$delGroup{$x};
                                    }
                                    # flag XMP/XML family 1 group for exclusion with leading '-'
                                    $$delGroup{"-$grp"} = 1;
                                    $didExcl = 1;
                                }
                            }
                            if (exists $$delGroup{$grp}) {
                                delete $$delGroup{$grp};
                            } else {
                                next unless $didExcl;
                            }
                        } else {
                            $$delGroup{$grp} = 1;
                            # add flag for XMP/XML family 1 groups if deleting all XMP
                            if ($grp =~ /^XM[LP]$/) {
                                $$delGroup{"$grp-*"} = 1;
                                push @donegrps, "$grp-*";
                            }
                            # remove all of this group from previous new values
                            $self->RemoveNewValuesForGroup($grp);
                        }
                        push @donegrps, $grp;
                    }
                    if ($verbose > 1 and @donegrps) {
                        @donegrps = sort @donegrps;
                        my $msg = $remove ? 'Excluding from deletion' : 'Deleting tags in';
                        print $out "  $msg: @donegrps\n";
                    }
                } else {
                    $err = "Not a deletable group: $wantGroup";
                }
            }
        } else {
            my $origTag = $tag;
            my $langCode;
            # allow language suffix of form "-en_CA" or "-<rfc3066>" on tag name
            if ($tag =~ /^(\w+)-([a-z]{2})(_[a-z]{2})$/i or # MIE
                $tag =~ /^(\w+)-([a-z]{2,3}|[xi])(-[a-z\d]{2,8}(-[a-z\d]{1,8})*)?$/i) # XMP/PNG
            {
                $tag = $1;
                # normalize case of language codes
                $langCode = lc($2);
                $langCode .= (length($3) == 3 ? uc($3) : lc($3)) if $3;
                my @newMatches = FindTagInfo($tag);
                foreach $tagInfo (@newMatches) {
                    # only allow language codes in tables which support them
                    next unless $$tagInfo{Table};
                    my $langInfoProc = $tagInfo->{Table}{LANG_INFO} or next;
                    my $langInfo = &$langInfoProc($tagInfo, $langCode);
                    push @matchingTags, $langInfo if $langInfo;
                }
                last if @matchingTags;
            } else {
                # look for a shortcut or alias
                require Image::ExifTool::Shortcuts;
                my ($match) = grep /^\Q$tag\E$/i, keys %Image::ExifTool::Shortcuts::Main;
                undef $err;
                if ($match and not $options{NoShortcut}) {
                    if (@{$Image::ExifTool::Shortcuts::Main{$match}} == 1) {
                        $tag = $Image::ExifTool::Shortcuts::Main{$match}[0];
                        @matchingTags = FindTagInfo($tag);
                        last if @matchingTags;
                    } else {
                        $options{NoShortcut} = 1;
                        foreach $tag (@{$Image::ExifTool::Shortcuts::Main{$match}}) {
                            my ($n, $e) = $self->SetNewValue($tag, $value, %options);
                            $numSet += $n;
                            $e and $err = $e;
                        }
                        undef $err if $numSet;  # no error if any set successfully
                        return ($numSet, $err) if wantarray;
                        $err and warn "$err\n";
                        return $numSet;
                    }
                }
            }
            if (not TagExists($tag)) {
                $err = "Tag '$origTag' does not exist";
                $err .= ' or has a bad language code' if $origTag =~ /-/;
            } elsif ($langCode) {
                $err = "Tag '$tag' does not support alternate languages";
            } elsif ($wantGroup) {
                $err = "Sorry, $wantGroup:$origTag doesn't exist or isn't writable";
            } else {
                $err = "Sorry, $origTag is not writable";
            }
            $verbose > 2 and print $out "$err\n";
        }
        # all done
        return ($numSet, $err) if wantarray;
        $err and warn "$err\n";
        return $numSet;
    }
    # get group name that we're looking for
    my $foundMatch = 0;
    my ($ifdName, $mieGroup);
    if ($wantGroup) {
        # set $ifdName if this group is a valid IFD or SubIFD name
        if ($wantGroup =~ /^IFD(\d+)$/i) {
            $ifdName = "IFD$1";
        } elsif ($wantGroup =~ /^SubIFD(\d+)$/i) {
            $ifdName = "SubIFD$1";
        } elsif ($wantGroup =~ /^Version(\d+)$/i) {
            $ifdName = "Version$1"; # Sony IDC VersionIFD
        } elsif ($wantGroup =~ /^MIE(\d*-?)(\w+)$/i) {
            $mieGroup = "MIE$1" . ucfirst(lc($2));
        } else {
            $ifdName = $exifDirs{lc($wantGroup)};
            if (not $ifdName and $wantGroup =~ /^XMP\b/i) {
                # must load XMP table to set group1 names
                my $table = GetTagTable('Image::ExifTool::XMP::Main');
                my $writeProc = $table->{WRITE_PROC};
                $writeProc and &$writeProc();
            }
        }
    }
#
# determine the groups for all tags found, and the tag with
# the highest priority group
#
    my (@tagInfoList, @writeAlsoList, %writeGroup, %preferred, %tagPriority, $avoid, $wasProtected);
    my $highestPriority = -1;
    foreach $tagInfo (@matchingTags) {
        $tag = $tagInfo->{Name};    # set tag so warnings will use proper case
        my ($writeGroup, $priority);
        if ($wantGroup) {
            my $lcWant = lc($wantGroup);
            # only set tag in specified group
            $writeGroup = $self->GetGroup($tagInfo, 0);
            unless (lc($writeGroup) eq $lcWant) {
                if ($writeGroup eq 'EXIF' or $writeGroup eq 'SonyIDC') {
                    next unless $ifdName;
                    # can't yet write PreviewIFD tags
                    $ifdName eq 'PreviewIFD' and ++$foundMatch, next;
                    $writeGroup = $ifdName;  # write to the specified IFD
                } elsif ($writeGroup eq 'MIE') {
                    next unless $mieGroup;
                    $writeGroup = $mieGroup; # write to specific MIE group
                    # set specific write group with document number if specified
                    if ($writeGroup =~ /^MIE\d+$/ and $tagInfo->{Table}{WRITE_GROUP}) {
                        $writeGroup = $tagInfo->{Table}{WRITE_GROUP};
                        $writeGroup =~ s/^MIE/$mieGroup/;
                    }
                } elsif (not $$tagInfo{AllowGroup} or $wantGroup !~ /^$$tagInfo{AllowGroup}$/i) {
                    # allow group1 name to be specified
                    my $grp1 = $self->GetGroup($tagInfo, 1);
                    unless ($grp1 and lc($grp1) eq $lcWant) {
                        # must also check group1 name directly in case it is different
                        $grp1 = $tagInfo->{Groups}{1};
                        next unless $grp1 and lc($grp1) eq $lcWant;
                    }
                }
            }
            $priority = 1000;   # highest priority since group was specified
        }
        ++$foundMatch;
        # must do a dummy call to the write proc to autoload write package
        # before checking Writable flag
        my $table = $tagInfo->{Table};
        my $writeProc = $table->{WRITE_PROC};
        # load source table if this was a user-defined table
        if ($$table{SRC_TABLE}) {
            my $src = GetTagTable($$table{SRC_TABLE});
            $writeProc = $$src{WRITE_PROC} unless $writeProc;
        }
        next unless $writeProc and &$writeProc();
        # must still check writable flags in case of UserDefined tags
        my $writable = $tagInfo->{Writable};
        next unless $writable or ($table->{WRITABLE} and
            not defined $writable and not $$tagInfo{SubDirectory});
        # set specific write group (if we didn't already)
        if (not $writeGroup or $translateWriteGroup{$writeGroup}) {
            # use default write group
            $writeGroup = $tagInfo->{WriteGroup} || $tagInfo->{Table}{WRITE_GROUP};
            # use group 0 name if no WriteGroup specified
            my $group0 = $self->GetGroup($tagInfo, 0);
            $writeGroup or $writeGroup = $group0;
            # get priority for this group
            unless ($priority) {
                $priority = $self->{WRITE_PRIORITY}{lc($writeGroup)};
                unless ($priority) {
                    $priority = $self->{WRITE_PRIORITY}{lc($group0)} || 0;
                }
            }
        }
        # don't write tag if protected
        if ($tagInfo->{Protected}) {
            my $prot = $tagInfo->{Protected} & ~$protected;
            if ($prot) {
                my %lkup = ( 1=>'unsafe', 2=>'protected', 3=>'unsafe and protected');
                $wasProtected = $lkup{$prot};
                if ($verbose > 1) {
                    my $wgrp1 = $self->GetWriteGroup1($tagInfo, $writeGroup);
                    print $out "Not writing $wgrp1:$tag ($wasProtected)\n";
                }
                next;
            }
        }
        # set priority for this tag
        $tagPriority{$tagInfo} = $priority;
        if ($priority > $highestPriority) {
            $highestPriority = $priority;
            %preferred = ( $tagInfo => 1 );
            $avoid = 0;
            ++$avoid if $$tagInfo{Avoid};
        } elsif ($priority == $highestPriority) {
            # create all tags with highest priority
            $preferred{$tagInfo} = 1;
            ++$avoid if $$tagInfo{Avoid};
        }
        if ($$tagInfo{WriteAlso}) {
            # store WriteAlso tags separately so we can set them first
            push @writeAlsoList, $tagInfo;
        } else {
            push @tagInfoList, $tagInfo;
        }
        $writeGroup{$tagInfo} = $writeGroup;
    }
    # sort tag info list in reverse order of priority (higest number last)
    # so we get the highest priority error message in the end
    @tagInfoList = sort { $tagPriority{$a} <=> $tagPriority{$b} } @tagInfoList;
    # must write any tags which also write other tags first
    unshift @tagInfoList, @writeAlsoList if @writeAlsoList;

    # don't create tags with priority 0 if group priorities are set
    if ($highestPriority == 0 and %{$self->{WRITE_PRIORITY}}) {
        undef %preferred;
    }
    # avoid creating tags with 'Avoid' flag set if there are other alternatives
    if ($avoid and %preferred) {
        if ($avoid < scalar(keys %preferred)) {
            # just remove the 'Avoid' tags since there are other preferred tags
            foreach $tagInfo (@tagInfoList) {
                delete $preferred{$tagInfo} if $$tagInfo{Avoid};
            }
        } elsif ($highestPriority < 1000) {
            # look for another priority tag to create instead
            my $nextHighest = 0;
            my @nextBestTags;
            foreach $tagInfo (@tagInfoList) {
                my $priority = $tagPriority{$tagInfo} or next;
                next if $priority == $highestPriority;
                next if $priority < $nextHighest;
                next if $$tagInfo{Avoid} or $$tagInfo{Permanent};
                next if $writeGroup{$tagInfo} eq 'MakerNotes';
                if ($nextHighest < $priority) {
                    $nextHighest = $priority;
                    undef @nextBestTags;
                }
                push @nextBestTags, $tagInfo;
            }
            if (@nextBestTags) {
                # change our preferred tags to the next best tags
                undef %preferred;
                foreach $tagInfo (@nextBestTags) {
                    $preferred{$tagInfo} = 1;
                }
            }
        }
    }
#
# generate new value hash for each tag
#
    my ($prioritySet, $createGroups, %alsoWrote);

    delete $$self{CHECK_WARN};  # reset CHECK_PROC warnings

    # loop through all valid tags to find the one(s) to write
    foreach $tagInfo (@tagInfoList) {
        next if $alsoWrote{$tagInfo};   # don't rewrite tags we already wrote
        # only process List or non-List tags if specified
        next if defined $listOnly and ($listOnly xor $$tagInfo{List});
        my $noConv;
        my $writeGroup = $writeGroup{$tagInfo};
        my $permanent = $$tagInfo{Permanent};
        $writeGroup eq 'MakerNotes' and $permanent = 1 unless defined $permanent;
        my $wgrp1 = $self->GetWriteGroup1($tagInfo, $writeGroup);
        $tag = $tagInfo->{Name};    # get proper case for tag name
        my $shift = $options{Shift};
        if (defined $shift) {
            # (can't currently shift list-type tags)
            if (not $tagInfo->{List}) {
                unless ($shift) {
                    # set shift according to AddValue/DelValue
                    $shift = 1 if $options{AddValue};
                    if ($options{DelValue}) {
                        # can shift a date/time with -=, but this is
                        # a conditional delete operation for other tags
                        $shift = -1 if $$tagInfo{Shift} and $$tagInfo{Shift} eq 'Time';
                    }
                }
                if ($shift and (not defined $value or not length $value)) {
                    # (now allow -= to be used for shiftable tag - v8.05)
                    #$err = "No value for time shift of $wgrp1:$tag";
                    #$verbose > 2 and print $out "$err\n";
                    #next;
                    undef $shift;
                }
            } elsif ($shift) {
                $err = "$wgrp1:$tag is not shiftable";
                $verbose > 2 and print $out "$err\n";
                next;
            }
        }
        my $val = $value;
        if (defined $val) {
            # check to make sure this is a List or Shift tag if adding
            if ($options{AddValue} and not ($shift or $tagInfo->{List})) {
                $err = "Can't add $wgrp1:$tag (not a List type)";
                $verbose > 2 and print $out "$err\n";
                next;
            }
            if ($shift) {
                if ($$tagInfo{Shift} and $$tagInfo{Shift} eq 'Time') {
                    # add '+' or '-' prefix to indicate shift direction
                    $val = ($shift > 0 ? '+' : '-') . $val;
                    # check the shift for validity
                    require 'Image/ExifTool/Shift.pl';
                    my $err2 = CheckShift($tagInfo->{Shift}, $val);
                    if ($err2) {
                        $err = "$err2 for $wgrp1:$tag";
                        $verbose > 2 and print $out "$err\n";
                        next;
                    }
                } elsif (IsFloat($val)) {
                    $val *= $shift;
                } else {
                    $err = "Shift value for $wgrp1:$tag is not a number";
                    $verbose > 2 and print $out "$err\n";
                    next;
                }
                $noConv = 1;    # no conversions if shifting tag
            } elsif (not length $val and $options{DelValue}) {
                $noConv = 1;    # no conversions for deleting empty value
            } elsif (ref $val eq 'HASH' and not $$tagInfo{Struct}) {
                $err = "Can't write a structure to $wgrp1:$tag";
                $verbose > 2 and print $out "$err\n";
                next;
            }
        } elsif ($permanent) {
            # can't delete permanent tags, so set them to DelValue or empty string instead
            if (defined $$tagInfo{DelValue}) {
                $val = $$tagInfo{DelValue};
                $noConv = 1;    # DelValue is the raw value, so no conversion necessary
            } else {
                $val = '';
            }
        } elsif ($options{AddValue} or $options{DelValue}) {
            $err = "No value to add or delete in $wgrp1:$tag";
            $verbose > 2 and print $out "$err\n";
            next;
        } else {
            if ($tagInfo->{DelCheck}) {
                #### eval DelCheck ($self, $tagInfo, $wantGroup)
                my $err2 = eval $tagInfo->{DelCheck};
                $@ and warn($@), $err2 = 'Error evaluating DelCheck';
                if ($err2) {
                    $err2 .= ' for' unless $err2 =~ /delete$/;
                    $err = "$err2 $wgrp1:$tag";
                    $verbose > 2 and print $out "$err\n";
                    next;
                } elsif (defined $err2) {
                    ++$numSet;  # (allow other tags to be set using DelCheck as a hook)
                    goto WriteAlso;
                }
            }
            $noConv = 1;    # value is not defined, so don't do conversion
        }
        # apply inverse PrintConv and ValueConv conversions
        # save ValueConv setting for use in ConvInv()
        unless ($noConv) {
            # set default conversion type used by ConvInv() and CHECK_PROC routines
            $$self{ConvType} = $options{Type} || ($self->{OPTIONS}{PrintConv} ? 'PrintConv' : 'ValueConv');
            my $e;
            ($val,$e) = $self->ConvInv($val, $tagInfo, $tag, $wgrp1, $$self{ConvType}, $wantGroup);
            if (defined $e) {
                if ($e) {
                    ($err = $e) =~ s/\$wgrp1/$wgrp1/g;
                } else {
                    ++$numSet;  # an empty error string causes error to be ignored
                }
            }
        }
        if (not defined $val and defined $value) {
            # if value conversion failed, we must still add a NEW_VALUE
            # entry for this tag it it was a DelValue
            next unless $options{DelValue};
            $val = 'xxx never delete xxx';
        }
        $self->{NEW_VALUE} or $self->{NEW_VALUE} = { };
        if ($options{Replace}) {
            # delete the previous new value
            $self->GetNewValueHash($tagInfo, $writeGroup, 'delete');
            # also delete related tag previous new values
            if ($$tagInfo{WriteAlso}) {
                my $wtag;
                foreach $wtag (keys %{$$tagInfo{WriteAlso}}) {
                    my ($n,$e) = $self->SetNewValue($wtag, undef, Replace=>2);
                    $numSet += $n;
                }
            }
            $options{Replace} == 2 and ++$numSet, next;
        }

        if (defined $val) {
            # we are editing this tag, so create a NEW_VALUE hash entry
            my $nvHash = $self->GetNewValueHash($tagInfo, $writeGroup, 'create');
            $nvHash->{WantGroup} = $wantGroup;
            # save maker note information if writing maker notes
            if ($$tagInfo{MakerNotes}) {
                $nvHash->{MAKER_NOTE_FIXUP} = $self->{MAKER_NOTE_FIXUP};
            }
            if ($options{DelValue} or $options{AddValue} or $shift) {
                # flag any AddValue or DelValue by creating the DelValue list
                $nvHash->{DelValue} or $nvHash->{DelValue} = [ ];
                if ($shift) {
                    # add shift value to list
                    $nvHash->{Shift} = $val;
                } elsif ($options{DelValue}) {
                    # don't create if we are replacing a specific value
                    $nvHash->{IsCreating} = 0 unless $val eq '' or $tagInfo->{List};
                    # add delete value to list
                    push @{$nvHash->{DelValue}}, ref $val eq 'ARRAY' ? @$val : $val;
                    if ($verbose > 1) {
                        my $verb = $permanent ? 'Replacing' : 'Deleting';
                        my $fromList = $tagInfo->{List} ? ' from list' : '';
                        my @vals = (ref $val eq 'ARRAY' ? @$val : $val);
                        foreach (@vals) {
                            if (ref $_ eq 'HASH') {
                                require 'Image/ExifTool/XMPStruct.pl';
                                $_ = Image::ExifTool::XMP::SerializeStruct($_);
                            }
                            print $out "$verb $wgrp1:$tag$fromList if value is '$_'\n";
                        }
                    }
                }
            }
            # set priority flag to add only the high priority info
            # (will only create the priority tag if it doesn't exist,
            #  others get changed only if they already exist)
            if ($preferred{$tagInfo} or $tagInfo->{Table}{PREFERRED}) {
                if ($permanent or $shift) {
                    # don't create permanent or Shift-ed tag but define IsCreating
                    # so we know that it is the preferred tag
                    $nvHash->{IsCreating} = 0;
                } elsif (($tagInfo->{List} and not $options{DelValue}) or
                         not ($nvHash->{DelValue} and @{$nvHash->{DelValue}}) or
                         # also create tag if any DelValue value is empty ('')
                         grep(/^$/,@{$nvHash->{DelValue}}))
                {
                    $nvHash->{IsCreating} = $options{EditOnly} ? 0 : ($options{EditGroup} ? 2 : 1);
                    # add to hash of groups where this tag is being created
                    $createGroups or $createGroups = $options{CreateGroups} || { };
                    $$createGroups{$self->GetGroup($tagInfo, 0)} = 1;
                    $nvHash->{CreateGroups} = $createGroups;
                }
            }
            if (%{$self->{DEL_GROUP}} and $nvHash->{IsCreating}) {
                my ($grp, @grps);
                foreach $grp (keys %{$self->{DEL_GROUP}}) {
                    next if $self->{DEL_GROUP}{$grp} == 2;
                    # set flag indicating tags were written after this group was deleted
                    $self->{DEL_GROUP}{$grp} = 2;
                    push @grps, $grp;
                }
                if ($verbose > 1 and @grps) {
                    @grps = sort @grps;
                    print $out "  Writing new tags after deleting groups: @grps\n";
                }
            }
            if ($shift or not $options{DelValue}) {
                $nvHash->{Value} or $nvHash->{Value} = [ ];
                if (not $tagInfo->{List}) {
                    # not a List tag -- overwrite existing value
                    $nvHash->{Value}[0] = $val;
                } else {
                    # add to existing list
                    push @{$nvHash->{Value}}, ref $val eq 'ARRAY' ? @$val : $val;
                }
                if ($verbose > 1) {
                    my $ifExists = $nvHash->{IsCreating} ?
                                  ($nvHash->{IsCreating} == 2 ? " if $writeGroup exists" : '') :
                                  (($nvHash->{DelValue} and @{$nvHash->{DelValue}}) ?
                                   ' if tag was deleted' : ' if tag exists');
                    my $verb = ($shift ? 'Shifting' : ($options{AddValue} ? 'Adding' : 'Writing'));
                    print $out "$verb $wgrp1:$tag$ifExists\n";
                }
            }
        } elsif ($permanent) {
            $err = "Can't delete $wgrp1:$tag";
            $verbose > 1 and print $out "$err\n";
            next;
        } elsif ($options{AddValue} or $options{DelValue}) {
            $verbose > 1 and print $out "Adding/Deleting nothing does nothing\n";
            next;
        } else {
            # create empty new value hash entry to delete this tag
            $self->GetNewValueHash($tagInfo, $writeGroup, 'delete');
            my $nvHash = $self->GetNewValueHash($tagInfo, $writeGroup, 'create');
            $nvHash->{WantGroup} = $wantGroup;
            $verbose > 1 and print $out "Deleting $wgrp1:$tag\n";
        }
        ++$numSet;
        $$setTags{$tagInfo} = 1 if $setTags;
        $prioritySet = 1 if $preferred{$tagInfo};
WriteAlso:
        # also write related tags
        my $writeAlso = $$tagInfo{WriteAlso};
        if ($writeAlso) {
            my ($wtag, $n);
            local $SIG{'__WARN__'} = \&SetWarning;
            foreach $wtag (keys %$writeAlso) {
                my %opts = (
                    Type => 'ValueConv',
                    Protected => $protected | 0x02,
                    AddValue => $options{AddValue},
                    DelValue => $options{DelValue},
                    CreateGroups => $createGroups,
                    SetTags => \%alsoWrote, # remember tags already written
                );
                undef $evalWarning;
                #### eval WriteAlso ($val)
                my $v = eval $writeAlso->{$wtag};
                $@ and $evalWarning = $@;
                unless ($evalWarning) {
                    ($n,$evalWarning) = $self->SetNewValue($wtag, $v, %opts);
                    $numSet += $n;
                    # count this as being set if any related tag is set
                    $prioritySet = 1 if $n and $preferred{$tagInfo};
                }
                if ($evalWarning and (not $err or $verbose > 2)) {
                    my $str = CleanWarning();
                    if ($str) {
                        $str .= " for $wtag" unless $str =~ / for [-\w:]+$/;
                        $str .= " in $wgrp1:$tag (WriteAlso)";
                        $err or $err = $str;
                        print $out "$str\n" if $verbose > 2;
                    }
                }
            }
        }
    }
    # print warning if we couldn't set our priority tag
    if (defined $err and not $prioritySet) {
        warn "$err\n" if $err and not wantarray;
    } elsif (not $numSet) {
        my $pre = $wantGroup ? ($ifdName || $wantGroup) . ':' : '';
        if ($wasProtected) {
            $err = "Tag '$pre$tag' is $wasProtected for writing";
        } elsif ($foundMatch) {
            $err = "Sorry, $pre$tag is not writable";
        } else {
            $err = "Tag '$pre$tag' does not exist";
        }
        $verbose > 2 and print $out "$err\n";
        warn "$err\n" unless wantarray;
    } elsif ($$self{CHECK_WARN}) {
        $err = $$self{CHECK_WARN};
        $verbose > 2 and print $out "$err\n";
    } elsif ($err and not $verbose) {
        undef $err;
    }
    return ($numSet, $err) if wantarray;
    return $numSet;
}

#------------------------------------------------------------------------------
# set new values from information in specified file
# Inputs: 0) ExifTool object reference, 1) source file name or reference, etc
#         2-N) List of tags to set (or all if none specified), or reference(s) to
#         hash for options to pass to SetNewValue.  The Replace option defaults
#         to 1 for SetNewValuesFromFile -- set this to 0 to allow multiple tags
#         to be copied to a list
# Returns: Hash of information set successfully (includes Warning or Error messages)
# Notes: Tag names may contain a group prefix, a leading '-' to exclude from copy,
#        and/or a trailing '#' to copy the ValueConv value.  The tag name '*' may
#        be used to represent all tags in a group.  An optional destination tag
#        may be specified with '>DSTTAG' ('DSTTAG<TAG' also works, but in this
#        case the source tag may also be an expression involving tag names).
sub SetNewValuesFromFile($$;@)
{
    local $_;
    my ($self, $srcFile, @setTags) = @_;
    my $key;

    # get initial SetNewValuesFromFile options
    my %opts = ( Replace => 1 );    # replace existing list items by default
    while (ref $setTags[0] eq 'HASH') {
        $_ = shift @setTags;
        foreach $key (keys %$_) {
            $opts{$key} = $_->{$key};
        }
    }
    # expand shortcuts
    @setTags and ExpandShortcuts(\@setTags);
    my $srcExifTool = new Image::ExifTool;
    my $options = $self->{OPTIONS};
    # set options for our extraction tool
    $srcExifTool->{TAGS_FROM_FILE} = 1;
    # +------------------------------------------+
    # ! DON'T FORGET!!  Must consider each new   !
    # ! option to decide how it is handled here. !
    # +------------------------------------------+
    $srcExifTool->Options(
        Binary          => 1,
        Charset         => $$options{Charset},
        CharsetID3      => $$options{CharsetID3},
        CharsetIPTC     => $$options{CharsetIPTC},
        CharsetPhotoshop=> $$options{CharsetPhotoshop},
        Composite       => $$options{Composite},
        CoordFormat     => $$options{CoordFormat} || '%d %d %.8f', # copy coordinates at high resolution unless otherwise specified
        DateFormat      => $$options{DateFormat},
        Duplicates      => 1,
        Escape          => $$options{Escape},
        ExtractEmbedded => $$options{ExtractEmbedded},
        FastScan        => $$options{FastScan},
        FixBase         => $$options{FixBase},
        IgnoreMinorErrors=>$$options{IgnoreMinorErrors},
        Lang            => $$options{Lang},
        LargeFileSupport=> $$options{LargeFileSupport},
        List            => 1,
        MakerNotes      => 1,
        MissingTagValue => $$options{MissingTagValue},
        Password        => $$options{Password},
        PrintConv       => $$options{PrintConv},
        QuickTimeUTC    => $$options{QuickTimeUTC},
        ScanForXMP      => $$options{ScanForXMP},
        StrictDate      => 1,
        Struct          => ($$options{Struct} or not defined $$options{Struct}) ? 1 : 0,
        Unknown         => $$options{Unknown},
    );
    my $printConv = $$options{PrintConv};
    if ($opts{Type}) {
        # save source type separately because it may be different than dst Type
        $opts{SrcType} = $opts{Type};
        # override PrintConv option with initial Type if given
        $printConv = ($opts{Type} eq 'PrintConv' ? 1 : 0);
        $srcExifTool->Options(PrintConv => $printConv);
    }
    my $srcType = $printConv ? 'PrintConv' : 'ValueConv';

    # get all tags from source file (including MakerNotes block)
    my $info = $srcExifTool->ImageInfo($srcFile);
    return $info if $$info{Error} and $$info{Error} eq 'Error opening file';
    delete $srcExifTool->{VALUE}{Error};  # delete so we can check this later

    # sort tags in reverse order so we get priority tag last
    my @tags = reverse sort keys %$info;
    my $tag;
#
# simply transfer all tags from source image if no tags specified
#
    unless (@setTags) {
        # transfer maker note information to this object
        $self->{MAKER_NOTE_FIXUP} = $srcExifTool->{MAKER_NOTE_FIXUP};
        $self->{MAKER_NOTE_BYTE_ORDER} = $srcExifTool->{MAKER_NOTE_BYTE_ORDER};
        foreach $tag (@tags) {
            # don't try to set errors or warnings
            next if $tag =~ /^(Error|Warning)\b/;
            # get approprite value type if necessary
            if ($opts{SrcType} and $opts{SrcType} ne $srcType) {
                $$info{$tag} = $srcExifTool->GetValue($tag, $opts{SrcType});
            }
            # set value for this tag
            my ($n, $e) = $self->SetNewValue($tag, $$info{$tag}, %opts);
            # delete this tag if we could't set it
            $n or delete $$info{$tag};
        }
        return $info;
    }
#
# transfer specified tags in the proper order
#
    # 1) loop through input list of tags to set, and build @setList
    my (@setList, $set, %setMatches);
    foreach (@setTags) {
        if (ref $_ eq 'HASH') {
            # update current options
            foreach $key (keys %$_) {
                $opts{$key} = $_->{$key};
            }
            next;
        }
        # make a copy of the current options for this setTag
        # (also use this hash to store expression and wildcard flags, EXPR and WILD)
        my $opts = { %opts };
        $tag = lc($_);  # change tag/group names to all lower case
        my ($fam, $grp, $dst, $dstGrp, $dstTag, $isExclude);
        # handle redirection to another tag
        if ($tag =~ /(.+?)\s*(>|<)\s*(.+)/) {
            $dstGrp = '';
            my $opt;
            if ($2 eq '>') {
                ($tag, $dstTag) = ($1, $3);
                # flag add and delete (ie. '+<' and '-<') redirections
                $opt = $1 if $tag =~ s/\s*([-+])$// or $dstTag =~ s/^([-+])\s*//;
            } else {
                ($tag, $dstTag) = ($3, $1);
                $opt = $1 if $dstTag =~ s/\s*([-+])$//;
                # handle expressions
                if ($tag =~ /\$/) {
                    $tag = $_;  # restore original case
                    # recover leading whitespace (except for initial single space)
                    $tag =~ s/(.+?)\s*(>|<) ?//;
                    $$opts{EXPR} = 1; # flag this expression
                    $grp = '';
                } else {
                    $opt = $1 if $tag =~ s/^([-+])\s*//;
                }
            }
            # translate '+' and '-' to appropriate SetNewValue option
            if ($opt) {
                $$opts{{ '+' => 'AddValue', '-' => 'DelValue' }->{$opt}} = 1;
                $$opts{Shift} = 0;  # shift if shiftable
            }
            ($dstGrp, $dstTag) = ($1, $2) if $dstTag =~ /(.*):(.+)/;
            # ValueConv may be specified separately on the destination with '#'
            $$opts{Type} = 'ValueConv' if $dstTag =~ s/#$//;
            # ignore leading family number
            $dstGrp = $2 if $dstGrp =~ /^(\d+)(.*)/ and $1 < 2;
            # replace 'all' with '*' in tag and group names
            $dstTag = '*' if $dstTag eq 'all';
            $dstGrp = '*' if $dstGrp eq 'all';
        }
        unless ($$opts{EXPR}) {
            $isExclude = ($tag =~ s/^-//);
            if ($tag =~ /^([-\w]*?|\*):(.+)/) {
                ($grp, $tag) = ($1, $2);
                # separate leading family number
                ($fam, $grp) = ($1, $2) if $grp =~ /^(\d+)(.*)/;
            } else {
                $grp = '';  # flag for don't care about group
            }
            # allow ValueConv to be specified by a '#' on the tag name
            if ($tag =~ s/#$//) {
                $$opts{SrcType} = 'ValueConv';
                $$opts{Type} = 'ValueConv' unless $dstTag;
            }
            # replace 'all' with '*' in tag and group names
            $tag = '*' if $tag eq 'all';
            $grp = '*' if $grp eq 'all';
            # allow wildcards in tag names
            if ($tag =~ /[?*]/ and $tag ne '*') {
                $$opts{WILD} = 1;   # set flag indicating wildcards were used
                $tag =~ s/\*/[-\\w]*/g;
                $tag =~ s/\?/[-\\w]/g;
            }
        }
        # redirect, exclude or set this tag (Note: $grp is '' if we don't care)
        if ($dstTag) {
            # redirect this tag
            $isExclude and return { Error => "Can't redirect excluded tag" };
            if ($dstTag ne '*') {
                if ($dstTag =~ /[?*]/) {
                    if ($dstTag eq $tag) {
                        $dstTag = '*';
                    } else {
                        return { Error => "Invalid use of wildcards in destination tag" };
                    }
                } elsif ($tag eq '*') {
                    return { Error => "Can't redirect from all tags to one tag" };
                }
            }
            # set destination group the same as source if necessary
          # (removed in 7.72 so '-xmp:*>*:*' will preserve XMP family 1 groups)
          # $dstGrp = $grp if $dstGrp eq '*' and $grp;
            # write to specified destination group/tag
            $dst = [ $dstGrp, $dstTag ];
        } elsif ($isExclude) {
            # implicitly assume '*' if first entry is an exclusion
            unshift @setList, [ undef, '*', '*', [ '', '*' ], $opts ] unless @setList;
            # exclude this tag by leaving $dst undefined
        } else {
            $dst = [ $grp, $$opts{WILD} ? '*' : $tag ]; # copy to same group
        }
        $grp or $grp = '*';     # use '*' for any group
        # save in reverse order so we don't set tags before an exclude
        unshift @setList, [ $fam, $grp, $tag, $dst, $opts ];
    }
    # 2) initialize lists of matching tags for each setTag
    foreach $set (@setList) {
        $$set[3] and $setMatches{$set} = [ ];
    }
    # 3) loop through all tags in source image and save tags matching each setTag
    my %rtnInfo;
    foreach $tag (@tags) {
        # don't try to set errors or warnings
        if ($tag =~ /^(Error|Warning)( |$)/) {
            $rtnInfo{$tag} = $$info{$tag};
            next;
        }
        # only set specified tags
        my $lcTag = lc(GetTagName($tag));
        my (@grp, %grp);
        foreach $set (@setList) {
            # check first for matching tag
            unless ($$set[2] eq $lcTag or $$set[2] eq '*') {
                # handle wildcards
                next unless $$set[4]{WILD} and $lcTag =~ /^$$set[2]$/;
            }
            # then check for matching group
            unless ($$set[1] eq '*') {
                # get lower case group names if not done already
                unless (@grp) {
                    @grp = map(lc, $srcExifTool->GetGroup($tag));
                    $grp{$_} = 1 foreach @grp;
                }
                # handle leading family number
                if (defined $$set[0]) {
                    next unless $grp[$$set[0]] and $$set[1] eq $grp[$$set[0]];
                } else {
                    next unless $grp{$$set[1]};
                }
            }
            last unless $$set[3];   # all done if we hit an exclude
            # add to the list of tags matching this setTag
            push @{$setMatches{$set}}, $tag;
        }
    }
    # 4) loop through each setTag in original order, setting new tag values
    foreach $set (reverse @setList) {
        # get options for SetNewValue
        my $opts = $$set[4];
        # handle expressions
        if ($$opts{EXPR}) {
            my $val = $srcExifTool->InsertTagValues(\@tags, $$set[2], 'Error');
            unless (defined $val) {
                # return warning if one of the tags didn't exist
                $tag = NextTagKey(\%rtnInfo, 'Warning');
                $rtnInfo{$tag} = $srcExifTool->GetValue('Error');
                delete $srcExifTool->{VALUE}{Error};
                next;
            }
            my ($dstGrp, $dstTag) = @{$$set[3]};
            $$opts{Protected} = 1;
            $$opts{Group} = $dstGrp if $dstGrp;
            my @rtnVals = $self->SetNewValue($dstTag, $val, %$opts);
            $rtnInfo{$dstTag} = $val if $rtnVals[0]; # tag was set successfully
            next;
        }
        foreach $tag (@{$setMatches{$set}}) {
            my ($val, $noWarn);
            if ($$opts{SrcType} and $$opts{SrcType} ne $srcType) {
                $val = $srcExifTool->GetValue($tag, $$opts{SrcType});
            } else {
                $val = $$info{$tag};
            }
            my ($dstGrp, $dstTag) = @{$$set[3]};
            if ($dstGrp) {
                if ($dstGrp eq '*') {
                    $dstGrp = $srcExifTool->GetGroup($tag, 1);
                    $noWarn = 1;    # don't warn on wildcard destinations
                }
                $$opts{Group} = $dstGrp;
            } else {
                delete $$opts{Group};
            }
            # transfer maker note information if setting this tag
            if ($srcExifTool->{TAG_INFO}{$tag}{MakerNotes}) {
                $self->{MAKER_NOTE_FIXUP} = $srcExifTool->{MAKER_NOTE_FIXUP};
                $self->{MAKER_NOTE_BYTE_ORDER} = $srcExifTool->{MAKER_NOTE_BYTE_ORDER};
            }
            if ($dstTag eq '*') {
                $dstTag = $tag;
                $noWarn = 1;
            }
            # allow protected tags to be copied if specified explicitly
            $$opts{Protected} = ($$set[2] eq '*' ? undef : 1);
            # set value(s) for this tag
            my ($rtn, $wrn) = $self->SetNewValue($dstTag, $val, %$opts);
            if ($wrn and not $noWarn) {
                # return this warning
                $rtnInfo{NextTagKey(\%rtnInfo, 'Warning')} = $wrn;
                $noWarn = 1;
            }
            $rtnInfo{$tag} = $val if $rtn;  # tag was set successfully
        }
    }
    return \%rtnInfo;   # return information that we set
}

#------------------------------------------------------------------------------
# Get new value(s) for tag
# Inputs: 0) ExifTool object reference, 1) tag name or tagInfo hash ref
#         2) optional pointer to return new value hash reference (not part of public API)
#    or   0) ExifTool ref, 1) new value hash reference (not part of public API)
# Returns: List of new Raw values (list may be empty if tag is being deleted)
# Notes: 1) Preferentially returns new value from Extra table if writable Extra tag exists
# 2) Must call AFTER IsOverwriting() returns 1 to get proper value for shifted times
# 3) Tag name is case sensitive and may be prefixed by family 0 or 1 group name
# 4) Value may have been modified by CHECK_PROC routine after ValueConv
sub GetNewValues($$;$)
{
    local $_;
    my $self = shift;
    my $tag = shift;
    my $nvHash;
    if ((ref $tag eq 'HASH' and $$tag{IsNVH}) or not defined $tag) {
        $nvHash = $tag;
    } else {
        my $newValueHashPt = shift;
        if ($self->{NEW_VALUE}) {
            my ($group, $tagInfo);
            if (ref $tag) {
                $nvHash = $self->GetNewValueHash($tag);
            } elsif (defined($tagInfo = $Image::ExifTool::Extra{$tag}) and
                     $$tagInfo{Writable})
            {
                $nvHash = $self->GetNewValueHash($tagInfo);
            } else {
                # separate group from tag name
                $group = $1 if $tag =~ s/(.*)://;
                my @tagInfoList = FindTagInfo($tag);
                # decide which tag we want
GNV_TagInfo:    foreach $tagInfo (@tagInfoList) {
                    my $nvh = $self->GetNewValueHash($tagInfo) or next;
                    # select tag in specified group if necessary
                    while ($group and $group ne $$nvh{WriteGroup}) {
                        my @grps = $self->GetGroup($tagInfo);
                        if ($grps[0] eq $$nvh{WriteGroup}) {
                            # check family 1 group only if WriteGroup is not specific
                            last if $group eq $grps[1];
                        } else {
                            # otherwise check family 0 group
                            last if $group eq $grps[0];
                        }
                        # step to next entry in list
                        $nvh = $$nvh{Next} or next GNV_TagInfo;
                    }
                    $nvHash = $nvh;
                    # give priority to the one we are creating
                    last if defined $nvHash->{IsCreating};
                }
            }
        }
        # return new value hash if requested
        $newValueHashPt and $$newValueHashPt = $nvHash;
    }
    unless ($nvHash and $nvHash->{Value}) {
        return () if wantarray;  # return empty list
        return undef;
    }
    my $vals = $nvHash->{Value};
    # do inverse raw conversion if necessary
    # - must also check after doing a Shift
    if ($$nvHash{TagInfo}{RawConvInv} or $$nvHash{Shift}) {
        my @copyVals = @$vals;  # modify a copy of the values
        $vals = \@copyVals;
        my $tagInfo = $$nvHash{TagInfo};
        my $conv = $$tagInfo{RawConvInv};
        my $table = $$tagInfo{Table};
        my ($val, $checkProc);
        $checkProc = $$table{CHECK_PROC} if $$nvHash{Shift} and $table;
        local $SIG{'__WARN__'} = \&SetWarning;
        undef $evalWarning;
        foreach $val (@$vals) {
            # must check value now if it was shifted
            if ($checkProc) {
                my $err = &$checkProc($self, $tagInfo, \$val);
                if ($err or not defined $val) {
                    $err or $err = 'Error generating raw value';
                    $self->WarnOnce("$err for $$tagInfo{Name}");
                    @$vals = ();
                    last;
                }
                next unless $conv;
            } else {
                last unless $conv;
            }
            # do inverse raw conversion
            if (ref($conv) eq 'CODE') {
                $val = &$conv($val, $self);
            } else {
                #### eval RawConvInv ($self, $val, $taginfo)
                $val = eval $conv;
                $@ and $evalWarning = $@;
            }
            if ($evalWarning) {
                # an empty warning ("\n") ignores tag with no error
                if ($evalWarning ne "\n") {
                    my $err = CleanWarning() . " in $$tagInfo{Name} (RawConvInv)";
                    $self->WarnOnce($err);
                }
                @$vals = ();
                last;
            }
        }
    }
    # return our value(s)
    return @$vals if wantarray;
    return $$vals[0];
}

#------------------------------------------------------------------------------
# Return the total number of new values set
# Inputs: 0) ExifTool object reference
# Returns: Scalar context) Number of new values that have been set
#          List context) Number of new values, number of "pseudo" values
# ("pseudo" values are those which don't require rewriting the file to change)
sub CountNewValues($)
{
    my $self = shift;
    my $newVal = $self->{NEW_VALUE};
    my $num = 0;
    my $tag;
    if ($newVal) {
        $num += scalar keys %$newVal;
        # don't count "fake" tags (only in Extra table)
        foreach $tag (qw{Geotag Geosync}) {
            --$num if defined $$newVal{$Image::ExifTool::Extra{$tag}};
        }
    }
    $num += scalar keys %{$self->{DEL_GROUP}};
    return $num unless wantarray;
    my $pseudo = 0;
    if ($newVal) {
        # (Note: all writable "pseudo" tags must be found in Extra table)
        foreach $tag (qw{FileName Directory FileModifyDate}) {
            ++$pseudo if defined $$newVal{$Image::ExifTool::Extra{$tag}};
        }
    }
    return ($num, $pseudo);
}

#------------------------------------------------------------------------------
# Save new values for subsequent restore
# Inputs: 0) ExifTool object reference
sub SaveNewValues($)
{
    my $self = shift;
    my $newValues = $self->{NEW_VALUE};
    my $key;
    foreach $key (keys %$newValues) {
        my $nvHash = $$newValues{$key};
        while ($nvHash) {
            $nvHash->{Save} = 1;  # set Save flag
            $nvHash = $nvHash->{Next};
        }
    }
    # initialize hash for saving overwritten new values
    $self->{SAVE_NEW_VALUE} = { };
    # make a copy of the delete group hash
    if ($self->{DEL_GROUP}) {
        my %delGrp = %{$self->{DEL_GROUP}};
        $self->{SAVE_DEL_GROUP} = \%delGrp;
    } else {
        delete $self->{SAVE_DEL_GROUP};
    }
}

#------------------------------------------------------------------------------
# Restore new values to last saved state
# Inputs: 0) ExifTool object reference
# Notes: Restores saved new values, but currently doesn't restore them in the
# original order, so there may be some minor side-effects when restoring tags
# with overlapping groups. ie) XMP:Identifier, XMP-dc:Identifier
sub RestoreNewValues($)
{
    my $self = shift;
    my $newValues = $self->{NEW_VALUE};
    my $savedValues = $self->{SAVE_NEW_VALUE};
    my $key;
    # 1) remove any new values which don't have the Save flag set
    if ($newValues) {
        my @keys = keys %$newValues;
        foreach $key (@keys) {
            my $lastHash;
            my $nvHash = $$newValues{$key};
            while ($nvHash) {
                if ($nvHash->{Save}) {
                    $lastHash = $nvHash;
                } else {
                    # remove this entry from the list
                    if ($lastHash) {
                        $lastHash->{Next} = $nvHash->{Next};
                    } elsif ($nvHash->{Next}) {
                        $$newValues{$key} = $nvHash->{Next};
                    } else {
                        delete $$newValues{$key};
                    }
                }
                $nvHash = $nvHash->{Next};
            }
        }
    }
    # 2) restore saved new values
    if ($savedValues) {
        $newValues or $newValues = $self->{NEW_VALUE} = { };
        foreach $key (keys %$savedValues) {
            if ($$newValues{$key}) {
                # add saved values to end of list
                my $nvHash = LastInList($$newValues{$key});
                $nvHash->{Next} = $$savedValues{$key};
            } else {
                $$newValues{$key} = $$savedValues{$key};
            }
        }
        $self->{SAVE_NEW_VALUE} = { };  # reset saved new values
    }
    # 3) restore delete groups
    if ($self->{SAVE_DEL_GROUP}) {
        my %delGrp = %{$self->{SAVE_DEL_GROUP}};
        $self->{DEL_GROUP} = \%delGrp;
    } else {
        delete $self->{DEL_GROUP};
    }
}

#------------------------------------------------------------------------------
# Set file modification time from FileModifyDate tag
# Inputs: 0) ExifTool object reference, 1) file name or file ref
#         2) modify time (-M) of original file (needed for time shift)
# Returns: 1=time changed OK, 0=nothing done, -1=error setting time
#          (and increments CHANGED flag if time was changed)
sub SetFileModifyDate($$;$)
{
    my ($self, $file, $originalTime) = @_;
    my $nvHash;
    my $val = $self->GetNewValues('FileModifyDate', \$nvHash);
    return 0 unless defined $val;
    my $isOverwriting = $self->IsOverwriting($nvHash);
    return 0 unless $isOverwriting;
    if ($isOverwriting < 0) {  # are we shifting time?
        # use original time of this file if not specified
        $originalTime = -M $file unless defined $originalTime;
        return 0 unless defined $originalTime;
        return 0 unless $self->IsOverwriting($nvHash, $^T - $originalTime*(24*3600));
        $val = $nvHash->{Value}[0]; # get shifted value
    }
    unless (utime($val, $val, $file)) {
        $self->Warn('Error setting FileModifyDate');
        return -1;
    }
    ++$self->{CHANGED};
    $self->VerboseValue('+ FileModifyDate', $val);
    return 1;
}

#------------------------------------------------------------------------------
# Change file name and/or directory from FileName and Directory tags
# Inputs: 0) ExifTool object reference, 1) current file name (including path)
#         2) New name (or undef to build from FileName and Directory tags)
# Returns: 1=name changed OK, 0=nothing changed, -1=error changing name
#          (and increments CHANGED flag if filename changed)
# Notes: Will not overwrite existing file.  Creates directories as necessary.
sub SetFileName($$;$)
{
    my ($self, $file, $newName) = @_;
    my ($nvHash, $doName, $doDir);
    # determine the new file name
    unless (defined $newName) {
        my $filename = $self->GetNewValues('FileName', \$nvHash);
        $doName = 1 if defined $filename and $self->IsOverwriting($nvHash, $file);
        my $dir = $self->GetNewValues('Directory', \$nvHash);
        $doDir = 1 if defined $dir and $self->IsOverwriting($nvHash, $file);
        return 0 unless $doName or $doDir;  # nothing to do
        if ($doName) {
            $newName = GetNewFileName($file, $filename);
            $newName = GetNewFileName($newName, $dir) if $doDir;
        } else {
            $newName = GetNewFileName($file, $dir);
        }
    }
    if (-e $newName) {
        # don't replace existing file
        $self->Warn("File '$newName' already exists");
        return -1;
    }
    # create directory for new file if necessary
    my $result;
    if (($result = CreateDirectory($newName)) != 0) {
        if ($result < 0) {
            $self->Warn("Error creating directory for '$newName'");
            return -1;
        }
        $self->VPrint(0, "Created directory for '$newName'");
    }
    # attempt to rename the file
    unless (rename $file, $newName) {
        local (*EXIFTOOL_SFN_IN, *EXIFTOOL_SFN_OUT);
        # renaming didn't work, so copy the file instead
        unless (open EXIFTOOL_SFN_IN, $file) {
            $self->Warn("Error opening '$file'");
            return -1;
        }
        unless (open EXIFTOOL_SFN_OUT, ">$newName") {
            close EXIFTOOL_SFN_IN;
            $self->Warn("Error creating '$newName'");
            return -1;
        }
        binmode EXIFTOOL_SFN_IN;
        binmode EXIFTOOL_SFN_OUT;
        my ($buff, $err);
        while (read EXIFTOOL_SFN_IN, $buff, 65536) {
            print EXIFTOOL_SFN_OUT $buff or $err = 1;
        }
        close EXIFTOOL_SFN_OUT or $err = 1;
        close EXIFTOOL_SFN_IN;
        if ($err) {
            unlink $newName;    # erase bad output file
            $self->Warn("Error writing '$newName'");
            return -1;
        }
        # preserve modification time
        my $modTime = $^T - (-M $file) * (24 * 3600);
        my $accTime = $^T - (-A $file) * (24 * 3600);
        utime($accTime, $modTime, $newName);
        # remove the original file
        unlink $file or $self->Warn('Error removing old file');
    }
    ++$self->{CHANGED};
    $self->VerboseValue('+ FileName', $newName);
    return 1;
}

#------------------------------------------------------------------------------
# Write information back to file
# Inputs: 0) ExifTool object reference,
#         1) input filename, file ref, or scalar ref (or '' or undef to create from scratch)
#         2) output filename, file ref, or scalar ref (or undef to overwrite)
#         3) optional output file type (required only if input file is not specified
#            and output file is a reference)
# Returns: 1=file written OK, 2=file written but no changes made, 0=file write error
sub WriteInfo($$;$$)
{
    local ($_, *EXIFTOOL_FILE2, *EXIFTOOL_OUTFILE);
    my ($self, $infile, $outfile, $outType) = @_;
    my (@fileTypeList, $fileType, $tiffType, $hdr, $seekErr, $type, $tmpfile);
    my ($inRef, $outRef, $closeIn, $closeOut, $outPos, $outBuff, $eraseIn);
    my $oldRaf = $self->{RAF};
    my $rtnVal = 0;

    # initialize member variables
    $self->Init();

    # first, save original file modify date if necessary
    # (do this now in case we are modifying file in place and shifting date)
    my ($nvHash, $originalTime);
    my $fileModifyDate =  $self->GetNewValues('FileModifyDate', \$nvHash);
    if (defined $fileModifyDate and $self->IsOverwriting($nvHash) < 0 and
        defined $infile and ref $infile ne 'SCALAR')
    {
        $originalTime = -M $infile;
    }
#
# do quick in-place change of file dir/name or date if that is all we are doing
#
    my ($numNew, $numPseudo) = $self->CountNewValues();
    if (not defined $outfile and defined $infile) {
        my $newFileName =  $self->GetNewValues('FileName', \$nvHash);
        if ($numNew == $numPseudo) {
            $rtnVal = 2;
            if (defined $fileModifyDate and (not ref $infile or UNIVERSAL::isa($infile,'GLOB'))) {
                $self->SetFileModifyDate($infile) > 0 and $rtnVal = 1;
            }
            if (defined $newFileName and not ref $infile) {
                $self->SetFileName($infile) > 0 and $rtnVal = 1;
            }
            return $rtnVal;
        } elsif (defined $newFileName and length $newFileName) {
            # can't simply rename file, so just set the output name if new FileName
            # --> in this case, must erase original copy
            if (ref $infile) {
                $outfile = $newFileName;
                # can't delete original
            } elsif ($self->IsOverwriting($nvHash, $infile)) {
                $outfile = GetNewFileName($infile, $newFileName);
                $eraseIn = 1; # delete original
            }
        }
    }
#
# set up input file
#
    if (ref $infile) {
        $inRef = $infile;
        if (UNIVERSAL::isa($inRef,'GLOB')) {
            seek($inRef, 0, 0); # make sure we are at the start of the file
        } elsif ($] >= 5.006 and (eval 'require Encode; Encode::is_utf8($$inRef)' or $@)) {
            # convert image data from UTF-8 to character stream if necessary
            my $buff = $@ ? pack('C*',unpack('U0C*',$$inRef)) : Encode::encode('utf8',$$inRef);
            if (defined $outfile) {
                $inRef = \$buff;
            } else {
                $$inRef = $buff;
            }
        }
    } elsif (defined $infile and $infile ne '') {
        # write to a temporary file if no output file given
        $outfile = $tmpfile = "${infile}_exiftool_tmp" unless defined $outfile;
        if (open(EXIFTOOL_FILE2, $infile)) {
            $fileType = GetFileType($infile);
            @fileTypeList = GetFileType($infile);
            $tiffType = $$self{FILE_EXT} = GetFileExtension($infile);
            $self->VPrint(0, "Rewriting $infile...\n");
            $inRef = \*EXIFTOOL_FILE2;
            $closeIn = 1;   # we must close the file since we opened it
        } else {
            $self->Error('Error opening file');
            return 0;
        }
    } elsif (not defined $outfile) {
        $self->Error("WriteInfo(): Must specify infile or outfile\n");
        return 0;
    } else {
        # create file from scratch
        $outType = GetFileExtension($outfile) unless $outType or ref $outfile;
        if (CanCreate($outType)) {
            $fileType = $tiffType = $outType;   # use output file type if no input file
            $infile = "$fileType file";         # make bogus file name
            $self->VPrint(0, "Creating $infile...\n");
            $inRef = \ '';      # set $inRef to reference to empty data
        } elsif ($outType) {
            $self->Error("Can't create $outType files");
            return 0;
        } else {
            $self->Error("Can't create file (unknown type)");
            return 0;
        }
    }
    unless (@fileTypeList) {
        if ($fileType) {
            @fileTypeList = ( $fileType );
        } else {
            @fileTypeList = @fileTypes;
            $tiffType = 'TIFF';
        }
    }
#
# set up output file
#
    if (ref $outfile) {
        $outRef = $outfile;
        if (UNIVERSAL::isa($outRef,'GLOB')) {
            binmode($outRef);
            $outPos = tell($outRef);
        } else {
            # initialize our output buffer if necessary
            defined $$outRef or $$outRef = '';
            $outPos = length($$outRef);
        }
    } elsif (not defined $outfile) {
        # editing in place, so write to memory first
        # (only when infile is a file ref or scalar ref)
        $outBuff = '';
        $outRef = \$outBuff;
        $outPos = 0;
    } elsif (-e $outfile) {
        $self->Error("File already exists: $outfile");
    } elsif (open(EXIFTOOL_OUTFILE, ">$outfile")) {
        $outRef = \*EXIFTOOL_OUTFILE;
        $closeOut = 1;  # we must close $outRef
        binmode($outRef);
        $outPos = 0;
    } else {
        my $tmp = $tmpfile ? ' temporary' : '';
        $self->Error("Error creating$tmp file: $outfile");
    }
#
# write the file
#
    until ($self->{VALUE}{Error}) {
        # create random access file object (disable seek test in case of straight copy)
        my $raf = new File::RandomAccess($inRef, 1);
        $raf->BinMode();
        if ($numNew == $numPseudo) {
            $rtnVal = 1;
            # just do a straight copy of the file (no "real" tags are being changed)
            my $buff;
            while ($raf->Read($buff, 65536)) {
                Write($outRef, $buff) or $rtnVal = -1, last;
            }
            last;
        } elsif (not ref $infile and ($infile eq '-' or $infile =~ /\|$/)) {
            # patch for Windows command shell pipe
            $raf->{TESTED} = -1;    # force buffering
        } else {
            $raf->SeekTest();
        }
       # $raf->Debug() and warn "  RAF debugging enabled!\n";
        my $inPos = $raf->Tell();
        $self->{RAF} = $raf;
        my %dirInfo = (
            RAF => $raf,
            OutFile => $outRef,
        );
        $raf->Read($hdr, 1024) or $hdr = '';
        $raf->Seek($inPos, 0) or $seekErr = 1;
        my $wrongType;
        until ($seekErr) {
            $type = shift @fileTypeList;
            # do quick test to see if this is the right file type
            if ($magicNumber{$type} and length($hdr) and $hdr !~ /^$magicNumber{$type}/s) {
                next if @fileTypeList;
                $wrongType = 1;
                last;
            }
            # save file type in member variable
            $dirInfo{Parent} = $self->{FILE_TYPE} = $self->{PATH}[0] = $type;
            # determine which directories we must write for this file type
            $self->InitWriteDirs($type);
            if ($type eq 'JPEG') {
                $rtnVal = $self->WriteJPEG(\%dirInfo);
            } elsif ($type eq 'TIFF') {
                # disallow writing of some TIFF-based RAW images:
                if (grep /^$tiffType$/, @{$noWriteFile{TIFF}}) {
                    $fileType = $tiffType;
                    undef $rtnVal;
                } else {
                    $dirInfo{Parent} = $tiffType;
                    $rtnVal = $self->ProcessTIFF(\%dirInfo);
                }
            } elsif ($type eq 'GIF') {
                require Image::ExifTool::GIF;
                $rtnVal = Image::ExifTool::GIF::ProcessGIF($self,\%dirInfo);
            } elsif ($type eq 'CRW') {
                require Image::ExifTool::CanonRaw;
                $rtnVal = Image::ExifTool::CanonRaw::WriteCRW($self, \%dirInfo);
            } elsif ($type eq 'MRW') {
                require Image::ExifTool::MinoltaRaw;
                $rtnVal = Image::ExifTool::MinoltaRaw::ProcessMRW($self, \%dirInfo);
            } elsif ($type eq 'RAF') {
                require Image::ExifTool::FujiFilm;
                $rtnVal = Image::ExifTool::FujiFilm::WriteRAF($self, \%dirInfo);
            } elsif ($type eq 'ORF' or $type eq 'RAW') {
                $rtnVal = $self->ProcessTIFF(\%dirInfo);
            } elsif ($type eq 'X3F') {
                require Image::ExifTool::SigmaRaw;
                $rtnVal = Image::ExifTool::SigmaRaw::ProcessX3F($self, \%dirInfo);
            } elsif ($type eq 'PNG') {
                require Image::ExifTool::PNG;
                $rtnVal = Image::ExifTool::PNG::ProcessPNG($self, \%dirInfo);
            } elsif ($type eq 'MIE') {
                require Image::ExifTool::MIE;
                $rtnVal = Image::ExifTool::MIE::ProcessMIE($self, \%dirInfo);
            } elsif ($type eq 'XMP') {
                require Image::ExifTool::XMP;
                $rtnVal = Image::ExifTool::XMP::WriteXMP($self, \%dirInfo);
            } elsif ($type eq 'PPM') {
                require Image::ExifTool::PPM;
                $rtnVal = Image::ExifTool::PPM::ProcessPPM($self, \%dirInfo);
            } elsif ($type eq 'PSD') {
                require Image::ExifTool::Photoshop;
                $rtnVal = Image::ExifTool::Photoshop::ProcessPSD($self, \%dirInfo);
            } elsif ($type eq 'EPS' or $type eq 'PS') {
                require Image::ExifTool::PostScript;
                $rtnVal = Image::ExifTool::PostScript::WritePS($self, \%dirInfo);
            } elsif ($type eq 'PDF') {
                require Image::ExifTool::PDF;
                $rtnVal = Image::ExifTool::PDF::WritePDF($self, \%dirInfo);
            } elsif ($type eq 'ICC') {
                require Image::ExifTool::ICC_Profile;
                $rtnVal = Image::ExifTool::ICC_Profile::WriteICC($self, \%dirInfo);
            } elsif ($type eq 'VRD') {
                require Image::ExifTool::CanonVRD;
                $rtnVal = Image::ExifTool::CanonVRD::ProcessVRD($self, \%dirInfo);
            } elsif ($type eq 'JP2') {
                require Image::ExifTool::Jpeg2000;
                $rtnVal = Image::ExifTool::Jpeg2000::ProcessJP2($self, \%dirInfo);
            } elsif ($type eq 'IND') {
                require Image::ExifTool::InDesign;
                $rtnVal = Image::ExifTool::InDesign::ProcessIND($self, \%dirInfo);
            } elsif ($type eq 'EXIF') {
                # go through WriteDirectory so block writes, etc are handled
                my $tagTablePtr = GetTagTable('Image::ExifTool::Exif::Main');
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr, \&WriteTIFF);
                if (defined $buff) {
                    $rtnVal = Write($outRef, $buff) ? 1 : -1;
                } else {
                    $rtnVal = 0;
                }
            } else {
                undef $rtnVal;  # flag that we don't write this type of file
            }
            # all done unless we got the wrong type
            last if $rtnVal;
            last unless @fileTypeList;
            # seek back to original position in files for next try
            $raf->Seek($inPos, 0) or $seekErr = 1, last;
            if (UNIVERSAL::isa($outRef,'GLOB')) {
                seek($outRef, 0, $outPos);
            } else {
                $$outRef = substr($$outRef, 0, $outPos);
            }
        }
        # print file format errors
        unless ($rtnVal) {
            my $err;
            if ($seekErr) {
                $err = 'Error seeking in file';
            } elsif ($fileType and defined $rtnVal) {
                if ($self->{VALUE}{Error}) {
                    # existing error message will do
                } elsif ($fileType eq 'RAW') {
                    $err = 'Writing this type of RAW file is not supported';
                } else {
                    if ($wrongType) {
                        $err = "Not a valid $fileType";
                        # do a quick check to see what this file looks like
                        foreach $type (@fileTypes) {
                            next unless $magicNumber{$type};
                            next unless $hdr =~ /^$magicNumber{$type}/s;
                            $err .= " (looks more like a $type)";
                            last;
                        }
                    } else {
                        $err = 'Format error in file';
                    }
                }
            } elsif ($fileType) {
                # get specific type of file from extension
                $fileType = GetFileExtension($infile) if $infile and GetFileType($infile);
                $err = "Writing of $fileType files is not yet supported";
            } else {
                $err = 'Writing of this type of file is not supported';
            }
            $self->Error($err) if $err;
            $rtnVal = 0;    # (in case it was undef)
        }
       # $raf->Close();  # only used to force debug output
        last;   # (didn't really want to loop)
    }
    # don't return success code if any error occurred
    if ($rtnVal > 0) {
        unless (Tell($outRef) or $self->{VALUE}{Error}) {
            # don't write a file with zero length
            if (defined $hdr and length $hdr) {
                $self->Error("Can't delete all meta information from $type file");
            } else {
                $self->Error('Nothing to write');
            }
        }
        $rtnVal = 0 if $self->{VALUE}{Error};
    }

    # rewrite original file in place if required
    if (defined $outBuff) {
        if ($rtnVal <= 0 or not $self->{CHANGED}) {
            # nothing changed, so no need to write $outBuff
        } elsif (UNIVERSAL::isa($inRef,'GLOB')) {
            my $len = length($outBuff);
            my $size;
            $rtnVal = -1 unless
                seek($inRef, 0, 2) and          # seek to the end of file
                ($size = tell $inRef) >= 0 and  # get the file size
                seek($inRef, 0, 0) and          # seek back to the start
                print $inRef $outBuff and       # write the new data
                ($len >= $size or               # if necessary:
                eval 'truncate($inRef, $len)'); #  shorten output file
        } else {
            $$inRef = $outBuff;                 # replace original data
        }
        $outBuff = '';  # free memory but leave $outBuff defined
    }
    # close input file if we opened it
    if ($closeIn) {
        # errors on input file are significant if we edited the file in place
        $rtnVal and $rtnVal = -1 unless close($inRef) or not defined $outBuff;
        if ($rtnVal > 0) {
            # copy Mac OS resource fork if it exists
            if ($^O eq 'darwin' and -s "$infile/rsrc") {
                if ($$self{DEL_GROUP} and $$self{DEL_GROUP}{RSRC}) {
                    $self->VPrint(0,"Deleting Mac OS resource fork\n");
                    ++$$self{CHANGED};
                } else {
                    $self->VPrint(0,"Copying Mac OS resource fork\n");
                    my ($buf, $err);
                    local (*SRC, *DST);
                    if (open SRC, "$infile/rsrc") {
                        if (open DST, ">$outfile/rsrc") {
                            binmode SRC; # (not necessary for Darwin, but let's be thorough)
                            binmode DST;
                            while (read SRC, $buf, 65536) {
                                print DST $buf or $err = 'copying', last;
                            }
                            close DST or $err or $err = 'closing';
                        } else {
                            # (this is normal if the destination filesystem isn't Mac OS)
                            $self->Warn('Error creating Mac OS resource fork');
                        }
                        close SRC;
                    } else {
                        $err = 'opening';
                    }
                    $rtnVal = 0 if $err and $self->Error("Error $err Mac OS resource fork", 1);
                }
            }
            # erase input file if renaming while editing information in place
            unlink $infile or $self->Warn('Error erasing original file') if $eraseIn;
        }
    }
    # close output file if we created it
    if ($closeOut) {
        # close file and set $rtnVal to -1 if there was an error
        $rtnVal and $rtnVal = -1 unless close($outRef);
        # erase the output file if we weren't successful
        if ($rtnVal <= 0) {
            unlink $outfile;
        # else rename temporary file if necessary
        } elsif ($tmpfile) {
            CopyFileAttrs($infile, $tmpfile);   # copy attributes to new file
            unless (rename($tmpfile, $infile)) {
                # some filesystems won't overwrite with 'rename', so try erasing original
                if (not unlink($infile)) {
                    unlink $tmpfile;
                    $self->Error('Error renaming temporary file');
                    $rtnVal = 0;
                } elsif (not rename($tmpfile, $infile)) {
                    $self->Error('Error renaming temporary file after deleting original');
                    $rtnVal = 0;
                }
            }
        }
    }
    # set FileModifyDate if requested (and if possible!)
    if (defined $fileModifyDate and $rtnVal > 0 and
        ($closeOut or ($closeIn and defined $outBuff)) and
        $self->SetFileModifyDate($closeOut ? $outfile : $infile, $originalTime) > 0)
    {
        ++$self->{CHANGED}; # we changed something
    }
    # check for write error and set appropriate error message and return value
    if ($rtnVal < 0) {
        $self->Error('Error writing output file') unless $$self{VALUE}{Error};
        $rtnVal = 0;    # return 0 on failure
    } elsif ($rtnVal > 0) {
        ++$rtnVal unless $self->{CHANGED};
    }
    # set things back to the way they were
    $self->{RAF} = $oldRaf;

    return $rtnVal;
}

#------------------------------------------------------------------------------
# Get list of all available tags for specified group
# Inputs: 0) optional group name (or string of names separated by colons)
# Returns: tag list (sorted alphabetically)
# Notes: Can't get tags for specific IFD
sub GetAllTags(;$)
{
    local $_;
    my $group = shift;
    my (%allTags, @groups);
    @groups = split ':', $group if $group;

    my $exifTool = new Image::ExifTool;
    LoadAllTables();    # first load all our tables
    my @tableNames = keys %allTables;

    # loop through all tables and save tag names to %allTags hash
    while (@tableNames) {
        my $table = GetTagTable(pop @tableNames);
        my $tagID;
        foreach $tagID (TagTableKeys($table)) {
            my @infoArray = GetTagInfoList($table,$tagID);
            my $tagInfo;
GATInfo:    foreach $tagInfo (@infoArray) {
                my $tag = $$tagInfo{Name};
                $tag or warn("no name for tag!\n"), next;
                # don't list subdirectories unless they are writable
                next if $$tagInfo{SubDirectory} and not $$tagInfo{Writable};
                next if $$tagInfo{Hidden};  # ignore hidden tags
                if (@groups) {
                    my @tg = $exifTool->GetGroup($tagInfo);
                    foreach $group (@groups) {
                        next GATInfo unless grep /^$group$/i, @tg;
                    }
                }
                $allTags{$tag} = 1;
            }
        }
    }
    return sort keys %allTags;
}

#------------------------------------------------------------------------------
# Get list of all writable tags
# Inputs: 0) optional group name (or names separated by colons)
# Returns: tag list (sorted alphbetically)
sub GetWritableTags(;$)
{
    local $_;
    my $group = shift;
    my (%writableTags, @groups);
    @groups = split ':', $group if $group;

    my $exifTool = new Image::ExifTool;
    LoadAllTables();
    my @tableNames = keys %allTables;

    while (@tableNames) {
        my $tableName = pop @tableNames;
        my $table = GetTagTable($tableName);
        # attempt to load Write tables if autoloaded
        my @path = split(/::/,$tableName);
        if (@path > 3) {
            my $i = $#path - 1;
            $path[$i] = "Write$path[$i]";   # add 'Write' before class name
            my $module = join('::',@path[0..($#path-1)]);
            eval "require $module"; # (fails silently if nothing loaded)
        }
        my $tagID;
        foreach $tagID (TagTableKeys($table)) {
            my @infoArray = GetTagInfoList($table,$tagID);
            my $tagInfo;
GWTInfo:    foreach $tagInfo (@infoArray) {
                my $tag = $$tagInfo{Name};
                $tag or warn("no name for tag!\n"), next;
                my $writable = $$tagInfo{Writable};
                next unless $writable or ($table->{WRITABLE} and
                    not defined $writable and not $$tagInfo{SubDirectory});
                next if $$tagInfo{Hidden};  # ignore hidden tags
                if (@groups) {
                    my @tg = $exifTool->GetGroup($tagInfo);
                    foreach $group (@groups) {
                        next GWTInfo unless grep /^$group$/i, @tg;
                    }
                }
                $writableTags{$tag} = 1;
            }
        }
    }
    return sort keys %writableTags;
}

#------------------------------------------------------------------------------
# Get list of all group names
# Inputs: 1) Group family number
# Returns: List of group names (sorted alphabetically)
sub GetAllGroups($)
{
    local $_;
    my $family = shift || 0;

    $family == 3 and return('Doc#', 'Main');
    $family == 4 and return('Copy#');

    LoadAllTables();    # first load all our tables

    my @tableNames = keys %allTables;

    # loop through all tag tables and get all group names
    my %allGroups;
    while (@tableNames) {
        my $table = GetTagTable(pop @tableNames);
        my ($grps, $grp, $tag, $tagInfo);
        $allGroups{$grp} = 1 if ($grps = $$table{GROUPS}) and ($grp = $$grps{$family});
        foreach $tag (TagTableKeys($table)) {
            my @infoArray = GetTagInfoList($table, $tag);
            foreach $tagInfo (@infoArray) {
                next unless ($grps = $$tagInfo{Groups}) and ($grp = $$grps{$family});
                $allGroups{$grp} = 1;
            }
        }
    }
    return sort keys %allGroups;
}

#------------------------------------------------------------------------------
# get priority group list for new values
# Inputs: 0) ExifTool object reference
# Returns: List of group names
sub GetNewGroups($)
{
    my $self = shift;
    return @{$self->{WRITE_GROUPS}};
}

#------------------------------------------------------------------------------
# Get list of all deletable group names
# Returns: List of group names (sorted alphabetically)
sub GetDeleteGroups()
{
    return sort @delGroups;
}

#==============================================================================
# Functions below this are not part of the public API

#------------------------------------------------------------------------------
# Un-escape string according to options settings and clear UTF-8 flag
# Inputs: 0) ExifTool ref, 1) string ref or string ref ref
# Notes: also de-references SCALAR values
sub Sanitize($$)
{
    my ($self, $valPt) = @_;
    # de-reference SCALAR references
    $$valPt = $$$valPt if ref $$valPt eq 'SCALAR';
    # make sure the Perl UTF-8 flag is OFF for the value if perl 5.6 or greater
    # (otherwise our byte manipulations get corrupted!!)
    if ($] >= 5.006 and (eval 'require Encode; Encode::is_utf8($$valPt)' or $@)) {
        # repack by hand if Encode isn't available
        $$valPt = $@ ? pack('C*',unpack('U0C*',$$valPt)) : Encode::encode('utf8',$$valPt);
    }
    # un-escape value if necessary
    if ($$self{OPTIONS}{Escape}) {
        # (XMP.pm and HTML.pm were require'd as necessary when option was set)
        if ($$self{OPTIONS}{Escape} eq 'XML') {
            $$valPt = Image::ExifTool::XMP::UnescapeXML($$valPt);
        } elsif ($$self{OPTIONS}{Escape} eq 'HTML') {
            $$valPt = Image::ExifTool::HTML::UnescapeHTML($$valPt);
        }
    }
}

#------------------------------------------------------------------------------
# Apply inverse conversions
# Inputs: 0) ExifTool ref, 1) value, 2) tagInfo (or Struct item) ref,
#         3) tag name, 4) group 1 name, 5) conversion type (or undef),
#         6) [optional] want group
# Returns: 0) converted value, 1) error string (or undef on success)
# Notes: Uses ExifTool "ConvType" member to specify conversion type
sub ConvInv($$$$$;$$)
{
    my ($self, $val, $tagInfo, $tag, $wgrp1, $convType, $wantGroup) = @_;
    my ($err, $type);

Conv: for (;;) {
        if (not defined $type) {
            # split value into list if necessary
            if ($$tagInfo{List}) {
                my $listSplit = $$tagInfo{AutoSplit} || $self->{OPTIONS}{ListSplit};
                if (defined $listSplit) {
                    $listSplit = ',?\s+' if $listSplit eq '1' and $$tagInfo{AutoSplit};
                    my @splitVal = split /$listSplit/, $val;
                    $val = \@splitVal if @splitVal > 1;
                }
            }
            $type = $convType || $$self{ConvType} || 'PrintConv';
        } elsif ($type ne 'ValueConv') {
            $type = 'ValueConv';
        } else {    
            # finally, do our value check
            my ($err2, $v);
            if ($tagInfo->{WriteCheck}) {
                #### eval WriteCheck ($self, $tagInfo, $val)
                $err2 = eval $tagInfo->{WriteCheck};
                $@ and warn($@), $err2 = 'Error evaluating WriteCheck';
            }
            unless ($err2) {
                my $table = $tagInfo->{Table};
                if ($table and $table->{CHECK_PROC} and not $$tagInfo{RawConvInv}) {
                    my $checkProc = $table->{CHECK_PROC};
                    if (ref $val eq 'ARRAY') {
                        # loop through array values
                        foreach $v (@$val) {
                            $err2 = &$checkProc($self, $tagInfo, \$v);
                            last if $err2;
                        }
                    } else {
                        $err2 = &$checkProc($self, $tagInfo, \$val);
                    }
                }
            }
            if (defined $err2) {
                # skip writing this tag if error string is empty
                $err2 or goto WriteAlso;
                $err = "$err2 for $wgrp1:$tag";
                $self->VPrint(2, "$err\n");
                undef $val; # value was invalid
            }
            last;
        }
        my $conv = $tagInfo->{$type};
        my $convInv = $tagInfo->{"${type}Inv"};
        # nothing to do at this level if no conversion defined
        next unless defined $conv or defined $convInv;

        my (@valList, $index, $convList, $convInvList);
        if (ref $val eq 'ARRAY') {
            # handle ValueConv of ListSplit and AutoSplit values
            @valList = @$val;
            $val = $valList[$index = 0];
        } elsif (ref $conv eq 'ARRAY' or ref $convInv eq 'ARRAY') {
            # handle conversion lists
            @valList = split /$listSep{$type}/, $val;
            $val = $valList[$index = 0];
            if (ref $conv eq 'ARRAY') {
                $convList = $conv;
                $conv = $$conv[0];
            }
            if (ref $convInv eq 'ARRAY') {
                $convInvList = $convInv;
                $convInv = $$convInv[0];
            }
        }
        # loop through multiple values if necessary
        for (;;) {
            if ($convInv) {
                # capture eval warnings too
                local $SIG{'__WARN__'} = \&SetWarning;
                undef $evalWarning;
                if (ref($convInv) eq 'CODE') {
                    $val = &$convInv($val, $self);
                } else {
                    #### eval PrintConvInv/ValueConvInv ($val, $self, $wantGroup)
                    $val = eval $convInv;
                    $@ and $evalWarning = $@;
                }
                if ($evalWarning) {
                    # an empty warning ("\n") ignores tag with no error
                    if ($evalWarning eq "\n") {
                        $err = '' unless defined $err;
                    } else {
                        $err = CleanWarning() . " in $wgrp1:$tag (${type}Inv)";
                        $self->VPrint(2, "$err\n");
                    }
                    undef $val;
                    last Conv;
                } elsif (not defined $val) {
                    $err = "Error converting value for $wgrp1:$tag (${type}Inv)";
                    $self->VPrint(2, "$err\n");
                    last Conv;
                }
            } elsif ($conv) {
                if (ref $conv eq 'HASH') {
                    my ($multi, $lc);
                    # insert alternate language print conversions if required
                    if ($$self{CUR_LANG} and $type eq 'PrintConv' and
                        ref($lc = $self->{CUR_LANG}{$tag}) eq 'HASH' and
                        ($lc = $$lc{PrintConv}))
                    {
                        my %newConv;
                        foreach (keys %$conv) {
                            my $val = $$conv{$_};
                            defined $$lc{$val} or $newConv{$_} = $val, next;
                            $newConv{$_} = $self->Decode($$lc{$val}, 'UTF8');
                        }
                        if ($$conv{BITMASK}) {
                            foreach (keys %{$$conv{BITMASK}}) {
                                my $val = $$conv{BITMASK}{$_};
                                defined $$lc{$val} or $newConv{BITMASK}{$_} = $val, next;
                                $newConv{BITMASK}{$_} = $self->Decode($$lc{$val}, 'UTF8');
                            }
                        }
                        $conv = \%newConv;
                    }
                    if ($$conv{BITMASK}) {
                        my $lookupBits = $$conv{BITMASK};
                        my ($val2, $err2) = EncodeBits($val, $lookupBits);
                        if ($err2) {
                            # ok, try matching a straight value
                            ($val, $multi) = ReverseLookup($val, $conv);
                            unless (defined $val) {
                                $err = "Can't encode $wgrp1:$tag ($err2)";
                                $self->VPrint(2, "$err\n");
                                last Conv;
                            }
                        } elsif (defined $val2) {
                            $val = $val2;
                        } else {
                            delete $$conv{BITMASK};
                            ($val, $multi) = ReverseLookup($val, $conv);
                            $$conv{BITMASK} = $lookupBits;
                        }
                    } else {
                        ($val, $multi) = ReverseLookup($val, $conv);
                    }
                    unless (defined $val) {
                        $err = "Can't convert $wgrp1:$tag (" .
                               ($multi ? 'matches more than one' : 'not in') . " $type)";
                        $self->VPrint(2, "$err\n");
                        last Conv;
                    }
                } elsif (not $$tagInfo{WriteAlso}) {
                    $err = "Can't convert value for $wgrp1:$tag (no ${type}Inv)";
                    $self->VPrint(2, "$err\n");
                    undef $val;
                    last Conv;
                }
            }
            last unless @valList;
            $valList[$index] = $val;
            if (++$index >= @valList) {
                # leave AutoSplit lists in ARRAY form, or join conversion lists
                $val = $$tagInfo{List} ? \@valList : join ' ', @valList;
                last;
            }
            $conv = $$convList[$index] if $convList;
            $convInv = $$convInvList[$index] if $convInvList;
            $val = $valList[$index];
        }
    } # end ValueConv/PrintConv loop

    return($val, $err);
}

#------------------------------------------------------------------------------
# convert tag names to values in a string (ie. "${EXIF:ISO}x $$" --> "100x $")
# Inputs: 0) ExifTool object ref, 1) reference to list of found tags
#         2) string with embedded tag names, 3) Options:
#               undef    - set missing tags to ''
#              'Error'   - issue minor error on missing tag (and return undef)
#              'Warn'    - issue minor warning on missing tag (and return undef)
#               Hash ref - hash for return of tag/value pairs
# Returns: string with embedded tag values (or '$info{TAGNAME}' entries with Hash ref option)
# Notes:
# - tag names are not case sensitive and may end with '#' for ValueConv value
# - uses MissingTagValue option if set
sub InsertTagValues($$$;$)
{
    my ($self, $foundTags, $line, $opt) = @_;
    my $rtnStr = '';
    while ($line =~ /(.*?)\$(\{?)([-\w]+|\$|\/)(.*)/s) {
        my (@tags, $pre, $var, $bra, $val, $tg, @vals, $type);
        ($pre, $bra, $var, $line) = ($1, $2, $3, $4);
        # "$$" represents a "$" symbol, and "$/" is a newline
        if ($var eq '$' or $var eq '/') {
            $var = "\n" if $var eq '/';
            $rtnStr .= "$pre$var";
            $line =~ s/^\}// if $bra;
            next;
        }
        # allow multiple group names
        while ($line =~ /^:([-\w]+)(.*)/s) {
            my $group = $var;
            ($var, $line) = ($1, $2);
            $var = "$group:$var";
        }
        # allow trailing '#' to indicate ValueConv value
        $type = 'ValueConv' if $line =~ s/^#//;
        # remove trailing bracket if there was a leading one
        $line =~ s/^\}// if $bra;
        push @tags, $var;
        ExpandShortcuts(\@tags);
        @tags or $rtnStr .= $pre, next;

        for (;;) {
            my $tag = shift @tags;
            if ($tag =~ /(.*):(.+)/) {
                my $group;
                ($group, $tag) = ($1, $2);
                # find the specified tag
                my @matches = grep /^$tag(\s|$)/i, @$foundTags;
                @matches = $self->GroupMatches($group, \@matches);
                foreach $tg (@matches) {
                    if (defined $val and $tg =~ / \((\d+)\)$/) {
                        # take the most recently extracted tag
                        my $tagNum = $1;
                        next if $tag !~ / \((\d+)\)$/ or $1 > $tagNum;
                    }
                    $val = $self->GetValue($tg, $type);
                    $tag = $tg;
                    last unless $tag =~ / /;    # all done if we got our best match
                }
            } else {
                # get the tag value
                $val = $self->GetValue($tag, $type);
                unless (defined $val) {
                    # check for tag name with different case
                    ($tg) = grep /^$tag$/i, @$foundTags;
                    if (defined $tg) {
                        $val = $self->GetValue($tg, $type);
                        $tag = $tg;
                    }
                }
            }
            if (ref $val eq 'ARRAY') {
                $val = join($self->{OPTIONS}{ListSep}, @$val);
            } elsif (ref $val eq 'SCALAR') {
                if ($self->{OPTIONS}{Binary} or $$val =~ /^Binary data/) {
                    $val = $$val;
                } else {
                    $val = 'Binary data ' . length($$val) . ' bytes';
                }
            } elsif (not defined $val) {
                last unless @tags;
                next;
            }
            last unless @tags;
            push @vals, $val;
            undef $val;
        }
        if (@vals) {
            push @vals, $val if defined $val;
            $val = join '', @vals;
        }
        unless (defined $val or ref $opt) {
            $val = $self->{OPTIONS}{MissingTagValue};
            unless (defined $val) {
                no strict 'refs';
                return undef if $opt and &$opt($self, "Tag '$var' not defined", 1);
                $val = '';
            }
        }
        if (ref $opt eq 'HASH') {
            $var .= '#' if $type;
            $rtnStr .= "$pre\$info{'$var'}";
            $$opt{$var} = $val;
        } else {
            $rtnStr .= "$pre$val";
        }
    }
    return $rtnStr . $line;
}

#------------------------------------------------------------------------------
# Is specified tag writable
# Inputs: 0) tag name, case insensitive (optional group name currently ignored)
# Returns: 0=exists but not writable, 1=writable, undef=doesn't exist
sub IsWritable($)
{
    my $tag = shift;
    $tag =~ s/^(.*)://; # ignore group name
    my @tagInfo = FindTagInfo($tag);
    unless (@tagInfo) {
        return 0 if TagExists($tag);
        return undef;
    }
    my $tagInfo;
    foreach $tagInfo (@tagInfo) {
        return 1 if $$tagInfo{Writable} or $tagInfo->{Table}{WRITABLE};
        # must call WRITE_PROC to autoload writer because this may set the writable tag
        my $writeProc = $tagInfo->{Table}{WRITE_PROC};
        next unless $writeProc;
        &$writeProc();  # dummy call to autoload writer
        return 1 if $$tagInfo{Writable};
    }
    return 0;
}

#------------------------------------------------------------------------------
# Create directory for specified file
# Inputs: 0) complete file name including path
# Returns: 1 = directory created, 0 = nothing done, -1 = error
sub CreateDirectory($)
{
    local $_;
    my $file = shift;
    my $rtnVal = 0;
    my $dir;
    ($dir = $file) =~ s/[^\/]*$//;  # remove filename from path specification
    if ($dir and not -d $dir) {
        my @parts = split /\//, $dir;
        $dir = '';
        foreach (@parts) {
            $dir .= $_;
            if (length $dir and not -d $dir) {
                # create directory since it doesn't exist
                mkdir($dir, 0777) or return -1;
                $rtnVal = 1;
            }
            $dir .= '/';
        }
    }
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Copy file attributes from one file to another
# Inputs: 0) source file name, 1) destination file name
# Notes: eventually add support for extended attributes?
sub CopyFileAttrs($$)
{
    my ($src, $dst) = @_;
    my ($mode, $uid, $gid) = (stat($src))[2, 4, 5];
    eval { chmod($mode & 07777, $dst) } if defined $mode;
    eval { chown($uid, $gid, $dst) } if defined $uid and defined $gid;
}

#------------------------------------------------------------------------------
# Get new file name
# Inputs: 0) existing name, 1) new name
# Returns: new file path name
sub GetNewFileName($$)
{
    my ($oldName, $newName) = @_;
    my ($dir, $name) = ($oldName =~ m{(.*/)(.*)});
    ($dir, $name) = ('', $oldName) unless defined $dir;
    if ($newName =~ m{/$}) {
        $newName = "$newName$name"; # change dir only
    } elsif ($newName !~ m{/}) {
        $newName = "$dir$newName";  # change name only if newname doesn't specify dir
    }                               # else change dir and name
    return $newName;
}

#------------------------------------------------------------------------------
# Get next available tag key
# Inputs: 0) hash reference (keys are tag keys), 1) tag name
# Returns: next available tag key
sub NextTagKey($$)
{
    my ($info, $tag) = @_;
    return $tag unless exists $$info{$tag};
    my $i;
    for ($i=1; ; ++$i) {
        my $key = "$tag ($i)";
        return $key unless exists $$info{$key};
    }
}

#------------------------------------------------------------------------------
# Reverse hash lookup
# Inputs: 0) value, 1) hash reference
# Returns: Hash key or undef if not found (plus flag for multiple matches in list context)
sub ReverseLookup($$)
{
    my ($val, $conv) = @_;
    return undef unless defined $val;
    my $multi;
    if ($val =~ /^Unknown\s*\((.*)\)$/i) {
        $val = $1;    # was unknown
        if ($val =~ /^0x([\da-fA-F]+)$/) {
            $val = hex($val);   # convert hex value
        }
    } else {
        my $qval = quotemeta $val;
        my @patterns = (
            "^$qval\$",         # exact match
            "^(?i)$qval\$",     # case-insensitive
            "^(?i)$qval",       # beginning of string
            "(?i)$qval",        # substring
        );
        # hash entries to ignore in reverse lookup
        my ($pattern, $found, $matches);
PAT:    foreach $pattern (@patterns) {
            $matches = scalar grep /$pattern/, values(%$conv);
            next unless $matches;
            # multiple matches are bad unless they were exact
            if ($matches > 1 and $pattern !~ /\$$/) {
                # don't match entries that we should ignore
                foreach (keys %ignorePrintConv) {
                    --$matches if defined $$conv{$_} and $$conv{$_} =~ /$pattern/;
                }
                last if $matches > 1;
            }
            foreach (sort keys %$conv) {
                next if $$conv{$_} !~ /$pattern/ or $ignorePrintConv{$_};
                $val = $_;
                $found = 1;
                last PAT;
            }
        }
        unless ($found) {
            # call OTHER conversion routine if available
            $val = $$conv{OTHER} ? &{$$conv{OTHER}}($val,1,$conv) : undef;
            $multi = 1 if $matches > 1;
        }
    }
    return ($val, $multi) if wantarray;
    return $val;
}

#------------------------------------------------------------------------------
# Return true if we are deleting or overwriting the specified tag
# Inputs: 0) ExifTool object ref, 1) new value hash reference
#         2) optional tag value (before RawConv) if deleting specific values
# Returns: >0 - tag should be overwritten
#          =0 - the tag should be preserved
#          <0 - not sure, we need the value to know
sub IsOverwriting($$;$)
{
    my ($self, $nvHash, $val) = @_;
    return 0 unless $nvHash;
    # overwrite regardless if no DelValues specified
    return 1 unless $$nvHash{DelValue};
    # never overwrite if DelValue list exists but is empty
    my $shift = $$nvHash{Shift};
    return 0 unless @{$$nvHash{DelValue}} or defined $shift;
    # return "don't know" if we don't have a value to test
    return -1 unless defined $val;
    # apply raw conversion if necessary
    my $tagInfo = $$nvHash{TagInfo};
    my $conv = $$tagInfo{RawConv};
    if ($conv) {
        local $SIG{'__WARN__'} = \&SetWarning;
        undef $evalWarning;
        if (ref $conv eq 'CODE') {
            $val = &$conv($val, $self);
        } else {
            my $tag = $$tagInfo{Name};
            #### eval RawConv ($self, $val, $tag, $tagInfo)
            $val = eval $conv;
            $@ and $evalWarning = $@;
        }
        return -1 unless defined $val;
    }
    # apply time/number shift if necessary
    if (defined $shift) {
        my $shiftType = $$tagInfo{Shift};
        unless ($shiftType and $shiftType eq 'Time') {
            unless (IsFloat($val)) {
                $self->Warn("Can't shift $$tagInfo{Name} (not a number)");
                return 0;
            }
            $shiftType = 'Number';  # allow any number to be shifted
        }
        require 'Image/ExifTool/Shift.pl';
        my $err = $self->ApplyShift($shiftType, $shift, $val, $nvHash);
        if ($err) {
            $self->Warn("$err when shifting $$tagInfo{Name}");
            return 0;
        }
        # ensure that the shifted value is valid and reformat if necessary
        my $checkVal = $self->GetNewValues($nvHash);
        return 0 unless defined $checkVal;
        # don't bother overwriting if value is the same
        return 0 if $val eq $$nvHash{Value}[0];
        return 1;
    }
    # return 1 if value matches a DelValue
    my $delVal;
    foreach $delVal (@{$$nvHash{DelValue}}) {
        return 1 if $val eq $delVal;
    }
    return 0;
}

#------------------------------------------------------------------------------
# Return true if we are creating the specified tag even if it didn't exist before
# Inputs: 0) new value hash reference
# Returns: true if we should add the tag
sub IsCreating($)
{
    return $_[0]{IsCreating};
}

#------------------------------------------------------------------------------
# Get write group for specified tag
# Inputs: 0) new value hash reference
# Returns: Write group name
sub GetWriteGroup($)
{
    return $_[0]{WriteGroup};
}

#------------------------------------------------------------------------------
# Get name of write group or family 1 group
# Inputs: 0) ExifTool ref, 1) tagInfo ref, 2) write group name
# Returns: Name of group for verbose message
sub GetWriteGroup1($$)
{
    my ($self, $tagInfo, $writeGroup) = @_;
    return $writeGroup unless $writeGroup =~ /^(MakerNotes|XMP|Composite)$/;
    return $self->GetGroup($tagInfo, 1);
}

#------------------------------------------------------------------------------
# Get new value hash for specified tagInfo/writeGroup
# Inputs: 0) ExifTool object reference, 1) reference to tag info hash
#         2) Write group name, 3) Options: 'delete' or 'create'
# Returns: new value hash reference for specified write group
#          (or first new value hash in linked list if write group not specified)
sub GetNewValueHash($$;$$)
{
    my ($self, $tagInfo, $writeGroup, $opts) = @_;
    my $nvHash = $self->{NEW_VALUE}{$tagInfo};

    my %opts;   # quick lookup for options
    $opts and $opts{$opts} = 1;
    $writeGroup = '' unless defined $writeGroup;

    if ($writeGroup) {
        # find the new value in the list with the specified write group
        while ($nvHash and $nvHash->{WriteGroup} ne $writeGroup) {
            $nvHash = $nvHash->{Next};
        }
    }
    # remove this entry if deleting, or if creating a new entry and
    # this entry is marked with "Save" flag
    if (defined $nvHash and ($opts{'delete'} or
        ($opts{'create'} and $nvHash->{Save})))
    {
        if ($opts{'delete'}) {
            $self->RemoveNewValueHash($nvHash, $tagInfo);
            undef $nvHash;
        } else {
            # save a copy of this new value hash
            my %copy = %$nvHash;
            # make copy of Value and DelValue lists
            my $key;
            foreach $key (keys %copy) {
                next unless ref $copy{$key} eq 'ARRAY';
                $copy{$key} = [ @{$copy{$key}} ];
            }
            my $saveHash = $self->{SAVE_NEW_VALUE};
            # add to linked list of saved new value hashes
            $copy{Next} = $saveHash->{$tagInfo};
            $saveHash->{$tagInfo} = \%copy;
            delete $nvHash->{Save}; # don't save it again
        }
    }
    if (not defined $nvHash and $opts{'create'}) {
        # create a new entry
        $nvHash = {
            TagInfo => $tagInfo,
            WriteGroup => $writeGroup,
            IsNVH => 1, # set flag so we can recognize a new value hash
        };
        # add entry to our NEW_VALUE hash
        if ($self->{NEW_VALUE}{$tagInfo}) {
            # add to end of linked list
            my $lastHash = LastInList($self->{NEW_VALUE}{$tagInfo});
            $lastHash->{Next} = $nvHash;
        } else {
            $self->{NEW_VALUE}{$tagInfo} = $nvHash;
        }
    }
    return $nvHash;
}

#------------------------------------------------------------------------------
# Load all tag tables
sub LoadAllTables()
{
    return if $loadedAllTables;

    # load all of our non-referenced tables (first our modules)
    my $table;
    foreach $table (@loadAllTables) {
        my $tableName = "Image::ExifTool::$table";
        $tableName .= '::Main' unless $table =~ /:/;
        GetTagTable($tableName);
    }
    # (then our special tables)
    GetTagTable('Image::ExifTool::Extra');
    GetTagTable('Image::ExifTool::Composite');
    # recursively load all tables referenced by the current tables
    my @tableNames = keys %allTables;
    my %pushedTables;
    while (@tableNames) {
        $table = GetTagTable(shift @tableNames);
        # call write proc if it exists in case it adds tags to the table
        my $writeProc = $table->{WRITE_PROC};
        $writeProc and &$writeProc();
        # recursively scan through tables in subdirectories
        foreach (TagTableKeys($table)) {
            my @infoArray = GetTagInfoList($table,$_);
            my $tagInfo;
            foreach $tagInfo (@infoArray) {
                my $subdir = $$tagInfo{SubDirectory} or next;
                my $tableName = $$subdir{TagTable} or next;
                # next if table already loaded or queued for loading
                next if $allTables{$tableName} or $pushedTables{$tableName};
                push @tableNames, $tableName;   # must scan this one too
                $pushedTables{$tableName} = 1;
            }
        }
    }
    $loadedAllTables = 1;
}

#------------------------------------------------------------------------------
# Remove new value hash from linked list (and save if necessary)
# Inputs: 0) ExifTool object reference, 1) new value hash ref, 2) tagInfo ref
sub RemoveNewValueHash($$$)
{
    my ($self, $nvHash, $tagInfo) = @_;
    my $firstHash = $self->{NEW_VALUE}{$tagInfo};
    if ($nvHash eq $firstHash) {
        # remove first entry from linked list
        if ($nvHash->{Next}) {
            $self->{NEW_VALUE}{$tagInfo} = $nvHash->{Next};
        } else {
            delete $self->{NEW_VALUE}{$tagInfo};
        }
    } else {
        # find the list element pointing to this hash
        $firstHash = $firstHash->{Next} while $firstHash->{Next} ne $nvHash;
        # remove from linked list
        $firstHash->{Next} = $nvHash->{Next};
    }
    # save the existing entry if necessary
    if ($nvHash->{Save}) {
        my $saveHash = $self->{SAVE_NEW_VALUE};
        # add to linked list of saved new value hashes
        $nvHash->{Next} = $saveHash->{$tagInfo};
        $saveHash->{$tagInfo} = $nvHash;
    }
}

#------------------------------------------------------------------------------
# Remove all new value entries for specified group
# Inputs: 0) ExifTool object reference, 1) group name
sub RemoveNewValuesForGroup($$)
{
    my ($self, $group) = @_;

    return unless $self->{NEW_VALUE};

    # make list of all groups we must remove
    my @groups = ( $group );
    push @groups, @{$removeGroups{$group}} if $removeGroups{$group};

    my ($out, @keys, $hashKey);
    $out = $self->{OPTIONS}{TextOut} if $self->{OPTIONS}{Verbose} > 1;

    # loop though all new values, and remove any in this group
    @keys = keys %{$self->{NEW_VALUE}};
    foreach $hashKey (@keys) {
        my $nvHash = $self->{NEW_VALUE}{$hashKey};
        # loop through each entry in linked list
        for (;;) {
            my $nextHash = $nvHash->{Next};
            my $tagInfo = $nvHash->{TagInfo};
            my ($grp0,$grp1) = $self->GetGroup($tagInfo);
            my $wgrp = $nvHash->{WriteGroup};
            # use group1 if write group is not specific
            $wgrp = $grp1 if $wgrp eq $grp0;
            if (grep /^($grp0|$wgrp)$/i, @groups) {
                $out and print $out "Removed new value for $wgrp:$$tagInfo{Name}\n";
                # remove from linked list
                $self->RemoveNewValueHash($nvHash, $tagInfo);
            }
            $nvHash = $nextHash or last;
        }
    }
}

#------------------------------------------------------------------------------
# Get list of tagInfo hashes for all new data
# Inputs: 0) ExifTool object reference, 1) optional tag table pointer
# Returns: list of tagInfo hashes
sub GetNewTagInfoList($;$)
{
    my ($self, $tagTablePtr) = @_;
    my @tagInfoList;
    my $nv = $self->{NEW_VALUE};
    if ($nv) {
        my $hashKey;
        foreach $hashKey (keys %$nv) {
            my $tagInfo = $nv->{$hashKey}{TagInfo};
            next if $tagTablePtr and $tagTablePtr ne $tagInfo->{Table};
            push @tagInfoList, $tagInfo;
        }
    }
    return @tagInfoList;
}

#------------------------------------------------------------------------------
# Get hash of tagInfo references keyed on tagID for a specific table
# Inputs: 0) ExifTool object reference, 1-N) tag table pointers
# Returns: hash reference
sub GetNewTagInfoHash($@)
{
    my $self = shift;
    my (%tagInfoHash, $hashKey);
    my $nv = $self->{NEW_VALUE};
    while ($nv) {
        my $tagTablePtr = shift || last;
        foreach $hashKey (keys %$nv) {
            my $tagInfo = $nv->{$hashKey}{TagInfo};
            next if $tagTablePtr and $tagTablePtr ne $tagInfo->{Table};
            $tagInfoHash{$$tagInfo{TagID}} = $tagInfo;
        }
    }
    return \%tagInfoHash;
}

#------------------------------------------------------------------------------
# Get a tagInfo/tagID hash for subdirectories we need to add
# Inputs: 0) ExifTool object reference, 1) parent tag table reference
#         2) parent directory name (taken from GROUP0 of tag table if not defined)
# Returns: Reference to Hash of subdirectory tagInfo references keyed by tagID
#          (plus Reference to edit directory hash in list context)
sub GetAddDirHash($$;$)
{
    my ($self, $tagTablePtr, $parent) = @_;
    $parent or $parent = $tagTablePtr->{GROUPS}{0};
    my $tagID;
    my %addDirHash;
    my %editDirHash;
    my $addDirs = $self->{ADD_DIRS};
    my $editDirs = $self->{EDIT_DIRS};
    foreach $tagID (TagTableKeys($tagTablePtr)) {
        my @infoArray = GetTagInfoList($tagTablePtr,$tagID);
        my $tagInfo;
        foreach $tagInfo (@infoArray) {
            next unless $$tagInfo{SubDirectory};
            # get name for this sub directory
            # (take directory name from SubDirectory DirName if it exists,
            #  otherwise Group0 name of SubDirectory TagTable or tag Group1 name)
            my $dirName = $tagInfo->{SubDirectory}{DirName};
            unless ($dirName) {
                # use tag name for directory name and save for next time
                $dirName = $$tagInfo{Name};
                $tagInfo->{SubDirectory}{DirName} = $dirName;
            }
            # save this directory information if we are writing it
            if ($$editDirs{$dirName} and $$editDirs{$dirName} eq $parent) {
                $editDirHash{$tagID} = $tagInfo;
                $addDirHash{$tagID} = $tagInfo if $$addDirs{$dirName};
            }
        }
    }
    return (\%addDirHash, \%editDirHash) if wantarray;
    return \%addDirHash;
}

#------------------------------------------------------------------------------
# Get localized version of tagInfo hash (used by MIE, XMP, PNG and QuickTime)
# Inputs: 0) tagInfo hash ref, 1) locale code (ie. "en_CA" for MIE)
# Returns: new tagInfo hash ref, or undef if invalid
# - sets LangCode member in new tagInfo
sub GetLangInfo($$)
{
    my ($tagInfo, $langCode) = @_;
    # make a new tagInfo hash for this locale
    my $table = $$tagInfo{Table};
    my $tagID = $$tagInfo{TagID} . '-' . $langCode;
    my $langInfo = $$table{$tagID};
    unless ($langInfo) {
        # make a new tagInfo entry for this locale
        $langInfo = {
            %$tagInfo,
            Name => $$tagInfo{Name} . '-' . $langCode,
            Description => Image::ExifTool::MakeDescription($$tagInfo{Name}) .
                           " ($langCode)",
            LangCode => $langCode,
        };
        AddTagToTable($table, $tagID, $langInfo);
    }
    return $langInfo;
}

#------------------------------------------------------------------------------
# initialize ADD_DIRS and EDIT_DIRS hashes for all directories that need
# need to be created or will have tags changed in them
# Inputs: 0) ExifTool object reference, 1) file type string (or map hash ref)
#         2) preferred family 0 group name for creating tags
# Notes: The ADD_DIRS and EDIT_DIRS keys are the directory names, and the values
#        are the names of the parent directories (undefined for a top-level directory)
sub InitWriteDirs($$;$)
{
    my ($self, $fileType, $preferredGroup) = @_;
    my $editDirs = $self->{EDIT_DIRS} = { };
    my $addDirs = $self->{ADD_DIRS} = { };
    my $fileDirs = $dirMap{$fileType};
    unless ($fileDirs) {
        return unless ref $fileType eq 'HASH';
        $fileDirs = $fileType;
    }
    my @tagInfoList = $self->GetNewTagInfoList();
    my ($tagInfo, $nvHash);

    # save the preferred group
    $$self{PreferredGroup} = $preferredGroup;

    foreach $tagInfo (@tagInfoList) {
        # cycle through all hashes in linked list
        for ($nvHash=$self->GetNewValueHash($tagInfo); $nvHash; $nvHash=$$nvHash{Next}) {
            # are we creating this tag? (otherwise just deleting or editing it)
            my $isCreating = $nvHash->{IsCreating};
            if ($isCreating) {
                # if another group is taking priority, only create
                # directory if specifically adding tags to this group
                # or if this tag isn't being added to the priority group
                $isCreating = 0 if $preferredGroup and
                    $preferredGroup ne $self->GetGroup($tagInfo, 0) and
                    $nvHash->{CreateGroups}{$preferredGroup};
            } else {
                # creating this directory if any tag is preferred and has a value
                $isCreating = 1 if $preferredGroup and $$nvHash{Value} and
                    $preferredGroup eq $self->GetGroup($tagInfo, 0);
            }
            # tag belongs to directory specified by WriteGroup, or by
            # the Group0 name if WriteGroup not defined
            my $dirName = $nvHash->{WriteGroup};
            # remove MIE copy number(s) if they exist
            if ($dirName =~ /^MIE\d*(-[a-z]+)?\d*$/i) {
                $dirName = 'MIE' . ($1 || '');
            }
            my @dirNames;
            while ($dirName) {
                my $parent = $$fileDirs{$dirName};
                if (ref $parent) {
                    push @dirNames, reverse @$parent;
                    $parent = pop @dirNames;
                }
                $$editDirs{$dirName} = $parent;
                $$addDirs{$dirName} = $parent if $isCreating and $isCreating != 2;
                $dirName = $parent || shift @dirNames
            }
        }
    }
    if (%{$self->{DEL_GROUP}}) {
        # add delete groups to list of edited groups
        foreach (keys %{$self->{DEL_GROUP}}) {
            next if /^-/;   # ignore excluded groups
            my $dirName = $_;
            # translate necessary group 0 names
            $dirName = $translateWriteGroup{$dirName} if $translateWriteGroup{$dirName};
            # convert XMP group 1 names
            $dirName = 'XMP' if $dirName =~ /^XMP-/;
            my @dirNames;
            while ($dirName) {
                my $parent = $$fileDirs{$dirName};
                if (ref $parent) {
                    push @dirNames, reverse @$parent;
                    $parent = pop @dirNames;
                }
                $$editDirs{$dirName} = $parent;
                $dirName = $parent || shift @dirNames
            }
        }
    }
    # special case to edit JFIF to get resolutions if editing EXIF information
    if ($$editDirs{IFD0} and $$fileDirs{JFIF}) {
        $$editDirs{JFIF} = 'IFD1';
        $$editDirs{APP0} = undef;
    }

    if ($self->{OPTIONS}{Verbose}) {
        my $out = $self->{OPTIONS}{TextOut};
        print $out "  Editing tags in: ";
        foreach (sort keys %$editDirs) { print $out "$_ "; }
        print $out "\n";
        return unless $self->{OPTIONS}{Verbose} > 1;
        print $out "  Creating tags in: ";
        foreach (sort keys %$addDirs) { print $out "$_ "; }
        print $out "\n";
    }
}

#------------------------------------------------------------------------------
# Write an image directory
# Inputs: 0) ExifTool object reference, 1) source directory information reference
#         2) tag table reference, 3) optional reference to writing procedure
# Returns: New directory data or undefined on error
sub WriteDirectory($$$;$)
{
    my ($self, $dirInfo, $tagTablePtr, $writeProc) = @_;
    my ($out, $nvHash);

    $tagTablePtr or return undef;
    $out = $self->{OPTIONS}{TextOut} if $self->{OPTIONS}{Verbose};
    # set directory name from default group0 name if not done already
    my $dirName = $$dirInfo{DirName};
    my $dataPt = $$dirInfo{DataPt};
    my $grp0 = $tagTablePtr->{GROUPS}{0};
    $dirName or $dirName = $$dirInfo{DirName} = $grp0;
    if (%{$self->{DEL_GROUP}}) {
        my $delGroup = $self->{DEL_GROUP};
        # delete entire directory if specified
        my $grp1 = $dirName;
        my $delFlag = ($$delGroup{$grp0} or $$delGroup{$grp1});
        if ($delFlag) {
            unless ($blockExifTypes{$$self{FILE_TYPE}}) {
                # restrict delete logic to prevent entire tiff image from being killed
                # (don't allow IFD0 to be deleted, and delete only ExifIFD if EXIF specified)
                if ($$self{FILE_TYPE} eq 'PSD') {
                    # don't delete Photoshop directories from PSD image
                    undef $grp1 if $grp0 eq 'Photoshop';
                } elsif ($$self{FILE_TYPE} =~ /^(EPS|PS)$/) {
                    # allow anything to be deleted from PostScript files
                } elsif ($grp1 eq 'IFD0') {
                    my $type = $self->{TIFF_TYPE} || $self->{FILE_TYPE};
                    $$delGroup{IFD0} and $self->Warn("Can't delete IFD0 from $type",1);
                    undef $grp1;
                } elsif ($grp0 eq 'EXIF' and $$delGroup{$grp0}) {
                    undef $grp1 unless $$delGroup{$grp1} or $grp1 eq 'ExifIFD';
                }
            }
            if ($grp1) {
                if ($dataPt or $$dirInfo{RAF}) {
                    ++$self->{CHANGED};
                    $out and print $out "  Deleting $grp1\n";
                    # can no longer validate TIFF_END if deleting an entire IFD
                    delete $self->{TIFF_END} if $dirName =~ /IFD/;
                }
                # don't add back into the wrong location
                my $right = $$self{ADD_DIRS}{$grp1};
                # (take care because EXIF directory name may be either EXIF or IFD0,
                #  but IFD0 will be the one that appears in the directory map)
                $right = $$self{ADD_DIRS}{IFD0} if not $right and $grp1 eq 'EXIF';
                if ($delFlag == 2 and $right) {
                    # also check grandparent because some routines create 2 levels in 1
                    my $right2 = $$self{ADD_DIRS}{$right} || '';
                    if (not $$dirInfo{Parent} or $$dirInfo{Parent} eq $right or
                        $$dirInfo{Parent} eq $right2)
                    {
                        # create new empty directory
                        my $data = '';
                        my %dirInfo = (
                            DirName    => $$dirInfo{DirName},
                            Parent     => $$dirInfo{Parent},
                            DirStart   => 0,
                            DirLen     => 0,
                            DataPt     => \$data,
                            NewDataPos => $$dirInfo{NewDataPos},
                            Fixup      => $$dirInfo{Fixup},
                        );
                        $dirInfo = \%dirInfo;
                    } else {
                        $self->Warn("Not recreating $grp1 in $$dirInfo{Parent} (should be in $right)",1);
                        return '';
                    }
                } else {
                    return '' unless $$dirInfo{NoDelete};
                }
            }
        }
    }
    # use default proc from tag table if no proc specified
    $writeProc or $writeProc = $$tagTablePtr{WRITE_PROC} or return undef;

    # copy or delete new directory as a block if specified
    my $blockName = $dirName;
    $blockName = 'EXIF' if $blockName eq 'IFD0';
    my $tagInfo = $Image::ExifTool::Extra{$blockName} || $$dirInfo{TagInfo};
    while ($tagInfo and ($nvHash = $self->{NEW_VALUE}{$tagInfo}) and $self->IsOverwriting($nvHash)) {
        # protect against writing EXIF to wrong file types, etc
        if ($blockName eq 'EXIF') {
            unless ($blockExifTypes{$$self{FILE_TYPE}}) {
                $self->Warn("Can't write EXIF as a block to $$self{FILE_TYPE} file");
                last;
            }
            unless ($writeProc eq \&Image::ExifTool::WriteTIFF) {
                # this could happen if we called WriteDirectory for an EXIF directory
                # without going through WriteTIFF as the WriteProc, which would be bad
                # because the EXIF block could end up with two TIFF headers
                $self->Warn('Internal error writing EXIF -- please report');
                last;
            }
        }
        my $verb = 'Writing';
        my $newVal = $self->GetNewValues($nvHash);
        unless (defined $newVal and length $newVal) {
            $verb = 'Deleting';
            $newVal = '';
        }
        $$dirInfo{BlockWrite} = 1;  # set flag indicating we did a block write
        $out and print $out "  $verb $blockName as a block\n";
        ++$self->{CHANGED};
        return $newVal;
    }
    # guard against writing the same directory twice
    if (defined $dataPt and defined $$dirInfo{DirStart} and defined $$dirInfo{DataPos}) {
        my $addr = $$dirInfo{DirStart} + $$dirInfo{DataPos} + ($$dirInfo{Base}||0) + $$self{BASE};
        # (Phase One P25 IIQ files have ICC_Profile duplicated in IFD0 and IFD1)
        if ($self->{PROCESSED}{$addr} and ($dirName ne 'ICC_Profile' or $$self{TIFF_TYPE} ne 'IIQ')) {
            if ($self->Error("$dirName pointer references previous $self->{PROCESSED}{$addr} directory", 1)) {
                return undef;
            } else {
                $self->Warn("Deleting duplicate $dirName directory");
                $out and print $out "  Deleting $dirName\n";
                return '';  # delete the duplicate directory
            }
        }
        $self->{PROCESSED}{$addr} = $dirName;
    }
    my $oldDir = $self->{DIR_NAME};
    my $isRewriting = ($$dirInfo{DirLen} or (defined $dataPt and length $$dataPt) or $$dirInfo{RAF});
    my $name;
    if ($out) {
        $name = ($dirName eq 'MakerNotes' and $$dirInfo{TagInfo}) ?
                 $dirInfo->{TagInfo}{Name} : $dirName;
        if (not defined $oldDir or $oldDir ne $name) {
            my $verb = $isRewriting ? 'Rewriting' : 'Creating';
            print $out "  $verb $name\n";
        }
    }
    my $saveOrder = GetByteOrder();
    my $oldChanged = $self->{CHANGED};
    $self->{DIR_NAME} = $dirName;
    push @{$self->{PATH}}, $$dirInfo{DirName};
    $$dirInfo{IsWriting} = 1;
    my $newData = &$writeProc($self, $dirInfo, $tagTablePtr);
    pop @{$self->{PATH}};
    # nothing changed if error occurred or nothing was created
    $self->{CHANGED} = $oldChanged unless defined $newData and (length($newData) or $isRewriting);
    $self->{DIR_NAME} = $oldDir;
    SetByteOrder($saveOrder);
    print $out "  Deleting $name\n" if $out and defined $newData and not length $newData;
    return $newData;
}

#------------------------------------------------------------------------------
# Uncommon utility routines to for reading binary data values
# Inputs: 0) data reference, 1) offset into data
sub Get64s($$)
{
    my ($dataPt, $pos) = @_;
    my $pt = GetByteOrder() eq 'MM' ? 0 : 4;    # get position of high word
    my $hi = Get32s($dataPt, $pos + $pt);       # preserve sign bit of high word
    my $lo = Get32u($dataPt, $pos + 4 - $pt);
    return $hi * 4294967296 + $lo;
}
sub Get64u($$)
{
    my ($dataPt, $pos) = @_;
    my $pt = GetByteOrder() eq 'MM' ? 0 : 4;    # get position of high word
    my $hi = Get32u($dataPt, $pos + $pt);       # (unsigned this time)
    my $lo = Get32u($dataPt, $pos + 4 - $pt);
    return $hi * 4294967296 + $lo;
}
# Decode extended 80-bit float used by Apple SANE and Intel 8087
# (note: different than the IEEE standard 80-bit float)
sub GetExtended($$)
{
    my ($dataPt, $pos) = @_;
    my $pt = GetByteOrder() eq 'MM' ? 0 : 2;    # get position of exponent
    my $exp = Get16u($dataPt, $pos + $pt);
    my $sig = Get64u($dataPt, $pos + 2 - $pt);  # get significand as int64u
    my $sign = $exp & 0x8000 ? -1 : 1;
    $exp = ($exp & 0x7fff) - 16383 - 63; # (-63 to fractionalize significand)
    return $sign * $sig * 2 ** $exp;
}

#------------------------------------------------------------------------------
# Dump data in hex and ASCII to console
# Inputs: 0) data reference, 1) length or undef, 2-N) Options:
# Options: Start => offset to start of data (default=0)
#          Addr => address to print for data start (default=DataPos+Start)
#          DataPos => address of start of data
#          Width => width of printout (bytes, default=16)
#          Prefix => prefix to print at start of line (default='')
#          MaxLen => maximum length to dump
#          Out => output file reference
#          Len => data length
sub HexDump($;$%)
{
    my $dataPt = shift;
    my $len    = shift;
    my %opts   = @_;
    my $start  = $opts{Start}  || 0;
    my $addr   = $opts{Addr};
    my $wid    = $opts{Width}  || 16;
    my $prefix = $opts{Prefix} || '';
    my $out    = $opts{Out}    || \*STDOUT;
    my $maxLen = $opts{MaxLen};
    my $datLen = length($$dataPt) - $start;
    my $more;
    $len = $opts{Len} if defined $opts{Len};

    $addr = $start + ($opts{DataPos} || 0) unless defined $addr;
    $len = $datLen unless defined $len;
    if ($maxLen and $len > $maxLen) {
        # print one line less to allow for $more line below
        $maxLen = int(($maxLen - 1) / $wid) * $wid;
        $more = $len - $maxLen;
        $len = $maxLen;
    }
    if ($len > $datLen) {
        print $out "$prefix    Warning: Attempted dump outside data\n";
        print $out "$prefix    ($len bytes specified, but only $datLen available)\n";
        $len = $datLen;
    }
    my $format = sprintf("%%-%ds", $wid * 3);
    my $tmpl = 'H2' x $wid; # ('(H2)*' would have been nice, but older perl versions don't support it)
    my $i;
    for ($i=0; $i<$len; $i+=$wid) {
        $wid > $len-$i and $wid = $len-$i, $tmpl = 'H2' x $wid;
        printf $out "$prefix%8.4x: ", $addr+$i;
        my $dat = substr($$dataPt, $i+$start, $wid);
        my $s = join(' ', unpack($tmpl, $dat));
        printf $out $format, $s;
        $dat =~ tr /\x00-\x1f\x7f-\xff/./;
        print $out "[$dat]\n";
    }
    $more and printf $out "$prefix    [snip $more bytes]\n";
}

#------------------------------------------------------------------------------
# Print verbose tag information
# Inputs: 0) ExifTool object reference, 1) tag ID
#         2) tag info reference (or undef)
#         3-N) extra parms:
# Parms: Index => Index of tag in menu (starting at 0)
#        Value => Tag value
#        DataPt => reference to value data block
#        DataPos => location of data block in file
#        Size => length of value data within block
#        Format => value format string
#        Count => number of values
#        Extra => Extra Verbose=2 information to put after tag number
#        Table => Reference to tag table
#        --> plus any of these HexDump() options: Start, Addr, Width
sub VerboseInfo($$$%)
{
    my ($self, $tagID, $tagInfo, %parms) = @_;
    my $verbose = $self->{OPTIONS}{Verbose};
    my $out = $self->{OPTIONS}{TextOut};
    my ($tag, $line, $hexID);

    # generate hex number if tagID is numerical
    if (defined $tagID) {
        $tagID =~ /^\d+$/ and $hexID = sprintf("0x%.4x", $tagID);
    } else {
        $tagID = 'Unknown';
    }
    # get tag name
    if ($tagInfo and $$tagInfo{Name}) {
        $tag = $$tagInfo{Name};
    } else {
        my $prefix;
        $prefix = $parms{Table}{TAG_PREFIX} if $parms{Table};
        if ($prefix or $hexID) {
            $prefix = 'Unknown' unless $prefix;
            $tag = $prefix . '_' . ($hexID ? $hexID : $tagID);
        } else {
            $tag = $tagID;
        }
    }
    my $dataPt = $parms{DataPt};
    my $size = $parms{Size};
    $size = length $$dataPt unless defined $size or not $dataPt;
    my $indent = $self->{INDENT};

    # Level 1: print tag/value information
    $line = $indent;
    my $index = $parms{Index};
    if (defined $index) {
        $line .= $index . ') ';
        $line .= ' ' if length($index) < 2;
        $indent .= '    '; # indent everything else to align with tag name
    }
    $line .= $tag;
    if ($tagInfo and $$tagInfo{SubDirectory}) {
        $line .= ' (SubDirectory) -->';
    } else {
        my $maxLen = 90 - length($line);
        if (defined $parms{Value}) {
            $line .= ' = ' . $self->Printable($parms{Value}, $maxLen);
        } elsif ($dataPt) {
            my $start = $parms{Start} || 0;
            $line .= ' = ' . $self->Printable(substr($$dataPt,$start,$size), $maxLen);
        }
    }
    print $out "$line\n";

    # Level 2: print detailed information about the tag
    if ($verbose > 1 and ($parms{Extra} or $parms{Format} or
        $parms{DataPt} or defined $size or $tagID =~ /\//))
    {
        $line = $indent . '- Tag ';
        if ($hexID) {
            $line .= $hexID;
        } else {
            $tagID =~ s/([\0-\x1f\x7f-\xff])/sprintf('\\x%.2x',ord $1)/ge;
            $line .= "'$tagID'";
        }
        $line .= $parms{Extra} if defined $parms{Extra};
        my $format = $parms{Format};
        if ($format or defined $size) {
            $line .= ' (';
            if (defined $size) {
                $line .= "$size bytes";
                $line .= ', ' if $format;
            }
            if ($format) {
                $line .= $format;
                $line .= '['.$parms{Count}.']' if $parms{Count};
            }
            $line .= ')';
        }
        $line .= ':' if $verbose > 2 and $parms{DataPt};
        print $out "$line\n";
    }

    # Level 3: do hex dump of value
    if ($verbose > 2 and $parms{DataPt}) {
        $parms{Out} = $out;
        $parms{Prefix} = $indent;
        # limit dump length if Verbose < 5
        $parms{MaxLen} = $verbose == 3 ? 96 : 2048 if $verbose < 5;
        HexDump($dataPt, $size, %parms);
    }
}

#------------------------------------------------------------------------------
# Dump trailer information
# Inputs: 0) ExifTool object ref, 1) dirInfo hash (RAF, DirName, DataPos, DirLen)
# Notes: Restores current file position before returning
sub DumpTrailer($$)
{
    my ($self, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my $curPos = $raf->Tell();
    my $trailer = $$dirInfo{DirName} || 'Unknown';
    my $pos = $$dirInfo{DataPos};
    my $verbose = $self->{OPTIONS}{Verbose};
    my $htmlDump = $self->{HTML_DUMP};
    my ($buff, $buf2);
    my $size = $$dirInfo{DirLen};
    $pos = $curPos unless defined $pos;

    # get full trailer size if not specified
    for (;;) {
        unless ($size) {
            $raf->Seek(0, 2) or last;
            $size = $raf->Tell() - $pos;
            last unless $size;
        }
        $raf->Seek($pos, 0) or last;
        if ($htmlDump) {
            my $num = $raf->Read($buff, $size) or return;
            my $desc = "$trailer trailer";
            $desc = "[$desc]" if $trailer eq 'Unknown';
            $self->HDump($pos, $num, $desc, undef, 0x08);
            last;
        }
        my $out = $self->{OPTIONS}{TextOut};
        printf $out "$trailer trailer (%d bytes at offset 0x%.4x):\n", $size, $pos;
        last unless $verbose > 2;
        my $num = $size;    # number of bytes to read
        # limit size if not very verbose
        if ($verbose < 5) {
            my $limit = $verbose < 4 ? 96 : 512;
            $num = $limit if $num > $limit;
        }
        $raf->Read($buff, $num) == $num or return;
        # read the end of the trailer too if not done already
        if ($size > 2 * $num) {
            $raf->Seek($pos + $size - $num, 0);
            $raf->Read($buf2, $num);
        } elsif ($size > $num) {
            $raf->Seek($pos + $num, 0);
            $raf->Read($buf2, $size - $num);
            $buff .= $buf2;
            undef $buf2;
        }
        HexDump(\$buff, undef, Addr => $pos, Out => $out);
        if (defined $buf2) {
            print $out "    [snip ", $size - $num * 2, " bytes]\n";
            HexDump(\$buf2, undef, Addr => $pos + $size - $num, Out => $out);
        }
        last;
    }
    $raf->Seek($curPos, 0);
}

#------------------------------------------------------------------------------
# Dump unknown trailer information
# Inputs: 0) ExifTool ref, 1) dirInfo ref (with RAF, DataPos and DirLen defined)
# Notes: changes dirInfo elements
sub DumpUnknownTrailer($$)
{
    my ($self, $dirInfo) = @_;
    my $pos = $$dirInfo{DataPos};
    my $endPos = $pos + $$dirInfo{DirLen};
    # account for preview/MPF image trailer
    my $prePos = $self->{VALUE}{PreviewImageStart} || $$self{PreviewImageStart};
    my $preLen = $self->{VALUE}{PreviewImageLength} || $$self{PreviewImageLength};
    my $tag = 'PreviewImage';
    my $mpImageNum = 0;
    my (%image, $lastOne);
    for (;;) {
        # add to Preview block list if valid and in the trailer
        $image{$prePos} = [$tag, $preLen] if $prePos and $preLen and $prePos+$preLen > $pos;
        last if $lastOne;   # checked all images
        # look for MPF images (in the the proper order)
        ++$mpImageNum;
        $prePos = $self->{VALUE}{"MPImageStart ($mpImageNum)"};
        if (defined $prePos) {
            $preLen = $self->{VALUE}{"MPImageLength ($mpImageNum)"};
        } else {
            $prePos = $self->{VALUE}{'MPImageStart'};
            $preLen = $self->{VALUE}{'MPImageLength'};
            $lastOne = 1;
        }
        $tag = "MPImage$mpImageNum";
    }
    # dump trailer sections in order
    $image{$endPos} = [ '', 0 ];    # add terminator "image"
    foreach $prePos (sort { $a <=> $b } keys %image) {
        if ($pos < $prePos) {
            # dump unknown trailer data
            $$dirInfo{DirName} = 'Unknown';
            $$dirInfo{DataPos} = $pos;
            $$dirInfo{DirLen} = $prePos - $pos;
            $self->DumpTrailer($dirInfo);
        }
        ($tag, $preLen) = @{$image{$prePos}};
        last unless $preLen;
        # dump image if verbose (it is htmlDump'd by ExtractImage)
        if ($self->{OPTIONS}{Verbose}) {
            $$dirInfo{DirName} = $tag;
            $$dirInfo{DataPos} = $prePos;
            $$dirInfo{DirLen}  = $preLen;
            $self->DumpTrailer($dirInfo);
        }
        $pos = $prePos + $preLen;
    }
}

#------------------------------------------------------------------------------
# Find last element in linked list
# Inputs: 0) element in list
# Returns: Last element in list
sub LastInList($)
{
    my $element = shift;
    while ($element->{Next}) {
        $element = $element->{Next};
    }
    return $element;
}

#------------------------------------------------------------------------------
# Print verbose directory information
# Inputs: 0) ExifTool object reference, 1) directory name or dirInfo ref
#         2) number of entries in directory (or 0 if unknown)
#         3) optional size of directory in bytes
sub VerboseDir($$;$$)
{
    my ($self, $name, $entries, $size) = @_;
    return unless $self->{OPTIONS}{Verbose};
    if (ref $name eq 'HASH') {
        $size = $$name{DirLen} unless $size;
        $name = $$name{Name} || $$name{DirName};
    }
    my $indent = substr($self->{INDENT}, 0, -2);
    my $out = $self->{OPTIONS}{TextOut};
    my $str = $entries ? " with $entries entries" : '';
    $str .= ", $size bytes" if $size;
    print $out "$indent+ [$name directory$str]\n";
}

#------------------------------------------------------------------------------
# Print verbose value while writing
# Inputs: 0) ExifTool object ref, 1) heading "ie. '+ IPTC:Keywords',
#         2) value, 3) [optional] extra text after value
sub VerboseValue($$$;$)
{
    return unless $_[0]{OPTIONS}{Verbose} > 1;
    my ($self, $str, $val, $xtra) = @_;
    my $out = $self->{OPTIONS}{TextOut};
    $xtra or $xtra = '';
    my $maxLen = 81 - length($str) - length($xtra);
    $val = $self->Printable($val, $maxLen);
    print $out "    $str = '$val'$xtra\n";
}

#------------------------------------------------------------------------------
# Pack Unicode numbers into UTF8 string
# Inputs: 0-N) list of Unicode numbers
# Returns: Packed UTF-8 string
sub PackUTF8(@)
{
    my @out;
    while (@_) {
        my $ch = pop;
        unshift(@out, $ch), next if $ch < 0x80;
        unshift(@out, 0x80 | ($ch & 0x3f));
        $ch >>= 6;
        unshift(@out, 0xc0 | $ch), next if $ch < 0x20;
        unshift(@out, 0x80 | ($ch & 0x3f));
        $ch >>= 6;
        unshift(@out, 0xe0 | $ch), next if $ch < 0x10;
        unshift(@out, 0x80 | ($ch & 0x3f));
        $ch >>= 6;
        unshift(@out, 0xf0 | ($ch & 0x07));
    }
    return pack('C*', @out);
}

#------------------------------------------------------------------------------
# Unpack numbers from UTF8 string
# Inputs: 0) UTF-8 string
# Returns: List of Unicode numbers (sets $evalWarning on error)
sub UnpackUTF8($)
{
    my (@out, $pos);
    pos($_[0]) = $pos = 0;  # start at beginning of string
    for (;;) {
        my ($ch, $newPos, $val, $byte);
        if ($_[0] =~ /([\x80-\xff])/g) {
            $ch = ord($1);
            $newPos = pos($_[0]) - 1;
        } else {
            $newPos = length $_[0];
        }
        # unpack 7-bit characters
        my $len = $newPos - $pos;
        push @out, unpack("x${pos}C$len",$_[0]) if $len;
        last unless defined $ch;
        $pos = $newPos + 1;
        # minimum lead byte for 2-byte sequence is 0xc2 (overlong sequences
        # not allowed), 0xf8-0xfd are restricted by RFC 3629 (no 5 or 6 byte
        # sequences), and 0xfe and 0xff are not valid in UTF-8 strings
        if ($ch < 0xc2 or $ch >= 0xf8) {
            push @out, ord('?');    # invalid UTF-8
            $evalWarning = 'Bad UTF-8';
            next;
        }
        # decode 2, 3 and 4-byte sequences
        my $n = 1;
        if ($ch < 0xe0) {
            $val = $ch & 0x1f;      # 2-byte sequence
        } elsif ($ch < 0xf0) {
            $val = $ch & 0x0f;      # 3-byte sequence
            ++$n;
        } else {
            $val = $ch & 0x07;      # 4-byte sequence
            $n += 2;
        }
        unless ($_[0] =~ /\G([\x80-\xbf]{$n})/g) {
            pos($_[0]) = $pos;      # restore position
            push @out, ord('?');    # invalid UTF-8
            $evalWarning = 'Bad UTF-8';
            next;
        }
        foreach $byte (unpack 'C*', $1) {
            $val = ($val << 6) | ($byte & 0x3f);
        }
        push @out, $val;    # save Unicode character value
        $pos += $n;         # position at end of UTF-8 character
    }
    return @out;
}

#------------------------------------------------------------------------------
# Inverse date/time print conversion (reformat to YYYY:mm:dd HH:MM:SS[.ss][+-HH:MM|Z])
# Inputs: 0) ExifTool object ref, 1) Date/Time string, 2) timezone flag:
#               0     - remove timezone and sub-seconds if they exist
#               1     - add timezone if it doesn't exist
#               undef - leave timezone alone
#         3) flag to allow date-only (YYYY, YYYY:mm or YYYY:mm:dd) or time without seconds
# Returns: formatted date/time string (or undef and issues warning on error)
# Notes: currently accepts different separators, but doesn't use DateFormat yet
sub InverseDateTime($$;$$)
{
    my ($self, $val, $tzFlag, $dateOnly) = @_;
    my ($rtnVal, $tz);
    # strip off timezone first if it exists
    if ($val =~ s/([+-])(\d{1,2}):?(\d{2})$//i) {
        $tz = sprintf("$1%.2d:$3", $2);
    } elsif ($val =~ s/Z$//i) {
        $tz = 'Z';
    } else {
        $tz = '';
    }
    # strip of sub seconds
    my $fs = $val =~ /(\.\d+)$/ ? $1 : '';
    if ($val =~ /(\d{4})/g) {           # get YYYY
        my $yr = $1;
        my @a = ($val =~ /\d{2}/g);     # get mm, dd, HH, and maybe MM, SS
        if (@a >= 3) {
            my $ss = $a[4];             # get SS
            push @a, '00' while @a < 5; # add MM, SS if not given
            # add/remove timezone if necessary
            if ($tzFlag) {
                if (not $tz) {
                    if (eval 'require Time::Local') {
                        # determine timezone offset for this time
                        my @args = ($a[4],$a[3],$a[2],$a[1],$a[0]-1,$yr-1900);
                        my $diff = Time::Local::timegm(@args) - TimeLocal(@args);
                        $tz = TimeZoneString($diff / 60);
                    } else {
                        $tz = 'Z';  # don't know time zone
                    }
                }
            } elsif (defined $tzFlag) {
                $tz = $fs = ''; # remove timezone and sub-seconds
            }
            if (defined $ss) {
                $ss = ":$ss";
            } elsif ($dateOnly) {
                $ss = '';
            } else {
                $ss = ':00';
            }
            # construct properly formatted date/time string
            $rtnVal = "$yr:$a[0]:$a[1] $a[2]:$a[3]$ss$fs$tz";
        } elsif ($dateOnly) {
            $rtnVal = join ':', $yr, @a;
        }
    }
    $rtnVal or warn "Invalid date/time (use YYYY:mm:dd HH:MM:SS[.ss][+/-HH:MM|Z])\n";
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Set byte order according to our current preferences
# Inputs: 0) ExifTool object ref
# Returns: new byte order ('II' or 'MM') and sets current byte order
# Notes: takes the first of the following that is valid:
#  1) ByteOrder option
#  2) new value for ExifByteOrder
#  3) makenote byte order from last file read
#  4) big endian
sub SetPreferredByteOrder($)
{
    my $self = shift;
    my $byteOrder = $self->Options('ByteOrder') ||
                    $self->GetNewValues('ExifByteOrder') ||
                    $self->{MAKER_NOTE_BYTE_ORDER} || 'MM';
    unless (SetByteOrder($byteOrder)) {
        warn "Invalid byte order '$byteOrder'\n" if $self->Options('Verbose');
        $byteOrder = $self->{MAKER_NOTE_BYTE_ORDER} || 'MM';
        SetByteOrder($byteOrder);
    }
    return GetByteOrder();
}

#------------------------------------------------------------------------------
# Assemble a continuing fraction into a rational value
# Inputs: 0) numerator, 1) denominator
#         2-N) list of fraction denominators, deepest first
# Returns: numerator, denominator (in list context)
sub AssembleRational($$@)
{
    @_ < 3 and return @_;
    my ($num, $denom, $frac) = splice(@_, 0, 3);
    return AssembleRational($frac*$num+$denom, $num, @_);
}

#------------------------------------------------------------------------------
# Convert a floating point number (or 'inf' or 'undef' or a fraction) into a rational
# Inputs: 0) floating point number, 1) optional maximum value (defaults to 0x7fffffff)
# Returns: numerator, denominator (in list context)
# Notes:
# - the returned rational will be accurate to at least 8 significant figures if possible
# - ie. an input of 3.14159265358979 returns a rational of 104348/33215,
#   which equals    3.14159265392142 and is accurate to 10 significant figures
# - these routines were a bit tricky, but fun to write!
sub Rationalize($;$)
{
    my $val = shift;
    return (1, 0) if $val eq 'inf';
    return (0, 0) if $val eq 'undef';
    return ($1,$2) if $val =~ m{^([-+]?\d+)/(\d+)$}; # accept fractional values
    # Note: Just testing "if $val" doesn't work because '0.0' is true!  (ugghh!)
    return (0, 1) if $val == 0;
    my $sign = $val < 0 ? ($val = -$val, -1) : 1;
    my ($num, $denom, @fracs);
    my $frac = $val;
    my $maxInt = shift || 0x7fffffff;
    for (;;) {
        my ($n, $d) = AssembleRational(int($frac + 0.5), 1, @fracs);
        if ($n > $maxInt or $d > $maxInt) {
            last if defined $num;
            return ($sign, $maxInt) if $val < 1;
            return ($sign * $maxInt, 1);
        }
        ($num, $denom) = ($n, $d);      # save last good values
        my $err = ($n/$d-$val) / $val;  # get error of this rational
        last if abs($err) < 1e-8;       # all done if error is small
        my $int = int($frac);
        unshift @fracs, $int;
        last unless $frac -= $int;
        $frac = 1 / $frac;
    }
    return ($num * $sign, $denom);
}

#------------------------------------------------------------------------------
# Utility routines to for writing binary data values
# Inputs: 0) value, 1) data ref, 2) offset
# Notes: prototype is (@) so values can be passed from list if desired
sub Set16s(@)
{
    my $val = shift;
    $val < 0 and $val += 0x10000;
    return Set16u($val, @_);
}
sub Set32s(@)
{
    my $val = shift;
    $val < 0 and $val += 0xffffffff, ++$val;
    return Set32u($val, @_);
}
sub SetRational64u(@) {
    my ($numer,$denom) = Rationalize($_[0],0xffffffff);
    my $val = Set32u($numer) . Set32u($denom);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
sub SetRational64s(@) {
    my ($numer,$denom) = Rationalize($_[0]);
    my $val = Set32s($numer) . Set32u($denom);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
sub SetRational32u(@) {
    my ($numer,$denom) = Rationalize($_[0],0xffff);
    my $val = Set16u($numer) . Set16u($denom);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
sub SetRational32s(@) {
    my ($numer,$denom) = Rationalize($_[0],0x7fff);
    my $val = Set16s($numer) . Set16u($denom);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
sub SetFixed16u(@) {
    my $val = int(shift() * 0x100 + 0.5);
    return Set16u($val, @_);
}
sub SetFixed16s(@) {
    my $val = shift;
    return Set16s(int($val * 0x100 + ($val < 0 ? -0.5 : 0.5)), @_);
}
sub SetFixed32u(@) {
    my $val = int(shift() * 0x10000 + 0.5);
    return Set32u($val, @_);
}
sub SetFixed32s(@) {
    my $val = shift;
    return Set32s(int($val * 0x10000 + ($val < 0 ? -0.5 : 0.5)), @_);
}
sub SetFloat(@) {
    my $val = SwapBytes(pack('f',$_[0]), 4);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
sub SetDouble(@) {
    # swap 32-bit words (ARM quirk) and bytes if necessary
    my $val = SwapBytes(SwapWords(pack('d',$_[0])), 8);
    $_[1] and substr(${$_[1]}, $_[2], length($val)) = $val;
    return $val;
}
#------------------------------------------------------------------------------
# hash lookups for writing binary data values
my %writeValueProc = (
    int8s => \&Set8s,
    int8u => \&Set8u,
    int16s => \&Set16s,
    int16u => \&Set16u,
    int16uRev => \&Set16uRev,
    int32s => \&Set32s,
    int32u => \&Set32u,
    rational32s => \&SetRational32s,
    rational32u => \&SetRational32u,
    rational64s => \&SetRational64s,
    rational64u => \&SetRational64u,
    fixed16u => \&SetFixed16u,
    fixed16s => \&SetFixed16s,
    fixed32u => \&SetFixed32u,
    fixed32s => \&SetFixed32s,
    float => \&SetFloat,
    double => \&SetDouble,
    ifd => \&Set32u,
);
# verify that we can write floats on this platform
{
    my %writeTest = (
        float =>  [ -3.14159, 'c0490fd0' ],
        double => [ -3.14159, 'c00921f9f01b866e' ],
    );
    my $format;
    my $oldOrder = GetByteOrder();
    SetByteOrder('MM');
    foreach $format (keys %writeTest) {
        my ($val, $hex) = @{$writeTest{$format}};
        # add floating point entries if we can write them
        next if unpack('H*', &{$writeValueProc{$format}}($val)) eq $hex;
        delete $writeValueProc{$format};    # we can't write them
    }
    SetByteOrder($oldOrder);
}

#------------------------------------------------------------------------------
# write binary data value (with current byte ordering)
# Inputs: 0) value, 1) format string
#         2) optional number of values (1 or string length if not specified)
#         3) optional data reference, 4) value offset
# Returns: packed value (and sets value in data) or undef on error
# Notes: May modify input value to round for integer formats
sub WriteValue($$;$$$$)
{
    my ($val, $format, $count, $dataPt, $offset) = @_;
    my $proc = $writeValueProc{$format};
    my $packed;

    if ($proc) {
        my @vals = split(' ',$val);
        if ($count) {
            $count = @vals if $count < 0;
        } else {
            $count = 1;   # assume 1 if count not specified
        }
        $packed = '';
        while ($count--) {
            $val = shift @vals;
            return undef unless defined $val;
            # validate numerical formats
            if ($format =~ /^int/) {
                unless (IsInt($val) or IsHex($val)) {
                    return undef unless IsFloat($val);
                    # round to nearest integer
                    $val = int($val + ($val < 0 ? -0.5 : 0.5));
                    $_[0] = $val;
                }
            } elsif (not IsFloat($val)) {
                return undef unless $format =~ /^rational/ and ($val eq 'inf' or
                    $val eq 'undef' or IsRational($val));
            }
            $packed .= &$proc($val);
        }
    } elsif ($format eq 'string' or $format eq 'undef') {
        $format eq 'string' and $val .= "\0";   # null-terminate strings
        if ($count and $count > 0) {
            my $diff = $count - length($val);
            if ($diff) {
                #warn "wrong string length!\n";
                # adjust length of string to match specified count
                if ($diff < 0) {
                    if ($format eq 'string') {
                        return undef unless $count;
                        $val = substr($val, 0, $count - 1) . "\0";
                    } else {
                        $val = substr($val, 0, $count);
                    }
                } else {
                    $val .= "\0" x $diff;
                }
            }
        } else {
            $count = length($val);
        }
        $dataPt and substr($$dataPt, $offset, $count) = $val;
        return $val;
    } else {
        warn "Sorry, Can't write $format values on this platform\n";
        return undef;
    }
    $dataPt and substr($$dataPt, $offset, length($packed)) = $packed;
    return $packed;
}

#------------------------------------------------------------------------------
# Encode bit mask (the inverse of DecodeBits())
# Inputs: 0) value to encode, 1) Reference to hash for encoding (or undef)
#         2) optional number of bits per word (defaults to 32), 3) total bits
# Returns: bit mask or undef on error (plus error string in list context)
sub EncodeBits($$;$$)
{
    my ($val, $lookup, $bits, $num) = @_;
    $bits or $bits = 32;
    $num or $num = $bits;
    my $words = int(($num + $bits - 1) / $bits);
    my @outVal = (0) x $words;
    if ($val ne '(none)') {
        my @vals = split /\s*,\s*/, $val;
        foreach $val (@vals) {
            my $bit;
            if ($lookup) {
                $bit = ReverseLookup($val, $lookup);
                # (Note: may get non-numerical $bit values from Unknown() tags)
                unless (defined $bit) {
                    if ($val =~ /\[(\d+)\]/) { # numerical bit specification
                        $bit = $1;
                    } else {
                        # don't return error string unless more than one value
                        return undef unless @vals > 1 and wantarray;
                        return (undef, "no match for '$val'");
                    }
                }
            } else {
                $bit = $val;
            }
            unless (IsInt($bit) and $bit < $num) {
                return undef unless wantarray;
                return (undef, IsInt($bit) ? 'bit number too high' : 'not an integer');
            }
            my $word = int($bit / $bits);
            $outVal[$word] |= (1 << ($bit - $word * $bits));
        }
    }
    return "@outVal";
}

#------------------------------------------------------------------------------
# get current position in output file
# Inputs: 0) file or scalar reference
# Returns: Current position or -1 on error
sub Tell($)
{
    my $outfile = shift;
    if (UNIVERSAL::isa($outfile,'GLOB')) {
        return tell($outfile);
    } else {
        return length($$outfile);
    }
}

#------------------------------------------------------------------------------
# write to file or memory
# Inputs: 0) file or scalar reference, 1-N) list of stuff to write
# Returns: true on success
sub Write($@)
{
    my $outfile = shift;
    if (UNIVERSAL::isa($outfile,'GLOB')) {
        return print $outfile @_;
    } elsif (ref $outfile eq 'SCALAR') {
        $$outfile .= join('', @_);
        return 1;
    }
    return 0;
}

#------------------------------------------------------------------------------
# Write trailer buffer to file (applying fixups if necessary)
# Inputs: 0) ExifTool object ref, 1) trailer dirInfo ref, 2) output file ref
# Returns: 1 on success
sub WriteTrailerBuffer($$$)
{
    my ($self, $trailInfo, $outfile) = @_;
    if ($self->{DEL_GROUP}{Trailer}) {
        $self->VPrint(0, "  Deleting trailer ($$trailInfo{Offset} bytes)\n");
        ++$self->{CHANGED};
        return 1;
    }
    my $pos = Tell($outfile);
    my $trailPt = $$trailInfo{OutFile};
    # apply fixup if necessary (AFCP requires this)
    if ($$trailInfo{Fixup}) {
        if ($pos > 0) {
            # shift offsets to final AFCP location and write it out
            $trailInfo->{Fixup}{Shift} += $pos;
            $trailInfo->{Fixup}->ApplyFixup($trailPt);
        } else {
            $self->Error("Can't get file position for trailer offset fixup",1);
        }
    }
    return Write($outfile, $$trailPt);
}

#------------------------------------------------------------------------------
# Add trailers as a block
# Inputs: 0) ExifTool object ref, 1) [optional] trailer data raf,
#         1 or 2-N) trailer types to add (or none to add all)
# Returns: new trailer ref, or undef
# - increments CHANGED if trailer was added
sub AddNewTrailers($;@)
{
    my ($self, @types) = @_;
    my $trailPt;
    ref $types[0] and $trailPt = shift @types;
    $types[0] or shift @types; # (in case undef data ref is passed)
    # add all possible trailers if none specified (currently only CanonVRD)
    @types or @types = qw(CanonVRD);
    # add trailers as a block
    my $type;
    foreach $type (@types) {
        next unless $self->{NEW_VALUE}{$Image::ExifTool::Extra{$type}};
        my $val = $self->GetNewValues($type) or next;
        my $verb = $trailPt ? 'Writing' : 'Adding';
        $self->VPrint(0, "  $verb $type as a block\n");
        if ($trailPt) {
            $$trailPt .= $val;
        } else {
            $trailPt = \$val;
        }
        ++$$self{CHANGED};
    }
    return $trailPt;
}

#------------------------------------------------------------------------------
# Write segment, splitting up into multiple segments if necessary
# Inputs: 0) file or scalar reference, 1) segment marker
#         2) segment header, 3) segment data ref, 4) segment type
# Returns: number of segments written, or 0 on error
sub WriteMultiSegment($$$$;$)
{
    my ($outfile, $marker, $header, $dataPt, $type) = @_;
    $type or $type = '';
    my $len = length($$dataPt);
    my $hdr = "\xff" . chr($marker);
    my $count = 0;
    my $maxLen = $maxSegmentLen - length($header);
    $maxLen -= 2 if $type eq 'ICC'; # leave room for segment counters
    my $num = int(($len + $maxLen - 1) / $maxLen);  # number of segments to write
    my $n;
    # write data, splitting into multiple segments if necessary
    # (each segment gets its own header)
    for ($n=0; $n<$len; $n+=$maxLen) {
        ++$count;
        my $size = $len - $n;
        $size > $maxLen and $size = $maxLen;
        my $buff = substr($$dataPt,$n,$size);
        $size += length($header);
        if ($type eq 'ICC') {
            $buff = pack('CC', $count, $num) . $buff;
            $size += 2;
        }
        # write the new segment with appropriate header
        my $segHdr = $hdr . pack('n', $size + 2);
        Write($outfile, $segHdr, $header, $buff) or return 0;
    }
    return $count;
}

#------------------------------------------------------------------------------
# Write XMP segment(s) to JPEG file
# Inputs: 0) ExifTool object ref, 1) outfile ref, 2) XMP data ref,
#         3) extended XMP data ref, 4) 32-char extended XMP GUID (or undef if no extended data)
# Returns: true on success, false on write error
sub WriteMultiXMP($$$$$)
{
    my ($self, $outfile, $dataPt, $extPt, $guid) = @_;
    my $success = 1;

    # write main XMP segment
    my $size = length($$dataPt) + length($xmpAPP1hdr);
    if ($size > $maxXMPLen) {
        $self->Error("XMP block too large for JPEG segment! ($size bytes)", 1);
        return 1;
    }
    my $app1hdr = "\xff\xe1" . pack('n', $size + 2);
    Write($outfile, $app1hdr, $xmpAPP1hdr, $$dataPt) or $success = 0;
    # write extended XMP segment(s) if necessary
    if (defined $guid) {
        $size = length($$extPt);
        my $maxLen = $maxXMPLen - 75; # maximum size without 75 byte header
        my $off;
        for ($off=0; $off<$size; $off+=$maxLen) {
            # header(75) = signature(35) + guid(32) + size(4) + offset(4)
            my $len = $size - $off;
            $len = $maxLen if $len > $maxLen;
            $app1hdr = "\xff\xe1" . pack('n', $len + 75 + 2);
            $self->VPrint(0, "Writing extended XMP segment ($len bytes)\n");
            Write($outfile, $app1hdr, $xmpExtAPP1hdr, $guid, pack('N2', $size, $off),
                  substr($$extPt, $off, $len)) or $success = 0;
        }
    }
    return $success;
}

#------------------------------------------------------------------------------
# WriteJPEG : Write JPEG image
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid JPEG file, or -1 if
#          an output file was specified and a write error occurred
sub WriteJPEG($$)
{
    my ($self, $dirInfo) = @_;
    my $outfile = $$dirInfo{OutFile};
    my $raf = $$dirInfo{RAF};
    my ($ch,$s,$length);
    my $verbose = $self->{OPTIONS}{Verbose};
    my $out = $self->{OPTIONS}{TextOut};
    my $rtnVal = 0;
    my ($err, %doneDir);
    my %dumpParms = ( Out => $out );
    my ($writeBuffer, $oldOutfile); # used to buffer writing until PreviewImage position is known

    # check to be sure this is a valid JPG file
    return 0 unless $raf->Read($s,2) == 2 and $s eq "\xff\xd8";
    $dumpParms{MaxLen} = 128 unless $verbose > 3;

    delete $self->{PREVIEW_INFO};   # reset preview information
    delete $self->{DEL_PREVIEW};    # reset flag to delete preview

    Write($outfile, $s) or $err = 1;
    # figure out what segments we need to write for the tags we have set
    my $addDirs = $self->{ADD_DIRS};
    my $editDirs = $self->{EDIT_DIRS};
    my $delGroup = $self->{DEL_GROUP};
    my $path = $$self{PATH};
    my $pn = scalar @$path;

    # set input record separator to 0xff (the JPEG marker) to make reading quicker
    local $/ = "\xff";
#
# pre-scan image to determine if any create-able segment already exists
#
    my $pos = $raf->Tell();
    my ($marker, @dirOrder, %dirCount);
    Prescan: for (;;) {
        # read up to next marker (JPEG markers begin with 0xff)
        $raf->ReadLine($s) or last;
        # JPEG markers can be padded with unlimited 0xff's
        for (;;) {
            $raf->Read($ch, 1) or last Prescan;
            $marker = ord($ch);
            last unless $marker == 0xff;
        }
        # SOS signifies end of meta information
        if ($marker == 0xda) {
            push(@dirOrder, 'SOS');
            $dirCount{SOS} = 1;
            last;
        }
        my $dirName;
        # handle SOF markers: SOF0-SOF15, except DHT(0xc4), JPGA(0xc8) and DAC(0xcc)
        if (($marker & 0xf0) == 0xc0 and ($marker == 0xc0 or $marker & 0x03)) {
            last unless $raf->Seek(7, 1);
        # read data for all markers except stand-alone
        # markers 0x00, 0x01 and 0xd0-0xd7 (NULL, TEM, RST0-RST7)
        } elsif ($marker!=0x00 and $marker!=0x01 and ($marker<0xd0 or $marker>0xd7)) {
            # read record length word
            last unless $raf->Read($s, 2) == 2;
            my $len = unpack('n',$s);   # get data length
            last unless defined($len) and $len >= 2;
            $len -= 2;  # subtract size of length word
            if (($marker & 0xf0) == 0xe0) {  # is this an APP segment?
                my $n = $len < 64 ? $len : 64;
                $raf->Read($s, $n) == $n or last;
                $len -= $n;
                # (Note: only necessary to recognize APP segments that we can create)
                if ($marker == 0xe0) {
                    $s =~ /^JFIF\0/         and $dirName = 'JFIF';
                    $s =~ /^JFXX\0\x10/     and $dirName = 'JFXX';
                } elsif ($marker == 0xe1) {
                    $s =~ /^$exifAPP1hdr/   and $dirName = 'IFD0';
                    $s =~ /^$xmpAPP1hdr/    and $dirName = 'XMP';
                    $s =~ /^$xmpExtAPP1hdr/ and $dirName = 'XMP';
                } elsif ($marker == 0xe2) {
                    $s =~ /^ICC_PROFILE\0/  and $dirName = 'ICC_Profile';
                } elsif ($marker == 0xec) {
                    $s =~ /^Ducky/          and $dirName = 'Ducky';
                } elsif ($marker == 0xed) {
                    $s =~ /^$psAPP13hdr/    and $dirName = 'Photoshop';
                }
                # initialize doneDir as a flag that the directory exists
                # (unless we are deleting it anyway)
                $doneDir{$dirName} = 0 if defined $dirName and not $$delGroup{$dirName};
            }
            $raf->Seek($len, 1) or last;
        }
        $dirName or $dirName = JpegMarkerName($marker);
        $dirCount{$dirName} = ($dirCount{$dirName} || 0) + 1;
        push @dirOrder, $dirName;
    }
    unless ($marker and $marker == 0xda) {
        $self->Error('Corrupted JPEG image');
        return 1;
    }
    $raf->Seek($pos, 0) or $self->Error('Seek error'), return 1;
#
# re-write the image
#
    my ($combinedSegData, $segPos, %extendedXMP);
    # read through each segment in the JPEG file
    Marker: for (;;) {

        # read up to next marker (JPEG markers begin with 0xff)
        my $segJunk;
        $raf->ReadLine($segJunk) or $segJunk = '';
        # remove the 0xff but write the rest of the junk up to this point
        chomp($segJunk);
        Write($outfile, $segJunk) if length $segJunk;
        # JPEG markers can be padded with unlimited 0xff's
        for (;;) {
            $raf->Read($ch, 1) or $self->Error('Format error'), return 1;
            $marker = ord($ch);
            last unless $marker == 0xff;
        }
        # read the segment data
        my $segData;
        # handle SOF markers: SOF0-SOF15, except DHT(0xc4), JPGA(0xc8) and DAC(0xcc)
        if (($marker & 0xf0) == 0xc0 and ($marker == 0xc0 or $marker & 0x03)) {
            last unless $raf->Read($segData, 7) == 7;
        # read data for all markers except stand-alone
        # markers 0x00, 0x01 and 0xd0-0xd7 (NULL, TEM, RST0-RST7)
        } elsif ($marker!=0x00 and $marker!=0x01 and ($marker<0xd0 or $marker>0xd7)) {
            # read record length word
            last unless $raf->Read($s, 2) == 2;
            my $len = unpack('n',$s);   # get data length
            last unless defined($len) and $len >= 2;
            $segPos = $raf->Tell();
            $len -= 2;  # subtract size of length word
            last unless $raf->Read($segData, $len) == $len;
        }
        # initialize variables for this segment
        my $hdr = "\xff" . chr($marker);    # segment header
        my $markerName = JpegMarkerName($marker);
        my $dirName = shift @dirOrder;      # get directory name
        $$path[$pn] = $markerName;
#
# create all segments that must come before this one
# (nothing comes before SOI or after SOS)
#
        while ($markerName ne 'SOI') {
            if (exists $$addDirs{JFIF} and not defined $doneDir{JFIF}) {
                $doneDir{JFIF} = 1;
                if ($verbose) {
                    print $out "Creating APP0:\n";
                    print $out "  Creating JFIF with default values\n";
                }
                my $jfif = "\x01\x02\x01\0\x48\0\x48\0\0";
                SetByteOrder('MM');
                my $tagTablePtr = GetTagTable('Image::ExifTool::JFIF::Main');
                my %dirInfo = (
                    DataPt   => \$jfif,
                    DirStart => 0,
                    DirLen   => length $jfif,
                );
                # must temporarily remove JFIF from DEL_GROUP so we can
                # delete JFIF and add it back again in a single step
                my $delJFIF = $$delGroup{JFIF};
                delete $$delGroup{JFIF};
                my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                $$delGroup{JFIF} = $delJFIF if defined $delJFIF;
                if (defined $newData and length $newData) {
                    my $app0hdr = "\xff\xe0" . pack('n', length($newData) + 7);
                    Write($outfile,$app0hdr,"JFIF\0",$newData) or $err = 1;
                }
            }
            # don't create anything before APP0 or APP1 EXIF (containing IFD0)
            last if $markerName eq 'APP0' or $dirCount{IFD0};
            # EXIF information must come immediately after APP0
            if (exists $$addDirs{IFD0} and not defined $doneDir{IFD0}) {
                $doneDir{IFD0} = 1;
                $verbose and print $out "Creating APP1:\n";
                # write new EXIF data
                $self->{TIFF_TYPE} = 'APP1';
                my $tagTablePtr = GetTagTable('Image::ExifTool::Exif::Main');
                my %dirInfo = (
                    DirName => 'IFD0',
                    Parent  => 'APP1',
                );
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr, \&WriteTIFF);
                if (defined $buff and length $buff) {
                    my $size = length($buff) + length($exifAPP1hdr);
                    if ($size <= $maxSegmentLen) {
                        # switch to buffered output if required
                        if (($$self{PREVIEW_INFO} or $$self{LeicaTrailer}) and not $oldOutfile) {
                            $writeBuffer = '';
                            $oldOutfile = $outfile;
                            $outfile = \$writeBuffer;
                            # account for segment, EXIF and TIFF headers
                            $$self{PREVIEW_INFO}{Fixup}{Start} += 18 if $$self{PREVIEW_INFO};
                            $$self{LeicaTrailer}{Fixup}{Start} += 18 if $$self{LeicaTrailer};
                        }
                        # write the new segment with appropriate header
                        my $app1hdr = "\xff\xe1" . pack('n', $size + 2);
                        Write($outfile,$app1hdr,$exifAPP1hdr,$buff) or $err = 1;
                    } else {
                        delete $self->{PREVIEW_INFO};
                        $self->Warn("EXIF APP1 segment too large! ($size bytes)");
                    }
                }
            }
            # APP13 Photoshop segment next
            last if $dirCount{Photoshop};
            if (exists $$addDirs{Photoshop} and not defined $doneDir{Photoshop}) {
                $doneDir{Photoshop} = 1;
                $verbose and print $out "Creating APP13:\n";
                # write new APP13 Photoshop record to memory
                my $tagTablePtr = GetTagTable('Image::ExifTool::Photoshop::Main');
                my %dirInfo = (
                    Parent => 'APP13',
                );
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                if (defined $buff and length $buff) {
                    WriteMultiSegment($outfile, 0xed, $psAPP13hdr, \$buff) or $err = 1;
                    ++$self->{CHANGED};
                }
            }
            # then APP1 XMP segment
            last if $dirCount{XMP};
            if (exists $$addDirs{XMP} and not defined $doneDir{XMP}) {
                $doneDir{XMP} = 1;
                $verbose and print $out "Creating APP1:\n";
                # write new XMP data
                my $tagTablePtr = GetTagTable('Image::ExifTool::XMP::Main');
                my %dirInfo = (
                    Parent      => 'APP1',
                    # specify MaxDataLen so XMP is split if required
                    MaxDataLen  => $maxXMPLen - length($xmpAPP1hdr),
                );
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                if (defined $buff and length $buff) {
                    WriteMultiXMP($self, $outfile, \$buff, $dirInfo{ExtendedXMP},
                                  $dirInfo{ExtendedGUID}) or $err = 1;
                }
            }
            # then APP2 ICC_Profile segment
            last if $dirCount{ICC_Profile};
            if (exists $$addDirs{ICC_Profile} and not defined $doneDir{ICC_Profile}) {
                $doneDir{ICC_Profile} = 1;
                next if $$delGroup{ICC_Profile} and $$delGroup{ICC_Profile} != 2;
                $verbose and print $out "Creating APP2:\n";
                # write new ICC_Profile data
                my $tagTablePtr = GetTagTable('Image::ExifTool::ICC_Profile::Main');
                my %dirInfo = (
                    Parent   => 'APP2',
                );
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                if (defined $buff and length $buff) {
                    WriteMultiSegment($outfile, 0xe2, "ICC_PROFILE\0", \$buff, 'ICC') or $err = 1;
                    ++$self->{CHANGED};
                }
            }
            # then APP12 Ducky segment
            last if $dirCount{Ducky};
            if (exists $$addDirs{Ducky} and not defined $doneDir{Ducky}) {
                $doneDir{Ducky} = 1;
                $verbose and print $out "Creating APP12 Ducky:\n";
                # write new Ducky segment data
                my $tagTablePtr = GetTagTable('Image::ExifTool::APP12::Ducky');
                my %dirInfo = (
                    Parent   => 'APP12',
                );
                my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                if (defined $buff and length $buff) {
                    my $size = length($buff) + 5;
                    if ($size <= $maxSegmentLen) {
                        # write the new segment with appropriate header
                        my $app12hdr = "\xff\xec" . pack('n', $size + 2);
                        Write($outfile, $app12hdr, 'Ducky', $buff) or $err = 1;
                    } else {
                        $self->Warn("Ducky APP12 segment too large! ($size bytes)");
                    }
                }
            }
            # finally, COM segment
            last if $dirCount{COM};
            if (exists $$addDirs{COM} and not defined $doneDir{COM}) {
                $doneDir{COM} = 1;
                next if $$delGroup{File} and $$delGroup{File} != 2;
                my $newComment = $self->GetNewValues('Comment');
                if (defined $newComment and length($newComment)) {
                    if ($verbose) {
                        print $out "Creating COM:\n";
                        $self->VerboseValue('+ Comment', $newComment);
                    }
                    WriteMultiSegment($outfile, 0xfe, '', \$newComment) or $err = 1;
                    ++$self->{CHANGED};
                }
            }
            last;   # didn't want to loop anyway
        }
        # decrement counter for this directory since we are about to process it
        --$dirCount{$dirName};
#
# rewrite existing segments
#
        # handle SOF markers: SOF0-SOF15, except DHT(0xc4), JPGA(0xc8) and DAC(0xcc)
        if (($marker & 0xf0) == 0xc0 and ($marker == 0xc0 or $marker & 0x03)) {
            $verbose and print $out "JPEG $markerName:\n";
            Write($outfile, $hdr, $segData) or $err = 1;
            next;
        } elsif ($marker == 0xda) {             # SOS
            pop @$path;
            $verbose and print $out "JPEG SOS\n";
            # write SOS segment
            $s = pack('n', length($segData) + 2);
            Write($outfile, $hdr, $s, $segData) or $err = 1;
            my ($buff, $endPos, $trailInfo);
            my $delPreview = $self->{DEL_PREVIEW};
            $trailInfo = IdentifyTrailer($raf) unless $$delGroup{Trailer};
            unless ($oldOutfile or $delPreview or $trailInfo or $$delGroup{Trailer}) {
                # blindly copy the rest of the file
                while ($raf->Read($buff, 65536)) {
                    Write($outfile, $buff) or $err = 1, last;
                }
                $rtnVal = 1;  # success unless we have a file write error
                last;         # all done
            }
            # write the rest of the image (as quickly as possible) up to the EOI
            my $endedWithFF;
            for (;;) {
                my $n = $raf->Read($buff, 65536) or last Marker;
                if (($endedWithFF and $buff =~ m/^\xd9/sg) or
                    $buff =~ m/\xff\xd9/sg)
                {
                    $rtnVal = 1; # the JPEG is OK
                    # write up to the EOI
                    my $pos = pos($buff);
                    Write($outfile, substr($buff, 0, $pos)) or $err = 1;
                    $buff = substr($buff, $pos);
                    last;
                }
                unless ($n == 65536) {
                    $self->Error('JPEG EOI marker not found');
                    last Marker;
                }
                Write($outfile, $buff) or $err = 1;
                $endedWithFF = substr($buff, 65535, 1) eq "\xff" ? 1 : 0;
            }
            # remember position of last data copied
            $endPos = $raf->Tell() - length($buff);
            # rewrite trailers if they exist
            if ($trailInfo) {
                my $tbuf = '';
                $raf->Seek(-length($buff), 1);  # seek back to just after EOI
                $$trailInfo{OutFile} = \$tbuf;  # rewrite the trailer
                $$trailInfo{ScanForAFCP} = 1;   # scan if necessary
                $self->ProcessTrailers($trailInfo) or undef $trailInfo;
            }
            if (not $oldOutfile) {
                # do nothing special
            } elsif ($$self{LeicaTrailer}) {
                my $trailLen;
                if ($trailInfo) {
                    $trailLen = $$trailInfo{DataPos} - $endPos;
                } else {
                    $raf->Seek(0, 2) or $err = 1;
                    $trailLen = $raf->Tell() - $endPos;
                }
                my $fixup = $$self{LeicaTrailer}{Fixup};
                $$self{LeicaTrailer}{TrailPos} = $endPos;
                $$self{LeicaTrailer}{TrailLen} = $trailLen;
                # get _absolute_ position of new Leica trailer
                my $absPos = Tell($oldOutfile) + length($$outfile);
                require Image::ExifTool::Panasonic;
                my $dat = Image::ExifTool::Panasonic::ProcessLeicaTrailer($self, $absPos);
                # allow some junk before Leica trailer (just in case)
                my $junk = $$self{LeicaTrailerPos} - $endPos;
                # set MakerNote pointer and size (subtract 10 for segment and EXIF headers)
                $fixup->SetMarkerPointers($outfile, 'LeicaTrailer', length($$outfile) - 10 + $junk);
                # use this fixup to set the size too (sneaky)
                my $trailSize = defined($dat) ? length($dat) - $junk : $$self{LeicaTrailer}{Size};
                $fixup->{Start} -= 4;  $fixup->{Shift} += 4;
                $fixup->SetMarkerPointers($outfile, 'LeicaTrailer', $trailSize);
                $fixup->{Start} += 4;  $fixup->{Shift} -= 4;
                # clean up and write the buffered data
                $outfile = $oldOutfile;
                undef $oldOutfile;
                Write($outfile, $writeBuffer) or $err = 1;
                undef $writeBuffer;
                if (defined $dat) {
                    Write($outfile, $dat) or $err = 1;  # write new Leica trailer
                    $delPreview = 1;                    # delete existing Leica trailer
                }
            } else {
                # locate preview image and fix up preview offsets
                my $scanLen = $$self{Make} =~ /Sony/i ? 65536 : 1024;
                if (length($buff) < $scanLen) { # make sure we have enough trailer to scan
                    my $buf2;
                    $buff .= $buf2 if $raf->Read($buf2, $scanLen - length($buff));
                }
                # get new preview image position, relative to EXIF base
                my $newPos = length($$outfile) - 10; # (subtract 10 for segment and EXIF headers)
                my $junkLen;
                # adjust position if image isn't at the start (ie. Olympus E-1/E-300)
                if ($buff =~ m/(\xff\xd8\xff.|.\xd8\xff\xdb)/sg) {
                    $junkLen = pos($buff) - 4;
                    # Sony previewimage trailer has a 32 byte header
                    $junkLen -= 32 if $$self{Make} =~/SONY/i and $junkLen > 32;
                    $newPos += $junkLen;
                }
                # fix up the preview offsets to point to the start of the new image
                my $previewInfo = $self->{PREVIEW_INFO};
                delete $self->{PREVIEW_INFO};
                my $fixup = $$previewInfo{Fixup};
                $newPos += ($$previewInfo{BaseShift} || 0);
                # adjust to absolute file offset if necessary (Samsung STMN)
                $newPos += Tell($oldOutfile) + 10 if $$previewInfo{Absolute};
                if ($$previewInfo{Relative}) {
                    # adjust for our base by looking at how far the pointer got shifted
                    $newPos -= $fixup->GetMarkerPointers($outfile, 'PreviewImage');
                } elsif ($$previewInfo{ChangeBase}) {
                    # Leica S2 uses relative offsets for the preview only (leica sucks)
                    my $makerOffset = $fixup->GetMarkerPointers($outfile, 'LeicaTrailer');
                    $newPos -= $makerOffset if $makerOffset;
                }
                $fixup->SetMarkerPointers($outfile, 'PreviewImage', $newPos);
                # clean up and write the buffered data
                $outfile = $oldOutfile;
                undef $oldOutfile;
                Write($outfile, $writeBuffer) or $err = 1;
                undef $writeBuffer;
                # write preview image
                if ($$previewInfo{Data} ne 'LOAD_PREVIEW') {
                    # write any junk that existed before the preview image
                    Write($outfile, substr($buff,0,$junkLen)) or $err = 1 if $junkLen;
                    # write the saved preview image
                    Write($outfile, $$previewInfo{Data}) or $err = 1;
                    delete $$previewInfo{Data};
                    # (don't increment CHANGED because we could be rewriting existing preview)
                    $delPreview = 1;    # remove old preview
                }
            }
            # copy over preview image if necessary
            unless ($delPreview) {
                my $extra;
                if ($trailInfo) {
                    # copy everything up to start of first processed trailer
                    $extra = $$trailInfo{DataPos} - $endPos;
                } else {
                    # copy everything up to end of file
                    $raf->Seek(0, 2) or $err = 1;
                    $extra = $raf->Tell() - $endPos;
                }
                if ($extra > 0) {
                    if ($$delGroup{Trailer}) {
                        $verbose and print $out "  Deleting unknown trailer ($extra bytes)\n";
                        ++$self->{CHANGED};
                    } else {
                        # copy over unknown trailer
                        $verbose and print $out "  Preserving unknown trailer ($extra bytes)\n";
                        $raf->Seek($endPos, 0) or $err = 1;
                        CopyBlock($raf, $outfile, $extra) or $err = 1;
                    }
                }
            }
            # write trailer if necessary
            if ($trailInfo) {
                $self->WriteTrailerBuffer($trailInfo, $outfile) or $err = 1;
                undef $trailInfo;
            }
            last;   # all done parsing file

        } elsif ($marker==0x00 or $marker==0x01 or ($marker>=0xd0 and $marker<=0xd7)) {
            $verbose and $marker and print $out "JPEG $markerName:\n";
            # handle stand-alone markers 0x00, 0x01 and 0xd0-0xd7 (NULL, TEM, RST0-RST7)
            Write($outfile, $hdr) or $err = 1;
            next;
        }
        #
        # NOTE: A 'next' statement after this point will cause $$segDataPt
        #       not to be written if there is an output file, so in this case
        #       the $self->{CHANGED} flags must be updated
        #
        my $segDataPt = \$segData;
        $length = length($segData);
        if ($verbose) {
            print $out "JPEG $markerName ($length bytes):\n";
            if ($verbose > 2 and $markerName =~ /^APP/) {
                HexDump($segDataPt, undef, %dumpParms);
            }
        }
        my ($segType, $del);
        # rewrite this segment only if we are changing a tag which is contained in its
        # directory (or deleting '*', in which case we need to identify the segment type)
        while (exists $$editDirs{$markerName} or $$delGroup{'*'}) {
            my $oldChanged = $self->{CHANGED};
            if ($marker == 0xe0) {              # APP0 (JFIF, CIFF)
                if ($$segDataPt =~ /^JFIF\0/) {
                    $segType = 'JFIF';
                    $$delGroup{JFIF} and $del = 1, last;
                    last unless $$editDirs{JFIF};
                    SetByteOrder('MM');
                    my $tagTablePtr = GetTagTable('Image::ExifTool::JFIF::Main');
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => $segPos,
                        DataLen  => $length,
                        DirStart => 5,     # directory starts after identifier
                        DirLen   => $length-5,
                        Parent   => $markerName,
                    );
                    my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                    if (defined $newData and length $newData) {
                        $$segDataPt = "JFIF\0" . $newData;
                    }
                } elsif ($$segDataPt =~ /^JFXX\0\x10/) {
                    $segType = 'JFXX';
                    $$delGroup{JFIF} and $del = 1;
                } elsif ($$segDataPt =~ /^(II|MM).{4}HEAPJPGM/s) {
                    $segType = 'CIFF';
                    $$delGroup{CIFF} and $del = 1, last;
                    last unless $$editDirs{CIFF};
                    my $newData = '';
                    my %dirInfo = (
                        RAF => new File::RandomAccess($segDataPt),
                        OutFile => \$newData,
                    );
                    require Image::ExifTool::CanonRaw;
                    if (Image::ExifTool::CanonRaw::WriteCRW($self, \%dirInfo) > 0) {
                        if (length $newData) {
                            $$segDataPt = $newData;
                        } else {
                            undef $segDataPt;
                            $del = 1;   # delete this segment
                        }
                    }
                }
            } elsif ($marker == 0xe1) {         # APP1 (EXIF, XMP)
                # check for EXIF data
                if ($$segDataPt =~ /^$exifAPP1hdr/) {
                    $segType = 'EXIF';
                    $doneDir{IFD0} and $self->Warn('Multiple APP1 EXIF segments');
                    $doneDir{IFD0} = 1;
                    last unless $$editDirs{IFD0};
                    # check del groups now so we can change byte order in one step
                    if ($$delGroup{IFD0} or $$delGroup{EXIF}) {
                        delete $doneDir{IFD0};  # delete so we will create a new one
                        $del = 1;
                        last;
                    }
                    # rewrite EXIF as if this were a TIFF file in memory
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => -6, # (remember: relative to Base!)
                        DirStart => 6,
                        Base     => $segPos + 6,
                        Parent   => $markerName,
                        DirName  => 'IFD0',
                    );
                    # write new EXIF data to memory
                    my $tagTablePtr = GetTagTable('Image::ExifTool::Exif::Main');
                    my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr, \&WriteTIFF);
                    if (defined $buff) {
                        # update segment with new data
                        $$segDataPt = $exifAPP1hdr . $buff;
                    } else {
                        last Marker unless $self->Options('IgnoreMinorErrors');
                        $self->{CHANGED} = $oldChanged; # nothing changed
                    }
                    # switch to buffered output if required
                    if (($$self{PREVIEW_INFO} or $$self{LeicaTrailer}) and not $oldOutfile) {
                        $writeBuffer = '';
                        $oldOutfile = $outfile;
                        $outfile = \$writeBuffer;
                        # must account for segment, EXIF and TIFF headers
                        $$self{PREVIEW_INFO}{Fixup}{Start} += 18 if $$self{PREVIEW_INFO};
                        $$self{LeicaTrailer}{Fixup}{Start} += 18 if $$self{LeicaTrailer};
                    }
                    # delete segment if IFD contains no entries
                    $del = 1 unless length($$segDataPt) > length($exifAPP1hdr);
                # check for XMP data
                } elsif ($$segDataPt =~ /^($xmpAPP1hdr|$xmpExtAPP1hdr)/) {
                    $segType = 'XMP';
                    $$delGroup{XMP} and $del = 1, last;
                    $doneDir{XMP} = ($doneDir{XMP} || 0) + 1;
                    last unless $$editDirs{XMP};
                    if ($doneDir{XMP} + $dirCount{XMP} > 1) {
                        # must assemble all XMP segments before writing
                        my ($guid, $extXMP);
                        if ($$segDataPt =~ /^$xmpExtAPP1hdr/) {
                            # save extended XMP data
                            if (length $$segDataPt < 75) {
                                $extendedXMP{Error} = 'Truncated data';
                            } else {
                                my ($size, $off) = unpack('x67N2', $$segDataPt);
                                $guid = substr($$segDataPt, 35, 32);
                                # remember extended data for each GUID
                                $extXMP = $extendedXMP{$guid};
                                if ($extXMP) {
                                    $size == $$extXMP{Size} or $extendedXMP{Error} = 'Invalid size';
                                } else {
                                    $extXMP = $extendedXMP{$guid} = { };
                                }
                                $$extXMP{Size} = $size;
                                $$extXMP{$off} = substr($$segDataPt, 75);
                            }
                        } else {
                            # save all main XMP segments (should normally be only one)
                            $extendedXMP{Main} = [] unless $extendedXMP{Main};
                            push @{$extendedXMP{Main}}, substr($$segDataPt, length $xmpAPP1hdr);
                        }
                        # continue processing only if we have read all the segments
                        next Marker if $dirCount{XMP};
                        # reconstruct an XMP super-segment
                        $$segDataPt = $xmpAPP1hdr;
                        $$segDataPt .= $_ foreach @{$extendedXMP{Main}};
                        foreach $guid (sort keys %extendedXMP) {
                            next unless length $guid == 32;     # ignore other keys
                            $extXMP = $extendedXMP{$guid};
                            next unless ref $extXMP eq 'HASH';  # (just to be safe)
                            my $size = $$extXMP{Size};
                            my (@offsets, $off);
                            for ($off=0; $off<$size; ) {
                                last unless defined $$extXMP{$off};
                                push @offsets, $off;
                                $off += length $$extXMP{$off};
                            }
                            if ($off == $size) {
                                # add all XMP to super-segment
                                $$segDataPt .= $$extXMP{$_} foreach @offsets;
                            } else {
                                $extendedXMP{Error} = 'Missing XMP data';
                            }
                        }
                        $self->Error("$extendedXMP{Error} in extended XMP", 1) if $extendedXMP{Error};
                    }
                    my $start = length $xmpAPP1hdr;
                    my $tagTablePtr = GetTagTable('Image::ExifTool::XMP::Main');
                    my %dirInfo = (
                        DataPt     => $segDataPt,
                        DirStart   => $start,
                        Parent     => $markerName,
                        # limit XMP size and create extended XMP if necessary
                        MaxDataLen => $maxXMPLen - length($xmpAPP1hdr),
                    );
                    my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                    if (defined $newData) {
                        undef %extendedXMP;
                        if (length $newData) {
                            # write multi-segment XMP (XMP plus extended XMP if necessary)
                            WriteMultiXMP($self, $outfile, \$newData, $dirInfo{ExtendedXMP},
                                          $dirInfo{ExtendedGUID}) or $err = 1;
                            undef $$segDataPt;  # free the old buffer
                            next Marker;
                        } else {
                            $$segDataPt = '';   # delete the XMP
                        }
                    } else {
                        $self->{CHANGED} = $oldChanged;
                        $verbose and print $out "    [XMP rewritten with no changes]\n";
                        if ($doneDir{XMP} > 1) {
                            # re-write original multi-segment XMP
                            my ($dat, $guid, $extXMP, $off);
                            foreach $dat (@{$extendedXMP{Main}}) {      # main XMP
                                next unless length $dat;
                                $s = pack('n', length($xmpAPP1hdr) + length($dat) + 2);
                                Write($outfile, $hdr, $s, $xmpAPP1hdr, $dat) or $err = 1;
                            }
                            foreach $guid (sort keys %extendedXMP) {    # extended XMP
                                next unless length $guid == 32;
                                $extXMP = $extendedXMP{$guid};
                                next unless ref $extXMP eq 'HASH';
                                my $size = $$extXMP{Size} or next;
                                for ($off=0; defined $$extXMP{$off}; $off += length $$extXMP{$off}) {
                                    $s = pack('n', length($xmpExtAPP1hdr) + length($$extXMP{$off}) + 42);
                                    Write($outfile, $hdr, $s, $xmpExtAPP1hdr, $guid,
                                          pack('N2', $size, $off), $$extXMP{$off}) or $err = 1;
                                }
                            }
                            undef $$segDataPt;  # free the old buffer
                            undef %extendedXMP;
                            next Marker;
                        }
                        # continue on to re-write original single-segment XMP
                    }
                    $del = 1 unless length $$segDataPt;
                } elsif ($$segDataPt =~ /^http/ or $$segDataPt =~ /<exif:/) {
                    $self->Warn('Ignored APP1 XMP segment with non-standard header', 1);
                }
            } elsif ($marker == 0xe2) {         # APP2 (ICC Profile, FPXR)
                if ($$segDataPt =~ /^ICC_PROFILE\0/) {
                    $segType = 'ICC_Profile';
                    $$delGroup{ICC_Profile} and $del = 1, last;
                    # must concatenate blocks of profile
                    my $block_num = ord(substr($$segDataPt, 12, 1));
                    my $blocks_tot = ord(substr($$segDataPt, 13, 1));
                    $combinedSegData = '' if $block_num == 1;
                    unless (defined $combinedSegData) {
                        $self->Warn('APP2 ICC_Profile segments out of sequence');
                        next Marker;
                    }
                    $combinedSegData .= substr($$segDataPt, 14);
                    # continue accumulating segments unless this is the last
                    next Marker unless $block_num == $blocks_tot;
                    $doneDir{ICC_Profile} and $self->Warn('Multiple ICC_Profile records');
                    $doneDir{ICC_Profile} = 1;
                    $segDataPt = \$combinedSegData;
                    $length = length $combinedSegData;
                    my $tagTablePtr = GetTagTable('Image::ExifTool::ICC_Profile::Main');
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => $segPos + 14,
                        DataLen  => $length,
                        DirStart => 0,
                        DirLen   => $length,
                        Parent   => $markerName,
                    );
                    my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                    if (defined $newData) {
                        undef $$segDataPt;  # free the old buffer
                        $segDataPt = \$newData;
                    }
                    length $$segDataPt or $del = 1, last;
                    # write as ICC multi-segment
                    WriteMultiSegment($outfile, $marker, "ICC_PROFILE\0", $segDataPt, 'ICC') or $err = 1;
                    undef $combinedSegData;
                    undef $$segDataPt;
                    next Marker;
                } elsif ($$segDataPt =~ /^FPXR\0/) {
                    $segType = 'FPXR';
                    $$delGroup{FlashPix} and $del = 1;
                }
            } elsif ($marker == 0xe3) {         # APP3 (Kodak Meta)
                if ($$segDataPt =~ /^(Meta|META|Exif)\0\0/) {
                    $segType = 'Kodak Meta';
                    $$delGroup{Meta} and $del = 1, last;
                    $doneDir{Meta} and $self->Warn('Multiple APP3 Meta segments');
                    $doneDir{Meta} = 1;
                    last unless $$editDirs{Meta};
                    # rewrite Meta IFD as if this were a TIFF file in memory
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => -6, # (remember: relative to Base!)
                        DirStart => 6,
                        Base     => $segPos + 6,
                        Parent   => $markerName,
                        DirName  => 'Meta',
                    );
                    # write new data to memory
                    my $tagTablePtr = GetTagTable('Image::ExifTool::Kodak::Meta');
                    my $buff = $self->WriteDirectory(\%dirInfo, $tagTablePtr, \&WriteTIFF);
                    if (defined $buff) {
                        # update segment with new data
                        $$segDataPt = substr($$segDataPt,0,6) . $buff;
                    } else {
                        last Marker unless $self->Options('IgnoreMinorErrors');
                        $self->{CHANGED} = $oldChanged; # nothing changed
                    }
                    # delete segment if IFD contains no entries
                    $del = 1 unless length($$segDataPt) > 6;
                }
            } elsif ($marker == 0xe5) {         # APP5 (Ricoh RMETA)
                if ($$segDataPt =~ /^RMETA\0/) {
                    $segType = 'Ricoh RMETA';
                    $$delGroup{RMETA} and $del = 1;
                }
            } elsif ($marker == 0xec) {         # APP12 (Ducky)
                if ($$segDataPt =~ /^Ducky/) {
                    $segType = 'Ducky';
                    $$delGroup{Ducky} and $del = 1, last;
                    $doneDir{Ducky} and $self->Warn('Multiple APP12 Ducky segments');
                    $doneDir{Ducky} = 1;
                    last unless $$editDirs{Ducky};
                    my $tagTablePtr = GetTagTable('Image::ExifTool::APP12::Ducky');
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => $segPos,
                        DataLen  => $length,
                        DirStart => 5,     # directory starts after identifier
                        DirLen   => $length-5,
                        Parent   => $markerName,
                    );
                    my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                    if (defined $newData) {
                        undef $$segDataPt;  # free the old buffer
                        # add header to new segment unless empty
                        $newData = 'Ducky' . $newData if length $newData;
                        $segDataPt = \$newData;
                    } else {
                        $self->{CHANGED} = $oldChanged;
                    }
                    $del = 1 unless length $$segDataPt;
                }
            } elsif ($marker == 0xed) {         # APP13 (Photoshop)
                if ($$segDataPt =~ /^$psAPP13hdr/) {
                    $segType = 'Photoshop';
                    # add this data to the combined data if it exists
                    if (defined $combinedSegData) {
                        $combinedSegData .= substr($$segDataPt,length($psAPP13hdr));
                        $segDataPt = \$combinedSegData;
                        $length = length $combinedSegData;  # update length
                    }
                    # peek ahead to see if the next segment is photoshop data too
                    if ($dirOrder[0] eq 'Photoshop') {
                        # initialize combined data if necessary
                        $combinedSegData = $$segDataPt unless defined $combinedSegData;
                        next Marker;    # get the next segment to combine
                    }
                    if ($doneDir{Photoshop}) {
                        $self->Warn('Multiple Photoshop records');
                        # only rewrite the first Photoshop segment when deleting this group
                        # (to remove multiples when deleting and adding back in one step)
                        $$delGroup{Photoshop} and $del = 1, last;
                    }
                    $doneDir{Photoshop} = 1;
                    # process APP13 Photoshop record
                    my $tagTablePtr = GetTagTable('Image::ExifTool::Photoshop::Main');
                    my %dirInfo = (
                        DataPt   => $segDataPt,
                        DataPos  => $segPos,
                        DataLen  => $length,
                        DirStart => 14,     # directory starts after identifier
                        DirLen   => $length-14,
                        Parent   => $markerName,
                    );
                    my $newData = $self->WriteDirectory(\%dirInfo, $tagTablePtr);
                    if (defined $newData) {
                        undef $$segDataPt;  # free the old buffer
                        $segDataPt = \$newData;
                    } else {
                        $self->{CHANGED} = $oldChanged;
                    }
                    length $$segDataPt or $del = 1, last;
                    # write as multi-segment
                    WriteMultiSegment($outfile, $marker, $psAPP13hdr, $segDataPt) or $err = 1;
                    undef $combinedSegData;
                    undef $$segDataPt;
                    next Marker;
                }
            } elsif ($marker == 0xfe) {         # COM (JPEG comment)
                my $newComment;
                unless ($doneDir{COM}) {
                    $doneDir{COM} = 1;
                    unless ($$delGroup{File} and $$delGroup{File} != 2) {
                        my $tagInfo = $Image::ExifTool::Extra{Comment};
                        my $nvHash = $self->GetNewValueHash($tagInfo);
                        if ($self->IsOverwriting($nvHash, $segData) or $$delGroup{File}) {
                            $newComment = $self->GetNewValues($nvHash);
                        } else {
                            delete $$editDirs{COM}; # we aren't editing COM after all
                            last;
                        }
                    }
                }
                $self->VerboseValue('- Comment', $$segDataPt);
                if (defined $newComment and length $newComment) {
                    # write out the comments
                    $self->VerboseValue('+ Comment', $newComment);
                    WriteMultiSegment($outfile, 0xfe, '', \$newComment) or $err = 1;
                } else {
                    $verbose and print $out "  Deleting COM segment\n";
                }
                ++$self->{CHANGED};     # increment the changed flag
                undef $segDataPt;       # don't write existing comment
            }
            last;   # didn't want to loop anyway
        }
        # delete necessary segments (including unknown segments if deleting all)
        if ($del or ($$delGroup{'*'} and not $segType and $marker>=0xe0 and $marker<=0xef)) {
            $segType = 'unknown' unless $segType;
            $verbose and print $out "  Deleting $markerName $segType segment\n";
            ++$self->{CHANGED};
            next Marker;
        }
        # write out this segment if $segDataPt is still defined
        if (defined $segDataPt) {
            # write the data for this record (the data could have been
            # modified, so recalculate the length word)
            my $size = length($$segDataPt);
            if ($size > $maxSegmentLen) {
                $segType or $segType = 'Unknown';
                $self->Error("$segType $markerName segment too large! ($size bytes)");
                $err = 1;
            } else {
                $s = pack('n', length($$segDataPt) + 2);
                Write($outfile, $hdr, $s, $$segDataPt) or $err = 1;
            }
            undef $$segDataPt;  # free the buffer
        }
    }
    pop @$path if @$path > $pn;
    # if oldOutfile is still set, there was an error copying the JPEG
    $oldOutfile and return 0;
    if ($rtnVal) {
        # add any new trailers we are creating
        my $trailPt = $self->AddNewTrailers();
        Write($outfile, $$trailPt) or $err = 1 if $trailPt;
    }
    # set return value to -1 if we only had a write error
    $rtnVal = -1 if $rtnVal and $err;
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Validate an image for writing
# Inputs: 0) ExifTool object reference, 1) raw value reference
# Returns: error string or undef on success
sub CheckImage($$)
{
    my ($self, $valPtr) = @_;
    if (length($$valPtr) and $$valPtr!~/^\xff\xd8/ and not
        $self->Options('IgnoreMinorErrors'))
    {
        return '[minor] Not a valid image';
    }
    return undef;
}

#------------------------------------------------------------------------------
# check a value for validity
# Inputs: 0) value reference, 1) format string, 2) optional count
# Returns: error string, or undef on success
# Notes: May modify value (if a count is specified for a string, it is null-padded
# to the specified length, and floating point values are rounded to integer if required)
sub CheckValue($$;$)
{
    my ($valPtr, $format, $count) = @_;
    my (@vals, $val, $n);

    if ($format eq 'string' or $format eq 'undef') {
        return undef unless $count and $count > 0;
        my $len = length($$valPtr);
        if ($format eq 'string') {
            $len >= $count and return 'String too long';
        } else {
            $len > $count and return 'Data too long';
        }
        if ($len < $count) {
            $$valPtr .= "\0" x ($count - $len);
        }
        return undef;
    }
    if ($count and $count != 1) {
        @vals = split(' ',$$valPtr);
        $count < 0 and ($count = @vals or return undef);
    } else {
        $count = 1;
        @vals = ( $$valPtr );
    }
    if (@vals != $count) {
        my $str = @vals > $count ? 'Too many' : 'Not enough';
        return "$str values specified ($count required)";
    }
    for ($n=0; $n<$count; ++$n) {
        $val = shift @vals;
        if ($format =~ /^int/) {
            # make sure the value is integer
            unless (IsInt($val)) {
                if (IsHex($val)) {
                    $val = $$valPtr = hex($val);
                } else {
                    # round single floating point values to the nearest integer
                    return 'Not an integer' unless IsFloat($val) and $count == 1;
                    $val = $$valPtr = int($val + ($val < 0 ? -0.5 : 0.5));
                }
            }
            my $rng = $intRange{$format} or return "Bad int format: $format";
            return "Value below $format minimum" if $val < $$rng[0];
            # (allow 0xfeedfeed code as value for 16-bit pointers)
            return "Value above $format maximum" if $val > $$rng[1] and $val != 0xfeedfeed;
        } elsif ($format =~ /^rational/ or $format eq 'float' or $format eq 'double') {
            # make sure the value is a valid floating point number
            unless (IsFloat($val)) {
                # allow 'inf', 'undef' and fractional rational values
                if ($format =~ /^rational/) {
                    next if $val eq 'inf' or $val eq 'undef';
                    if ($val =~ m{^([-+]?\d+)/(\d+)$}) {
                        next unless $1 < 0 and $format =~ /u$/;
                        return 'Must be an unsigned rational';
                    }
                }
                return 'Not a floating point number' 
            }
            if ($format =~ /^rational\d+u$/ and $val < 0) {
                return 'Must be a positive number';
            }
        }
    }
    return undef;   # success!
}

#------------------------------------------------------------------------------
# check new value for binary data block
# Inputs: 0) ExifTool object ref, 1) tagInfo hash ref, 2) raw value ref
# Returns: error string or undef (and may modify value) on success
sub CheckBinaryData($$$)
{
    my ($self, $tagInfo, $valPtr) = @_;
    my $format = $$tagInfo{Format};
    unless ($format) {
        my $table = $$tagInfo{Table};
        if ($table and $$table{FORMAT}) {
            $format = $$table{FORMAT};
        } else {
            # use default 'int8u' unless specified
            $format = 'int8u';
        }
    }
    my $count;
    if ($format =~ /(.*)\[(.*)\]/) {
        $format = $1;
        $count = $2;
        # can't evaluate $count now because we don't know $size yet
        undef $count if $count =~ /\$size/;
    }
    return CheckValue($valPtr, $format, $count);
}

#------------------------------------------------------------------------------
# Copy data block from RAF to output file in max 64kB chunks
# Inputs: 0) RAF ref, 1) outfile ref, 2) block size
# Returns: 1 on success, 0 on read error, undef on write error
sub CopyBlock($$$)
{
    my ($raf, $outfile, $size) = @_;
    my $buff;
    for (;;) {
        last unless $size > 0;
        my $n = $size > 65536 ? 65536 : $size;
        $raf->Read($buff, $n) == $n or return 0;
        Write($outfile, $buff) or return undef;
        $size -= $n;
    }
    return 1;
}

#------------------------------------------------------------------------------
# copy image data from one file to another
# Inputs: 0) ExifTool object reference
#         1) reference to list of image data [ position, size, pad bytes ]
#         2) output file ref
# Returns: true on success
sub CopyImageData($$$)
{
    my ($self, $imageDataBlocks, $outfile) = @_;
    my $raf = $self->{RAF};
    my ($dataBlock, $err);
    my $num = @$imageDataBlocks;
    $self->VPrint(0, "  Copying $num image data blocks\n") if $num;
    foreach $dataBlock (@$imageDataBlocks) {
        my ($pos, $size, $pad) = @$dataBlock;
        $raf->Seek($pos, 0) or $err = 'read', last;
        my $result = CopyBlock($raf, $outfile, $size);
        $result or $err = defined $result ? 'read' : 'writ';
        # pad if necessary
        Write($outfile, "\0" x $pad) or $err = 'writ' if $pad;
        last if $err;
    }
    if ($err) {
        $self->Error("Error ${err}ing image data");
        return 0;
    }
    return 1;
}

#------------------------------------------------------------------------------
# write to binary data block
# Inputs: 0) ExifTool object ref, 1) source dirInfo ref, 2) tag table ref
# Returns: Binary data block or undefined on error
sub WriteBinaryData($$$)
{
    my ($self, $dirInfo, $tagTablePtr) = @_;
    $self or return 1;    # allow dummy access to autoload this package

    # get default format ('int8u' unless specified)
    my $dataPt = $$dirInfo{DataPt} or return undef;
    my $defaultFormat = $$tagTablePtr{FORMAT} || 'int8u';
    my $increment = FormatSize($defaultFormat);
    unless ($increment) {
        warn "Unknown format $defaultFormat\n";
        return undef;
    }
    # extract data members first if necessary
    my @varOffsets;
    if ($$tagTablePtr{DATAMEMBER}) {
        $$dirInfo{DataMember} = $$tagTablePtr{DATAMEMBER};
        $$dirInfo{VarFormatData} = \@varOffsets;
        $self->ProcessBinaryData($dirInfo, $tagTablePtr);
        delete $$dirInfo{DataMember};
        delete $$dirInfo{VarFormatData};
    }
    my $dirStart = $$dirInfo{DirStart} || 0;
    my $dirLen = $$dirInfo{DirLen} || length($$dataPt) - $dirStart;
    my $newData = substr($$dataPt, $dirStart, $dirLen) or return undef;
    my $dirName = $$dirInfo{DirName};
    my $varSize = 0;
    my @varInfo = @varOffsets;
    my $tagInfo;
    $dataPt = \$newData;
    foreach $tagInfo ($self->GetNewTagInfoList($tagTablePtr)) {
        my $tagID = $tagInfo->{TagID};
        # evaluate conditional tags now if necessary
        if (ref $$tagTablePtr{$tagID} eq 'ARRAY' or $$tagInfo{Condition}) {
            my $writeInfo = $self->GetTagInfo($tagTablePtr, $tagID);
            next unless $writeInfo and $writeInfo eq $tagInfo;
        }
        # add offsets for variable-sized tags if necessary
        while (@varInfo and $varInfo[0] < $tagID) {
            shift @varInfo;             # discard index
            $varSize = shift @varInfo;  # get accumulated variable size
        }
        my $count = 1;
        my $format = $$tagInfo{Format};
        my $entry = int($tagID) * $increment + $varSize; # relative offset of this entry
        if ($format) {
            if ($format =~ /(.*)\[(.*)\]/) {
                $format = $1;
                $count = $2;
                my $size = $dirLen; # used in eval
                # evaluate count to allow count to be based on previous values
                #### eval Format size ($size, $self) - NOTE: %val not supported for writing
                $count = eval $count;
                $@ and warn($@), next;
            } elsif ($format eq 'string') {
                # string with no specified count runs to end of block
                $count = ($dirLen > $entry) ? $dirLen - $entry : 0;
            }
        } else {
            $format = $defaultFormat;
        }
        my $val = ReadValue($dataPt, $entry, $format, $count, $dirLen-$entry);
        next unless defined $val;
        my $nvHash = $self->GetNewValueHash($tagInfo);
        next unless $self->IsOverwriting($nvHash, $val);
        my $newVal = $self->GetNewValues($nvHash);
        next unless defined $newVal;    # can't delete from a binary table
        # only write masked bits if specified
        my $mask = $$tagInfo{Mask};
        $newVal = ($newVal & $mask) | ($val & ~$mask) if defined $mask;
        # set the size
        if ($$tagInfo{DataTag} and not $$tagInfo{IsOffset}) {
            warn 'Internal error' unless $newVal == 0xfeedfeed;
            my $data = $self->GetNewValues($$tagInfo{DataTag});
            $newVal = length($data) if defined $data;
            my $format = $$tagInfo{Format} || $$tagTablePtr{FORMAT} || 'int32u';
            if ($format =~ /^int16/ and $newVal > 0xffff) {
                $self->Error("$$tagInfo{DataTag} is too large (64 kB max. for this file)");
            }
        }
        my $rtnVal = WriteValue($newVal, $format, $count, $dataPt, $entry);
        if (defined $rtnVal) {
            $self->VerboseValue("- $dirName:$$tagInfo{Name}", $val);
            $self->VerboseValue("+ $dirName:$$tagInfo{Name}", $newVal);
            ++$self->{CHANGED};
        }
    }
    # add necessary fixups for any offsets
    if ($$tagTablePtr{IS_OFFSET} and $$dirInfo{Fixup}) {
        $varSize = 0;
        @varInfo = @varOffsets;
        my $fixup = $$dirInfo{Fixup};
        my $tagID;
        foreach $tagID (@{$tagTablePtr->{IS_OFFSET}}) {
            $tagInfo = $self->GetTagInfo($tagTablePtr, $tagID) or next;
            while (@varInfo and $varInfo[0] < $tagID) {
                shift @varInfo;
                $varSize = shift @varInfo;
            }
            my $entry = $tagID * $increment + $varSize; # (no offset to dirStart for new dir data)
            next unless $entry <= $dirLen - 4;
            # (Ricoh has 16-bit preview image offsets, so can't just assume int32u)
            my $format = $$tagInfo{Format} || $$tagTablePtr{FORMAT} || 'int32u';
            my $offset = ReadValue($dataPt, $entry, $format, 1, $dirLen-$entry);
            # ignore if offset is zero (ie. Ricoh DNG uses this to indicate no preview)
            next unless $offset;
            $fixup->AddFixup($entry, $$tagInfo{DataTag}, $format);
            # handle the preview image now if this is a JPEG file
            next unless $self->{FILE_TYPE} eq 'JPEG' and $$tagInfo{DataTag} and
                $$tagInfo{DataTag} eq 'PreviewImage' and defined $$tagInfo{OffsetPair};
            # NOTE: here we assume there are no var-sized tags between the
            # OffsetPair tags.  If this ever becomes possible we must recalculate
            # $varSize for the OffsetPair tag here!
            $entry = $$tagInfo{OffsetPair} * $increment + $varSize;
            my $size = ReadValue($dataPt, $entry, $format, 1, $dirLen-$entry);
            my $previewInfo = $self->{PREVIEW_INFO};
            $previewInfo or $previewInfo = $self->{PREVIEW_INFO} = { };
            # set flag indicating we are using short pointers
            $$previewInfo{IsShort} = 1 unless $format eq 'int32u';
            $$previewInfo{Absolute} = 1 if $$tagInfo{IsOffset} and $$tagInfo{IsOffset} eq '3';
            # get the value of the Composite::PreviewImage tag
            $$previewInfo{Data} = $self->GetNewValues($Image::ExifTool::Composite{PreviewImage});
            unless (defined $$previewInfo{Data}) {
                if ($offset >= 0 and $offset + $size <= $$dirInfo{DataLen}) {
                    $$previewInfo{Data} = substr(${$$dirInfo{DataPt}},$offset,$size);
                } else {
                    $$previewInfo{Data} = 'LOAD_PREVIEW'; # flag to load preview later
                }
            }
        }
    }
    # write any necessary SubDirectories
    if ($$tagTablePtr{IS_SUBDIR}) {
        $varSize = 0;
        @varInfo = @varOffsets;
        my $tagID;
        foreach $tagID (@{$$tagTablePtr{IS_SUBDIR}}) {
            my $tagInfo = $self->GetTagInfo($tagTablePtr, $tagID);
            next unless defined $tagInfo;
            while (@varInfo and $varInfo[0] < $tagID) {
                shift @varInfo;
                $varSize = shift @varInfo;
            }
            my $entry = int($tagID) * $increment + $varSize;
            last if $entry >= $dirLen;
            # get value for Condition if necessary
            unless ($tagInfo) {
                my $more = $dirLen - $entry;
                $more = 128 if $more > 128;
                my $v = substr($newData, $entry, $more);
                $tagInfo = $self->GetTagInfo($tagTablePtr, $tagID, \$v);
                next unless $tagInfo;
            }
            next unless $$tagInfo{SubDirectory}; # (just to be safe)
            my %subdirInfo = ( DataPt => \$newData, DirStart => $entry );
            my $subTablePtr = GetTagTable($tagInfo->{SubDirectory}{TagTable});
            my $dat = $self->WriteDirectory(\%subdirInfo, $subTablePtr);
            substr($newData, $entry) = $dat if defined $dat and length $dat;
        }
    }
    return $newData;
}

#------------------------------------------------------------------------------
# Write TIFF as a directory
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
# Returns: New directory data or undefined on error
sub WriteTIFF($$$)
{
    my ($self, $dirInfo, $tagTablePtr) = @_;
    my $buff = '';
    $$dirInfo{OutFile} = \$buff;
    return $buff if $self->ProcessTIFF($dirInfo, $tagTablePtr) > 0;
    return undef;
}

1; # end

__END__

=head1 NAME

Image::ExifTool::Writer.pl - ExifTool routines for writing meta information

=head1 SYNOPSIS

These routines are autoloaded by Image::ExifTool when required.

=head1 DESCRIPTION

This module contains ExifTool write routines and other infrequently
used routines.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 SEE ALSO

L<Image::ExifTool(3pm)|Image::ExifTool>

=cut
