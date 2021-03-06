#!/usr/bin/perl -w

use Cwd;
use File::Path;
use XML::Simple;
use Data::Dumper;
use Time::Interval;
use File::Copy;
use Template;
use POSIX qw(strftime);
use IO::Handle;
use POSIX qw(O_CREAT);

# TODO NOTES
#   Wrap configuration behind an object?
#   It should be able to generate out an email report too. 
#   Or is this just a text version of the front page?
# config needs an email address in it for broken builds and nightly reports.
# warning system. Or should this use PMD/Checkstyle?

sub usage() {
    print "Usage:\n";
    print " ./integrate.pl foo-integration.xml all               -  Will build every component specified in the given xml file. \n";
    print " ./integrate.pl foo-integration.xml all <projects> -  Will build only the components specified. \n";
    print " ./integrate.pl foo-integration.xml update            -  Will build only components that have had a changed file since the last update. \n";
    print " ./integrate.pl foo-integration.xml update <projects> -  Will build only the components specified. \n";
    exit 1;
}

# repository methods
sub checkout {
    my ($repository, $project, $tag, $directory)  = @_;
    if($repository =~ /svn:/) {
        if($tag) {
            # also change the repository over to releases
            $tag = '-'.$tag;
        } else {
            $tag = '';
        }
        $repository = substr( $repository, 4 );
        system "svn co $repository/$project $directory";
    } 
    elsif($repository =~ /cvs:/) {
        if($tag) {
            $tag = '-r '.$tag;
        } else {
            $tag = '';
        }
        $repository = substr( $repository, 3 );
        system "cvs -qd $repository co $tag -d $directory $project";
    } else {
        warn "Attempt to use unknown type of repository. \n";
    }
}

sub update {
    my ($repository, $directory)  = @_;
    $previousDirectory = getcwd();
    chdir $directory;
    if($repository =~ /svn:/) {
        $repository = substr( $repository, 4 );
        $update_info = `svn update | grep -v 'At revision'`;
    } 
    elsif($repository =~ /cvs:/) {
        $repository = substr( $repository, 3 );
        $update_info = `cvs -q update -dP | grep -v '^[?MA]'`;
    } else {
        warn "Attempt to use unknown type of repository. \n";
    }
    chdir $previousDirectory;
    return $update_info;
}
# end of repository methods

sub xdoc2html {
    my ($source, $target)  = @_;
    open(SOURCE, $source);
    open(TARGET, ">$target");
    while(<SOURCE>) {
       s/document\>/html\>/;
       s/properties\>/head\>/;
       s/\<\/section\>//;
       s/\<\/subsection\>//;
       s/\<section name="([^"]*)"/\<h1\>$1\<\/h1/;
       s/\<subsection name="([^"]*)"/\<h2\>$1\<\/h2/;
       print TARGET $_;
    }
}

if(@ARGV < 2) {
    usage();
}

my $conf_filename = $ARGV[0];
my $action = $ARGV[1];

my @filter = ();
if(@ARGV > 2) {
    @filter = @ARGV[2 .. $#ARGV];
}

# This is pretty ugly, need to pass CONF_FILE to XMLin somehow.
open(CONF_FILE, $conf_filename) or die("Unable to open xml configuration: $!");
close(CONF_FILE);

my $conf = XMLin($conf_filename, forcearray=>1);
#NOT YET#$global_repository = $conf->{'buildables'}->{'repository'};
$reportDirectory = $conf->{'site'}[0]->{'target'};
@projects = @{$conf->{'buildables'}[0]->{'buildable'}};

my @build_list = ();

# action may be 'all', 'update'

# loop over each component in the xml
    foreach $buildable (@projects) {
        $project = $buildable->{'project'};

        $repository = $buildable->{'repository'};
        $tag = $buildable->{'tag'};
        if(not $tag) {
            $directory = $project;
        } else {
            $directory = ${project}. '_' . $tag;
        }
        $buildable->{'directory'} = $directory;
        $buildable->{'escapedDirectory'} = $directory;
        $buildable->{'escapedDirectory'} =~ s/\//_/g;

        if(@filter) {
            unless(grep (/$project/, @filter)) {
                next;
            }
        }

        # is component there?
        if(-e $directory) {
            if($action eq 'all' or $action eq 'update') {
                # yes: do update check. 
                print "Updating $directory\n";
                $updated = update($repository, $directory);
                $buildable->{'updated'} = $updated;

                # if all:
                if($action eq 'all') {
                    # put in list of ones to build - build-list
                    push @build_list, $buildable;
                }
                elsif($action eq 'update' and $updated) {
                    # put in list of ones to build - build-list
                    push @build_list, $buildable;
                }
            }
        } else {
            print "Checking out $directory\n";
            # no: checkout
            checkout($repository, $project, $tag, $directory);
            # put in list of ones to build - build-list
            push @build_list, $buildable;
        }
    }


# check if these are really needed
    unlink('LAST_BUILD');
    unlink('SCM_UPDATE');
    unlink('REASON');
    mkpath($reportDirectory);

    my $failure = 0;

    # loop over each component in build-list
    foreach $buildable (@build_list) {
        if( not -e $buildable->{'directory'} ) {
            warn "Unable to build $buildable->{'project'} as not checked out. ";
            chdir $previousDirectory;
            next;
        }
        $previousDirectory = getcwd();
        chdir $buildable->{'directory'};
        if( not -e 'project.xml' ) {
            warn "Unable to build $buildable->{'project'} as not mavenised. ";
            chdir $previousDirectory;
            next;
        }

        unlink 'maven.log', 'velocity.log', 'ERROR.log', 'OUTPUT.log', 'DOCS-ERROR.log', 'DOCS-OUTPUT.log';
        print "Building $buildable->{'project'} in $buildable->{'directory'}/\n";

# Build the code
        $buildable->{'build_attempted'} = 1;
        # install a snapshot jar
        system("maven -b clean jar:install-snapshot clean jar 2> /dev/null > /dev/null");
        # build the main jar - record build-time
        my $start = time();
        system("maven -b clean jar 2> ERROR.log > OUTPUT.log");
        my $end = time();
        $buildable->{'duration'} = parseInterval(seconds => $end - $start, String => 1);

        $failure = $? >> 8;
        $buildable->{'failed'} = $failure;


# build the site - maven + ttk
        print "Building documentation\n";
        if( not -e 'LICENSE.txt' ) {
            sysopen(FH, "LICENSE.txt", O_CREAT);
            close(FH);
        }
        system("maven -b maven-simian-plugin maven-jdepend-plugin:report maven-jcoverage-plugin:report checkstyle maven-junit-report-plugin:report maven-jxr-plugin:report pmd javadoc 2> DOCS-ERROR.log > DOCS-OUTPUT.log");
        system("maven -b javadoc pmd checkstyle maven-jxr-plugin:report maven-junit-report-plugin:report 2>> DOCS-ERROR.log >> DOCS-OUTPUT.log");
        # report build-time, build-date, update-reason

        my $reportDir = "${previousDirectory}/${reportDirectory}/$buildable->{'directory'}";

# Pisses SVN off when the .svn files go away
#        rmtree($reportDir);
`find $reportDir -type f | grep -v '\.svn' | xargs rm`;
`find $reportDir -type d | grep -v '\.svn' | xargs rmdir 2>&1 | grep -v 'Directory not empty'`;
        mkpath($reportDir);

        copy "ERROR.log", "$reportDir/";

# TODO: Rewrite these to not need things being deleted
        # deploy reports
##        move "target/docs/apidocs/", "$reportDir/apidocs/";
##        move "target/docs/jcoverage/", "$reportDir/jcoverage/";
##        move "target/docs/xref/", "$reportDir/xref/";
##        move "target/docs/xref-test/", "$reportDir/xref-test/";
## TMP: Port this to Perl
        if(-d "target/docs/apidocs/") {
            `mkdir -p $reportDir/apidocs/`;
            `cp -r target/docs/apidocs/* $reportDir/apidocs/`;
        }
        if(-d "target/docs/jcoverage/") {
            `mkdir -p $reportDir/jcoverage/`;
            `cp -r target/docs/jcoverage/* $reportDir/jcoverage/`;
        }
        if(-d "target/docs/xref/") {
            `mkdir -p $reportDir/xref/`;
            `cp -r target/docs/xref/* $reportDir/xref/`;
        }
        if(-d "target/docs/xref-test/") {
            `mkdir -p $reportDir/xref-test/`;
            `cp -r target/docs/xref-test/* $reportDir/xref-test/`;
        }

        move "target/generated-xdocs/simian-report.xml", $reportDir;
        move "target/generated-xdocs/pmd-report.xml", $reportDir;
        move "target/generated-xdocs/jdepend-report.xml", $reportDir;
# TODO: Search and replace the image in the checkstyle-report
        move "target/generated-xdocs/checkstyle-report.xml", $reportDir;
        move "target/generated-xdocs/checkstyle.rss", $reportDir;
# TODO: Search and replace the image in the junit-report
        move "target/generated-xdocs/junit-report.xml", $reportDir;

        # deploy targets

## Needs to get smarter.
#    DEPLOYABLES=`ls -1 target/*.?ar 2>/dev/null`
##    rm -f $reportDir/$i/builds/*.jar
#    DT=`date +%Y%m%d`
#    for t in $DEPLOYABLES
#    do
#        mkdir -p $reportDir/$i/builds/
#        file_name=`echo $t | sed 's/target\///' | sed 's/\.[^\.]*ar//'`
#        file_ext=`echo $t | sed 's/target\///' | sed 's/.*\.//g'`
#        mv $t $reportDir/$i/builds/${file_name}-$DT.${file_ext}
#    done
#
#    date +"%Y/%m/%d %k:%M" > $reportDir/$i/BUILD_TIME
#    if [ -e $buildDir/SCM_UPDATE ];
#    then
#        cat $buildDir/SCM_UPDATE | grep "$i " > $reportDir/$i/REASON
#    fi
#    if [ -e $buildDir/REASON ];
#    then
#        cat $buildDir/REASON > $reportDir/$i/REASON
#    fi
#    echo $BUILD_DURATION > $reportDir/$i/BUILD_DURATION
#
#    echo "Finished with $i"
#    cd -
#done

        chdir $previousDirectory;
    }

    foreach $buildable (@projects) {
        # see if it was a failure
        my $failure = $buildable->{'failed'};
        if(not $failure) {
            if(-e "$buildable->{'directory'}/ERROR.log") {
                # search the error log to see if it contains certain phrases
                open(ERROR_FILE, "$buildable->{'directory'}/ERROR.log");
                while(<ERROR_FILE>) {
                    if( /BUILD FAILED|build cannot continue/ ) {
# seems to then grep for [ERROR] in OUTPUT.log and put this in ERROR.log
                        $failure = 1;
                    }
                }
                close(ERROR_FILE);
            }
        }
        if($failure) {
            if($buildable->{'build_attempted'}) {
                print "Failed to build $buildable->{'project'}\n";
                if($action eq 'update') {
                    `cat $buildable->{'directory'}/ERROR.log | mail -s 'Failed to build $buildable->{'project'}' bayard\@osjava.org`;
                }
            }
            $buildable->{'failed'} = "There were errors in building $buildable->{'project'}"; 
        }
    }

$timestamp = strftime("%Y/%m/%e %H:%M", localtime());


# REPORT SYSTEM
    if(@build_list) {
        print "Building front page\n";
        my $tt = Template->new();
        my $input = 'templates/group.tt';
        my $vars = {
            projects => \@projects,
            site => $conf->{'site'}[0],
            timestamp => $timestamp,
        };
        $tt->process($input, $vars, "$reportDirectory/index.html") || die $tt->error();

        foreach $buildable (@build_list) {
            print "Building report for $buildable->{'project'}\n";

            $directory = "$buildable->{'directory'}";
      
            # create report pages from xml files
            my %reports;
            my @xml_reports = ('checkstyle', 'pmd', 'junit', 'simian', 'jdepend');
      
            foreach $xml_report (@xml_reports) {
                if( -e "$reportDirectory/$directory/${xml_report}-report.xml" ) {
                    xdoc2html("$reportDirectory/$directory/${xml_report}-report.xml", "$reportDirectory/$directory/${xml_report}-report.html");
                    $reports{$xml_report} = "$directory/${xml_report}-report.html";
                }
            }
      
            my %sub_sites = ('apidocs' => 'javadoc', 'xref' => 'source', 'jcoverage' => 'test coverage', 'builds' => 'builds');
            while( ($key, $value) = each %sub_sites) {
                if(-e "$reportDirectory/$directory/$key") {
                    $reports{$value} = "$directory/$key";
                }
            }
      
            # generate a report page, with links if targets of said links are available
            # TODO: Make the links unactive if not available, rather than not there
            my $input = 'templates/project.tt';
            my $vars = {
                project => $buildable,
                site => $conf->{'site'}[0],
                timestamp => $timestamp,
                reports => \%reports,
                build_reason => $action,
                error_log_filename => "$reportDirectory/".$buildable->{'directory'}."/ERROR.log",
            };
            $tt->process($input, $vars, "$reportDirectory/report_".$buildable->{'escapedDirectory'}.".html") || die $tt->error();
        }
    }

exit (scalar @build_list != 0);
